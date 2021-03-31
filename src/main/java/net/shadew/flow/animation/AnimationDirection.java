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

/**
 * A direction function of an animation. This function transforms the animation time forward or backward, or in any
 * complex direction that is desired. This interface is functional and can be implemented with a lambda, but a few basic
 * animation directions are provided as constants of this interface.
 */
public interface AnimationDirection {

    /**
     * Plays the animation forward at linear speed.
     */
    AnimationDirection FORWARD = t -> t;

    /**
     * Plays the animation backward at linear speed.
     */
    AnimationDirection BACKWARD = t -> 1 - t;

    /**
     * Plays the animation forward at linear speed, but twice as fast, and then backward at linear speed, again twice as
     * fast.
     */
    AnimationDirection FORWARD_BACKWARD = t -> t < 0.5 ? t * 2 : 1 - (t - 0.5) * 2;

    /**
     * Applies the animation direction by transforming the animation progress in any desired direction.
     *
     * @param t The animation progress (without easing), as a number between 0 and 1
     * @return The transformed animation progress, as a number between 0 and 1
     */
    double applyDirection(double t);
}
