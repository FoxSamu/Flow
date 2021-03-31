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

import net.shadew.flow.view.canvas2d.context.CanvasContext;

public class Rectangle extends AbstractShape {
    private float width;
    private float height;

    public void setSize(float width, float height) {
        setWidth(width);
        setHeight(height);
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    @Override
    protected boolean drawPath(CanvasContext ctx) {
        ctx.rect(-width / 2, -height / 2, width, height);
        return true;
    }
}
