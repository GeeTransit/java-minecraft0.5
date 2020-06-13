/*
George Zhang
Main entry class.
*/

package geetransit.minecraft05;

import geetransit.minecraft05.engine.*;
import geetransit.minecraft05.game.*;

public class Main {

	public static void main(String[] args) {
		try {
			boolean fullscreen = false;
			boolean vSync = true;
			int targetFps = 5;
			int targetUps = 30;
			Window window = new Window("Hello World!", 300, 300, fullscreen, vSync, targetFps, targetUps);
			ILogic logic = new Game();
			Engine engine = new Engine(window, logic);
			engine.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
