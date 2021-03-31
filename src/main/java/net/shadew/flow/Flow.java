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

package net.shadew.flow;

import java.io.FileInputStream;
import java.io.IOException;

import net.shadew.flow.animation.ColorPropertyAnimation;
import net.shadew.flow.animation.Easing;
import net.shadew.flow.animation.FloatPropertyAnimation;
import net.shadew.flow.animation.Timeline;
import net.shadew.flow.flat.FlatStage;
import net.shadew.flow.flat.RootNode;
import net.shadew.flow.flat.animations.RotateAnimation;
import net.shadew.flow.flat.animations.TranslateAnimation;
import net.shadew.flow.flat.shapes.Circle;
import net.shadew.flow.flat.shapes.Rectangle;
import net.shadew.flow.flat.shapes.Text;
import net.shadew.flow.output.DisplayOutput;
import net.shadew.flow.output.OutputContext;
import net.shadew.flow.util.Font;
import net.shadew.flow.view.ViewStack;
import net.shadew.flow.view.canvas2d.context.GL3NanoVGContext;

public class Flow {
    private final ViewStack viewStack = new ViewStack();
    private final Timeline timeline = new Timeline();
    //    private final OutputContext context = new OutputContext(1920, 1080, WriteOutput.flushInto(960, 540, new MP4Sink("rendered/rendered.mp4")), viewStack, timeline, 60);
//    private final OutputContext context = new OutputContext(1920, 1080, WriteOutput.flushInto(1920, 1080, new MP4Sink("rendered/rendered_2d_3d.mp4")), viewStack, timeline, 60);
    private final OutputContext context = new OutputContext(1920, 1080, DisplayOutput.framed(960, 540), viewStack, timeline, 60);

    private Font font;

    private Flow() {
        init();
    }

    private void init() {
        try {
            font = Font.load(new FileInputStream("test_fonts/JetBrainsMono-Bold.ttf"));
            font.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FlatStage stage = new FlatStage(() -> new GL3NanoVGContext(0));
        setupScene(stage, stage.root, timeline);
        viewStack.addLayer(stage);
        context.init();
    }

    private void cleanup() {
        context.cleanup();
    }

    private void run() {
        context.doSeconds(0, 5);
    }

    public static void main(String[] args) {
        Flow flow = new Flow();
        flow.run();
        flow.cleanup();
    }

    private void setupScene(FlatStage stage, RootNode root, Timeline tl) {
        Rectangle rectangle1 = new Rectangle();
        rectangle1.setWidth(200);
        rectangle1.setHeight(100);

        rectangle1.translate(960, 540);
        rectangle1.setFillColor(0xFF3477EB);
        rectangle1.setStrokeColor(0xFF082E70);
        rectangle1.setStrokeWidth(3);

        root.appendChild(rectangle1);

        Rectangle rectangle2 = new Rectangle();
        rectangle2.setWidth(200);
        rectangle2.setHeight(100);

        rectangle2.translate(960 + 400, 540);
        rectangle2.setFillColor(0xFF3FE090);
        rectangle2.setStrokeColor(0xFF087008);
        rectangle2.setStrokeWidth(3);

        root.appendChild(rectangle2);

        Circle circle = new Circle();
        circle.setRadius(100);

        circle.translate(960 - 400, 540);
        circle.setFillColor(0xFFE05F3F);
        circle.setStrokeColor(0xFF872A13);
        circle.setStrokeWidth(3);

        root.appendChild(circle);

        Text text = new Text();
        text.setWriteMotionX(80);
        text.setFont(font);
        text.setSize(200);
        text.setText("Flow engine!");

        text.translate(960, 540 - 200);
        text.setFillColor(0x33000000);
        text.setStrokeColor(0xFF000000);
        text.setStrokeWidth(3);

        text.setWriteProgress(0);

        root.appendChild(text);

        Text text2 = new Text();
        text2.setWriteMotionX(40);
        text2.setFont(font);
        text2.setSize(100);
        text2.setText("Enough 2D, time for 3D!");

        text2.translate(960, 540);
        text2.setFillColor(0xFF000000);

        text2.setWriteProgress(0);

        root.appendChild(text2);

        Text text3 = new Text();
        text3.setWriteMotionX(16);
        text3.setFont(font);
        text3.setSize(40);
        text3.setText("(Which is gonna take a lot of work because OpenGL)");

        text3.translate(960, 540 + 70);
        text3.setFillColor(0xFF000000);

        text3.setWriteProgress(0);

        root.appendChild(text3);

        root.setBackgroundColor(0xFFFFFFFF);

        tl.append(new TranslateAnimation(rectangle1).offset(0, 200).start(1).duration(1).easing(Easing.QUART_IN_OUT));
        tl.append(new RotateAnimation(rectangle1).angle((float) Math.PI).start(1).duration(1).easing(Easing.QUART_IN_OUT));
        tl.append(new FloatPropertyAnimation(rectangle1::getWidth, rectangle1::setWidth).target(400).start(1).duration(1).easing(Easing.QUART_IN_OUT));
        tl.append(new ColorPropertyAnimation(rectangle1::getFillColor, rectangle1::setFillColor).target(0x003477EB).start(1).duration(1).easing(Easing.QUART_IN_OUT));

        tl.append(new TranslateAnimation(rectangle2).offset(0, 240).start(1).duration(1).easing(Easing.QUART_IN_OUT));
        tl.append(new RotateAnimation(rectangle2).angle((float) Math.PI).start(1).duration(1).easing(Easing.QUART_IN_OUT));
        tl.append(new FloatPropertyAnimation(rectangle2::getWidth, rectangle2::setWidth).target(400).start(1).duration(1).easing(Easing.QUART_IN_OUT));
        tl.append(new ColorPropertyAnimation(rectangle2::getFillColor, rectangle2::setFillColor).target(0x003FE090).start(1).duration(1).easing(Easing.QUART_IN_OUT));

        tl.append(new TranslateAnimation(circle).offset(0, 160).start(1).duration(1).easing(Easing.QUART_IN_OUT));
        tl.append(new RotateAnimation(circle).angle((float) Math.PI).start(1).duration(1).easing(Easing.QUART_IN_OUT));
        tl.append(new FloatPropertyAnimation(circle::getRadius, circle::setRadius).target(200).start(1).duration(1).easing(Easing.QUART_IN_OUT));
        tl.append(new ColorPropertyAnimation(circle::getFillColor, circle::setFillColor).target(0x00E05F3F).start(1).duration(1).easing(Easing.QUART_IN_OUT));

        tl.append(new FloatPropertyAnimation(text::getWriteProgress, text::setWriteProgress).target(1).start(2).duration(0.5).easing(Easing.CUBIC_OUT));
        tl.append(new FloatPropertyAnimation(text2::getWriteProgress, text2::setWriteProgress).target(1).start(2.5).duration(0.5).easing(Easing.CUBIC_OUT));
        tl.append(new FloatPropertyAnimation(text3::getWriteProgress, text3::setWriteProgress).target(1).start(2.75).duration(0.5).easing(Easing.CUBIC_OUT));
    }
}
