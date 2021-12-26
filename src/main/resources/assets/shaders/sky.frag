#version 330 core

uniform float yOffset;
uniform int layer;
uniform float time;
in vec2 vPos;
out vec4 FragColor;

#define midDay vec3(0.72, 0.83, 1)
#define midSun vec3(0.78, 0.33, 0.21)
#define midNight vec3(0.04, 0.05, 0.08)

#define midColorCount 13
vec3 midColors[midColorCount] = vec3[midColorCount](
midSun,
midDay,
midDay,
midDay,
midDay,
midDay,
midSun,
midNight,
midNight,
midNight,
midNight,
midNight,
midSun
);


#define upDay vec3(0.45, 0.7, 1)
#define upSun vec3(0.17, 0.27, 0.38)
#define upNight vec3(0, 0, 0)

#define upColorCount 13
vec3 upColors[upColorCount] = vec3[upColorCount](
upSun,
upDay,
upDay,
upDay,
upDay,
upDay,
upSun,
upNight,
upNight,
upNight,
upNight,
upNight,
upSun
);

#define downDay vec3(0.16, 0.22, 0.72)
#define downSun vec3(0.09, 0.11, 0.33)
#define downNight vec3(0.05, 0.05, 0.12)

#define downColorCount 13
vec3 downColors[downColorCount] = vec3[downColorCount](
downSun,
downDay,
downDay,
downDay,
downDay,
downDay,
downSun,
downNight,
downNight,
downNight,
downNight,
downNight,
downSun
);

vec3 sqrtMix(vec3 a, vec3 b, float r) {
    vec3 sqrtted = vec3(a.r*a.r*(1.0-r)+b.r*b.r*(r), a.g*a.g*(1.0-r)+b.g*b.g*(r), a.b*a.b*(1.0-r)+b.b*b.b*(r));
    return sqrt(sqrtted);
}

vec3 midColor(float r) {
    int i = int(r * float(midColorCount-1));
    return sqrtMix(midColors[i], midColors[i + 1], (r - float(i)/float(midColorCount-1)) * float(midColorCount-1));
}

vec3 upColor(float r) {
    int i = int(r * float(upColorCount-1));
    return sqrtMix(upColors[i], upColors[i + 1], (r - float(i)/float(upColorCount-1)) * float(upColorCount-1));
}
vec3 downColor(float r) {
    int i = int(r * float(downColorCount-1));
    return sqrtMix(downColors[i], downColors[i + 1], (r - float(i)/float(downColorCount-1)) * float(downColorCount-1));
}

void main() {
    vec3 midColor = midColor(time);
    vec3 color;
    float multiplier = 1.4;
    if (layer == 1) {
        color = upColor(time);
        //        color = vec3(0.46, 0.71, 1);
        multiplier = 1.2;
    } else if (yOffset > -5) {
        color = downColor(time);
        multiplier = 3;
    } else {
        color = vec3(0.0, 0.0, 0.0);
    }
    FragColor = vec4(mix(color, midColor, min(1.0, multiplier * distance(vPos, vec2(0, 0)))), 1.0);
}
