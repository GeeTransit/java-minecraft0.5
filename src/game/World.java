/*
George Zhang
World scene implementation.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import java.util.*;
import org.joml.Vector3f;
import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class World implements Loopable {
	public static final float CHANGE_DELAY = 0.2f;  // time between block change (place / remove)
	public static final float MOVEMENT_STEP = 3.0f;  // distance moved in 1 second
	public static final float SPRINT_MULTIPLIER = 1.5f;  // sprinting change
	public static final float BLOCK_SCALE = 0.5f;  // block scaling (mesh is 2x2x2)
	public static final float BLOCK_RADIUS = 2f;  // radius around block (for frustum culling)

	private final Mouse mouse;
	private final Camera camera;
	private final Countdown countdown;

	private Shader shader;
	private final Map<String, Mesh> meshMap;
	private final Map<Mesh, List<BlockItem>> blockMap;
	private final List<BlockItem> blockList;

	private final ClosestItem<BlockItem> closestItem;
	private final Vector3f movement;
	private String change;  // ""=air

	public World(Mouse mouse, Camera camera) {
		this.mouse = mouse;
		this.camera = camera;
		this.countdown = new Countdown(CHANGE_DELAY);

		this.meshMap = new HashMap<>();
		this.blockMap = new HashMap<>();
		this.blockList = new ArrayList<>();

		this.closestItem = new ClosestItem<>();
		this.movement = new Vector3f();
	}

	public String getChange() { return this.change; }
	public float getWait() { return this.countdown.getWait(); }

	@Override
	public void init(Window window) {
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

		// Create the blocks' mesh
		this
			.putMesh("grassblock", this.loadMesh("/res/cube-fblr,u,d.obj", "/res/grassblock.png"))
			.putMesh("cobbleblock", this.loadMesh("/res/cube-fblrud.obj", "/res/cobbleblock.png"))

		// get heightmap
		try (HeightMap map = HeightMap.loadFromImage("/res/heightmap.png")) {
			// create terrain
			for (int x = 0; x < map.width; x++) {
				for (int z = 0; z < map.length; z++) {
					int y = (int) map.compressExpand(map.heightAt(x, z), 0, map.MAX_COLOR, 0, 16);
					this.addBlock(this.newBlock("grassblock").setPosition(x, y, z));
					for (int k = y-1; k >= Math.max(y-2, 0); k--)
						this.addBlock(this.newBlock("cobbleblock").setPosition(x, k, z));
				}
			}
		}

		// add spawn markers (-2z is forwards)
		this
			.addBlock(this.newBlock("grassblock").setPosition(+1, +1,  0))
			.addBlock(this.newBlock("grassblock").setPosition(-1, +1,  0))
			.addBlock(this.newBlock("grassblock").setPosition( 0, +1, +1))
			.addBlock(this.newBlock("grassblock").setPosition( 0, +1, -2));
	}

	@Override
	public void input(Window window) {
		// movement
		this.movement.zero();
		boolean SPRINTING = (!window.isKeyDown(GLFW_KEY_LEFT_SHIFT) && window.isKeyDown(GLFW_KEY_LEFT_CONTROL));

		if (window.isKeyDown(GLFW_KEY_W)) this.movement.z--;
		if (window.isKeyDown(GLFW_KEY_S)) this.movement.z++;
		if (window.isKeyDown(GLFW_KEY_A)) this.movement.x--;
		if (window.isKeyDown(GLFW_KEY_D)) this.movement.x++;

		if (window.isKeyDown(GLFW_KEY_LEFT_SHIFT)) this.movement.y--;
		if (window.isKeyDown(GLFW_KEY_SPACE)) this.movement.y++;

		if (this.movement.length() > 1f) this.movement.div(this.movement.length());
		if (SPRINTING && this.movement.z < 0) this.movement.mul(SPRINT_MULTIPLIER);

		// placing / removing
		this.change = null;
		if (window.isKeyDown(GLFW_KEY_0)) this.change = "";
		if (window.isKeyDown(GLFW_KEY_1)) this.change = "grassblock";
		if (window.isKeyDown(GLFW_KEY_2)) this.change = "cobbleblock";
		if (this.change == null) this.countdown.reset();
	}

	@Override
	public void update(float interval) {
		// movement
		this.camera.movePosition(this.movement, interval*MOVEMENT_STEP);

		// placing / removing
		this.countdown.add(interval);
		if (this.change != null && this.countdown.nextOnce()) {
			this.closestItem.update(this.blockList, this.camera);
			if (this.closestItem.closest != null) {
				if (this.change.equals("")) {
					this.removeBlock(this.closestItem.closest);
				} else {
					Vector3f position = new Vector3f();
					position.set(this.closestItem.direction);  // get normalized camera direction
					position.negate();  // move towards camera
					position.mul(0.01f);  // add a small offset (to go to next block)
					position.add(this.closestItem.hit);  // start from intersection point
					position.round();  // round to grid
					check: {
						for (BlockItem block : this.blockList)
							if (block.getPosition().equals(position))
								break check;
						// else
						this.addBlock(this.newBlock(this.change).setPosition(position));
					}
				}
			}
		}

		// update selected block
		for (BlockItem block : this.blockList)
			block.setSelected(false);
		this.closestItem.update(this.blockList, this.camera);
		if (this.closestItem.closest != null)
			this.closestItem.closest.setSelected(true);
	}

	@Override
	public void render(Window window) {
		this.shader.bind();
		this.shader.set("texture_sampler", 0);
		this.shader.set("projectionMatrix", window.getProjectionMatrix());
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);

		// view
		Matrix4f viewMatrix = this.camera.getViewMatrix();

		// update visible blocks
		for (BlockItem block : this.blockList)
			block.setVisible(this.camera.insideFrustum(block.getPosition(), BLOCK_RADIUS*block.getScale()));

		// draw blocks
		Matrix4f temp = new Matrix4f();
		for (Map.Entry<Mesh, List<BlockItem>> entry : this.blockMap.entrySet())
			entry.getKey().render(
				this.shader,
				entry.getValue().stream().filter(item -> item.isVisible()),
				(shader, item) -> {
					item.buildModelViewMatrix(viewMatrix, temp);
					shader.set("modelViewMatrix", temp);
					shader.set("isSelected", item.isSelected());
				}
			);

		glDisable(GL_CULL_FACE);
		this.shader.unbind();
	}

	@Override
	public void cleanup() {
		this.shader.cleanup();
		for (Mesh mesh : this.meshMap.values())
			mesh.cleanup();
	}

	// block helpers
	private static Mesh loadMesh(String objFileName, String textureFileName) {
		Mesh mesh = ObjLoader.loadMesh(objFileName);
		mesh.setTexture(new Texture(textureFileName));
		return mesh;
	}

	private World putMesh(String name, Mesh mesh) {
		this.meshMap.put(name, mesh);
		return this;
	}

	private BlockItem newBlock(String name) {
		Mesh mesh = this.meshMap.get(name);
		return new BlockItem(mesh).setScale(BLOCK_SCALE);
	}

	private World addBlock(BlockItem block) {
		Mesh mesh = block.getMesh();
		this.blockList.add(block);
		if (!this.blockMap.containsKey(mesh))
			this.blockMap.put(mesh, new ArrayList<>());
		this.blockMap.get(mesh).add(block);
		return this;
	}

	private World removeBlock(BlockItem block) {
		Mesh mesh = block.getMesh();
		this.blockList.remove(block);
		this.blockMap.get(mesh).remove(block);
		if (this.blockMap.get(mesh).size() == 0)
			this.blockMap.remove(mesh);
		return this;
	}
}
