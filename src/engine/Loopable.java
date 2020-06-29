/*
George Zhang
Loopable interface class. (used by Window)
*/

package geetransit.minecraft05.engine;

public interface Loopable extends Initializable, Inputtable, Updateable, Renderable {
	@Override
	default void init(Window window) {}

	@Override
	default void input(Window window) {}

	@Override
	default void update(float interval) {}

	@Override
	default void render(Window window) {}
}
