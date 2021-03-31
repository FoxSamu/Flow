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

package net.shadew.flow.flat.animations;

import org.joml.Matrix3x2f;

import net.shadew.flow.animation.Animation;
import net.shadew.flow.flat.Node2D;

public abstract class TransformAnimation extends Animation {
    protected final Node2D node;
    private final Matrix3x2f matrix = new Matrix3x2f();

    public TransformAnimation(Node2D node) {
        this.node = node;
    }

    @Override
    protected void preApply() {

    }

    @Override
    protected void apply(double t) {
        node.pushTransform();
        node.getTransform(matrix);
        applyTransform(matrix, (float) t);
        node.setTransform(matrix);
    }

    @Override
    protected void postApply() {
        apply(1);
    }

    @Override
    protected void preUnapply() {

    }

    @Override
    protected void unapply(double t) {
        node.popTransform();
    }

    @Override
    protected void postUnapply() {
        unapply(1);
    }

    protected abstract void applyTransform(Matrix3x2f matrix, float t);
}
