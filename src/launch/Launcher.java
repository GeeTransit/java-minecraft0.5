/*
App launcher.
Note the absence of a package.
Source # http://www.jdotsoft.com/JarClassLoader.php
*/

import com.jdotsoft.jarloader.JarClassLoader;

public class Launcher {
	// change this string to your main class.
	public static final String invoke = "geetransit.minecraft05.Main";
	public static void main(String[] args) throws Throwable {
		new JarClassLoader().invokeMain(invoke, args);
	}
}
