/*
ahbejarano
Skybox class.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import java.util.*;
import org.joml.*;

import static org.lwjgl.glfw.GLFW.*;

public class Skybox implements Loopable {
	private Camera camera;
	private Countdown countdown;

	private Shader shader;
	private Item skybox;

	private boolean toggle;
	private boolean visible;

	public Skybox(Camera camera) {
		this.camera = camera;
		this.countdown = new Countdown(0.5f);
		this.visible = true;
	}

	@Override
	public void init(Window window) throws Exception {
		this.shader = new Shader();
		this.shader.compileVertex(Utils.loadResource("/res/vertex-3d.vs"));
		this.shader.compileFragment(Utils.loadResource("/res/fragment-3d.fs"));
		this.shader.link();

		this.shader.create("projectionMatrix");
		this.shader.create("modelViewMatrix");
		this.shader.create("texture_sampler");
		this.shader.create("color");
		this.shader.create("isTextured");

		Mesh mesh = ObjLoader.loadMesh("/res/skybox.obj");
		mesh.setTexture(new Texture("/res/skybox.png"));
		this.skybox = new Item(mesh);
		this.skybox.setPosition(0, 0, 0);
	}

	@Override
	public void input(Window window) {
		this.toggle = window.isKeyDown(GLFW_KEY_T);
		if (!this.toggle)
			this.countdown.reset();
	}

	@Override
	public void update(float interval) {
		this.skybox.setScale(this.camera.getFar() * 0.5f);

		// toggle skybox
		this.countdown.add(interval);
		if (this.toggle && this.countdown.nextOnce())
			this.visible = !this.visible;
	}

	@Override
	public void render(Window window) {
		if (!this.visible)
			return;

		this.shader.bind();
		this.shader.set("texture_sampler", 0);
		this.shader.set("projectionMatrix", window.getProjectionMatrix());

		// remove view translation
		Matrix4f viewMatrix = this.camera.getViewMatrix();
		Vector3f oldTanslation = viewMatrix.getTranslation(new Vector3f());
		viewMatrix.setTranslation(0, 0, 0);

		// draw skybox
		Matrix4f temp = new Matrix4f();
		this.skybox.getMesh().render(this.shader, this.skybox, ($, $$) -> {
			this.skybox.buildModelViewMatrix(viewMatrix, temp);
			this.shader.set("modelViewMatrix", temp);
		});

		// undo view translation removal
		viewMatrix.setTranslation(oldTanslation);

		this.shader.unbind();
	}

	@Override
	public void cleanup() {
		this.shader.cleanup();
		this.skybox.getMesh().cleanup();
	}
}
