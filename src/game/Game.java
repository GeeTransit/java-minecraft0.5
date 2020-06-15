/*
George Zhang
Game logic implementation.
*/

package geetransit.minecraft05.game;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.Version;

import geetransit.minecraft05.engine.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game extends SceneBase {
	private Mouse mouse;
	private Camera camera;
	
	private Background background;
	private World world;
	private Hud hud;
	
	public Game() {
		super();
		this.mouse = new Mouse();
		this.camera = new Camera();
		
		this.background = new Background();
		this.world = new World(this.mouse, this.camera);
		this.hud = new Hud(this.mouse, this.camera);
		
		// add scenes
		this.addScene(this.background).addScene(this.world).addScene(this.hud);
	}
	
	public Mouse getMouse() { return this.mouse; }
	public Camera getCamera() { return this.camera; }
	
	public Background getBackground() { return this.background; }
	public World getWorld() { return this.world; }
	public Hud getHud() { return this.hud; }
	
	@Override
	public void init(Window window) throws Exception {
		System.out.println("LWJGL version: " + Version.getVersion());
		System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
		
		// setup mouse
		this.mouse.init(window);
		
		// call child scenes' init
		super.init(window);
		
		// Use correct depth checking
		glEnable(GL_DEPTH_TEST);
		
		// makes text better
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		window.setKeyCallback((handle, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				window.setShouldClose(true);  // We will detect this in the rendering loop
			if (key == GLFW_KEY_V && action == GLFW_RELEASE)
				window.next.add("vSync", () -> window.setVSync(!window.isVSync()));
			if (key == GLFW_KEY_F && action == GLFW_RELEASE)
				if (!window.isWindowed())
					window.next.add("mode", () -> window.setMode(Window.WINDOWED));
				else if ((mods & GLFW_MOD_SHIFT) > 0)
					// hold down shift when pressing F to use real fullscreen
					window.next.add("mode", () -> window.setMode(Window.FULLSCREEN));
				else
					window.next.add("mode", () -> window.setMode(Window.BORDERLESS));
			if (key == GLFW_KEY_LEFT && !window.isVSync() && action == GLFW_RELEASE)
				window.next.add("targetFps", () -> window.setTargetFps(Math.max(1, window.getTargetFps() - 1)));
			if (key == GLFW_KEY_RIGHT && !window.isVSync() && action == GLFW_RELEASE)
				window.next.add("targetFps", () -> window.setTargetFps(window.getTargetFps() + 1));
			// debug
			if (key == GLFW_KEY_U && action == GLFW_RELEASE) {
				System.out.println(this.camera);
				System.out.println(this.mouse);
			}
		});
	}
	
	@Override
	public void input(Window window) {
		this.mouse.input(window);
		super.input(window);
	}
	
	@Override
	public void render(Window window) {
		// clear the framebuffer
		window.clear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		super.render(window);
	}
}
