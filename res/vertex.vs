#version 130

// bind in ShaderProgram.link
in vec3 position;  // layout(location = 0)
in vec3 colour;  // layout(location = 1)

out vec4 outPosition;
out vec3 exColour;

void main()
{
	gl_Position = vec4(position, 1.0);
	exColour = colour;
}
