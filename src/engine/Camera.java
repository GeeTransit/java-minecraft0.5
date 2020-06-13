/*
ahbejarano
Camera wrapper class.
*/

package geetransit.minecraft05.engine;

import org.joml.Vector3f;

public class Camera {
	public final Vector3f position;
	public final Vector3f rotation;

	public Camera(Vector3f position, Vector3f rotation) {
		this.position = position;
		this.rotation = rotation;
	}
	public Camera() { this(new Vector3f(), new Vector3f()); }

	public void movePosition(Vector3f offset) {
		if (offset.z != 0) {
			this.position.x += (float) Math.sin(Math.toRadians(this.rotation.y)) * -1.0f * offset.z;
			this.position.z += (float) Math.cos(Math.toRadians(this.rotation.y)) * offset.z;
		}
		if (offset.x != 0) {
			this.position.x += (float) Math.sin(Math.toRadians(this.rotation.y - 90)) * -1.0f * offset.x;
			this.position.z += (float) Math.cos(Math.toRadians(this.rotation.y - 90)) * offset.x;
		}
		position.y += offset.y;
	}
}
