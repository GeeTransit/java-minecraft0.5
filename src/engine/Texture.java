/*
ahbejarano
Texture helper class.
*/

package geetransit.minecraft05.engine;

import java.nio.*;
import org.lwjgl.system.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

	public final int id;

	public Texture(String fileName) throws Exception { this(loadTexture(fileName)); }
	public Texture(int id) { this.id = id; }

	public void bind() {
		glBindTexture(GL_TEXTURE_2D, this.id);
	}
	public void cleanup() {
		glDeleteTextures(this.id);
	}
	
	// Note fileName cannot use classpath
	private static int loadTexture(String fileName) throws Exception {
		int width;
		int height;
		ByteBuffer buffer;
		
		// Load Texture file
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer widthBuffer = stack.mallocInt(1);
			IntBuffer heightBuffer = stack.mallocInt(1);
			IntBuffer channelsBuffer = stack.mallocInt(1);

			buffer = stbi_load(fileName, widthBuffer, heightBuffer, channelsBuffer, 4);
			if (buffer == null)
				throw new Exception("Image file [" + fileName  + "] not loaded: " + stbi_failure_reason());

			// Get width and height of image
			width = widthBuffer.get();
			height = heightBuffer.get();
		}

		// Create a new OpenGL texture
		int textureId = glGenTextures();
		// Bind the texture
		glBindTexture(GL_TEXTURE_2D, textureId);

		// Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		// glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		// glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		// Upload the texture data
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		// Generate Mip Map
		glGenerateMipmap(GL_TEXTURE_2D);

		stbi_image_free(buffer);

		return textureId;
	}
}
