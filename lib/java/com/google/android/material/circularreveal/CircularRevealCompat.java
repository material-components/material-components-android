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
package com.google.android.material.circularreveal;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewAnimationUtils;
import androidx.annotation.NonNull;
import com.google.android.material.circularreveal.CircularRevealWidget.CircularRevealEvaluator;
import com.google.android.material.circularreveal.CircularRevealWidget.CircularRevealProperty;
import com.google.android.material.circularreveal.CircularRevealWidget.RevealInfo;

/**
 * Defines compat implementations of circular reveal animations.
 *
 * @see ViewAnimationUtils
 */
public final class CircularRevealCompat {

  private CircularRevealCompat() {}

  /**
   * Returns an Animator to animate a clipping circle. The startRadius will be the current {@link
   * CircularRevealWidget#getRevealInfo()}'s {@link RevealInfo#radius} at the start of the
   * animation.
   *
   * <p>This is meant to be used as a drop-in replacement for {@link
   * ViewAnimationUtils#createCircularReveal(View, int, int, float, float)}. In pre-L APIs, a
   * backwards compatible version of the Animator will be returned.
   *
   * <p>You must also call {@link
   * CircularRevealCompat#createCircularRevealListener(CircularRevealWidget)} and add the returned
   * AnimatorListener to this Animator or preferably to the overall AnimatorSet.
   */
  @NonNull
  public static Animator createCircularReveal(
      @NonNull CircularRevealWidget view, float centerX, float centerY, float endRadius) {
    Animator revealInfoAnimator =
        ObjectAnimator.ofObject(
            view,
            CircularRevealProperty.CIRCULAR_REVEAL,
            CircularRevealEvaluator.CIRCULAR_REVEAL,
            new RevealInfo(centerX, centerY, endRadius));
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      // Ideally, the start radius would be inferred from the RevealInfo at the time of animation
      // start (usually on the next event loop iteration). So we approximate.
      RevealInfo revealInfo = view.getRevealInfo();
      if (revealInfo == null) {
        throw new IllegalStateException(
            "Caller must set a non-null RevealInfo before calling this.");
      }
      float startRadius = revealInfo.radius;
      Animator circularRevealAnimator =
          ViewAnimationUtils.createCircularReveal(
              (View) view, (int) centerX, (int) centerY, startRadius, endRadius);
      AnimatorSet set = new AnimatorSet();
      set.playTogether(revealInfoAnimator, circularRevealAnimator);
      return set;
    } else {
      return revealInfoAnimator;
    }
  }

  /**
   * Returns an Animator to animate a clipping circle.
   *
   * <p>This is meant to be used as a drop-in replacement for {@link
   * ViewAnimationUtils#createCircularReveal(View, int, int, float, float)}. In pre-L APIs, a
   * backwards compatible version of the Animator will be returned.
   *
   * <p>You must also call {@link
   * CircularRevealCompat#createCircularRevealListener(CircularRevealWidget)} and add the returned
   * AnimatorListener to this Animator or preferably to the overall AnimatorSet.
   */
  @NonNull
  public static Animator createCircularReveal(
      CircularRevealWidget view, float centerX, float centerY, float startRadius, float endRadius) {
    Animator revealInfoAnimator =
        ObjectAnimator.ofObject(
            view,
            CircularRevealProperty.CIRCULAR_REVEAL,
            CircularRevealEvaluator.CIRCULAR_REVEAL,
            new RevealInfo(centerX, centerY, startRadius),
            new RevealInfo(centerX, centerY, endRadius));
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      Animator circularRevealAnimator =
          ViewAnimationUtils.createCircularReveal(
              (View) view, (int) centerX, (int) centerY, startRadius, endRadius);
      AnimatorSet set = new AnimatorSet();
      set.playTogether(revealInfoAnimator, circularRevealAnimator);
      return set;
    } else {
      return revealInfoAnimator;
    }
  }

  /**
   * Creates an AnimatorListener to be applied to either the Animator returned from {@link
   * #createCircularReveal} or preferably to the overall AnimatorSet.
   */
  @NonNull
  public static AnimatorListener createCircularRevealListener(
      @NonNull final CircularRevealWidget view) {
    return new AnimatorListenerAdapter() {
      @Override
      public void onAnimationStart(Animator animation) {
        view.buildCircularRevealCache();
      }

      @Override
      public void onAnimationEnd(Animator animation) {
        view.destroyCircularRevealCache();
      }
    };
  }
}
