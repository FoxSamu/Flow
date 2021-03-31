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

package net.shadew.flow.view;

import net.shadew.flow.util.fbo.Framebuffer;
import net.shadew.flow.util.window.Window;

public class SimpleViewport implements Viewport {
    private final int bufferWidth;
    private final int bufferHeight;
    private final float pixelRatio;
    private final float viewWidth;
    private final float viewHeight;

    public SimpleViewport(int bufferWidth, int bufferHeight, float pixelRatio, float viewWidth, float viewHeight) {
        this.bufferWidth = bufferWidth;
        this.bufferHeight = bufferHeight;
        this.pixelRatio = pixelRatio;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    public SimpleViewport(Window window, float viewWidth, float viewHeight) {
        this.bufferWidth = window.getViewportWidth();
        this.bufferHeight = window.getViewportHeight();
        this.pixelRatio = (float) window.getViewportWidth() / window.getWidth();
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    public SimpleViewport(Framebuffer framebuffer, float viewWidth, float viewHeight) {
        this.bufferWidth = framebuffer.getViewportWidth();
        this.bufferHeight = framebuffer.getViewportHeight();
        this.pixelRatio = 1;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    @Override
    public int bufferWidth() {
        return bufferWidth;
    }

    @Override
    public int bufferHeight() {
        return bufferHeight;
    }

    @Override
    public float pixelRatio() {
        return pixelRatio;
    }

    @Override
    public float viewWidth() {
        return viewWidth;
    }

    @Override
    public float viewHeight() {
        return viewHeight;
    }
}
