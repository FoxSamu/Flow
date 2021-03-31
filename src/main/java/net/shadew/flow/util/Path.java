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

package net.shadew.flow.util;

import org.joml.Vector2f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;
import java.util.List;

import net.shadew.util.misc.ColorUtil;
import net.shadew.util.misc.MathUtil;

import static org.lwjgl.nanovg.NanoVG.*;

/**
 * A path object stores a renderable path, and can be used to create outline tracking or morphing animations. Paths can
 * be constructed using operations like {@link #moveTo}, {@link #lineTo}, {@link #quadTo} or {@link #curveTo}. While
 * these operations involve curves, a path object boils them down into line segments for easier processing.
 */
public class Path {
    /**
     * The fill color of this path
     */
    public int fillColor;

    /**
     * The stroke color of this path
     */
    public int strokeColor;

    /**
     * The stroke width of this path
     */
    public float strokeWidth;

    private final List<Segment> segments = new ArrayList<>();

    /**
     * Resets the path, removing all points and subpaths
     */
    public void clear() {
        segments.clear();
    }

    /**
     * Initiates a new subpath at the given coordinates. This must be called as a first call after constructing a new
     * path object, after calling {@link #clear} or after closing the last subpath using {@link #close}.
     *
     * @param x The starting X coordinate
     * @param y The starting Y coordinate
     */
    public void moveTo(float x, float y) {
        segments.add(new Segment(x, y));
    }

    /**
     * Adds a straight line segment between the last path endpoint and the given coordinates. This requires that the
     * subpath constructed by the last operation is present and not closed.
     *
     * @param x The X endpoint
     * @param y The Y endpoint
     */
    public void lineTo(float x, float y) {
        if (segments.isEmpty()) {
            throw new IllegalStateException("No moveTo called");
        }

        Segment seg = segments.get(segments.size() - 1);
        if (seg.closed) {
            throw new IllegalStateException("No moveTo called");
        }
        seg.lineTo(x, y);
    }

    /**
     * Adds a quadratic bezier between the last path endpoint and the given coordinates. This requires that the subpath
     * constructed by the last operation is present and not closed. For easier processing, the bezier is added as {@code
     * segmentation} {@link #lineTo} operations along the path of the bezier curve.
     *
     * @param cx           The control point X
     * @param cy           The control point Y
     * @param x            The X endpoint
     * @param y            The Y endpoint
     * @param segmentation The amount of line segments (and thus precision) of the rendered bezier
     */
    public void quadTo(float cx, float cy, float x, float y, int segmentation) {
        if (segments.isEmpty()) {
            throw new IllegalStateException("No moveTo called");
        }

        Segment seg = segments.get(segments.size() - 1);
        if (seg.closed) {
            throw new IllegalStateException("No moveTo called");
        }

        Vector2f start = seg.points.get(seg.points.size() - 1);

        for (int i = 1; i <= segmentation; i++) {
            float t = (float) i / segmentation;
            seg.lineTo(
                quadBezier(start.x, cx, x, t),
                quadBezier(start.y, cy, y, t)
            );
        }
        seg.lineTo(x, y);
    }

    private static float quadBezier(float a, float c, float b, float t) {
        return MathUtil.lerp(MathUtil.lerp(a, c, t), MathUtil.lerp(c, b, t), t);
    }

    /**
     * Adds a cubic bezier between the last path endpoint and the given coordinates. This requires that the subpath
     * constructed by the last operation is present and not closed. For easier processing, the bezier is added as {@code
     * segmentation} {@link #lineTo} operations along the path of the bezier curve.
     *
     * @param cx1          The first control point X
     * @param cy1          The first control point Y
     * @param cx2          The second control point X
     * @param cy2          The second control point Y
     * @param x            The X endpoint
     * @param y            The Y endpoint
     * @param segmentation The amount of line segments (and thus precision) of the rendered bezier
     */
    public void curveTo(float cx1, float cy1, float cx2, float cy2, float x, float y, int segmentation) {
        if (segments.isEmpty()) {
            throw new IllegalStateException("No moveTo called");
        }

        Segment seg = segments.get(segments.size() - 1);
        if (seg.closed) {
            throw new IllegalStateException("No moveTo called");
        }

        Vector2f start = seg.points.get(seg.points.size() - 1);

        for (int i = 1; i <= segmentation; i++) {
            float t = (float) i / segmentation;
            seg.lineTo(
                cubicBezier(start.x, cx1, cx2, x, t),
                cubicBezier(start.y, cy1, cy2, y, t)
            );
        }
        seg.lineTo(x, y);
    }

