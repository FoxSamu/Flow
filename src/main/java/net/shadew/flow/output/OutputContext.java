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

package net.shadew.flow.output;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import net.shadew.flow.animation.Timeline;
import net.shadew.flow.util.window.Window;
import net.shadew.flow.view.ViewStack;

public class OutputContext {
    private final float viewWidth;
    private final float viewHeight;
    private final Output output;
    private final ViewStack viewStack;
    private final Timeline timeline;
    private final Timer timer = new Timer();
    private final int fps;
    private Window window;
    private long frame;

    public OutputContext(float viewWidth, float viewHeight, Output output, ViewStack viewStack, Timeline timeline, int fps) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.output = output;
        this.viewStack = viewStack;
        this.timeline = timeline;
        this.fps = fps;
    }

    public void init() {
        Window.initGLFW();

        window = new Window(960, 540);
        window.grabContext();

        GL.createCapabilities();

        output.init(window, viewWidth, viewHeight, fps, timer);
        viewStack.init();
    }

    public void cleanup() {
        viewStack.cleanup();
        output.cleanup();

        GL.destroy();

        window.dispose();
        Window.terminateGLFW();
    }

    public void render() {
        output.startFrame(timer.getFrame());

        double time = timer.getTime(fps);
        timeline.runAnimations(time);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
        viewStack.render(output.getViewport(), time);
        timeline.cleanupAnimations(time);

        output.endFrame(timer.getFrame());
    }

    private void driveTimer() {
        while ((timer.shouldContinue() || !output.isFiniteTime()) && !window.shouldClose()) {
            render();
            timer.nextFrame();
        }
    }

    public void doFrames(long from, long to) {
        timer.setRange(from, to);
        driveTimer();
    }

    public void doSeconds(double from, double to) {
        timer.setRange((long) (from * fps), (long) (to * fps));
        driveTimer();
    }

    public void driveUntilTerminate(long startFrame) {
        timer.setInfinitelyFrom(startFrame);
        driveTimer();
    }
}
