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
	
	private TextItem textItem;
	private Mouse mouse;
	private Camera camera;
	
	public Hud(Mouse mouse, Camera camera) {
		super();
		this.setRenderer(new Renderer(this) {
			public Shader createShader(Window window) throws Exception {
				return this.create2DShader("/res/vertex-2d.vs", "/res/fragment-2d.fs");
			}
			public void render(Window window) {
				this.render2DScene(window);
			}
		});
		this.mouse = mouse;
		this.camera = camera;
	}
	
	@Override
	public void init(Window window) throws Exception {
		super.init(window);
		this.textItem = new TextItem("", FONT_FILE, FONT_COLS, FONT_ROWS);
		this.addItem(this.textItem);
	}
	
	@Override
	public void render(Window window) {
		this.textItem.setText("" + this.camera + "\n" + this.mouse);
		this.textItem.getMesh().setColor(new Vector4f(1, 1, 1, 1));
		this.textItem.setPosition(10f, window.getHeight() - 50f, 0);
		this.textItem.setScale(0.3f);
		super.render(window);
	}
}
