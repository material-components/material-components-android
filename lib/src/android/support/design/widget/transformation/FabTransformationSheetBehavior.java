/*
 * Copyright 2017 The Android Open Source Project
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
package android.support.design.widget.transformation;

import static android.support.design.animation.AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR;

import android.content.Context;
import android.support.design.animation.MotionTiming;
import android.support.design.animation.Positioning;
import android.support.design.animation.TranslationTiming;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

/**
 * Behavior that should be attached to any sheet that should appear when a {@link
 * FloatingActionButton} is {@link FloatingActionButton#setExpanded(boolean)} expanded}.
 *
 * <p>A sheet usually has some width and height that's smaller than the screen, has an elevation,
 * and may have a scrim underneath.
 */
public class FabTransformationSheetBehavior extends FabTransformationBehavior {

  public FabTransformationSheetBehavior() {}

  public FabTransformationSheetBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected FabTransformationSpec onCreateMotionSpec(
      View dependency, View child, boolean expanded) {
    if (expanded) {
      FabTransformationSpec expandSpec = new FabTransformationSpec();
      expandSpec.totalDuration = 345;
      expandSpec.elevation = new MotionTiming(0, 150);
      expandSpec.translationX =
          new TranslationTiming(
              new MotionTiming(0, 345), new MotionTiming(0, 150), new MotionTiming(0, 345));
      expandSpec.translationY =
          new TranslationTiming(
              new MotionTiming(0, 345), new MotionTiming(0, 345), new MotionTiming(0, 150));
      expandSpec.iconFade = new MotionTiming(0, 120);
      expandSpec.expansion = new MotionTiming(45, 255, FAST_OUT_SLOW_IN_INTERPOLATOR);
      expandSpec.color = new MotionTiming(75, 75);
      expandSpec.contentFade = new MotionTiming(150, 150);

      expandSpec.positioning = new Positioning(Gravity.CENTER, 0f, 0f);
      return expandSpec;
    } else {
      FabTransformationSpec collapseSpec = new FabTransformationSpec();
      collapseSpec.totalDuration = 300;
      collapseSpec.elevation = new MotionTiming(150, 150);
      collapseSpec.translationX =
          new TranslationTiming(
              new MotionTiming(0, 300), new MotionTiming(0, 255), new MotionTiming(45, 255));
      collapseSpec.translationY =
          new TranslationTiming(
              new MotionTiming(0, 300), new MotionTiming(45, 255), new MotionTiming(0, 255));
      collapseSpec.iconFade = new MotionTiming(150, 150);
      collapseSpec.expansion = new MotionTiming(0, 180, FAST_OUT_SLOW_IN_INTERPOLATOR);
      collapseSpec.color = new MotionTiming(60, 150);
      collapseSpec.contentFade = new MotionTiming(0, 75);

      collapseSpec.positioning = new Positioning(Gravity.CENTER, 0f, 0f);
      return collapseSpec;
    }
  }
}
