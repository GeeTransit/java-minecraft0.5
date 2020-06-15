/*
George Zhang
Engine class.
*/

package geetransit.minecraft05.engine;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.Version;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Engine implements Runnable {
	private Window window;
	private Scene scene;
	
	private boolean updateVSync = false;
	private boolean updateSize = false;
	private boolean destroyed = false;
	
	public Engine(
		Window window,
		Scene scene
	) {
		this.window = window;
		this.scene = scene;
	}
	
	@Override
	public void run() {
		this.window.createWindow();
		new Thread(() -> this.window.renderThread(this.scene)).start();
		this.window.eventThread();
	}
	
	public Window getWindow() { return this.window; }
	public Scene getScene() { return this.scene; }
}
