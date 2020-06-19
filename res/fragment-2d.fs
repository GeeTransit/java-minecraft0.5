#version 130

in vec2 outCoord;
out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform vec4 color;
uniform int isTextured;

void main() {
	if (isTextured > 0) {
		fragColor = color * texture(texture_sampler, outCoord);
	} else {
		fragColor = color;
	}
}
