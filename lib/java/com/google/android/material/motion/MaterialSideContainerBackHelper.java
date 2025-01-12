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
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import androidx.activity.BackEventCompat;
import androidx.annotation.GravityInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import com.google.android.material.animation.AnimationUtils;

/**
 * Utility class for side container views on the left or right edge of the screen (e.g., side sheet,
 * nav drawer, etc.) that support back progress animations.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class MaterialSideContainerBackHelper extends MaterialBackAnimationHelper<View> {

  private final float maxScaleXDistanceShrink;
  private final float maxScaleXDistanceGrow;
  private final float maxScaleYDistance;

  public MaterialSideContainerBackHelper(@NonNull View view) {
    super(view);

    Resources resources = view.getResources();
    maxScaleXDistanceShrink =
        resources.getDimension(R.dimen.m3_back_progress_side_container_max_scale_x_distance_shrink);
    maxScaleXDistanceGrow =
        resources.getDimension(R.dimen.m3_back_progress_side_container_max_scale_x_distance_grow);
    maxScaleYDistance =
        resources.getDimension(R.dimen.m3_back_progress_side_container_max_scale_y_distance);
  }

  public void startBackProgress(@NonNull BackEventCompat backEvent) {
    super.onStartBackProgress(backEvent);
  }

  public void updateBackProgress(@NonNull BackEventCompat backEvent, @GravityInt int gravity) {
    if (super.onUpdateBackProgress(backEvent) == null) {
      return;
    }

    boolean leftSwipeEdge = backEvent.getSwipeEdge() == BackEventCompat.EDGE_LEFT;
    updateBackProgress(backEvent.getProgress(), leftSwipeEdge, gravity);
  }

  @VisibleForTesting
  public void updateBackProgress(float progress, boolean leftSwipeEdge, @GravityInt int gravity) {
    progress = interpolateProgress(progress);
    boolean leftGravity = checkAbsoluteGravity(gravity, Gravity.LEFT);

    boolean swipeEdgeMatchesGravity = leftSwipeEdge == leftGravity;

    int width = view.getWidth();
    int height = view.getHeight();

    if (width <= 0f || height <= 0f) {
      return;
    }

    float maxScaleXDeltaShrink = maxScaleXDistanceShrink / width;
    float maxScaleXDeltaGrow = maxScaleXDistanceGrow / width;
    float maxScaleYDelta = maxScaleYDistance / height;

    view.setPivotX(leftGravity ? 0 : width);
    float endScaleXDelta = swipeEdgeMatchesGravity ? maxScaleXDeltaGrow : -maxScaleXDeltaShrink;
    float scaleXDelta = AnimationUtils.lerp(0, endScaleXDelta, progress);
    float scaleX = 1 + scaleXDelta;
    float scaleYDelta = AnimationUtils.lerp(0, maxScaleYDelta, progress);
    float scaleY = 1 - scaleYDelta;

    if (Float.isNaN(scaleX) || Float.isNaN(scaleY)) {
      return;
    }

    view.setScaleX(scaleX);
    view.setScaleY(scaleY);

    if (view instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) view;
      for (int i = 0; i < viewGroup.getChildCount(); i++) {
        View childView = viewGroup.getChildAt(i);
        // Preserve the original aspect ratio and container alignment of the child content, and add
        // content margins.
        childView.setPivotX(
            leftGravity
                ? (width - childView.getRight() + childView.getWidth())
                : -childView.getLeft());
        childView.setPivotY(-childView.getTop());
        float childScaleX = swipeEdgeMatchesGravity ? 1 - scaleXDelta : 1f;
        float childScaleY = scaleY != 0f ? scaleX / scaleY * childScaleX : 1f;

        if (Float.isNaN(childScaleX) || Float.isNaN(childScaleY)) {
          continue;
        }

        childView.setScaleX(childScaleX);
        childView.setScaleY(childScaleY);
      }
    }
  }

  public void finishBackProgress(
      @NonNull BackEventCompat backEvent,
      @GravityInt int gravity,
      @Nullable AnimatorListener animatorListener,
      @Nullable AnimatorUpdateListener finishAnimatorUpdateListener) {
    boolean leftSwipeEdge = backEvent.getSwipeEdge() == BackEventCompat.EDGE_LEFT;
    boolean leftGravity = checkAbsoluteGravity(gravity, Gravity.LEFT);
    float scaledWidth = view.getWidth() * view.getScaleX() + getEdgeMargin(leftGravity);
    ObjectAnimator finishAnimator =
        ObjectAnimator.ofFloat(view, View.TRANSLATION_X, leftGravity ? -scaledWidth : scaledWidth);
    if (finishAnimatorUpdateListener != null) {
      finishAnimator.addUpdateListener(finishAnimatorUpdateListener);
    }
    finishAnimator.setInterpolator(new FastOutSlowInInterpolator());
    finishAnimator.setDuration(
        AnimationUtils.lerp(hideDurationMax, hideDurationMin, backEvent.getProgress()));
    finishAnimator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            view.setTranslationX(0);
            updateBackProgress(/* progress= */ 0, leftSwipeEdge, gravity);
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

    AnimatorSet cancelAnimatorSet = new AnimatorSet();
    cancelAnimatorSet.playTogether(
        ObjectAnimator.ofFloat(view, View.SCALE_X, 1),
        ObjectAnimator.ofFloat(view, View.SCALE_Y, 1));

    if (view instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) view;
      for (int i = 0; i < viewGroup.getChildCount(); i++) {
        View childView = viewGroup.getChildAt(i);
        cancelAnimatorSet.playTogether(ObjectAnimator.ofFloat(childView, View.SCALE_Y, 1));
      }
    }

    cancelAnimatorSet.setDuration(cancelDuration);
    cancelAnimatorSet.start();
  }

  private boolean checkAbsoluteGravity(@GravityInt int gravity, @GravityInt int checkFor) {
    int absoluteGravity = Gravity.getAbsoluteGravity(gravity, view.getLayoutDirection());
    return (absoluteGravity & checkFor) == checkFor;
  }

  private int getEdgeMargin(boolean leftGravity) {
    LayoutParams layoutParams = view.getLayoutParams();
    if (layoutParams instanceof MarginLayoutParams) {
      MarginLayoutParams marginLayoutParams = (MarginLayoutParams) layoutParams;
      return leftGravity ? marginLayoutParams.leftMargin : marginLayoutParams.rightMargin;
    }
    return 0;
  }
}
