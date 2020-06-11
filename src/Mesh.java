/*
ahbejarano
Mesh class.
*/

package geetransit.minecraft05.engine;

import java.nio.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Mesh {
	private final int vaoId;
	private final int posVboId;
	private final int idxVboId;
	private final int vertexCount;

	public Mesh(float[] positions, int[] indices) {
		FloatBuffer positionBuffer = null;
		IntBuffer indicesBuffer = null;
		try {
			positionBuffer = memAllocFloat(positions.length);
			positionBuffer.put(positions).flip();
			this.vertexCount = indices.length;

			// Create the VAO / VBO and bind to it
			this.vaoId = glGenVertexArrays();
			glBindVertexArray(this.vaoId);
			
			this.posVboId = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, this.posVboId);
			glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL_STATIC_DRAW);
			
			this.idxVboId = glGenBuffers();
			indicesBuffer = memAllocInt(indices.length);
			indicesBuffer.put(indices).flip();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.idxVboId);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
			
			// Enable location 0 (attribute)
			glEnableVertexAttribArray(0);
			
			// Define structure of the data
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

			// Unbind the VBO / VAB
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
		
		} finally {
			if (positionBuffer != null) {
				memFree(positionBuffer);
			}
			if (indicesBuffer != null) {
				memFree(indicesBuffer);
			}
		}
	}

	public int getVaoId() { return this.vaoId; }
	public int getVertexCount() { return this.vertexCount; }

	public void cleanup() {
		glDisableVertexAttribArray(0);

		// Delete the VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(this.posVboId);
		glDeleteBuffers(this.idxVboId);

		// Delete the VAO
		glBindVertexArray(0);
		glDeleteVertexArrays(this.vaoId);
	}
}
