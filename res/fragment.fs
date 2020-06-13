#version 130

in vec2 outCoord;
out vec4 fragColor;

uniform sampler2D texture_sampler;

void main()
{
	fragColor = texture(texture_sampler, outCoord);
}