    private static float cubicBezier(float a, float c1, float c2, float b, float t) {
        return quadBezier(MathUtil.lerp(a, c1, t), MathUtil.lerp(c1, c2, t), MathUtil.lerp(c2, b, t), t);
    }

    /**
     * Adds a line segment followed by an arc in the given direction. The arc follows the path of a circle with the
     * given radius, fit in the desired corner. Note that the endpoint of the path is at the end of the arc, not at the
     * given endpoint (this can be achieved by doing another {@link #lineTo} operation to that endpoint). Moreover, you
     * can see this as a {@link #lineTo} operation, but rounding the corner with the given radius. This method requires
     * that the subpath constructed by the last operation is present and not closed. For easier processing, the arc is
     * added as {@code segmentation} {@link #lineTo} operations along the path of the arc.
     *
     * @param cx           The X coordinate of the arc corner
     * @param cy           The Y coordinate of the arc corner
     * @param ex           The X coordinate of the point the arc should point to
     * @param ey           The Y coordinate of the point the arc should point to
     * @param segmentation The amount of line segments (and thus precision) of the rendered arc
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    public void arcTo(float cx, float cy, float ex, float ey, float r, int segmentation) {
        if (segments.isEmpty()) {
            throw new IllegalStateException("No moveTo called");
        }

        Segment seg = segments.get(segments.size() - 1);
        if (seg.closed) {
            throw new IllegalStateException("No moveTo called");
        }

        Vector2f start = seg.points.get(seg.points.size() - 1);

        float sx = start.x, sy = start.y;

        // Vectors m and n, given M = S - C and N = E - C
        float mx = sx - cx, my = sy - cy, ml = (float) Math.sqrt(mx * mx + my * my);
        float nx = ex - cx, ny = ey - cy, nl = (float) Math.sqrt(nx * nx + ny * ny);

        // Unit vectors u and v, given u = m / |m| and v = n / |n|
        float ux = mx / ml, uy = my / ml;
        float vx = nx / nl, vy = ny / nl;

        float c = ux * vy - uy * vx; // u x v  (cross product)
        float d = ux * vx + uy * vy; // u . v  (dot product)

        float a = (float) Math.acos(d); // Angle between u and v
        float l = (1 + d) / Math.abs(c); // Given a is the angle between u and v, this is equal to cot(a/2)

        float csign = Math.signum(c); // Winding sign of the arc
        float pux = -uy, puy = ux; // Perpendicular of u
        float spx = ux * l * r + cx, spy = uy * l * r + cy; // Start point of the actual arc
        float ccx = spx + pux * csign * r, ccy = spy + puy * csign * r; // Center of the arc's circle

        float ta = ((float) Math.PI - a) * -csign;

        seg.lineTo(spx, spy);
        for (int i = 1; i <= segmentation; i++) {
            float t = (float) i / segmentation * ta;

            // Rotation matrix:
            // | cos(x) -sin(x) |
            // | sin(x)  cos(x) |

            float cos = (float) Math.cos(t);
            float sin = (float) Math.sin(t);

            // Vector from center to arc start
            float ix = spx - ccx, iy = spy - ccy;

            // Rotate this vector
            float px = cos * ix - sin * iy, py = sin * ix + cos * iy;

            seg.lineTo(px + ccx, py + ccy);
        }
    }

    /**
     * Closes the current subpath. This method requires that the subpath constructed by the last operation is present
     * and not closed.
     */
    public void close() {
        if (segments.isEmpty()) {
            throw new IllegalStateException("No moveTo called");
        }

        Segment seg = segments.get(segments.size() - 1);
        if (seg.closed) {
            throw new IllegalStateException("No moveTo called");
        }
        seg.closed = true;
    }

