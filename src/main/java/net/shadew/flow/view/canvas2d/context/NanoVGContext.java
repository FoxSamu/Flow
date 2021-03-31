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

package net.shadew.flow.view.canvas2d.context;

import org.joml.*;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.stb.STBTTVertex;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import net.shadew.flow.util.Colors;
import net.shadew.flow.util.Font;
import net.shadew.flow.util.NativeImage;
import net.shadew.util.misc.ColorUtil;

import static org.lwjgl.nanovg.NanoVG.*;

public abstract class NanoVGContext implements CanvasContext {
    private final long nvg;
    private final NVGColor fill = NVGColor.malloc();
    private final NVGColor stroke = NVGColor.malloc();
    private final NVGPaint fillPaint = NVGPaint.malloc();
    private final NVGPaint strokePaint = NVGPaint.malloc();

    private final Matrix3x2f transform = new Matrix3x2f();
    private final Matrix3x2fStack transformStack = new Matrix3x2fStack(256);

    public NanoVGContext(int flags) {
        this.nvg = createContext(flags);
    }

    @Override
    public void dispose() {
        fill.free();
        stroke.free();
        fillPaint.free();
        strokePaint.free();
        disposeContext(nvg);
    }

    protected abstract void disposeContext(long nvg);
    protected abstract long createContext(int flags);

    @Override
    public void beginFrame(float width, float height, float pixelRatio) {
        nvgBeginFrame(nvg, width, height, pixelRatio);
    }

    public void cancelFrame() {
        nvgCancelFrame(nvg);
    }

    @Override
    public void endFrame() {
        nvgEndFrame(nvg);
    }

    @Override
    public CanvasContext beginPath() {
        nvgBeginPath(nvg);
        return this;
    }

    @Override
    public CanvasContext moveTo(float x, float y) {
        nvgMoveTo(nvg, x, y);
        return this;
    }

    @Override
    public CanvasContext moveTo(Vector2fc v) {
        moveTo(v.x(), v.y());
        return this;
    }

    @Override
    public CanvasContext lineTo(float x, float y) {
        nvgLineTo(nvg, x, y);
        return this;
    }

    @Override
    public CanvasContext lineTo(Vector2fc v) {
        lineTo(v.x(), v.y());
        return this;
    }

    @Override
    public CanvasContext quadTo(float cx, float cy, float x, float y) {
        nvgQuadTo(nvg, cx, cy, x, y);
        return this;
    }

    @Override
    public CanvasContext quadTo(Vector2fc c, Vector2fc v) {
        quadTo(c.x(), c.y(), v.x(), v.y());
        return this;
    }

    @Override
    public CanvasContext curveTo(float cx1, float cy1, float cx2, float cy2, float x, float y) {
        nvgBezierTo(nvg, cx1, cy1, cx2, cy2, x, y);
        return this;
    }

    @Override
    public CanvasContext curveTo(Vector2fc c1, Vector2fc c2, Vector2fc v) {
        curveTo(c1.x(), c1.y(), c2.x(), c2.y(), v.x(), v.y());
        return this;
    }

    @Override
    public CanvasContext arcTo(float x1, float y1, float x2, float y2, float radius) {
        nvgArcTo(nvg, x1, y1, x2, y2, radius);
        return this;
    }

    @Override
    public CanvasContext arcTo(Vector2fc p1, Vector2fc p2, float radius) {
        arcTo(p1.x(), p1.y(), p2.x(), p2.y(), radius);
        return this;
    }

    @Override
    public CanvasContext closePath() {
        nvgClosePath(nvg);
        return this;
    }

    @Override
    public void fill() {
        nvgFill(nvg);
    }

    @Override
    public void stroke() {
        nvgStroke(nvg);
    }

    @Override
    public CanvasContext fillColor(int argb) {
        nvgFillColor(nvg, Colors.rgba(fill, argb));
        return this;
    }

    @Override
    public CanvasContext fillColor(float r, float g, float b, float a) {
        fillColor(ColorUtil.rgba(r, g, b, a));
        return this;
    }

