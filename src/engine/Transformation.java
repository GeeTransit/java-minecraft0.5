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

	public Transformation() {
		this.projectionMatrix = new Matrix4f();
		this.viewMatrix = new Matrix4f();
		this.modelViewMatrix = new Matrix4f();
	}

	public final Matrix4f getProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
		return this.projectionMatrix.setPerspective(fov, width / height, zNear, zFar);
	}
	
	public Matrix4f getViewMatrix(Camera camera) {
		return this.viewMatrix
			.identity()
			// First do the rotation so camera rotates over its position
			.rotate((float) Math.toRadians(camera.getRotation().x), new Vector3f(1, 0, 0))
			.rotate((float) Math.toRadians(camera.getRotation().y), new Vector3f(0, 1, 0))
			// Then do the translation
			.translate(camera.getPosition().negate(new Vector3f()));
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
}
