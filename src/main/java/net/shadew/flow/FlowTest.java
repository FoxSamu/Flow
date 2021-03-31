/*
 * Copyright 2021 Shadew
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shadew.flow;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;
import org.joml.Vector2f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import net.shadew.flow.tree.Scene;
import net.shadew.flow.util.Colors;
import net.shadew.flow.util.Font;
import net.shadew.flow.util.NativeImage;
import net.shadew.flow.util.Path;
import net.shadew.flow.util.fbo.Framebuffer;
import net.shadew.flow.util.fbo.MsaaRenderbuffer;
import net.shadew.flow.util.fbo.Renderbuffer;
import net.shadew.flow.util.window.Monitor;
import net.shadew.flow.util.window.Window;
import net.shadew.util.misc.MathUtil;

import static org.jcodec.common.Codec.H264;
import static org.jcodec.common.Format.*;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.opengl.GL13.*;

public class FlowTest {
    private final Scene scene;
    private Window window;
    private Font font;
    private long nvg;

    private SequenceEncoder encoder;
    private File tmp = new File("rendered");
    private Framebuffer fbo;
    private MsaaRenderbuffer rbo;
    private MsaaRenderbuffer drbo;
    private MsaaRenderbuffer srbo;

    private Framebuffer outfbo;
    private Renderbuffer outrbo;

    private FlowTest(Scene scene) {
        this.scene = scene;
        init();
    }

    private void init() {
        Window.initGLFW();
        window = new Window(960, 540);
        window.setDecorated(true);
        window.center(Monitor.getPrimary());
        window.grabContext();
        window.show();
        GL.createCapabilities();
        Window.swapInterval(1);

        try {
            font = Font.load(new FileInputStream("test_fonts/JetBrainsMono-Regular.ttf"));

            new File("rendered").mkdirs();
            encoder = new SequenceEncoder(NIOUtils.writableChannel(new File("rendered/out.mov")), Rational.R(60, 1), MOV, H264, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        font.init();

        nvg = NanoVGGL3.nvgCreate(0);

        fbo = new Framebuffer();
        fbo.setSize(1920, 1080);

        rbo = new MsaaRenderbuffer(GL32.GL_FRAMEBUFFER, GL_RGBA, 16);
        drbo = new MsaaRenderbuffer(GL32.GL_FRAMEBUFFER, GL_DEPTH_COMPONENT, 16);
        srbo = new MsaaRenderbuffer(GL32.GL_FRAMEBUFFER, GL_STENCIL_INDEX, 16);

        fbo.addAttachment(GL32.GL_COLOR_ATTACHMENT0, rbo);
        fbo.addAttachment(GL32.GL_DEPTH_ATTACHMENT, drbo);
        fbo.addAttachment(GL32.GL_STENCIL_ATTACHMENT, srbo);

        outfbo = new Framebuffer();
        outfbo.setSize(1920, 1080);

        outrbo = new Renderbuffer(GL32.GL_FRAMEBUFFER, GL_RGBA);

        outfbo.addAttachment(GL32.GL_COLOR_ATTACHMENT0, outrbo);
    }

    private void destroy() {
        rbo.dispose();
        drbo.dispose();
        srbo.dispose();
        fbo.dispose();
        outrbo.dispose();
        outfbo.dispose();
        NanoVGGL3.nvgDelete(nvg);
        font.dispose();
        try {
            encoder.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
        window.close();
        window.dispose();
        Window.terminateGLFW();
    }

    private void loop() {
        while (!window.shouldClose()) {
            float rmx = (float) window.getMouseX(), rmy = (float) window.getMouseY();
            int ww = window.getViewportWidth(), wh = window.getViewportHeight();
            int vw = 1920, vh = 1080;
            render(ww, wh, vw, vh, rmx / ww * vw, rmy / wh * vh);

            if (frame == 85) {
                fbo.bind();

                Framebuffer.useDrawBuffers(GL32.GL_COLOR_ATTACHMENT0, GL32.GL_DEPTH_ATTACHMENT, GL32.GL_STENCIL_ATTACHMENT);

                render(fbo.getViewportWidth(), fbo.getViewportHeight(), vw, vh, rmx / ww * vw, rmy / wh * vh);
                fbo.unbind();

                fbo.bindRead();
                outfbo.bindDraw();

                GL32.glBlitFramebuffer(
                    0, 0, fbo.getViewportWidth(), fbo.getViewportHeight(),
                    0, 0, outfbo.getViewportWidth(), outfbo.getViewportHeight(),
                    GL_COLOR_BUFFER_BIT, GL_LINEAR
                );

                fbo.unbindRead();
                outfbo.unbindDraw();

                outfbo.bind();

                NativeImage image = NativeImage.allocate(fbo.getViewportWidth(), fbo.getViewportHeight());
                glReadBuffer(GL32.GL_COLOR_ATTACHMENT0);
                glReadPixels(0, 0, fbo.getViewportWidth(), fbo.getViewportHeight(), GL_RGBA, GL_UNSIGNED_BYTE, image.getInitializedBuffer());

                image.savePng("rendered/out.png", true);
                image.dispose();

                outfbo.unbind();
            }

            frame++;

            window.swapBuffers();
            Window.pollEvents();
        }
    }

    private long frame = 0;

    private void render(int w, int h, float vw, float vh, float mx, float my) {
        GL11.glViewport(0, 0, w, h);
        GL11.glClearColor(0, 0, 0, 1);
        GL11.glEnable(GL_MULTISAMPLE);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
        nvgBeginFrame(nvg, vw, vh, 1);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer lg_b = stack.mallocInt(1);
            IntBuffer asc_b = stack.mallocInt(1);
            IntBuffer desc_b = stack.mallocInt(1);

            float scale = font.scaleForEMToPixels(180);

            font.getVMetrics(asc_b, desc_b, lg_b);
            int asc_i = asc_b.get(0);
            int desc_i = desc_b.get(0);
            int hgt = asc_i;
            float asc = asc_i * scale;
            float desc = desc_i * scale;
            float lg = lg_b.get(0) * scale;

            nvgSave(nvg);
            nvgTranslate(nvg, vw / 2f, vh / 2f);
            nvgScale(nvg, 1f, -1f);

            NVGColor color = NVGColor.mallocStack(stack);
            Colors.rgba(color, 0xFFFFFFFF);

            nvgStrokeColor(nvg, color);
            nvgFillColor(nvg, color);
            nvgStrokeWidth(nvg, 3);
            nvgLineJoin(nvg, NVG_ROUND);

            Path path = new Path();


//            float x = +300;
//
//            String txt = "42-69";
//
//            IntBuffer adv = stack.mallocInt(1);
//            IntBuffer lsb = stack.mallocInt(1);
//
//            for (char ch : txt.toCharArray()) {
//                //nvgBeginPath(nvg);
//                path.clear();
//
//                int glyph = font.glyphIndex(ch);
//                STBTTVertex.Buffer buf = font.getShape(glyph);
//
//                boolean close = false;
//                while (buf.hasRemaining()) {
//                    STBTTVertex vertex = buf.get();
//                    switch (vertex.type()) {
//                        case STBTruetype.STBTT_vmove:
//                            if (close) {
//                                //nvgClosePath(nvg);
//                                //nvgPathWinding(nvg, NVG_HOLE);
//                                //path.close();
//                                close = false;
//                            }
//                            //nvgMoveTo(nvg, vertex.x() * scale - x, vertex.y() * scale);
//                            path.moveTo(vertex.x() * scale - x, vertex.y() * scale);
//                            break;
//                        case STBTruetype.STBTT_vline:
//                            //nvgLineTo(nvg, vertex.x() * scale - x, vertex.y() * scale);
//                            path.lineTo(vertex.x() * scale - x, vertex.y() * scale);
//                            close = true;
//                            break;
//                        case STBTruetype.STBTT_vcurve:
//                            //nvgQuadTo(nvg, vertex.cx() * scale - x, vertex.cy() * scale, vertex.x() * scale - x, vertex.y() * scale);
//                            path.quadTo(vertex.cx() * scale - x, vertex.cy() * scale, vertex.x() * scale - x, vertex.y() * scale, 20);
//                            close = true;
//                            break;
//                        case STBTruetype.STBTT_vcubic:
//                            //nvgBezierTo(nvg, vertex.cx() * scale - x, vertex.cy() * scale, vertex.cx1() * scale - x, vertex.cy1() * scale, vertex.x() * scale - x, vertex.y() * scale);
//                            path.curveTo(vertex.cx() * scale - x, vertex.cy() * scale, vertex.cx1() * scale - x, vertex.cy1() * scale, vertex.x() * scale - x, vertex.y() * scale, 20);
//                            close = true;
//                            break;
//                    }
//                }
//                if (close) {
//                    //nvgClosePath(nvg);
//                    //nvgPathWinding(nvg, NVG_HOLE);
//                    //path.close();
//                }
//                buf.free();
//
//                font.getHMetrics(glyph, adv, lsb);
//                x -= adv.get(0) * scale;
//
//
//                path.fillColor = 0xFF555555;
//                path.strokeColor = 0xFFFFFFFF;
//                path.strokeWidth = 3;
//
//                path.draw(nvg, MathUtil.clamp(frame / 240f % 2 - 1, 0, 1), MathUtil.clamp(frame / 240f % 2, 0, 1));
//                //path.draw(nvg, 0, 1);
//                //NanoVG.nvgFill(nvg);
//            }


//            IntBuffer x1_b = stack.mallocInt(1);
//            IntBuffer y1_b = stack.mallocInt(1);
//            IntBuffer x2_b = stack.mallocInt(1);
//            IntBuffer y2_b = stack.mallocInt(1);
//
//            font.getBox(glyph, x1_b, y1_b, x2_b, y2_b);
//            float x1 = x1_b.get(0) * scale;
//            float y1 = y1_b.get(0) * scale;
//            float x2 = x2_b.get(0) * scale;
//            float y2 = y2_b.get(0) * scale;
//
//            Colors.rgba(color, 0xFFFFFF00);
//
//            NanoVG.nvgStrokeColor(nvg, color);
//            NanoVG.nvgFillColor(nvg, color);
//            NanoVG.nvgStrokeWidth(nvg, 3);
//
//            NanoVG.nvgBeginPath(nvg);
//            NanoVG.nvgRect(nvg, x1, y1, x2 - x1, y2 - y1);
//
//            NanoVG.nvgStroke(nvg);
//
//            IntBuffer adv_b = stack.mallocInt(1);
//            IntBuffer left_b = stack.mallocInt(1);
//
//            font.getHMetrics(glyph, adv_b, left_b);
//            float adv = adv_b.get(0) * scale;
//            float left = left_b.get(0) * scale;
//
//            Colors.rgba(color, 0xFF00FFFF);
//
//            NanoVG.nvgStrokeColor(nvg, color);
//            NanoVG.nvgFillColor(nvg, color);
//            NanoVG.nvgStrokeWidth(nvg, 3);
//
//            NanoVG.nvgBeginPath(nvg);
//            NanoVG.nvgMoveTo(nvg, 0, desc);
//            NanoVG.nvgLineTo(nvg, 0, asc);
//            NanoVG.nvgMoveTo(nvg, left, desc);
//            NanoVG.nvgLineTo(nvg, left, asc);
//            NanoVG.nvgMoveTo(nvg, adv, desc);
//            NanoVG.nvgLineTo(nvg, adv, asc);
//
//            NanoVG.nvgStroke(nvg);
//
//            Colors.rgba(color, 0xFFFF00FF);
//
//            NanoVG.nvgStrokeColor(nvg, color);
//            NanoVG.nvgFillColor(nvg, color);
//            NanoVG.nvgStrokeWidth(nvg, 3);
//
//            NanoVG.nvgBeginPath(nvg);
//            NanoVG.nvgMoveTo(nvg, 0, desc);
//            NanoVG.nvgLineTo(nvg, adv, desc);
//            NanoVG.nvgMoveTo(nvg, 0, asc);
//            NanoVG.nvgLineTo(nvg, adv, asc);
//            NanoVG.nvgMoveTo(nvg, 0, desc - lg);
//            NanoVG.nvgLineTo(nvg, adv, desc - lg);
//            NanoVG.nvgMoveTo(nvg, 0, 0);
//            NanoVG.nvgLineTo(nvg, adv, 0);
//
//            NanoVG.nvgStroke(nvg);

            float ax = -200, ay = 0;
            float bx = mx - vw / 2f, by = -(my - vh / 2f);
            float cx = 0, cy = -200;
            float dx = 200, dy = 0;

            nvgBeginPath(nvg);
            nvgMoveTo(nvg, ax, ay);
            nvgLineTo(nvg, bx, by);
            nvgLineTo(nvg, cx, cy);
            nvgLineTo(nvg, dx, dy);

            nvgStrokeColor(nvg, Colors.rgba(color, 0xFF0000FF));
            nvgStroke(nvg);

            nvgBeginPath(nvg);
            nvgMoveTo(nvg, ax, ay);
            nvgBezierTo(nvg, bx, by, cx, cy, dx, dy);

            nvgStrokeColor(nvg, Colors.rgba(color, 0xFFFFFF00));
            nvgStroke(nvg);

            Vector2f exX = new Vector2f();
            Vector2f exY = new Vector2f();
            boolean xe = findExtremes(ax, bx, cx, dx, exX);
            boolean ye = findExtremes(ay, by, cy, dy, exY);

//            nvgBeginPath(nvg);
//
//            int seg = 10;
//            for (int i = 0; i <= seg; i++) {
//                float t = (float) i / seg;
//
//
//                float tx = cubicBezier(ax, bx, cx, dx, t);
//                float ty = cubicBezier(ay, by, cy, dy, t);
//                float dtx = cubicBezierDerivative(ax, bx, cx, dx, t) / seg;
//                float dty = cubicBezierDerivative(ay, by, cy, dy, t) / seg;
//
//                nvgMoveTo(nvg, tx - dtx, ty - dty);
//                nvgLineTo(nvg, tx + dtx, ty + dty);
//            }
//
//            nvgStrokeColor(nvg, Colors.rgba(color, 0xFF00FFFF));
//            nvgStroke(nvg);

            List<Vector2f> extremes = new ArrayList<>();

            if (xe) {
                float x1 = exX.x;
                float x2 = exX.y;

                if (x1 >= 0 && x1 <= 1) {
                    Vector2f extreme1 = new Vector2f(
                        cubicBezier(ax, bx, cx, dx, x1),
                        cubicBezier(ay, by, cy, dy, x1)
                    );
                    extremes.add(extreme1);
                }
                if (x2 >= 0 && x2 <= 1) {
                    Vector2f extreme2 = new Vector2f(
                        cubicBezier(ax, bx, cx, dx, x2),
                        cubicBezier(ay, by, cy, dy, x2)
                    );
                    extremes.add(extreme2);
                }
            }
            if (ye) {
                float x1 = exY.x;
                float x2 = exY.y;

                if (x1 >= 0 && x1 <= 1) {
                    Vector2f extreme1 = new Vector2f(
                        cubicBezier(ax, bx, cx, dx, x1),
                        cubicBezier(ay, by, cy, dy, x1)
                    );
                    extremes.add(extreme1);
                }
                if (x2 >= 0 && x2 <= 1) {
                    Vector2f extreme2 = new Vector2f(
                        cubicBezier(ax, bx, cx, dx, x2),
                        cubicBezier(ay, by, cy, dy, x2)
                    );
                    extremes.add(extreme2);
                }
            }

            extremes.add(new Vector2f(ax, ay));
            extremes.add(new Vector2f(dx, dy));

            float nx = Float.POSITIVE_INFINITY;
            float ny = Float.POSITIVE_INFINITY;
            float px = Float.NEGATIVE_INFINITY;
            float py = Float.NEGATIVE_INFINITY;

            for (Vector2f ex : extremes) {
                nx = Math.min(ex.x, nx);
                ny = Math.min(ex.y, ny);
                px = Math.max(ex.x, px);
                py = Math.max(ex.y, py);
            }

            nvgBeginPath(nvg);
            nvgRect(nvg, nx, ny, px - nx, py - ny);

            nvgStrokeColor(nvg, Colors.rgba(color, 0xFFFF00FF));
            nvgStroke(nvg);

            for (Vector2f ex : extremes) {
                nvgBeginPath(nvg);
                nvgCircle(nvg, ex.x, ex.y, 3);

                nvgFillColor(nvg, Colors.rgba(color, 0xFFFFFFFF));
                nvgFill(nvg);
            }


            List<Vector2f> evenSpaced = new ArrayList<>();

            int seg = 10;

            for (int i = 0; i < seg; i++) {
                float t = (float) i / seg;
                float bzx = cubicBezier(ax, bx, cx, dx, t);
                float bzy = cubicBezier(ay, by, cy, dy, t);

                evenSpaced.add(new Vector2f(bzx, bzy));
            }

            for (Vector2f ex : evenSpaced) {
                nvgBeginPath(nvg);
                nvgCircle(nvg, ex.x, ex.y, 3);

                nvgFillColor(nvg, Colors.rgba(color, 0xFF00FF00));
                nvgFill(nvg);
            }


            nvgRestore(nvg);
        }

        nvgEndFrame(nvg);

//        ByteBuffer rgb = MemoryUtil.memAlloc(window.getViewportWidth() * window.getViewportHeight() * 3);
//        glReadPixels(0, 0, window.getViewportWidth(), window.getViewportHeight(), GL_RGB, GL_UNSIGNED_BYTE, rgb);
//        rgb.position(0).limit(rgb.capacity());
//
//        tmp.mkdirs();
//
//        byte[] rgbs = new byte[window.getViewportWidth() * window.getViewportHeight() * 3];
//        int h = window.getViewportHeight();
//        int w = window.getViewportWidth() * 3;
//        for (int i = 0; i < h; i ++) {
//            int d = (h - i - 1) * w;
//            int d2 = d + w;
//            rgb.get(rgbs, d, w);
//
//            for (int j = d; j < d2; j ++) {
//                int b = rgbs[j] & 0xff;
//                rgbs[j] = (byte) (b - 128);
//            }
//        }
//
//        Picture pic = Picture.createPicture(window.getViewportWidth(), window.getViewportHeight(), new byte[][] { rgbs }, ColorSpace.RGB);
//
//        try {
//            encoder.encodeNativeFrame(pic);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        MemoryUtil.memFree(rgb);
//        STBImageWrite.stbi_flip_vertically_on_write(true);
//        STBImageWrite.stbi_write_png(new File(tmp, "f_" + frame + ".png").toString(), window.getViewportWidth(), window.getViewportHeight(), 3, buf, 0);
    }

    public static void main(String[] args) {
        FlowTest flow = new FlowTest(null);
        flow.loop();
        flow.destroy();
    }

    private static boolean findExtremes(float e, float f, float g, float h, Vector2f out) {
        float u = f - e;
        float v = g - f;
        float w = h - g;

        /*
         * B'(t) = at^2 + bt + c
         *
         * a = u - 2v + w
         * b = 2(v - u)
         * c = u
         */

        float a = u - 2 * v + w;
        float b = 2 * (v - u);
        float c = u;

        // discriminator: d = b^2 - 4ac
        float d = b * b - 4 * a * c;
        if (d < 0) return false;
        float sd = (float) Math.sqrt(d);

        float q = 2 * a;
        float x1 = (-b - sd) / q;
        float x2 = (-b + sd) / q;

        out.set(x1, x2);
        return true;
    }

    private static float quadBezier(float a, float c, float b, float t) {
        return MathUtil.lerp(MathUtil.lerp(a, c, t), MathUtil.lerp(c, b, t), t);
    }

    private static float cubicBezier(float a, float c1, float c2, float b, float t) {
        return quadBezier(MathUtil.lerp(a, c1, t), MathUtil.lerp(c1, c2, t), MathUtil.lerp(c2, b, t), t);
    }

    private static float cubicBezierDerivative(float a, float b, float c, float d, float t) {
        float nt = 1 - t;
        return 3 * nt * nt * (b - a) + 6 * nt * t * (c - b) + 3 * t * t * (d - c);
    }
}
