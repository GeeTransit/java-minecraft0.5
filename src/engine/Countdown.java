/*
George Zhang
Countdown class.
*/

package geetransit.minecraft05.engine;

public class Countdown {
	private float interval;
	private float wait;

	public Countdown(float interval) {
		this.interval = interval;
		this.wait = 0f;
	}

	public float getInterval() { return this.interval; }
	public Countdown setInterval(float interval) { this.interval = interval; return this; }

	public Countdown add(float time) {
		this.wait -= time;
		return this;
	}

	public Countdown reset() {
		this.wait = 0f;
		return this;
	}

	// while (countdown.next())
	public boolean next() {
		if (this.wait > 0)
			return false;
		this.wait += this.interval;
		return true;
	}

	// if (countdown.nextOnce())
	public boolean nextOnce() {
		if (this.wait > 0)
			return false;
		this.wait = this.interval;
		return true;
	}
}
