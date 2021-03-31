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

package net.shadew.flow.util;

import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import net.shadew.util.misc.ColorUtil;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.stb.STBImageResize.*;
import static org.lwjgl.stb.STBImageWrite.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public final class NativeImage implements Disposable {
    private final ByteBuffer buf;
    private final int width;
    private final int height;

    private NativeImage(ByteBuffer buf, int width, int height) {
        this.buf = buf;
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ByteBuffer getInitializedBuffer() {
        buf.position(0).limit(buf.capacity());
        return buf;
    }

    public ByteBuffer getBuffer() {
        return buf;
    }

    public int getColor(int x, int y) {
        int i = (y * width + x) * 4;
        int r = buf.get(i) & 0xFF;
        int g = buf.get(i + 1) & 0xFF;
        int b = buf.get(i + 2) & 0xFF;
        int a = buf.get(i + 3) & 0xFF;
        return ColorUtil.rgba(r, g, b, a);
    }

    public void setColor(int x, int y, int argb) {
        int i = (y * width + x) * 4;
        buf.put(i, (byte) ColorUtil.redi(argb));
        buf.put(i + 1, (byte) ColorUtil.greeni(argb));
        buf.put(i + 2, (byte) ColorUtil.bluei(argb));
        buf.put(i + 3, (byte) ColorUtil.alphai(argb));
    }

    public void blit(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2, NativeImage out) {
        int stride1 = width - w1;
        int stride2 = out.getWidth() - w2;
        buf.limit(buf.capacity());
        buf.position(y1 * width * 4 + x1);

        ByteBuffer outBuf = out.getBuffer();
        outBuf.limit(outBuf.capacity());
        outBuf.position(y2 * out.getWidth() * 4 + x2);

        stbir_resize_uint8(buf, w1, h1, stride1, outBuf, w2, h2, stride2, 4);
    }

    @Override
    public void dispose() {
        memFree(buf);
    }

    public boolean savePng(String file, boolean flipy) {
        stbi_flip_vertically_on_write(flipy);
        buf.position(0).limit(buf.capacity());
        return stbi_write_png(file, width, height, 4, buf, 0);
    }

    public boolean saveBmp(String file, boolean flipy) {
        stbi_flip_vertically_on_write(flipy);
        buf.position(0).limit(buf.capacity());
        return stbi_write_bmp(file, width, height, 4, buf);
    }

    public boolean saveJpg(String file, boolean flipy, int quality) {
        stbi_flip_vertically_on_write(flipy);
        buf.position(0).limit(buf.capacity());
        return stbi_write_jpg(file, width, height, 4, buf, quality);
    }

    public boolean savePng(OutputStream stream, boolean flipy) throws IOException {
        stbi_flip_vertically_on_write(flipy);
        try {
            buf.position(0).limit(buf.capacity());
            return stbi_write_png_to_func((context, data, size) -> {
                byte[] bytes = new byte[size];
                ByteBuffer buf = memByteBuffer(data, size);
                buf.get(bytes, 0, size);
                try {
                    stream.write(bytes);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }, 0, width, height, 4, buf, 0);
        } catch (UncheckedIOException exc) {
            throw exc.getCause();
        }
    }

    public boolean saveBmp(OutputStream stream, boolean flipy) throws IOException {
        stbi_flip_vertically_on_write(flipy);
        try {
            buf.position(0).limit(buf.capacity());
            return stbi_write_bmp_to_func((context, data, size) -> {
                byte[] bytes = new byte[size];
                ByteBuffer buf = memByteBuffer(data, size);
                buf.get(bytes, 0, size);
                try {
                    stream.write(bytes);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }, 0, width, height, 4, buf);
        } catch (UncheckedIOException exc) {
            throw exc.getCause();
        }
    }

    public boolean saveJpg(OutputStream stream, boolean flipy, int quality) throws IOException {
        stbi_flip_vertically_on_write(flipy);
        try {
            buf.position(0).limit(buf.capacity());
            return stbi_write_jpg_to_func((context, data, size) -> {
                byte[] bytes = new byte[size];
                ByteBuffer buf = memByteBuffer(data, size);
                buf.get(bytes, 0, size);
                try {
                    stream.write(bytes);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }, 0, width, height, 4, buf, quality) == 1;
        } catch (UncheckedIOException exc) {
            throw exc.getCause();
        }
    }

    public static NativeImage allocate(int width, int height) {
        return new NativeImage(memCalloc(width * height * 4), width, height);
    }

    public static NativeImage load(String file) throws IOException {
        try (MemoryStack stack = stackPush()) {
            stbi_set_unpremultiply_on_load(true);
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer c = stack.mallocInt(1);
            ByteBuffer buffer = stbi_load(file.replace('/', File.separatorChar), w, h, c, 4);

            if (buffer == null) {
                throw new IOException("Failed to load image: " + stbi_failure_reason());
            }

            NativeImage img = allocate(w.get(0), h.get(0));
            ByteBuffer imgBuf = img.getBuffer();

            imgBuf.position(0).limit(imgBuf.capacity());
            buffer.position(0).limit(imgBuf.capacity());

            imgBuf.put(buffer);

            stbi_image_free(buffer);

            return img;
        }
    }

    public static NativeImage load(File file) throws IOException {
        return load(file.toString().replace('/', File.separatorChar));
    }

    public static NativeImage load(ByteBuffer memory) throws IOException {
        try (MemoryStack stack = stackPush()) {
            stbi_set_unpremultiply_on_load(true);
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer c = stack.mallocInt(1);
            ByteBuffer buffer = stbi_load_from_memory(memory, w, h, c, 4);

            if (buffer == null) {
                throw new IOException("Failed to load image: " + stbi_failure_reason());
            }

            NativeImage img = allocate(w.get(0), h.get(0));
            ByteBuffer imgBuf = img.getBuffer();

            imgBuf.position(0).limit(imgBuf.capacity());
            buffer.position(0).limit(imgBuf.capacity());

            imgBuf.put(buffer);

            stbi_image_free(buffer);

            return img;
        }
    }

    public static NativeImage fromRaw(int w, int h, ByteBuffer buffer) {
        NativeImage img = allocate(w, h);
        ByteBuffer imgBuf = img.getBuffer();

        imgBuf.position(0).limit(imgBuf.capacity());
        buffer.position(0).limit(imgBuf.capacity());

        imgBuf.put(buffer);

        return img;
    }

    public static NativeImage copyOf(NativeImage other) {
        return fromRaw(other.width, other.height, other.buf);
    }
}
