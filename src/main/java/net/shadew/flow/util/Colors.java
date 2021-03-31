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

package net.shadew.flow.util;

import org.lwjgl.nanovg.NVGColor;

import net.shadew.util.misc.ColorUtil;

public class Colors {
    public static NVGColor transparent(NVGColor color) {
        return color.r(0).g(0).b(0).a(0);
    }

    public static NVGColor rgba(NVGColor color, float r, float g, float b, float a) {
        return color.r(r).g(g).b(b).a(a);
    }

    public static NVGColor rgb(NVGColor color, float r, float g, float b) {
        return rgba(color, r, g, b, 1);
    }

    public static NVGColor rgba(NVGColor color, double r, double g, double b, double a) {
        return rgba(color, (float) r, (float) g, (float) b, (float) a);
    }

    public static NVGColor rgb(NVGColor color, double r, double g, double b) {
        return rgba(color, r, g, b, 1);
    }

    public static NVGColor rgba(NVGColor color, int argb) {
        return rgba(color, ColorUtil.redf(argb), ColorUtil.greenf(argb), ColorUtil.bluef(argb), ColorUtil.alphaf(argb));
    }

    public static NVGColor rgb(NVGColor color, int rgb) {
        return rgb(color, ColorUtil.redf(rgb), ColorUtil.greenf(rgb), ColorUtil.bluef(rgb));
    }

    public static NVGColor rgba(NVGColor color, int rgb, float a) {
        return rgba(color, ColorUtil.redf(rgb), ColorUtil.greenf(rgb), ColorUtil.bluef(rgb), a);
    }

    public static NVGColor rgba(NVGColor color, int rgb, double a) {
        return rgba(color, ColorUtil.redf(rgb), ColorUtil.greenf(rgb), ColorUtil.bluef(rgb), (float) a);
    }

    public static NVGColor hsva(NVGColor color, double h, double s, double v, double a) {
        return rgba(color, ColorUtil.hsva(h, s, v, a));
    }

    public static NVGColor hsv(NVGColor color, double h, double s, double v) {
        return rgba(color, ColorUtil.hsva(h, s, v));
    }

    public static NVGColor hsva(NVGColor color, float h, float s, float v, float a) {
        return rgba(color, ColorUtil.hsva(h, s, v, a));
    }

    public static NVGColor hsv(NVGColor color, float h, float s, float v) {
        return rgba(color, ColorUtil.hsva(h, s, v));
    }

    public static NVGColor hsla(NVGColor color, double h, double s, double l, double a) {
        return rgba(color, ColorUtil.hsla(h, s, l, a));
    }

    public static NVGColor hsl(NVGColor color, double h, double s, double l) {
        return rgba(color, ColorUtil.hsla(h, s, l));
    }

    public static NVGColor hsla(NVGColor color, float h, float s, float l, float a) {
        return rgba(color, ColorUtil.hsla(h, s, l, a));
    }

    public static NVGColor hsl(NVGColor color, float h, float s, float l) {
        return rgba(color, ColorUtil.hsla(h, s, l));
    }
}
