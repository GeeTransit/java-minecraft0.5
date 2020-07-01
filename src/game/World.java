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
	private final Map<String, Mesh> meshMap;  // name -> mesh
	private final Map<String, List<String>> groupMap;  // group -> list<name>
	private final Map<String, Boolean> orderMap;  // group -> ordered
	private final Map<String, List<BlockItem>> blockMap;  // group -> list<block>
	private final List<BlockItem> blockList;

	private final ClosestItem<BlockItem> closestItem;
	private final Vector3f movement;
	private String change;  // ""=air

	public World(Mouse mouse, Camera camera) {
		this.mouse = mouse;
		this.camera = camera;
		this.countdown = new Countdown(CHANGE_DELAY);

		this.meshMap = new HashMap<>();
		this.groupMap = new HashMap<>();
		this.orderMap = new HashMap<>();
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

		// create groups and block meshes
		this.addGroup("grass", false);
		this.putMesh("grass", "grassblock", this.loadMesh("/res/cube-fblr,u,d.obj", "/res/grassblock.png"));

		this.addGroup("cobble", false);
		this.putMesh("cobble", "cobbleblock", this.loadMesh("/res/cube-fblrud.obj", "/res/cobbleblock.png"));

		this.addGroup("glass", true);
		this.putMesh("glass", "glassblock", this.loadMesh("/res/cube-fblrud.obj", "/res/glassblock.png"));

		// get heightmap
		try (HeightMap map = HeightMap.loadFromImage("/res/heightmap.png")) {
			// create terrain
			for (int x = 0; x < map.width; x++) {
				for (int z = 0; z < map.length; z++) {
					int y = (int) map.compressExpand(map.heightAt(x, z), 0, map.MAX_COLOR, 0, 16);
					this.addBlock("grassblock", x, y, z);
					for (int k = y-1; k >= Math.max(y-2, 0); k--)
						this.addBlock("cobbleblock", x, k, z);
				}
			}
		}

		// add spawn markers (-2z is forwards)
		this.addBlock("grassblock", +1, +1,  0);
		this.addBlock("grassblock", -1, +1,  0);
		this.addBlock("grassblock",  0, +1, +1);
		this.addBlock("grassblock",  0, +1, -2);
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
		if (window.isKeyDown(GLFW_KEY_3)) this.change = "glassblock";
		if (this.change == null) this.countdown.reset();
	}

	@Override
	public void update(float interval) {
		// movement
		this.camera.movePosition(this.movement, interval*MOVEMENT_STEP);
		if (!this.movement.equals(0, 0, 0))
			for (Map.Entry<String, Boolean> entry : this.orderMap.entrySet())
				if (entry.getValue())
					this.reorderBlock(entry.getKey());

		this.countdown.add(interval);
		if (this.change != null && this.countdown.nextOnce()) {
			// placing / removing
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
						this.addBlock(this.change, position);
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

		// update visible blocks
		for (BlockItem block : this.blockList)
			block.setVisible(this.camera.insideFrustum(block.getPosition(), BLOCK_RADIUS*block.getScale()));

		// reorder ordered groups
		for (String group : this.blockMap.keySet())
			if (this.orderMap.get(group))
				this.reorderBlock(group);

		Matrix4f viewMatrix = this.camera.getViewMatrix();  // view matrix
		Matrix4f temp = new Matrix4f();  // temporary matrix (stores model view matrix)

		// opaque blocks
		for (String group : this.groupMap.keySet())
			if (!this.orderMap.get(group))
				this.renderBlock(group, viewMatrix, temp);

		// transparent blocks
		for (String group : this.groupMap.keySet())
			if (this.orderMap.get(group))
				this.renderBlock(group, viewMatrix, temp);

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

	private void putMesh(String group, String name, Mesh mesh) {
		// mesh
		if (this.meshMap.containsKey(name))
			throw new RuntimeException("mesh already defined: "+name);
		this.meshMap.put(name, mesh);

		// group
		if (!this.groupMap.containsKey(group))
			throw new RuntimeException("group not defined: "+group);
		if (!this.groupMap.get(group).contains(name))
			this.groupMap.get(group).add(name);
	}

	private void addGroup(String group, boolean order) { this.addGroup(group, order, false); }
	private void addGroup(String group, boolean order, boolean redefine) {
		if (!redefine && this.groupMap.containsKey(group))
			throw new RuntimeException("group already defined: "+group);

		this.groupMap.put(group, new ArrayList<>());
		this.orderMap.put(group, order);
		this.blockMap.put(group, new ArrayList<>());
	}

	private BlockItem newBlock(String name) {
		Mesh mesh = this.meshMap.get(name);
		return new BlockItem(mesh).setScale(BLOCK_SCALE);
	}

	private void renderBlock(String group, Matrix4f viewMatrix, Matrix4f temp) {
		List<BlockItem> blocks = this.blockMap.get(group);
		if (this.groupMap.get(group).size() == 1) {
			if (blocks.size() > 0){
				blocks.get(0).getMesh().render(
					this.shader,
					blocks.stream().filter(block -> block.isVisible()),
					(shader, block) -> {
						block.buildModelViewMatrix(viewMatrix, temp);
						shader.set("modelViewMatrix", temp);
						shader.set("isSelected", block.isSelected());
					}
				);
			}
		} else {
			for (BlockItem block : blocks)
				block.getMesh().render(this.shader, block, (shader, block2) -> {
					block.buildModelViewMatrix(viewMatrix, temp);
					shader.set("modelViewMatrix", temp);
					shader.set("isSelected", block2.isSelected());
				});
		}
	}

	private String nameOf(BlockItem block) {
		for (Map.Entry<String, Mesh> entry : this.meshMap.entrySet())
			if (entry.getValue() == block.getMesh())
				return entry.getKey();
		throw new RuntimeException("could not find parent name: "+block);
	}
	private String groupOf(String name) {
		for (Map.Entry<String, List<String>> entry : this.groupMap.entrySet())
			if (entry.getValue().contains(name))
				return entry.getKey();
		throw new RuntimeException("could not find parent group: "+name);
	}

	private void addBlock(String name, Vector3f position) { this.addBlock(name, position.x, position.y, position.z); }
	private void addBlock(String name, float x, float y, float z) {
		Mesh mesh = this.meshMap.get(name);
		BlockItem block = new BlockItem(mesh);
		block.setScale(BLOCK_SCALE);
		block.setPosition(x, y, z);

		String group = this.groupOf(name);
		this.blockList.add(block);
		this.blockMap.get(group).add(block);
		if (this.orderMap.get(group))
			this.reorderBlock(group);
	}

	private void reorderBlock(String group) {
		Vector3f cameraPosition = this.camera.getPosition();
		this.blockMap.get(group).sort(Comparator.<BlockItem>comparingDouble(
			block -> block.getPosition().distance(cameraPosition)
		));
	}

	private void removeBlock(BlockItem block) {
		String name = this.nameOf(block);
		String group = this.groupOf(name);
		this.blockList.remove(block);
		this.blockMap.get(group).remove(block);
	}
}
