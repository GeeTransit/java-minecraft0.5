/*
ahbejarano
2020-06-10
Shader program.
*/

package geetransit.minecraft05.engine;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
	private final int programId;

	private int vertexShaderId;
	private int fragmentShaderId;

	public ShaderProgram() throws Exception {
		this.programId = glCreateProgram();
		if (this.programId == 0) {
			throw new Exception("Could not create Shader");
		}
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