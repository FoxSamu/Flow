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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {
    private static final Logger LOGGER = LogManager.getLogger();

    private final long handle;
    private boolean disposed;

    public Window(int w, int h, String title) {
        this.handle = glfwCreateWindow(w, h, title, NULL, NULL);
        if (handle == NULL)
            throw new RuntimeException("Failed to create GLFW window");
    }

    public Window(int w, int h) {
        this(w, h, "");
    }


    public void setPos(int x, int y) {
        glfwSetWindowPos(handle, x, y);
    }

    public void setSize(int w, int h) {
        glfwSetWindowSize(handle, w, h);
    }

    public void setTitle(String title) {
        glfwSetWindowTitle(handle, title);
    }

    public void setCursorPos(double x, double y) {
        glfwSetCursorPos(handle, x, y);
    }

    public void show() {
        glfwShowWindow(handle);
    }

    public void hide() {
        glfwHideWindow(handle);
    }

    public void setVisible(boolean visible) {
        if (visible) show();
        else hide();
    }

    public int getWidth() {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            IntBuffer width = mem.mallocInt(1);
            glfwGetWindowSize(handle, width, null);
            return width.get(0);
        }
    }

    public int getHeight() {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            IntBuffer height = mem.mallocInt(1);
            glfwGetWindowSize(handle, null, height);
            return height.get(0);
        }
    }

    public int getViewportWidth() {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            IntBuffer width = mem.mallocInt(1);
            glfwGetFramebufferSize(handle, width, null);
            return width.get(0);
        }
    }

    public int getViewportHeight() {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            IntBuffer height = mem.mallocInt(1);
            glfwGetFramebufferSize(handle, null, height);
            return height.get(0);
        }
    }

    public int getX() {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            IntBuffer x = mem.mallocInt(1);
            glfwGetWindowPos(handle, x, null);
            return x.get(0);
        }
    }

    public int getY() {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            IntBuffer y = mem.mallocInt(1);
            glfwGetWindowPos(handle, null, y);
            return y.get(0);
        }
    }

    public boolean isVisible() {
        return glfwGetWindowAttrib(handle, GLFW_VISIBLE) == GLFW_TRUE;
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public void close() {
        glfwSetWindowShouldClose(handle, true);
    }

    public void setClose(boolean close) {
        glfwSetWindowShouldClose(handle, close);
    }

    public void grabContext() {
        glfwMakeContextCurrent(handle);
    }

    public void swapBuffers() {
        glfwSwapBuffers(handle);
    }

    public void updateGlViewport() {
        GL11.glViewport(0, 0, getViewportWidth(), getViewportHeight());
    }

    public float getAspectRatio(boolean vertical) {
        float h = vertical ? getViewportHeight() : getViewportWidth();
        float v = vertical ? getViewportWidth() : getViewportHeight();
        return h / v;
    }

    public void dispose() {
        disposed = true;
        glfwDestroyWindow(handle);
    }

    public boolean isDisposed() {
        return disposed;
    }

    public void setAttrib(int attrib, int value) {
        glfwSetWindowAttrib(handle, attrib, value);
    }

    public void setAttrib(int attrib, boolean value) {
        glfwSetWindowAttrib(handle, attrib, value ? GLFW_TRUE : GLFW_FALSE);
    }

    public int getAttrib(int attrib) {
        return glfwGetWindowAttrib(handle, attrib);
    }

    public boolean getFlagAttrib(int attrib) {
        return glfwGetWindowAttrib(handle, attrib) == GLFW_TRUE;
    }

    public void setOpacity(float opac) {
        glfwSetWindowOpacity(handle, opac);
    }

    public void setSizeLimits(int minW, int minH, int maxW, int maxH) {
        glfwSetWindowSizeLimits(handle, minW, minH, maxW, maxH);
    }

    public void setResizable(boolean resizable) {
        setAttrib(GLFW_RESIZABLE, resizable);
    }

    public void setFloating(boolean floating) {
        setAttrib(GLFW_FLOATING, floating);
    }

    public void setAutoIconify(boolean autoIconify) {
        setAttrib(GLFW_AUTO_ICONIFY, autoIconify);
    }

    public void setFocusOnShow(boolean focusOnShow) {
        setAttrib(GLFW_FOCUS_ON_SHOW, focusOnShow);
    }

    public void setDecorated(boolean focusOnShow) {
        setAttrib(GLFW_DECORATED, focusOnShow);
    }

    public void setSamples(int samples) {
        setAttrib(GLFW_SAMPLES, samples);
    }

    public boolean isResizable() {
        return getFlagAttrib(GLFW_RESIZABLE);
    }

    public boolean isFloating() {
        return getFlagAttrib(GLFW_FLOATING);
    }

    public boolean isAutoIconify() {
        return getFlagAttrib(GLFW_AUTO_ICONIFY);
    }

    public boolean doesFocusOnShow() {
        return getFlagAttrib(GLFW_FOCUS_ON_SHOW);
    }

    public boolean isFocused() {
        return getFlagAttrib(GLFW_FOCUSED);
    }

    public boolean isIconified() {
        return getFlagAttrib(GLFW_ICONIFIED);
    }

    public boolean isMaximized() {
        return getFlagAttrib(GLFW_MAXIMIZED);
    }

    public boolean isHovered() {
        return getFlagAttrib(GLFW_HOVERED);
    }

    public boolean isDecorated() {
        return getFlagAttrib(GLFW_DECORATED);
    }

    public int getSamples() {
        return getAttrib(GLFW_SAMPLES);
    }

    public boolean hasTransparentFramebuffer() {
        return getFlagAttrib(GLFW_TRANSPARENT_FRAMEBUFFER);
    }

    public void focus() {
        glfwFocusWindow(handle);
    }

    public void setKeyCallback(GLFWKeyCallbackI callback) {
        glfwSetKeyCallback(handle, callback);
    }

    public void setMouseButtonCallback(GLFWMouseButtonCallbackI callback) {
        glfwSetMouseButtonCallback(handle, callback);
    }

    public void setScrollCallback(GLFWScrollCallbackI callback) {
        glfwSetScrollCallback(handle, callback);
    }

    public void setMouseMoveCallback(GLFWCursorPosCallbackI callback) {
        glfwSetCursorPosCallback(handle, callback);
    }

    public void setMouseEnterCallback(GLFWCursorEnterCallbackI callback) {
        glfwSetCursorEnterCallback(handle, callback);
    }

    public void setCharCallback(GLFWCharCallbackI callback) {
        glfwSetCharCallback(handle, callback);
    }

    public void setFileDropCallback(GLFWDropCallbackI callback) {
        glfwSetDropCallback(handle, callback);
    }

    public void setFramebufferSizeCallback(GLFWFramebufferSizeCallbackI callback) {
        glfwSetFramebufferSizeCallback(handle, callback);
    }

    public void setCloseCallback(GLFWWindowCloseCallbackI callback) {
        glfwSetWindowCloseCallback(handle, callback);
    }

    public void setFocusCallback(GLFWWindowFocusCallbackI callback) {
        glfwSetWindowFocusCallback(handle, callback);
    }

    public void setIconifyCallback(GLFWWindowIconifyCallbackI callback) {
        glfwSetWindowIconifyCallback(handle, callback);
    }

    public void setMaximizeCallback(GLFWWindowMaximizeCallbackI callback) {
        glfwSetWindowMaximizeCallback(handle, callback);
    }

    public void setPositionCallback(GLFWWindowPosCallbackI callback) {
        glfwSetWindowPosCallback(handle, callback);
    }

    public void setRefreshCallback(GLFWWindowRefreshCallbackI callback) {
        glfwSetWindowRefreshCallback(handle, callback);
    }

    public void setResizeCallback(GLFWWindowSizeCallbackI callback) {
        glfwSetWindowSizeCallback(handle, callback);
    }

    public void setContentScaleCallback(GLFWWindowContentScaleCallbackI callback) {
        glfwSetWindowContentScaleCallback(handle, callback);
    }

    public boolean isKeyDown(int key) {
        return glfwGetKey(handle, key) == GLFW_PRESS;
    }

    public boolean isMouseDown(int button) {
        return glfwGetMouseButton(handle, button) == GLFW_PRESS;
    }

    public boolean isMouseOver() {
        return true;
    }

    public double getMouseX() {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            DoubleBuffer out = mem.mallocDouble(1);
            glfwGetCursorPos(handle, out, null);
            return out.get(0);
        }
    }

    public double getMouseY() {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            DoubleBuffer out = mem.mallocDouble(1);
            glfwGetCursorPos(handle, null, out);
            return out.get(0);
        }
    }

    public void setCursor(Cursor cursor) {
        glfwSetCursor(handle, cursor.getHandle());
    }

    public void setCursorMode(int mode) {
        glfwSetInputMode(handle, GLFW_CURSOR, mode);
    }

    public Monitor getFullScreenMonitor() {
        long monitor = glfwGetWindowMonitor(handle);
        return monitor == NULL ? null : new Monitor(monitor);
    }

    public void setFullScreenMonitor(Monitor monitor) {
        if (monitor == null) {
            glfwSetWindowMonitor(handle, NULL, getX(), getY(), getWidth(), getHeight(), GLFW_DONT_CARE);
            return;
        }
        int x = monitor.getAreaX();
        int y = monitor.getAreaX();
        int w = monitor.getAreaWidth();
        int h = monitor.getAreaHeight();
        glfwSetWindowMonitor(handle, monitor.pointer, x, y, w, h, GLFW_DONT_CARE);
    }

    public void center(Monitor monitor) {
        int mx = monitor.getAreaX();
        int my = monitor.getAreaY();
        int mw = monitor.getAreaWidth();
        int mh = monitor.getAreaHeight();
        int ww = getWidth();
        int wh = getHeight();

        int cx = mx + mw / 2;
        int cy = my + mh / 2;
        int wx = cx - ww / 2;
        int wy = cy - wh / 2;

        setPos(wx, wy);
    }

    public static void setJoystickCallback(GLFWJoystickCallbackI callback) {
        glfwSetJoystickCallback(callback);
    }

    public static void setMonitorCallback(GLFWMonitorCallbackI callback) {
        glfwSetMonitorCallback(callback);
    }

    public static void copyToClipboard(String str) {
        glfwSetClipboardString(NULL, str);
    }

    public static void swapInterval(int interval) {
        glfwSwapInterval(interval);
    }

    public static void pollEvents() {
        glfwPollEvents();
    }

    public static void initGLFW() {
        GLFWErrorCallback.create(new GLFWErrorCallback() {
            @Override
            public void invoke(int error, long description) {
                String msg = getDescription(description);

                LOGGER.error("GLFW error {}", error);
                LOGGER.error("  Description: " + msg);
                LOGGER.error("  Stacktrace:");
                StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                for (int i = 4; i < stack.length; i++) {
                    LOGGER.error("    {}", stack[i].toString());
                }
            }
        }).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        revertConfig();
        setDefaultVisible(false);
        setDefaultResizable(true);
        setOpenGLVersion("3.2");
        setDefaultSamples(16);
    }

    public static void setDefaultResizable(boolean resizable) {
        configure(GLFW_RESIZABLE, resizable);
    }

    public static void setDefaultVisible(boolean visible) {
        configure(GLFW_VISIBLE, visible);
    }

    public static void setDefaultSamples(int samples) {
        configure(GLFW_SAMPLES, samples);
    }

    public static void revertConfig() {
        glfwDefaultWindowHints();
    }

    public static void configure(int hint, int value) {
        glfwWindowHint(hint, value);
    }

    public static void configure(int hint, boolean value) {
        glfwWindowHint(hint, value ? GLFW_TRUE : GLFW_FALSE);
    }

    public static void configure(int hint, String value) {
        glfwWindowHintString(hint, value);
    }

    public static void setOpenGLVersion(String version) {
        int pt = version.indexOf('.');
        int maj, min;

        if (pt < 0) {
            maj = parse(version, version);
            min = 0;
        } else {
            maj = parse(version.substring(0, pt), version);
            min = parse(version.substring(pt + 1), version);
        }

        setOpenGLVersion(maj, min);
    }

    private static int parse(String ver, String full) {
        try {
            int i = Integer.parseInt(ver);
            if (i < 0) {
                throw new NumberFormatException();
            }
            return i;
        } catch (NumberFormatException exc) {
            throw new NumberFormatException("Invalid version: " + full);
        }
    }

    private static void setOpenGLVersion(int major, int minor) {
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, major);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, minor);
        if (major > 3 || major == 3 && minor >= 2) {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        } else {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_ANY_PROFILE);
        }
        if (major > 3) {
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        } else {
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_FALSE);
        }
    }

    public static void terminateGLFW() {
        glfwTerminate();
        Optional.ofNullable(glfwSetErrorCallback(null))
                .ifPresent(Callback::free);
    }
}
