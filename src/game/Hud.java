/*
ahbejarano
Hud implementation.
*/

package geetransit.minecraft05.game;

import org.joml.Vector4f;
import geetransit.minecraft05.engine.*;

public class Hud extends SceneRender {
	private static final int FONT_COLS = 16;
	private static final int FONT_ROWS = 16;
	private static final String FONT_FILE = "/res/font.png";
	
	private TextItem text;
	private Item compass;
	private Mouse mouse;
	private Camera camera;
	
	public Hud(Mouse mouse, Camera camera) {
		super();
		this.setRenderer(new Renderer(this) {
			public Shader create(Window window) throws Exception {
				return this.create2D("/res/vertex-2d.vs", "/res/fragment-2d.fs");
			}
			public void render(Window window) {
				this.render2D(window);
			}
		});
		this.mouse = mouse;
		this.camera = camera;
	}
	
	@Override
	public void init(Window window) throws Exception {
		super.init(window);
		this.text = new TextItem("", FONT_FILE, FONT_COLS, FONT_ROWS);
		this.text.getMesh().setColor(new Vector4f(1, 1, 1, 1));
		this.compass = new Item(ObjLoader.loadMesh("/res/compass.obj"));
		this.compass.setScale(40f);
		this.compass.getMesh().setColor(new Vector4f(1, 1, 1, 1));
		this.addItem(this.text).addItem(this.compass);
	}
	
	@Override
	public void render(Window window) {
		this.text.setText("" + this.camera + "\n" + this.mouse);
		this.text.setPosition(10f, window.getHeight() * 0.9f, 0f);
		this.text.setScale(window.getWidth() * (1/3000f));
		this.compass.setPosition(window.getWidth() * 0.95f, window.getWidth() * 0.05f, 0f);
		this.compass.setRotation(0f, 0f, 180f - this.camera.getRotation().y);
		this.compass.setScale(window.getWidth() * (1/20f));
		super.render(window);
	}
}
