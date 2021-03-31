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

class EasingUtil {
    static double quad(double t) {
        return t * t;
    }

    static double cub(double t) {
        return t * t * t;
    }

    static double quart(double t) {
        return t * t * t * t;
    }

    static double quin(double t) {
        return t * t * t * t * t;
    }

    static double quadBezier(double a, double c, double b, double t) {
        return MathUtil.lerp(MathUtil.lerp(a, c, t), MathUtil.lerp(c, b, t), t);
    }

    static double cubicBezier(double a, double c1, double c2, double b, double t) {
        return quadBezier(MathUtil.lerp(a, c1, t), MathUtil.lerp(c1, c2, t), MathUtil.lerp(c2, b, t), t);
    }


    // Cardano's algorithm

    private static double accept(double t) {
        return t >= 0 && t <= 1 ? t : -1;
    }

    private static double cbrt(double v) {
        if (v < 0)
            return -Math.cbrt(-v);
        else
            return Math.cbrt(v);
    }

    static double getFirstRoot(double pa, double pb, double pc, double pd) {
        double // Polynomial coordinates
            a = 3 * pa - 6 * pb + 3 * pc,
            b = -3 * pa + 3 * pb,
            c = pa,
            d = -pa + 3 * pb - 3 * pc + pd;

        // check: are we actually a cubic curve?
        if (d == 0) {
            // not a cubic curve, are we quadratic?
            if (a == 0) {
                // neither a quadratic curve, are we linear?
                if (b == 0) {
                    // neither linear, there's no solution
                    return -1;
                }

                // we're linear
                return accept(-c / b);
            }

            // we're quadratic
            double q = Math.sqrt(b * b - 4 * a * c), a2 = 2 * a;
            double x = accept((q - b) / a2);
            if (x < 0) x = accept((-b - q) / a2);
            return x;
        }

        // we're cubic
        a /= d;
        b /= d;
        c /= d;

        double
            p = (3 * b - a * a) / 3,
            p3 = p / 3,
            q = (2 * a * a * a - 9 * a * b + 27 * c) / 27,
            q2 = q / 2,
            discriminant = q2 * q2 + p3 * p3 * p3;

        // three roots, find first one on range 0-1
        if (discriminant < 0) {
            double mp3 = -p3;
            double mp33 = mp3 * mp3 * mp3;
            double r = Math.sqrt(mp33);
            double t = -q / (2 * r);
            double cosphi = MathUtil.clamp(t, -1, 1);
            double phi = Math.acos(cosphi);
            double crtr = cbrt(r);
            double t1 = 2 * crtr;

            double x = accept(t1 * Math.cos(phi / 3) - a / 3);
            if (x < 0) x = accept(t1 * Math.cos((phi + 2 * Math.PI) / 3) - a / 3);
            if (x < 0) x = accept(t1 * Math.cos((phi + 4 * Math.PI) / 3) - a / 3);
            return x;
        }

        // two roots: find first one on range 0-1
        if (discriminant == 0) {
            double u1 = q2 < 0 ? cbrt(-q2) : -cbrt(q2);
            double x = accept(2 * u1 - a / 3);
            if (x < 0) x = accept(-u1 - a / 3);
            return x;
        }

        // one root: return if on range 0-1
        double sd = Math.sqrt(discriminant);
        double u1 = cbrt(sd - q2);
        double v1 = cbrt(sd + q2);

        return accept(u1 - v1 - a / 3);
    }
}
