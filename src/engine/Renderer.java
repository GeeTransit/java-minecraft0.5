/*
ahbejarano
Renderer class.
*/

package geetransit.minecraft05.engine;

import java.util.*;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL30.*;

public abstract class Renderer {
	protected SceneRender parent;
	protected Shader shader;
	protected Transformation transformation;
	
	private static final float Z_NEAR = 0.01f;
	private static final float Z_FAR = 1000f;
	
	public Renderer(SceneRender parent) {
		this.parent = parent;
		this.transformation = new Transformation();
	}
	
	public SceneRender getParent() { return this.parent; }
	public Renderer setParent(SceneRender parent) { this.parent = parent; return this; }
	
	public void init(Window window) throws Exception {
		this.shader = this.create(window);
	}
	
	public abstract Shader create(Window window) throws Exception;
	
	public Shader createShader(String vertex, String fragment) throws Exception {
		Shader shader = new Shader();
		shader.createVertexShader(Utils.loadResource(vertex));
		shader.createFragmentShader(Utils.loadResource(fragment));
		shader.link();
		return shader;
	}
	
	public Shader create3D(String vertex, String fragment) throws Exception {
		Shader shader = this.createShader(vertex, fragment);
		shader.createUniform("projectionMatrix");
		shader.createUniform("modelViewMatrix");
		shader.createUniform("texture_sampler");
		shader.createUniform("color");
		shader.createUniform("useTexture");
		return shader;
	}
	
	public Shader create2D(String vertex, String fragment) throws Exception {
		Shader shader = this.createShader(vertex, fragment);
		shader.createUniform("projModelMatrix");
		shader.createUniform("texture_sampler");
		shader.createUniform("color");
		shader.createUniform("useTexture");
		return shader;
	}
	
	public abstract void render(Window window);
	
	public void render3D(Window window, Camera camera) {
		this.render3D(window, camera, this.parent.getItems());
	}
	public void render3D(Window window, Camera camera, Iterable<Item> items) {
		this.shader.bind();
		
		// projection
		Matrix4f projectionMatrix = this.transformation.getProjectionMatrix(window, camera);
		this.shader.setUniform("projectionMatrix", projectionMatrix);
		
		// view
		Matrix4f viewMatrix = this.transformation.getViewMatrix(camera);
		
		// Draw meshes
		this.shader.setUniform("texture_sampler", 0);
		for (Item item : items) {
			Matrix4f modelViewMatrix = this.transformation.getModelViewMatrix(item, viewMatrix);
			this.shader.setUniform("modelViewMatrix", modelViewMatrix);
			this.shader.setUniform("color", item.getMesh().getColor());
			this.shader.setUniform("useTexture", item.getMesh().isTexture());
			item.render(window);
		}

		this.shader.unbind();
	}
	
	public void render2D(Window window) {
		this.render2D(window, this.parent.getItems());
	}
	public void render2D(Window window, Iterable<Item> items) {
		this.shader.bind();
		
		// source # https://stackoverflow.com/a/5467636
		glDepthMask(false);  // disable writes to Z-Buffer
		glDisable(GL_DEPTH_TEST);  // disable depth-testing

		Matrix4f orthoMatrix = this.transformation.getOrthoProjectionMatrix(window);
		for (Item item : items) {
			Matrix4f projModelMatrix = this.transformation.getOrthoProjModelMatrix(item, orthoMatrix);
			this.shader.setUniform("projModelMatrix", projModelMatrix);
			this.shader.setUniform("color", item.getMesh().getColor());
			this.shader.setUniform("useTexture", item.getMesh().isTexture());
			item.render(window);
		}
		
		glDepthMask(true);
		glEnable(GL_DEPTH_TEST);

		this.shader.unbind();
	}
	
	public void cleanup() {
		if (this.shader != null) {
			this.shader.cleanup();
			this.shader = null;
		}
	}
	
	public void renderSkybox(Window window, Camera camera) {
		this.renderSkybox(window, camera, this.parent.getItems());
	}
	public void renderSkybox(Window window, Camera camera, Iterable<Item> items) {
		this.shader.bind();
		
		// projection
		Matrix4f projectionMatrix = this.transformation.getProjectionMatrix(window, camera);
		this.shader.setUniform("projectionMatrix", projectionMatrix);
		
		// view
		Matrix4f viewMatrix = this.transformation.getViewMatrix(camera);
		
		// remove translation (different from render3D)
		viewMatrix.setTranslation(0, 0, 0);
		
		// Draw meshes
		this.shader.setUniform("texture_sampler", 0);
		for (Item item : items) {
			Matrix4f modelViewMatrix = this.transformation.getModelViewMatrix(item, viewMatrix);
			this.shader.setUniform("modelViewMatrix", modelViewMatrix);
			this.shader.setUniform("color", item.getMesh().getColor());
			this.shader.setUniform("useTexture", item.getMesh().isTexture());
			item.render(window);
		}

		this.shader.unbind();
	}
}
