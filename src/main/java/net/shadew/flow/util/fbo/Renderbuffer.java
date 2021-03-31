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

import static org.lwjgl.opengl.GL32.*;

public class Renderbuffer implements FramebufferAttachment {
    private final int fboTarget;
    private final int format;
    private int rboId;
    private boolean generated;

    public Renderbuffer(int fboTarget, int format) {
        this.fboTarget = fboTarget;
        this.format = format;
    }

    @Override
    public void initSize(Framebuffer framebuffer, int attachment, int width, int height) {
        if (!generated) {
            rboId = glGenRenderbuffers();
            generated = true;
        }

        framebuffer.bind();
        bind();
        glRenderbufferStorage(GL_RENDERBUFFER, format, width, height);
        unbind();
        glFramebufferRenderbuffer(fboTarget, attachment, GL_RENDERBUFFER, rboId);
        framebuffer.unbind();
    }

    @Override
    public void dispose() {
        if (generated) {
            glDeleteRenderbuffers(rboId);
            generated = false;
        }
    }

    public void bind() {
        glBindRenderbuffer(GL_RENDERBUFFER, rboId);
    }

    public void unbind() {
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
    }
}
