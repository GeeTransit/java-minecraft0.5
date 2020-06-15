#version 130

// bind in Shader.link
in vec3 position;  // layout(location = 0)
in vec2 coord;  // layout(location = 1)

out vec2 outCoord;

uniform mat4 projModelMatrix;

void main()
{
	gl_Position = projModelMatrix * vec4(position, 1.0);
	outCoord = coord;
}
