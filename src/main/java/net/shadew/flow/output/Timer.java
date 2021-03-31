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

public class Timer {
    private long startFrame;
    private long endFrame;
    private long frame;
    private boolean paused;
    private float frameOffset;

    public void setRange(long startFrame, long endFrame) {
        this.startFrame = startFrame;
        this.endFrame = endFrame;
    }

    public void setInfinitelyFrom(long startFrame) {
        this.startFrame = startFrame;
        this.endFrame = startFrame - 1;
    }

    public long getFrame() {
        return frame;
    }

    public float getFrameOffset() {
        return frameOffset;
    }

    public long getStartFrame() {
        return startFrame;
    }

    public long getEndFrame() {
        return endFrame;
    }

    public void nextFrame() {
        if (frame < startFrame) {
            frame = startFrame;
            frameOffset = 0;
        }
        if (!paused) {
            frame++;
            frameOffset = 0;
        }
    }

    public double getTime(int fps) {
        return (double) (frame + frameOffset) / fps;
    }

    public boolean isPaused() {
        return paused;
    }

    public void pause() {
        paused = true;
    }

    public void unpause() {
        paused = false;
    }

    public void replay() {
        frame = startFrame;
    }

    public boolean shouldContinue() {
        if (endFrame <= startFrame) {
            return false;
        }
        return frame < endFrame;
    }

    public void setFrame(long frame, float off) {
        if (frame < startFrame) {
            this.frame = startFrame;
            this.frameOffset = 0;
        } else {
            this.frame = frame;
            this.frameOffset = off;
        }
    }
}
