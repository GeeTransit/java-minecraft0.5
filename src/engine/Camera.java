/*
ahbejarano
Camera wrapper class.
*/

package geetransit.minecraft05.engine;

import org.joml.Vector3f;

public class Camera {
	private static final float FOV = 80f;
	private static final float NEAR = 0.01f;
	private static final float FAR = 50f;
	
	private final Vector3f position;
	private final Vector3f rotation;
	private float fov;
	private float near;
	private float far;

	public Camera(Vector3f position, Vector3f rotation, float fov, float near, float far) {
		this.position = position;
		this.rotation = rotation;
		this.fov = (float) Math.toRadians(fov);
		this.near = near;
		this.far = far;
	}
	public Camera(Vector3f position, Vector3f rotation, float fov) { this(position, rotation, fov, NEAR, FAR); }
	public Camera(Vector3f position, Vector3f rotation) { this(position, rotation, FOV); }
	public Camera() { this(new Vector3f(), new Vector3f()); }
	
	public Vector3f getPosition() { return this.position; }
	public Vector3f getRotation() { return this.rotation; }
	public float getFov()  { return (float) Math.toDegrees(this.fov); }
	public float getNear() { return this.near; }
	public float getFar()  { return this.far; }
	public Camera setPosition(Vector3f position)         { this.position.set(position); return this; }
	public Camera setPosition(float x, float y, float z) { this.position.set(x, y, z); return this; }
	public Camera setRotation(Vector3f rotation)         { this.rotation.set(rotation); return this; }
	public Camera setRotation(float x, float y, float z) { this.rotation.set(x, y, z); return this; }
	public Camera setFov(float fov)   { this.fov = (float) Math.toRadians(fov); return this; }
	public Camera setNear(float near) { this.near = near; return this; }
	public Camera setFar(float far)   { this.far = far; return this; }

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
	
	public String toString() {
		return String.format(
			"<%s position=%s rotation=%s>",
			this.getClass().getSimpleName(),
			this.getPosition(),
			this.getRotation()
		);
	}
}
