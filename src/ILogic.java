/*
George Zhang
2020-06-08
Game logic interface.
*/

package geetransit.minecraft05;

public interface ILogic {
	
	void init(Window window);
	
	void input(Window window);
	
	void update(float interval);
	
	void render(Window window);
}