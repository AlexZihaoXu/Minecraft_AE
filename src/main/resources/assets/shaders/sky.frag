#version 330 core

uniform float yOffset;
in vec2 vPos;
out vec4 FragColor;


void main() {
    vec3 midColor = vec3(0.7, 0.82, 1);
    vec3 color;
    float multiplier = 1.4;
    if (yOffset > 49) {
        color = vec3(0.46, 0.71, 1);
        multiplier = 1.2;
    } else if (yOffset > -5) {
        color = vec3(0.18, 0.24, 0.73);
        multiplier = 3;
    } else {
        color = vec3(0.0, 0.0, 0.0);
    }
    FragColor = vec4(mix(color, midColor, min(1.0, multiplier * distance(vPos, vec2(0, 0))) ), 1.0);
}
