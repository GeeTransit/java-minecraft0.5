/*
George Zhang
Ticker class.
*/

package geetransit.minecraft05.engine;

public class Ticker {
	private float interval;
	private float accumulated;

	public Ticker(float interval) {
		this.interval = interval;
		this.accumulated = 0f;
	}

	public float getInterval() { return this.interval; }
	public Ticker setInterval(float interval) { this.interval = interval; return this; }

	public Ticker add(float time) {
		this.accumulated += time;
		return this;
	}

	public Ticker reset() {
		this.accumulated = 0f;
		return this;
	}

	// while (ticker.next())
	public boolean next() {
		if (this.accumulated < this.interval)
			return false;
		this.accumulated -= this.interval;
		return true;
	}

	// if (ticker.nextOnce())
	public boolean nextOnce() {
		if (this.accumulated < this.interval)
			return false;
		this.accumulated %= this.interval;
		return true;
	}
}
