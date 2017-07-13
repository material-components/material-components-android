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
 * Behavior that should be attached to any sheet that should appear when a {@link ExpandableWidget}
 * is {@link ExpandableWidget#setExpanded(boolean)} expanded}.
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
    return super.layoutDependsOn(parent, child, dependency)
        && dependency instanceof FloatingActionButton;
  }

  @Override
  public void onAttachedToLayoutParams(@NonNull CoordinatorLayout.LayoutParams lp) {
    if (lp.dodgeInsetEdges == Gravity.NO_GRAVITY) {
      // If the developer hasn't set dodgeInsetEdges, lets set it to BOTTOM so that
      // we dodge any Snackbars, matching FAB's behavior.
      lp.dodgeInsetEdges = Gravity.BOTTOM;
    }
  }

  @Override
  protected void jumpToState(ExpandableWidget dep, View child) {
    super.jumpToState(dep, child);

    View dependency = (View) dep;
    if (dep.isExpanded()) {
      // A bug exists in 4.4.4 where setVisibility() did not invalidate the view.
      dependency.setAlpha(0f);
      dependency.setVisibility(View.INVISIBLE);
    } else {
      // A bug exists in 4.4.4 where setVisibility() did not invalidate the view.
      dependency.setAlpha(1f);
      dependency.setVisibility(View.VISIBLE);
    }
  }

  @Override
  protected Animator createAnimation(ExpandableWidget dep, View child) {
    List<Animator> animations = new ArrayList<>();

    View dependency = (View) dep;
    if (dep.isExpanded()) {
      animations.add(ObjectAnimator.ofFloat(dependency, View.ALPHA, 1f, 0f));
      animations.add(ObjectAnimator.ofFloat(child, View.ALPHA, 0f, 1f));
    } else {
      animations.add(ObjectAnimator.ofFloat(dependency, View.ALPHA, 0f, 1f));
      animations.add(ObjectAnimator.ofFloat(child, View.ALPHA, 1f, 0f));
    }

    AnimatorSet set = new AnimatorSet();
    set.playTogether(animations);
    set.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            if (!dep.isExpanded()) {
              // A bug exists in 4.4.4 where setVisibility() did not invalidate the view.
              dependency.setAlpha(1f);
              dependency.setVisibility(View.VISIBLE);
            }
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            if (dep.isExpanded()) {
              // A bug exists in 4.4.4 where setVisibility() did not invalidate the view.
              dependency.setAlpha(0f);
              dependency.setVisibility(View.INVISIBLE);
            }
          }
        });
    return set;
  }
}
