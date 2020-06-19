/*
George Zhang
Engine class.
*/

package geetransit.minecraft05.engine;

public class Engine implements Runnable {
	private Window window;
	private Scene scene;
	
	private boolean updateVSync = false;
	private boolean updateSize = false;
	private boolean destroyed = false;
	
	public Engine(
		Window window,
		Scene scene
	) {
		this.window = window;
		this.scene = scene;
	}
	
	@Override
	public void run() {
		this.window.createWindow();
		new Thread(() -> this.window.renderThread(this.scene)).start();
		this.window.eventThread();
	}
	
	public Window getWindow() { return this.window; }
	public Scene getScene() { return this.scene; }
}
