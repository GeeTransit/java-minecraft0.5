/*
ahbejarano
Text item class.
*/

package geetransit.minecraft05.engine;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Quaternionf;

public class TextItem extends Item {
	private String text;
	private final FontTexture fontTexture;

	public TextItem(String text, FontTexture fontTexture) throws Exception {
		super(fontTexture.buildMesh(text));
		this.text = text;
		this.fontTexture = fontTexture;
	}

	protected TextItem setMesh(Mesh mesh) { super.setMesh(mesh); return this; }
	public TextItem setPosition(Vector3f position) { super.setPosition(position); return this; }
	public TextItem setPosition(float x, float y, float z) { super.setPosition(x, y, z); return this; }
	public TextItem setRotation(Quaternionf rotation) { super.setRotation(rotation); return this; }
	public TextItem setRotation(float x, float y, float z) { super.setRotation(x, y, z); return this; }
	public TextItem setScale(float scale) { super.setScale(scale); return this; }

	public String getText() { return this.text; }
	public FontTexture getFontTexture() { return this.fontTexture; }

	public TextItem setText(String text) {
		this.text = text;
		Vector4f color = this.getMesh().getColor();
		this.getMesh().cleanup(false);
		this.setMesh(this.fontTexture.buildMesh(this.text));
		this.getMesh().setColor(color);
		return this;
	}
}
