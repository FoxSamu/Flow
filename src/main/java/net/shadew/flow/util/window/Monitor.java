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

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;

public class Monitor {
    final long pointer;

    Monitor(long pointer) {
        this.pointer = pointer;
    }

    public String getName() {
        return glfwGetMonitorName(pointer);
    }

    public float getContentScaleX() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buf = stack.mallocFloat(1);
            glfwGetMonitorContentScale(pointer, buf, null);
            return buf.get(0);
        }
    }

    public float getContentScaleY() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buf = stack.mallocFloat(1);
            glfwGetMonitorContentScale(pointer, null, buf);
            return buf.get(0);
        }
    }

    public int getPhysX() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer xBuf = stack.mallocInt(1);
            glfwGetMonitorPos(pointer, xBuf, null);
            return xBuf.get(0);
        }
    }

    public int getPhysY() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer yBuf = stack.mallocInt(1);
            glfwGetMonitorPos(pointer, null, yBuf);
            return yBuf.get(0);
        }
    }

    public int getPhysWidth() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer wBuf = stack.mallocInt(1);
            glfwGetMonitorPhysicalSize(pointer, wBuf, null);
            return wBuf.get(0);
        }
    }

    public int getPhysHeight() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer hBuf = stack.mallocInt(1);
            glfwGetMonitorPhysicalSize(pointer, null, hBuf);
            return hBuf.get(0);
        }
    }

    public int getAreaX() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer xBuf = stack.mallocInt(1);
            glfwGetMonitorWorkarea(pointer, xBuf, null, null, null);
            return xBuf.get(0);
        }
    }

    public int getAreaY() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer yBuf = stack.mallocInt(1);
            glfwGetMonitorWorkarea(pointer, null, yBuf, null, null);
            return yBuf.get(0);
        }
    }

    public int getAreaWidth() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer wBuf = stack.mallocInt(1);
            glfwGetMonitorWorkarea(pointer, null, null, wBuf, null);
            return wBuf.get(0);
        }
    }

    public int getAreaHeight() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer hBuf = stack.mallocInt(1);
            glfwGetMonitorWorkarea(pointer, null, null, null, hBuf);
            return hBuf.get(0);
        }
    }

    public static Monitor getPrimary() {
        return new Monitor(glfwGetPrimaryMonitor());
    }

    public static List<Monitor> getMonitors() {
        List<Monitor> monitors = new ArrayList<>();
        PointerBuffer buf = glfwGetMonitors();

        if (buf == null) {
            return monitors;
        }

        while (buf.hasRemaining()) {
            monitors.add(new Monitor(buf.get()));
        }

        return monitors;
    }

    public static Monitor forPointer(long pointer) {
        return new Monitor(pointer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Monitor monitor = (Monitor) o;
        return pointer == monitor.pointer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointer);
    }
}
