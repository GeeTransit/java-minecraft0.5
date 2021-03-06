/*
ahbejarano
Renderer class.
*/

package geetransit.minecraft05.engine;

import java.util.*;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL30.*;

public abstract class Renderer {
	protected Transformation transformation;
	
	public Renderer() {
		this.transformation = new Transformation();
	}
	
	// create shaders
	public abstract void init(Window window) throws Exception;
	
	// render scene
	public abstract void render(Window window);
	
	// destroy shaders
	public abstract void cleanup();
	
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
		shader.createUniform("isTextured");
		shader.createUniform("isSelected");
		return shader;
	}
	
	public Shader create2D(String vertex, String fragment) throws Exception {
		Shader shader = this.createShader(vertex, fragment);
		shader.createUniform("projModelMatrix");
		shader.createUniform("texture_sampler");
		shader.createUniform("color");
		shader.createUniform("isTextured");
		return shader;
	}
	
	public void render3D(Shader shader, Window window, Camera camera, List<Item> items) {
		shader.bind();
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		
		// projection
		Matrix4f projectionMatrix = this.transformation.getProjectionMatrix(window, camera);
		shader.setUniform("projectionMatrix", projectionMatrix);
		
		// view
		Matrix4f viewMatrix = camera.getViewMatrix();
		
		// Draw meshes
		shader.setUniform("texture_sampler", 0);
		int itemsSize = items.size();
		for (int i = 0; i < itemsSize; i++) {
			Item item = items.get(i);
			Mesh mesh = item.getMesh();
			Matrix4f modelViewMatrix = this.transformation.getModelViewMatrix(item, viewMatrix);
			shader.setUniform("modelViewMatrix", modelViewMatrix);
			shader.setUniform("color", mesh.getColor());
			shader.setUniform("isTextured", mesh.isTextured());
			shader.setUniform("isSelected", item.isSelected());
			mesh.prepare(this.getMeshFromItems(items, i-1, itemsSize));
			mesh.render();
			mesh.restore(this.getMeshFromItems(items, i+1, itemsSize));
		}
		
		glDisable(GL_CULL_FACE);

		shader.unbind();
	}
	
	// note does NOT call Item.render(Window)
	public void render3DMap(Shader shader, Window window, Camera camera, Map<Mesh, List<Item>> map) {
		shader.bind();
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		
		// projection
		Matrix4f projectionMatrix = this.transformation.getProjectionMatrix(window, camera);
		shader.setUniform("projectionMatrix", projectionMatrix);
		
		// view
		Matrix4f viewMatrix = camera.getViewMatrix();
		
		// Draw meshes
		shader.setUniform("texture_sampler", 0);
		for (Map.Entry<Mesh, List<Item>> entry : map.entrySet()) {
			Mesh mesh = entry.getKey();
			List<Item> items = entry.getValue();
			shader.setUniform("color", mesh.getColor());
			shader.setUniform("isTextured", mesh.isTextured());
			mesh.prepare();
			
			// single : loop through items
			for (Item item : items) {
				Matrix4f modelViewMatrix = this.transformation.getModelViewMatrix(item, viewMatrix);
				shader.setUniform("modelViewMatrix", modelViewMatrix);
				shader.setUniform("isSelected", item.isSelected());
				mesh.render();
			}
			
			mesh.restore();
		}
		
		glDisable(GL_CULL_FACE);
		shader.unbind();
	}
	
	public void render2D(Shader shader, Window window, List<Item> items) {
		shader.bind();
		
		// source # https://stackoverflow.com/a/5467636
		glDepthMask(false);  // disable writes to Z-Buffer
		glDisable(GL_DEPTH_TEST);  // disable depth-testing

		Matrix4f orthoMatrix = this.transformation.getOrthoProjectionMatrix(window);
		
		// Draw meshes
		shader.setUniform("texture_sampler", 0);
		for (Item item : items) {
			Matrix4f projModelMatrix = this.transformation.getOrthoProjModelMatrix(item, orthoMatrix);
			shader.setUniform("projModelMatrix", projModelMatrix);
			shader.setUniform("color", item.getMesh().getColor());
			shader.setUniform("isTextured", item.getMesh().isTextured());
			item.render(window);
		}
		
		glDepthMask(true);
		glEnable(GL_DEPTH_TEST);

		shader.unbind();
	}
	
	public void renderSkybox(Shader shader, Window window, Camera camera, List<Item> items) {
		shader.bind();
		
		// projection
		Matrix4f projectionMatrix = this.transformation.getProjectionMatrix(window, camera);
		shader.setUniform("projectionMatrix", projectionMatrix);
		
		// view
		Matrix4f viewMatrix = camera.getViewMatrix();
		
		// remove translation (different from render3D)
		viewMatrix.setTranslation(0, 0, 0);
		
		// Draw meshes
		shader.setUniform("texture_sampler", 0);
		for (Item item : items) {
			Matrix4f modelViewMatrix = this.transformation.getModelViewMatrix(item, viewMatrix);
			shader.setUniform("modelViewMatrix", modelViewMatrix);
			shader.setUniform("color", item.getMesh().getColor());
			shader.setUniform("isTextured", item.getMesh().isTextured());
			item.render(window);
		}

		shader.unbind();
	}
	
	public Mesh getMeshFromItems(List<Item> items, int index) { return this.getMeshFromItems(items, index, items.size()); }
	public Mesh getMeshFromItems(List<Item> items, int index, int size) {
		if (0 <= index && index < size)
			return items.get(index).getMesh();
		return null;
	}
}
