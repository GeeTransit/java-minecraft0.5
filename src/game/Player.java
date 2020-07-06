/*
George Zhang
Player interaction class.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import java.util.*;
import org.joml.Vector3f;
import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Player implements Loopable {
	public static final float CHANGE_DELAY = 0.2f;  // time between block change (place / remove)
	public static final float MOVEMENT_STEP = 3.0f;  // distance moved in 1 second
	public static final float SPRINT_MULTIPLIER = 1.5f;  // sprinting change

	private final Mouse mouse;
	private final Camera camera;
	private final World world;

	private final Countdown countdown;
	private final Vector3f movement;
	private String change;  // ""=air

	public Player(Mouse mouse, Camera camera, World world) {
		this.mouse = mouse;
		this.camera = camera;
		this.world = world;

		this.countdown = new Countdown(CHANGE_DELAY);
		this.movement = new Vector3f();
	}

	public String getChange() { return this.change; }
	public float getWait() { return this.countdown.getWait(); }

	@Override
	public void input(Window window) {
		// movement
		this.movement.zero();
		boolean SPRINTING = !window.isKeyDown(GLFW_KEY_LEFT_SHIFT) && window.isKeyDown(GLFW_KEY_LEFT_CONTROL);

		if (window.isKeyDown(GLFW_KEY_W)) this.movement.z--;
		if (window.isKeyDown(GLFW_KEY_S)) this.movement.z++;
		if (window.isKeyDown(GLFW_KEY_A)) this.movement.x--;
		if (window.isKeyDown(GLFW_KEY_D)) this.movement.x++;

		if (window.isKeyDown(GLFW_KEY_LEFT_SHIFT)) this.movement.y--;
		if (window.isKeyDown(GLFW_KEY_SPACE)) this.movement.y++;

		if (this.movement.length() > 1f) this.movement.div(this.movement.length());
		if (SPRINTING && this.movement.z < 0) this.movement.mul(SPRINT_MULTIPLIER);

		// placing / removing
		this.change = null;
		if (window.isKeyDown(GLFW_KEY_0)) this.change = "";
		if (window.isKeyDown(GLFW_KEY_1)) this.change = "grassblock";
		if (window.isKeyDown(GLFW_KEY_2)) this.change = "cobbleblock";
		if (window.isKeyDown(GLFW_KEY_3)) this.change = "glassblock";
		if (this.change == null) this.countdown.reset();
	}

	@Override
	public void update(float interval) {
		// movement
		this.camera.movePosition(this.movement, interval*MOVEMENT_STEP);

		// placing / removing
		this.countdown.add(interval);
		if (this.change != null && this.countdown.nextOnce()) {
			ClosestItem<BlockItem> closest = this.world.updateClosest();
			// check if block found
			if (closest.closest != null) {
				Vector3f position = new Vector3f();
				position.set(closest.direction);  // get normalized camera direction
				position.negate();  // move towards camera
				position.mul(0.001f * (this.change.equals("") ? -1 : 1));  // go to block
				position.add(closest.hit);  // start from intersection point
				position.round();  // round to grid
				this.world.setBlock(this.change, position);
			}
		}
	}
}
