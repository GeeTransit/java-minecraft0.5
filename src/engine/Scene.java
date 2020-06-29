/*
George Zhang
Scene class.
Encapsulates a whole scene (init, loop, cleanup)
*/

package geetransit.minecraft05.engine;

import java.util.*;

public class Scene implements Loopable {
	// bit flags to add to different lists
	public static final int INIT = 0b0001;
	public static final int INPUT = 0b0010;
	public static final int UPDATE = 0b0100;
	public static final int RENDER = 0b1000;
	public static final int ALL = INIT & INPUT & UPDATE & RENDER;

	private final List<Initializable> inits;
	private final List<Inputtable> inputs;
	private final List<Updateable> updates;
	private final List<Renderable> renders;

	public Scene() {
		this.inits = new ArrayList<>();
		this.inputs = new ArrayList<>();
		this.updates = new ArrayList<>();
		this.renders = new ArrayList<>();
	}

	public List<Initializable> getInits() { return this.inits; }
	public List<Inputtable> getInputs() { return this.inputs; }
	public List<Updateable> getUpdates() { return this.updates; }
	public List<Renderable> getRenders() { return this.renders; }

	public Scene addInit(Initializable init) { this.inits.add(init); return this; }
	public Scene addInput(Inputtable input) { this.inputs.add(input); return this; }
	public Scene addUpdate(Updateable update) { this.updates.add(update); return this; }
	public Scene addRender(Renderable render) { this.renders.add(render); return this; }

	public Scene addTo(int flags, Object obj) {
		if ((flags & INIT) != 0)
			this.addInit((Initializable) obj);
		if ((flags & INPUT) != 0)
			this.addInput((Inputtable) obj);
		if ((flags & UPDATE) != 0)
			this.addUpdate((Updateable) obj);
		if ((flags & RENDER) != 0)
			this.addRender((Renderable) obj);
		return this;
	}

	public Scene addFrom(Object obj) {
		if (obj instanceof Initializable)
			this.addTo(INIT, obj);
		if (obj instanceof Inputtable)
			this.addTo(INPUT, obj);
		if (obj instanceof Updateable)
			this.addTo(UPDATE, obj);
		if (obj instanceof Renderable)
			this.addTo(RENDER, obj);
		return this;
	}

	@Override
	public void init(Window window) {
		for (Initializable init : this.getInits())
			init.init(window);
	}

	@Override
	public void input(Window window) {
		for (Inputtable input : this.getInputs())
			input.input(window);
	}

	@Override
	public void render(Window window) {
		for (Renderable render : this.getRenders())
			render.render(window);
	}

	@Override
	public void update(float interval) {
		for (Updateable update : this.getUpdates())
			update.update(interval);
	}

	@Override
	public void cleanup() {
		for (Initializable init : this.getInits())
			init.cleanup();
	}
}
