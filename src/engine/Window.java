/*
George Zhang
Window class.
*/

package geetransit.minecraft05.engine;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {
	private long handle;
	private Timer timer;
	private Object lock;
	
	private String title;
	private int width;
	private int height;
	private boolean fullscreen;
	
	private boolean vSync;
	private int targetFps;
	private int targetUps;
	
	private int nextWidth;
	private int nextHeight;
	private boolean nextFullscreen;
	private boolean nextVSync;
	
	private boolean updateVSync = false;
	private boolean updateSize = false;
	private boolean destroyed = false;
	
	private long monitor;
	private GLFWVidMode vidmode;
	private GLFWFramebufferSizeCallbackI resizeCallback;
	
	public Window(
		String title,
		int width,
		int height,
		boolean fullscreen,
		boolean vSync,
		int targetFps,
		int targetUps
	) {
		this.title = title;
		this.width = width;
		this.height = height;
		this.fullscreen = fullscreen;
		
		this.vSync = vSync;
		this.targetFps = targetFps;
		this.targetUps = targetUps;
		
		this.timer = new Timer();
		this.lock = new Object();
	}
	
	public void create() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure our window
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable

		// Get the resolution of the primary monitor
		this.monitor = glfwGetPrimaryMonitor();
		this.vidmode = glfwGetVideoMode(monitor);
		
		// Create the window
		this.handle = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
		if (this.handle == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

        // Setup resize callback
        glfwSetFramebufferSizeCallback(this.handle, this.resizeCallback = (window, width, height) -> {
			if (width > 0 && height > 0)
				this.setNextSize(width, height);
        });

		// Center our window
		this.updateWindowMonitor();
		
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
		
		// Check vSync
		glfwSwapInterval(this.isVSync() ? 1 : 0);
		
		// Start timer.
		this.timer.init();
	}
	
	// Render loop.
	public void render(ILogic logic) {
		float elapsedTime;
		float accumulator = 0f;
		
		while (!this.isDestroyed()) {
			elapsedTime = this.timer.getElapsedTime();
			accumulator += elapsedTime;
			
			logic.input(this);
			
			float interval = 1f / this.getTargetUps();
			while (accumulator >= interval) {
				logic.update(interval);
				accumulator -= interval;
			}
			
			logic.render(this);
			if (!this.isVSync()) {
				this.sync();
			}
		}
	}
	
	// Sync with target FPS.
	private void sync() {
		float loopSlot = 1f / this.getTargetFps();
		double endTime = this.timer.getLastLoopTime() + loopSlot;
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
			if (!this.destroyed)
				glfwSwapBuffers(this.handle); // swap the color buffers
		}
	}
	
	public boolean checkUpdateSize() {
		if (!this.shouldUpdateSize())
			return false;
		
		this.fullscreen = this.nextFullscreen;
		this.width = this.nextWidth;
		this.height = this.nextHeight;
		glViewport(0, 0, this.getWidth(), this.getHeight());
		
		this.updateWindowMonitor();
		this.updateSize = false;
		return true;
	}
	
	public boolean checkUpdateVSync() {
		if (!this.shouldUpdateVSync())
			return false;
		
		this.vSync = this.nextVSync;
		
		this.updateSwapInterval();
		this.updateVSync = false;
		return true;
	}
	
	protected void updateWindowMonitor() {
		glfwSetWindowMonitor(
			this.getHandle(), this.getCurrentMonitor(),
			this.getCenteredXPos(), this.getCenteredYPos(),
			this.getWidth(), this.getHeight(),
			GLFW_DONT_CARE
		);
	}
	
	protected void updateSwapInterval() {
		glfwSwapInterval(this.isVSync() ? 1 : 0);
	}
	
	
	public void setShouldClose(boolean shouldClose) {
		glfwSetWindowShouldClose(this.getHandle(), true);
	}
	public void clear(int bits) {
		glClear(bits);
	}
	public void clearColor(float r, float g, float b, float a) {
		glClearColor(r, g, b, a);
	}
	
	public GLFWKeyCallback setKeyCallback(GLFWKeyCallbackI keyCallback) {
		return glfwSetKeyCallback(this.getHandle(), keyCallback);
	}
	public int getKey(int key) {
		return glfwGetKey(this.getHandle(), key);
	}
	public boolean isKeyDown(int key) {
		return (this.getKey(key) == GLFW_PRESS);
	}
	
	public long getHandle() { return this.handle; }
	public Object getLock() { return this.lock; }
	public boolean isDestroyed() { return this.destroyed; }
	protected long getCurrentMonitor() { return this.isFullscreen() ? this.monitor : NULL; }
	
	public int getTargetFps() { return this.targetFps; }
	public void setTargetFps(int targetFps) { this.targetFps = targetFps; }
	
	public int getTargetUps() { return this.targetUps; }
	public void setTargetUps(int targetFps) { this.targetUps = targetUps; }
	
	public int getWidth() { return this.isFullscreen() ? this.getScreenWidth() : this.getWindowWidth(); }
	public int getHeight() { return this.isFullscreen() ? this.getScreenHeight() : this.getWindowHeight(); }
	public int getWindowWidth() { return this.width; }
	public int getWindowHeight() { return this.height; }
	public int getScreenWidth() { return this.vidmode.width(); }
	public int getScreenHeight() { return this.vidmode.height(); }
	public int getCenteredXPos() { return this.isFullscreen() ? 0 : (this.getScreenWidth() - this.getWindowWidth()) / 2; }
	public int getCenteredYPos() { return this.isFullscreen() ? 0 : (this.getScreenHeight() - this.getWindowHeight()) / 2; }
	public boolean isFullscreen() { return this.fullscreen; }
	public boolean shouldUpdateSize() { return this.updateSize; }
	public void setNextSize(int nextWidth, int nextHeight) { 
		this.nextWidth = nextWidth;
		this.nextHeight = nextHeight;
		this.nextFullscreen = this.fullscreen;
		this.updateSize = true;
	}
	public void setNextSize(boolean fullscreen) { 
		this.nextWidth = this.width;
		this.nextHeight = this.height;
		this.nextFullscreen = fullscreen;
		this.updateSize = true;
	}
	
	public boolean isVSync() { return this.vSync; }
	public boolean shouldUpdateVSync() { return this.updateVSync; }
	public void setNextVSync(boolean nextVSync) { 
		this.nextVSync = nextVSync;
		this.updateVSync = true;
	}
}
