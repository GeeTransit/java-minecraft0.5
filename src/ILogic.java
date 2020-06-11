/*
George Zhang
Game logic interface.
*/

package geetransit.minecraft05.engine;

public interface ILogic {
	
	void init(Window window) throws Exception;
	
	void input(Window window);
	
	void update(float interval);
	
	void render(Window window);
	
	void cleanup();
}
