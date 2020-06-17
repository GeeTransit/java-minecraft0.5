/*
ahbejarano
Mesh class.
*/

package geetransit.minecraft05.engine;

import java.util.*;
import java.util.function.*;
import java.nio.*;
import org.joml.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Mesh {
	public static final Vector3f DEFAULT_COLOUR = new Vector3f(0.0f, 0.0f, 0.0f);
	
	protected final int vaoId;
	protected final int vertexCount;
	protected final List<Integer> vboIdList;
	
	protected Texture texture;
	protected Vector4f color;

	public Mesh(float[] posArray, int[] indexArray, float[] coordArray) {
		this.vboIdList = new ArrayList<>();
		this.vertexCount = indexArray.length;
		int vboId;
		
		FloatBuffer posBuffer = null;
		IntBuffer indexBuffer = null;
		FloatBuffer coordBuffer = null;
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

			// Unbind the VBO / VAB
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
			
		} finally {
			if (posBuffer != null) memFree(posBuffer);
			if (indexBuffer != null) memFree(indexBuffer);
			if (coordBuffer != null) memFree(coordBuffer);
		}
	}

	public int getVaoId() { return this.vaoId; }
	public int getVertexCount() { return this.vertexCount; }
	
	// texture takes precedence
	public Texture getTexture() { return this.texture; }
	public Vector4f getColor() { return this.color; }
	public Mesh setTexture(Texture texture) { this.texture = texture; return this; }
	public Mesh setColor(Vector3f color) { this.color = new Vector4f(color, 1f); return this; }
	public Mesh setColor(Vector4f color) { this.color = color; return this; }
	public boolean useTexture() { return this.texture != null; }
	
	// prepare mesh
	public void prepare() { this.prepare(null); }
	public void prepare(Mesh lastMesh) {
		if (this == lastMesh)
			return;
		if (this.useTexture()) {
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, this.texture.getId());
		}
		glBindVertexArray(this.vaoId);
	}
	
	// draw elements
	public void render() {
		glDrawElements(GL_TRIANGLES, this.vertexCount, GL_UNSIGNED_INT, 0);
	}
	
	// Restore state
	public void restore() { this.restore(null); }
	public void restore(Mesh nextMesh) {
		if (this == nextMesh)
			return;
		glBindVertexArray(0);
	}
	
	protected void deleteVbos() {
		// Delete the VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		for (int id : this.vboIdList)
			glDeleteBuffers(id);
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
		if (cleanupTexture && this.useTexture()) {
			this.texture.cleanup();
			this.texture = null;
		}
		this.deleteVao();
	}
}
