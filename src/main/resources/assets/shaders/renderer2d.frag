#version 330 core

in vec2 vPos;// current x, y
out vec4 FragColor;

uniform vec4 color;
uniform int drawMode;
uniform sampler2D texture0;
uniform vec4 rect;
uniform vec4 srcRect;
uniform float texWidth;
uniform float texHeight;
uniform int background;

vec2 getCoord() {
    return vec2(
    (srcRect.r + srcRect.b * vPos.x) / texWidth,
    (srcRect.g + srcRect.a * vPos.y) / texHeight
    );
}

void main() {
    vec4 destRect = rect;

    if (drawMode == 0) {
        FragColor = color;
    } else if (drawMode == 1){
        vec2 pos = getCoord();
        FragColor = texture(texture0, vec2(pos.x, 1.0F - pos.y));
    } else {
        vec2 pos = getCoord();
        pos = vec2(pos.x - 3 / texWidth, pos.y);
        float alpha = texture(texture0, pos).r;
        vec4 c = vec4(color.r, color.g, color.b, alpha);

        if (background == 1 && alpha < 0.1F) {
            vec4 c2 = texture(texture0, vec2(pos.x - 3 / texWidth, pos.y - 3 / texHeight));
            if (c2.a > 0.1F) {
                c = vec4(0.23F * c2.r, 0.23F * c2.g, 0.23F * c2.b, 0.6F * c2.a);
            }
        } else if (background == 2 && alpha < 0.1F) {
            c = vec4(0.2, 0.2, 0.2, 0.6);
        }
        FragColor = c;
    }
}