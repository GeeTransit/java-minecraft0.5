/*
ahbejarano
Skybox class.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

public class Skybox extends Scene {
	private Renderer renderer;
	private Camera camera;
	private Item skybox;

	public Skybox(Camera camera) {
		super();
		this.addFrom(this.renderer = new Renderer() {
			Shader shader;
			public void init(Window window) throws Exception {
				shader = create3D("/res/vertex-3d.vs", "/res/fragment-3d.fs");
			}
			public void render(Window window) {
				render3DSkybox(shader, window, Skybox.this.camera);
			}
			public void cleanup() {
				destroy(shader);
			}
		});
		this.camera = camera;
	}

	@Override
	public void init(Window window) throws Exception {
		Mesh mesh = ObjLoader.loadMesh("/res/skybox.obj");
		mesh.setTexture(new Texture("/res/skybox.png"));
		this.skybox = new Item(mesh).setPosition(0, 0, 0);
		this.renderer.addItem(this.skybox);
		super.init(window);
	}

	@Override
	public void render(Window window) {
		this.skybox.setScale(this.camera.getFar() * 0.5f);
		super.render(window);
	}
}
