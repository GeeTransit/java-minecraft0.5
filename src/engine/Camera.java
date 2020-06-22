/*
ahbejarano
Camera wrapper class.
*/

package geetransit.minecraft05.engine;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Matrix4f;

public class Camera implements Inputtable {
	public static final float FOV = 80f;
	public static final float NEAR = 0.01f;
	public static final float FAR = 50f;
	public static final float SENSITIVITY = 0.75f;

	private Mouse mouse;

	private final Vector3f position;
	private final Vector3f rotation;  // in degrees
	private final Matrix4f viewMatrix;

	private float fov;
	private float near;
	private float far;
	private float sensitivity;

	private final Vector3f radiansRotation;
	private final Vector3f negativePosition;

	public Camera(Mouse mouse) {
		this.mouse = mouse;

		this.position = new Vector3f();
		this.rotation = new Vector3f();
		this.viewMatrix = new Matrix4f();

		this.setFov(FOV);
		this.setNear(NEAR);
		this.setFar(FAR);
		this.setSensitivity(SENSITIVITY);

		this.radiansRotation = new Vector3f();
		this.negativePosition = new Vector3f();
	}

	@Override
	public void input(Window window) {
		if (this.mouse.isInside())
			this.rotateMovement(this.mouse.getMovement(), this.sensitivity);
	}

	public Mouse getMouse() { return this.mouse; }
	public Vector3f getPosition() { return this.position; }
	public Vector3f getRotation() { return this.rotation; }
	public float getFov() { return (float) Math.toDegrees(this.fov); }
	public float getNear() { return this.near; }
	public float getFar() { return this.far; }
	public float getSensitivity() { return this.sensitivity; }

	public Camera setMouse(Mouse mouse) { this.mouse = mouse; return this; }
	public Camera setPosition(Vector3f position) { this.position.set(position); return this; }
	public Camera setPosition(float x, float y, float z) { this.position.set(x, y, z); return this; }
	public Camera setRotation(Vector3f rotation) { this.rotation.set(rotation); return this; }
	public Camera setRotation(float x, float y, float z) { this.rotation.set(x, y, z); return this; }
	public Camera setFov(float fov) { this.fov = (float) Math.toRadians(fov); return this; }
	public Camera setNear(float near) { this.near = near; return this; }
	public Camera setFar(float far) { this.far = far; return this; }
	public Camera setSensitivity(float sensitivity) { this.sensitivity = sensitivity; return this; }

	public Camera movePosition(Vector3f position) { return this.movePosition(position.x, position.y, position.z); }
	public Camera movePosition(float x, float y, float z) {
		// TODO optimize this (using functions inside Vector3f)
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

	public Camera rotateMovement(Vector2f movement, float sensitivity) {
		float x = movement.x;
		float y = movement.y;
		if (mouse.isLeft())  // dragging
			this.moveRotation(-y*sensitivity, -x*sensitivity, 0);
		if (mouse.isRight())  // panning
			this.moveRotation(y*sensitivity, x*sensitivity, 0);
		this.rotation.x = Math.max(-90f, Math.min(90f, this.rotation.x));  // don't allow neck snapping
		return this;
	}

	public Vector3f getRadiansRotation() {
		return this.radiansRotation.set(
			(float) Math.toRadians(this.rotation.x),
			(float) Math.toRadians(this.rotation.y),
			(float) Math.toRadians(this.rotation.z)
		);
	}

	public Vector3f getNegativePosition() {
		return this.position.negate(this.negativePosition);
	}

	public Matrix4f getViewMatrix() {
		return this.viewMatrix
			.identity()
			.rotateXYZ(this.getRadiansRotation())
			.translate(this.getNegativePosition());
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
