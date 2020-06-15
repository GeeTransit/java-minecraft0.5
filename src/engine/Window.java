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
	public final Bucket next;
	
	private String title;
	private int xPos;
	private int yPos;
	private int width;
	private int height;
	private int mode;
	
	public static final int WINDOWED = 0;
	public static final int BORDERLESS = 1;
	public static final int FULLSCREEN = 2;
	
	private boolean vSync;
	private int targetFps;
	private int targetUps;
	
	private float elapsedTime;
	private float accumulatedTime;
	
	private boolean destroyed = false;
	
	private long monitor;
	private GLFWVidMode vidmode;
	
	public Window(
		String title,
		int width,
		int height,
		int mode,
		boolean vSync,
		int targetFps,
		int targetUps
	) {
		this.title = title;
		this.width = width;
		this.height = height;
		this.mode = mode;
		
		this.vSync = vSync;
		this.targetFps = targetFps;
		this.targetUps = targetUps;
		
		this.timer = new Timer();
		this.lock = new Object();
		this.next = new Bucket();
	}
	
	public void createWindow() {
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
		glfwWindowHint(GLFW_FOCUSED, GL_TRUE); // get focus when shown

		// Get the resolution of the primary monitor
		this.monitor = glfwGetPrimaryMonitor();
		this.vidmode = glfwGetVideoMode(monitor);
		
		// Create the window
		this.handle = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
		if (this.handle == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

        // Setup resize callback
        glfwSetFramebufferSizeCallback(this.handle, (window, width, height) -> {
			if (width > 0 && height > 0)
				this.next.add("size", () -> {
					if (this.isWindowed())
						this.setSize(width, height);
				});
        });

		// Center our window
		this.xPos = this.getCenteredXPos();
		this.yPos = this.getCenteredYPos();
		this.updateWindowMonitor();
	}
	
	public void eventThread() {
		this.eventLoop();
		this.eventDestroy();
		this.eventTerminate();
	}
	
	private void eventLoop() {
		// Make the window visible
		glfwShowWindow(this.handle);
		
		while (!glfwWindowShouldClose(this.handle)) {
			// Only force focus when in borderless fullscreen
			if (this.isBorderless())
				glfwFocusWindow(this.handle);
			// This will block until an event occurs.
			// (Helps reduce CPU usage.)
			glfwWaitEvents();
		}
	}
	
	private void eventDestroy() {
		synchronized (this.lock) {
			this.destroyed = true;
			// Release window (safely)
			glfwDestroyWindow(this.handle);
		}
		// Release window callbacks
		glfwFreeCallbacks(this.handle);
	}
	
	public void eventTerminate() {
		// Terminate GLFW and release the error function
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	public void renderThread(Scene scene) {
		try {
			this.renderInit(scene);
			this.renderLoop(scene);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			scene.cleanup();
		}
	}
	
	private void renderInit(Scene scene) throws Exception {
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
		
		// init
		scene.init(this);
		
		// Start timer.
		this.timer.init();
	}
	
	// Render loop.
	private void renderLoop(Scene scene) {
		while (!this.isDestroyed()) {
			this.elapsedTime = this.timer.getElapsedTime();
			this.accumulatedTime += this.elapsedTime;
			
			// input
			scene.input(this);
			
			// update
			float interval = 1f / this.getTargetUps();
			while (this.accumulatedTime >= interval) {
				scene.update(interval);
				this.accumulatedTime -= interval;
			}
			
			// bucket
			this.next.run("mode");
			this.next.run("size");
			this.next.run("vSync");
			this.next.run("targetFps");
			this.next.run("targetUps");
			
			// render
			scene.render(this);
			this.renderUpdate();
			if (!this.isVSync())
				this.renderSync();
		}
	}
	
	// Sync with target FPS.
	private void renderSync() {
		float loopSlot = 1f / this.getTargetFps();
		double endTime = this.timer.getLastLoopTime() + loopSlot;
		while (timer.getTime() < endTime && this.next.empty()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}
	
	private void renderUpdate() {
		// This can fail if not sync'd. (Can only swap when window exists)
		synchronized (this.lock) {
			if (!this.destroyed)
				glfwSwapBuffers(this.handle); // swap the color buffers
		}
	}
	
	protected void updateWindowPos() {
		int[] xPos = new int[1];
		int[] yPos = new int[1];
		glfwGetWindowPos(this.getHandle(), xPos, yPos);
		this.xPos = xPos[0];
		this.yPos = yPos[0];
	}
	
	protected void updateWindowMonitor() {
		this.setAttrib(GLFW_DECORATED, this.isBorderless() ? 0 : 1);
		glfwSetWindowMonitor(
			this.getHandle(), this.getCurrentMonitor(), this.getXPos(), this.getYPos(),
			this.getWidth(), this.getHeight(), GLFW_DONT_CARE
		);
	}
	
	protected void updateSwapInterval(boolean vSync) { glfwSwapInterval(vSync ? 1 : 0); }
	protected void updateSwapInterval() { this.updateSwapInterval(this.isVSync()); }
	protected long getCurrentMonitor() { return this.isFullscreen() ? this.monitor : NULL; }
	
	public void setShouldClose(boolean shouldClose) { glfwSetWindowShouldClose(this.getHandle(), true); }
	public void clear(int bits) { glClear(bits); }
	public void clearColor(float r, float g, float b, float a) { glClearColor(r, g, b, a); }
	
	public GLFWKeyCallback setKeyCallback(GLFWKeyCallbackI keyCallback) { return glfwSetKeyCallback(this.getHandle(), keyCallback); }
	public void setAttrib(int attrib, int value)  { glfwSetWindowAttrib(this.getHandle(), attrib, value); }
	public long getAttrib(int attrib)             { return glfwGetWindowAttrib(this.getHandle(), attrib); }
	public void setInputMode(int mode, int value) { glfwSetInputMode(this.getHandle(), mode, value); }
	public int getKey(int key)                    { return glfwGetKey(this.getHandle(), key); }
	public boolean isKeyDown(int key)             { return (this.getKey(key) == GLFW_PRESS); }
	
	public long getHandle()      { return this.handle; }
	public Object getLock()      { return this.lock; }
	public boolean isDestroyed() { return this.destroyed; }
	public float getElapsedTime() { return this.elapsedTime; }
	public float getAccumulatedTime() { return this.accumulatedTime; }
	
	public int getTargetFps() { return this.targetFps; }
	public int getTargetUps() { return this.targetUps; }
	public void setTargetFps(int targetFps) { this.targetFps = targetFps; }
	public void setTargetUps(int targetFps) { this.targetUps = targetUps; }
	
	// width, height
	public int getWidth()        { return !this.isWindowed() ? this.getScreenWidth() : this.getWindowWidth(); }
	public int getHeight()       { return !this.isWindowed() ? this.getScreenHeight() : this.getWindowHeight(); }
	public int getWindowWidth()  { return this.width; }
	public int getWindowHeight() { return this.height; }
	public int getScreenWidth()  { return this.vidmode.width(); }
	public int getScreenHeight() { return this.vidmode.height(); }
	// xpos, ypos
	public int getXPos()         { return !this.isWindowed() ? this.getScreenXPos() : this.getWindowXPos(); }
	public int getYPos()         { return !this.isWindowed() ? this.getScreenYPos() : this.getWindowYPos(); }
	public int getWindowXPos()   { return this.xPos; }
	public int getWindowYPos()   { return this.yPos; }
	public int getScreenXPos()   { return 0; }
	public int getScreenYPos()   { return 0; }
	public int getCenteredXPos() { return !this.isWindowed() ? 0 : (this.getScreenWidth() - this.getWindowWidth()) / 2; }
	public int getCenteredYPos() { return !this.isWindowed() ? 0 : (this.getScreenHeight() - this.getWindowHeight()) / 2; }
	// setter
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
		this.updateWindowPos();
		glViewport(0, 0, this.getWidth(), this.getHeight());
		this.updateWindowMonitor();
	}
	
	public int getMode()          { return this.mode; }
	public boolean isWindowed()   { return this.mode == WINDOWED; }
	public boolean isBorderless() { return this.mode == BORDERLESS; }
	public boolean isFullscreen() { return this.mode == FULLSCREEN; }
	public void setMode(int mode) {
		if (this.isWindowed())
			this.updateWindowPos();
		this.mode = mode;
		if (this.isFullscreen())
			// workaround to ensure vSync is correct after real fullscreen
			this.next.add("vSync", () -> this.setVSync(this.isVSync()));
		glViewport(0, 0, this.getWidth(), this.getHeight());
		this.updateWindowMonitor();
	}
	
	public boolean isVSync() { return this.vSync; }
	public void setVSync(boolean vSync) {
		this.vSync = vSync;
		this.updateSwapInterval();
	}
}
