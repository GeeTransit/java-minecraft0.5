#version 130

// bind in Shader.link
in vec3 position;  // layout(location = 0)
in vec3 colour;  // layout(location = 1)

out vec4 outPosition;
out vec3 exColour;

uniform mat4 projectionMatrix;

void main()
{
	gl_Position = projectionMatrix * vec4(position, 1.0);
	exColour = colour;
}
