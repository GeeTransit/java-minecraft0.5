/*
George Zhang
Class to help find closest intersecting cube.
*/

package geetransit.minecraft05.engine;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Intersectionf;

public class ClosestItem {
	public float distance;  // distance from camera
	public Item closest;
	public Vector3f hit;  // position of intersection
	public Vector3f direction;  // points away from camera
	
	private final Vector3f max;
	private final Vector3f min;
	private final Vector2f nearFar;
	
	public ClosestItem(List<Item> items, Camera camera) {
		this.distance = Float.POSITIVE_INFINITY;
		this.closest = null;
		this.hit = null;
		this.direction = new Vector3f();
		
		this.min = new Vector3f();
		this.max = new Vector3f();
		this.nearFar = new Vector2f();
		
		// get camera direction
		camera.getViewMatrix().positiveZ(this.direction);
		this.direction
			.negate()
			.normalize();
		
		// loop through all items
		for (Item item : items) {
			this.min.set(item.getPosition());
			this.max.set(item.getPosition());
			this.min.add(-item.getScale(), -item.getScale(), -item.getScale());
			this.max.add(item.getScale(), item.getScale(), item.getScale());
			
			// check if intersects and is closer
			if (Intersectionf.intersectRayAab(
				camera.getPosition(), this.direction,
				this.min, this.max, this.nearFar
			)) {
				if (this.nearFar.x < this.distance) {
					this.distance = nearFar.x;
					this.closest = item;
					this.hit = new Vector3f(camera.getPosition());
					this.hit.add(this.direction.mul(this.distance, new Vector3f()));
				}
			}
		}
	}
}
