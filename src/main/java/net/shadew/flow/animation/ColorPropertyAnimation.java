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

import net.shadew.util.misc.ColorUtil;
import net.shadew.util.misc.MathUtil;

public class ColorPropertyAnimation extends Animation {
    private final Getter getter;
    private final Setter setter;

    private int start;
    private int target;

    public ColorPropertyAnimation(Getter getter, Setter setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public ColorPropertyAnimation target(int value) {
        target = value;
        return this;
    }

    public int getTarget() {
        return target;
    }

    @Override
    protected void preApply() {

    }

    @Override
    protected void apply(double t) {
        start = getter.get();
        setter.set(ColorUtil.interpolate(start, target, MathUtil.clamp((float) t, 0, 1)));
    }

    @Override
    protected void postApply() {
        start = getter.get();
        setter.set(target);
    }

    @Override
    protected void preUnapply() {

    }

    @Override
    protected void unapply(double t) {
        setter.set(start);
    }

    @Override
    protected void postUnapply() {
        setter.set(start);
    }

    public interface Getter {
        int get();
    }

    public interface Setter {
        void set(int v);
    }
}
