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
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * Behavior that should be attached to a scrim that should appear when a {@link
 * FloatingActionButton} is {@link FloatingActionButton#setExpanded(boolean)} expanded}.
 */
public class FabTransformationScrimBehavior extends ExpandableTransformationBehavior {

  public FabTransformationScrimBehavior() {
  }

  public FabTransformationScrimBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
    return dependency instanceof FloatingActionButton;
  }

  @Override
  public boolean onTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {
    // TODO: Implement click detection so clients don't have to manually set a listener.
    return super.onTouchEvent(parent, child, ev);
  }

  @NonNull
  @Override
  protected Animator onCreateExpandedStateChangeAnimation(
      ExpandableWidget dep, View child, boolean expanded) {
    List<Animator> animations = new ArrayList<>();

    if (expanded) {
      animations.add(ObjectAnimator.ofFloat(child, View.ALPHA, 0f, 1f));
    } else {
      animations.add(ObjectAnimator.ofFloat(child, View.ALPHA, 1f, 0f));
    }

    AnimatorSet set = new AnimatorSet();
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
    set.playTogether(animations);
    return set;
  }
}
