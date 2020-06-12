/*
ahbejarano
Utility methods.
*/

package geetransit.minecraft05.engine;

import java.io.InputStream;
import java.util.Scanner;

import java.nio.charset.StandardCharsets;

public class Utils {

	public static String loadResource(String fileName) throws Exception {
		try (
			InputStream in = Utils.class.getResourceAsStream(fileName);
			Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name());
		) {
			return scanner.useDelimiter("\\A").next();
		}
	}

}
