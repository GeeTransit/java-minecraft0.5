/*
ahbejarano
Texture helper class.
*/

package geetransit.minecraft05.engine;

import java.nio.*;
import org.lwjgl.system.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

	private int id;
	private int width;
	private int height;

	public Texture(String fileName) throws Exception {
		this.loadTexture(fileName);
	}
	public Texture(int id, int width, int height) throws Exception {
		this.id = id;
		this.width = width;
		this.height = height;
	}
	
	public int getId() { return this.id; }
	public int getWidth() { return this.width; }
	public int getHeight() { return this.height; }

	public void bind() {
		glBindTexture(GL_TEXTURE_2D, this.id);
	}
	public void cleanup() {
		glDeleteTextures(this.id);
	}
	
	private void loadTexture(String fileName) throws Exception {
		ByteBuffer image = Utils.loadImage(fileName, (width, height) -> { this.width = width; this.height = height; });

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
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
		// Generate Mip Map
		glGenerateMipmap(GL_TEXTURE_2D);
		
		Utils.freeImage(image);
		
		this.id = textureId;
	}
}
