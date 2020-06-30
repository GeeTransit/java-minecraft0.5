/*
George Zhang
Background scene.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import static org.lwjgl.glfw.GLFW.*;

public class Background implements Loopable {
	public static final float COLOR_STEP = 0.3f;  // how much colour changed in 1 second

	private int direction;
	private float color;

	public Background() {
		this.direction = 0;
		this.color = 0.5f;
	}

	@Override
	public void init(Window window) {
		// blank background for first frame
		window.clearColor(1f, 1f, 1f, 0f);
	}

	@Override
	public void input(Window window) {
		if (window.isKeyDown(GLFW_KEY_L)) this.color = 0f;

		this.direction = 0;
		if (window.isKeyDown(GLFW_KEY_UP)) this.direction++;
		if (window.isKeyDown(GLFW_KEY_DOWN)) this.direction--;
	}

	@Override
	public void update(float interval) {
		this.color = Math.max(0f, Math.min(1f, this.color + this.direction * interval*COLOR_STEP));
	}

	@Override
	public void render(Window window) {
		// Different color based on vSync or not (colorful = vSync on)
		if (window.isVSync())
			window.clearColor(1-this.color, this.color/2+0.5f, this.color, 0.0f);
		else
			window.clearColor(this.color, this.color, this.color, 0.0f);
	}
}
