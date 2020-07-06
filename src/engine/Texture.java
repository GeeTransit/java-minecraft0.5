/*
ahbejarano
Texture helper class.
*/

package geetransit.minecraft05.engine;

import java.nio.*;

import static org.lwjgl.opengl.GL30.*;

public class Texture implements AutoCloseable {
	private final int id;
	private final int width;
	private final int length;

	public Texture(String file) {
		try (Image image = new Image(file)) {
			this.width = image.width;
			this.length = image.length;

			// Create a new OpenGL texture
			this.id = glGenTextures();
			// Bind the texture
			glBindTexture(GL_TEXTURE_2D, this.id);

			// Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
			glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

			// make text easier to read
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

			// Upload the texture data
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.length, 0, GL_RGBA, GL_UNSIGNED_BYTE, image.buffer);
			// Generate Mip Map
			glGenerateMipmap(GL_TEXTURE_2D);
		}
	}

	protected Texture(int id, int width, int length) {
		this.id = id;
		this.width = width;
		this.length = length;
	}

	public int getId() { return this.id; }
	public int getWidth() { return this.width; }
	public int getLength() { return this.length; }

	public void bind() {
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, this.id);
	}

	public void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	@Override
	public void close() {
		glDeleteTextures(this.id);
	}
}
