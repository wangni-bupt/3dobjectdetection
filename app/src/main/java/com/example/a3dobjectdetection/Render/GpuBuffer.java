package com.example.a3dobjectdetection.Render;

import android.opengl.GLES30;
import android.util.Log;

import java.nio.Buffer;

public class GpuBuffer {

    private static final String TAG = GpuBuffer.class.getSimpleName();

    //这些值引用相应Java数据类型的字节数。
    public static final int INT_SIZE = 4;
    public static final int FLOAT_SIZE = 4;

    private final int target;
    private final int numberOfBytesPerEntry;//每个键的字节数
    private final int[] bufferId = {0};
    private int size;
    private int capacity;

    public GpuBuffer(int target, int numberOfBytesPerEntry, Buffer entries) {
        if (entries != null) {
            if (!entries.isDirect()) { //当且仅当此缓冲区是直接缓冲区时
                throw new IllegalArgumentException("If non-null, entries buffer must be a direct buffer");
            }
            // Some GPU drivers will fail with out of memory errors if glBufferData or glBufferSubData is
            // called with a size of 0, so avoid this case.
            if (entries.limit() == 0) {
                entries = null;
            }
            //limit 指定还有多少数据需要写出(在从缓冲区写入通道时)，或者还有多少空间可以读入数据(在从通道读入缓冲区时)，
        }
        this.target = target;
        this.numberOfBytesPerEntry = numberOfBytesPerEntry;
        if (entries == null) {
            this.size = 0;
            this.capacity = 0;
        } else {
            this.size = entries.limit();
            this.capacity = entries.limit();
        }

        try {
            //清除VAO以防止意外的状态更改。
            GLES30.glBindVertexArray(0);// 绑定一个顶点数组对象
            GLError.maybeThrowGLException("Failed to unbind vertex array", "glBindVertexArray");

            GLES30.glGenBuffers(1, bufferId, 0);//生成缓冲区对象,名称存储到bufferID
            GLError.maybeThrowGLException("Failed to generate buffers", "glGenBuffers");

            GLES30.glBindBuffer(target, bufferId[0]);//绑定存储缓存区
            GLError.maybeThrowGLException("Failed to bind buffer object", "glBindBuffer");

            if (entries != null) {
                //https://blog.csdn.net/qq_24236769/article/details/77127069
                entries.rewind();//把position设为0，limit不变，一般在把数据重写入Buffer前调用。
                // 创建并初始化缓冲区对象的数据存储
                GLES30.glBufferData(
                        target, entries.limit() * numberOfBytesPerEntry, entries, GLES30.GL_DYNAMIC_DRAW);
            }
            GLError.maybeThrowGLException("Failed to populate buffer object", "glBufferData");
        } catch (Throwable t) {
            free();
            throw t;
        }
    }

    public void set(Buffer entries) {
        // Some GPU drivers will fail with out of memory errors if glBufferData or glBufferSubData is
        // called with a size of 0, so avoid this case.
        if (entries == null || entries.limit() == 0) {
            size = 0;
            return;
        }
        if (!entries.isDirect()) {
            throw new IllegalArgumentException("If non-null, entries buffer must be a direct buffer");
        }
        GLES30.glBindBuffer(target, bufferId[0]);
        GLError.maybeThrowGLException("Failed to bind vertex buffer object", "glBindBuffer");

        entries.rewind();

        if (entries.limit() <= capacity) {
            GLES30.glBufferSubData(target, 0, entries.limit() * numberOfBytesPerEntry, entries);
            GLError.maybeThrowGLException("Failed to populate vertex buffer object", "glBufferSubData");
            size = entries.limit();
        } else {
            GLES30.glBufferData(
                    target, entries.limit() * numberOfBytesPerEntry, entries, GLES30.GL_DYNAMIC_DRAW);
            GLError.maybeThrowGLException("Failed to populate vertex buffer object", "glBufferData");
            size = entries.limit();
            capacity = entries.limit();
        }
    }

    public void free() {
        if (bufferId[0] != 0) {
            GLES30.glDeleteBuffers(1, bufferId, 0);//删除缓存
            GLError.maybeLogGLError(Log.WARN, TAG, "Failed to free buffer object", "glDeleteBuffers");
            bufferId[0] = 0;
        }
    }

    public int getBufferId() {
        return bufferId[0];
    }

    public int getSize() {
        return size;
    }
}
