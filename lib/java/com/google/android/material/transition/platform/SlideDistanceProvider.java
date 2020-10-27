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

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import androidx.core.view.ViewCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A class that can configure and create an {@link Animator} that slides a view vertically or
 * horizontally slide over a specific distance.
 */
@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
public final class SlideDistanceProvider implements VisibilityAnimatorProvider {

  private static final int DEFAULT_DISTANCE = -1;

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
  @Px private int slideDistance = DEFAULT_DISTANCE;

  public SlideDistanceProvider(@GravityFlag int slideEdge) {
    this.slideEdge = slideEdge;
  }

  @GravityFlag
  public int getSlideEdge() {
    return slideEdge;
  }

  public void setSlideEdge(@GravityFlag int slideEdge) {
    this.slideEdge = slideEdge;
  }

  /**
   * Get the distance this animator will translate its target. If set to -1, the default slide
   * distance will be used.
   *
   * @see #setSlideDistance(int)
   */
  @Px
  public int getSlideDistance() {
    return slideDistance;
  }

  /**
   * Set the distance this animator will translate its target.
   *
   * <p>By default, this value is set to -1 which indicates that the default slide distance,
   * R.dimen.mtrl_transition_shared_axis_slide_distance will be used. Setting the slide distance to
   * any other value will override this default.
   *
   * @throws IllegalArgumentException If {@code slideDistance} is negative.
   */
  public void setSlideDistance(@Px int slideDistance) {
    if (slideDistance < 0) {
      throw new IllegalArgumentException(
          "Slide distance must be positive. If attempting to reverse the direction of the slide,"
              + " use setSlideEdge(int) instead.");
    }
    this.slideDistance = slideDistance;
  }

  @Nullable
  @Override
  public Animator createAppear(@NonNull ViewGroup sceneRoot, @NonNull View view) {
    return createTranslationAppearAnimator(
        sceneRoot, view, slideEdge, getSlideDistanceOrDefault(view.getContext()));
  }

  @Nullable
  @Override
  public Animator createDisappear(@NonNull ViewGroup sceneRoot, @NonNull View view) {
    return createTranslationDisappearAnimator(
        sceneRoot, view, slideEdge, getSlideDistanceOrDefault(view.getContext()));
  }

  private int getSlideDistanceOrDefault(Context context) {
    if (slideDistance != DEFAULT_DISTANCE) {
      return slideDistance;
    }

    return context
        .getResources()
        .getDimensionPixelSize(R.dimen.mtrl_transition_shared_axis_slide_distance);
  }

  private static Animator createTranslationAppearAnimator(
      View sceneRoot, View view, @GravityFlag int slideEdge, @Px int slideDistance) {
    final float originalX = view.getTranslationX();
    final float originalY = view.getTranslationY();
    switch (slideEdge) {
      case Gravity.LEFT:
        return createTranslationXAnimator(view, originalX + slideDistance, originalX, originalX);
      case Gravity.TOP:
        return createTranslationYAnimator(view, originalY - slideDistance, originalY, originalY);
      case Gravity.RIGHT:
        return createTranslationXAnimator(view, originalX - slideDistance, originalX, originalX);
      case Gravity.BOTTOM:
        return createTranslationYAnimator(view, originalY + slideDistance, originalY, originalY);
      case Gravity.START:
        return createTranslationXAnimator(
            view,
            isRtl(sceneRoot) ? originalX + slideDistance : originalX - slideDistance,
            originalX,
            originalX);
      case Gravity.END:
        return createTranslationXAnimator(
            view,
            isRtl(sceneRoot) ? originalX - slideDistance : originalX + slideDistance,
            originalX,
            originalX);
      default:
        throw new IllegalArgumentException("Invalid slide direction: " + slideEdge);
    }
  }

  private static Animator createTranslationDisappearAnimator(
      View sceneRoot, View view, @GravityFlag int slideEdge, @Px int slideDistance) {
    final float originalX = view.getTranslationX();
    final float originalY = view.getTranslationY();
    switch (slideEdge) {
      case Gravity.LEFT:
        return createTranslationXAnimator(view, originalX, originalX - slideDistance, originalX);
      case Gravity.TOP:
        return createTranslationYAnimator(view, originalY, originalY + slideDistance, originalY);
      case Gravity.RIGHT:
        return createTranslationXAnimator(view, originalX, originalX + slideDistance, originalX);
      case Gravity.BOTTOM:
        return createTranslationYAnimator(view, originalY, originalY - slideDistance, originalY);
      case Gravity.START:
        return createTranslationXAnimator(
            view,
            originalX,
            isRtl(sceneRoot) ? originalX - slideDistance : originalX + slideDistance,
            originalX);
      case Gravity.END:
        return createTranslationXAnimator(
            view,
            originalX,
            isRtl(sceneRoot) ? originalX + slideDistance : originalX - slideDistance,
            originalX);
      default:
        throw new IllegalArgumentException("Invalid slide direction: " + slideEdge);
    }
  }

  private static Animator createTranslationXAnimator(
      final View view,
      float startTranslation,
      float endTranslation,
      final float originalTranslation) {
    ObjectAnimator animator =
        ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, startTranslation, endTranslation));
    animator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            view.setTranslationX(originalTranslation);
          }
        });
    return animator;
  }

  private static Animator createTranslationYAnimator(
      final View view,
      float startTranslation,
      float endTranslation,
      final float originalTranslation) {
    ObjectAnimator animator =
        ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, startTranslation, endTranslation));
    animator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            view.setTranslationY(originalTranslation);
          }
        });
    return animator;
  }

  private static boolean isRtl(View view) {
    return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
  }
}
