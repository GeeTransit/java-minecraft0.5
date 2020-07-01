/*
George Zhang
Game logic implementation.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.Version;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game extends Scene {
	private Mouse mouse;
	private Camera camera;

	private Background background;
	private Skybox skybox;
	private View view;
	private Hud hud;

	public Game() {
		super();

		// inputs
		this.mouse = new Mouse();
		this.camera = new Camera(this.mouse);
		this
			.addFrom(this.mouse)
			.addFrom(this.camera);

		// child scenes
		this.background = new Background();
		this.skybox = new Skybox(this.camera);
		this.view = new View(this.mouse, this.camera);
		this.hud = new Hud(this.mouse, this.camera, this.view);
		this
			.addFrom(this.background)
			.addFrom(this.skybox)
			.addFrom(this.view)
			.addFrom(this.hud);
	}

	@Override
	public void init(Window window) {
		System.out.println("LWJGL version: " + Version.getVersion());
		System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));

		// call child scenes' init
		super.init(window);

		// Use correct depth checking
		glEnable(GL_DEPTH_TEST);

		// makes text better
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		window.setKeyCallback((handle, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_F4 && action == GLFW_RELEASE && ((mods & GLFW_MOD_ALT) != 0)) {
				window.setShouldClose(true);  // We will detect this in the rendering loop
				window.postEmptyEvent();
			}
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
				window.next.add("mode", () -> {
					if (!window.isWindowed())
						window.setMode(Window.WINDOWED);
				});
				window.next.add("inputMode", () -> {
					if (window.getInputMode(GLFW_CURSOR) == GLFW_CURSOR_DISABLED) {
						window.setInputMode(GLFW_CURSOR, GLFW_CURSOR_NORMAL);
						if (glfwRawMouseMotionSupported())
							window.setInputMode(GLFW_RAW_MOUSE_MOTION, GLFW_FALSE);
					}
				});
				window.postEmptyEvent();
			}
			if (key == GLFW_KEY_M && action == GLFW_RELEASE) {
				window.next.add("inputMode", () -> {
					if (window.getInputMode(GLFW_CURSOR) == GLFW_CURSOR_DISABLED) {
						window.setInputMode(GLFW_CURSOR, GLFW_CURSOR_NORMAL);
						if (glfwRawMouseMotionSupported())
							window.setInputMode(GLFW_RAW_MOUSE_MOTION, GLFW_FALSE);
					} else {
						window.setInputMode(GLFW_CURSOR, GLFW_CURSOR_DISABLED);
						if (glfwRawMouseMotionSupported())
							window.setInputMode(GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
					}
				});
				window.postEmptyEvent();
			}
			if (key == GLFW_KEY_V && action == GLFW_RELEASE)
				window.next.add("vSync", () -> window.setVSync(!window.isVSync()));
			if (key == GLFW_KEY_F && action == GLFW_RELEASE)
				if (!window.isWindowed())
					window.next.add("mode", () -> window.setMode(Window.WINDOWED));
				else if ((mods & GLFW_MOD_SHIFT) != 0)
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
				System.out.println("mouse="+this.mouse);
				System.out.println("camera="+this.camera);
			}
		});
	}

	@Override
	public void render(Window window) {
		// clear the framebuffer
		window.clear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		// build matrices
		window.buildProjectionMatrix(this.camera);
		window.buildOrthoProjectionMatrix();
		this.camera.buildViewMatrix();
		this.camera.updateFrustum(window.getProjectionMatrix());

		// render scenes
		super.render(window);
	}
}
