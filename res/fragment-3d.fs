#version 130

in vec2 outCoord;
out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform vec4 color;
uniform int isTextured;
uniform int isSelected;

void main()
{
	if (isTextured > 0)
	{
		fragColor = texture(texture_sampler, outCoord);
	}
	else
	{
		fragColor = color;
	}
	if (isSelected > 0)
	{
		fragColor = vec4(fragColor.x, fragColor.y, 11, 1);
	}
}
