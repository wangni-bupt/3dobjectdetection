#version 300 es
uniform mat4 u_ModelViewProjection;
uniform float u_PointSize;
uniform mat4 u_ModelView;

layout(location = 0) in vec3 a_Position;
out vec3 v_ViewPosition;
void main() {
  v_ViewPosition = (u_ModelView * vec4(a_Position.xyz, 1.0)).xyz;
  gl_Position = u_ModelViewProjection * vec4(a_Position.xyz, 1.0);
  gl_PointSize = u_PointSize;
}