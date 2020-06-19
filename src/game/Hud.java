/*
ahbejarano
Hud implementation.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import org.joml.Vector4f;
import static org.lwjgl.glfw.GLFW.*;

public class Hud extends SceneRender {
	private static final int FONT_COLS = 16;
	private static final int FONT_ROWS = 16;
	private static final String FONT_FILE = "/res/font.png";
	
	private TextItem text;
	private Item compass;
	private Item crosshair;
	private Mouse mouse;
	private Camera camera;
	private World world;
	
	public Hud(Mouse mouse, Camera camera, World world) {
		super();
		this.setRenderer(new Renderer() {
			Shader shader;
			public void init(Window window) throws Exception {
				shader = create2D("/res/vertex-2d.vs", "/res/fragment-2d.fs");
			}
			public void render(Window window) {
				render2D(shader, window, Hud.this.getItems());
			}
			public void cleanup() {
				shader.cleanup();
			}
		});
		this.mouse = mouse;
		this.camera = camera;
		this.world = world;
	}
	
	@Override
	public void init(Window window) throws Exception {
		super.init(window);
		
		this.text = new TextItem("", FONT_FILE, FONT_COLS, FONT_ROWS);
		this.text.getMesh().setColor(1, 1, 1);
		
		this.compass = new Item(ObjLoader.loadMesh("/res/compass.obj"));
		this.compass.getMesh().setColor(1, 1, 1);
		
		this.crosshair = new Item(ObjLoader.loadMesh("/res/crosshair.obj"));
		this.crosshair.getMesh().setColor(1, 1, 1);
		
		this
			.addItem(this.text)
			.addItem(this.compass)
			.addItem(this.crosshair);
	}
	
	@Override
	public void render(Window window) {
		this.text.setText(String.format(
			"vsync=%s mode=%s mouse=%s\nchange=%s wait=%s\ncamera=%s\nmouse=%s",
			window.isVSync(), window.getMode(), window.getInputMode(GLFW_CURSOR) == GLFW_CURSOR_NORMAL,
			this.world.getChange(), this.world.getWait(),
			this.camera, this.mouse
		));
		this.text.setPosition(10f, window.getHeight() * 0.85f, 0f);
		this.text.setScale(window.getWidth() * (1/3500f));
		
		this.compass.setPosition(window.getWidth() * 0.95f, window.getWidth() * 0.05f, 0f);
		this.compass.setRotation(0f, 0f, 180f - this.camera.getRotation().y);
		this.compass.setScale(window.getWidth() * (1/20f));
		
		this.crosshair.setPosition(window.getWidth() * 0.5f, window.getHeight() * 0.5f, 0f);
		this.crosshair.setScale(window.getWidth() * (1/50f));
		
		super.render(window);
	}
}
