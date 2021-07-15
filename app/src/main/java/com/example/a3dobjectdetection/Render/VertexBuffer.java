package com.example.a3dobjectdetection.Render;

import android.opengl.GLES30;

import java.io.Closeable;
import java.io.IOException;
import java.nio.FloatBuffer;

//管理顶点缓存的类
public class VertexBuffer implements Closeable {

    private final GpuBuffer buffer;
    private final int numberOfEntriesPerVertex;

    //每个顶点的键数
    public VertexBuffer(SampleRender render, int numberOfEntriesPerVertex, FloatBuffer entries) {
        if (entries != null && entries.limit() % numberOfEntriesPerVertex != 0) {
            throw new IllegalArgumentException(
                    //如果非空，顶点缓冲区数据必须能被每个顶点的数据点数整除
                    "If non-null, vertex buffer data must be divisible by the number of data points per"
                            + " vertex");
        }

        this.numberOfEntriesPerVertex = numberOfEntriesPerVertex;
        //数组缓冲区对象。**标识GL_ARRAY_BUFFER指定数组缓冲区对象用于创建保存顶点数据的缓冲区对象
        buffer = new GpuBuffer(GLES30.GL_ARRAY_BUFFER, GpuBuffer.FLOAT_SIZE, entries);
    }

    //更新新数据
    public void set(FloatBuffer entries) {
        if (entries != null && entries.limit() % numberOfEntriesPerVertex != 0) {
            throw new IllegalArgumentException(
                    "If non-null, vertex buffer data must be divisible by the number of data points per"
                            + " vertex");
        }
        buffer.set(entries);
    }

    @Override
    public void close()  {
        buffer.free();
    }

    int getBufferId() {
        return buffer.getBufferId();
    }

    int getNumberOfEntriesPerVertex() {
        return numberOfEntriesPerVertex;
    }

    int getNumberOfVertices() {
        return buffer.getSize() / numberOfEntriesPerVertex;
    }
}
