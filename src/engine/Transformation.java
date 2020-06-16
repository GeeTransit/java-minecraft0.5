/*
ahbejarano
Transformation helper class.
*/

package geetransit.minecraft05.engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transformation {
	private final Matrix4f projectionMatrix;
	private final Matrix4f viewMatrix;
	private final Matrix4f modelViewMatrix;
	private final Matrix4f orthoProjectionMatrix;
	private final Matrix4f orthoProjModelMatrix;
	private final Vector3f rotateVector;

	public Transformation() {
		this.projectionMatrix = new Matrix4f();
		this.viewMatrix = new Matrix4f();
		this.modelViewMatrix = new Matrix4f();
		this.orthoProjectionMatrix = new Matrix4f();
		this.orthoProjModelMatrix = new Matrix4f();
		this.rotateVector = new Vector3f();
	}

	public Matrix4f getProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
		return this.projectionMatrix.setPerspective(-fov, width / height, zNear, zFar);
	}
	public Matrix4f getProjectionMatrix(Window window, Camera camera) {
		return this.getProjectionMatrix(
			camera.getFov(), (float) window.getWidth(), (float) window.getHeight(),
			camera.getNear(), camera.getFar()
		);
	}
	
	public Matrix4f getViewMatrix(Camera camera) {
		return this.viewMatrix
			.identity()
			// First do the rotation so camera rotates over its position
			.rotate((float) Math.toRadians(camera.getRotation().x), this.rotateVector.set(1, 0, 0))
			.rotate((float) Math.toRadians(camera.getRotation().y), this.rotateVector.set(0, 1, 0))
			// Then do the translation
			.translate(camera.getPosition().negate(this.rotateVector));
	}
	
	public Matrix4f getModelViewMatrix(Item item, Matrix4f viewMatrix) {
		return this.modelViewMatrix
			.set(viewMatrix)
			.translate(item.getPosition())
			.rotateX((float) Math.toRadians(-item.getRotation().x))
			.rotateY((float) Math.toRadians(-item.getRotation().y))
			.rotateZ((float) Math.toRadians(-item.getRotation().z))
			.scale(item.getScale());
	}
	
	public Matrix4f getOrthoProjectionMatrix(Window window) {
		return this.orthoProjectionMatrix.setOrtho2D(0, window.getWidth(), window.getHeight(), 0);
	}
	
	public Matrix4f getOrthoProjModelMatrix(Item item, Matrix4f orthoMatrix) {
		return this.orthoProjModelMatrix
			.set(orthoMatrix)
			.translate(item.getPosition())
			.rotateX((float) Math.toRadians(-item.getRotation().x))
			.rotateY((float) Math.toRadians(-item.getRotation().y))
			.rotateZ((float) Math.toRadians(-item.getRotation().z))
			.scale(item.getScale());
	}
}
