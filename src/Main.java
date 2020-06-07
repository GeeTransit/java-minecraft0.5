/*
George Zhang
2020-06-07
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

	private boolean vSync;
	private String title;
	private int width;
	private int height;
	private boolean resized;
	private Timer timer;
	
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
	}

	public void run() {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");

		try {
			this.init();
			this.loop();

			// Release window and window callbacks
			glfwFreeCallbacks(this.window);
			glfwDestroyWindow(this.window);
		
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
            this.width = width;
            this.height = height;
            this.resized = true;
        });

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(this.window, (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
				glfwSetWindowShouldClose(this.window, true); // We will detect this in the rendering loop
			}
			if (key == GLFW_KEY_V && action == GLFW_RELEASE) {
				this.vSync = !this.vSync;
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

		// Make the OpenGL context current
		glfwMakeContextCurrent(this.window);
		
		// Setup vSync
		this.updateVSync();

		// Make the window visible
		glfwShowWindow(this.window);
		
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the ContextCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		// Set the clear color
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
		
		this.timer.init();
	}

	private void loop() {
		float elapsedTime;
		float accumulator = 0f;
		float interval = 1f / TARGET_UPS;
		
		boolean running = true;
		while (running && !glfwWindowShouldClose(window)) {
			elapsedTime = this.timer.getElapsedTime();
			accumulator += elapsedTime;
			
			this.input();
			this.updateVSync();
			
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
		// Poll for window events. The key callback above will only be
		// invoked during this call.
		glfwPollEvents();
		
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
		if (this.resized) {
			glViewport(0, 0, this.width, this.height);
			this.resized = false;
		}
		
		glClearColor(this.color, this.color, this.color, 0.0f);
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

		glfwSwapBuffers(this.window); // swap the color buffers
	}
	
	private void updateVSync() {
		glfwSwapInterval(this.vSync ? 1 : 0);
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
