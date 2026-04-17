#include <flutter/runtime_effect.glsl>

precision highp float;

uniform vec2 uResolution;
uniform float uTime;
uniform vec2 uCamera;

out vec4 fragColor;

float hash(vec2 p) {
  p = fract(p * vec2(127.1, 311.7));
  p += dot(p, p + 34.23);
  return fract(p.x * p.y);
}

float noise(vec2 p) {
  vec2 i = floor(p);
  vec2 f = fract(p);
  f = f * f * (3.0 - 2.0 * f);
  float a = hash(i);
  float b = hash(i + vec2(1.0, 0.0));
  float c = hash(i + vec2(0.0, 1.0));
  float d = hash(i + vec2(1.0, 1.0));
  return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
  float value = 0.0;
  float amplitude = 0.5;
  for (int i = 0; i < 5; i++) {
    value += noise(p) * amplitude;
    p *= 2.03;
    amplitude *= 0.5;
  }
  return value;
}

float lineGlow(float distanceToLine, float width) {
  return exp(-distanceToLine * distanceToLine / max(width * width, 0.0001));
}

float starField(vec2 p) {
  vec2 grid = p * vec2(165.0, 95.0);
  vec2 cell = floor(grid);
  vec2 local = fract(grid) - 0.5;
  float seed = hash(cell);
  float star = smoothstep(0.997, 1.0, seed);
  float shape = exp(-dot(local, local) * mix(60.0, 240.0, seed));
  return star * shape * mix(0.25, 1.0, hash(cell + 4.7));
}

vec3 temperature(float t) {
  vec3 deep = vec3(0.42, 0.18, 0.035);
  vec3 amber = vec3(1.0, 0.58, 0.16);
  vec3 white = vec3(1.0, 0.91, 0.72);
  vec3 blueWhite = vec3(0.78, 0.88, 1.0);
  vec3 hot = mix(deep, amber, smoothstep(0.0, 0.45, t));
  hot = mix(hot, white, smoothstep(0.35, 0.82, t));
  return mix(hot, blueWhite, smoothstep(0.82, 1.0, t));
}

float directDisk(vec2 p, float shadow, float time, out float beam) {
  float x = p.x;
  float y = p.y;
  float extent = smoothstep(1.22, 0.18, abs(x));
  float gap = smoothstep(shadow * 0.72, shadow * 1.42, abs(x));
  float turbulence = fbm(vec2(x * 8.0 + time * 0.28, y * 55.0));
  float thickness = mix(0.018, 0.052, turbulence);
  float core = lineGlow(y + 0.012 * sin(x * 11.0 + time), thickness);
  float halo = lineGlow(y, 0.11) * 0.35;
  beam = 0.58 + pow(smoothstep(-0.65, 1.0, x), 2.35) * 2.2;
  return (core * 1.4 + halo) * extent * gap * beam;
}

float upperLensedDisk(vec2 p, float shadow, float time, out float beam) {
  float x = p.x;
  float nx = clamp(x / 0.98, -1.0, 1.0);
  float curve = sqrt(max(0.0, 1.0 - nx * nx));
  float yCurve = -shadow * 0.18 - curve * shadow * 1.22;
  float span = smoothstep(1.14, 0.18, abs(x));
  float turbulence = fbm(vec2(x * 7.5 + time * 0.18, p.y * 24.0));
  float width = mix(0.026, 0.07, turbulence);
  beam = 0.52 + pow(smoothstep(-0.75, 1.0, x), 2.0) * 1.75;
  float glow = lineGlow(p.y - yCurve, width);
  float soft = lineGlow(p.y - yCurve, 0.18) * 0.28;
  return (glow + soft) * span * beam;
}

float lowerLensedDisk(vec2 p, float shadow, float time, out float beam) {
  float x = p.x;
  float nx = clamp(x / 0.88, -1.0, 1.0);
  float curve = sqrt(max(0.0, 1.0 - nx * nx));
  float yCurve = shadow * 0.18 + curve * shadow * 0.48;
  float span = smoothstep(0.96, 0.16, abs(x));
  float width = 0.028 + fbm(vec2(x * 6.0 - time * 0.1, p.y * 18.0)) * 0.035;
  beam = 0.38 + pow(smoothstep(-0.55, 1.0, x), 2.0) * 0.7;
  return lineGlow(p.y - yCurve, width) * span * beam;
}

float photonRing(vec2 p, float radius) {
  float r = length(p);
  float circle = lineGlow(r - radius, 0.01);
  float halo = lineGlow(r - radius * 1.03, 0.035) * 0.42;
  float topLift = smoothstep(-0.22, 0.28, -p.y);
  return (circle * 1.15 + halo) * (0.75 + topLift * 0.45);
}

void main() {
  vec2 fragCoord = FlutterFragCoord().xy;
  vec2 uv = (fragCoord - 0.5 * uResolution) / min(uResolution.x, uResolution.y);
  uv.y += 0.02;

  float time = uTime;
  float orbit = uCamera.x;
  float tilt = uCamera.y;
  float yaw = orbit * 0.55;
  float pitch = tilt * 0.32;
  float cy = cos(yaw);
  float sy = sin(yaw);
  float cp = cos(pitch);
  float sp = sin(pitch);
  uv = mat2(cy, -sy, sy, cy) * uv;
  uv.y = uv.y * (1.0 + abs(tilt) * 0.28) + pitch * 0.22;

  float shadow = 0.185 * (1.0 - abs(tilt) * 0.08);
  float ringRadius = shadow * 1.12;

  float r = length(uv);
  vec2 lensDir = uv / max(r, 0.001);
  float lensAmount = 0.028 / (r * r + 0.035);
  vec2 lensedUv = uv + lensDir * lensAmount;
  lensedUv.y = lensedUv.y * cp + sp * 0.12;

  vec3 color = vec3(0.0);
  float stars = starField(lensedUv + vec2(time * 0.0015, 0.0));
  color += vec3(0.55, 0.68, 1.0) * stars * smoothstep(shadow * 0.95, shadow * 2.4, r);

  float beam = 1.0;
  vec2 diskUv = vec2(uv.x, uv.y * (1.0 + tilt * 0.42));
  float upper = upperLensedDisk(diskUv, shadow, time, beam);
  color += temperature(min(1.0, upper * 0.75 + beam * 0.22)) * upper * (0.78 + tilt * 0.12);

  float lower = lowerLensedDisk(diskUv, shadow, time, beam);
  color += temperature(0.55 + beam * 0.16) * lower * (0.32 - tilt * 0.08);

  float direct = directDisk(diskUv, shadow, time, beam);
  color += temperature(min(1.0, direct * 0.35 + beam * 0.22)) * direct;

  float ring = photonRing(uv, ringRadius);
  color += vec3(1.0, 0.88, 0.62) * ring * 1.15;

  float shadowMask = smoothstep(shadow * 1.04, shadow * 0.94, r);
  color = mix(color, vec3(0.0), shadowMask);

  float innerCut = smoothstep(shadow * 1.28, shadow * 1.05, r);
  color *= 1.0 - innerCut * 0.72;
  color += vec3(1.0, 0.72, 0.32) * exp(-pow(abs(uv.y), 2.0) / 0.0018) *
      smoothstep(1.25, 0.22, abs(uv.x)) * smoothstep(shadow * 0.9, shadow * 1.45, abs(uv.x)) * 0.38;

  color = vec3(1.0) - exp(-color * 1.25);
  color = pow(color, vec3(0.86));

  fragColor = vec4(color, 1.0);
}
