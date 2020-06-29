/*
ahbejarano
Shader wrapper class.
*/

package geetransit.minecraft05.engine;

import java.util.*;
import org.joml.*;
import org.lwjgl.system.*;
import static org.lwjgl.opengl.GL20.*;

public class Shader {
	private final int program;

	private int vertex;
	private int fragment;

	private final Map<String, Integer> uniforms;

	public Shader() throws Exception {
		this.program = glCreateProgram();
		if (this.program == 0)
			throw new Exception("Could not create Shader");
		this.uniforms = new HashMap<>();
	}

	public void createUniform(String name) throws Exception {
		int location = glGetUniformLocation(this.program, name);
		if (location < 0)
			throw new Exception("Could not find uniform:" + name);
		this.uniforms.put(name, location);
	}

	public void setUniform(String name, Matrix4f value) {
		// Dump the matrix into a float buffer
		try (MemoryStack stack = MemoryStack.stackPush()) {
			glUniformMatrix4fv(this.uniforms.get(name), false, value.get(stack.mallocFloat(16)));
		}
	}
	public void setUniform(String name, int value) {
		glUniform1i(this.uniforms.get(name), value);
	}
	public void setUniform(String name, boolean value) {
		this.setUniform(name, value ? 1 : 0);
	}
	public void setUniform(String name, Vector3f value) {
		glUniform3f(this.uniforms.get(name), value.x, value.y, value.z);
	}
	public void setUniform(String name, Vector4f value) {
		glUniform4f(this.uniforms.get(name), value.x, value.y, value.z, value.w);
	}

	public void createVertexShader(String code) throws Exception {
		this.vertex = this.createShader(code, GL_VERTEX_SHADER);
	}

	public void createFragmentShader(String code) throws Exception {
		this.fragment = this.createShader(code, GL_FRAGMENT_SHADER);
	}

	protected int createShader(String code, int type) throws Exception {
		int id = glCreateShader(type);
		if (id == 0)
			throw new Exception("Error creating shader. Type: " + type);

		glShaderSource(id, code);
		glCompileShader(id);

		if (glGetShaderi(id, GL_COMPILE_STATUS) == 0)
			throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(id, 1024));
		glAttachShader(this.program, id);

		return id;
	}

	public void link() throws Exception {
		glLinkProgram(this.program);
		if (glGetProgrami(this.program, GL_LINK_STATUS) == 0)
			throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(this.program, 1024));

		if (this.vertex != 0)
			glDetachShader(this.program, this.vertex);
		if (this.fragment != 0)
			glDetachShader(this.program, this.fragment);

		// definition in res/vertex.vs
		// equivalent of `layout (location = #) ...`
		glBindAttribLocation(this.program, 0, "position");
		glBindAttribLocation(this.program, 1, "coords");
		glValidateProgram(this.program);
		if (glGetProgrami(this.program, GL_VALIDATE_STATUS) == 0)
			System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(this.program, 1024));
	}

	public void bind() {
		glUseProgram(this.program);
	}

	public void unbind() {
		glUseProgram(0);
	}

	public void cleanup() {
		this.unbind();
		if (this.program != 0)
			glDeleteProgram(this.program);
	}
}
