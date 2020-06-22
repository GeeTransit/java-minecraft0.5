/*
George Zhang
Engine class.
*/

package geetransit.minecraft05.engine;

public class Engine implements Runnable {
	private Window window;
	private Loopable loop;
	
	private boolean updateVSync = false;
	private boolean updateSize = false;
	private boolean destroyed = false;
	
	public Engine(
		Window window,
		Loopable loop
	) {
		this.window = window;
		this.loop = loop;
	}
	
	@Override
	public void run() {
		this.window.createWindow();
		new Thread(() -> this.window.renderThread(this.loop)).start();
		this.window.eventThread();
	}
	
	public Window getWindow() { return this.window; }
	public Loopable getLoop() { return this.loop; }
}
