/*
ahbejarano
Hud implementation.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import java.util.*;
import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Hud implements Loopable {
	private static final int FONT_COLS = 16;
	private static final int FONT_ROWS = 16;
	private static final String FONT_FILE = "/res/font.png";

	private Mouse mouse;
	private Camera camera;
	private World world;
	private Window window;

	private Shader shader;
	private List<Item> items;

	private TextItem text;
	private Item compass;
	private Item crosshair;

	public Hud(Mouse mouse, Camera camera, World world) {
		this.mouse = mouse;
		this.camera = camera;
		this.world = world;

		this.items = new ArrayList<>();
	}

	@Override
	public void init(Window window) {
		this.shader = new Shader();
		this.shader.compileVertex(Utils.loadResource("/res/vertex-2d.vs"));
		this.shader.compileFragment(Utils.loadResource("/res/fragment-2d.fs"));
		this.shader.link();

		this.shader.create("projModelMatrix");
		this.shader.create("texture_sampler");
		this.shader.create("color");
		this.shader.create("isTextured");

		this.text = new TextItem("", new FontTexture(FONT_FILE, FONT_COLS, FONT_ROWS));
		this.text.getMesh().setColor(1, 1, 1);

		this.compass = new Item(ObjLoader.loadMesh("/res/compass.obj"));
		this.compass.getMesh().setColor(1, 1, 1);

		this.crosshair = new Item(ObjLoader.loadMesh("/res/crosshair.obj"));
		this.crosshair.getMesh().setColor(1, 1, 1);

		this.items.add(this.text);
		this.items.add(this.compass);
		this.items.add(this.crosshair);

		this.window = window;
	}

	@Override
	public void update(float interval) {
		this.text.setPosition(10f, this.window.getHeight() * 0.85f, 0f);
		this.text.setScale(this.window.getWidth() * (1/3500f));
		this.text.setText(String.format(
			"vsync=%s mode=%s mouse=%s\nchange=%s wait=%s\ncamera=%s\nmouse=%s",
			this.window.isVSync(), this.window.getMode(), this.window.getInputMode(GLFW_CURSOR) == GLFW_CURSOR_NORMAL,
			this.world.getChange(), Math.max(0, this.world.getWait()),
			this.camera, this.mouse
		));

		this.compass.setPosition(this.window.getWidth() * 0.95f, this.window.getWidth() * 0.05f, 0f);
		this.compass.setRotation(0f, 0f, 180f - this.camera.getRotation().y);
		this.compass.setScale(this.window.getWidth() * (1/20f));

		this.crosshair.setPosition(this.window.getWidth() * 0.5f, this.window.getHeight() * 0.5f, 0f);
		this.crosshair.setScale(this.window.getWidth() * (1/50f));
	}

	@Override
	public void render(Window window) {
		this.shader.bind();
		this.shader.set("texture_sampler", 0);

		// disable depth testing : source # https://stackoverflow.com/a/5467636
		glDepthMask(false);  // disable writes to Z-Buffer
		glDisable(GL_DEPTH_TEST);  // disable depth-testing

		Matrix4f orthoMatrix = window.getOrthoProjectionMatrix();

		// draw items
		Matrix4f temp = new Matrix4f();
		for (Item item : this.items)
			// ($, $$) are ignored paramenters
			item.getMesh().render(this.shader, item, ($, $$) -> {
				item.buildOrthoProjModelMatrix(orthoMatrix, temp);
				this.shader.set("projModelMatrix", temp);
			});

		glDepthMask(true);
		glEnable(GL_DEPTH_TEST);
		this.shader.unbind();
	}

	@Override
	public void cleanup() {
		this.shader.cleanup();
		for (Item item : this.items)
			item.getMesh().cleanup();
	}
}
