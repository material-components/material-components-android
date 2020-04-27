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

/*
 * NOTE: THIS CLASS IS AUTO-GENERATED FROM THE EQUIVALENT CLASS IN THE PARENT TRANSITION PACKAGE.
 * IT SHOULD NOT BE EDITED DIRECTLY.
 */
package com.google.android.material.transition.platform;

import android.animation.Animator;
import android.animation.AnimatorSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.transition.TransitionValues;
import android.transition.Visibility;
import com.google.android.material.animation.AnimatorSetCompat;
import java.util.ArrayList;
import java.util.List;

/** A {@link Visibility} transition that is composed of a primary and secondary animator. */
@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
abstract class MaterialVisibility<P extends VisibilityAnimatorProvider>
    extends Visibility {

  private P primaryAnimatorProvider;

  @Nullable private VisibilityAnimatorProvider secondaryAnimatorProvider;

  void initialize() {
    primaryAnimatorProvider = getDefaultPrimaryAnimatorProvider();
    secondaryAnimatorProvider = getDefaultSecondaryAnimatorProvider();
  }

  @NonNull
  abstract P getDefaultPrimaryAnimatorProvider();

  @Nullable
  abstract VisibilityAnimatorProvider getDefaultSecondaryAnimatorProvider();

  @NonNull
  public P getPrimaryAnimatorProvider() {
    if (primaryAnimatorProvider == null) {
      primaryAnimatorProvider = getDefaultPrimaryAnimatorProvider();
    }
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
    return createAnimator(sceneRoot, view, startValues, endValues, true);
  }

  @Override
  public Animator onDisappear(
      ViewGroup sceneRoot, View view, TransitionValues startValues, TransitionValues endValues) {
    return createAnimator(sceneRoot, view, startValues, endValues, false);
  }

  private Animator createAnimator(
      ViewGroup sceneRoot,
      View view,
      TransitionValues startValues,
      TransitionValues endValues,
      boolean isAppearing) {
    AnimatorSet set = new AnimatorSet();
    List<Animator> animators = new ArrayList<>();

    Animator primaryAnimator;
    if (isAppearing) {
      primaryAnimator =
          getPrimaryAnimatorProvider().createAppear(sceneRoot, view, startValues, endValues);
    } else {
      primaryAnimator =
          getPrimaryAnimatorProvider().createDisappear(sceneRoot, view, startValues, endValues);
    }
    if (primaryAnimator != null) {
      animators.add(primaryAnimator);
    }

    VisibilityAnimatorProvider secondary = getSecondaryAnimatorProvider();
    if (secondary != null) {
      Animator secondaryAnimator;
      if (isAppearing) {
        secondaryAnimator = secondary.createAppear(sceneRoot, view, startValues, endValues);
      } else {
        secondaryAnimator = secondary.createDisappear(sceneRoot, view, startValues, endValues);
      }
      if (secondaryAnimator != null) {
        animators.add(secondaryAnimator);
      }
    }

    AnimatorSetCompat.playTogether(set, animators);
    return set;
  }
}
