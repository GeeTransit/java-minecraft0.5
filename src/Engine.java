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
			this.logic.init(this);
			new Thread(this::renderLoop).start();
			this.eventLoop();
			
			synchronized (this.lock) {
				this.destroyed = true;
				// Release window (safely)
				glfwDestroyWindow(this.window);
			}
			
			// Release window callbacks
			glfwFreeCallbacks(this.window);
			
		} catch (Throwable e) {
			e.printStackTrace();
		
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
				this.updateSize = true;
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

	private void renderLoop() {
		try {
			// This adds the OpenGL context into this function.
			glfwMakeContextCurrent(this.window);
			
			// This line is critical for LWJGL's interoperation with GLFW's
			// OpenGL context, or any context that is managed externally.
			// LWJGL detects the context that is current in the current thread,
			// creates the ContextCapabilities instance and makes the OpenGL
			// bindings available for use.
			GL.createCapabilities();
			
			// Initialize timer's start.
			this.timer.init();
			
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
				
				this.render();
				
				if (!this.vSync) {
					this.sync();
				}
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private void sync () {
		float loopSlot = 1f / this.targetFps;
		double endTime = this.timer.getLastLoopTime() + loopSlot;
		while (this.timer.getTime() < endTime) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}
	
	private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
		
		if (this.updateVSync) {
			glfwSwapInterval(this.vSync ? 1 : 0);
			this.updateVSync = false;
		}
		
		if (this.updateSize) {
			glViewport(0, 0, this.width, this.height);
			this.updateSize = false;
		}
		
		this.logic.render(this);
		
		// This can fail if not sync'd. (Can only swap when window exists)
		synchronized (this.lock) {
			if (!this.destroyed) {
				glfwSwapBuffers(this.window); // swap the color buffers
			}
		}
	}
	
	public long getWindow() { return this.window; }
	
	public boolean getVSync() { return this.vSync; }
	public void setVSync(boolean vSync) { this.vSync = vSync; }
	public void setUpdateVSync() { this.updateVSync = true; }
}
