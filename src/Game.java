/*
George Zhang
Game logic implementation.
*/

package geetransit.minecraft05.game;

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
	private Renderer renderer;
	private Mesh mesh;

	public Game() {
		this.direction = 0;
		this.color = 0.5f;
		this.renderer = new Renderer();
	}
	
	@Override
	public void init(Window window) throws Exception {
		System.out.println("LWJGL version: " + Version.getVersion());
		System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
		
		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window.getHandle(), (handle, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
				glfwSetWindowShouldClose(handle, true); // We will detect this in the rendering loop
			}
			if (key == GLFW_KEY_V && action == GLFW_RELEASE) {
				window.setUpdateVSync(!window.isVSync());
			}
			if (key == GLFW_KEY_LEFT && action == GLFW_RELEASE) {
				window.setTargetFps(Math.max(1, window.getTargetFps() - 1));
			}
			if (key == GLFW_KEY_RIGHT && action == GLFW_RELEASE) {
				window.setTargetFps(window.getTargetFps() + 1);
			}
		});
		
		glClearColor(this.color, this.color, this.color, 0.0f);
		
		// Link shaders. (located in res/)
		this.renderer.init();
		
		// Create rectangle mesh
		float[] positions = new float[]{
			-0.5f,  0.5f, -1.0f,
			-0.5f, -0.5f, -1.0f,
			 0.5f, -0.5f, -1.0f,
			 0.5f,  0.5f, -1.0f,
		};
		float[] colours = new float[]{
			0.5f, 0.0f, 0.0f,
			0.0f, 0.5f, 0.0f,
			0.0f, 0.0f, 0.5f,
			0.0f, 0.5f, 0.5f,
		};
		int[] indices = new int[]{
			0, 1, 3, 3, 1, 2,
		};
		this.mesh = new Mesh(positions, colours, indices);
	}
	
	@Override
	public void input(Window window) {
		this.direction = 0;
		if (glfwGetKey(window.getHandle(), GLFW_KEY_UP) == GLFW_PRESS) {
			this.direction++;
		}
		if (glfwGetKey(window.getHandle(), GLFW_KEY_DOWN) == GLFW_PRESS) {
			this.direction--;
		}
	}
	
	@Override
	public void update(float interval) {
		this.color += this.direction * 0.01f;
		if (color > 1) {
			this.color = 1.0f;
		} else if (color < 0) {
			this.color = 0.0f;
		}
	}
	
	@Override
	public void render(Window window) {
		// Check for resize / vSync change (not sure if ordering matters)
		boolean updatedSize = window.checkUpdateSize();
		window.checkUpdateVSync();
		
		if (updatedSize) {
			this.renderer.resize(window);
		}
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
		
		// Different colour based on vSync or not (colourful = vSync on)
		if (window.isVSync()) {
			glClearColor(1-this.color, this.color/2+0.5f, this.color, 0.0f);
		} else {
			glClearColor(this.color, this.color, this.color, 0.0f);
		}
		
		this.renderer.render(window, this.mesh);
		
		// Swap buffers
		window.update();
	}
	
	@Override
	public void cleanup() {
		this.renderer.cleanup();
		this.mesh.cleanup();
	}
}
