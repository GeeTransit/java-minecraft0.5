/*
George Zhang
Encapsulate an image (used to get pixel values).
*/

package geetransit.minecraft05.engine;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.stb.STBImage;

public class Image implements AutoCloseable {
	public static final int CHANNELS = 4;
	public static final int MAX = 0x00FFFFFF;
	public static final int MAX_ALPHA = 0xFFFFFFFF;

	public final ByteBuffer buffer;
	public final int width;
	public final int length;

	public Image(String file) { this(Utils.loadByteArray(file)); }
	public Image(byte[] array) { this((ByteBuffer) MemoryUtil.memAlloc(array.length).put(array).flip()); }
	public Image(ByteBuffer raw) {
		// Load Texture file
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer width = stack.mallocInt(1);
			IntBuffer length = stack.mallocInt(1);
			IntBuffer channels = stack.mallocInt(1);

			this.buffer = STBImage.stbi_load_from_memory(raw, width, length, channels, 4);
			if (this.buffer == null)
				throw new RuntimeException("Buffer could not be loaded: " + STBImage.stbi_failure_reason());

			// store width and length of image
			this.width = width.get();
			this.length = length.get();
		}
	}

	@Override
	public void close() {
		STBImage.stbi_image_free(this.buffer);
	}

	// usage: compressExpand(heightAt(...), 0, MAX_COLOR, 0, 16)
	public static float compressExpand(int f, float cMin, float cMax, float eMin, float eMax) {
		return expand(compress(f, cMin, cMax), eMin, eMax);
	}
	public static float compress(int f, float min, float max) { return (f-min) / (max-min); }
	public static float expand(float f, float min, float max) { return min + f*(max-min); }

	// RRGGBB
	public int pixel(int x, int z) {
		int i = x*CHANNELS + z*CHANNELS*this.width;
		return 0
			| (this.buffer.get(i + 0) & 0xFF) << 020
			| (this.buffer.get(i + 1) & 0xFF) << 010
			| (this.buffer.get(i + 2) & 0xFF) << 000;
	}

	// RRGGBBAA
	// remember to use >>> when shifting down
	public int pixelAlpha(int x, int z) {
		int i = x*CHANNELS + z*CHANNELS*this.width;
		return 0
			| (this.buffer.get(i + 0) & 0xFF) << 030
			| (this.buffer.get(i + 1) & 0xFF) << 020
			| (this.buffer.get(i + 2) & 0xFF) << 010
			| (this.buffer.get(i + 3) & 0xFF) << 000;
	}
}
