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

package net.shadew.flow.util.fbo;

import java.util.HashMap;
import java.util.Map;

import net.shadew.flow.util.Disposable;

import static org.lwjgl.opengl.GL32.*;

public class Framebuffer implements Disposable {
    private int width;
    private int height;
    private final Map<Integer, FramebufferAttachment> attachments = new HashMap<>();
    private final int handle;

    public Framebuffer() {
        handle = glGenFramebuffers();
    }

    public Framebuffer(int w, int h) {
        this();
        setSize(w, h);
    }

    public void setSize(int w, int h) {
        if (w != width || h != height) {
            for (Map.Entry<Integer, FramebufferAttachment> attachment : attachments.entrySet()) {
                attachment.getValue().initSize(this, attachment.getKey(), w, h);
            }
        }
        width = w;
        height = h;
    }

    public void addAttachment(int attachmentId, FramebufferAttachment attachment) {
        FramebufferAttachment old = attachments.put(attachmentId, attachment);
        if (old != null) {
            old.dispose();
        }
        attachment.initSize(this, attachmentId, width, height);
    }

    public void removeAttachment(int attachmentId) {
        FramebufferAttachment old = attachments.remove(attachmentId);
        if (old != null) {
            old.dispose();
        }
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, handle);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void bindRead() {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, handle);
    }

    public void unbindRead() {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
    }

    public void bindDraw() {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, handle);
    }

    public void unbindDraw() {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
    }

    public void checkComplete() {
        bind();
        int completionStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        unbind();
        switch (completionStatus) {
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                throw new IllegalStateException("Incomplete framebuffer attachement");
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                throw new IllegalStateException("Missing framebuffer attachement");
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                throw new IllegalStateException("Incomplete draw buffer");
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                throw new IllegalStateException("Incomplete read buffer");
            case GL_FRAMEBUFFER_UNSUPPORTED:
                throw new IllegalStateException("Unsupported framebuffer setup");
            case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
                throw new IllegalStateException("Renderbuffer samples do not match");
            case GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS:
                throw new IllegalStateException("Incomplete framebuffer layer targets");
            case GL_FRAMEBUFFER_COMPLETE:
                return;
            default:
                throw new IllegalStateException("Incomplete framebuffer");
        }
    }

    @Override
    public void dispose() {
        glDeleteFramebuffers(handle);

        for (FramebufferAttachment attachment : attachments.values()) {
            attachment.dispose();
        }
    }

    public void updateGlViewport() {
        glViewport(0, 0, width, height);
    }

    public int getViewportWidth() {
        return width;
    }

    public int getViewportHeight() {
        return height;
    }

    public float getAspectRatio(boolean vertical) {
        float h = vertical ? height : width;
        float v = vertical ? width : height;
        return h / v;
    }

    public static void blit(Framebuffer src, int srcX, int srcY, int srcW, int srcH, Framebuffer dst, int dstX, int dstY, int dstW, int dstH, int mask, int filter) {
        src.bindRead();
        dst.bindDraw();
        glBlitFramebuffer(srcX, srcY, srcX + srcW, srcY + srcH, dstX, dstY, dstX + dstW, dstY + dstH, mask, filter);
        src.unbindRead();
        dst.unbindDraw();
    }

    public static void useDrawBuffers(int... buffers) {
        glDrawBuffers(buffers);
    }
}
