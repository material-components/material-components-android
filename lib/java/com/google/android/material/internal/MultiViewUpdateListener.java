/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import java.util.Collection;

/**
 * An {@link AnimatorUpdateListener} that provides a framework for applying the animated value to
 * multiple views.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class MultiViewUpdateListener implements AnimatorUpdateListener {

  private final Listener listener;
  private final View[] views;

  @SuppressLint("LambdaLast")
  public MultiViewUpdateListener(@NonNull Listener listener, @NonNull View... views) {
    this.listener = listener;
    this.views = views;
  }

  @SuppressLint("LambdaLast")
  public MultiViewUpdateListener(@NonNull Listener listener, @NonNull Collection<View> views) {
    this.listener = listener;
    this.views = views.toArray(new View[0]);
  }

  @Override
  public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
    for (View view : views) {
      listener.onAnimationUpdate(valueAnimator, view);
    }
  }

  interface Listener {
    /**
     * This method will be called during {@link
     * AnimatorUpdateListener#onAnimationUpdate(ValueAnimator)} for each view that's provided to the
     * {@link MultiViewUpdateListener}.
     */
    void onAnimationUpdate(@NonNull ValueAnimator valueAnimator, @NonNull View view);
  }

  /**
   * Returns a {@link MultiViewUpdateListener.Listener} that can be used to apply the animated alpha
   * value to the input views.
   */
  @NonNull
  public static MultiViewUpdateListener alphaListener(@NonNull View... views) {
    return new MultiViewUpdateListener(MultiViewUpdateListener::setAlpha, views);
  }

  /**
   * Returns a {@link MultiViewUpdateListener.Listener} that can be used to apply the animated alpha
   * value to the input collection of views.
   */
  @NonNull
  public static MultiViewUpdateListener alphaListener(@NonNull Collection<View> views) {
    return new MultiViewUpdateListener(MultiViewUpdateListener::setAlpha, views);
  }

  private static void setAlpha(@NonNull ValueAnimator animator, @NonNull View view) {
    view.setAlpha((Float) animator.getAnimatedValue());
  }

  /**
   * Returns a {@link MultiViewUpdateListener.Listener} that can be used to apply the animated scale
   * value to the input views.
   */
  @NonNull
  public static MultiViewUpdateListener scaleListener(@NonNull View... views) {
    return new MultiViewUpdateListener(MultiViewUpdateListener::setScale, views);
  }

  /**
   * Returns a {@link MultiViewUpdateListener.Listener} that can be used to apply the animated scale
   * value to the input collection of views.
   */
  @NonNull
  public static MultiViewUpdateListener scaleListener(@NonNull Collection<View> views) {
    return new MultiViewUpdateListener(MultiViewUpdateListener::setScale, views);
  }

  private static void setScale(@NonNull ValueAnimator animator, @NonNull View view) {
    Float scale = (Float) animator.getAnimatedValue();
    view.setScaleX(scale);
    view.setScaleY(scale);
  }

  /**
   * Returns a {@link MultiViewUpdateListener.Listener} that can be used to apply the animated
   * translation x value to the input views.
   */
  @NonNull
  public static MultiViewUpdateListener translationXListener(@NonNull View... views) {
    return new MultiViewUpdateListener(MultiViewUpdateListener::setTranslationX, views);
  }

  /**
   * Returns a {@link MultiViewUpdateListener.Listener} that can be used to apply the animated
   * translation x value to the input collection of views.
   */
  @NonNull
  public static MultiViewUpdateListener translationXListener(@NonNull Collection<View> views) {
    return new MultiViewUpdateListener(MultiViewUpdateListener::setTranslationX, views);
  }

  private static void setTranslationX(@NonNull ValueAnimator animator, @NonNull View view) {
    view.setTranslationX((Float) animator.getAnimatedValue());
  }

  /**
   * Returns a {@link MultiViewUpdateListener.Listener} that can be used to apply the animated
   * translation y value to the input views.
   */
  @NonNull
  public static MultiViewUpdateListener translationYListener(@NonNull View... views) {
    return new MultiViewUpdateListener(MultiViewUpdateListener::setTranslationY, views);
  }

  /**
   * Returns a {@link MultiViewUpdateListener.Listener} that can be used to apply the animated
   * translation y value to the input collection of views.
   */
  @NonNull
  public static MultiViewUpdateListener translationYListener(@NonNull Collection<View> views) {
    return new MultiViewUpdateListener(MultiViewUpdateListener::setTranslationY, views);
  }

  private static void setTranslationY(@NonNull ValueAnimator animator, @NonNull View view) {
    view.setTranslationY((Float) animator.getAnimatedValue());
  }
}
