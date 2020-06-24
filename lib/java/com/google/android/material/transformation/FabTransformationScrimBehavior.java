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

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.animation.AnimatorSetCompat;
import com.google.android.material.animation.MotionTiming;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

/**
 * Behavior that should be attached to a scrim that should appear when a {@link
 * FloatingActionButton} is {@link FloatingActionButton#setExpanded(boolean)} expanded}.
 *
 * @deprecated Use {@link com.google.android.material.transition.MaterialContainerTransform}
 *     instead.
 */
@Deprecated
public class FabTransformationScrimBehavior extends ExpandableTransformationBehavior {

  public static final long EXPAND_DELAY = 75;
  public static final long EXPAND_DURATION = 150;

  public static final long COLLAPSE_DELAY = 0;
  public static final long COLLAPSE_DURATION = 150;

  private final MotionTiming expandTiming = new MotionTiming(EXPAND_DELAY, EXPAND_DURATION);
  private final MotionTiming collapseTiming = new MotionTiming(COLLAPSE_DELAY, COLLAPSE_DURATION);

  public FabTransformationScrimBehavior() {}

  public FabTransformationScrimBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
    return dependency instanceof FloatingActionButton;
  }

  @Override
  public boolean onTouchEvent(
      @NonNull CoordinatorLayout parent, @NonNull View child, @NonNull MotionEvent ev) {
    // TODO: Implement click detection so clients don't have to manually set a listener.
    return super.onTouchEvent(parent, child, ev);
  }

  @NonNull
  @Override
  protected AnimatorSet onCreateExpandedStateChangeAnimation(
      @NonNull View dependency,
      @NonNull final View child,
      final boolean expanded,
      boolean isAnimating) {
    List<Animator> animations = new ArrayList<>();
    List<AnimatorListener> listeners = new ArrayList<>();

    createScrimAnimation(child, expanded, isAnimating, animations, listeners);

    AnimatorSet set = new AnimatorSet();
    AnimatorSetCompat.playTogether(set, animations);
    set.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            if (expanded) {
              child.setVisibility(View.VISIBLE);
            }
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            if (!expanded) {
              child.setVisibility(View.INVISIBLE);
            }
          }
        });
    return set;
  }

  private void createScrimAnimation(
      @NonNull View child,
      boolean expanded,
      boolean currentlyAnimating,
      @NonNull List<Animator> animations,
      List<AnimatorListener> unusedListeners) {
    MotionTiming timing = expanded ? expandTiming : collapseTiming;

    Animator animator;
    if (expanded) {
      if (!currentlyAnimating) {
        child.setAlpha(0f);
      }
      animator = ObjectAnimator.ofFloat(child, View.ALPHA, 1f);
    } else {
      animator = ObjectAnimator.ofFloat(child, View.ALPHA, 0f);
    }

    timing.apply(animator);
    animations.add(animator);
  }
}
