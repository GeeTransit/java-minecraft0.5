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
			int mode = Window.WINDOWED;
			boolean vSync = true;
			int targetFps = 10;
			Window window = new Window("Hello World!", 300, 300, mode, vSync, targetFps);
			Loopable loop = new Game();
			Engine engine = new Engine(window, loop);
			engine.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
