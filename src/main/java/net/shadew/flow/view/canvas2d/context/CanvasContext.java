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

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import net.shadew.flow.util.Disposable;
import net.shadew.flow.util.Font;
import net.shadew.flow.util.NativeImage;

public interface CanvasContext extends Disposable {
    void beginFrame(float width, float height, float pixelRatio);
    void endFrame();

    CanvasContext beginPath();
    CanvasContext moveTo(float x, float y);
    CanvasContext moveTo(Vector2fc v);
    CanvasContext lineTo(float x, float y);
    CanvasContext lineTo(Vector2fc v);
    CanvasContext quadTo(float cx, float cy, float x, float y);
    CanvasContext quadTo(Vector2fc c, Vector2fc v);
    CanvasContext curveTo(float cx1, float cy1, float cx2, float cy2, float x, float y);
    CanvasContext curveTo(Vector2fc c1, Vector2fc c2, Vector2fc v);
    CanvasContext arcTo(float x1, float y1, float x2, float y2, float radius);
    CanvasContext arcTo(Vector2fc p1, Vector2fc p2, float radius);
    CanvasContext closePath();

    void fill();
    void stroke();

    CanvasContext fillColor(int argb);
    CanvasContext fillColor(float r, float g, float b, float a);
    CanvasContext fillLinearGradient(int sargb, float sx, float sy, int eargb, float ex, float ey);
    CanvasContext fillRadialGradient(float cx, float cy, int iargb, float ir, int oargb, float or);
    CanvasContext fillBoxGradient(float x, float y, float w, float h, float r, float f, int iargb, int oargb);
    CanvasContext fillTexture(Texture texture, float ox, float oy, float w, float h, float angle, float alpha);

    CanvasContext strokeColor(int argb);
    CanvasContext strokeColor(float r, float g, float b, float a);
    CanvasContext strokeLinearGradient(int sargb, float sx, float sy, int eargb, float ex, float ey);
    CanvasContext strokeRadialGradient(float cx, float cy, int iargb, float ir, int oargb, float or);
    CanvasContext strokeBoxGradient(float x, float y, float w, float h, float r, float f, int iargb, int oargb);
    CanvasContext strokeTexture(Texture texture, float ox, float oy, float w, float h, float angle, float alpha);

    CanvasContext strokeWidth(float width);

    CanvasContext lineCap(LineCap cap);
    CanvasContext lineJoin(LineJoin join);
    CanvasContext miterLimit(float lim);
    CanvasContext pathWinding(Winding winding);

    CanvasContext globalAlpha(float alpha);
    CanvasContext globalCompositeBlendFunc(BlendFactor src, BlendFactor dst);
    CanvasContext globalCompositeBlendFunc(BlendFactor srcC, BlendFactor dstC, BlendFactor srcA, BlendFactor dstA);
    CanvasContext globalCompositeOperation(CompositeOperation op);

    Texture newTexture(NativeImage image, int flags);

    CanvasContext save();
    CanvasContext restore();
    CanvasContext reset();

    CanvasContext pushTransform();
    CanvasContext popTransform();
    CanvasContext setIdentity();
    CanvasContext setTransform(float m00, float m10, float m20, float m01, float m11, float m21);
    CanvasContext setTransform(Matrix3x2fc mat);
    CanvasContext transform(float m00, float m10, float m20, float m01, float m11, float m21);
    CanvasContext transform(Matrix3x2fc mat);
    CanvasContext translate(float x, float y);
    CanvasContext translate(Vector2fc v);
    CanvasContext rotate(float angle);
    CanvasContext rotateDegrees(float angle);
    CanvasContext scale(float s);
    CanvasContext scale(float x, float y);
    CanvasContext scale(Vector2fc v);
    CanvasContext mirrorX();
    CanvasContext mirrorY();
    CanvasContext skewX(float angle);
    CanvasContext skewXDegrees(float angle);
    CanvasContext skewY(float angle);
    CanvasContext skewYDegrees(float angle);
    Matrix3x2f getTransform();
    Matrix3x2f getTransform(Matrix3x2f mat);
    Vector2f transformPoint(float x, float y);
    Vector2f transformPoint(float x, float y, Vector2f out);
    Vector2f transformPoint(Vector2fc in);
    Vector2f transformPoint(Vector2fc in, Vector2f out);
    Vector2f transformPointInverse(float x, float y);
    Vector2f transformPointInverse(float x, float y, Vector2f out);
    Vector2f transformPointInverse(Vector2fc in);
    Vector2f transformPointInverse(Vector2fc in, Vector2f out);

    CanvasContext line(float x1, float y1, float x2, float y2);
    CanvasContext line(Vector2fc v1, Vector2fc v2);
    CanvasContext arc(float cx, float cy, float radius, float angle1, float angle2, Winding winding);
    CanvasContext arc(Vector2fc center, float radius, float angle1, float angle2, Winding winding);
    CanvasContext arcDegrees(float cx, float cy, float radius, float angle1, float angle2, Winding winding);
    CanvasContext arcDegrees(Vector2fc center, float radius, float angle1, float angle2, Winding winding);
    CanvasContext circle(float cx, float cy, float radius);
    CanvasContext circle(Vector2fc center, float radius);
    CanvasContext ellipse(float cx, float cy, float rx, float ry);
    CanvasContext ellipse(Vector2fc center, float rx, float ry);
    CanvasContext ellipse(Vector2fc center, Vector2fc radius);
    CanvasContext rect(float x, float y, float w, float h);
    CanvasContext rect(Vector2fc pos, float w, float h);
    CanvasContext rect(Vector2fc pos, Vector2fc size);
    CanvasContext roundedRect(float x, float y, float w, float h, float r);
    CanvasContext roundedRect(Vector2fc pos, float w, float h, float r);
    CanvasContext roundedRect(Vector2fc pos, Vector2fc size, float r);
    CanvasContext roundedRect(float x, float y, float w, float h, float rtl, float rtr, float rbr, float rbl);
    CanvasContext roundedRect(Vector2fc pos, float w, float h, float rtl, float rtr, float rbr, float rbl);
    CanvasContext roundedRect(Vector2fc pos, Vector2fc size, float rtl, float rtr, float rbr, float rbl);
    CanvasContext glyph(int cp, float srcX, float srcY, float fontSize, Font font);
    CanvasContext glyph(char ch, float srcX, float srcY, float fontSize, Font font);
    CanvasContext text(String text, float srcX, float srcY, float fontSize, Font font);
}
