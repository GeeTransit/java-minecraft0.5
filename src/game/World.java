/*
George Zhang
World scene implementation.
*/

package geetransit.minecraft05.game;

import java.nio.*;
import org.joml.Vector3f;
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
	
	public static final int MAX_COLOR = 255*255*255;

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
	
	@Override
	public void init(Window window) throws Exception {
		super.init(window);
		
		// Create the cube mesh
		Mesh grassblock = ObjLoader.loadMesh("/res/cube.obj");
		grassblock.setTexture(new Texture("/res/grassblock.png"));
		Mesh cobbleblock = ObjLoader.loadMesh("/res/cube.obj");
		cobbleblock.setTexture(new Texture("/res/cobbleblock.png"));
		
		// get heightmap
		int widthArray[] = {0};
		int lengthArray[] = {0};
		ByteBuffer map = Utils.loadImage("/res/heightmap.png", widthArray, lengthArray);
		int width = widthArray[0];
		int length = lengthArray[0];
		
		// create terrain
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < length; j++) {
				int height = (int) this.expand(this.compress(this.heightAt(map, i, j, width), 0, MAX_COLOR), 0, 16);
				this.addItem(new Item(grassblock).setScale(0.5f).setPosition(i, height, j));
				for (int k = height-1; k >= Math.max(height-2, 0); k--) {
					this.addItem(new Item(cobbleblock).setScale(0.5f).setPosition(i, k, j));
				}
			}
		}
		
		// free heightmap
		Utils.freeImage(map);
		
		// add spawn markers (-2z is forwards)
		this
			.addItem(new Item(grassblock).setScale(0.5f).setPosition(+1, +1,  0))
			.addItem(new Item(grassblock).setScale(0.5f).setPosition(-1, +1,  0))
			.addItem(new Item(grassblock).setScale(0.5f).setPosition( 0, +1, +1))
			.addItem(new Item(grassblock).setScale(0.5f).setPosition( 0, +1, -2));
	}
	
	@Override
	public void input(Window window) {
		super.input(window);
		
		this.movement.zero();
		boolean sprinting = (!window.isKeyDown(GLFW_KEY_LEFT_SHIFT) && window.isKeyDown(GLFW_KEY_LEFT_CONTROL));
		
		if (window.isKeyDown(GLFW_KEY_W)) this.movement.z--;
		if (window.isKeyDown(GLFW_KEY_S)) this.movement.z++;
		if (window.isKeyDown(GLFW_KEY_A)) this.movement.x--;
		if (window.isKeyDown(GLFW_KEY_D)) this.movement.x++;
		
		if (window.isKeyDown(GLFW_KEY_LEFT_SHIFT)) this.movement.y--;
		if (window.isKeyDown(GLFW_KEY_SPACE)) this.movement.y++;
		if (sprinting && this.movement.z < 0) this.movement.z *= 2;
		
		if (this.movement.length() > 1f) this.movement.div(this.movement.length());
		if (sprinting && this.movement.z < 0) this.movement.mul(1.5f);
	}
	
	@Override
	public void update(float interval) {
		super.update(interval);
		
		if (this.mouse.isInside()) {
			float x = this.mouse.getMovement().x;
			float y = this.mouse.getMovement().y;
			if (this.mouse.isLeft())  // dragging
				this.camera.moveRotation(-y*this.sensitivity, -x*this.sensitivity, 0);
			if (this.mouse.isRight())  // panning
				this.camera.moveRotation(y*this.sensitivity, x*this.sensitivity, 0);
			this.camera.getRotation().x = Math.max(-90f, Math.min(90f, this.camera.getRotation().x));
		}
		
		this.camera.movePosition(this.movement.mul(this.step, new Vector3f()));
	}
	
	// usage: expand(compress(heightAt(...), 0, MAX_COLOR), 0, 16)
	private float compress(int f, float min, float max) { return (f-min) / (max-min); }
	private float expand(float f, float min, float max) { return min + f*(max-min); }
	
	private int heightAt(ByteBuffer buffer, int x, int z, int width) { return this.heightAt(buffer, x*4 + z*4*width); }
	private int heightAt(ByteBuffer buffer, int i) {
		byte r = buffer.get(i + 0);
		byte g = buffer.get(i + 1);
		byte b = buffer.get(i + 2);
		byte a = buffer.get(i + 3);
		return 0
			// | ((0xFF & a) << 24)  // removed cuz it turns overflows int
			| ((0xFF & r) << 16)
			| ((0xFF & g) << 8)
			| ((0xFF & b) << 0);
	}
}
