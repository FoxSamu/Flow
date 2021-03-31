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

/**
 * An easing of an animation. By default, animations are played using linear interpolation. An easing transforms the
 * animation progress in a non-linear way to make the animation more smooth and thus more natural. This interface is
 * functional and can be implemented using a lambda, but a few presets exist as constant fields of this interface.
 */
public interface Easing {

    /**
     * The linear easing function, also known as the identity easing function. This is the default easing function which
     * does not transform time.
     *
     * <pre>E(t) = t</pre>
     */
    Easing LINEAR = t -> t;

    /**
     * A sinusoidal easing function with smooth start and end.
     */
    Easing SINE_IN_OUT = t -> (1 - Math.cos(t * Math.PI)) / 2;

    /**
     * A quadratically (t<sup>2</sup>) biased easing function with smooth start and end.
     */
    Easing QUAD_IN_OUT = t -> t < 0.5 ? EasingUtil.quad(t * 2) / 2 : 1 - EasingUtil.quad((1 - t) * 2) / 2;

    /**
     * A cubically (t<sup>3</sup>) biased easing function with smooth start and end.
     */
    Easing CUBIC_IN_OUT = t -> t < 0.5 ? EasingUtil.cub(t * 2) / 2 : 1 - EasingUtil.cub((1 - t) * 2) / 2;

    /**
     * A quartically (t<sup>4</sup>) biased easing function with smooth start and end.
     */
    Easing QUART_IN_OUT = t -> t < 0.5 ? EasingUtil.quart(t * 2) / 2 : 1 - EasingUtil.quart((1 - t) * 2) / 2;

    /**
     * A quintically (t<sup>5</sup>) biased easing function with smooth start and end.
     */
    Easing QUIN_IN_OUT = t -> t < 0.5 ? EasingUtil.quin(t * 2) / 2 : 1 - EasingUtil.quin((1 - t) * 2) / 2;

    /**
     * A sinusoidal easing function with instant start and smooth end.
     */
    Easing SINE_OUT = t -> Math.sin(t * Math.PI / 2);

    /**
     * A quadratically (t<sup>2</sup>) biased easing function with instant start and smooth end.
     */
    Easing QUAD_OUT = t -> 1 - EasingUtil.quad(1 - t);

    /**
     * A cubically (t<sup>3</sup>) biased easing function with instant start and smooth end.
     */
    Easing CUBIC_OUT = t -> 1 - EasingUtil.cub(1 - t);

    /**
     * A quartically (t<sup>4</sup>) biased easing function with instant start and smooth end.
     */
    Easing QUART_OUT = t -> 1 - EasingUtil.quart(1 - t);

    /**
     * A quintically (t<sup>5</sup>) biased easing function with instant start and smooth end.
     */
    Easing QUIN_OUT = t -> 1 - EasingUtil.quin(1 - t);

    /**
     * A sinusoidal easing function with smooth start and instant end.
     */
    Easing SINE_IN = t -> 1 - Math.cos(t * Math.PI / 2);

    /**
     * A quadratically (t<sup>2</sup>) biased easing function with smooth start and instant end.
     */
    Easing QUAD_IN = EasingUtil::quad;

    /**
     * A cubically (t<sup>3</sup>) biased easing function with smooth start and instant end.
     */
    Easing CUBIC_IN = EasingUtil::cub;

    /**
     * A quartically (t<sup>4</sup>) biased easing function with smooth start and instant end.
     */
    Easing QUART_IN = EasingUtil::quart;

    /**
     * A quintically (t<sup>5</sup>) biased easing function with smooth start and instant end.
     */
    Easing QUIN_IN = EasingUtil::quin;

    /**
     * The easing function matching the <code>ease</code> easing in CSS.
     */
    Easing EASE = cubicBezier2D(.25, .1, .25, 1);

    /**
     * The easing function matching the <code>ease-in</code> easing in CSS.
     */
    Easing EASE_IN = cubicBezier2D(.42, 0, 1, 1);

    /**
     * The easing function matching the <code>ease-out</code> easing in CSS.
     */
    Easing EASE_OUT = cubicBezier2D(0, 0, .58, 1);

    /**
     * The easing function matching the <code>ease-in-out</code> easing in CSS.
     */
    Easing EASE_IN_OUT = cubicBezier2D(.42, 0, .58, 1);

    /**
     * Morphs the animation progress using an easing curve.
     *
     * @param t The linear animation progress as a number between 0 and 1, probably transformed by an {@link
     *          AnimationDirection} function
     * @return The eased animation progress as a number between 0 and 1. Any number outside this range is allowed and
     *     will cause the animation to extrapolate (where possible).
     */
    double ease(double t);

    /**
     * Creates an easing function from a 1D cubic bezier between 0 and 1, using the given two control points.
     *
     * @param cp1 The first control point
     * @param cp2 The second control point
     * @return An easing function from a 1D cubic bezier
     */
    static Easing cubicBezier1D(double cp1, double cp2) {
        return t -> EasingUtil.cubicBezier(0, cp1, cp2, 1, t);
    }


    /**
     * Creates an easing function from a 2D cubic bezier between 0,0 and 1,1, using the given two control points. The
     * curve defined by the given control points maps X-coordinates (linear time) to Y-coordinates (eased time) by means
     * of cubic rootfinding.
     *
     * This method replicates the <code>cubic-bezier</code> function in CSS, you can generate a curve at
     * <a href="https://cubic-bezier.com/">https://cubic-bezier.com/</a> and directly copy its inputs as parameters of
     * this method.
     *
     * @param cp1x The first control point X, will be clamped to a number between 0 and 1
     * @param cp1y The first control point Y
     * @param cp2x The second control point X, will be clamped to a number between 0 and 1
     * @param cp2y The second control point Y
     * @return An easing function from a 2D cubic bezier
     */
    static Easing cubicBezier2D(double cp1x, double cp1y, double cp2x, double cp2y) {
        double c1x = MathUtil.clamp(cp1x, 0, 1);
        double c2x = MathUtil.clamp(cp2x, 0, 1);

        return t -> {
            t = MathUtil.clamp(t, 0, 1);
            double xa = -t;
            double xb = c1x - t;
            double xc = c2x - t;
            double xd = 1 - t;

            double rt = EasingUtil.getFirstRoot(xa, xb, xc, xd);
            return EasingUtil.cubicBezier(0, cp1y, cp2y, 1, rt);
        };
    }

    /**
     * Converts an in-easing function to an out-easing function and vice versa
     *
     * @param fn The easing function to flip
     * @return The flipped easing function
     */
    static Easing flip(Easing fn) {
        return t -> 1 - fn.ease(1 - t);
    }

    /**
     * Converts an in-easing function to an in-out-easing function and an out-easing function to an out-in-easing
     * function
     *
     * @param fn The easing function to make in-out or out-in
     * @return The in-out or out-in easing function
     */
    static Easing toInOut(Easing fn) {
        return t -> t < 0.5 ? fn.ease(t * 2) / 2 : 1 - fn.ease(t * 2) / 2;
    }
}
