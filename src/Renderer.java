/*
ahbejarano
Renderer class.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import org.joml.*;
import org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {
	private int vboId;
	private int vaoId;
	private ShaderProgram shaderProgram;
	
	private float fov;
	private static final float Z_NEAR = 0.00f;
	private static final float Z_FAR = 1000f;
	private Matrix4f projectionMatrix;
	
	public Renderer() {
		this.fov = (float) java.lang.Math.toRadians(60.0f);
		this.projectionMatrix = new Matrix4f();
	}
	
	public void init(Window window) throws Exception {
		this.shaderProgram = new ShaderProgram();
		this.shaderProgram.createVertexShader(Utils.loadResource("/res/vertex.vs"));
		this.shaderProgram.createFragmentShader(Utils.loadResource("/res/fragment.fs"));
		this.shaderProgram.link();
		this.shaderProgram.createUniform("projectionMatrix");
		this.resize(window);
	}
	
	public void resize(Window window) {
		float aspectRatio = (float) window.getWidth() / window.getHeight();
		this.projectionMatrix.setPerspective(this.fov, aspectRatio, Z_NEAR, Z_FAR);
	}

	public void render(Window window, Mesh mesh) {
		this.shaderProgram.bind();
		this.shaderProgram.setUniform("projectionMatrix", this.projectionMatrix);

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
