/*
George Zhang
Game logic implementation.
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

public class Game implements ILogic {
	
	private int direction;
	private float color;
	private Vector3f movement;
	private Renderer renderer;
	private Item[] items;

	public Game() {
		this.direction = 0;
		this.color = 0.5f;
		this.movement = new Vector3f();
		this.renderer = new Renderer();
	}
	
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
			if (key == GLFW_KEY_LEFT && action == GLFW_RELEASE)
				window.setTargetFps(Math.max(1, window.getTargetFps() - 1));
			if (key == GLFW_KEY_RIGHT && action == GLFW_RELEASE)
				window.setTargetFps(window.getTargetFps() + 1);
		});
		
		window.clearColor(this.color, this.color, this.color, 0.0f);
		
		// Link shaders. (located in res/)
		this.renderer.init();
		
		// Create rectangle mesh
		float[] positions = {
			-0.5f,  0.5f, -1.0f,
			-0.5f, -0.5f, -1.0f,
			 0.5f, -0.5f, -1.0f,
			 0.5f,  0.5f, -1.0f,
		};
		float[] colours = {
			0.5f, 0.0f, 0.0f,
			0.0f, 0.5f, 0.0f,
			0.0f, 0.0f, 0.5f,
			0.0f, 1.0f, 1.0f,
		};
		int[] indices = {0, 1, 3, 3, 1, 2};
		this.items = new Item[]{new Item(new Mesh(positions, colours, indices))};
	}
	
	@Override
	public void input(Window window) {
		this.direction = 0;
		if (window.isKeyDown(GLFW_KEY_UP)) this.direction++;
		if (window.isKeyDown(GLFW_KEY_DOWN)) this.direction--;
		
		this.movement.zero();
		if (window.isKeyDown(GLFW_KEY_W)) this.movement.z--;
		if (window.isKeyDown(GLFW_KEY_A)) this.movement.x--;
		if (window.isKeyDown(GLFW_KEY_S)) this.movement.z++;
		if (window.isKeyDown(GLFW_KEY_D)) this.movement.x++;
		if (window.isKeyDown(GLFW_KEY_SPACE)) this.movement.y++;
		if (window.isKeyDown(GLFW_KEY_LEFT_SHIFT)) this.movement.y--;
		if (window.isKeyDown(GLFW_KEY_LEFT_CONTROL)) this.movement.z *= 2;
	}
	
	@Override
	public void update(float interval) {
		this.color += this.direction * 0.01f;
		if (color > 1) this.color = 1.0f;
		if (color < 0) this.color = 0.0f;
		
		Vector3f movement = new Vector3f();
		this.movement.mul(-0.05f, movement);
		for (Item item : this.items)
			item.position.add(movement);
	}
	
	@Override
	public void render(Window window) {
		// Check for resize / vSync change (not sure if ordering matters)
		window.checkUpdateSize();
		window.checkUpdateVSync();
		
		// clear the framebuffer
		window.clear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		// Different colour based on vSync or not (colourful = vSync on)
		if (window.isVSync())
			window.clearColor(1-this.color, this.color/2+0.5f, this.color, 0.0f);
		else
			window.clearColor(this.color, this.color, this.color, 0.0f);
		
		this.renderer.render(window, this.items);
		
		// Swap buffers
		window.update();
	}
	
	@Override
	public void cleanup() {
		this.renderer.cleanup();
		for (Item item : this.items)
			item.mesh.cleanup();
	}
}
