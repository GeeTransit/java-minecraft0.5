/*
George Zhang
Render class
*/

package geetransit.minecraft05.engine;

import java.util.List;
import java.util.ArrayList;

public class Render implements Renderable {
	private List<Renderable> renders;
	
	public Render(List<Renderable> renders) {
		this.renders = renders;
	}
	public Render() { this(new ArrayList<>()); }

	public List<Renderable> getRenders() { return this.renders; }
	public Render addRender(Renderable render) { this.renders.add(render); return this; }

	@Override
	public void render(Window window) {
		for (Renderable render : this.getRenders())
			render.render(window);
	}
}
