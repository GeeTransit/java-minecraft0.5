/*
ahbejarano
Texture helper class.
*/

package geetransit.minecraft05.engine;

import java.nio.*;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Texture {

	private int id;
	private int width;
	private int length;

	public Texture(String fileName) throws Exception {
		this.loadTexture(fileName);
	}
	public Texture(int id, int width, int length) throws Exception {
		this.id = id;
		this.width = width;
		this.length = length;
	}

	public int getId() { return this.id; }
	public int getWidth() { return this.width; }
	public int getLength() { return this.length; }

	public void bind() {
		glBindTexture(GL_TEXTURE_2D, this.id);
	}
	public void prepare() {
		glActiveTexture(GL_TEXTURE0);
		this.bind();
	}
	public void cleanup() {
		glDeleteTextures(this.id);
	}

	private void loadTexture(String fileName) throws Exception {
		ByteBuffer image = Utils.loadImage(fileName, (w, l) -> { this.width = w; this.length = l; });

		// Create a new OpenGL texture
		int textureId = glGenTextures();
		// Bind the texture
		glBindTexture(GL_TEXTURE_2D, textureId);

		// Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		// make text easier to read
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		// Upload the texture data
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.length, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
		// Generate Mip Map
		glGenerateMipmap(GL_TEXTURE_2D);

		Utils.freeImage(image);

		this.id = textureId;
	}
}
