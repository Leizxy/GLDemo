#extension GL_OES_EGL_image_external : require
//必须 写的 固定的  意思   用采样器
//所有float类型数据的精度是lowp
precision mediump float;
varying vec2 aCoord;
//采样器  uniform static
uniform samplerExternalOES vTexture;

uniform int type;
void main() {
    if (type == 1){
        //Opengl 自带函数
        vec4 rgba = texture2D(vTexture, aCoord);
        //    灰色  滤镜
        float color=(rgba.r + rgba.g + rgba.b) / 3.0;
        vec4 tempColor=vec4(color, color, color, 1);
        gl_FragColor=tempColor;
    } else if (type == 2){
        vec4 rgba = texture2D(vTexture, aCoord);
        gl_FragColor=rgba+vec4(0.1, 0.1, 0.0, 0.0);
    } else if (type == 3){
        vec4 rgba = texture2D(vTexture, aCoord);
        gl_FragColor=rgba+vec4(0.0, 0.0, 0.3, 0.0);
    } else if (type == 4){
        vec4 rgba = texture2D(vTexture, vec2(aCoord.y, aCoord.x));
        gl_FragColor=vec4(rgba.r, rgba.g, rgba.b, rgba.a);
    } else {
        vec4 rgba = texture2D(vTexture, aCoord);
        gl_FragColor = vec4(rgba.r, rgba.g, rgba.b, rgba.a);
    }
}