    /**
     * Draws this path on the given {@link NanoVG} context.
     *
     * The {@code start} and {@code end} parameters indicate which part of the path should be rendered. They are numbers
     * between 0 and 1 that interpolate the path's start and end points. The path object ensures that the length of the
     * rendered path is exactly equal to the length of the total path multiplied by {@code end - start}. The given
     * segment is rendered of each subpath. Note that the path is only rendered as a closed path if start is 0 and end
     * is 1.
     *
     * @param nvg   The NanoVG context to render the path on
     * @param start The start of the path
     * @param end   The end of the path
     */
    public void draw(long nvg, float start, float end) {
        if (segments.size() < 1) {
            return;
        }

        boolean hasStroke = strokeWidth > 0 || ColorUtil.alphai(strokeColor) != 0;
        boolean hasFill = ColorUtil.alphai(fillColor) != 0;
        if (!hasFill && !hasStroke) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            NVGColor fill = NVGColor.mallocStack(stack);
            NVGColor stroke = NVGColor.mallocStack(stack);

            Colors.rgba(fill, fillColor);
            Colors.rgba(stroke, strokeColor);

            nvgStrokeColor(nvg, stroke);
            nvgFillColor(nvg, fill);
            nvgStrokeWidth(nvg, strokeWidth);

            nvgBeginPath(nvg);

            boolean closeLast = false;
            for (Segment segment : segments) {
                segment.draw(nvg, start, end, closeLast);
                closeLast = segment.closed;// && end == 1 && start == 0;
            }
            if (closeLast) {
                nvgClosePath(nvg);
            }
            nvgPathWinding(nvg, NVG_HOLE);

            if (hasFill) nvgFill(nvg);
            if (hasStroke) nvgStroke(nvg);
        }
    }


    private static class Segment {
        boolean closed;
        final List<Vector2f> points = new ArrayList<>();

        Segment(float x, float y) {
            points.add(new Vector2f(x, y));
        }

        void lineTo(float x, float y) {
            points.add(new Vector2f(x, y));
        }

        void draw(long nvg, float start, float end, boolean closeLastPath) {
            if (points.size() < 2) {
                return;
            }

            int count = points.size();
            float len = 0;

            for (int i = 0; i < count; i++) {
                int j = i + 1;
                if (j == count) {
                    if (!closed) continue; // There's no line back to the beginning
                    j = 0;
                }

                Vector2f c = points.get(i);
                Vector2f n = points.get(j);
                len += c.distance(n);
            }

            float fstart = start * len;
            float fend = end * len;

            float clen = 0;

            boolean began = false;

            for (int i = 0; i < count; i++) {
                int j = i + 1;
                if (j == count) {
                    if (!closed) continue; // There's no line back to the beginning
                    j = 0;
                }

                Vector2f c = points.get(i);
                Vector2f n = points.get(j);

                float dist = c.distance(n);

                if (dist > 0) {
                    float nlen = clen + dist;

                    float s = MathUtil.clamp(MathUtil.unlerp(clen, nlen, fstart), 0, 1);
                    float e = MathUtil.clamp(MathUtil.unlerp(clen, nlen, fend), 0, 1);
                    if (s != e) {
                        if (!began && s <= 1) {
                            float sx = MathUtil.lerp(c.x, n.x, s);
                            float sy = MathUtil.lerp(c.y, n.y, s);
                            nvgMoveTo(nvg, sx, sy);
                            if (closeLastPath) {
                                nvgClosePath(nvg);
                            }
                            nvgPathWinding(nvg, NVG_HOLE);
                            began = true;
                        }
                        if (e > 0 && began) {
                            float ex = MathUtil.lerp(c.x, n.x, e);
                            float ey = MathUtil.lerp(c.y, n.y, e);
                            nvgLineTo(nvg, ex, ey);
                        }
                    }
                }

                clen += dist;
            }
        }
    }
}
