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
	private Vector4f color;

	public Mesh(float[] posArray, int[] indexArray, float[] coordArray) {
		this.vboIdList = new ArrayList<>();
		this.vertexCount = indexArray.length;
		this.color = new Vector4f();
		this.setColor(DEFAULT_COLOUR);
		
		int vboId;
		FloatBuffer posBuffer = null;
		IntBuffer indexBuffer = null;
		FloatBuffer coordBuffer = null;
		try {
			// Create the VAO
			this.vaoId = glGenVertexArrays();
			glBindVertexArray(this.vaoId);
			
			
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
	
	public Texture getTexture() { return this.texture; }
	public Mesh setTexture(Texture texture) { this.texture = texture; return this; }
	public boolean isTextured() { return this.texture != null; }
	
	public Vector4f getColor() { return this.color; }
	public Mesh setColor(float r, float g, float b) { this.setColor(new Vector3f(r, g, b)); return this; }
	public Mesh setColor(float r, float g, float b, float a) { this.setColor(new Vector4f(r, g, b, a)); return this; }
	public Mesh setColor(Vector3f color) { this.setColor(new Vector4f(color, 1f)); return this; }
	public Mesh setColor(Vector4f color) { this.color.set(color); return this; }
	
	// prepare mesh
	public void prepare() { this.prepare(null); }
	public void prepare(Mesh lastMesh) {
		if (this == lastMesh)
			return;
		if (this.isTextured())
			this.texture.prepare();
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
		if (cleanupTexture && this.isTextured())
			this.texture.cleanup();
		this.deleteVao();
	}
}
