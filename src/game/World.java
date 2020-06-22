/*
George Zhang
World scene implementation.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import java.util.*;

import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class World extends Scene {
	public static final float CHANGE_DELAY = 0.2f;
	public static final float STEP = 0.1f;

	private final Renderer renderer;
	private final Mouse mouse;
	private final Camera camera;

	private final Map<String, Item> blockMap;
	private final ClosestItem closestItem;
	private final Vector3f movement;
	private float step;
	private int render;

	private String change;  // ""=air
	private float wait;  // time until next place / remove

	public World(Mouse mouse, Camera camera) {
		super();
		this.addFrom(this.renderer = new Renderer() {
			Shader shader;
			public void init(Window window) throws Exception {
				shader = create3D("/res/vertex-3d.vs", "/res/fragment-3d.fs");
			}
			public void render(Window window) {
				render3D(shader, window, World.this.camera);
			}
			public void cleanup() {
				destroy(shader);
			}
		});

		this.mouse = mouse;
		this.camera = camera;

		this.blockMap = new HashMap<>();
		this.closestItem = new ClosestItem();
		this.movement = new Vector3f();
		this.step = STEP;
	}

	public String getChange() { return this.change; }
	public float getWait() { return this.wait; }

	public float getStep() { return this.step; }
	public World setStep(float step) { this.step = step; return this; }

	@Override
	public void init(Window window) throws Exception {
		// Create the blocks' mesh
		this.blockMap.put("grassblock", this.loadBlock("/res/cube.obj", "/res/grassblock.png"));
		this.blockMap.put("cobbleblock", this.loadBlock("/res/cube.obj", "/res/cobbleblock.png"));

		// get heightmap
		try (HeightMap map = HeightMap.loadFromImage("/res/heightmap.png")) {
			// create terrain
			for (int x = 0; x < map.width; x++) {
				for (int z = 0; z < map.length; z++) {
					int y = (int) map.compressExpand(map.heightAt(x, z), 0, map.MAX_COLOR, 0, 16);
					this.renderer.addItem(this.newBlock("grassblock").setPosition(x, y, z));
					for (int k = y-1; k >= Math.max(y-2, 0); k--) {
						this.renderer.addItem(this.newBlock("cobbleblock").setPosition(x, k, z));
					}
				}
			}
		}

		// add spawn markers (-2z is forwards)
		this.renderer
			.addItem(this.newBlock("grassblock").setPosition(+1, +1,  0))
			.addItem(this.newBlock("grassblock").setPosition(-1, +1,  0))
			.addItem(this.newBlock("grassblock").setPosition( 0, +1, +1))
			.addItem(this.newBlock("grassblock").setPosition( 0, +1, -2));

		super.init(window);
	}

	public void input(Window window) {
		super.input(window);

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
		if (SPRINTING && this.movement.z < 0) this.movement.mul(1.5f);

		// render distance (camera)
		this.render = 0;
		if (window.isKeyDown(GLFW_KEY_L)) this.camera.setFar(Camera.FAR);
		if (window.isKeyDown(GLFW_KEY_RIGHT_BRACKET)) this.render++;
		if (window.isKeyDown(GLFW_KEY_LEFT_BRACKET)) this.render--;

		// placing / removing
		this.change = null;
		if (window.isKeyDown(GLFW_KEY_0)) this.change = "";
		if (window.isKeyDown(GLFW_KEY_1)) this.change = "grassblock";
		if (window.isKeyDown(GLFW_KEY_2)) this.change = "cobbleblock";
	}

	public void update(float interval) {
		// movement
		this.camera.movePosition(this.movement, 30*interval * this.step);

		// render distance
		this.camera.setFar(Math.max(Camera.NEAR+0.01f, this.camera.getFar() + 0.1f*this.render));

		// placing / removing
		if (this.change != null && this.wait <= 0) {
			this.closestItem.update(this.renderer.items, this.camera);
			if (this.closestItem.closest != null) {
				if (this.change.equals("")) {
					this.renderer.removeItem(this.closestItem.closest);
				} else {
					Vector3f position = new Vector3f();
					position.set(this.closestItem.direction);  // get normalized camera direction
					position.negate();  // move towards camera
					position.mul(0.01f);  // add a small offset (to go to next block)
					position.add(this.closestItem.hit);  // start from intersection point
					position.round();  // round to grid
					check: {
						for (Item item : this.renderer.items)
							if (item.getPosition().equals(position))
								break check;
						// else
						this.renderer.addItem(this.newBlock(this.change).setPosition(position));
					}
				}
			}
			this.wait += this.CHANGE_DELAY;
		}

		// update wait time
		if (this.wait > 0)
			this.wait -= interval;
		if (this.wait < 0 && this.change == null)
			this.wait = 0;

		super.update(interval);
	}

	public void render(Window window) {
		this.updateSelectedItem();
		super.render(window);
	}

	private void updateSelectedItem() {
		for (Item item : this.renderer.items)
			item.setSelected(false);
		this.closestItem.update(this.renderer.items, this.camera);
		if (this.closestItem.closest != null)
			this.closestItem.closest.setSelected(true);
	}

	private static Item loadBlock(String objFileName, String textureFileName) throws Exception {
		Mesh mesh = ObjLoader.loadMesh(objFileName);
		mesh.setTexture(new Texture(textureFileName));
		Item block = new Item(mesh);
		block.setScale(0.5f);
		return block;
	}

	private Item newBlock(String name) {
		return this.blockMap.get(name).clone();
	}
}
