/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.navigationrail;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.transition.Transition;
import androidx.transition.TransitionValues;
import com.google.android.material.navigation.NavigationBarMenuItemView;

/**
 * A {@link Transition} that animates the {@link NavigationBarMenuItemView} label horizontally when
 * the label is fading in.
 */
class LabelMoveTransition extends Transition {

  private static final String LABEL_VISIBILITY = "NavigationRailLabelVisibility";
  private static final float HORIZONTAL_DISTANCE = -30f;

  @Override
  public void captureStartValues(@NonNull TransitionValues transitionValues) {
    transitionValues.values.put(LABEL_VISIBILITY, transitionValues.view.getVisibility());
  }

  @Override
  public void captureEndValues(@NonNull TransitionValues transitionValues) {
    transitionValues.values.put(LABEL_VISIBILITY, transitionValues.view.getVisibility());
  }

  @Nullable
  @Override
  public Animator createAnimator(@NonNull ViewGroup sceneRoot,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues) {
    if (startValues == null || endValues == null
        || startValues.values.get(LABEL_VISIBILITY) == null
        || endValues.values.get(LABEL_VISIBILITY) == null) {
      return super.createAnimator(sceneRoot, startValues, endValues);
    }
    // Only animate if the view is appearing
    if ((int) startValues.values.get(LABEL_VISIBILITY) != View.GONE
        || (int) endValues.values.get(LABEL_VISIBILITY) != View.VISIBLE) {
      return super.createAnimator(sceneRoot, startValues, endValues);
    }
    View view = endValues.view;
    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
    animator.addUpdateListener(
        animation -> {
          float progress = animation.getAnimatedFraction();
          view.setTranslationX(HORIZONTAL_DISTANCE * (1 - progress));
        });
    return animator;
  }
}
