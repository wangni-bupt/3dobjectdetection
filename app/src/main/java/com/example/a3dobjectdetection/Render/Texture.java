package com.example.a3dobjectdetection.Render;

import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;

//管理纹理的类
public class Texture implements Closeable {

    private static final String TAG = Texture.class.getSimpleName();
    private final Target target;//目标纹理
    private final int[] textureId = {0};



    //纹理边界的渲染方式，超出边界的情况https://mengqd.blog.csdn.net/article/details/104085587
    public enum WrapMode {
        CLAMP_TO_EDGE(GLES30.GL_CLAMP_TO_EDGE),
        MIRRORED_REPEAT(GLES30.GL_MIRRORED_REPEAT),
        REPEAT(GLES30.GL_REPEAT);

        /* package-private */
        final int glesEnum;

        private WrapMode(int glesEnum) {
            this.glesEnum = glesEnum;
        }
    }

    //目标纹理格式 GL_TEXTURE_2D 2D纹理
    public enum Target {
        TEXTURE_2D(GLES30.GL_TEXTURE_2D),
        TEXTURE_EXTERNAL_OES(GLES11Ext.GL_TEXTURE_EXTERNAL_OES),
        TEXTURE_CUBE_MAP(GLES30.GL_TEXTURE_CUBE_MAP);

        final int glesEnum;

        private Target(int glesEnum) {
            this.glesEnum = glesEnum;
        }
    }

    //纹理颜色格式
    public enum ColorFormat {
        LINEAR(GLES30.GL_RGBA8),
        SRGB(GLES30.GL_SRGB8_ALPHA8);

        final int glesEnum;

        private ColorFormat(int glesEnum) {
            this.glesEnum = glesEnum;
        }
    }


    public Texture(SampleRender render, Target target, WrapMode wrapMode) {
        this(render, target, wrapMode, /*useMipmaps=*/ true);
    }
    //useMipmaps纹理映射，纹理构造器
    public Texture(SampleRender render, Target target, WrapMode wrapMode, boolean useMipmaps) {
        this.target = target;

        GLES30.glGenTextures(1, textureId, 0);//产生一个纹理
        GLError.maybeThrowGLException("Texture creation failed", "glGenTextures");
        //提高渲染的性能以及提升场景的视觉质量,选定纹理过滤方式
        int minFilter = useMipmaps ? GLES30.GL_LINEAR_MIPMAP_LINEAR : GLES30.GL_LINEAR;

        try {
            GLES30.glBindTexture(target.glesEnum, textureId[0]);//绑定纹理
            GLError.maybeThrowGLException("Failed to bind texture", "glBindTexture");
            //设置纹理参数，纹理过滤
            GLES30.glTexParameteri(target.glesEnum, GLES30.GL_TEXTURE_MIN_FILTER, minFilter);
            GLError.maybeThrowGLException("Failed to set texture parameter", "glTexParameteri");
            GLES30.glTexParameteri(target.glesEnum, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLError.maybeThrowGLException("Failed to set texture parameter", "glTexParameteri");

            GLES30.glTexParameteri(target.glesEnum, GLES30.GL_TEXTURE_WRAP_S, wrapMode.glesEnum);
            GLError.maybeThrowGLException("Failed to set texture parameter", "glTexParameteri");
            GLES30.glTexParameteri(target.glesEnum, GLES30.GL_TEXTURE_WRAP_T, wrapMode.glesEnum);
            GLError.maybeThrowGLException("Failed to set texture parameter", "glTexParameteri");
        } catch (Throwable t) {
            close();
            throw t;
        }
    }
    @Override
    public void close()  {
        if (textureId[0] != 0) {
            GLES30.glDeleteTextures(1, textureId, 0);//删除纹理
            GLError.maybeLogGLError(Log.WARN, TAG, "Failed to free texture", "glDeleteTextures");
            textureId[0] = 0;
        }
    }

    public int getTextureId() {
        return textureId[0];
    }

    Target getTarget() {
        return target;
    }
}
