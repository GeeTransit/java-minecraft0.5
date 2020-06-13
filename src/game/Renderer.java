/*
ahbejarano
Renderer class.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import java.util.*;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {
	private Shader shader;
	
	private float fov;
	private static final float Z_NEAR = 0.01f;
	private static final float Z_FAR = 1000f;
	private Transformation transformation;
	
	public Renderer(float fov) {
		this.fov = (float) Math.toRadians(fov);
		this.transformation = new Transformation();
	}
	public Renderer() { this(60f); }
	
	public void init() throws Exception {
		this.shader = new Shader();
		this.shader.createVertexShader(Utils.loadResource("/res/vertex.vs"));
		this.shader.createFragmentShader(Utils.loadResource("/res/fragment.fs"));
		this.shader.link();
		
		this.shader.createUniform("projectionMatrix");
		this.shader.createUniform("modelViewMatrix");
		this.shader.createUniform("texture_sampler");
	}

	public void render(Window window, Camera camera, List<Item> items) {
		this.shader.bind();
		
		// projection
		Matrix4f projectionMatrix = this.transformation.getProjectionMatrix(
			this.fov,
			window.getWidth(),
			window.getHeight(),
			Z_NEAR,
			Z_FAR
		);
		this.shader.setUniform("projectionMatrix", projectionMatrix);
		
		// view
		Matrix4f viewMatrix = this.transformation.getViewMatrix(camera);
		
		// Draw meshes
		this.shader.setUniform("texture_sampler", 0);
		for (Item item : items) {
			Matrix4f modelViewMatrix = this.transformation.getModelViewMatrix(item, viewMatrix);
			this.shader.setUniform("modelViewMatrix", modelViewMatrix);
			item.mesh.render();
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
