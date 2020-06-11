/*
ahbejarano
Renderer class.
*/

package geetransit.minecraft05.game;

import org.lwjgl.system.MemoryUtil;
import geetransit.minecraft05.engine.*;

import java.nio.FloatBuffer;

import geetransit.minecraft05.engine.*;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glViewport;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Renderer {
	private int vboId;
	private int vaoId;
	private ShaderProgram shaderProgram;
	
	public Renderer() {}
	
	public void init() throws Exception {
		this.shaderProgram = new ShaderProgram();
		this.shaderProgram.createVertexShader(Utils.loadResource("/res/vertex.vs"));
		this.shaderProgram.createFragmentShader(Utils.loadResource("/res/fragment.fs"));
		this.shaderProgram.link();
		
		float[] vertices = new float[]{
			 0.0f,  0.5f, 0.0f,
			-0.5f, -0.5f, 0.0f,
			 0.5f, -0.5f, 0.0f
		};
		
		FloatBuffer verticesBuffer = null;
		try {
			verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
			verticesBuffer.put(vertices).flip();

			// Create the VAO and bind to it
			this.vaoId = glGenVertexArrays();
			glBindVertexArray(this.vaoId);

			// Create the VBO and bint to it
			this.vboId = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, this.vboId);
			glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
			// Enable location 0
			glEnableVertexAttribArray(0);
			// Define structure of the data
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

			// Unbind the VBO
			glBindBuffer(GL_ARRAY_BUFFER, 0);

			// Unbind the VAO
			glBindVertexArray(0);
		
		} finally {
			if (verticesBuffer != null) {
				MemoryUtil.memFree(verticesBuffer);
			}
		}
	}

	public void render(Window window) {
		this.shaderProgram.bind();

		// Bind to the VAO
		glBindVertexArray(this.vaoId);

		// Draw the vertices
		glDrawArrays(GL_TRIANGLES, 0, 3);

		// Restore state
		glBindVertexArray(0);

		this.shaderProgram.unbind();
	}

	public void cleanup() {
		if (this.shaderProgram != null) {
			this.shaderProgram.cleanup();
			this.shaderProgram = null;
		}

		glDisableVertexAttribArray(0);

		// Delete the VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(this.vboId);

		// Delete the VAO
		glBindVertexArray(0);
		glDeleteVertexArrays(this.vaoId);
	}
}
