/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.transition;

import android.animation.Animator;
import android.animation.AnimatorSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import androidx.transition.TransitionValues;
import androidx.transition.Visibility;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.animation.AnimatorSetCompat;
import java.util.ArrayList;
import java.util.List;

/** A {@link Visibility} transition that is composed of a primary and secondary animator. */
abstract class MaterialVisibility<P extends VisibilityAnimatorProvider> extends Visibility {

  private final P primaryAnimatorProvider;

  @Nullable private VisibilityAnimatorProvider secondaryAnimatorProvider;

  protected MaterialVisibility(
      P primaryAnimatorProvider, @Nullable VisibilityAnimatorProvider secondaryAnimatorProvider) {
    this.primaryAnimatorProvider = primaryAnimatorProvider;
    this.secondaryAnimatorProvider = secondaryAnimatorProvider;
    setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
  }

  @NonNull
  public P getPrimaryAnimatorProvider() {
    return primaryAnimatorProvider;
  }

  @Nullable
  public VisibilityAnimatorProvider getSecondaryAnimatorProvider() {
    return secondaryAnimatorProvider;
  }

  public void setSecondaryAnimatorProvider(
      @Nullable VisibilityAnimatorProvider secondaryAnimatorProvider) {
    this.secondaryAnimatorProvider = secondaryAnimatorProvider;
  }

  @Override
  public Animator onAppear(
      ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
    return createAnimator(sceneRoot, view, true);
  }

  @Override
  public Animator onDisappear(
      ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
    return createAnimator(sceneRoot, view, false);
  }

  private Animator createAnimator(ViewGroup sceneRoot, View view, boolean appearing) {
    AnimatorSet set = new AnimatorSet();
    List<Animator> animators = new ArrayList<>();

    Animator primaryAnimator =
        appearing
            ? primaryAnimatorProvider.createAppear(sceneRoot, view)
            : primaryAnimatorProvider.createDisappear(sceneRoot, view);
    if (primaryAnimator != null) {
      animators.add(primaryAnimator);
    }

    if (secondaryAnimatorProvider != null) {
      Animator secondaryAnimator =
          appearing
              ? secondaryAnimatorProvider.createAppear(sceneRoot, view)
              : secondaryAnimatorProvider.createDisappear(sceneRoot, view);
      if (secondaryAnimator != null) {
        animators.add(secondaryAnimator);
      }
    }

    AnimatorSetCompat.playTogether(set, animators);
    return set;
  }
}
