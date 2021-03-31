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

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import net.shadew.flow.util.window.Monitor;
import net.shadew.flow.util.window.Window;
import net.shadew.flow.view.Viewport;
import net.shadew.util.misc.MathUtil;

public class DisplayOutput implements Output {
    private final int width, height;
    private final boolean fullscreen;
    private Window window;
    private Timer timer;
    private WindowViewport viewport;
    private long expectedFrameTime;
    private long frameStart;
    private long frameEnd;

    private DisplayOutput(int width, int height, boolean fullscreen) {
        this.width = width;
        this.height = height;
        this.fullscreen = fullscreen;
    }

    @Override
    public void init(Window window, float viewWidth, float viewHeight, int fps, Timer timer) {
        if (fullscreen) {
            window.setFullScreenMonitor(Monitor.getPrimary());
        } else {
            window.setSize(width, height);
            window.center(Monitor.getPrimary());
        }

        window.show();
        window.focus();

        window.setKeyCallback((window1, key, scancode, action, mods) -> {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                window.close();
            }
            if (key == GLFW.GLFW_KEY_R && action == GLFW.GLFW_PRESS) {
                timer.replay();
            }
            if (key == GLFW.GLFW_KEY_SPACE && action == GLFW.GLFW_PRESS) {
                if (timer.isPaused()) {
                    timer.unpause();
                } else {
                    timer.pause();
                }
            }
            if (key == GLFW.GLFW_KEY_F && action == GLFW.GLFW_PRESS) {
                if (window.getFullScreenMonitor() == null) {
                    window.setFullScreenMonitor(Monitor.getPrimary());
                } else {
                    window.setFullScreenMonitor(null);
                    window.setSize(width, height);
                    window.center(Monitor.getPrimary());
                }
            }
        });

        window.setScrollCallback((window1, xoffset, yoffset) -> {
            double d = yoffset;
            if (isControlDown()) {
                d = yoffset * 0.1;
            }
            double f = timer.getFrame() + timer.getFrameOffset() + d;
            timer.setFrame((long) f, (float) (f % 1));
        });

        this.window = window;
        this.viewport = new WindowViewport(window, viewWidth, viewHeight);
        this.expectedFrameTime = 1000000000L / fps;
        this.timer = timer;
    }

    private boolean isControlDown() {
        return window.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) || window.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    @Override
    public void startFrame(long frameNumber) {
        if (window.isMouseDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            double d = window.getMouseX() / window.getWidth();
            double f = MathUtil.lerp(timer.getStartFrame(), timer.getEndFrame(), MathUtil.clamp(d, 0, 1));
            timer.setFrame((long) f, isControlDown() ? 0 : (float) (f % 1));
        }
        frameStart = System.nanoTime();
        GL11.glViewport(0, 0, window.getViewportWidth(), window.getViewportHeight());
    }

    @Override
    public void endFrame(long frameNumber) {
        window.swapBuffers();
        Window.pollEvents();

        long frameEnd = System.nanoTime();
        long frameTime = frameEnd - frameStart;
        try {
            long remaining = expectedFrameTime - frameTime;
            if (remaining > 0) {
                long millis = remaining / 1000000L;
                int nanos = (int) (remaining % 1000000L);
                Thread.sleep(millis, nanos);
            }
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void cleanup() {

    }

    @Override
    public Viewport getViewport() {
        return viewport;
    }

    @Override
    public boolean isFiniteTime() {
        return window.shouldClose();
    }

    public static DisplayOutput framed(int w, int h) {
        return new DisplayOutput(w, h, false);
    }

    public static DisplayOutput fullscreen(int w, int h) {
        return new DisplayOutput(w, h, true);
    }

    private static class WindowViewport implements Viewport {
        private final Window window;
        private final float viewWidth;
        private final float viewHeight;

        private WindowViewport(Window window, float viewWidth, float viewHeight) {
            this.window = window;
            this.viewWidth = viewWidth;
            this.viewHeight = viewHeight;
        }

        @Override
        public int bufferWidth() {
            return window.getViewportWidth();
        }

        @Override
        public int bufferHeight() {
            return window.getViewportHeight();
        }

        @Override
        public float pixelRatio() {
            return (float) window.getViewportWidth() / window.getWidth();
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
}
