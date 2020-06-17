/*
ahbejarano
Game item wrapper class.
*/

package geetransit.minecraft05.engine;

import org.joml.Vector3f;
import org.joml.Quaternionf;

public class Item {
	protected Mesh mesh;

	private final Vector3f position;
	private final Quaternionf rotation;  // Degrees, not radians.
	private float scale;

	public Item(Mesh mesh) {
		this();
		this.mesh = mesh;
	}
	public Item() {
		this.position = new Vector3f();
		this.rotation = new Quaternionf();
		this.scale = 1;
	}
	
	public void render(Window window) {
		this.mesh.prepare();
		this.mesh.render();
		this.mesh.restore();
	}
	
	public void cleanup() {
		this.mesh.cleanup();
	}
	
	public Mesh getMesh() { return this.mesh; }
	public Vector3f getPosition() { return this.position; }
	public Quaternionf getRotation() { return this.rotation; }
	public float getScale() { return this.scale; }
	
	public Item setPosition(Vector3f position) { this.position.set(position); return this; }
	public Item setPosition(float x, float y, float z) {
		this.position.x = x;
		this.position.y = y;
		this.position.z = z;
		return this;
	}
	
	public Item setRotation(Quaternionf rotation) { this.rotation.set(rotation); return this; }
	public Item setRotation(float x, float y, float z) {
		this.rotation.x = x;
		this.rotation.y = y;
		this.rotation.z = z;
		return this;
	}
	
	public Item setScale(float scale) {
		this.scale = scale;
		return this;
	}
}
