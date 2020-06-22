/*
ahbejarano
Renderer abstract helper class.
*/

package geetransit.minecraft05.engine;

import java.util.*;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL30.*;

public abstract class Renderer implements Initializable, Renderable {
	public final Map<Mesh, List<Item>> map;
	public final List<Item> items;
	public final Transformation transformation;

	public Renderer() {
		this.map = new HashMap<>();
		this.items = new ArrayList<>();
		this.transformation = new Transformation();
	}

	// create shaders: shader = create?(VERTEX_SHADER, FRAGMENT_SHADER);
	public abstract void init(Window window) throws Exception;

	// render scene: render?(shader, window, ?);
	public abstract void render(Window window);

	// destroy shaders: shader.cleanup();
	public abstract void cleanup();

	public Renderer addItem(Item item) {
		Mesh mesh = item.getMesh();
		this.items.add(item);
		if (!this.map.containsKey(mesh))
			this.map.put(mesh, new ArrayList<>());
		this.map.get(mesh).add(item);
		return this;
	}

	public Renderer removeItem(Item item) {
		Mesh mesh = item.getMesh();
		this.items.remove(item);
		this.map.get(mesh).remove(item);
		if (this.map.get(mesh).size() == 0)
			this.map.remove(mesh);
		return this;
	}

	// shader creators
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

	// note does NOT call Item.render(Window)
	public void render3D(Shader shader, Window window, Camera camera) {
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
		for (Map.Entry<Mesh, List<Item>> entry : this.map.entrySet()) {
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

	// uses the List<Item> of items
	public void render2DList(Shader shader, Window window) {
		shader.bind();

		// source # https://stackoverflow.com/a/5467636
		glDepthMask(false);  // disable writes to Z-Buffer
		glDisable(GL_DEPTH_TEST);  // disable depth-testing

		Matrix4f orthoMatrix = this.transformation.getOrthoProjectionMatrix(window);

		// Draw meshes
		shader.setUniform("texture_sampler", 0);
		for (Item item : this.items) {
			Mesh mesh = item.getMesh();
			shader.setUniform("color", mesh.getColor());
			shader.setUniform("isTextured", mesh.isTextured());
			mesh.prepare();

			Matrix4f projModelMatrix = this.transformation.getOrthoProjModelMatrix(item, orthoMatrix);
			shader.setUniform("projModelMatrix", projModelMatrix);
			mesh.render();

			mesh.restore();
		}

		glDepthMask(true);
		glEnable(GL_DEPTH_TEST);

		shader.unbind();
	}

	// note does NOT call Item.render(Window)
	public void render3DSkybox(Shader shader, Window window, Camera camera) {
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
		for (Map.Entry<Mesh, List<Item>> entry : this.map.entrySet()) {
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

		shader.unbind();
	}

	public void destroy(Shader shader) {
		this.destroyShader(shader);
		for (Mesh mesh : this.map.keySet())
			mesh.cleanup();
	}

	public void destroyShader(Shader shader) {
		shader.cleanup();
	}
}
