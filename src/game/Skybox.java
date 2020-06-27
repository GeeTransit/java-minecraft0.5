/*
ahbejarano
Skybox class.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import java.util.*;
import org.joml.Matrix4f;

public class Skybox implements Loopable {
	private Camera camera;
	private Shader shader;
	private Item skybox;

	public Skybox(Camera camera) {
		this.camera = camera;
	}

	@Override
	public void init(Window window) throws Exception {
		this.shader = new Shader();
		this.shader.createVertexShader(Utils.loadResource("/res/vertex-3d.vs"));
		this.shader.createFragmentShader(Utils.loadResource("/res/fragment-3d.fs"));
		this.shader.link();

		this.shader.createUniform("projectionMatrix");
		this.shader.createUniform("modelViewMatrix");
		this.shader.createUniform("texture_sampler");
		this.shader.createUniform("color");
		this.shader.createUniform("isTextured");

		Mesh mesh = ObjLoader.loadMesh("/res/skybox.obj");
		mesh.setTexture(new Texture("/res/skybox.png"));
		this.skybox = new Item(mesh);
		this.skybox.setPosition(0, 0, 0);
	}

	@Override
	public void update(float interval) {
		this.skybox.setScale(this.camera.getFar() * 0.5f);
	}

	@Override
	public void render(Window window) {
		this.shader.bind();
		this.shader.setUniform("texture_sampler", 0);
		this.shader.setUniform("projectionMatrix", window.buildProjectionMatrix(this.camera));

		// remove view translation
		Matrix4f viewMatrix = this.camera.buildViewMatrix();
		viewMatrix.setTranslation(0, 0, 0);

		// draw skybox
		Matrix4f temp = new Matrix4f();
		this.skybox.getMesh().render(this.shader, this.skybox, ($, $$) -> {
			this.skybox.buildModelViewMatrix(viewMatrix, temp);
			this.shader.setUniform("modelViewMatrix", temp);
		});

		this.shader.unbind();
	}

	@Override
	public void cleanup() {
		this.shader.cleanup();
		this.skybox.getMesh().cleanup();
	}
}
