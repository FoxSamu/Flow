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

import java.io.File;

import net.shadew.flow.util.NativeImage;

public class JpgSink implements Sink {
    private final String filename;
    private final int quality;

    public JpgSink(String filename, int quality) {
        this.filename = filename;
        this.quality = quality;
    }

    @Override
    public void init(int fps) {
        new File(filename).getParentFile().mkdirs();
    }

    @Override
    public void flushBuffer(long frameNumber, NativeImage buffer) {
        buffer.saveJpg(String.format(filename, frameNumber), true, quality);
    }

    @Override
    public void cleanup() {

    }
}
