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

package net.shadew.flow.flat.shapes;

import net.shadew.flow.flat.Node2D;
import net.shadew.flow.view.Viewport;
import net.shadew.flow.view.canvas2d.context.CanvasContext;
import net.shadew.util.misc.ColorUtil;

public abstract class AbstractShape extends Node2D {
    protected int fillColor;
    protected int strokeColor;
    protected float strokeWidth;

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    @Override
    protected void renderNode(CanvasContext ctx, Viewport vp, double time) {
        boolean fill = ColorUtil.alphai(fillColor) != 0;
        boolean stroke = strokeWidth > 0 && ColorUtil.alphai(strokeColor) != 0;

        if (fill || stroke) {
            ctx.beginPath();
            if (drawPath(ctx)) {

                if (fill) {
                    ctx.fillColor(fillColor);
                    ctx.fill();
                }

                if (stroke) {
                    ctx.strokeColor(strokeColor);
                    ctx.strokeWidth(strokeWidth);
                    ctx.stroke();
                }
            }
        }
    }

    protected abstract boolean drawPath(CanvasContext ctx);
}
