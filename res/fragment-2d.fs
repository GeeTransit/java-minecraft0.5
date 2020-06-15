#version 130

in vec2 outCoord;
out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform vec4 color;

void main()
{
	fragColor = color * texture(texture_sampler, outCoord);
}
