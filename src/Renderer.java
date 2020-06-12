/*
ahbejarano
Renderer class.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {
	private Shader shader;
	
	private float fov;
	private static final float Z_NEAR = 0.01f;
	private static final float Z_FAR = 1000f;
	private Matrix4f projectionMatrix;
	
	public Renderer() {
		this.fov = (float) Math.toRadians(60.0f);
		this.projectionMatrix = new Matrix4f();
	}
	
	public void init() throws Exception {
		this.shader = new Shader();
		this.shader.createVertexShader(Utils.loadResource("/res/vertex.vs"));
		this.shader.createFragmentShader(Utils.loadResource("/res/fragment.fs"));
		this.shader.link();
		this.shader.createUniform("projectionMatrix");
	}
	
	public void resize(Window window) {
		float aspectRatio = (float) window.getWidth() / window.getHeight();
		this.projectionMatrix.setPerspective(this.fov, aspectRatio, Z_NEAR, Z_FAR);
	}

	public void render(Window window, Mesh[] meshes) {
		this.shader.bind();
		this.resize(window);
		this.shader.setUniform("projectionMatrix", this.projectionMatrix);

		// Draw meshes
		for (Mesh mesh : meshes) {
			mesh.render();
		}

		this.shader.unbind();
	}

	public void cleanup() {
		if (this.shader != null) {
			this.shader.cleanup();
			this.shader = null;
		}
	}
}