    @Override
    public CanvasContext fillLinearGradient(int sargb, float sx, float sy, int eargb, float ex, float ey) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NVGColor a = NVGColor.mallocStack(stack);
            NVGColor b = NVGColor.mallocStack(stack);
            nvgLinearGradient(nvg, sx, sy, ex, ey, Colors.rgba(a, sargb), Colors.rgba(b, eargb), fillPaint);
            nvgFillPaint(nvg, fillPaint);
        }
        return this;
    }

    @Override
    public CanvasContext fillRadialGradient(float cx, float cy, int iargb, float ir, int oargb, float or) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NVGColor a = NVGColor.mallocStack(stack);
            NVGColor b = NVGColor.mallocStack(stack);
            nvgRadialGradient(nvg, cx, cy, ir, or, Colors.rgba(a, iargb), Colors.rgba(b, oargb), fillPaint);
            nvgFillPaint(nvg, fillPaint);
        }
        return this;
    }

    @Override
    public CanvasContext fillBoxGradient(float x, float y, float w, float h, float r, float f, int iargb, int oargb) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NVGColor a = NVGColor.mallocStack(stack);
            NVGColor b = NVGColor.mallocStack(stack);
            nvgBoxGradient(nvg, x, y, w, h, r, f, Colors.rgba(a, iargb), Colors.rgba(b, oargb), fillPaint);
            nvgFillPaint(nvg, fillPaint);
        }
        return this;
    }

    @Override
    public CanvasContext fillTexture(Texture texture, float ox, float oy, float w, float h, float angle, float alpha) {
        if (texture == null) {
            throw new NullPointerException();
        }

        if (!(texture instanceof TextureImpl)) {
            throw new IllegalArgumentException("Image not part of current context");
        }
        TextureImpl i = (TextureImpl) texture;
        if (i.nvg != nvg) {
            throw new IllegalArgumentException("Image not part of current context");
        }

        nvgImagePattern(nvg, ox, oy, w, h, angle, i.handle, alpha, fillPaint);
        nvgFillPaint(nvg, fillPaint);
        return this;
    }

    @Override
    public CanvasContext strokeColor(int argb) {
        nvgStrokeColor(nvg, Colors.rgba(stroke, argb));
        return this;
    }

    @Override
    public CanvasContext strokeColor(float r, float g, float b, float a) {
        nvgStrokeColor(nvg, Colors.rgba(stroke, r, g, b, a));
        return this;
    }

    @Override
    public CanvasContext strokeLinearGradient(int sargb, float sx, float sy, int eargb, float ex, float ey) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NVGColor a = NVGColor.mallocStack(stack);
            NVGColor b = NVGColor.mallocStack(stack);
            nvgLinearGradient(nvg, sx, sy, ex, ey, Colors.rgba(a, sargb), Colors.rgba(b, eargb), strokePaint);
            nvgFillPaint(nvg, strokePaint);
        }
        return this;
    }

    @Override
    public CanvasContext strokeRadialGradient(float cx, float cy, int iargb, float ir, int oargb, float or) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NVGColor a = NVGColor.mallocStack(stack);
            NVGColor b = NVGColor.mallocStack(stack);
            nvgRadialGradient(nvg, cx, cy, ir, or, Colors.rgba(a, iargb), Colors.rgba(b, oargb), strokePaint);
            nvgStrokePaint(nvg, strokePaint);
        }
        return this;
    }

    @Override
    public CanvasContext strokeBoxGradient(float x, float y, float w, float h, float r, float f, int iargb, int oargb) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            NVGColor a = NVGColor.mallocStack(stack);
            NVGColor b = NVGColor.mallocStack(stack);
            nvgBoxGradient(nvg, x, y, w, h, r, f, Colors.rgba(a, iargb), Colors.rgba(b, oargb), strokePaint);
            nvgStrokePaint(nvg, strokePaint);
        }
        return this;
    }

    @Override
    public CanvasContext strokeTexture(Texture texture, float ox, float oy, float w, float h, float angle, float alpha) {
        if (texture == null) {
            throw new NullPointerException();
        }

        if (!(texture instanceof TextureImpl)) {
            throw new IllegalArgumentException("Image not part of current context");
        }
        TextureImpl i = (TextureImpl) texture;
        if (i.nvg != nvg) {
            throw new IllegalArgumentException("Image not part of current context");
        }

        nvgImagePattern(nvg, ox, oy, w, h, angle, i.handle, alpha, strokePaint);
        nvgStrokePaint(nvg, strokePaint);
        return this;
    }

    @Override
    public CanvasContext strokeWidth(float width) {
        nvgStrokeWidth(nvg, width);
        return this;
    }

    @Override
    public Texture newTexture(NativeImage image, int flags) {
        int img = nvgCreateImageRGBA(nvg, image.getWidth(), image.getHeight(), flags, image.getBuffer());
        return new TextureImpl(img, nvg);
    }

    private static class TextureImpl implements Texture {
        private final int handle;
        private final long nvg;

        private TextureImpl(int handle, long nvg) {
            this.handle = handle;
            this.nvg = nvg;
        }

        @Override
        public void dispose() {
            nvgDeleteImage(nvg, handle);
        }
    }

    @Override
    public CanvasContext save() {
        nvgSave(nvg);
        return this;
    }

    @Override
    public CanvasContext reset() {
        nvgReset(nvg);
        return this;
    }

    @Override
    public CanvasContext restore() {
        nvgRestore(nvg);
        return this;
    }

    @Override
    public CanvasContext pushTransform() {
        getTransform(transformStack);
        transformStack.pushMatrix();
        return this;
    }

    @Override
    public CanvasContext popTransform() {
        transformStack.popMatrix();
        setTransform(transformStack);
        return this;
    }

    @Override
    public CanvasContext setTransform(float m00, float m10, float m20, float m01, float m11, float m21) {
        nvgResetTransform(nvg);
        nvgTransform(nvg, m00, m01, m10, m11, m20, m21);
        return this;
    }

    @Override
    public CanvasContext setTransform(Matrix3x2fc mat) {
        return setTransform(mat.m00(), mat.m10(), mat.m20(), mat.m01(), mat.m11(), mat.m21());
    }

    @Override
    public CanvasContext transform(float m00, float m10, float m20, float m01, float m11, float m21) {
        nvgTransform(nvg, m00, m01, m10, m11, m20, m21);
        return this;
    }

    @Override
    public CanvasContext transform(Matrix3x2fc mat) {
        return transform(mat.m00(), mat.m10(), mat.m20(), mat.m01(), mat.m11(), mat.m21());
    }

    @Override
    public CanvasContext setIdentity() {
        nvgResetTransform(nvg);
        return this;
    }

    @Override
    public CanvasContext translate(float x, float y) {
        nvgTranslate(nvg, x, y);
        return this;
    }

    @Override
    public CanvasContext translate(Vector2fc v) {
        nvgTranslate(nvg, v.x(), v.y());
        return this;
    }

    @Override
    public CanvasContext rotate(float angle) {
        nvgRotate(nvg, angle);
        return this;
    }

    @Override
    public CanvasContext rotateDegrees(float angle) {
        nvgRotate(nvg, nvgDegToRad(angle));
        return this;
    }

    @Override
    public CanvasContext scale(float s) {
        nvgScale(nvg, s, s);
        return this;
    }

    @Override
    public CanvasContext scale(float x, float y) {
        nvgScale(nvg, x, y);
        return this;
    }

    @Override
    public CanvasContext scale(Vector2fc v) {
        nvgScale(nvg, v.x(), v.y());
        return this;
    }

    @Override
    public CanvasContext mirrorX() {
        nvgScale(nvg, -1, 1);
        return this;
    }

    @Override
    public CanvasContext mirrorY() {
        nvgScale(nvg, 1, -1);
        return this;
    }

    @Override
    public CanvasContext skewX(float angle) {
        nvgSkewX(nvg, angle);
        return this;
    }

    @Override
    public CanvasContext skewXDegrees(float angle) {
        nvgSkewX(nvg, nvgDegToRad(angle));
        return this;
    }

    @Override
    public CanvasContext skewY(float angle) {
        nvgSkewY(nvg, angle);
        return this;
    }

    @Override
    public CanvasContext skewYDegrees(float angle) {
        nvgSkewY(nvg, nvgDegToRad(angle));
        return this;
    }

    @Override
    public Matrix3x2f getTransform(Matrix3x2f mat) {
        if (mat == null) {
            mat = new Matrix3x2f();
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buf = stack.mallocFloat(6);
            nvgCurrentTransform(nvg, buf);
            mat.set(buf);
        }
        return mat;
    }

    @Override
    public Matrix3x2f getTransform() {
        return getTransform(new Matrix3x2f());
    }

    @Override
    public Vector2f transformPoint(float x, float y, Vector2f out) {
        if (out == null) {
            out = new Vector2f();
        }
        Matrix3x2f t = getTransform(transform);
        out.x = x * t.m00 + y * t.m10 + t.m20;
        out.y = x * t.m01 + y * t.m11 + t.m21;
        return out;
    }

    @Override
    public Vector2f transformPoint(Vector2fc in, Vector2f out) {
        return transformPoint(in.x(), in.y(), out);
    }

    @Override
    public Vector2f transformPoint(float x, float y) {
        return transformPoint(x, y, new Vector2f());
    }

    @Override
    public Vector2f transformPoint(Vector2fc in) {
        return transformPoint(in.x(), in.y(), new Vector2f());
    }

    @Override
    public Vector2f transformPointInverse(float x, float y, Vector2f out) {
        if (out == null) {
            out = new Vector2f();
        }
        Matrix3x2f t = getTransform(transform);
        t.invert();
        out.x = x * t.m00 + y * t.m10 + t.m20;
        out.y = x * t.m01 + y * t.m11 + t.m21;
        return out;
    }

    @Override
    public Vector2f transformPointInverse(Vector2fc in, Vector2f out) {
        return transformPointInverse(in.x(), in.y(), out);
    }

    @Override
    public Vector2f transformPointInverse(float x, float y) {
        return transformPointInverse(x, y, new Vector2f());
    }

    @Override
    public Vector2f transformPointInverse(Vector2fc in) {
        return transformPointInverse(in.x(), in.y(), new Vector2f());
    }

    @Override
    public CanvasContext line(float x1, float y1, float x2, float y2) {
        moveTo(x1, y1);
        lineTo(x2, y2);
        return this;
    }

    @Override
    public CanvasContext line(Vector2fc v1, Vector2fc v2) {
        moveTo(v1);
        lineTo(v2);
        return this;
    }

    @Override
    public CanvasContext arc(float cx, float cy, float radius, float angle1, float angle2, Winding winding) {
        nvgArc(nvg, cx, cy, radius, angle1, angle2, winding == Winding.CLOCKWISE ? NVG_CW : NVG_CCW);
        return this;
    }

    @Override
    public CanvasContext arc(Vector2fc center, float radius, float angle1, float angle2, Winding winding) {
        return arc(center.x(), center.y(), radius, angle1, angle2, winding);
    }

    @Override
    public CanvasContext arcDegrees(float cx, float cy, float radius, float angle1, float angle2, Winding winding) {
        return arc(cx, cy, radius, nvgDegToRad(angle1), nvgDegToRad(angle2), winding);
    }

    @Override
    public CanvasContext arcDegrees(Vector2fc center, float radius, float angle1, float angle2, Winding winding) {
        return arcDegrees(center.x(), center.y(), radius, angle1, angle2, winding);
    }

    @Override
    public CanvasContext circle(float cx, float cy, float radius) {
        nvgCircle(nvg, cx, cy, radius);
        return this;
    }

    @Override
    public CanvasContext circle(Vector2fc center, float radius) {
        return circle(center.x(), center.y(), radius);
    }

    @Override
    public CanvasContext ellipse(float cx, float cy, float rx, float ry) {
        nvgEllipse(nvg, cx, cy, rx, ry);
        return this;
    }

    @Override
    public CanvasContext ellipse(Vector2fc center, float rx, float ry) {
        return ellipse(center.x(), center.y(), rx, ry);
    }

    @Override
    public CanvasContext ellipse(Vector2fc center, Vector2fc radius) {
        return ellipse(center.x(), center.y(), radius.x(), radius.y());
    }

    @Override
    public CanvasContext rect(float cx, float cy, float w, float h) {
        nvgRect(nvg, cx, cy, w, h);
        return this;
    }

    @Override
    public CanvasContext rect(Vector2fc pos, float w, float h) {
        return rect(pos.x(), pos.y(), w, h);
    }

    @Override
    public CanvasContext rect(Vector2fc pos, Vector2fc size) {
        return rect(pos.x(), pos.y(), size.x(), size.y());
    }

    @Override
    public CanvasContext roundedRect(float cx, float cy, float w, float h, float r) {
        nvgRoundedRect(nvg, cx, cy, w, h, r);
        return this;
    }

    @Override
    public CanvasContext roundedRect(Vector2fc pos, float w, float h, float r) {
        return roundedRect(pos.x(), pos.y(), w, h, r);
    }

    @Override
    public CanvasContext roundedRect(Vector2fc pos, Vector2fc size, float r) {
        return roundedRect(pos.x(), pos.y(), size.x(), size.y(), r);
    }

    @Override
    public CanvasContext roundedRect(float cx, float cy, float w, float h, float rtl, float rtr, float rbr, float rbl) {
        nvgRoundedRectVarying(nvg, cx, cy, w, h, rtl, rtr, rbr, rbl);
        return this;
    }

    @Override
    public CanvasContext roundedRect(Vector2fc pos, float w, float h, float rtl, float rtr, float rbr, float rbl) {
        return roundedRect(pos.x(), pos.y(), w, h, rtl, rtr, rbr, rbl);
    }

    @Override
    public CanvasContext roundedRect(Vector2fc pos, Vector2fc size, float rtl, float rtr, float rbr, float rbl) {
        return roundedRect(pos.x(), pos.y(), size.x(), size.y(), rtl, rtr, rbr, rbl);
    }

    @Override
    public CanvasContext lineCap(LineCap cap) {
        switch (cap == null ? LineCap.BUTT : cap) {
            case BUTT:
                nvgLineCap(nvg, NVG_BUTT);
                break;
            case ROUND:
                nvgLineCap(nvg, NVG_ROUND);
                break;
            case SQUARE:
                nvgLineCap(nvg, NVG_SQUARE);
                break;
        }
        return this;
    }

    @Override
    public CanvasContext lineJoin(LineJoin join) {
        switch (join == null ? LineJoin.MITER : join) {
            case MITER:
                nvgLineJoin(nvg, NVG_MITER);
                break;
            case ROUND:
                nvgLineJoin(nvg, NVG_ROUND);
                break;
            case BEVEL:
                nvgLineJoin(nvg, NVG_BEVEL);
                break;
        }
        return this;
    }

    @Override
    public CanvasContext miterLimit(float lim) {
        nvgMiterLimit(nvg, lim);
        return this;
    }

    @Override
    public CanvasContext globalAlpha(float alpha) {
        nvgGlobalAlpha(nvg, alpha);
        return this;
    }

    @Override
    public CanvasContext pathWinding(Winding winding) {
        nvgPathWinding(nvg, winding == Winding.CLOCKWISE ? NVG_CW : NVG_CCW);
        return this;
    }

    private static int toNvg(BlendFactor factor, int nullValue) {
        if (factor == null) {
            return nullValue;
        }
        switch (factor) {
            case ZERO: return NVG_ZERO;
            case ONE: return NVG_ONE;
            case SRC_COLOR: return NVG_SRC_COLOR;
            case ONE_MINUS_SRC_COLOR: return NVG_ONE_MINUS_SRC_COLOR;
            case DST_COLOR: return NVG_DST_COLOR;
            case ONE_MINUS_DST_COLOR: return NVG_ONE_MINUS_DST_COLOR;
            case SRC_ALPHA: return NVG_SRC_ALPHA;
            case ONE_MINUS_SRC_ALPHA: return NVG_ONE_MINUS_SRC_ALPHA;
            case DST_ALPHA: return NVG_DST_ALPHA;
            case ONE_MINUS_DST_ALPHA: return NVG_ONE_MINUS_DST_ALPHA;
            case SRC_ALPHA_SATURATE: return NVG_SRC_ALPHA_SATURATE;
        }
        return nullValue;
    }

    @Override
    public CanvasContext globalCompositeBlendFunc(BlendFactor src, BlendFactor dst) {
        nvgGlobalCompositeBlendFunc(nvg, toNvg(src, NVG_SRC_ALPHA), toNvg(dst, NVG_ONE_MINUS_SRC_ALPHA));
        return this;
    }

    @Override
    public CanvasContext globalCompositeBlendFunc(BlendFactor srcC, BlendFactor dstC, BlendFactor srcA, BlendFactor dstA) {
        nvgGlobalCompositeBlendFuncSeparate(
            nvg,
            toNvg(srcC, NVG_SRC_ALPHA), toNvg(dstC, NVG_ONE_MINUS_SRC_ALPHA),
            toNvg(srcA, NVG_ZERO), toNvg(dstA, NVG_ONE)
        );
        return this;
    }

    private static int toNvg(CompositeOperation factor, int nullValue) {
        if (factor == null) {
            return nullValue;
        }
        switch (factor) {
            case SRC_OVER: return NVG_SOURCE_OVER;
            case SRC_IN: return NVG_SOURCE_IN;
            case SRC_OUT: return NVG_SOURCE_OUT;
            case SRC_ATOP: return NVG_ATOP;
            case DST_OVER: return NVG_DESTINATION_OVER;
            case DST_IN: return NVG_DESTINATION_IN;
            case DST_OUT: return NVG_DESTINATION_OUT;
            case DST_ATOP: return NVG_DESTINATION_ATOP;
            case LIGHTER: return NVG_LIGHTER;
            case COPY: return NVG_COPY;
            case XOR: return NVG_XOR;
        }
        return nullValue;
    }

    @Override
    public CanvasContext globalCompositeOperation(CompositeOperation op) {
        nvgGlobalCompositeOperation(nvg, toNvg(op, NVG_SOURCE_OVER));
        return this;
    }

    private float pathGlyph(int i, float srcX, float srcY, float fontSize, Font font) {
        STBTTVertex.Buffer vertices = font.getShape(i);


        float scale = font.scaleForEMToPixels(fontSize);

        if (vertices != null) {
            try {
                boolean close = false;
                while (vertices.hasRemaining()) {
                    STBTTVertex vtx = vertices.get();

                    switch (vtx.type()) {
                        case STBTruetype.STBTT_vmove:
                            moveTo(srcX + vtx.x() * scale, srcY - vtx.y() * scale);
                            if (close) {
                                closePath();
                                pathWinding(Winding.CLOCKWISE);
                                close = false;
                            }
                            break;
                        case STBTruetype.STBTT_vline:
                            lineTo(srcX + vtx.x() * scale, srcY - vtx.y() * scale);
                            close = true;
                            break;
                        case STBTruetype.STBTT_vcurve:
                            quadTo(
                                srcX + vtx.cx() * scale, srcY - vtx.cy() * scale,
                                srcX + vtx.x() * scale, srcY - vtx.y() * scale
                            );
                            close = true;
                            break;
                        case STBTruetype.STBTT_vcubic:
                            curveTo(
                                srcX + vtx.cx() * scale, srcY - vtx.cy() * scale,
                                srcX + vtx.cx1() * scale, srcY - vtx.cy1() * scale,
                                srcX + vtx.x() * scale, srcY - vtx.y() * scale
                            );
                            close = true;
                            break;
                    }
                }
                if (close) {
                    closePath();
                    pathWinding(Winding.CLOCKWISE);
                }
            } finally {
                vertices.free();
            }
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer adv = stack.mallocInt(1);
            IntBuffer lsb = stack.mallocInt(1);

            font.getHMetrics(i, adv, lsb);
            return adv.get(0) * scale + srcX;
        }
    }

    @Override
    public CanvasContext glyph(int cp, float srcX, float srcY, float fontSize, Font font) {
        pathGlyph(font.glyphIndex(cp), srcX, srcY, fontSize, font);
        return this;
    }

    @Override
    public CanvasContext glyph(char ch, float srcX, float srcY, float fontSize, Font font) {
        return glyph((int) ch, srcX, srcY, fontSize, font);
    }

    private float curX, curY;

    @Override
    public CanvasContext text(String text, float srcX, float srcY, float fontSize, Font font) {
        String[] lns = text.split("\r\n|\r|\n");

        float hgt;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer asc = stack.mallocInt(1);
            IntBuffer desc = stack.mallocInt(1);
            IntBuffer gap = stack.mallocInt(1);
            font.getVMetrics(asc, desc, gap);

            hgt = (asc.get(0) - desc.get(0) + gap.get(0)) * font.scaleForEMToPixels(fontSize);
        }

        curY = srcY;
        for (String ln : lns) {
            curX = srcX;
            ln.codePoints().forEachOrdered(i -> curX = pathGlyph(font.glyphIndex(i), curX, curY, fontSize, font));
            curY += hgt;
        }
        return this;
    }
}
