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
import android.animation.TimeInterpolator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
  private final List<VisibilityAnimatorProvider> additionalAnimatorProviders = new ArrayList<>();

  protected MaterialVisibility(
      P primaryAnimatorProvider, @Nullable VisibilityAnimatorProvider secondaryAnimatorProvider) {
    this.primaryAnimatorProvider = primaryAnimatorProvider;
    this.secondaryAnimatorProvider = secondaryAnimatorProvider;
  }

  /**
   * Returns the primary {@link VisibilityAnimatorProvider} for this transition, which can be
   * modified but not swapped out completely.
   */
  @NonNull
  public P getPrimaryAnimatorProvider() {
    return primaryAnimatorProvider;
  }

  /**
   * Returns the secondary {@link VisibilityAnimatorProvider} for this transition or null, which can
   * be modified or swapped out completely for a different {@link VisibilityAnimatorProvider}.
   *
   * @see #setSecondaryAnimatorProvider(VisibilityAnimatorProvider)
   */
  @Nullable
  public VisibilityAnimatorProvider getSecondaryAnimatorProvider() {
    return secondaryAnimatorProvider;
  }

  /**
   * Sets the secondary {@link VisibilityAnimatorProvider}, which provides animators to be played
   * together with the primary {@link VisibilityAnimatorProvider}.
   */
  public void setSecondaryAnimatorProvider(
      @Nullable VisibilityAnimatorProvider secondaryAnimatorProvider) {
    this.secondaryAnimatorProvider = secondaryAnimatorProvider;
  }

  /**
   * Adds an additional {@link VisibilityAnimatorProvider}, which provides animators be played
   * together with the primary and secondary {@link VisibilityAnimatorProvider
   * VisibilityAnimatorProviders}.
   *
   * @see #getPrimaryAnimatorProvider()
   * @see #getSecondaryAnimatorProvider()
   */
  public void addAdditionalAnimatorProvider(
      @NonNull VisibilityAnimatorProvider additionalAnimatorProvider) {
    additionalAnimatorProviders.add(additionalAnimatorProvider);
  }

  /**
   * Removes an additional {@link VisibilityAnimatorProvider} that was previously added.
   *
   * @see #addAdditionalAnimatorProvider(VisibilityAnimatorProvider)
   */
  public boolean removeAdditionalAnimatorProvider(
      @NonNull VisibilityAnimatorProvider additionalAnimatorProvider) {
    return additionalAnimatorProviders.remove(additionalAnimatorProvider);
  }

  /**
   * Clears all additional {@link VisibilityAnimatorProvider VisibilityAnimatorProviders} that were
   * previously added.
   *
   * @see #addAdditionalAnimatorProvider(VisibilityAnimatorProvider)
   */
  public void clearAdditionalAnimatorProvider() {
    additionalAnimatorProviders.clear();
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

  private Animator createAnimator(
      @NonNull ViewGroup sceneRoot, @NonNull View view, boolean appearing) {
    AnimatorSet set = new AnimatorSet();
    List<Animator> animators = new ArrayList<>();

    addAnimatorIfNeeded(animators, primaryAnimatorProvider, sceneRoot, view, appearing);

    addAnimatorIfNeeded(animators, secondaryAnimatorProvider, sceneRoot, view, appearing);

    for (VisibilityAnimatorProvider additionalAnimatorProvider : additionalAnimatorProviders) {
      addAnimatorIfNeeded(animators, additionalAnimatorProvider, sceneRoot, view, appearing);
    }

    maybeApplyThemeValues(sceneRoot.getContext(), appearing);

    AnimatorSetCompat.playTogether(set, animators);
    return set;
  }

  private static void addAnimatorIfNeeded(
      List<Animator> animators,
      @Nullable VisibilityAnimatorProvider animatorProvider,
      ViewGroup sceneRoot,
      View view,
      boolean appearing) {
    if (animatorProvider == null) {
      return;
    }
    Animator animator =
        appearing
            ? animatorProvider.createAppear(sceneRoot, view)
            : animatorProvider.createDisappear(sceneRoot, view);
    if (animator != null) {
      animators.add(animator);
    }
  }

  private void maybeApplyThemeValues(@NonNull Context context, boolean appearing) {
    TransitionUtils.maybeApplyThemeDuration(this, context, getDurationThemeAttrResId(appearing));
    TransitionUtils.maybeApplyThemeInterpolator(
        this, context, getEasingThemeAttrResId(appearing), getDefaultEasingInterpolator(appearing));
  }

  @AttrRes
  int getDurationThemeAttrResId(boolean appearing) {
    return TransitionUtils.NO_ATTR_RES_ID;
  }

  @AttrRes
  int getEasingThemeAttrResId(boolean appearing) {
    return TransitionUtils.NO_ATTR_RES_ID;
  }

  @NonNull
  TimeInterpolator getDefaultEasingInterpolator(boolean appearing) {
    return AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR;
  }

  // STRIP-FROM-PLATFORM-TRANSITIONS-PACKAGE
  @Override // STRIP-FROM-PLATFORM-TRANSITIONS-PACKAGE
  public boolean isSeekingSupported() { // STRIP-FROM-PLATFORM-TRANSITIONS-PACKAGE
    return true; // STRIP-FROM-PLATFORM-TRANSITIONS-PACKAGE
  } // STRIP-FROM-PLATFORM-TRANSITIONS-PACKAGE
}
