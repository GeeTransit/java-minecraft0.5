/*
George Zhang
Init class
*/

package geetransit.minecraft05.engine;

import java.util.List;
import java.util.ArrayList;

public class Init implements Initializable {
	private List<Initializable> inits;
	
	public Init(List<Initializable> inits) {
		this.inits = inits;
	}
	public Init() { this(new ArrayList<>()); }

	public List<Initializable> getInits() { return this.inits; }
	public Init addInit(Initializable init) { this.inits.add(init); return this; }

	@Override
	public void init(Window window) throws Exception {
		for (Initializable init : this.getInits())
			init.init(window);
	}

	@Override
	public void cleanup() {
		for (Initializable init : this.getInits())
			init.cleanup();
	}
}
