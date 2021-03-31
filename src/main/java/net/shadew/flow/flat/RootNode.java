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

package net.shadew.flow.flat;

import net.shadew.flow.view.Viewport;
import net.shadew.flow.view.canvas2d.context.CanvasContext;
import net.shadew.util.misc.ColorUtil;

public class RootNode extends Node2D {
    private int backgroundColor;

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }



    @Override
    protected void renderNode(CanvasContext ctx, Viewport vp, double time) {
        if (ColorUtil.alphai(backgroundColor) != 0) {
            ctx.pushTransform();
            ctx.setIdentity();
            ctx.beginPath();
            ctx.rect(0, 0, vp.viewWidth(), vp.viewHeight());
            ctx.fillColor(backgroundColor);
            ctx.fill();
            ctx.popTransform();
        }
    }

    @Override
    protected boolean isRoot() {
        return true;
    }
}
