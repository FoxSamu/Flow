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

import java.util.function.DoubleConsumer;

public class SimpleAnimation extends Animation {
    private final DoubleConsumer consumer;

    public SimpleAnimation(DoubleConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    protected void preApply() {

    }

    @Override
    protected void apply(double t) {
        consumer.accept(t);
    }

    @Override
    protected void postApply() {
        consumer.accept(getDirection().applyDirection(1));
    }

    @Override
    protected void preUnapply() {

    }

    @Override
    protected void unapply(double t) {

    }

    @Override
    protected void postUnapply() {

    }
}
