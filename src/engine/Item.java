/*
ahbejarano
Game item class.
*/

package geetransit.minecraft05.engine;

import org.joml.Vector3f;
import org.joml.Quaternionf;
import org.joml.Matrix4f;

public class Item {
	private Mesh mesh;
	private final Vector3f position;
	private final Quaternionf rotation;  // Degrees, not radians.
	private float scale;

	public Item(Mesh mesh) {
		this();
		this.mesh = mesh;
	}
	protected Item() {
		this.position = new Vector3f();
		this.rotation = new Quaternionf();
		this.scale = 1;
	}

	public Mesh getMesh() { return this.mesh; }
	protected Item setMesh(Mesh mesh) { this.mesh = mesh; return this; }

	public Vector3f getPosition() { return this.position; }
	public Item setPosition(Vector3f position) { this.position.set(position); return this; }
	public Item setPosition(float x, float y, float z) {
		this.position.x = x;
		this.position.y = y;
		this.position.z = z;
		return this;
	}

	public Quaternionf getRotation() { return this.rotation; }
	public Item setRotation(Quaternionf rotation) { this.rotation.set(rotation); return this; }
	public Item setRotation(float x, float y, float z) {
		this.rotation.x = x;
		this.rotation.y = y;
		this.rotation.z = z;
		return this;
	}

	public float getScale() { return this.scale; }
	public Item setScale(float scale) {
		this.scale = scale;
		return this;
	}

	public Matrix4f buildModelMatrix(Matrix4f result) {
		return result.translationRotateScale(this.position, this.rotation, this.scale);
	}

	public Matrix4f buildModelViewMatrix(Matrix4f viewMatrix, Matrix4f result) {
		return viewMatrix.mulAffine(this.buildModelMatrix(result), result);
	}

	// these 2 are different?
	public Matrix4f newBuildOrthoProjModelMatrix(Matrix4f orthoMatrix, Matrix4f result) {
		return orthoMatrix.mulOrthoAffine(this.buildModelMatrix(result), result);
	}
	public Matrix4f buildOrthoProjModelMatrix(Matrix4f orthoMatrix, Matrix4f result) {
		return result
			.set(orthoMatrix)
			.translate(this.position)
			.rotateX((float) Math.toRadians(-this.rotation.x))
			.rotateY((float) Math.toRadians(-this.rotation.y))
			.rotateZ((float) Math.toRadians(-this.rotation.z))
			.scale(this.scale);
	}
}
