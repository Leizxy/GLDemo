attribute vec4 vPosition;
attribute vec2 vCoord;
varying vec2 aCoord;

void main() {
    aCoord = vCoord;
    gl_Position = vPosition;
}
