/*
George Zhang
World scene implementation.
*/

package geetransit.minecraft05.game;

import java.util.*;
import java.nio.*;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Matrix4f;
import org.joml.Intersectionf;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.Version;

import geetransit.minecraft05.engine.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class World extends SceneRender {
	private Mouse mouse;
	private float sensitivity;
	
	private Camera camera;
	private float step;
	private Vector3f movement;
	
	private Map<String, Item> blockMap;
	
	private static final float CHANGE_DELAY = 0.2f;
	private String change;  // ""=air
	private float wait;  // time until next place / remove
	
	private static final int MAX_COLOR = 255*255*255;

	public World(Mouse mouse, Camera camera) {
		super();
		this.setRenderer(new Renderer(this) {
			public Shader create(Window window) throws Exception {
				return this.create3D("/res/vertex-3d.vs", "/res/fragment-3d.fs");
			}
			public void render(Window window) {
				this.render3DMap(window, World.this.getCamera());
			}
		});
		
		this.mouse = mouse;
		this.sensitivity = 0.3f;
		
		this.camera = camera;
		this.step = 0.1f;
		this.movement = new Vector3f();
	}
	
	public Mouse getMouse()       { return this.mouse; }
	public float getSensitivity() { return this.sensitivity; }
	
	public Camera getCamera()     { return this.camera; }
	public float getStep()        { return this.step; }
	public Vector3f getMovement() { return this.movement; }
	
	public String getChange() { return this.change; }
	public float getWait() { return this.wait; }
	
	@Override
	public void init(Window window) throws Exception {
		super.init(window);
		
		// Create the blocks' mesh
		this.blockMap = new HashMap<>();
		this.blockMap.put("grassblock", this.loadBlock("/res/cube.obj", "/res/grassblock.png"));
		this.blockMap.put("cobbleblock", this.loadBlock("/res/cube.obj", "/res/cobbleblock.png"));
		
		// get heightmap
		try (HeightMap map = HeightMap.loadFromImage("/res/heightmap.png")) {
			// create terrain
			for (int x = 0; x < map.width; x++) {
				for (int z = 0; z < map.length; z++) {
					int y = (int) map.compressExpand(map.heightAt(x, z), 0, MAX_COLOR, 0, 16);
					this.addItem(this.newBlock("grassblock").setPosition(x, y, z));
					for (int k = y-1; k >= Math.max(y-2, 0); k--) {
						this.addItem(this.newBlock("cobbleblock").setPosition(x, k, z));
					}
				}
			}
		}
		
		// add spawn markers (-2z is forwards)
		this
			.addItem(this.newBlock("grassblock").setPosition(+1, +1,  0))
			.addItem(this.newBlock("grassblock").setPosition(-1, +1,  0))
			.addItem(this.newBlock("grassblock").setPosition( 0, +1, +1))
			.addItem(this.newBlock("grassblock").setPosition( 0, +1, -2));
	}

	@Override
	public void input(Window window) {
		super.input(window);
		
		// movement
		this.movement.zero();
		boolean sprinting = (!window.isKeyDown(GLFW_KEY_LEFT_SHIFT) && window.isKeyDown(GLFW_KEY_LEFT_CONTROL));
		
		if (window.isKeyDown(GLFW_KEY_W)) this.movement.z--;
		if (window.isKeyDown(GLFW_KEY_S)) this.movement.z++;
		if (window.isKeyDown(GLFW_KEY_A)) this.movement.x--;
		if (window.isKeyDown(GLFW_KEY_D)) this.movement.x++;
		
		if (window.isKeyDown(GLFW_KEY_LEFT_SHIFT)) this.movement.y--;
		if (window.isKeyDown(GLFW_KEY_SPACE)) this.movement.y++;
		
		if (this.movement.length() > 1f) this.movement.div(this.movement.length());
		if (sprinting && this.movement.z < 0) this.movement.mul(1.5f);
		
		// placing / removing
		if (this.change == null) {
			if (window.isKeyDown(GLFW_KEY_0)) this.change = "";
			if (window.isKeyDown(GLFW_KEY_1)) this.change = "grassblock";
			if (window.isKeyDown(GLFW_KEY_2)) this.change = "cobbleblock";
		}
	}
	
	@Override
	public void update(float interval) {
		super.update(interval);
		
		// movement
		if (this.mouse.isInside())
			this.camera.rotateUsingMouse(this.mouse, this.sensitivity);
		this.camera.movePosition(this.movement.mul(this.step, new Vector3f()));
		
		// placing / removing
		if (this.change != null && this.wait <= 0) {
			ClosestItem closestItem = new ClosestItem(this.getItems(), this.camera);
			if (closestItem.closest != null) {
				if (this.change.equals("")) {
					this.removeItem(closestItem.closest);
				} else {
					Vector3f position = new Vector3f();
					position.set(closestItem.direction);  // get normalized camera direction
					position.negate();  // move towards camera
					position.mul(0.01f);
					position.add(closestItem.hit);  // start from intersection point
					position.round();  // round to grid
					this.addItem(this.newBlock(this.change).setPosition(position));
				}
			}
			this.wait += this.CHANGE_DELAY;
			this.change = null;
		}
		
		// update wait time
		if (this.wait > 0)
			this.wait -= interval;
		if (this.wait < 0 && this.change == null)
			this.wait = 0;
	}
	
	@Override
	public void render(Window window) {
		this.updateSelectedItem();
		super.render(window);
	}
	
	// CAN return null
	private void updateSelectedItem() {
		ClosestItem closestItem = new ClosestItem(this.getItems(), this.camera);
		for (Item item : this.getItems())
			item.setSelected(false);
		if (closestItem.closest != null)
			closestItem.closest.setSelected(true);
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
