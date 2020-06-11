/*
ahbejarano
Mesh class.
*/

package geetransit.minecraft05.engine;

import java.nio.FloatBuffer;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {
	private final int vaoId;
	private final int vboId;
	private final int vertexCount;

	public Mesh(float[] vertices) {
		FloatBuffer verticesBuffer = null;
		try {
			verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
			verticesBuffer.put(vertices).flip();
			this.vertexCount = vertices.length / 3;

			// Create the VAO / VBO and bind to it
			this.vaoId = glGenVertexArrays();
			glBindVertexArray(this.vaoId);
			this.vboId = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, this.vboId);
			glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
			
			// Enable location 0 (attribute)
			glEnableVertexAttribArray(0);
			
			// Define structure of the data
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

			// Unbind the VBO / VAB
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
		
		} finally {
			if (verticesBuffer != null) {
				MemoryUtil.memFree(verticesBuffer);
			}
		}
	}

	public int getVaoId() { return this.vaoId; }
	public int getVertexCount() { return this.vertexCount; }

	public void cleanUp() {
		glDisableVertexAttribArray(0);

		// Delete the VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(vboId);

		// Delete the VAO
		glBindVertexArray(0);
		glDeleteVertexArrays(vaoId);
	}
}
