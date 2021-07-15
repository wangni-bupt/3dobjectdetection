package com.example.a3dobjectdetection.Render;

import android.opengl.GLES30;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;

//定义如何渲染3D对象的顶点、面和其他属性的集合。
public class Mesh implements Closeable {

    private static final String TAG = Mesh.class.getSimpleName();

    private final int[] vertexArrayId = {0};
    private final PrimitiveMode primitiveMode;
    private final IndexBuffer indexBuffer;
    private final VertexBuffer[] vertexBuffers;

    //渲染的物体，点，线，面等
    public enum PrimitiveMode {
        POINTS(GLES30.GL_POINTS),
        LINE_STRIP(GLES30.GL_LINE_STRIP),
        LINE_LOOP(GLES30.GL_LINE_LOOP),
        LINES(GLES30.GL_LINES),
        TRIANGLE_STRIP(GLES30.GL_TRIANGLE_STRIP), //绘制三角形
        TRIANGLE_FAN(GLES30.GL_TRIANGLE_FAN),
        TRIANGLES(GLES30.GL_TRIANGLES);

        /* package-private */
        final int glesEnum;

        private PrimitiveMode(int glesEnum) {
            this.glesEnum = glesEnum;
        }
    }

    public Mesh(
            SampleRender render,
            PrimitiveMode primitiveMode,
            IndexBuffer indexBuffer,
            VertexBuffer[] vertexBuffers) {
        if (vertexBuffers == null || vertexBuffers.length == 0) {
            throw new IllegalArgumentException("Must pass at least one vertex buffer");
        }

        this.primitiveMode = primitiveMode;
        this.indexBuffer = indexBuffer;
        this.vertexBuffers = vertexBuffers;

        try {
            // Create vertex array
            GLES30.glGenVertexArrays(1, vertexArrayId, 0);//创建一个顶点数组
            GLError.maybeThrowGLException("Failed to generate a vertex array", "glGenVertexArrays");

            // Bind vertex array
            GLES30.glBindVertexArray(vertexArrayId[0]);//绑定顶点数组
            GLError.maybeThrowGLException("Failed to bind vertex array object", "glBindVertexArray");

            if (indexBuffer != null) {
                GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getBufferId());//绑定缓存
            }

            for (int i = 0; i < vertexBuffers.length; ++i) {
                // Bind each vertex buffer to vertex array
                GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBuffers[i].getBufferId());//绑定缓存
                GLError.maybeThrowGLException("Failed to bind vertex buffer", "glBindBuffer");
                GLES30.glVertexAttribPointer(//指定顶点i属性
                        i, vertexBuffers[i].getNumberOfEntriesPerVertex(), GLES30.GL_FLOAT, false, 0, 0);
                GLError.maybeThrowGLException(
                        "Failed to associate vertex buffer with vertex array", "glVertexAttribPointer");
                GLES30.glEnableVertexAttribArray(i);//启动顶点缓存
                GLError.maybeThrowGLException(
                        "Failed to enable vertex buffer", "glEnableVertexAttribArray");
            }
        } catch (Throwable t) {
            close();
            throw t;
        }
    }

    public void lowLevelDraw() {
        if (vertexArrayId[0] == 0) {
            throw new IllegalStateException("Tried to draw a freed Mesh");
        }

        GLES30.glBindVertexArray(vertexArrayId[0]);
        GLError.maybeThrowGLException("Failed to bind vertex array object", "glBindVertexArray");
        if (indexBuffer == null) {
            // Sanity check for debugging
            int numberOfVertices = vertexBuffers[0].getNumberOfVertices();
            for (int i = 1; i < vertexBuffers.length; ++i) {
                if (vertexBuffers[i].getNumberOfVertices() != numberOfVertices) {
                    throw new IllegalStateException("Vertex buffers have mismatching numbers of vertices");
                }
            }
            GLES30.glDrawArrays(primitiveMode.glesEnum, 0, numberOfVertices);
            GLError.maybeThrowGLException("Failed to draw vertex array object", "glDrawArrays");
        } else {
            GLES30.glDrawElements(
                    primitiveMode.glesEnum, indexBuffer.getSize(), GLES30.GL_UNSIGNED_INT, 0);
            GLError.maybeThrowGLException(
                    "Failed to draw vertex array object with indices", "glDrawElements");
        }
    }

    @Override
    public void close()  {
        if (vertexArrayId[0] != 0) {
            GLES30.glDeleteVertexArrays(1, vertexArrayId, 0);
            GLError.maybeLogGLError(
                    Log.WARN, TAG, "Failed to free vertex array object", "glDeleteVertexArrays");
        }
    }
}
