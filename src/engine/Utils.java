/*
ahbejarano
Utility methods.
*/

package geetransit.minecraft05.engine;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import java.nio.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class Utils {
	
	public static InputStream loadInputStream(String file) throws Exception {
		return Utils.class.getResourceAsStream(file);
	}
	
	// source # https://stackoverflow.com/a/17861016
	public static byte[] loadByteArray(String file) throws Exception {
		InputStream in = loadInputStream(file);
		ByteArrayOutputStream out = new ByteArrayOutputStream(); 
		byte[] buffer = new byte[0xFFFF];
		int len;
		while ((len = in.read(buffer)) != -1)
			out.write(buffer, 0, len);
		return out.toByteArray();
	}

	public static String loadResource(String file) throws Exception {
		try (
			InputStream in = loadInputStream(file);
			Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name());
		) {
			return scanner.useDelimiter("\\A").next();
		}
	}

	public static Stream<String> loadLinesStream(String file) throws Exception {
		// source # https://stackoverflow.com/a/30336423
		InputStream in = loadInputStream(file);
		InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8.name());
		return new BufferedReader(isr).lines();
	}
	
	public static int[] intListToArray(List<Integer> intList) {
		return intList.stream().mapToInt(i -> i).toArray();
	}
	public static float[] floatListToArray(List<Float> floatList) {
		float[] floatArray = new float[floatList.size()];
		IntStream.range(0, floatList.size()).forEach(i -> { floatArray[i] = floatList.get(i).floatValue(); });
		return floatArray;
	}
}
