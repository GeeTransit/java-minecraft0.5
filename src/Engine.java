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
	private long window;
	private Object lock;
	
	private boolean vSync;
	private String title;
	private int width;
	private int height;
	private Timer timer;
	
	private ILogic logic;
	
	private boolean updateVSync = false;
	private boolean updateSize = false;
	private boolean destroyed = false;
	
	private int targetFps;
	private int targetUps;
	
	public Engine(
		String title,
		int width,
		int height, 
		boolean vSync,
		int targetFps,
		int targetUps,
		ILogic logic
	) {
		this.title = title;
		this.width = width;
		this.height = height;
		this.vSync = vSync;
		this.targetFps = targetFps;
		this.targetUps = targetUps;
		this.logic = logic;
		this.timer = new Timer();
		this.lock = new Object();
	}
	
	@Override
	public void run() {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");

		try {
			this.init();
			new Thread(this::render).start();
			this.eventLoop();
			
			synchronized (this.lock) {
				this.destroyed = true;
				// Release window (safely)
				glfwDestroyWindow(this.window);
			}
			
			// Release window callbacks
			glfwFreeCallbacks(this.window);
			
		} finally {
			// Terminate GLFW and release the GLFWerrorfun
			glfwTerminate();
			glfwSetErrorCallback(null).free();
		}
	}
	
	protected void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		// Configure our window
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable

		// Create the window
		this.window = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
		if (this.window == NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}

        // Setup resize callback
        glfwSetFramebufferSizeCallback(this.window, (window, width, height) -> {
			if (width > 0 && height > 0) {
				this.width = width;
				this.height = height;
				this.setUpdateSize(true);
			}
        });

		// Get the resolution of the primary monitor
		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		// Center our window
		glfwSetWindowPos(
			this.window,
			(vidmode.width() - this.width) / 2,
			(vidmode.height() - this.height) / 2
		);

		// Make the window visible
		glfwShowWindow(this.window);
		
	}
	
	private void eventLoop() {
		while (!glfwWindowShouldClose(this.window)) {
			// This will block until an event occurs.
			// (Helps reduce CPU usage.)
			glfwWaitEvents();
		}
	}
	
	private void render() {
		// This adds the OpenGL context into this function.
		glfwMakeContextCurrent(this.window);
		
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the ContextCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();
		
		this.logic.init(this);
		this.timer.init();
		
		this.renderLoop();
	}

	private void renderLoop() {
		// Actual render loop:
		float elapsedTime;
		float accumulator = 0f;
		float interval = 1f / this.targetUps;
		
		while (!this.destroyed) {
			elapsedTime = this.timer.getElapsedTime();
			accumulator += elapsedTime;
			
			this.logic.input(this);
			
			// updates > render
			while (accumulator >= interval) {
				this.logic.update(interval);
				accumulator -= interval;
			}
			
			this.logic.render(this);
			
			if (!this.vSync) {
				this.sync();
			}
		}
	}
	
	private void sync() {
		float loopSlot = 1f / this.targetFps;
		double endTime = this.timer.getLastLoopTime() + loopSlot;
		while (this.timer.getTime() < endTime && !this.shouldUpdateSize()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public void checkUpdateSize() {
		if (this.shouldUpdateSize()) {
			glViewport(0, 0, this.getWidth(), this.getHeight());
			this.setUpdateSize(false);
		}
	}
	
	public void checkUpdateVSync() {
		if (this.shouldUpdateVSync()) {
			glfwSwapInterval(this.isVSync() ? 1 : 0);
			this.setUpdateVSync(false);
		}
	}
	
	public long getWindow() { return this.window; }
	public boolean isDestroyed() { return this.destroyed; }
	public Object getLock() { return this.lock; }
	
	public int getTargetFps() { return this.targetFps; }
	public void setTargetFps(int targetFps) { this.targetFps = targetFps; }
	
	public int getTargetUps() { return this.targetUps; }
	public void setTargetUps(int targetFps) { this.targetUps = targetUps; }
	
	public int getWidth() { return this.width; }
	public int getHeight() { return this.height; }
	public boolean shouldUpdateSize() { return this.updateSize; }
	public void setUpdateSize(boolean updateSize) { this.updateSize = updateSize; }
	
	public boolean isVSync() { return this.vSync; }
	public void setVSync(boolean vSync) { this.vSync = vSync; }
	public boolean shouldUpdateVSync() { return this.updateVSync; }
	public void setUpdateVSync(boolean updateVSync) { this.updateVSync = updateVSync; }
}
