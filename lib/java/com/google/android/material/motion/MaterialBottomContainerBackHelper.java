/*
 * Copyright 2023 The Android Open Source Project
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
package com.google.android.material.motion;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.BackEventCompat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import com.google.android.material.animation.AnimationUtils;

/**
 * Utility class for container views on the bottom edge of the screen (e.g., bottom sheet) that
 * support back progress animations.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class MaterialBottomContainerBackHelper extends MaterialBackAnimationHelper<View> {

  private final float maxScaleXDistance;
  private final float maxScaleYDistance;

  public MaterialBottomContainerBackHelper(@NonNull View view) {
    super(view);

    Resources resources = view.getResources();
    maxScaleXDistance =
        resources.getDimension(R.dimen.m3_back_progress_bottom_container_max_scale_x_distance);
    maxScaleYDistance =
        resources.getDimension(R.dimen.m3_back_progress_bottom_container_max_scale_y_distance);
  }

  public void startBackProgress(@NonNull BackEventCompat backEvent) {
    super.onStartBackProgress(backEvent);
  }

  public void updateBackProgress(@NonNull BackEventCompat backEvent) {
    if (super.onUpdateBackProgress(backEvent) == null) {
      return;
    }

    updateBackProgress(backEvent.getProgress());
  }

  @VisibleForTesting
  public void updateBackProgress(float progress) {
    progress = interpolateProgress(progress);

    float width = view.getWidth();
    float height = view.getHeight();
    if (width <= 0f || height <= 0f) {
      return;
    }

    float maxScaleXDelta = maxScaleXDistance / width;
    float maxScaleYDelta = maxScaleYDistance / height;
    float scaleXDelta = AnimationUtils.lerp(0, maxScaleXDelta, progress);
    float scaleYDelta = AnimationUtils.lerp(0, maxScaleYDelta, progress);
    float scaleX = 1 - scaleXDelta;
    float scaleY = 1 - scaleYDelta;

    if (Float.isNaN(scaleX) || Float.isNaN(scaleY)) {
      return;
    }

    view.setScaleX(scaleX);
    view.setPivotY(height);
    view.setScaleY(scaleY);

    if (view instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) view;
      for (int i = 0; i < viewGroup.getChildCount(); i++) {
        View childView = viewGroup.getChildAt(i);
        // Preserve the original aspect ratio and container alignment of the child content.
        childView.setPivotY(-childView.getTop());
        childView.setScaleY(scaleY != 0f ? scaleX / scaleY : 1f);
      }
    }
  }

  public void finishBackProgressPersistent(
      @NonNull BackEventCompat backEvent, @Nullable AnimatorListener animatorListener) {
    Animator animator = createResetScaleAnimator();
    animator.setDuration(
        AnimationUtils.lerp(hideDurationMax, hideDurationMin, backEvent.getProgress()));
    if (animatorListener != null) {
      animator.addListener(animatorListener);
    }
    animator.start();
  }

  public void finishBackProgressNotPersistent(
      @NonNull BackEventCompat backEvent, @Nullable AnimatorListener animatorListener) {
    float scaledHeight = view.getHeight() * view.getScaleY();
    ObjectAnimator finishAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, scaledHeight);
    finishAnimator.setInterpolator(new FastOutSlowInInterpolator());
    finishAnimator.setDuration(
        AnimationUtils.lerp(hideDurationMax, hideDurationMin, backEvent.getProgress()));
    finishAnimator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            view.setTranslationY(0);
            updateBackProgress(/* progress= */ 0);
          }
        });
    if (animatorListener != null) {
      finishAnimator.addListener(animatorListener);
    }
    finishAnimator.start();
  }

  public void cancelBackProgress() {
    if (super.onCancelBackProgress() == null) {
      return;
    }

    Animator animator = createResetScaleAnimator();
    animator.setDuration(cancelDuration);
    animator.start();
  }

  private Animator createResetScaleAnimator() {
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(
        ObjectAnimator.ofFloat(view, View.SCALE_X, 1),
        ObjectAnimator.ofFloat(view, View.SCALE_Y, 1));
    if (view instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) view;
      for (int i = 0; i < viewGroup.getChildCount(); i++) {
        View childView = viewGroup.getChildAt(i);
        animatorSet.playTogether(ObjectAnimator.ofFloat(childView, View.SCALE_Y, 1));
      }
    }
    animatorSet.setInterpolator(new FastOutSlowInInterpolator());
    return animatorSet;
  }
}
