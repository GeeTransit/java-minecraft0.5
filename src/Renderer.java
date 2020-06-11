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
	}

	public void render(Window window, Mesh mesh) {
		this.shaderProgram.bind();

		// Draw mesh
		glBindVertexArray(mesh.getVaoId());
		glDrawArrays(GL_TRIANGLES, 0, mesh.getVertexCount());

		// Restore state
		glBindVertexArray(0);

		this.shaderProgram.unbind();
	}

	public void cleanup() {
		if (this.shaderProgram != null) {
			this.shaderProgram.cleanup();
			this.shaderProgram = null;
		}
	}
}
