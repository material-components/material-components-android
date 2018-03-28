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
package android.support.design.transformation;

import android.support.design.R;

import android.content.Context;
import android.support.annotation.AnimatorRes;
import android.support.design.animation.MotionSpec;
import android.support.design.animation.Positioning;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.Gravity;

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
  protected FabTransformationSpec onCreateMotionSpec(Context context, boolean expanded) {
    @AnimatorRes int specRes;
    if (expanded) {
      specRes = R.animator.mtrl_fab_transformation_sheet_expand_spec;
    } else {
      specRes = R.animator.mtrl_fab_transformation_sheet_collapse_spec;
    }

    FabTransformationSpec spec = new FabTransformationSpec();
    spec.timings = MotionSpec.createFromResource(context, specRes);
    spec.positioning = new Positioning(Gravity.CENTER, 0f, 0f);
    return spec;
  }
}
