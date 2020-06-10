/*
George Zhang
2020-06-10
Engine class.
*/

package geetransit.minecraft05;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.Version;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Engine implements Runnable {
	private Window window;
	private ILogic logic;
	private Timer timer;
	
	private boolean updateVSync = false;
	private boolean updateSize = false;
	private boolean destroyed = false;
	
	public Engine(
		Window window,
		ILogic logic
	) {
		this.window = window;
		this.logic = logic;
		this.timer = new Timer();
	}
	
	@Override
	public void run() {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");

		try {
			this.create();
			new Thread(this::render).start();
			this.event();
			
		} finally {
			this.cleanup();
		}
	}
	
	// Create window.
	private void create() {
		this.window.create();
	}
	
	// Event loop and destruction of window.
	private void event() {
		this.window.event();
		this.window.destroy();
	}
	
	// Cleanup of everything (called after destruction of window).
	private void cleanup() {
		this.logic.cleanup();
	}
	
	// Render initialization and loop (in separate thread).
	private void render() {
		this.window.init();
		this.logic.init(this.window);
		this.timer.init();
		this.window.render(this.logic, this.timer);
	}
	
	public Window getWindow() { return this.window; }
	public ILogic getLogic() { return this.logic; }
}
