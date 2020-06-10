/*
George Zhang
2020-06-08
Window class.
*/

package geetransit.minecraft05;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {
	private long handle;
	private Object lock;
	
	private String title;
	private int width;
	private int height;
	
	private boolean vSync;
	private int targetFps;
	private int targetUps;
	
	private boolean updateVSync = false;
	private boolean updateSize = false;
	private boolean destroyed = false;
	
	public Window(
		String title,
		int width,
		int height, 
		boolean vSync,
		int targetFps,
		int targetUps
	) {
		this.title = title;
		this.width = width;
		this.height = height;
		this.vSync = vSync;
		this.targetFps = targetFps;
		this.targetUps = targetUps;
		this.lock = new Object();
	}
	
	public void create() {
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
		this.handle = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
		if (this.handle == NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}

        // Setup resize callback
        glfwSetFramebufferSizeCallback(this.handle, (window, width, height) -> {
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
			this.handle,
			(vidmode.width() - this.width) / 2,
			(vidmode.height() - this.height) / 2
		);

		// Make the window visible
		glfwShowWindow(this.handle);
		
	}
	
	public void event() {
		while (!glfwWindowShouldClose(this.handle)) {
			// This will block until an event occurs.
			// (Helps reduce CPU usage.)
			glfwWaitEvents();
		}
	}
	
	public void destroy() {
		synchronized (this.lock) {
			this.destroyed = true;
			// Release window (safely)
			glfwDestroyWindow(this.handle);
		}
		// Release window callbacks
		glfwFreeCallbacks(this.handle);
	}
	
	public void terminate() {
		// Terminate GLFW and release the GLFWerrorfun
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	public void init() {
		// This adds the OpenGL context into this function.
		glfwMakeContextCurrent(this.handle);
		
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the ContextCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();
	}
	
	// Render loop.
	public void render(ILogic logic, Timer timer) {
		float elapsedTime;
		float accumulator = 0f;
		
		while (!this.isDestroyed()) {
			elapsedTime = timer.getElapsedTime();
			accumulator += elapsedTime;
			
			logic.input(this);
			
			float interval = 1f / this.getTargetUps();
			while (accumulator >= interval) {
				logic.update(interval);
				accumulator -= interval;
			}
			
			logic.render(this);
			if (!this.isVSync()) {
				this.sync(timer);
			}
		}
	}
	
	// Sync with target FPS.
	private void sync(Timer timer) {
		float loopSlot = 1f / this.getTargetFps();
		double endTime = timer.getLastLoopTime() + loopSlot;
		while (timer.getTime() < endTime && !this.shouldUpdateSize()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public void update() {
		// This can fail if not sync'd. (Can only swap when window exists)
		synchronized (this.lock) {
			if (!this.destroyed) {
				glfwSwapBuffers(this.handle); // swap the color buffers
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
	
	public long getHandle() { return this.handle; }
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
