/*
ahbejarano
Mesh class.
*/

package geetransit.minecraft05.engine;

import java.util.*;
import java.nio.*;
import org.joml.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Mesh {
	private static final Vector3f DEFAULT_COLOUR = new Vector3f(1.0f, 1.0f, 1.0f);
	
	private final int vaoId;
	private final int vertexCount;
	private final List<Integer> vboIdList;
	
	private Texture texture;
	private Vector3f color;

	public Mesh(float[] positions, int[] indices, float[] coords, float[] normals) {
		this.vboIdList = new ArrayList<>();
		this.vertexCount = indices.length;
		int vboId;
		
		FloatBuffer positionBuffer = null;
		IntBuffer indexBuffer = null;
		FloatBuffer coordsBuffer = null;
		FloatBuffer normalsBuffer = null;
		try {
			// Create the VAO
			this.vaoId = glGenVertexArrays();
			glBindVertexArray(this.vaoId);
			
			this.setColor(DEFAULT_COLOUR);
			
			// Position VBO
			vboId = glGenBuffers();
			this.vboIdList.add(vboId);
			positionBuffer = memAllocFloat(positions.length);
			positionBuffer.put(positions).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(0);
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			
			// Index VBO
			vboId = glGenBuffers();
			this.vboIdList.add(vboId);
			indexBuffer = memAllocInt(indices.length);
			indexBuffer.put(indices).flip();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
			
			// texture coords VBO
			vboId = glGenBuffers();
			this.vboIdList.add(vboId);
			coordsBuffer = memAllocFloat(coords.length);
			coordsBuffer.put(coords).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, coordsBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(1);
			glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			
			// normals VBO
			vboId = glGenBuffers();
			this.vboIdList.add(vboId);
			normalsBuffer = memAllocFloat(normals.length);
			normalsBuffer.put(normals).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(2);
			glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

			// Unbind the VBO / VAB
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
			
		
		} finally {
			if (positionBuffer != null) memFree(positionBuffer);
			if (indexBuffer != null) memFree(indexBuffer);
			if (coordsBuffer != null) memFree(coordsBuffer);
			if (normalsBuffer != null) memFree(normalsBuffer);
		}
	}

	public int getVaoId() { return this.vaoId; }
	public int getVertexCount() { return this.vertexCount; }
	
	// texture takes precedence
	public void setTexture(Texture texture) { this.texture = texture; }
	public Texture getTexture() { return this.texture; }
	public void setColor(Vector3f color) { this.color = color; }
	public Vector3f getColor() { return this.color; }
	public boolean isTexture() { return this.texture != null; }
	
	public void render() {
		if (this.isTexture()) {
			// Activate and bind first texture unit
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, this.texture.id);
		}
		
		// Bind and draw the mesh
		glBindVertexArray(this.vaoId);
		glDrawElements(GL_TRIANGLES, this.vertexCount, GL_UNSIGNED_INT, 0);

		// Restore state
		glBindVertexArray(0);
	}

	public void cleanup() {
		glDisableVertexAttribArray(0);

		// Delete the VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		this.vboIdList.stream().forEach(id -> glDeleteBuffers(id));
		
		if (this.isTexture())
			this.texture.cleanup();

		// Delete the VAO
		glBindVertexArray(0);
		glDeleteVertexArrays(this.vaoId);
	}
}
