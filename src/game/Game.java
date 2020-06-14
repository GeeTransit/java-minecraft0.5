/*
George Zhang
Game logic implementation.
*/

package geetransit.minecraft05.game;

import java.util.*;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.Version;

import geetransit.minecraft05.engine.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game implements ILogic {
	
	private int direction;
	private float color;
	private float step;
	private float sensitivity;
	private Vector3f movement;
	private Renderer renderer;
	private Camera camera;
	private Mouse mouse;
	private List<Item> items;

	public Game(float step, float sensitivity, float fov) {
		this.direction = 0;
		this.color = 0.5f;
		this.step = step;
		this.sensitivity = sensitivity;
		this.movement = new Vector3f();
		this.renderer = new Renderer(fov);
		this.camera = new Camera();
		this.mouse = new Mouse();
		this.items = new ArrayList<>();
	}
	public Game() { this(60f); }
	public Game(float fov) { this(0.05f, 0.3f, fov); }
	public Game(float step, float sensitivity) { this(step, sensitivity, 60f); }
	
	@Override
	public void init(Window window) throws Exception {
		System.out.println("LWJGL version: " + Version.getVersion());
		System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
		
		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		window.setKeyCallback((handle, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				window.setShouldClose(true);  // We will detect this in the rendering loop
			if (key == GLFW_KEY_V && action == GLFW_RELEASE)
				window.setNextVSync(!window.isVSync());
			if (key == GLFW_KEY_F && action == GLFW_RELEASE)
				window.setNextSize(!window.isFullscreen());
			if (key == GLFW_KEY_LEFT && !window.isVSync() && action == GLFW_RELEASE)
				window.setTargetFps(Math.max(1, window.getTargetFps() - 1));
			if (key == GLFW_KEY_RIGHT && !window.isVSync() && action == GLFW_RELEASE)
				window.setTargetFps(window.getTargetFps() + 1);
		});
		
		window.clearColor(this.color, this.color, this.color, 0.0f);
		
		// Set callbacks
		this.mouse.init(window);
		
		// Link shaders. (located in res/)
		this.renderer.init();
		
		// Create the cube mesh
		Mesh mesh = ObjLoader.loadMesh("/res/cube.obj");
		mesh.setTexture(new Texture("/res/grassblock.png"));
		
		this.items.add(new Item(mesh).setScale(0.5f).setPosition(-1, -1, -1));
		this.items.add(new Item(mesh).setScale(0.5f).setPosition( 0, -1, -1));
		this.items.add(new Item(mesh).setScale(0.5f).setPosition(+1,  0, -1));
		this.items.add(new Item(mesh).setScale(0.5f).setPosition(-1, -1,  0));
		this.items.add(new Item(mesh).setScale(0.5f).setPosition( 0,  0,  0));
		this.items.add(new Item(mesh).setScale(0.5f).setPosition(+1, +1,  0));
		this.items.add(new Item(mesh).setScale(0.5f).setPosition(-1,  0, +1));
		this.items.add(new Item(mesh).setScale(0.5f).setPosition( 0, +1, +1));
		this.items.add(new Item(mesh).setScale(0.5f).setPosition(+1, +1, +1));
		
		// Use correct depth checking
		glEnable(GL_DEPTH_TEST);
	}
	
	@Override
	public void input(Window window) {
		this.mouse.input(window);
		
		if (window.isKeyDown(GLFW_KEY_L)) {
			this.color = 0f;
		}
		
		this.direction = 0;
		if (window.isKeyDown(GLFW_KEY_UP)) this.direction++;
		if (window.isKeyDown(GLFW_KEY_DOWN)) this.direction--;
		
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
		this.color += this.direction * 0.01f;
		this.color = Math.max(0f, Math.min(1f, this.color));
		
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
	
	@Override
	public void render(Window window) {
		// Check for resize / vSync change (not sure if ordering matters)
		window.checkUpdateSize();
		window.checkUpdateVSync();
		
		// clear the framebuffer
		window.clear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		// Different color based on vSync or not (colorful = vSync on)
		if (window.isVSync())
			window.clearColor(1-this.color, this.color/2+0.5f, this.color, 0.0f);
		else
			window.clearColor(this.color, this.color, this.color, 0.0f);
		
		this.renderer.render(window, this.camera, this.items);
		
		// Swap buffers
		window.update();
	}
	
	@Override
	public void cleanup() {
		this.renderer.cleanup();
		this.items.stream().forEach(item -> item.getMesh().cleanup());
	}
}
