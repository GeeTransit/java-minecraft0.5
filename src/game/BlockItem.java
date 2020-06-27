/*
George Zhang
Block item subclass.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import org.joml.Vector3f;
import org.joml.Quaternionf;

public class BlockItem extends Item {
	private boolean selected;

	public BlockItem(Mesh mesh) {
		super(mesh);
		this.selected = false;
	}

	public BlockItem setPosition(Vector3f position) { super.setPosition(position); return this; }
	public BlockItem setPosition(float x, float y, float z) { super.setPosition(x, y, z); return this; }
	public BlockItem setRotation(Quaternionf rotation) { super.setRotation(rotation); return this; }
	public BlockItem setRotation(float x, float y, float z) { super.setRotation(x, y, z); return this; }
	public BlockItem setScale(float scale) { super.setScale(scale); return this; }

	public boolean isSelected() { return this.selected; }
	public BlockItem setSelected(boolean selected) { this.selected = selected; return this; }
}
