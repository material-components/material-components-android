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

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.core.view.ViewCompat;
import android.transition.TransitionValues;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A class that can configure and create an {@link Animator} that slides a view vertically or
 * horizontally slide over a specific distance.
 */
@RequiresApi(VERSION_CODES.LOLLIPOP)
public class SlideDistanceProvider implements VisibilityAnimatorProvider {

  /**
   * GravityFlag definitions.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({Gravity.LEFT, Gravity.TOP, Gravity.RIGHT, Gravity.BOTTOM, Gravity.START, Gravity.END})
  @Retention(RetentionPolicy.SOURCE)
  public @interface GravityFlag {}

  @GravityFlag private int slideEdge;
  @Px private int slideDistance;

  public SlideDistanceProvider(@NonNull Context context, @GravityFlag int slideEdge) {
    this.slideEdge = slideEdge;
    this.slideDistance =
        context
            .getResources()
            .getDimensionPixelSize(R.dimen.mtrl_transition_shared_axis_slide_distance);
  }

  @GravityFlag
  public int getSlideEdge() {
    return slideEdge;
  }

  public void setSlideEdge(@GravityFlag int slideEdge) {
    this.slideEdge = slideEdge;
  }

  @Px
  public int getSlideDistance() {
    return slideDistance;
  }

  public void setSlideDistance(@Px int slideDistance) {
    this.slideDistance = slideDistance;
  }

  @Nullable
  @Override
  public Animator createAppear(
      @NonNull ViewGroup sceneRoot,
      @NonNull View view,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues) {
    return createTranslationAppearAnimator(sceneRoot, view);
  }

  @Nullable
  @Override
  public Animator createDisappear(
      @NonNull ViewGroup sceneRoot,
      @NonNull View view,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues) {
    return createTranslationDisappearAnimator(sceneRoot, view);
  }

  private Animator createTranslationAppearAnimator(View sceneRoot, View view) {
    switch (slideEdge) {
      case Gravity.LEFT:
        return createTranslationXAnimator(view, slideDistance, 0);
      case Gravity.TOP:
        return createTranslationYAnimator(view, -slideDistance, 0);
      case Gravity.RIGHT:
        return createTranslationXAnimator(view, -slideDistance, 0);
      case Gravity.BOTTOM:
        return createTranslationYAnimator(view, slideDistance, 0);
      case Gravity.START:
        return createTranslationXAnimator(
            view, isRtl(sceneRoot) ? slideDistance : -slideDistance, 0);
      case Gravity.END:
        return createTranslationXAnimator(
            view, isRtl(sceneRoot) ? -slideDistance : slideDistance, 0);
      default:
        throw new IllegalArgumentException("Invalid slide direction: " + slideEdge);
    }
  }

  private Animator createTranslationDisappearAnimator(View sceneRoot, View view) {
    switch (slideEdge) {
      case Gravity.LEFT:
        return createTranslationXAnimator(view, 0, -slideDistance);
      case Gravity.TOP:
        return createTranslationYAnimator(view, 0, slideDistance);
      case Gravity.RIGHT:
        return createTranslationXAnimator(view, 0, slideDistance);
      case Gravity.BOTTOM:
        return createTranslationYAnimator(view, 0, -slideDistance);
      case Gravity.START:
        return createTranslationXAnimator(
            view, 0, isRtl(sceneRoot) ? -slideDistance : slideDistance);
      case Gravity.END:
        return createTranslationXAnimator(
            view, 0, isRtl(sceneRoot) ? slideDistance : -slideDistance);
      default:
        throw new IllegalArgumentException("Invalid slide direction: " + slideEdge);
    }
  }

  private static Animator createTranslationXAnimator(
      View view, float startTranslation, float endTranslation) {
    return ObjectAnimator.ofPropertyValuesHolder(
        view, PropertyValuesHolder.ofFloat(View.TRANSLATION_X, startTranslation, endTranslation));
  }

  private static Animator createTranslationYAnimator(
      View view, float startTranslation, float endTranslation) {
    return ObjectAnimator.ofPropertyValuesHolder(
        view, PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, startTranslation, endTranslation));
  }

  private static boolean isRtl(View view) {
    return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
  }
}
