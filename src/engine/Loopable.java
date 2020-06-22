/*
George Zhang
Loopable interface class. (used by Window)
*/

package geetransit.minecraft05.engine;

public interface Loopable extends Initializable, Inputtable, Updateable, Renderable {
	void init(Window window) throws Exception;
	void input(Window window);
	void update(float interval);
	void render(Window window);
	void cleanup();
}
