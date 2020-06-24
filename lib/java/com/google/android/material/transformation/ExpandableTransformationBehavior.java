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
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.expandable.ExpandableWidget;

/**
 * Base Behavior for views that can react to an {@link ExpandableWidget}'s {@link
 * ExpandableWidget#setExpanded(boolean)} state changes by transforming the ExpandableWidget into
 * itself.
 *
 * <p>Behaviors should override {@link #onCreateExpandedStateChangeAnimation(View, View, boolean,
 * boolean)} to return an animation
 *
 * @deprecated Use {@link com.google.android.material.transition.MaterialContainerTransform}
 *     instead.
 */
@Deprecated
public abstract class ExpandableTransformationBehavior extends ExpandableBehavior {

  @Nullable private AnimatorSet currentAnimation;

  public ExpandableTransformationBehavior() {}

  public ExpandableTransformationBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * Creates an AnimatorSet to be played for this expanded state change.
   *
   * <p>If the new {@code expanded} state is true, the {@code dependency} should be hidden and the
   * {@code child} should be shown.
   *
   * <p>If the new {@code expanded} state is false, the {@code dependency} should be shown and the
   * {@code child} should be hidden.
   *
   * @param dependency the {@link com.google.android.material.expandable.ExpandableWidget}
   * dependency containing the new expanded state.
   * @param child the view that should react to the change in expanded state.
   * @param expanded the new expanded state.
   * @param isAnimating whether this state change occurred while a previous state change was still
   */
  @NonNull
  protected abstract AnimatorSet onCreateExpandedStateChangeAnimation(
      View dependency, View child, boolean expanded, boolean isAnimating);

  @CallSuper
  @Override
  protected boolean onExpandedStateChange(
      View dependency, View child, boolean expanded, boolean animated) {
    boolean currentlyAnimating = currentAnimation != null;
    if (currentlyAnimating) {
      currentAnimation.cancel();
    }

    currentAnimation =
        onCreateExpandedStateChangeAnimation(dependency, child, expanded, currentlyAnimating);
    currentAnimation.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            currentAnimation = null;
          }
        });

    currentAnimation.start();
    if (!animated) {
      // Synchronously end the animation, jumping to the end state.
      // AnimatorSet has synchronous listener behavior on all supported APIs.
      currentAnimation.end();
    }

    return true;
  }
}
