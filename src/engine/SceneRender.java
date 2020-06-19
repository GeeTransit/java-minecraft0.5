/*
George Zhang
Base rendered scene implementation.
*/

package geetransit.minecraft05.engine;

import java.util.*;

public abstract class SceneRender extends SceneBase {
	private Renderer renderer;
	private List<Item> items;
	private Map<Mesh, List<Item>> meshMap;

	public SceneRender(Renderer renderer) {
		super();
		this.renderer = renderer;
		this.items = new ArrayList<>();
		this.meshMap = new HashMap<>();
	}
	public SceneRender() {
		this(null);
	}
	
	public Renderer getRenderer() { return this.renderer; }
	public SceneRender setRenderer(Renderer renderer) { this.renderer = renderer; return this; }
	
	public List<Item> getItems() { return this.items; }
	public Map<Mesh, List<Item>> getMeshMap() { return this.meshMap; }
	public SceneRender addItem(Item item) {
		Mesh mesh = item.getMesh();
		this.items.add(item);
		this.meshMap.putIfAbsent(mesh, new ArrayList<>());
		this.meshMap.get(mesh).add(item);
		return this;
	}
	public SceneRender removeItem(Item item) {
		Mesh mesh = item.getMesh();
		this.items.remove(item);
		this.meshMap.get(mesh).remove(item);
		if (this.meshMap.get(mesh).size() == 0)
			this.meshMap.remove(mesh);
		return this;
	}
	
	@Override
	public void init(Window window) throws Exception {
		this.renderer.init(window);
		super.init(window);
	}
	
	@Override
	public void input(Window window) {
		super.input(window);
	}
	
	@Override
	public void render(Window window) {
		this.renderer.render(window);
		super.render(window);
	}
	
	@Override
	public void cleanup() {
		this.renderer.cleanup();
		super.cleanup();
		for (Item item : this.getItems())
			item.cleanup();
	}
}
