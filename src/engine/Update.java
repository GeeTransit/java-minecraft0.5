/*
George Zhang
Update class
*/

package geetransit.minecraft05.engine;

import java.util.List;
import java.util.ArrayList;

public class Update implements Updateable {
	private List<Updateable> updates;
	
	public Update(List<Updateable> updates) {
		this.updates = updates;
	}
	public Update() { this(new ArrayList<>()); }

	public List<Updateable> getUpdates() { return this.updates; }
	public Update addUpdate(Updateable update) { this.updates.add(update); return this; }

	@Override
	public void update(float interval) {
		for (Updateable update : this.getUpdates())
			update.update(interval);
	}
}
