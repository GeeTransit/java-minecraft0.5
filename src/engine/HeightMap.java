/*
George Zhang
Encapsulate an image (used to get pixel values).
*/

package geetransit.minecraft05.engine;

import java.nio.ByteBuffer;

public class HeightMap implements AutoCloseable {
	public static final int CHANNELS = 4;
	public static final int MAX_COLOR = 255*255*255;

	public final ByteBuffer buffer;
	public final int width;
	public final int length;

	public HeightMap(ByteBuffer buffer, int width, int length) {
		this.buffer = buffer;
		this.width = width;
		this.length = length;
	}

	public static HeightMap loadFromImage(String fileName) {
		int width[] = {0}, length[] = {0};
		ByteBuffer buffer = Utils.loadImage(fileName, width, length);
		return new HeightMap(buffer, width[0], length[0]);
	}

	@Override
	public void close() {
		Utils.freeImage(this.buffer);
	}

	// usage: compressExpand(heightAt(...), 0, MAX_COLOR, 0, 16)
	public static float compressExpand(int f, float cMin, float cMax, float eMin, float eMax) {
		return expand(compress(f, cMin, cMax), eMin, eMax);
	}
	public static float compress(int f, float min, float max) { return (f-min) / (max-min); }
	public static float expand(float f, float min, float max) { return min + f*(max-min); }

	public int heightAt(int x, int z) { return this.heightAt(x*CHANNELS + z*CHANNELS*this.width); }
	public int heightAt(int i) {
		byte r = this.buffer.get(i + 0);
		byte g = this.buffer.get(i + 1);
		byte b = this.buffer.get(i + 2);
		byte a = this.buffer.get(i + 3);
		return 0
			// | ((0xFF & a) << 24)  // removed cuz it turns overflows int
			| ((0xFF & r) << 16)
			| ((0xFF & g) << 8)
			| ((0xFF & b) << 0);
	}
}
