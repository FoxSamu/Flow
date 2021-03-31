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

import java.util.ArrayList;
import java.util.List;

public class Timeline {
    private final List<Updater> updaters = new ArrayList<>();

    public void append(Updater updater) {
        insert(animationCount(), updater);
    }

    public void prepend(Updater updater) {
        insert(0, updater);
    }

    public void insert(int index, Updater updater) {
        updaters.remove(updater); // Remove so we can move it to the desired position
        updaters.add(index, updater);
    }

    public boolean remove(Updater updater) {
        return updaters.remove(updater);
    }

    public int animationCount() {
        return updaters.size();
    }

    public boolean insertBefore(Updater target, Updater updater) {
        int index = updaters.indexOf(target);
        if (index < 0) {
            return false;
        }
        insert(index, updater);
        return true;
    }

    public boolean insertAfter(Updater target, Updater updater) {
        int index = updaters.indexOf(target);
        if (index < 0) {
            return false;
        }
        insert(index + 1, updater);
        return true;
    }

    public void runAnimations(double time) {
        for (Updater updater : updaters) {
            updater.update(time);
        }
    }

    public void cleanupAnimations(double time) {
        for (Updater updater : updaters) {
            updater.postUpdate(time);
        }
    }
}
