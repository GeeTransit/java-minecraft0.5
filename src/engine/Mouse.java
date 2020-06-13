/*
ahbejarano
Camera wrapper class.
*/

package geetransit.minecraft05.engine;

import org.joml.Vector2d;
import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.*;

public class Mouse {
	public final Vector2i current;
	public final Vector2i movement;
	private Vector2i previous;

	public boolean inside = false;
	public boolean left = false;
	public boolean right = false;

	public Mouse() {
		this.current = new Vector2f(-1, -1);
		this.movement = new Vector2f();
		this.previous = new Vector2f(0, 0);
	}

	public void init(Window window) {
		glfwSetCursorPosCallback(window.getHandle(), (handle, x, y) -> {
			this.current.x = (float) x;
			this.current.y = (float) y;
		});
		glfwSetCursorEnterCallback(window.getHandle(), (handle, entered) -> {
			this.inside = entered;
		});
		glfwSetMouseButtonCallback(window.getHandle(), (handle, button, action, mode) -> {
			if (button == GLFW_MOUSE_BUTTON_1) this.left = (action == GLFW_PRESS);
			if (button == GLFW_MOUSE_BUTTON_2) this.right = (action == GLFW_PRESS);
		});
	}

	public void input(Window window) {
		this.current.sub(this.previous, this.movement);
		this.previous.set(this.current);
	}
}
