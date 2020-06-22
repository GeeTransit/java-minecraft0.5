/*
George Zhang
Base scene implementation. (no rendering)
*/

package geetransit.minecraft05.engine;

import java.util.*;

public abstract class SceneBase implements Loopable {
	private List<Loopable> scenes;

	public SceneBase() {
		this.scenes = new ArrayList<>();
	}
	
	public List<Loopable> getScenes() { return this.scenes; }
	public SceneBase addScene(Loopable scene) { this.scenes.add(scene); return this; }
	
	public void init(Window window) throws Exception {
		for (Loopable scene : this.getScenes())
			scene.init(window);
	}
	
	public void input(Window window) {
		for (Loopable scene : this.getScenes())
			scene.input(window);
	}
	
	public void update(float interval) {
		for (Loopable scene : this.getScenes())
			scene.update(interval);
	}
	
	public void render(Window window) {
		for (Loopable scene : this.getScenes())
			scene.render(window);
	}
	
	public void cleanup() {
		for (Loopable scene : this.getScenes())
			scene.cleanup();
	}
}
