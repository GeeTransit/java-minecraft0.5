/*
ahbejarano
Skybox class.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import java.util.*;
import org.joml.Vector3f;
import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Skybox implements Loopable {
	public static final float RENDER_STEP = 3.0f;  // render changed in 1 second
	public static final float RENDER_MIN = Math.min(Camera.NEAR, 5f);  // render changed in 1 second
	public static final float RENDER_DELAY = 0.5f;  // time between skybox toggling
	public static final float SKYBOX_SCALE = 0.5f;  // skybox scale (multiplied with camera far)

	private Camera camera;
	private Countdown countdown;

	private Shader shader;
	private Item skybox;

	private boolean toggle;
	private boolean visible;
	private int render;

	public Skybox(Camera camera) {
		this.camera = camera;
		this.countdown = new Countdown(RENDER_DELAY);
		this.visible = true;
	}

	@Override
	public void init(Window window) {
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
		// render distance (camera)
		this.render = 0;
		if (window.isKeyDown(GLFW_KEY_L)) this.camera.setFar(Camera.FAR);
		if (window.isKeyDown(GLFW_KEY_RIGHT_BRACKET)) this.render++;
		if (window.isKeyDown(GLFW_KEY_LEFT_BRACKET)) this.render--;

		// toggle skybox
		this.toggle = window.isKeyDown(GLFW_KEY_T);
		if (!this.toggle)
			this.countdown.reset();
	}

	@Override
	public void update(float interval) {
		// render distance
		this.camera.setFar(Math.max(RENDER_MIN, this.camera.getFar() + this.render * interval*RENDER_STEP));

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

		// disable depth testing (very back)
		glDepthMask(false);
		glDisable(GL_DEPTH_TEST);

		// remove view translation
		Matrix4f viewMatrix = this.camera.getViewMatrix();
		Vector3f oldTanslation = viewMatrix.getTranslation(new Vector3f());
		viewMatrix.setTranslation(0, 0, 0);

		// draw skybox
		Matrix4f temp = new Matrix4f();
		this.skybox.getMesh().render(this.shader, this.skybox, (shader, item) -> {
			item.buildModelViewMatrix(viewMatrix, temp);
			shader.set("modelViewMatrix", temp);
		});

		// undo view translation removal
		viewMatrix.setTranslation(oldTanslation);

		glDepthMask(true);
		glEnable(GL_DEPTH_TEST);
		this.shader.unbind();
	}

	@Override
	public void cleanup() {
		this.shader.cleanup();
		this.skybox.getMesh().close();
	}
}
