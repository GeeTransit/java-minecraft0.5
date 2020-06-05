# Java Template Project

Place your java source files (.java) into the `src/` folder. Run / double click `run.bat` to compile, package, and launch the app.

## Requirements

Tested on Windows 7 only.

## Advanced

When there is a word in `inline code`, it signifies a variable inside the `config.ini` file, under the `[PROJECT]` heading.

There are 3 stages inside run.bat:
1. Compiling (src/\*.java -> bin/\*.class)
2. Packaging (bin/\*.class -> dist/.jar)
3. Launching (java -jar dist/.jar)

### Compiling

All .java files inside `source` will be compiled into `compile`.

**NOTE: All files will be compiled, regardless of whether the file has been modified or not.**

### Packaging

All files inside `compile`, `library`, and `resource` will be zipped into a .jar inside `package`.

### Launching

The .jar file inside `package` will be launched using `java -jar %JARFILE%`.
