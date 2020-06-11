/*
ahbejarano
Renderer class.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;
import static org.lwjgl.opengl.GL30.*;

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
		glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);

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
