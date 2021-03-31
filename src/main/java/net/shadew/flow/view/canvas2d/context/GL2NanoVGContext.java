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

import org.lwjgl.nanovg.NanoVGGL2;

public class GL2NanoVGContext extends NanoVGContext {
    public GL2NanoVGContext(int flags) {
        super(flags);
    }

    @Override
    protected void disposeContext(long nvg) {
        NanoVGGL2.nvgDelete(nvg);
    }

    @Override
    protected long createContext(int flags) {
        return NanoVGGL2.nvgCreate(flags);
    }
}
