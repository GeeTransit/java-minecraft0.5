/*
George Zhang
Base rendered scene implementation.
*/

package geetransit.minecraft05.engine;

import java.util.List;
import java.util.ArrayList;

public abstract class SceneRender extends SceneBase {
	protected Renderer renderer;
	protected List<Item> items;

	public SceneRender(Renderer renderer) {
		super();
		this.renderer = renderer;
		this.items = new ArrayList<>();
	}
	public SceneRender() {
		this(null);
	}
	
	public Renderer getRenderer() { return this.renderer; }
	public List<Item> getItems() { return this.items; }
	public SceneRender setRenderer(Renderer renderer) { this.renderer = renderer; return this; }
	public SceneRender addItem(Item item) { this.items.add(item); return this; }
	
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
