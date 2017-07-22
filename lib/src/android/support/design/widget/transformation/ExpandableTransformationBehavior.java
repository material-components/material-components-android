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
import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.expandable.ExpandableWidget;
import android.util.AttributeSet;
import android.view.View;

/**
 * Base Behavior for views that can react to an {@link ExpandableWidget}'s {@link
 * ExpandableWidget#setExpanded(boolean)} state changes by transforming the ExpandableWidget into
 * itself.
 *
 * <p>Behaviors should override {@link #onCreateExpandedStateChangeAnimation(ExpandableWidget, View,
 * boolean)} to return an animation
 */
public abstract class ExpandableTransformationBehavior extends ExpandableBehavior {

  @Nullable private Animator currentAnimation;

  public ExpandableTransformationBehavior() {}

  public ExpandableTransformationBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * Creates an Animator to be played for this expanded state change.
   *
   * <p>If the new {@code expanded} state is true, the {@code dependency} should be hidden and the
   * {@code child} should be shown.
   *
   * <p>If the new {@code expanded} state is false, the {@code dependency} should be shown and the
   * {@code child} should be hidden.
   *
   * @param dependency the {@link ExpandableWidget} dependency containing the new expanded state.
   * @param child the view that should react to the change in expanded state.
   * @param expanded the new expanded state.
   */
  @NonNull
  protected abstract Animator onCreateExpandedStateChangeAnimation(
      ExpandableWidget dependency, View child, boolean expanded);

  @CallSuper
  @Override
  protected boolean onExpandedStateChange(
      ExpandableWidget dependency, View child, boolean expanded, boolean animated) {
    if (currentAnimation != null) {
      currentAnimation.cancel();
    }

    currentAnimation = onCreateExpandedStateChangeAnimation(dependency, child, expanded);
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
      currentAnimation.end();
    }

    return true;
  }
}
