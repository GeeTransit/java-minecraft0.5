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
			.rotate((float) Math.toRadians(camera.rotation.x), new Vector3f(1, 0, 0))
			.rotate((float) Math.toRadians(camera.rotation.y), new Vector3f(0, 1, 0))
			// Then do the translation
			.translate(-camera.position.x, -camera.position.y, -camera.position.z);
	}
	
	public Matrix4f getModelViewMatrix(Item item, Matrix4f viewMatrix) {
		return this.modelViewMatrix
			.set(viewMatrix)
			.translate(item.position)
			.rotateX((float) Math.toRadians(-item.rotation.x))
			.rotateY((float) Math.toRadians(-item.rotation.y))
			.rotateZ((float) Math.toRadians(-item.rotation.z))
			.scale(item.scale);
	}
}
