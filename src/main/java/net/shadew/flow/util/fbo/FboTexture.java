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

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL32.*;

public class FboTexture implements FramebufferAttachment {
    private final int internalFormat;
    private final int format;
    private final int formatType;
    private int interpolation = GL_LINEAR;
    private int textureId;
    private int width;
    private int height;

    public FboTexture(int internalFormat, int format, int formatType) {
        this.internalFormat = internalFormat;
        this.format = format;
        this.formatType = formatType;
    }

    public void setInterpolation(int interpolation) {
        this.interpolation = interpolation;
        if (textureId != 0) {
            bind();
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, interpolation);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, interpolation);
            unbind();
        }
    }

    @Override
    public void initSize(Framebuffer framebuffer, int attachment, int width, int height) {
        if (textureId == 0) {
            textureId = glGenTextures();
        }

        bind();
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, formatType, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, interpolation);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, interpolation);
        unbind();

        framebuffer.bind();
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, GL_TEXTURE_2D, textureId, 0);
        framebuffer.unbind();

        this.width = width;
        this.height = height;
    }

    @Override
    public void dispose() {
        if (textureId != 0) {
            glDeleteTextures(textureId);
            textureId = 0;
        }
    }


    public int getTexID() {
        return textureId;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public static FboTexture defaultColor() {
        return new FboTexture(GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE);
    }
}
