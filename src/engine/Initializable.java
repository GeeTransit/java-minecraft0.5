/*
George Zhang
Initializable interface.
*/

package geetransit.minecraft05.engine;

public interface Initializable {
	void init(Window window);
	default void cleanup() {}
}
