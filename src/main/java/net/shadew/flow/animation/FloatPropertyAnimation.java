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

public class FloatPropertyAnimation extends Animation {
    private final Getter getter;
    private final Setter setter;

    private float start;
    private float target;

    public FloatPropertyAnimation(Getter getter, Setter setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public FloatPropertyAnimation target(float value) {
        target = value;
        return this;
    }

    public float getTarget() {
        return target;
    }

    @Override
    protected void preApply() {

    }

    @Override
    protected void apply(double t) {
        start = getter.get();
        setter.set(MathUtil.lerp(start, target, (float) t));
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
        float get();
    }

    public interface Setter {
        void set(float v);
    }
}
