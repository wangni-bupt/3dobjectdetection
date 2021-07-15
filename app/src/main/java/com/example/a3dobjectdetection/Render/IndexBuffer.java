package com.example.a3dobjectdetection.Render;

import android.opengl.GLES30;

import java.io.Closeable;
import java.io.IOException;
import java.nio.IntBuffer;
//顶点索引列表
public class IndexBuffer implements Closeable {

    private final GpuBuffer buffer;

    public IndexBuffer(SampleRender render, IntBuffer entries) {
        buffer = new GpuBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, GpuBuffer.INT_SIZE, entries);
    }
    @Override
    public void close()  {
        buffer.free();
    }

    int getBufferId() {
        return buffer.getBufferId();
    }

    int getSize() {
        return buffer.getSize();
    }
}
