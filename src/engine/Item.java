/*
ahbejarano
Game item wrapper class.
*/

package geetransit.minecraft05.engine;

import org.joml.Vector3f;

public class Item {
	public final Mesh mesh;

	public final Vector3f position;
	public final Vector3f rotation;  // Degrees, not radians.
	public float scale;

	public Item(Mesh mesh) {
		this.mesh = mesh;
		this.position = new Vector3f();
		this.rotation = new Vector3f();
		this.scale = 1;
	}
	
	public Item setPosition(float x, float y, float z) {
		this.position.x = x;
		this.position.y = y;
		this.position.z = z;
		return this;
	}
	
	public Item setScale(float scale) {
		this.scale = scale;
		return this;
	}
}
