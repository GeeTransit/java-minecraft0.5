/*
ahbejarano
2020-06-07
Timer utility class.
Source # https://github.com/lwjglgamedev/lwjglbook/blob/master/chapter02/src/main/java/org/lwjglb/engine/Timer.java
*/

package geetransit.minecraft05;

public class Timer {

    private double lastLoopTime;
    
    public void init() {
        this.lastLoopTime = getTime();
    }

    public double getTime() {
        return System.nanoTime() / 1000_000_000.0;
    }

    public float getElapsedTime() {
        double time = getTime();
        float elapsedTime = (float) (time - this.lastLoopTime);
        this.lastLoopTime = time;
        return elapsedTime;
    }

    public double getLastLoopTime() {
        return this.lastLoopTime;
    }
}
