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

import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTVertex;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Font {
    private final ByteBuffer buffer;
    private STBTTFontinfo fontInfo;

    public Font(ByteBuffer buffer) {
        this(buffer.remaining());
        this.buffer.put(buffer);
        this.buffer.flip();
    }

    private Font(int bufferSize) {
        this.buffer = memAlloc(bufferSize);
    }

    public boolean init() {
        fontInfo = STBTTFontinfo.malloc();
        return stbtt_InitFont(fontInfo, buffer);
    }

    public int glyphIndex(int codepoint) {
        return stbtt_FindGlyphIndex(fontInfo, codepoint);
    }

    public STBTTVertex.Buffer getShape(int glyphIndex) {
        return stbtt_GetGlyphShape(fontInfo, glyphIndex);
    }

    public boolean isEmpty(int glyphIndex) {
        return stbtt_IsGlyphEmpty(fontInfo, glyphIndex);
    }

    public void getBox(int glyphIndex, IntBuffer x1, IntBuffer y1, IntBuffer x2, IntBuffer y2) {
        stbtt_GetGlyphBox(fontInfo, glyphIndex, x1, y1, x2, y2);
    }

    public void getHMetrics(int glyphIndex, IntBuffer advanceWidth, IntBuffer leftSideBearing) {
        stbtt_GetGlyphHMetrics(fontInfo, glyphIndex, advanceWidth, leftSideBearing);
    }

    public void getVMetrics(IntBuffer ascent, IntBuffer descent, IntBuffer lineGap) {
        stbtt_GetFontVMetrics(fontInfo, ascent, descent, lineGap);
    }

    public float scaleForPixelHeight(float size) {
        return stbtt_ScaleForPixelHeight(fontInfo, size);
    }

    public float scaleForEMToPixels(float size) {
        return stbtt_ScaleForMappingEmToPixels(fontInfo, size);
    }

    public void dispose() {
        fontInfo.free();
        memFree(buffer);
    }

    public static Font load(InputStream in) throws IOException {
        try {
            ByteBuffer data = MemoryUtil.memAlloc(4096);

            byte[] readBuf = new byte[4096];
            int r;
            while ((r = in.read(readBuf)) != -1) {
                if (r > 0) {
                    if (data.remaining() < r) {
                        ByteBuffer newData = MemoryUtil.memAlloc(data.capacity() + 4096);
                        data.flip();
                        newData.put(data);
                        MemoryUtil.memFree(data);
                        data = newData;
                    }
                    data.put(readBuf, 0, r);
                }
            }

            data.flip();

            Font font = new Font(data.limit());
            font.buffer.put(data);
            font.buffer.flip();

            MemoryUtil.memFree(data);

            return font;
        } finally {
            in.close();
        }
    }
}
