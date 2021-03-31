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

import java.util.function.Supplier;

import net.shadew.flow.view.Viewport;
import net.shadew.flow.view.canvas2d.Canvas2DLayer;
import net.shadew.flow.view.canvas2d.context.CanvasContext;

public class FlatStage extends Canvas2DLayer {
    public final RootNode root = new RootNode();

    public FlatStage(Supplier<? extends CanvasContext> contextFactory) {
        super(contextFactory);
    }

    @Override
    protected void init(CanvasContext ctx) {

    }

    @Override
    protected void render(Viewport viewport, CanvasContext ctx, double time) {
        root.render(ctx, viewport, time);
    }

    @Override
    protected void cleanup(CanvasContext ctx) {
        root.traverse(Node2D::dispose);
    }
}
