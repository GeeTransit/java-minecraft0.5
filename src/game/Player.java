/*
George Zhang
Player interaction class.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import java.util.*;
import java.util.function.BiConsumer;
import org.joml.Vector3f;
import org.joml.Matrix4f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Player implements Loopable {
	public static final float CHANGE_DELAY = 0.2f;  // time between block change (place / remove)
	public static final float MOVEMENT_STEP = 3.0f;  // distance moved in 1 second
	public static final float SPRINT_MULTIPLIER = 1.5f;  // sprinting change

	public static final int FONT_COLS = 16;
	public static final int FONT_ROWS = 16;
	public static final String FONT_FILE = "/res/font.png";

	private final Mouse mouse;
	private final Camera camera;
	private final World world;
	private Window window;

	private final Countdown countdown;
	private final Vector3f movement;
	private String change;  // ""=air

	private Shader shader;
	private List<Item> items;

	private TextItem text;
	private Item compass;
	private Item crosshair;

	public Player(Mouse mouse, Camera camera, World world) {
		this.mouse = mouse;
		this.camera = camera;
		this.world = world;

		this.countdown = new Countdown(CHANGE_DELAY);
		this.movement = new Vector3f();
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
	public void input(Window window) {
		// movement
		this.movement.zero();
		boolean SPRINTING = !window.isKeyDown(GLFW_KEY_LEFT_SHIFT) && window.isKeyDown(GLFW_KEY_LEFT_CONTROL);

		if (window.isKeyDown(GLFW_KEY_W)) this.movement.z--;
		if (window.isKeyDown(GLFW_KEY_S)) this.movement.z++;
		if (window.isKeyDown(GLFW_KEY_A)) this.movement.x--;
		if (window.isKeyDown(GLFW_KEY_D)) this.movement.x++;

		if (window.isKeyDown(GLFW_KEY_LEFT_SHIFT)) this.movement.y--;
		if (window.isKeyDown(GLFW_KEY_SPACE)) this.movement.y++;

		if (this.movement.length() > 1f) this.movement.div(this.movement.length());
		if (SPRINTING && this.movement.z < 0) this.movement.mul(SPRINT_MULTIPLIER);

		// placing / removing
		this.change = null;
		if (window.isKeyDown(GLFW_KEY_0)) this.change = "";
		if (window.isKeyDown(GLFW_KEY_1)) this.change = "grassblock";
		if (window.isKeyDown(GLFW_KEY_2)) this.change = "cobbleblock";
		if (window.isKeyDown(GLFW_KEY_3)) this.change = "glassblock";
		if (this.change == null) this.countdown.reset();
	}

	@Override
	public void update(float interval) {
		// movement
		this.camera.movePosition(this.movement, interval*MOVEMENT_STEP);

		// placing / removing
		this.countdown.add(interval);
		if (this.change != null && this.countdown.nextOnce()) {
			ClosestItem<BlockItem> closest = this.world.updateClosest();
			// check if block found
			if (closest.closest != null) {
				Vector3f position = new Vector3f();
				position.set(closest.direction);  // get normalized camera direction
				position.negate();  // move towards camera
				position.mul(0.001f * (this.change.equals("") ? -1 : 1));  // go to block
				position.add(closest.hit);  // start from intersection point
				position.round();  // round to grid
				this.world.setBlock(this.change, position);
			}
		}

		this.text.setPosition(10f, this.window.getHeight() * 0.85f, 0f);
		this.text.setScale(this.window.getWidth() * (1/3500f));
		this.text.setText(String.format(
			"vsync=%s mode=%s mouse=%s\nchange=%s wait=%s\ncamera=%s\nmouse=%s",
			this.window.isVSync(), this.window.getMode(), this.window.getInputMode(GLFW_CURSOR) == GLFW_CURSOR_NORMAL,
			this.change, Math.max(0, this.countdown.getWait()),
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
		Matrix4f temp = new Matrix4f();
		BiConsumer<Shader, Item> setup = (shader, item) -> {
			item.buildOrthoProjModelMatrix(orthoMatrix, temp);
			shader.set("projModelMatrix", temp);
		};

		// draw items
		for (Item item : this.items)
			item.getMesh().render(this.shader, item, setup);

		glDepthMask(true);
		glEnable(GL_DEPTH_TEST);
		this.shader.unbind();
	}

	@Override
	public void cleanup() {
		this.shader.close();
		for (Item item : this.items)
			item.getMesh().close();
	}
}
