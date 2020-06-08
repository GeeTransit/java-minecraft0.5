/*
George Zhang
2020-06-08
Game logic interface.
*/

package geetransit.minecraft05;

public interface ILogic {
	
	void init(Engine engine) throws Exception;
	
	void input(Engine engine);
	
	void update(float interval);
	
	void render(Engine engine);
}
