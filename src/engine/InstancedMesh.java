/*
ahbejarano
Instanced mesh class.
*/

package geetransit.minecraft05.engine;

import java.util.*;
import java.util.function.*;
import java.nio.*;
import org.joml.Matrix4f;
import org.lwjgl.system.*;
import static org.lwjgl.opengl.GL33.*;

public class InstancedMesh extends Mesh {
	private static final int FLOAT_BYTES = 4;
	private static final int VECTOR4F_BYTES = 4*FLOAT_BYTES;
	private static final int MATRIX4F_BYTES = 4*VECTOR4F_BYTES;
	private static final int MATRIX4F_SIZE = 4*4;
	
	protected final int instances;
	protected final int modelViewVBO;
	protected FloatBuffer modelViewBuffer;

	public InstancedMesh(float[] posArray, int[] indexArray, float[] coordArray, int instances) {
		super(posArray, indexArray, coordArray);
		this.instances = instances;
		
		glBindVertexArray(this.vaoId);
		
		// model view matrix
		this.modelViewVBO = glGenBuffers();
		this.vboIdList.add(this.modelViewVBO);
		this.modelViewBuffer = MemoryUtil.memAllocFloat(this.instances*MATRIX4F_SIZE);
		glBindBuffer(GL_ARRAY_BUFFER, this.modelViewVBO);
		int stride = 2;
		for (int pointer = 0; pointer < 4; pointer++) {
			glEnableVertexAttribArray(stride);
				System.out.println("glEnableVertexAttribArray:"+glGetError());
			glVertexAttribPointer(stride, 4, GL_FLOAT, false, MATRIX4F_BYTES, pointer*VECTOR4F_BYTES);
				System.out.println("glVertexAttribPointer:"+glGetError());
			glVertexAttribDivisor(stride, 1);
				System.out.println("glVertexAttribDivisor:"+glGetError());
			stride++;
		}
		
		// Unbind the VBO / VAB
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}
	
	@Override
	public void cleanup() {
		super.cleanup();
		if (this.modelViewBuffer != null) {
			MemoryUtil.memFree(this.modelViewBuffer);
			this.modelViewBuffer = null;
		}
	}

	public int getInstances() { return this.instances; }
	
	public void render3DList(List<Item> items, Transformation transformation, Matrix4f viewMatrix) {
		int size = this.getInstances();
		int length = items.size();
		for (int start = 0; start < length; start += size) {
			int end = Math.min(length, start + size);
			List<Item> chunk = items.subList(start, end);
			render3DChunk(chunk, transformation, viewMatrix);
		}
	}

	private void render3DChunk(List<Item> items, Transformation transformation, Matrix4f viewMatrix) {
		this.modelViewBuffer.clear();
		
		int index = 0;
		for (Item item : items) {
			Matrix4f modelViewMatrix = transformation.getModelViewMatrix(item, viewMatrix);
			modelViewMatrix.get(index*MATRIX4F_SIZE, this.modelViewBuffer);
			index++;
		}
		
		glBindBuffer(GL_ARRAY_BUFFER, this.modelViewVBO);
			// System.out.println("glBindBuffer:"+glGetError());
		glBufferData(GL_ARRAY_BUFFER, this.modelViewBuffer, GL_DYNAMIC_DRAW);
			// System.out.println("glBufferData:"+glGetError());
		
		glDrawElementsInstanced(GL_TRIANGLES, this.vertexCount, GL_UNSIGNED_INT, 0, items.size());
			// System.out.println("glDrawElementsInstanced:"+glGetError());
		// glDrawArraysInstanced(GL_TRIANGLES, 0, this.vertexCount, items.size());
			// System.out.println("glDrawArraysInstanced:"+glGetError());
		glBindBuffer(GL_ARRAY_BUFFER, 0);
			// System.out.println("glBindBuffer(0):"+glGetError());
		// throw new RuntimeException("why are you my clarity");
	}
}
