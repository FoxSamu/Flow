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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

import net.shadew.flow.util.NativeImage;
import net.shadew.flow.util.fbo.Framebuffer;
import net.shadew.flow.util.fbo.MsaaRenderbuffer;
import net.shadew.flow.util.fbo.Renderbuffer;
import net.shadew.flow.util.window.Window;
import net.shadew.flow.view.SimpleViewport;
import net.shadew.flow.view.Viewport;

import static org.lwjgl.opengl.GL11.*;

public class WriteOutput implements Output {
    private final int width, height;
    private final Sink out;
    private final int samples;
    private SimpleViewport viewport;

    private Framebuffer renderFbo;
    private MsaaRenderbuffer renderColorRbo;
    private MsaaRenderbuffer renderDepthStencilRbo;

    private Framebuffer outputFbo;
    private Renderbuffer outputRbo;

    private NativeImage outImage;

    private WriteOutput(int width, int height, Sink out, int samples) {
        this.width = width;
        this.height = height;
        this.out = out;
        this.samples = samples;
    }

    @Override
    public void init(Window window, float viewWidth, float viewHeight, int fps, Timer timer) {
        renderFbo = new Framebuffer();
        renderFbo.setSize(width, height);
        renderColorRbo = new MsaaRenderbuffer(GL32.GL_FRAMEBUFFER, GL32.GL_RGBA, samples);
        renderDepthStencilRbo = new MsaaRenderbuffer(GL32.GL_FRAMEBUFFER, GL32.GL_DEPTH32F_STENCIL8, samples);
        renderFbo.addAttachment(GL32.GL_COLOR_ATTACHMENT0, renderColorRbo);
        renderFbo.addAttachment(GL32.GL_DEPTH_STENCIL_ATTACHMENT, renderDepthStencilRbo);

        outputFbo = new Framebuffer();
        outputFbo.setSize(width, height);
        outputRbo = new Renderbuffer(GL32.GL_FRAMEBUFFER, GL32.GL_RGBA);
        outputFbo.addAttachment(GL32.GL_COLOR_ATTACHMENT0, outputRbo);

        outImage = NativeImage.allocate(width, height);

        viewport = new SimpleViewport(width, height, 1, viewWidth, viewHeight);

        out.init(fps);
    }

    @Override
    public void startFrame(long frameNumber) {
        System.out.println("Writing frame " + frameNumber);
        GL11.glViewport(0, 0, width, height);

        Framebuffer.useDrawBuffers(GL32.GL_COLOR_ATTACHMENT0, GL32.GL_DEPTH_ATTACHMENT, GL32.GL_STENCIL_ATTACHMENT);
        renderFbo.bind();
    }

    @Override
    public void endFrame(long frameNumber) {
        renderFbo.unbind();

        // Blit MSAA buffer to output buffer, we can't read from MSAA buffers
        renderFbo.bindRead();
        outputFbo.bindDraw();

        GL32.glBlitFramebuffer(
            0, 0, renderFbo.getViewportWidth(), renderFbo.getViewportHeight(),
            0, 0, outputFbo.getViewportWidth(), outputFbo.getViewportHeight(),
            GL_COLOR_BUFFER_BIT, GL_LINEAR
        );

        renderFbo.unbindRead();
        outputFbo.unbindDraw();

        // Read output buffer into image instance
        outputFbo.bind();

        glReadBuffer(GL32.GL_COLOR_ATTACHMENT0);
        glReadPixels(
            0, 0, outputFbo.getViewportWidth(), outputFbo.getViewportHeight(),
            GL_RGBA, GL_UNSIGNED_BYTE,
            outImage.getInitializedBuffer()
        );

        out.flushBuffer(frameNumber, outImage);

        outputFbo.unbind();
    }

    @Override
    public void cleanup() {
        out.cleanup();

        renderColorRbo.dispose();
        renderDepthStencilRbo.dispose();
        renderFbo.dispose();

        outputRbo.dispose();
        outputFbo.dispose();

        outImage.dispose();
    }

    @Override
    public Viewport getViewport() {
        return viewport;
    }

    @Override
    public boolean isFiniteTime() {
        return true;
    }

    public static WriteOutput flushInto(int w, int h, Sink sink, int samples) {
        return new WriteOutput(w, h, sink, samples);
    }

    public static WriteOutput flushInto(int w, int h, Sink sink) {
        return new WriteOutput(w, h, sink, 16);
    }
}
