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

package net.shadew.flow.view.canvas2d.context;

import org.lwjgl.nanovg.NanoVG;

import net.shadew.flow.util.Disposable;

public interface Texture extends Disposable {
    int GENERATE_MIPMAPS = NanoVG.NVG_IMAGE_GENERATE_MIPMAPS;
    int REPEAT_X = NanoVG.NVG_IMAGE_REPEATX;
    int REPEAT_Y = NanoVG.NVG_IMAGE_REPEATY;
    int FLIP_VERTICAL = NanoVG.NVG_IMAGE_FLIPY;
    int NEAREST = NanoVG.NVG_IMAGE_NEAREST;
}
