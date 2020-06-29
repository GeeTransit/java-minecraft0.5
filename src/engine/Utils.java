/*
ahbejarano
Utility methods.
*/

package geetransit.minecraft05.engine;

import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;

import java.nio.*;
import java.nio.charset.StandardCharsets;

import org.lwjgl.system.*;
import org.lwjgl.stb.*;

public class Utils {

	public static InputStream loadInputStream(String file) {
		InputStream in = Utils.class.getResourceAsStream(file);
		if (in == null)
			throw new RuntimeException("file [" + file + "] does not exist");
		return in;
	}

	// source # https://stackoverflow.com/a/17861016
	public static byte[] loadByteArray(String file) {
		try (
			InputStream in = loadInputStream(file);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
		) {
			byte[] buffer = new byte[0xFFFF];
			int len;
			while ((len = in.read(buffer)) != -1)
				out.write(buffer, 0, len);
			return out.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String loadResource(String file) {
		try (
			InputStream in = loadInputStream(file);
			Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name());
		) {
			return scanner.useDelimiter("\\A").next();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Stream<String> loadLinesStream(String file) {
		try {
			// source # https://stackoverflow.com/a/30336423
			InputStream in = loadInputStream(file);
			InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8.name());
			return new BufferedReader(isr).lines();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static ByteBuffer loadImage(String fileName, BiConsumer<Integer, Integer> consumer) {
		ByteBuffer imageBuffer;
		ByteBuffer rawBuffer;

		// Load Texture file
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer widthBuffer = stack.mallocInt(1);
			IntBuffer heightBuffer = stack.mallocInt(1);
			IntBuffer channelsBuffer = stack.mallocInt(1);

			byte[] array = loadByteArray(fileName);
			rawBuffer = MemoryUtil.memAlloc(array.length);
			rawBuffer.put(array).flip();

			imageBuffer = STBImage.stbi_load_from_memory(rawBuffer, widthBuffer, heightBuffer, channelsBuffer, 4);
			if (imageBuffer == null)
				throw new RuntimeException("Image file [" + fileName + "] not loaded: " + STBImage.stbi_failure_reason());

			// Get width and height of image
			consumer.accept(widthBuffer.get(), heightBuffer.get());
		}

		return imageBuffer;
	}
	public static ByteBuffer loadImage(String fileName, int[] widthArray, int[] heightArray) {
		return loadImage(fileName, (width, height) -> { widthArray[0] = width; heightArray[0] = height; });
	}
	public static void freeImage(ByteBuffer imageBuffer) {
		STBImage.stbi_image_free(imageBuffer);
	}

	public static int[] intListToArray(List<Integer> intList) {
		return intList.stream().mapToInt(i -> i).toArray();
	}
	public static float[] floatListToArray(List<Float> floatList) {
		float[] floatArray = new float[floatList.size()];
		int i = 0;
		for (float f : floatList)
			floatArray[i++] = f;
		return floatArray;
	}
}
