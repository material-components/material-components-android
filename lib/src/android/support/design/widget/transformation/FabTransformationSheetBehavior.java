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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.expandable.ExpandableWidget;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * Behavior that should be attached to any sheet that should appear when a {@link
 * FloatingActionButton} is {@link FloatingActionButton#setExpanded(boolean)} expanded}.
 *
 * <p>A sheet usually has some width and height that's smaller than the screen, has an elevation,
 * and may have a scrim underneath.
 */
public class FabTransformationSheetBehavior extends ExpandableTransformationBehavior {

  public FabTransformationSheetBehavior() {
  }

  public FabTransformationSheetBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
    return dependency instanceof FloatingActionButton;
  }

  @Override
  public void onAttachedToLayoutParams(@NonNull CoordinatorLayout.LayoutParams lp) {
    if (lp.dodgeInsetEdges == Gravity.NO_GRAVITY) {
      // If the developer hasn't set dodgeInsetEdges, lets set it to BOTTOM so that
      // we dodge any Snackbars, matching FAB's behavior.
      lp.dodgeInsetEdges = Gravity.BOTTOM;
    }
  }

  @NonNull
  @Override
  protected Animator onCreateExpandedStateChangeAnimation(
      ExpandableWidget dependency, View child, boolean expanded) {
    List<Animator> animations = new ArrayList<>();

    View dep = (View) dependency;
    if (expanded) {
      animations.add(ObjectAnimator.ofFloat(dep, View.ALPHA, 1f, 0f));
      animations.add(ObjectAnimator.ofFloat(child, View.ALPHA, 0f, 1f));
    } else {
      animations.add(ObjectAnimator.ofFloat(dep, View.ALPHA, 0f, 1f));
      animations.add(ObjectAnimator.ofFloat(child, View.ALPHA, 1f, 0f));
    }

    AnimatorSet set = new AnimatorSet();
    set.playTogether(animations);
    set.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            if (expanded) {
              child.setVisibility(View.VISIBLE);
            } else {
              // A bug exists in 4.4.4 where setVisibility() did not invalidate the view.
              dep.setAlpha(1f);
              dep.setVisibility(View.VISIBLE);
            }
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            if (expanded) {
              // A bug exists in 4.4.4 where setVisibility() did not invalidate the view.
              dep.setAlpha(0f);
              dep.setVisibility(View.INVISIBLE);
            } else {
              child.setVisibility(View.INVISIBLE);
            }
          }
        });
    return set;
  }
}
