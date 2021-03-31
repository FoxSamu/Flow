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

package net.shadew.flow.view.canvas2d;

import java.util.function.Supplier;

import net.shadew.flow.view.ViewLayer;
import net.shadew.flow.view.Viewport;
import net.shadew.flow.view.canvas2d.context.CanvasContext;

public abstract class Canvas2DLayer implements ViewLayer {
    private final Supplier<? extends CanvasContext> contextFactory;
    private CanvasContext context;

    protected Canvas2DLayer(Supplier<? extends CanvasContext> contextFactory) {
        this.contextFactory = contextFactory;
    }

    @Override
    public void init() {
        context = contextFactory.get();
        init(context);
    }

    @Override
    public void render(Viewport viewport, double time) {
        context.beginFrame(viewport.viewWidth(), viewport.viewHeight(), 1);
        render(viewport, context, time);
        context.endFrame();
    }

    @Override
    public void cleanup() {
        cleanup(context);
        context.dispose();
    }

    protected abstract void init(CanvasContext ctx);
    protected abstract void render(Viewport viewport, CanvasContext ctx, double time);
    protected abstract void cleanup(CanvasContext ctx);
}
