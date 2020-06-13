/*
ahbejarano
Utility methods.
*/

package geetransit.minecraft05.engine;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Scanner;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Utils {
	
	public static InputStream loadStream(String file) throws Exception {
		return Utils.class.getResourceAsStream(file);
	}
	
	// source # https://stackoverflow.com/a/17861016
	public static byte[] loadByteArray(String file) throws Exception {
		InputStream in = loadStream(file);
		ByteArrayOutputStream out = new ByteArrayOutputStream(); 
		byte[] buffer = new byte[0xFFFF];
		int len;
		while ((len = in.read(buffer)) != -1)
			out.write(buffer, 0, len);
		return out.toByteArray();
	}

	public static String loadResource(String file) throws Exception {
		try (
			InputStream in = loadStream(file);
			Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name());
		) {
			return scanner.useDelimiter("\\A").next();
		}
	}

}
