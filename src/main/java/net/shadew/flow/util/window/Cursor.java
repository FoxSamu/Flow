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

package net.shadew.flow.util.window;

import org.lwjgl.glfw.GLFWImage;

import static org.lwjgl.glfw.GLFW.*;

public class Cursor {
    private final long handle;

    private Cursor(long handle) {
        this.handle = handle;
    }

    public void dispose() {
        glfwDestroyCursor(handle);
    }

    public long getHandle() {
        return handle;
    }

    public static Cursor create(int defaultType) {
        return new Cursor(glfwCreateStandardCursor(defaultType));
    }

    public static Cursor create(GLFWImage image, int xHot, int yHot) {
        return new Cursor(glfwCreateCursor(image, xHot, yHot));
    }
}
