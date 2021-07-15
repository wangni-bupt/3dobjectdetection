package com.example.a3dobjectdetection.Render;

import android.content.res.AssetManager;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SampleRender {

    private static final String TAG = SampleRender.class.getSimpleName();
    private final AssetManager assetManager;//asset管理器

    private int viewportWidth = 1; //设备上能够显示的部分
    private int viewportHeight = 1;

    public SampleRender(GLSurfaceView glSurfaceView, Renderer renderer, AssetManager assetManager) {
        this.assetManager = assetManager;
        //https://danny-jiang.blog.csdn.net/article/details/80866681?utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7Edefault-7.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromBaidu%7Edefault-7.control
        //这部分就是surfaceview的初始化过程
        //https://source.android.google.cn/devices/graphics/arch-sv-glsv
        //GLSurfaceView实际上为它自己创建一个Window，可以当做是在视图层里打穿一个洞，底层的OpenGL surface显示出来，但是，它没有动画或者变形特效。
        glSurfaceView.setPreserveEGLContextOnPause(true);//控制当GLSurfaceView暂停和恢复时EGL上下文是否被保留。
        glSurfaceView.setEGLContextClientVersion(3);//通知默认的EGLContextFactory和默认的EGLConfigChooser选择哪个EGLContext客户端版本。
        //安装一个配置选择器，它将选择一个至少具有指定的depthSize和stencilSize，以及精确指定的redSize, greenSize, blueSize和alphaSize的配置。
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        //设置与此视图关联的渲染器。
        //Surface 对象使应用能够渲染要在屏幕上显示的图像,可以理解成画布吧
        glSurfaceView.setRenderer(
                new GLSurfaceView.Renderer() {
                    @Override
                    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                        GLES30.glEnable(GLES30.GL_BLEND);//开启和关闭服务器端GL功能,混合功能，将片元颜色和颜色缓冲区的颜色进行混合，参考glBlendFunc。
                        GLError.maybeThrowGLException("Failed to enable blending", "glEnable");
                        renderer.onSurfaceCreated(SampleRender.this);
                    }

                    @Override
                    //Called when the surface changed size.
                    //Called after the surface is created and whenever the OpenGL ES surface size changes.
                    public void onSurfaceChanged(GL10 gl, int w, int h) {
                        viewportWidth = w;
                        viewportHeight = h;
                        renderer.onSurfaceChanged(SampleRender.this, w, h);
                    }

                    @Override
                    public void onDrawFrame(GL10 gl) {
                        clear(/*framebuffer=*/ null, 0f, 0f, 0f, 1f);
                        renderer.onDrawFrame(SampleRender.this);
                    }
                });
        //设置渲染模式。当renderMode为RENDERMODE_CONTINUOUSLY时，渲染器会被反复调用以重新渲染场景。
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.setWillNotDraw(false);//当一个View不需要绘制内容时，系统进行相应优化, 默认情况下：View 不启用该标记位（设置为false）；ViewGroup 默认启用（设置为true）

    }

    public void clear(Framebuffer framebuffer, float r, float g, float b, float a) {
        useFramebuffer(framebuffer);
        //为颜色缓冲区指定清除值
        GLES30.glClearColor(r, g, b, a);
        GLError.maybeThrowGLException("Failed to set clear color", "glClearColor");
        //启用或禁用写入深度缓冲区
        GLES30.glDepthMask(true);
        GLError.maybeThrowGLException("Failed to set depth write mask", "glDepthMask");
        //可以使用 | 运算符组合不同的缓冲标志位，表明需要清除的缓冲，
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        GLError.maybeThrowGLException("Failed to clear framebuffer", "glClear");
    }

    private void useFramebuffer(Framebuffer framebuffer) {
        int framebufferId;
        int viewportWidth;
        int viewportHeight;
        if (framebuffer == null) {
            framebufferId = 0;// 保留值零以表示由窗口系统提供的默认帧缓冲区
            viewportWidth = this.viewportWidth;
            viewportHeight = this.viewportHeight;
        } else {
            framebufferId = framebuffer.getFramebufferId();
            viewportWidth = framebuffer.getWidth();
            viewportHeight = framebuffer.getHeight();
        }
        //https://blog.csdn.net/flycatdeng/article/details/82664532
        //https://www.jianshu.com/p/29a7dde7d21f
        //指定帧缓冲区的使用操作类型，可以取GL_DRAW_FRAMEBUFFER, GL_READ_FRAMEBUFFER or GL_FRAMEBUFFER之一。GL_DRAW_FRAMEBUFFER对帧缓冲区进行写操作（渲染）
        //绑定帧缓存，绑定到GL_FRAMEBUFFER目标后，接下来所有的读、写帧缓冲的操作都会影响到当前绑定的帧缓冲。
        //0，绑定默认的帧缓存
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId);
        GLError.maybeThrowGLException("Failed to bind framebuffer", "glBindFramebuffer");
        //设置视口 指定视口矩形的左下角坐标，以像素为单位，初始值为（0，0）。s
        GLES30.glViewport(0, 0, viewportWidth, viewportHeight);
        GLError.maybeThrowGLException("Failed to set viewport dimensions", "glViewport");
    }

    public void draw(Mesh mesh, Shader shader) {
        draw(mesh, shader, /*framebuffer=*/ null);
    }

    /**
     * Draw a {@link Mesh} with the specified {@link Shader} to the given {@link Framebuffer}.
     *
     * <p>The {@code framebuffer} argument may be null, in which case the default framebuffer is used.
     */
    public void draw(Mesh mesh, Shader shader, Framebuffer framebuffer) {
        useFramebuffer(framebuffer);
        shader.lowLevelUse();
        mesh.lowLevelDraw();
    }

    public AssetManager getAssets() {
        return assetManager;
    }

    public static interface Renderer {
        /**
         * Called by {@link SampleRender} when the GL render surface is created.
         *
         * <p>See {@link GLSurfaceView.Renderer#onSurfaceCreated}.
         */
        public void onSurfaceCreated(SampleRender render);

        /**
         * Called by {@link SampleRender} when the GL render surface dimensions are changed.
         *
         * <p>See {@link GLSurfaceView.Renderer#onSurfaceChanged}.
         */
        public void onSurfaceChanged(SampleRender render, int width, int height);

        /**
         * Called by {@link SampleRender} when a GL frame is to be rendered.
         *
         * <p>See {@link GLSurfaceView.Renderer#onDrawFrame}.
         */
        public void onDrawFrame(SampleRender render);
    }

}
