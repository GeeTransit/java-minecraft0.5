/*
George Zhang
2020-06-08
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
			this.window.terminate();
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
	
	// Render initialization and loop (in separate thread).
	private void render() {
		this.window.init();
		this.logic.init(this.window);
		this.timer.init();
		
		this.renderLoop();
	}
	
	// Render loop.
	private void renderLoop() {
		float elapsedTime;
		float accumulator = 0f;
		
		while (!this.window.isDestroyed()) {
			elapsedTime = this.timer.getElapsedTime();
			accumulator += elapsedTime;
			
			this.logic.input(this.window);
			
			float interval = 1f / this.window.getTargetUps();
			while (accumulator >= interval) {
				this.logic.update(interval);
				accumulator -= interval;
			}
			
			this.logic.render(this.window);
			if (!this.window.isVSync()) {
				this.sync();
			}
		}
	}
	
	// Sync with target FPS (from window).
	private void sync() {
		float loopSlot = 1f / this.window.getTargetFps();
		double endTime = this.timer.getLastLoopTime() + loopSlot;
		while (this.timer.getTime() < endTime && !this.window.shouldUpdateSize()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public Window getWindow() { return this.window; }
}
