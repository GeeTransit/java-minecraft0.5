/*
George Zhang
World view class.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.joml.Vector3f;
import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class World implements Loopable, AutoCloseable {
	public static final float BLOCK_SCALE = 0.5f;  // block scaling (mesh is 2x2x2)
	public static final float BLOCK_RADIUS = 2f;  // radius around block (for frustum culling)

	private Shader shader;
	private final Camera camera;
	private final ClosestItem<BlockItem> closest;

	private final Map<String, List<BlockItem>> blocks;  // type -> list<block>
	private final Map<String, Mesh> meshes;  // type -> mesh
	private final Set<String> transparentNames;  // type (if exists, transparent)
	private final List<BlockItem> transparentBlocks;  // list<block> (ordered)

	public World(Camera camera) {
		this.camera = camera;
		this.closest = new ClosestItem<>();

		this.blocks = new HashMap<>();
		this.meshes = new HashMap<>();
		this.transparentNames = new HashSet<>();
		this.transparentBlocks = new ArrayList<>();
	}

	@Override
	public void init(Window window) {
		this.createShader();

		// add block types
		this.addFullType("grassblock", ObjLoader.loadMesh("/res/cube-fblr,u,d.obj", "/res/grassblock.png"));
		this.addFullType("cobbleblock", ObjLoader.loadMesh("/res/cube-fblrud.obj", "/res/cobbleblock.png"));
		this.addTransparentType("glassblock", ObjLoader.loadMesh("/res/cube-fblrud.obj", "/res/glassblock.png"));

		// build world
		this.buildSpawn("grassblock", "cobbleblock");
		try (Image image = new Image("/res/heightmap.png")) {
			// heightmap
			this.buildSimple(image, "grassblock", "cobbleblock");
		}
	}

	@Override
	public void update(float interval) {
		// best to leave it up to others whether they want to update or not
	}

	@Override
	public void render(Window window) {
		this.updateTransparent();
		this.updateVisible();
		this.updateSelected();
		this.renderWorld(window);
	}

	@Override
	public void cleanup() {
		this.close();
	}

	public void createShader() {
		this.shader = new Shader();
		this.shader.compileVertex(Utils.loadResource("/res/vertex-3d.vs"));
		this.shader.compileFragment(Utils.loadResource("/res/fragment-3d-block.fs"));
		this.shader.link();

		this.shader.create("projectionMatrix");
		this.shader.create("modelViewMatrix");
		this.shader.create("texture_sampler");
		this.shader.create("color");
		this.shader.create("isTextured");
		this.shader.create("isSelected");
	}

	public void buildSimple(Image image, String top, String bottom) {
		// create terrain
		//     T
		//   T B
		// T B B
		// B B B
		// B B
		for (int x = 0; x < image.width; x++) {
			for (int z = 0; z < image.length; z++) {
				int y = (int) Image.compressExpand(image.pixel(x, z), 0, Image.MAX, 0, 16);
				this.setBlock(top, x, y, z);
				for (int k = y-1; k >= Math.max(y-2, 0); k--)
					this.setBlock(bottom, x, k, z);
			}
		}
	}

	public void buildSpawn(String normal, String left) {
		// add spawn markers (-2z is forwards, cobble is left)
		//   N
		//
		// L   N
		//   N
		this.setBlock(normal, +1,  0,  0);
		this.setBlock(left,   -1,  0,  0);
		this.setBlock(normal,  0,  0, +1);
		this.setBlock(normal,  0,  0, -2);
	}

	public void updateTransparent() {
		// reorder transparent blocks
		Vector3f position = this.camera.getPosition();
		this.transparentBlocks.sort(Comparator.<BlockItem>comparingDouble(
			block -> -block.getPosition().distance(position)
		));
	}

	public void updateVisible() {
		// update visible blocks
		for (BlockItem block : this.iterBlocks())
			block.setVisible(this.camera.insideFrustum(block.getPosition(), BLOCK_RADIUS*block.getScale()));
	}

	public void updateSelected() {
		// update selected block
		for (BlockItem block : this.iterBlocks())
			block.setSelected(false);
		this.closest.update(this.iterBlocks(), this.camera);
		if (this.closest.closest != null)
			this.closest.closest.setSelected(true);
	}

	public void renderWorld(Window window) {
		this.shader.bind();
		this.shader.set("texture_sampler", 0);
		this.shader.set("projectionMatrix", window.getProjectionMatrix());
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);

		Matrix4f viewMatrix = this.camera.getViewMatrix();  // view matrix
		Matrix4f temp = new Matrix4f();  // temporary matrix (stores model view matrix)
		BiConsumer<Shader, BlockItem> setup = (shader, block) -> {
			block.buildModelViewMatrix(viewMatrix, temp);
			shader.set("modelViewMatrix", temp);
			shader.set("isSelected", block.isSelected());
		};

		// opaque blocks (loop by type)
		for (Map.Entry<String, List<BlockItem>> entry : this.blocks.entrySet()) {
			Mesh mesh = this.meshes.get(entry.getKey());
			Stream<BlockItem> blocks = entry.getValue().stream().filter(block -> block.isVisible());
			mesh.render(this.shader, blocks, setup);
		}

		// transparent blocks (loop individually)
		for (BlockItem block : this.transparentBlocks) {
			Mesh mesh = block.getMesh();
			if (block.isVisible())
				mesh.render(this.shader, block, setup);
		}

		glDisable(GL_CULL_FACE);
		this.shader.unbind();
	}

	@Override
	public void close() {
		this.shader.close();
		for (Mesh mesh : this.meshes.values())
			mesh.close();
	}

	private Iterable<BlockItem> iterBlocks() {
		return this.streamBlocks()::iterator;
	}

	private Stream<BlockItem> streamBlocks() {
		Stream<BlockItem> blocks = this.blocks.values().stream().flatMap(Collection::stream);
		Stream<BlockItem> transparentBlocks = this.transparentBlocks.stream();
		return Stream.concat(blocks, transparentBlocks);
	}

	public ClosestItem<BlockItem> updateClosest() {
		this.closest.update(this.iterBlocks(), this.camera);
		return this.closest;
	}

	public void addFullType(String name, Mesh mesh) {
		if (this.meshes.containsKey(name))
			throw new RuntimeException("type already defined: "+name);
		this.meshes.put(name, mesh);
		this.blocks.put(name, new ArrayList<>());
	}

	public void addTransparentType(String name, Mesh mesh) {
		if (this.meshes.containsKey(name))
			throw new RuntimeException("type already defined: "+name);
		this.meshes.put(name, mesh);
		this.transparentNames.add(name);
	}

	public void setBlock(String name, Vector3f position) { this.setBlock(name, position.x, position.y, position.z); }
	public void setBlock(String name, float x, float y, float z) {
		this.removeBlock(x, y, z);
		if (name.length() != 0)
			this.addBlock(name, x, y, z);
	}

	private void addBlock(String name, float x, float y, float z) {
		BlockItem block = new BlockItem(this.meshes.get(name));
		block.setScale(BLOCK_SCALE);
		block.setPosition(x, y, z);
		this.addBlock(block);
	}

	private void addBlock(BlockItem block) {
		String name = this.nameOf(block.getMesh());
		if (this.transparentNames.contains(name))
			this.transparentBlocks.add(block);
		else
			this.blocks.get(name).add(block);
	}

	private void removeBlock(float x, float y, float z) {
		for (BlockItem block : this.iterBlocks())
			if (block.getPosition().equals(x, y, z)) {
				this.removeBlock(block);
				return;
			}
	}

	private void removeBlock(BlockItem block) {
		String name = this.nameOf(block.getMesh());
		if (this.transparentNames.contains(name))
			this.transparentBlocks.remove(block);
		else
			this.blocks.get(name).remove(block);
	}

	private String nameOf(Mesh mesh) {
		for (Map.Entry<String, Mesh> entry : this.meshes.entrySet())
			if (entry.getValue() == mesh)
				return entry.getKey();
		throw new RuntimeException("could not find name of mesh");
	}
}
