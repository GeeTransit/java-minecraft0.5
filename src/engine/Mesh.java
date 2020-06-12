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
	private final int[] vboIds;

	public Mesh(float[] positions, float[] colors, int[] indices) {
		this.vertexCount = indices.length;
		
		FloatBuffer positionBuffer = null;
		FloatBuffer colorBuffer = null;
		IntBuffer indexBuffer = null;
		try {
			// Create the VAO
			this.vaoId = glGenVertexArrays();
			glBindVertexArray(this.vaoId);
			
			// Position VBO
			int positionVboId = glGenBuffers();
			positionBuffer = memAllocFloat(positions.length);
			positionBuffer.put(positions).flip();
			glBindBuffer(GL_ARRAY_BUFFER, positionVboId);
			glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(0);
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			
			// Color VBO
			int colorVboId = glGenBuffers();
			colorBuffer = memAllocFloat(colors.length);
			colorBuffer.put(colors).flip();
			glBindBuffer(GL_ARRAY_BUFFER, colorVboId);
			glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(1);
			glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
			
			// Index VBO
			int indexVboId = glGenBuffers();
			indexBuffer = memAllocInt(indices.length);
			indexBuffer.put(indices).flip();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexVboId);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

			// Unbind the VBO / VAB
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
			
			// Add VBOs to VBO array
			this.vboIds = new int[]{positionVboId, colorVboId, indexVboId};
		
		} finally {
			if (positionBuffer != null) memFree(positionBuffer);
			if (colorBuffer != null) memFree(colorBuffer);
			if (indexBuffer != null) memFree(indexBuffer);
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
		for (int id : this.vboIds)
			glDeleteBuffers(id);

		// Delete the VAO
		glBindVertexArray(0);
		glDeleteVertexArrays(this.vaoId);
	}
}
