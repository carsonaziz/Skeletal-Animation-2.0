#version 330 core

in vec2 outTextCoord;

out vec4 FragColour;

uniform sampler2D meshTexture;

void main() {
	FragColour = texture(meshTexture, outTextCoord);
}