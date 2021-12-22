#version 330 core

in vec4 vColor;
in vec2 texCoord;

uniform sampler2D texture0;

out vec4 FragColor;

void main() {
    vec4 color = texture(texture0, texCoord);
    if (color.a == 0) {
        discard;
    }
    FragColor = color * vColor;
//    FragColor = vec4(max(1, texture(texture0, texCoord).r), 1, 1, 1) * vColor;
}
