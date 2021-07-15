package com.example.a3dobjectdetection.Render;

import android.opengl.GLES30;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;

//管理帧缓存的类
public class Framebuffer implements Closeable {

    private static final String TAG = Framebuffer.class.getSimpleName();
    private final int[] framebufferId = {0};
    private final Texture colorTexture;//颜色纹理
    private final Texture depthTexture;//深度纹理
    private int width = -1;
    private int height = -1;

    public Framebuffer(SampleRender render, int width, int height) {
        try {
            colorTexture =
                    new Texture(
                            render,
                            Texture.Target.TEXTURE_2D,
                            Texture.WrapMode.CLAMP_TO_EDGE,
                            /*useMipmaps=*/ false);
            depthTexture =
                    new Texture(
                            render,
                            Texture.Target.TEXTURE_2D,
                            Texture.WrapMode.CLAMP_TO_EDGE,
                            /*useMipmaps=*/ false);

            // Set parameters of the depth texture so that it's readable by shaders.

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, depthTexture.getTextureId());//绑定纹理进行操作
            GLError.maybeThrowGLException("Failed to bind depth texture", "glBindTexture");
            //设置纹理参数，纹理过滤
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_COMPARE_MODE, GLES30.GL_NONE);
            GLError.maybeThrowGLException("Failed to set texture parameter", "glTexParameteri");
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
            GLError.maybeThrowGLException("Failed to set texture parameter", "glTexParameteri");
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
            GLError.maybeThrowGLException("Failed to set texture parameter", "glTexParameteri");

            // Set initial dimensions.设置初始维度
            resize(width, height);

            // Create framebuffer object and bind to the color and depth textures.
            //创建framebuffer对象并绑定到颜色和深度纹理。
            //n指定要生成的帧缓冲区对象名称的数量。
            //framebuffers指定存储生成的帧缓冲区对象名称的数组。
            GLES30.glGenFramebuffers(1, framebufferId, 0);
            GLError.maybeThrowGLException("Framebuffer creation failed", "glGenFramebuffers");
            //使用帧缓冲区，绑定一个命名的帧缓冲区对象
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId[0]);
            GLError.maybeThrowGLException("Failed to bind framebuffer", "glBindFramebuffer");
            //绑定颜色纹理和深度纹理
            GLES30.glFramebufferTexture2D(
                    GLES30.GL_FRAMEBUFFER,
                    GLES30.GL_COLOR_ATTACHMENT0,
                    GLES30.GL_TEXTURE_2D,
                    colorTexture.getTextureId(),
                    /*level=*/ 0);
            GLError.maybeThrowGLException(
                    "Failed to bind color texture to framebuffer", "glFramebufferTexture2D");
            GLES30.glFramebufferTexture2D(
                    GLES30.GL_FRAMEBUFFER,
                    GLES30.GL_DEPTH_ATTACHMENT,
                    GLES30.GL_TEXTURE_2D,
                    depthTexture.getTextureId(),
                    /*level=*/ 0);
            GLError.maybeThrowGLException(
                    "Failed to bind depth texture to framebuffer", "glFramebufferTexture2D");
            //返回帧缓冲区对象的帧缓冲区完整性状态
            int status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER);
            if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
                throw new IllegalStateException("Framebuffer construction not complete: code " + status);
            }
        } catch (Throwable t) {
            close();
            throw t;
        }
    }

    @Override
    public void close() {
        if (framebufferId[0] != 0) {
            GLES30.glDeleteFramebuffers(1, framebufferId, 0);//删除帧缓存
            GLError.maybeLogGLError(Log.WARN, TAG, "Failed to free framebuffer", "glDeleteFramebuffers");
            framebufferId[0] = 0;
        }
        colorTexture.close();//删除颜色和深度纹理
        depthTexture.close();
    }

    public int getFramebufferId() {
        return framebufferId[0];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    //通过给定维度更新帧缓存
    public void resize(int width, int height) {
        if (this.width == width && this.height == height) {
            return;
        }
        this.width = width;
        this.height = height;

        // Color texture 颜色纹理https://blog.csdn.net/yf0811240333/article/details/43524791/
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, colorTexture.getTextureId());//绑定颜色纹理
        GLError.maybeThrowGLException("Failed to bind color texture", "glBindTexture");
        GLES30.glTexImage2D( //指定一个二维纹理图像,纹理格式
                GLES30.GL_TEXTURE_2D,
                /*level=*/ 0,
                GLES30.GL_RGBA,
                width,
                height,
                /*border=*/ 0,
                GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,
                /*pixels=*/ null);
        //指定颜色纹理格式失败
        GLError.maybeThrowGLException("Failed to specify color texture format", "glTexImage2D");

        // Depth texture
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, depthTexture.getTextureId());
        GLError.maybeThrowGLException("Failed to bind depth texture", "glBindTexture");
        GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                /*level=*/ 0,
                GLES30.GL_DEPTH_COMPONENT32F,
                width,
                height,
                /*border=*/ 0,
                GLES30.GL_DEPTH_COMPONENT,
                GLES30.GL_FLOAT,
                /*pixels=*/ null);
        GLError.maybeThrowGLException("Failed to specify depth texture format", "glTexImage2D");
    }
}
