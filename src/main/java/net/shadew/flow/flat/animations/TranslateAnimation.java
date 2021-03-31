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

import net.shadew.flow.flat.Node2D;

public class TranslateAnimation extends TransformAnimation {
    private float x;
    private float y;

    public TranslateAnimation(Node2D node) {
        super(node);
    }

    public TranslateAnimation offset(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    protected void applyTransform(Matrix3x2f matrix, float t) {
        matrix.translate(x * t, y * t);
    }
}
