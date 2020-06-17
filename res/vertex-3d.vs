#version 130

// bind in Shader.link
in vec3 position;  // layout(location = 0)
in vec2 coord;  // layout(location = 1)
in mat4 modelViewInstancedMatrix;  // layout(location = 2-5)

out vec2 outCoord;

uniform mat4 projectionMatrix;
uniform mat4 modelViewMatrix;
uniform int useInstanced;

void main()
{
	mat4 mvMatrix;
    if ( useInstanced < 0 )
	{
		mvMatrix = modelViewInstancedMatrix;
	}
	else
	{
		mvMatrix = modelViewMatrix;
	}
	gl_Position = projectionMatrix * mvMatrix * vec4(position, 1.0);
	outCoord = coord;
}
