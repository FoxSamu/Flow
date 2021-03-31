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

package net.shadew.flow.flat;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;
import org.joml.Vector2fc;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.shadew.flow.util.Disposable;
import net.shadew.flow.view.Viewport;
import net.shadew.flow.view.canvas2d.context.CanvasContext;

public abstract class Node2D implements Disposable {

    @Override
    public void dispose() {

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName());
        if (name != null) {
            builder.append("#").append(name);
        }
        for (String tag : tags) {
            builder.append(".").append(tag);
        }
        return builder.toString();
    }

    public String toDetailedString() {
        StringBuilder builder = new StringBuilder(toString());
        builder.append(" [").append(System.lineSeparator());
        boolean[] del = {false};
        BiConsumer<String, String> propertyConsumer = (k, v) -> {
            if (del[0]) builder.append(",").append(System.lineSeparator());
            del[0] = true;
            builder.append("    ").append(k).append(": ").append(v);
        };
        addStringDetails(propertyConsumer);
        builder.append(System.lineSeparator()).append("]");
        return builder.toString();
    }

    protected void addStringDetails(BiConsumer<String, String> properties) {
        properties.accept("parent", parent + "");
        properties.accept("children", children + "");
        properties.accept("visibility", visibility + "");
    }

    //
    // IDENTIFICATION
    //

    private String name;
    private final Set<String> tags = new HashSet<>();
    private final Set<String> tagsImmutable = Collections.unmodifiableSet(tags);

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTags(String tag) {
        clearTags();
        addTags(tag);
    }

    public void addTags(String tag) {
        forEachTag(tag, tags::add);
    }

    public void removeTags(String tag) {
        forEachTag(tag, tags::remove);
    }

    public boolean hasTags(String tag) {
        boolean[] out = {true};
        forEachTag(tag, t -> out[0] &= tags.contains(t));
        return out[0];
    }

    public void clearTags() {
        tags.clear();
    }

    public Set<String> getTags() {
        return tagsImmutable;
    }

    public String getTagsAsString() {
        return String.join(" ", tags);
    }

    private static final Pattern TAG_SPLIT_PATTERN = Pattern.compile("\\s+");

    private static void forEachTag(String tags, Consumer<String> fn) {
        tags = tags.toLowerCase(Locale.ROOT);

        Matcher matcher = TAG_SPLIT_PATTERN.matcher(tags);

        int start = 0, end;
        while (matcher.find()) {
            end = matcher.start();

            if (end > start) {
                fn.accept(tags.substring(start, end));
            }

            start = matcher.end();
        }
    }

    private static Set<String> toTagSet(String tags) {
        Set<String> out = new HashSet<>();
        forEachTag(tags, out::add);
        return out;
    }


    //
    // VISIBILITY
    //

    public static final int HIDDEN = 0;
    public static final int SHOW_CHILDREN = 1;
    public static final int SHOW_SELF = 2;
    public static final int VISIBLE = SHOW_SELF | SHOW_CHILDREN;

    private int visibility = VISIBLE;

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public int getVisibility() {
        return visibility;
    }

    public void show() {
        visibility = VISIBLE;
    }

    public void hide() {
        visibility = HIDDEN;
    }


    //
    // RENDERING
    //

    public void render(CanvasContext ctx, Viewport vp, double time) {
        if (visibility != 0) {
            ctx.pushTransform();
            ctx.transform(transform);

            if ((visibility & SHOW_SELF) != 0) {
                renderNode(ctx, vp, time);
            }
            if ((visibility & SHOW_CHILDREN) != 0) {
                for (Node2D child : children) {
                    child.render(ctx, vp, time);
                }
            }

            ctx.popTransform();
        }
    }

    protected abstract void renderNode(CanvasContext ctx, Viewport vp, double time);


    //
    // CHILDREN
    //

    private final List<Node2D> children = new ArrayList<>();
    private final List<Node2D> childrenImmutable = Collections.unmodifiableList(children);
    private Node2D parent;

    protected boolean isRoot() {
        return false;
    }

    public int childCount() {
        return children.size();
    }

    public void insertChild(int index, Node2D child) {
        if (child == this) {
            throw new IllegalArgumentException("Cannot add node to itself");
        }
        if (child.isRoot()) {
            throw new IllegalArgumentException("Node " + child + " is a root node");
        }
        if (child.parent != null) {
            throw new IllegalArgumentException("Node " + child + " already has a parent");
        }
        children.add(index, child);
        child.parent = this;
    }

    public void appendChild(Node2D child) {
        insertChild(childCount(), child);
    }

    public void prependChild(Node2D child) {
        insertChild(0, child);
    }

    public boolean insertBefore(Node2D target, Node2D child) {
        int i = indexOf(target);
        if (i < 0)
            return false;
        insertChild(i, child);
        return true;
    }

    public boolean insertAfter(Node2D target, Node2D child) {
        int i = indexOf(target);
        if (i < 0)
            return false;
        insertChild(i + 1, child);
        return true;
    }

    public int indexOf(Node2D target) {
        return children.indexOf(target);
    }

    public boolean hasChild(Node2D target) {
        return children.contains(target);
    }

    public boolean removeChild(Node2D target) {
        if (children.remove(target)) {
            target.parent = null;
            return true;
        }
        return false;
    }

    public boolean moveChildTo(int index, Node2D child) {
        if (children.remove(child)) {
            children.add(index, child);
            return true;
        }
        return false;
    }

    public boolean moveChildToStart(Node2D child) {
        return moveChildTo(0, child);
    }

    public boolean moveChildToEnd(Node2D child) {
        return moveChildTo(childCount(), child);
    }

    public boolean moveChildBefore(Node2D target, Node2D child) {
        int i = indexOf(target);
        if (i >= 0)
            return moveChildTo(i, child);
        return false;
    }

    public boolean moveChildAfter(Node2D target, Node2D child) {
        int i = indexOf(target);
        if (i >= 0)
            return moveChildTo(i + 1, child);
        return false;
    }

    public final List<Node2D> getChildren() {
        return childrenImmutable;
    }

    public final Node2D getParent() {
        return parent;
    }

    public void forEachChild(Consumer<? super Node2D> action) {
        children.forEach(action);
    }

    public void removeAllChildren(boolean dispose) {
        children.forEach(c -> {
            c.parent = null;
            if (dispose)
                c.traverse(Node2D::dispose);
        });
        children.clear();
    }


    //
    // TRANSFORMATION
    //

    private final Matrix3x2fStack transform = new Matrix3x2fStack(256);

    public void pushTransform() {
        transform.pushMatrix();
    }

    public void popTransform() {
        transform.popMatrix();
    }

    public void transform(Matrix3x2fc mat) {
        transform(mat.m00(), mat.m10(), mat.m20(), mat.m01(), mat.m11(), mat.m21());
    }

    public Matrix3x2f getTransform(Matrix3x2f mat) {
        return transform.get(mat);
    }

    public void setTransform(Matrix3x2fc mat) {
        transform.set(mat);
    }

    public void transform(float m00, float m10, float m20, float m01, float m11, float m21) {
        float nm00 = m00 * transform.m00 + m10 * transform.m01;
        float nm01 = m01 * transform.m00 + m11 * transform.m01;
        float nm10 = m00 * transform.m10 + m10 * transform.m11;
        float nm11 = m01 * transform.m10 + m11 * transform.m11;
        float nm20 = m00 * transform.m20 + m10 * transform.m21 + m20;
        float nm21 = m01 * transform.m20 + m11 * transform.m21 + m21;
        transform.m00 = nm00;
        transform.m01 = nm01;
        transform.m10 = nm10;
        transform.m11 = nm11;
        transform.m20 = nm20;
        transform.m21 = nm21;
    }

    public void translate(float x, float y) {
        transform.translate(x, y);
    }

    public void translate(Vector2fc vec) {
        transform.translate(vec);
    }

    public void rotate(float angle) {
        transform.rotate(angle);
    }

    public void scale(float s) {
        transform.scale(s);
    }

    public void scale(float x, float y) {
        transform.scale(x, y);
    }

    public void scale(Vector2fc vec) {
        transform.scale(vec.x(), vec.y());
    }

    public void resetTransform() {
        transform.identity();
    }


    //
    // SEARCHING
    //

    @SuppressWarnings("unchecked")
    private <N> Collection<N> search(Function<Node2D, ? extends Node2D> filter, Collection<N> out) {
        Node2D n = filter.apply(this);
        if (n != null) {
            out.add((N) n);
        }

        for (Node2D child : children) {
            child.search(filter, out);
        }

        return out;
    }

    private static Function<Node2D, Node2D> filter(Predicate<? super Node2D> filter) {
        return n -> filter.test(n) ? n : null;
    }

    public Collection<? super Node2D> findNodesByName(String name, Collection<? super Node2D> out) {
        if (name == null) throw new NullPointerException();

        if (out == null) out = new ArrayList<>();
        return search(filter(n -> name.equals(n.name)), out);
    }

    public Collection<? super Node2D> findNodesByTags(String tag, Collection<? super Node2D> out) {
        if (tag == null) throw new NullPointerException();
        Set<String> tags = toTagSet(tag);
        if (tags.isEmpty()) throw new IllegalArgumentException("Empty tag list");

        if (out == null) out = new ArrayList<>();
        return search(filter(n -> n.tags.containsAll(tags)), out);
    }

    public <T extends Node2D> Collection<? super T> findNodesByType(Class<? extends T> type, Collection<? super T> out) {
        if (type == null) throw new NullPointerException();
        if (out == null) out = new ArrayList<>();
        return search(n -> type.isInstance(n) ? type.cast(n) : null, out);
    }

    public Collection<? super Node2D> findNodesMatching(Predicate<? super Node2D> filter, Collection<? super Node2D> out) {
        if (filter == null) throw new NullPointerException();

        if (out == null) out = new ArrayList<>();
        return search(filter(filter), out);
    }

    public Collection<? super Node2D> findAllNodes(Collection<? super Node2D> out) {
        if (out == null) out = new ArrayList<>();
        return search(Function.identity(), out);
    }

    public void traverse(Consumer<? super Node2D> fn) {
        fn.accept(this);

        for (Node2D child : children) {
            child.traverse(fn);
        }
    }
}
