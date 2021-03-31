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

import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

import net.shadew.flow.util.NativeImage;

import static org.jcodec.common.Codec.H264;
import static org.jcodec.common.Format.*;

public class MP4Sink implements Sink {
    private final String filename;

    private SequenceEncoder encoder;

    public MP4Sink(String filename) {
        this.filename = filename;
    }

    @Override
    public void init(int fps) {
        new File(filename).getParentFile().mkdirs();
        try {
            encoder = new SequenceEncoder(NIOUtils.writableChannel(new File(filename)), Rational.R(fps, 1), MOV, H264, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void flushBuffer(long frameNumber, NativeImage buffer) {
        int w = buffer.getWidth();
        int h = buffer.getHeight();
        ByteBuffer buf = buffer.getBuffer();

        byte[] rgbs = new byte[w * h * 3];
        for (int i = 0; i < h; i++) {
            int bi = i * w;
            int ri = (h - i - 1) * w;

            for (int j = 0; j < w; j++) {
                int c = buf.getInt((j + bi) * 4);

                int d = (j + ri) * 3;
                rgbs[d] = (byte) ((c >>> 0) - 128);
                rgbs[d + 1] = (byte) ((c >>> 8) - 128);
                rgbs[d + 2] = (byte) ((c >>> 16) - 128);
            }
        }

        Picture pic = Picture.createPicture(w, h, new byte[][] {rgbs}, ColorSpace.RGB);

        try {
            encoder.encodeNativeFrame(pic);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void cleanup() {
        try {
            encoder.finish();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
