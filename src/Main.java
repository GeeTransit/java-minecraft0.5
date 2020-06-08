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

	public void init(Engine engine) {
		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(engine.getWindow(), (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			}
			if (key == GLFW_KEY_V && action == GLFW_RELEASE) {
				engine.setVSync(!engine.getVSync());
				engine.setUpdateVSync();
			}
		});

		// Set the clear color
		// glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
	}
	
	public void input(Engine engine) {
		this.direction = 0;
		if (glfwGetKey(engine.getWindow(), GLFW_KEY_UP) == GLFW_PRESS) {
			this.direction++;
		}
		if (glfwGetKey(engine.getWindow(), GLFW_KEY_DOWN) == GLFW_PRESS) {
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
	
	public void render(Engine engine) {
		glClearColor(this.color, this.color, this.color, 0.0f);
	}

	public static void main(String[] args) {
		try {
			boolean vSync = true;
			int targetFps = 5;
			int targetUps = 30;
			ILogic logic = new Main();
			Engine engine = new Engine("Hello World!", 300, 300, vSync, targetFps, targetUps, logic);
			engine.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
