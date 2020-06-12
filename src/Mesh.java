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
	private final int vertexCount;
	private final int positionVboId;
	private final int colourVboId;
	private final int indexVboId;

	public Mesh(float[] positions, float[] colours, int[] indices) {
		FloatBuffer positionBuffer = null;
		FloatBuffer colourBuffer = null;
		IntBuffer indexBuffer = null;
		try {
			this.vertexCount = indices.length;

			// Create the VAO
			this.vaoId = glGenVertexArrays();
			glBindVertexArray(this.vaoId);
			
			// Position VBO
			this.positionVboId = glGenBuffers();
			positionBuffer = memAllocFloat(positions.length);
			positionBuffer.put(positions).flip();
			glBindBuffer(GL_ARRAY_BUFFER, this.positionVboId);
			glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(0);
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			
			// Colour VBO
			this.colourVboId = glGenBuffers();
			colourBuffer = memAllocFloat(colours.length);
			colourBuffer.put(colours).flip();
			glBindBuffer(GL_ARRAY_BUFFER, this.colourVboId);
			glBufferData(GL_ARRAY_BUFFER, colourBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(1);
			glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
			
			// Index VBO
			this.indexVboId = glGenBuffers();
			indexBuffer = memAllocInt(indices.length);
			indexBuffer.put(indices).flip();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indexVboId);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

			// Unbind the VBO / VAB
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
		
		} finally {
			if (positionBuffer != null) {
				memFree(positionBuffer);
			}
			if (colourBuffer != null) {
				memFree(colourBuffer);
			}
			if (indexBuffer != null) {
				memFree(indexBuffer);
			}
		}
	}

	public int getVaoId() { return this.vaoId; }
	public int getVertexCount() { return this.vertexCount; }
	
	public void render() {
		glBindVertexArray(this.vaoId);
		glDrawElements(GL_TRIANGLES, this.vertexCount, GL_UNSIGNED_INT, 0);

		// Restore state
		glBindVertexArray(0);
	}

	public void cleanup() {
		glDisableVertexAttribArray(0);

		// Delete the VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(this.positionVboId);
		glDeleteBuffers(this.colourVboId);
		glDeleteBuffers(this.indexVboId);

		// Delete the VAO
		glBindVertexArray(0);
		glDeleteVertexArrays(this.vaoId);
	}
}
