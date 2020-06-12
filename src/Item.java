/*
ahbejarano
Game item wrapper class.
*/

package geetransit.minecraft05.engine;

import org.joml.Vector3f;

public class Item {
	private final Mesh mesh;

	private final Vector3f position;
	private final Vector3f rotation;  // Degrees, not radians.
	private float scale;

	public Item(Mesh mesh) {
		this.mesh = mesh;
		this.position = new Vector3f();
		this.rotation = new Vector3f();
		this.scale = 1;
	}

	public Mesh getMesh() { return mesh; }
	public Vector3f getPosition() { return this.position; }
	public Vector3f getRotation() { return this.rotation; }
	public float getScale() { return this.scale; }

	public void setPosition(float x, float y, float z) {
		this.position.x = x;
		this.position.y = y;
		this.position.z = z;
	}

	public void setRotation(float x, float y, float z) {
		this.rotation.x = x;
		this.rotation.y = y;
		this.rotation.z = z;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}
}
