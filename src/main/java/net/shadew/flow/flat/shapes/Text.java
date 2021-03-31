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

import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import net.shadew.flow.util.Font;
import net.shadew.flow.view.Viewport;
import net.shadew.flow.view.canvas2d.context.CanvasContext;
import net.shadew.util.misc.ColorUtil;

public class Text extends AbstractShape {
    private String text;
    private Font font;
    private float size;
    private float writeProgress = 1;
    private float writeMotionX = 0, writeMotionY = 0;

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Font getFont() {
        return font;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public float getSize() {
        return size;
    }

    public void setWriteProgress(float writeProgress) {
        this.writeProgress = writeProgress;
    }

    public float getWriteProgress() {
        return writeProgress;
    }

    public void setWriteMotion(float writeMotionX, float writeMotionY) {
        this.writeMotionX = writeMotionX;
        this.writeMotionY = writeMotionY;
    }

    public void setWriteMotionX(float writeMotionX) {
        this.writeMotionX = writeMotionX;
    }

    public void setWriteMotionY(float writeMotionY) {
        this.writeMotionY = writeMotionY;
    }

    public float getWriteMotionX() {
        return writeMotionX;
    }

    public float getWriteMotionY() {
        return writeMotionY;
    }

    @Override
    protected final boolean drawPath(CanvasContext ctx) {
        return false;
    }

    @Override
    protected void renderNode(CanvasContext ctx, Viewport vp, double time) {
        if (writeProgress <= 0) return;

        boolean fill = ColorUtil.alphai(fillColor) != 0;
        boolean stroke = strokeWidth > 0 && ColorUtil.alphai(strokeColor) != 0;

        if (fill || stroke) {
            float scale = font.scaleForEMToPixels(size);

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer adv = stack.mallocInt(1);
                IntBuffer lsb = stack.mallocInt(1);
                IntBuffer asc = stack.mallocInt(1);
                IntBuffer desc = stack.mallocInt(1);
                IntBuffer gap = stack.mallocInt(1);
                IntBuffer cw = stack.mallocInt(1);
                FloatBuffer curX = stack.mallocFloat(1);
                FloatBuffer curY = stack.mallocFloat(1);
                LongBuffer curI = stack.mallocLong(1);

                font.getVMetrics(asc, desc, gap);

                float lh = (asc.get(0) - desc.get(0) + gap.get(0)) * scale;

                curY.put(0, 0);
                String[] lines = text.split("\r\n|\r|\n");
                for (String ln : lines) {
                    cw.put(0, 0);
                    ln.codePoints().forEachOrdered(i -> {
                        int gi = font.glyphIndex(i);
                        font.getHMetrics(gi, adv, lsb);

                        cw.put(0, cw.get(0) + adv.get(0));
                    });

                    float width = cw.get() * scale;

                    curX.put(0, -width / 2);
                    long c = ln.codePoints().count();
                    float progl = 1 / (float) c;
                    curI.put(0, 0);
                    ln.codePoints().forEachOrdered(i -> {
                        float prog = curI.get(0) / (float) c;

                        if (prog < writeProgress) {
                            ctx.beginPath();

                            float alpha = Math.min(1, (writeProgress - prog) / progl);
                            float off = 1 - alpha;
                            off = off * off * off;

                            ctx.glyph(i, curX.get(0) - off * writeMotionX, curY.get(0) - off * writeMotionY, size, font);

                            if (fill) {
                                ctx.fillColor(ColorUtil.withAlpha(ColorUtil.rgb(fillColor), ColorUtil.alphaf(fillColor) * alpha));
                                ctx.fill();
                            }

                            if (stroke) {
                                ctx.strokeColor(ColorUtil.withAlpha(ColorUtil.rgb(strokeColor), ColorUtil.alphaf(strokeColor) * alpha));
                                ctx.strokeWidth(strokeWidth);
                                ctx.stroke();
                            }
                        }

                        int gi = font.glyphIndex(i);
                        font.getHMetrics(gi, adv, lsb);

                        curX.put(0, curX.get(0) + adv.get(0) * scale);
                        curI.put(0, curI.get(0) + 1);
                    });

                    curY.put(0, curY.get(0) + lh);
                }
            }
        }
    }
}
