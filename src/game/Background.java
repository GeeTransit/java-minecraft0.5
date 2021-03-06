/*
George Zhang
Background scene.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

import static org.lwjgl.glfw.GLFW.*;

public class Background implements Scene {
	private int direction;
	private float color;
	
	private Camera camera;
	private int render;
	
	public Background(Camera camera) {
		this.direction = 0;
		this.color = 0.5f;
		this.camera = camera;
		this.render = 0;
	}
	
	@Override
	public void init(Window window) throws Exception {
		// blank background for first frame
		window.clearColor(1f, 1f, 1f, 0f);
	}
	
	@Override
	public void input(Window window) {
		if (window.isKeyDown(GLFW_KEY_L)) this.color = 0f;
		
		this.direction = 0;
		if (window.isKeyDown(GLFW_KEY_UP)) this.direction++;
		if (window.isKeyDown(GLFW_KEY_DOWN)) this.direction--;
		
		// render distance (camera)
		this.render = 0;
		if (window.isKeyDown(GLFW_KEY_L)) this.camera.setFar(Camera.FAR);
		if (window.isKeyDown(GLFW_KEY_RIGHT_BRACKET)) this.render++;
		if (window.isKeyDown(GLFW_KEY_LEFT_BRACKET)) this.render--;
	}
	
	@Override
	public void update(float interval) {
		this.color = Math.max(0f, Math.min(1f, this.color+0.01f*this.direction));
		this.camera.setFar(Math.max(Camera.NEAR+0.01f, this.camera.getFar() + 0.1f*this.render));
	}
	
	@Override
	public void render(Window window) {
		// Different color based on vSync or not (colorful = vSync on)
		if (window.isVSync())
			window.clearColor(1-this.color, this.color/2+0.5f, this.color, 0.0f);
		else
			window.clearColor(this.color, this.color, this.color, 0.0f);
	}
	
	@Override
	public void cleanup() {}
}
