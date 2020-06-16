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
		this.setRenderer(new Renderer(this) {
			public Shader create(Window window) throws Exception {
				return this.create3D("/res/vertex-3d.vs", "/res/fragment-3d.fs");
			}
			public void render(Window window) {
				this.renderSkybox(window, Skybox.this.getCamera());
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
	public void input(Window window) {
		super.input(window);
		this.skybox.setScale(this.camera.getFar() * 0.5f);
	}
}
