/*
George Zhang
Bucket class (map of lambdas).
*/

package geetransit.minecraft05.engine;

import java.util.*;

public class Bucket {
	public final Map<String, Runnable> next;
	public Bucket() { this.next = new HashMap<>(); }

	public boolean run(String string) {
		Runnable runnable = this.remove(string);
		if (runnable == null)
			return false;
		runnable.run();
		return true;
	}
	public boolean contains(String string) { return this.next.containsKey(string); }
	public Runnable remove(String string) { return this.next.remove(string); }
	public boolean empty() { return this.next.isEmpty(); }
	public Bucket add(String string, Runnable runnable) { this.next.put(string, runnable); return this; }

	public String toString() { return this.next.toString(); }
}
