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
package com.google.android.material.transformation;

import com.google.android.material.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewParent;
import androidx.annotation.AnimatorRes;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.animation.MotionSpec;
import com.google.android.material.animation.Positioning;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.HashMap;
import java.util.Map;

/**
 * Behavior that should be attached to any sheet that should appear when a {@link
 * FloatingActionButton} is {@link FloatingActionButton#setExpanded(boolean)} expanded}.
 *
 * <p>A sheet usually has some width and height that's smaller than the screen, has an elevation,
 * and may have a scrim underneath.
 *
 * @deprecated Use {@link com.google.android.material.transition.MaterialContainerTransform}
 *     instead.
 */
@Deprecated
public class FabTransformationSheetBehavior extends FabTransformationBehavior {

  @Nullable private Map<View, Integer> importantForAccessibilityMap;

  public FabTransformationSheetBehavior() {}

  public FabTransformationSheetBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @NonNull
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

  @CallSuper
  @Override
  protected boolean onExpandedStateChange(
      @NonNull View dependency, @NonNull View child, boolean expanded, boolean animated) {
    updateImportantForAccessibility(child, expanded);
    return super.onExpandedStateChange(dependency, child, expanded, animated);
  }

  private void updateImportantForAccessibility(@NonNull View sheet, boolean expanded) {
    ViewParent viewParent = sheet.getParent();
    if (!(viewParent instanceof CoordinatorLayout)) {
      return;
    }

    CoordinatorLayout parent = (CoordinatorLayout) viewParent;
    final int childCount = parent.getChildCount();
    if (expanded) {
      importantForAccessibilityMap = new HashMap<>(childCount);
    }

    for (int i = 0; i < childCount; i++) {
      final View child = parent.getChildAt(i);

      // Don't change the accessibility importance of the sheet or the scrim.
      boolean hasScrimBehavior =
          (child.getLayoutParams() instanceof CoordinatorLayout.LayoutParams)
              && (((CoordinatorLayout.LayoutParams) child.getLayoutParams()).getBehavior()
                  instanceof FabTransformationScrimBehavior);
      if (child == sheet || hasScrimBehavior) {
        continue;
      }

      if (!expanded) {
        if (importantForAccessibilityMap != null
            && importantForAccessibilityMap.containsKey(child)) {
          // Restores the original important for accessibility value of the child view.
          child.setImportantForAccessibility(importantForAccessibilityMap.get(child));
        }
      } else {
        // Saves the important for accessibility value of the child view.
        importantForAccessibilityMap.put(child, child.getImportantForAccessibility());

        child.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
      }
    }

    if (!expanded) {
      importantForAccessibilityMap = null;
    }
  }
}
