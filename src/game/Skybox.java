/*
ahbejarano
Skybox class.
*/

package geetransit.minecraft05.game;

import geetransit.minecraft05.engine.*;

public class Skybox extends SceneRender {
	private Camera camera;
	private Item skybox;

    public Skybox(Camera camera) {
        super();
		this.setRenderer(new Renderer() {
			Shader shader;
			public void init(Window window) throws Exception {
				shader = create3D("/res/vertex-3d.vs", "/res/fragment-3d.fs");
			}
			public void render(Window window) {
				renderSkybox(shader, window, Skybox.this.getCamera(), Skybox.this.getItems());
			}
			public void cleanup() {
				shader.cleanup();
			}
		});
		this.camera = camera;
    }
	
	public Camera getCamera() { return this.camera; }
	
	@Override
	public void init(Window window) throws Exception {
		super.init(window);
		Mesh mesh = ObjLoader.loadMesh("/res/skybox.obj");
		mesh.setTexture(new Texture("/res/skybox.png"));
		this.skybox = new Item(mesh).setPosition(0, 0, 0);
		this.addItem(this.skybox);
	}
	
	@Override
	public void render(Window window) {
		this.skybox.setScale(this.camera.getFar() * 0.5f);
		super.render(window);
	}
}
