/*
ahbejarano
Camera wrapper class.
*/

package geetransit.minecraft05.engine;

import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.*;

public class Mouse implements Initializable, Inputtable {
	private final Vector2f current;
	private final Vector2f movement;
	private final Vector2f previous;

	private boolean inside = false;
	private boolean left = false;
	private boolean right = false;

	public Mouse() {
		this.current = new Vector2f(-1, -1);
		this.movement = new Vector2f();
		this.previous = new Vector2f(0, 0);
	}
	
	public Vector2f getMovement() { return this.movement; }
	public Vector2f getCurrent() { return this.current; }
	public boolean isInside() { return this.inside; }
	public boolean isLeft() { return this.left; }
	public boolean isRight() { return this.right; }

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
		this.movement.div(window.getTargetUps() * window.getElapsedTime());
		this.previous.set(this.current);
	}
	
	public String toString() {
		return String.format(
			"<%s movement=%s current=%s>",
			this.getClass().getSimpleName(),
			this.getMovement(),
			this.getCurrent()
		);
	}
}
