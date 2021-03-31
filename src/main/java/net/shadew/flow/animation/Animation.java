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

package net.shadew.flow.animation;

import net.shadew.util.misc.MathUtil;

public abstract class Animation extends Updater {
    public static final int REPEAT_INFINITE = 0;
    public static final int REPEAT_OFF = 1;

    private double start = 0;
    private double end = 0;
    private int repeat = REPEAT_OFF;
    private AnimationDirection direction = AnimationDirection.FORWARD;
    private Easing easing = Easing.LINEAR;

    public Animation start(double s) {
        start = s;
        end = Math.max(start, end);
        return this;
    }

    public Animation end(double e) {
        end = e;
        start = Math.min(start, end);
        return this;
    }

    public Animation duration(double d) {
        if (d < 0) {
            end = start;
            start = end - d;
        } else {
            end = start + d;
        }
        return this;
    }

    public double getStart() {
        return start;
    }

    public double getEnd() {
        return end;
    }

    public double getDuration() {
        return end - start;
    }

    public Animation repeat(int r) {
        if (r < 0) r = REPEAT_INFINITE;
        repeat = r;
        return this;
    }

    public int getRepeat() {
        return repeat;
    }

    public double getRepeatedEnd() {
        if (start == end) {
            return start;
        }
        if (repeat == REPEAT_INFINITE) {
            return Double.POSITIVE_INFINITY;
        }
        return start + getRepeatedDuration();
    }

    public double getRepeatedDuration() {
        if (start == end) {
            return 0;
        }
        if (repeat == REPEAT_INFINITE) {
            return Double.POSITIVE_INFINITY;
        }
        return getDuration() * repeat;
    }

    public Animation direction(AnimationDirection dir) {
        if (dir == null) dir = AnimationDirection.FORWARD;
        direction = dir;
        return this;
    }

    public AnimationDirection getDirection() {
        return direction;
    }

    public Animation easing(Easing fn) {
        if (fn == null) fn = Easing.LINEAR;
        easing = fn;
        return this;
    }

    public Easing getEasing() {
        return easing;
    }

    @Override
    public void update(double time) {
        if (start == end) {
            if (time < start) {
                preApply();
            } else {
                postApply();
            }
        } else {
            double t = MathUtil.unlerp(start, end, time);

            if (t < 0) {
                preApply();
            } else {
                if (repeat == REPEAT_INFINITE || t < repeat) {
                    apply(easing.ease(direction.applyDirection(t % 1)));
                } else {
                    postApply();
                }
            }
        }
    }

    @Override
    public void postUpdate(double time) {
        if (start == end) {
            if (time < start) {
                preUnapply();
            } else {
                postUnapply();
            }
        } else {
            double t = MathUtil.unlerp(start, end, time);

            if (t < 0) {
                preUnapply();
            } else {
                if (repeat == REPEAT_INFINITE || t < repeat) {
                    unapply(easing.ease(direction.applyDirection(t % 1)));
                } else {
                    postUnapply();
                }
            }
        }
    }

    protected abstract void preApply();
    protected abstract void apply(double t);
    protected abstract void postApply();

    protected abstract void preUnapply();
    protected abstract void unapply(double t);
    protected abstract void postUnapply();
}
