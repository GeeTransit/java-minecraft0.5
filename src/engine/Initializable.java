/*
George Zhang
Initializable interface.
*/

package geetransit.minecraft05.engine;

public interface Initializable {
	void init(Window window) throws Exception;
	void cleanup();
}
