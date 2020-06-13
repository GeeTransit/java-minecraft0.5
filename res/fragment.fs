#version 130

in vec2 outCoord;
out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform vec3 color;
uniform int useTexture;

void main()
{
	if (useTexture == 1)
	{
		fragColor = texture(texture_sampler, outCoord);
	}
	else
	{
		fragColor = vec4(color, 1);
	}
}
