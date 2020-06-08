/*
George Zhang
2020-06-08
Main entry class.
*/

package geetransit.minecraft05;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.Version;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

	// The window handle
	private long window;
	private Object lock;

	private String title;
	private int width;
	private int height;
	private boolean vSync;
	private Timer timer;
	
	private boolean updateVSync = false;
	private boolean updateSize = false;
	private boolean destroyed = false;
	
	private int direction;
	private float color;

    public static final int TARGET_FPS = 5;
    public static final int TARGET_UPS = 30;
	
	public Main(String title, int width, int height, boolean vSync) {
		this.title = title;
		this.width = width;
		this.height = height;
		this.vSync = vSync;
		this.timer = new Timer();
		this.lock = new Object();
	}

	public void run() {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");

		try {
			this.init();
			new Thread(this::renderLoop).start();
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

	private void init() {
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

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(this.window, (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
				glfwSetWindowShouldClose(this.window, true); // We will detect this in the rendering loop
			}
			if (key == GLFW_KEY_V && action == GLFW_RELEASE) {
				this.vSync = !this.vSync;
				this.updateVSync = true;
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
		while (!glfwWindowShouldClose(window)) {
			// This will block until an event occurs.
			// (Helps reduce CPU usage.)
			glfwWaitEvents();
		}
	}

	private void renderLoop() {
		// This adds the OpenGL context into this function.
        glfwMakeContextCurrent(this.window);
		
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the ContextCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		// Set the clear color
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
		
		// Initialize timer's start.
		this.timer.init();
		
		// Actual render loop:
		float elapsedTime;
		float accumulator = 0f;
		float interval = 1f / TARGET_UPS;
		
		while (!this.destroyed) {
			elapsedTime = this.timer.getElapsedTime();
			accumulator += elapsedTime;
			
			this.input();
			
			// updates > render
			while (accumulator >= interval) {
				this.update(interval);
				accumulator -= interval;
			}
			
			this.render();
			
			if (!this.vSync) {
				this.sync();
			}
		}
	}
	
	private void sync () {
		float loopSlot = 1f / TARGET_FPS;
		double endTime = this.timer.getLastLoopTime() + loopSlot;
		while (this.timer.getTime() < endTime) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}
	
	private void input() {
		this.direction = 0;
		if (glfwGetKey(this.window, GLFW_KEY_UP) == GLFW_PRESS) {
			this.direction++;
		}
		if (glfwGetKey(this.window, GLFW_KEY_DOWN) == GLFW_PRESS) {
			this.direction--;
		}
	}
	
	private void update(float interval) {
		this.color += direction * 0.01f;
		if (color > 1) {
			this.color = 1.0f;
		} else if (color < 0) {
			this.color = 0.0f;
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
		
		glClearColor(this.color, this.color, this.color, 0.0f);
		
		// This can fail if not sync'd. (Can only swap when window exists)
		synchronized (this.lock) {
			if (!this.destroyed)
				glfwSwapBuffers(this.window); // swap the color buffers
		}
	}

	public static void main(String[] args) {
		try {
			boolean vSync = true;
			Main main = new Main("Hello World!", 300, 300, vSync);
			main.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
