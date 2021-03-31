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

package net.shadew.flow.tree;

import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.system.MemoryStack;

import net.shadew.flow.util.Colors;
import net.shadew.util.misc.ColorUtil;

import static org.lwjgl.nanovg.NanoVG.*;

/**
 * A generic node in a 2D animation scene
 */
public abstract class Node {
    public final Vector2f position = new Vector2f();
    public final Matrix3x2f transform = new Matrix3x2f();

    public abstract float sizeX();
    public abstract float sizeY();

    public Vector2f size(Vector2f out) {
        if (out == null) out = new Vector2f();
        out.x = sizeX();
        out.y = sizeY();
        return out;
    }

    public abstract float originX();
    public abstract float originY();

    public Vector2f origin(Vector2f out) {
        if (out == null) out = new Vector2f();
        out.x = originX();
        out.y = originY();
        return out;
    }

    public float lowerX() {
        return originX() - position.x;
    }

    public float lowerY() {
        return originY() - position.y;
    }

    public Vector2f lower(Vector2f out) {
        if (out == null) out = new Vector2f();
        out.x = lowerX();
        out.y = lowerY();
        return out;
    }

    public float upperX() {
        return lowerX() + sizeX();
    }

    public float upperY() {
        return lowerY() + sizeY();
    }

    public Vector2f upper(Vector2f out) {
        if (out == null) out = new Vector2f();
        out.x = upperX();
        out.y = upperY();
        return out;
    }

    public float centerX() {
        return lowerX() + sizeX() / 2f;
    }

    public float centerY() {
        return lowerY() + sizeY() / 2f;
    }

    public Vector2f center(Vector2f out) {
        if (out == null) out = new Vector2f();
        out.x = centerX();
        out.y = centerY();
        return out;
    }

    protected abstract void draw(long nvg, float x, float y, float w, float h);

    public void drawNode(long nvg, boolean debug) {
        nvgSave(nvg);
        nvgTranslate(nvg, position.x, position.y);
        nvgTransform(
            nvg,
            transform.m00, transform.m10, transform.m20,
            transform.m01, transform.m11, transform.m21
        );

        draw(nvg, -originX(), -originY(), sizeX(), sizeY());

        nvgRestore(nvg);

        if (debug) {
            nvgSave(nvg);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                NVGColor col = NVGColor.mallocStack(stack);
                nvgStrokeWidth(nvg, 2);

                float minX = lowerX();
                float minY = lowerY();
                float maxX = upperX();
                float maxY = upperY();
                float centerX = centerX();
                float centerY = centerY();
                float positionX = position.x;
                float positionY = position.y;
                float sizeX = sizeX();
                float sizeY = sizeY();

                nvgStrokeColor(nvg, Colors.rgb(col, ColorUtil.LIME));

                nvgBeginPath(nvg);
                nvgMoveTo(nvg, minX, positionY);
                nvgLineTo(nvg, maxX, positionY);
                nvgMoveTo(nvg, positionX, minY);
                nvgLineTo(nvg, positionX, maxY);
                nvgStroke(nvg);

                nvgStrokeColor(nvg, Colors.rgb(col, ColorUtil.BLUE));

                nvgBeginPath(nvg);
                nvgMoveTo(nvg, minX, centerY);
                nvgLineTo(nvg, maxX, centerY);
                nvgMoveTo(nvg, centerX, minY);
                nvgLineTo(nvg, centerX, maxY);
                nvgStroke(nvg);


                nvgStrokeColor(nvg, Colors.rgb(col, ColorUtil.RED));

                nvgBeginPath(nvg);
                nvgRect(nvg, minX, minY, sizeX, sizeY);
                nvgStroke(nvg);

                nvgFillColor(nvg, Colors.rgb(col, ColorUtil.LIME));

                nvgBeginPath(nvg);
                nvgCircle(nvg, positionX, positionY, 3);
                nvgFill(nvg);

                nvgFillColor(nvg, Colors.rgb(col, ColorUtil.BLUE));

                nvgBeginPath(nvg);
                nvgCircle(nvg, centerX, centerY, 3);
                nvgFill(nvg);

                nvgFillColor(nvg, Colors.rgb(col, ColorUtil.RED));

                nvgBeginPath(nvg);
                nvgCircle(nvg, minX, minY, 3);
                nvgCircle(nvg, maxX, minY, 3);
                nvgCircle(nvg, maxX, maxY, 3);
                nvgCircle(nvg, minX, maxY, 3);
                nvgFill(nvg);
            }
            nvgRestore(nvg);
        }
    }
}
