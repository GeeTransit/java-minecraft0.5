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
	private Transformation transformation;
	
	public Renderer() {
		this.fov = (float) Math.toRadians(60.0f);
		this.transformation = new Transformation();
	}
	
	public void init() throws Exception {
		this.shader = new Shader();
		this.shader.createVertexShader(Utils.loadResource("/res/vertex.vs"));
		this.shader.createFragmentShader(Utils.loadResource("/res/fragment.fs"));
		this.shader.link();
		
		this.shader.createUniform("projectionMatrix");
		this.shader.createUniform("worldMatrix");
	}

	public void render(Window window, Item[] items) {
		this.shader.bind();
		Matrix4f projectionMatrix = this.transformation.getProjectionMatrix(
			this.fov,
			window.getWidth(),
			window.getHeight(),
			Z_NEAR,
			Z_FAR
		);
		this.shader.setUniform("projectionMatrix", projectionMatrix);

		// Draw meshes
		for (Item item : items) {
			Matrix4f worldMatrix = this.transformation.getWorldMatrix(
                item.getPosition(),
                item.getRotation(),
                item.getScale()
			);
			this.shader.setUniform("worldMatrix", worldMatrix);
			item.getMesh().render();
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
