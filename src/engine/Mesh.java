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
	private static final Vector3f DEFAULT_COLOUR = new Vector3f(0.0f, 0.0f, 0.0f);
	
	private final int vaoId;
	private final int vertexCount;
	private final List<Integer> vboIdList;
	
	private Texture texture;
	private Vector3f color;

	public Mesh(float[] posArray, int[] indexArray, float[] coordArray, float[] normalArray) {
		this.vboIdList = new ArrayList<>();
		this.vertexCount = indexArray.length;
		int vboId;
		
		FloatBuffer posBuffer = null;
		IntBuffer indexBuffer = null;
		FloatBuffer coordBuffer = null;
		FloatBuffer normalBuffer = null;
		try {
			// Create the VAO
			this.vaoId = glGenVertexArrays();
			glBindVertexArray(this.vaoId);
			
			this.setColor(DEFAULT_COLOUR);
			
			// Position VBO
			vboId = glGenBuffers();
			this.vboIdList.add(vboId);
			posBuffer = memAllocFloat(posArray.length);
			posBuffer.put(posArray).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(0);
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			
			// Index VBO
			vboId = glGenBuffers();
			this.vboIdList.add(vboId);
			indexBuffer = memAllocInt(indexArray.length);
			indexBuffer.put(indexArray).flip();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
			
			// texture coords VBO
			vboId = glGenBuffers();
			this.vboIdList.add(vboId);
			coordBuffer = memAllocFloat(coordArray.length);
			coordBuffer.put(coordArray).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, coordBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(1);
			glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			
			// normals VBO
			vboId = glGenBuffers();
			this.vboIdList.add(vboId);
			normalBuffer = memAllocFloat(normalArray.length);
			normalBuffer.put(normalArray).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
			glEnableVertexAttribArray(2);
			glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

			// Unbind the VBO / VAB
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
			
		} finally {
			if (posBuffer != null) memFree(posBuffer);
			if (indexBuffer != null) memFree(indexBuffer);
			if (coordBuffer != null) memFree(coordBuffer);
			if (normalBuffer != null) memFree(normalBuffer);
		}
	}

	public int getVaoId() { return this.vaoId; }
	public int getVertexCount() { return this.vertexCount; }
	
	// texture takes precedence
	public Texture getTexture() { return this.texture; }
	public Vector3f getColor() { return this.color; }
	public Mesh setTexture(Texture texture) { this.texture = texture; return this; }
	public Mesh setColor(Vector3f color) { this.color = color; return this; }
	public boolean isTexture() { return this.texture != null; }
	
	public void render() {
		if (this.isTexture()) {
			// Activate and bind first texture unit
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, this.texture.getId());
		}
		
		// Bind and draw the mesh
		glBindVertexArray(this.vaoId);
		glDrawElements(GL_TRIANGLES, this.vertexCount, GL_UNSIGNED_INT, 0);

		// Restore state
		glBindVertexArray(0);
	}
	
	protected void deleteVbos() {
		// Delete the VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		this.vboIdList.stream().forEach(id -> glDeleteBuffers(id));
	}
	
	protected void disableVao() {
		glDisableVertexAttribArray(0);
	}
	
	protected void deleteVao() {
		// Delete the VAO
		glBindVertexArray(0);
		glDeleteVertexArrays(this.vaoId);
	}
	
	public void cleanup() { this.cleanup(true); }
	public void cleanup(boolean cleanupTexture) {
		this.disableVao();
		this.deleteVbos();
		if (cleanupTexture && this.isTexture())
			this.texture.cleanup();
		this.deleteVao();
	}
}
