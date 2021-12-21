#version 330 core

in vec4 vColor;
in vec2 texCoord;

uniform sampler2D texture0;

out vec4 FragColor;

void main() {
//    FragColor = texture(texture0, texCoord) * max(1, vColor.r);
    FragColor = vec4(max(1, texture(texture0, texCoord).r), 1, 1, 1) * vColor;
}
