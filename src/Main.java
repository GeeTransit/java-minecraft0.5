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
			Window window = new Window("Hello World!", 300, 300, Window.WINDOWED, /*vSync*/ true, /*targetFps*/ 10);
			Loopable loop = new Game();
			Engine engine = new Engine(window, loop);
			engine.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
