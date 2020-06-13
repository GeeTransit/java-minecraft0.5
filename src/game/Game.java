/*
George Zhang
Game logic implementation.
*/

package geetransit.minecraft05.game;

import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.Version;

import geetransit.minecraft05.engine.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game implements ILogic {
	
	private int direction;
	private float color;
	private boolean rotate;
	private Vector3f movement;
	private Renderer renderer;
	private Item[] items;
	private GLFWKeyCallbackI keyCallback;

	public Game() {
		this.direction = 0;
		this.color = 0.5f;
		this.rotate = true;
		this.movement = new Vector3f();
		this.renderer = new Renderer();
	}
	
	@Override
	public void init(Window window) throws Exception {
		System.out.println("LWJGL version: " + Version.getVersion());
		System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
		
		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		window.setKeyCallback(this.keyCallback = (handle, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				window.setShouldClose(true);  // We will detect this in the rendering loop
			if (key == GLFW_KEY_V && action == GLFW_RELEASE)
				window.setNextVSync(!window.isVSync());
			if (key == GLFW_KEY_R && action == GLFW_RELEASE)
				this.rotate = !rotate;
			if (key == GLFW_KEY_F && action == GLFW_RELEASE)
				window.setNextSize(!window.isFullscreen());
			if (key == GLFW_KEY_LEFT && action == GLFW_RELEASE)
				window.setTargetFps(Math.max(1, window.getTargetFps() - 1));
			if (key == GLFW_KEY_RIGHT && action == GLFW_RELEASE)
				window.setTargetFps(window.getTargetFps() + 1);
		});
		
		window.clearColor(this.color, this.color, this.color, 0.0f);
		
		// Link shaders. (located in res/)
		this.renderer.init();
		
		// Create the cube mesh
		float[] positions = {
			// V0
			-0.5f, 0.5f, 0.5f,
			// V1
			-0.5f, -0.5f, 0.5f,
			// V2
			0.5f, -0.5f, 0.5f,
			// V3
			0.5f, 0.5f, 0.5f,
			// V4
			-0.5f, 0.5f, -0.5f,
			// V5
			0.5f, 0.5f, -0.5f,
			// V6
			-0.5f, -0.5f, -0.5f,
			// V7
			0.5f, -0.5f, -0.5f,
			
			// For text coords in top face
			// V8: V4 repeated
			-0.5f, 0.5f, -0.5f,
			// V9: V5 repeated
			0.5f, 0.5f, -0.5f,
			// V10: V0 repeated
			-0.5f, 0.5f, 0.5f,
			// V11: V3 repeated
			0.5f, 0.5f, 0.5f,

			// For text coords in right face
			// V12: V3 repeated
			0.5f, 0.5f, 0.5f,
			// V13: V2 repeated
			0.5f, -0.5f, 0.5f,

			// For text coords in left face
			// V14: V0 repeated
			-0.5f, 0.5f, 0.5f,
			// V15: V1 repeated
			-0.5f, -0.5f, 0.5f,

			// For text coords in bottom face
			// V16: V6 repeated
			-0.5f, -0.5f, -0.5f,
			// V17: V7 repeated
			0.5f, -0.5f, -0.5f,
			// V18: V1 repeated
			-0.5f, -0.5f, 0.5f,
			// V19: V2 repeated
			0.5f, -0.5f, 0.5f,
		};
		int[] indices = {
			// Front face
			0, 1, 3, 3, 1, 2,
			// Top Face
			8, 10, 11, 9, 8, 11,
			// Right face
			12, 13, 7, 5, 12, 7,
			// Left face
			14, 15, 6, 4, 14, 6,
			// Bottom face
			16, 18, 19, 17, 16, 19,
			// Back face
			4, 6, 7, 5, 4, 7,
		};
		float[] coords = {
			0.0f, 0.0f,
			0.0f, 0.5f,
			0.5f, 0.5f,
			0.5f, 0.0f,
			
			0.0f, 0.0f,
			0.5f, 0.0f,
			0.0f, 0.5f,
			0.5f, 0.5f,
			
			// For text coords in top face
			0.0f, 0.5f,
			0.5f, 0.5f,
			0.0f, 1.0f,
			0.5f, 1.0f,

			// For text coords in right face
			0.0f, 0.0f,
			0.0f, 0.5f,

			// For text coords in left face
			0.5f, 0.0f,
			0.5f, 0.5f,

			// For text coords in bottom face
			0.5f, 0.0f,
			1.0f, 0.0f,
			0.5f, 0.5f,
			1.0f, 0.5f,
		};
		Texture texture = new Texture("/res/grassblock.png");
		Mesh mesh = new Mesh(positions, indices, coords, texture);
		Item item = new Item(mesh);
		this.items = new Item[]{item};
		
		// Use correct depth checking
		glEnable(GL_DEPTH_TEST);
	}
	
	@Override
	public void input(Window window) {
		this.direction = 0;
		if (window.isKeyDown(GLFW_KEY_UP)) this.direction++;
		if (window.isKeyDown(GLFW_KEY_DOWN)) this.direction--;
		
		this.movement.zero();
		if (window.isKeyDown(GLFW_KEY_W)) this.movement.z--;
		if (window.isKeyDown(GLFW_KEY_A)) this.movement.x--;
		if (window.isKeyDown(GLFW_KEY_S)) this.movement.z++;
		if (window.isKeyDown(GLFW_KEY_D)) this.movement.x++;
		if (window.isKeyDown(GLFW_KEY_SPACE)) this.movement.y++;
		if (window.isKeyDown(GLFW_KEY_LEFT_SHIFT)) this.movement.y--;
		if (window.isKeyDown(GLFW_KEY_LEFT_CONTROL)) this.movement.z *= 2;
	}
	
	@Override
	public void update(float interval) {
		this.color += this.direction * 0.01f;
		if (color > 1) this.color = 1.0f;
		if (color < 0) this.color = 0.0f;
		
		Vector3f movement = new Vector3f();
		this.movement.mul(-0.05f, movement);
		for (Item item : this.items) {
			item.position.add(movement);
			if (this.rotate) {
				item.rotation.add(1.5f, 1.5f, 1.5f);
				item.rotation.x %= 360;
				item.rotation.y %= 360;
				item.rotation.z %= 360;
			}
		}
	}
	
	@Override
	public void render(Window window) {
		// Check for resize / vSync change (not sure if ordering matters)
		window.checkUpdateSize();
		window.checkUpdateVSync();
		
		// clear the framebuffer
		window.clear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		// Different color based on vSync or not (colorful = vSync on)
		if (window.isVSync())
			window.clearColor(1-this.color, this.color/2+0.5f, this.color, 0.0f);
		else
			window.clearColor(this.color, this.color, this.color, 0.0f);
		
		this.renderer.render(window, this.items);
		
		// Swap buffers
		window.update();
	}
	
	@Override
	public void cleanup() {
		this.renderer.cleanup();
		for (Item item : this.items)
			item.mesh.cleanup();
	}
}
