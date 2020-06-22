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
	private final int programId;

	private int vertexShaderId;
	private int fragmentShaderId;

	private final Map<String, Integer> uniforms;

	public Shader() throws Exception {
		this.programId = glCreateProgram();
		if (this.programId == 0) {
			throw new Exception("Could not create Shader");
		}
		this.uniforms = new HashMap<>();
	}

	public void createUniform(String name) throws Exception {
		int location = glGetUniformLocation(this.programId, name);
		if (location < 0) {
			throw new Exception("Could not find uniform:" + name);
		}
		uniforms.put(name, location);
	}

	public void setUniform(String name, Matrix4f value) {
		// Dump the matrix into a float buffer
		try (MemoryStack stack = MemoryStack.stackPush()) {
			glUniformMatrix4fv(uniforms.get(name), false, value.get(stack.mallocFloat(16)));
		}
	}
	public void setUniform(String name, int value) {
		glUniform1i(uniforms.get(name), value);
	}
	public void setUniform(String name, boolean value) {
		glUniform1i(uniforms.get(name), value ? 1 : 0);
	}
	public void setUniform(String name, Vector3f value) {
		glUniform3f(uniforms.get(name), value.x, value.y, value.z);
	}
	public void setUniform(String name, Vector4f value) {
		glUniform4f(uniforms.get(name), value.x, value.y, value.z, value.w);
	}

	public void createVertexShader(String shaderCode) throws Exception {
		this.vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
	}

	public void createFragmentShader(String shaderCode) throws Exception {
		this.fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
	}

	protected int createShader(String shaderCode, int shaderType) throws Exception {
		int shaderId = glCreateShader(shaderType);
		if (shaderId == 0) {
			throw new Exception("Error creating shader. Type: " + shaderType);
		}

		glShaderSource(shaderId, shaderCode);
		glCompileShader(shaderId);

		if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
			throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
		}
		glAttachShader(programId, shaderId);

		return shaderId;
	}

	public void link() throws Exception {
		glLinkProgram(this.programId);
		if (glGetProgrami(this.programId, GL_LINK_STATUS) == 0) {
			throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(this.programId, 1024));
		}

		if (this.vertexShaderId != 0) {
			glDetachShader(this.programId, this.vertexShaderId);
		}
		if (this.fragmentShaderId != 0) {
			glDetachShader(this.programId, this.fragmentShaderId);
		}

		// definition in res/vertex.vs
		// equivalent of `layout (location = #) ...`
		glBindAttribLocation(this.programId, 0, "position");
		glBindAttribLocation(this.programId, 1, "coords");
		glValidateProgram(this.programId);
		if (glGetProgrami(this.programId, GL_VALIDATE_STATUS) == 0) {
			System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(this.programId, 1024));
		}

	}

	public void bind() {
		glUseProgram(this.programId);
	}

	public void unbind() {
		glUseProgram(0);
	}

	public void cleanup() {
		this.unbind();
		if (this.programId != 0) {
			glDeleteProgram(this.programId);
		}
	}
}