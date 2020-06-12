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
	private ILogic logic;
	
	private boolean updateVSync = false;
	private boolean updateSize = false;
	private boolean destroyed = false;
	
	public Engine(
		Window window,
		ILogic logic
	) {
		this.window = window;
		this.logic = logic;
	}
	
	@Override
	public void run() {
		this.create();
		new Thread(this::render).start();
		this.event();
	}
	
	// Create window.
	protected void create() {
		this.window.create();
	}
	
	// Event loop and destruction of window.
	protected void event() {
		this.window.event();
		this.window.destroy();
	}
	
	// Render initialization and loop (in separate thread).
	protected void render() {
		try {
			this.window.init();
			this.logic.init(this.window);
			this.window.render(this.logic);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			this.logic.cleanup();
		}
	}
	
	public Window getWindow() { return this.window; }
	public ILogic getLogic() { return this.logic; }
}
