/*
ahbejarano
Transformation helper class.
*/

package geetransit.minecraft05.engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionf;

public class Transformation {
	private final Matrix4f projectionMatrix;
	private final Matrix4f modelViewMatrix;
	private final Matrix4f orthoProjectionMatrix;
	private final Matrix4f orthoProjModelMatrix;
	private final Matrix4f modelMatrix;

	public Transformation() {
		this.projectionMatrix = new Matrix4f();
		this.modelViewMatrix = new Matrix4f();
		this.orthoProjectionMatrix = new Matrix4f();
		this.orthoProjModelMatrix = new Matrix4f();
		this.modelMatrix = new Matrix4f();
	}

	public Matrix4f getProjectionMatrix(Window window, Camera camera) {
		return this.projectionMatrix.setPerspective(
			-camera.getFov(), (float) window.getWidth() / window.getHeight(),
			camera.getNear(), camera.getFar()
		);
	}
	
	public Matrix4f getModelMatrix(Item item) {
		return this.modelMatrix.translationRotateScale(item.getPosition(), item.getRotation(), item.getScale());
	}
	
	public Matrix4f getModelViewMatrix(Item item, Matrix4f viewMatrix) {
		return viewMatrix.mulAffine(this.getModelMatrix(item), this.modelViewMatrix);
	}
	
	public Matrix4f getOrthoProjectionMatrix(Window window) {
		return this.orthoProjectionMatrix.setOrtho2D(0, window.getWidth(), window.getHeight(), 0);
	}
	
	// these 2 are different?
	public Matrix4f newGetOrthoProjModelMatrix(Item item, Matrix4f orthoMatrix) {
		return orthoMatrix.mulOrthoAffine(this.getModelMatrix(item), this.orthoProjModelMatrix);
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
