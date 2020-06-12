#version 130

// bind in Shader.link
in vec3 position;  // layout(location = 0)
in vec3 color;  // layout(location = 1)

out vec4 outPosition;
out vec3 exColor;

uniform mat4 projectionMatrix;
uniform mat4 worldMatrix;

void main()
{
	gl_Position = projectionMatrix * worldMatrix * vec4(position, 1.0);
	exColor = color;
}
