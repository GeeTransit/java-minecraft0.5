/*
George Zhang
Base scene implementation. (no rendering)
*/

package geetransit.minecraft05.engine;

import java.util.List;
import java.util.ArrayList;

public abstract class SceneBase implements Scene {
	private List<Scene> scenes;

	public SceneBase() {
		this.scenes = new ArrayList<>();
	}
	
	public List<Scene> getScenes() { return this.scenes; }
	public SceneBase addScene(Scene scene) { this.scenes.add(scene); return this; }
	
	public void init(Window window) throws Exception {
		for (Scene scene : this.getScenes())
			scene.init(window);
	}
	
	public void input(Window window) {
		for (Scene scene : this.getScenes())
			scene.input(window);
	}
	
	public void update(float interval) {
		for (Scene scene : this.getScenes())
			scene.update(interval);
	}
	
	public void render(Window window) {
		for (Scene scene : this.getScenes())
			scene.render(window);
	}
	
	public void cleanup() {
		for (Scene scene : this.getScenes())
			scene.cleanup();
	}
}
