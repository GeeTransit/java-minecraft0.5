/*
George Zhang
World scene implementation.
*/

package geetransit.minecraft05.game;

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

	public World(Mouse mouse, Camera camera) {
		super();
		this.setRenderer(new Renderer(this) {
			public Shader createShader(Window window) throws Exception {
				return this.create3DShader("/res/vertex-3d.vs", "/res/fragment-3d.fs");
			}
			public void render(Window window) {
				this.render3DScene(window, World.this.getCamera());
			}
		});
		
		this.mouse = mouse;
		this.sensitivity = 0.3f;
		
		this.camera = camera;
		this.step = 0.05f;
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
		Mesh mesh = ObjLoader.loadMesh("/res/cube.obj");
		mesh.setTexture(new Texture("/res/grassblock.png"));
		
		this
			.addItem(new Item(mesh).setScale(0.5f).setPosition(-1, -1, -1))
			.addItem(new Item(mesh).setScale(0.5f).setPosition( 0, -1, -1))
			.addItem(new Item(mesh).setScale(0.5f).setPosition(+1,  0, -1))
			.addItem(new Item(mesh).setScale(0.5f).setPosition(-1, -1,  0))
			.addItem(new Item(mesh).setScale(0.5f).setPosition( 0,  0,  0))
			.addItem(new Item(mesh).setScale(0.5f).setPosition(+1, +1,  0))
			.addItem(new Item(mesh).setScale(0.5f).setPosition(-1,  0, +1))
			// missing ones is +z (back) and +xz (back-right)
			// .addItem(new Item(mesh).setScale(0.5f).setPosition( 0, +1, +1))
			// .addItem(new Item(mesh).setScale(0.5f).setPosition(+1, +1, +1))
			;
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
}
