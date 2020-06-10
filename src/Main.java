/*
George Zhang
2020-06-08
Main entry class.
*/

package geetransit.minecraft05;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.Version;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main implements ILogic {
	
	private int direction;
	private float color;

	public Main() {
		this.direction = 0;
		this.color = 0.0f;
	}

	public void init(Window window) {
		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window.getHandle(), (handle, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
				glfwSetWindowShouldClose(handle, true); // We will detect this in the rendering loop
			}
			if (key == GLFW_KEY_V && action == GLFW_RELEASE) {
				window.setVSync(!window.isVSync());
				window.setUpdateVSync(true);
			}
			if (key == GLFW_KEY_LEFT && action == GLFW_RELEASE) {
				window.setTargetFps(Math.max(1, window.getTargetFps() - 1));
			}
			if (key == GLFW_KEY_RIGHT && action == GLFW_RELEASE) {
				window.setTargetFps(window.getTargetFps() + 1);
			}
		});
		
		glClearColor(this.color, this.color, this.color, 0.0f);
	}
	
	public void input(Window window) {
		this.direction = 0;
		if (glfwGetKey(window.getHandle(), GLFW_KEY_UP) == GLFW_PRESS) {
			this.direction++;
		}
		if (glfwGetKey(window.getHandle(), GLFW_KEY_DOWN) == GLFW_PRESS) {
			this.direction--;
		}
	}
	
	public void update(float interval) {
		this.color += this.direction * 0.01f;
		if (color > 1) {
			this.color = 1.0f;
		} else if (color < 0) {
			this.color = 0.0f;
		}
	}
	
	public void render(Window window) {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
		
		// Check for resize / vSync change
		window.checkUpdateVSync();
		window.checkUpdateSize();
		
		glClearColor(this.color, this.color, this.color, 0.0f);
		
		// Swap buffers
		window.update();
	}
	
	public void cleanup() {
	}

	public static void main(String[] args) {
		try {
			boolean vSync = true;
			int targetFps = 5;
			int targetUps = 30;
			Window window = new Window("Hello World!", 300, 300, vSync, targetFps, targetUps);
			ILogic logic = new Main();
			Engine engine = new Engine(window, logic);
			engine.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
