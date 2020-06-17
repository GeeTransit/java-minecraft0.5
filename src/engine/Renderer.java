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
	
	private static final float Z_NEAR = 0.01f;
	private static final float Z_FAR = 1000f;
	
	public Renderer() {
		this.transformation = new Transformation();
	}
	
	// create shaders
	public abstract void init(Window window) throws Exception;
	
	// render scene
	public abstract void render(Window window);
	
	// cleanup shaders
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
		shader.createUniform("useInstanced");
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
	
	public void render3D(Shader shader, Window window, Camera camera, SceneRender scene) {
		shader.bind();
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		
		// projection
		Matrix4f projectionMatrix = this.transformation.getProjectionMatrix(window, camera);
		shader.setUniform("projectionMatrix", projectionMatrix);
		
		// view
		Matrix4f viewMatrix = this.transformation.getViewMatrix(camera);
		
		// Draw meshes
		shader.setUniform("texture_sampler", 0);
		shader.setUniform("useInstanced", 0);
		List<Item> items = scene.getItems();
		int itemsSize = items.size();
		for (int i = 0; i < itemsSize; i++) {
			Item item = items.get(i);
			Matrix4f modelViewMatrix = this.transformation.getModelViewMatrix(item, viewMatrix);
			shader.setUniform("modelViewMatrix", modelViewMatrix);
			shader.setUniform("color", item.getMesh().getColor());
			shader.setUniform("useTexture", item.getMesh().useTexture());
			item.render(window);
		}
		
		glDisable(GL_CULL_FACE);
		shader.unbind();
	}
	
	public void render3DList(Shader shader, Window window, Camera camera, SceneRender scene) {
		shader.bind();
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		
		// projection
		Matrix4f projectionMatrix = this.transformation.getProjectionMatrix(window, camera);
		shader.setUniform("projectionMatrix", projectionMatrix);
		
		// view
		Matrix4f viewMatrix = this.transformation.getViewMatrix(camera);
		
		shader.setUniform("texture_sampler", 0);
		shader.setUniform("useInstanced", 0);
		List<Item> items = scene.getItems();
		if (!items.isEmpty()) {
			Mesh firstMesh = items.get(0).getMesh();
			shader.setUniform("color", firstMesh.getColor());
			shader.setUniform("useTexture", firstMesh.useTexture());
			firstMesh.prepare();
			
			for (Item item : items) {
				Matrix4f modelViewMatrix = this.transformation.getModelViewMatrix(item, viewMatrix);
				shader.setUniform("modelViewMatrix", modelViewMatrix);
				item.getMesh().render();
			}
			
			firstMesh.restore();
		}
		
		glDisable(GL_CULL_FACE);
		shader.unbind();
	}
	
	// note does NOT call Item.render(Window)
	public void render3DMap(Shader shader, Window window, Camera camera, SceneRender scene) {
		shader.bind();
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		
		// projection
		Matrix4f projectionMatrix = this.transformation.getProjectionMatrix(window, camera);
		shader.setUniform("projectionMatrix", projectionMatrix);
		
		// view
		Matrix4f viewMatrix = this.transformation.getViewMatrix(camera);
		
		// Draw meshes
		shader.setUniform("texture_sampler", 0);
		for (Map.Entry<Mesh, List<Item>> entry : scene.getMeshMap().entrySet()) {
			Mesh mesh = entry.getKey();
			List<Item> items = entry.getValue();
			shader.setUniform("color", mesh.getColor());
			shader.setUniform("useTexture", mesh.useTexture());
			mesh.prepare();
			
			// check if it's instanced
			if (mesh instanceof InstancedMesh) {
				// instanced : render all of them
				shader.setUniform("useInstanced", 1);
				InstancedMesh instancedMesh = (InstancedMesh) mesh;
				instancedMesh.render3DList(items, this.transformation, viewMatrix);
			} else {
				// single : loop through items
				shader.setUniform("useInstanced", 0);
				for (Item item : items) {
					Matrix4f modelViewMatrix = this.transformation.getModelViewMatrix(item, viewMatrix);
					shader.setUniform("modelViewMatrix", modelViewMatrix);
					mesh.render();
				}
			}
			mesh.restore();
		}
		
		glDisable(GL_CULL_FACE);
		shader.unbind();
	}
	
	public void render2D(Shader shader, Window window, SceneRender scene) {
		shader.bind();
		
		// source # https://stackoverflow.com/a/5467636
		glDepthMask(false);  // disable writes to Z-Buffer
		glDisable(GL_DEPTH_TEST);  // disable depth-testing

		Matrix4f orthoMatrix = this.transformation.getOrthoProjectionMatrix(window);
		
		// Draw meshes
		shader.setUniform("texture_sampler", 0);
		for (Item item : scene.getItems()) {
			Matrix4f projModelMatrix = this.transformation.getOrthoProjModelMatrix(item, orthoMatrix);
			shader.setUniform("projModelMatrix", projModelMatrix);
			shader.setUniform("color", item.getMesh().getColor());
			shader.setUniform("useTexture", item.getMesh().useTexture());
			item.render(window);
		}
		
		glDepthMask(true);
		glEnable(GL_DEPTH_TEST);

		shader.unbind();
	}
	
	public void renderSkybox(Shader shader, Window window, Camera camera, SceneRender scene) {
		shader.bind();
		
		// projection
		Matrix4f projectionMatrix = this.transformation.getProjectionMatrix(window, camera);
		shader.setUniform("projectionMatrix", projectionMatrix);
		
		// view
		Matrix4f viewMatrix = this.transformation.getViewMatrix(camera);
		
		// remove translation (different from render3D)
		viewMatrix.setTranslation(0, 0, 0);
		
		// Draw meshes
		shader.setUniform("texture_sampler", 0);
		shader.setUniform("useInstanced", 0);
		for (Item item : scene.getItems()) {
			Matrix4f modelViewMatrix = this.transformation.getModelViewMatrix(item, viewMatrix);
			shader.setUniform("modelViewMatrix", modelViewMatrix);
			shader.setUniform("color", item.getMesh().getColor());
			shader.setUniform("useTexture", item.getMesh().useTexture());
			item.render(window);
		}

		shader.unbind();
	}
	
	// this WILL return null
	public Mesh getMeshFromItems(List<Item> items, int index) { return this.getMeshFromItems(items, index, items.size()); }
	public Mesh getMeshFromItems(List<Item> items, int index, int size) {
		if (0 <= index && index < size)
			return items.get(index).getMesh();
		return null;
	}
}
