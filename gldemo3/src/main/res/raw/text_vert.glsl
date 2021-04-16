uniform mat4 vMatrix;
attribute vec4 vPosition;
attribute vec2 vCoord;
varying vec2 aCoord;

void main() {
    gl_Position = vMatrix * vPosition;
    aCoord = vCoord;
}
