/*
ahbejarano
Camera wrapper class.
*/

package geetransit.minecraft05.engine;

import org.joml.Vector3f;

public class Camera {
	private final Vector3f position;
	private final Vector3f rotation;

	public Camera(Vector3f position, Vector3f rotation) {
		this.position = position;
		this.rotation = rotation;
	}
	public Camera() { this(new Vector3f(), new Vector3f()); }
	
	public Vector3f getPosition() { return this.position; }
	public Vector3f getRotation() { return this.rotation; }
	public Camera setPosition(float x, float y, float z) { this.position.set(x, y, z); return this; }
	public Camera setRotation(float x, float y, float z) { this.rotation.set(x, y, z); return this; }

	public Camera movePosition(Vector3f position) { return this.movePosition(position.x, position.y, position.z); }
	public Camera movePosition(float x, float y, float z) {
		if (z != 0) {
			this.position.x += (float) Math.sin(Math.toRadians(this.rotation.y)) * -1.0f * z;
			this.position.z += (float) Math.cos(Math.toRadians(this.rotation.y)) * z;
		}
		if (x != 0) {
			this.position.x += (float) Math.sin(Math.toRadians(this.rotation.y - 90)) * -1.0f * x;
			this.position.z += (float) Math.cos(Math.toRadians(this.rotation.y - 90)) * x;
		}
		this.position.y += y;
		return this;
	}
	
	public Camera moveRotation(Vector3f rotation) { this.rotation.add(rotation); return this; }
	public Camera moveRotation(float x, float y, float z) {
		this.rotation.add(x, y, z);
		return this;
	}
}
