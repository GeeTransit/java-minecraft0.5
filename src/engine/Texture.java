/*
ahbejarano
Texture helper class.
*/

package geetransit.minecraft05.engine;

import java.nio.*;

import static org.lwjgl.opengl.GL30.*;

public class Texture {
	private final int id;
	private final int width;
	private final int length;

	public Texture(String fileName) {
		int widthArray[] = {0};
		int lengthArray[] = {0};
		ByteBuffer image = Utils.loadImage(fileName, widthArray, lengthArray);
		this.width = widthArray[0];
		this.length = lengthArray[0];

		// Create a new OpenGL texture
		this.id = glGenTextures();
		// Bind the texture
		this.bind();

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
		glBindTexture(GL_TEXTURE_2D, this.id);
	}
	public void prepare() {
		glActiveTexture(GL_TEXTURE0);
		this.bind();
	}
	public void cleanup() {
		glDeleteTextures(this.id);
	}
}
