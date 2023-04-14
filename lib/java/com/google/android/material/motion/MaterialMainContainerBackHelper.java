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
import static com.google.android.material.animation.AnimationUtils.lerp;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.RoundedCorner;
import android.view.View;
import android.view.WindowInsets;
import android.window.BackEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.internal.ClippableRoundedCornerLayout;
import com.google.android.material.internal.ViewUtils;

/**
 * Utility class for main container views usually filling the entire screen (e.g., search view) that
 * support back progress animations.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class MaterialMainContainerBackHelper extends MaterialBackAnimationHelper<View> {

  private static final float MIN_SCALE = 0.9f;

  private final float minEdgeGap;
  private final float maxTranslationY;

  private float initialTouchY;
  @Nullable private Rect initialHideToClipBounds;
  @Nullable private Rect initialHideFromClipBounds;
  @Nullable private Integer expandedCornerSize;

  public MaterialMainContainerBackHelper(@NonNull View view) {
    super(view);

    Resources resources = view.getResources();
    minEdgeGap = resources.getDimension(R.dimen.m3_back_progress_main_container_min_edge_gap);
    maxTranslationY =
        resources.getDimension(R.dimen.m3_back_progress_main_container_max_translation_y);
  }

  @Nullable
  public Rect getInitialHideToClipBounds() {
    return initialHideToClipBounds;
  }

  @Nullable
  public Rect getInitialHideFromClipBounds() {
    return initialHideFromClipBounds;
  }

  @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void startBackProgress(@NonNull BackEvent backEvent, @NonNull View collapsedView) {
    super.onStartBackProgress(backEvent);

    startBackProgress(backEvent.getTouchY(), collapsedView);
  }

  @VisibleForTesting
  @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void startBackProgress(float touchY, @NonNull View collapsedView) {
    collapsedView.setVisibility(View.INVISIBLE);

    initialHideToClipBounds = ViewUtils.calculateRectFromBounds(view);
    initialHideFromClipBounds = ViewUtils.calculateOffsetRectFromBounds(view, collapsedView);
    initialTouchY = touchY;
  }

  @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void updateBackProgress(@NonNull BackEvent backEvent, float collapsedCornerSize) {
    super.onUpdateBackProgress(backEvent);

    boolean leftSwipeEdge = backEvent.getSwipeEdge() == BackEvent.EDGE_LEFT;
    updateBackProgress(
        backEvent.getProgress(), leftSwipeEdge, backEvent.getTouchY(), collapsedCornerSize);
  }

  @VisibleForTesting
  @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void updateBackProgress(
      float progress, boolean leftSwipeEdge, float touchY, float collapsedCornerSize) {
    float width = view.getWidth();
    float height = view.getHeight();
    float scale = lerp(1, MIN_SCALE, progress);

    float availableHorizontalSpace = max(0, (width - MIN_SCALE * width) / 2 - minEdgeGap);
    float translationX = lerp(0, availableHorizontalSpace, progress) * (leftSwipeEdge ? 1 : -1);

    float availableVerticalSpace = max(0, (height - scale * height) / 2 - minEdgeGap);
    float maxTranslationY = min(availableVerticalSpace, this.maxTranslationY);
    float yDelta = touchY - initialTouchY;
    float yProgress = Math.abs(yDelta) / height;
    float translationYDirection = Math.signum(yDelta);
    float translationY = AnimationUtils.lerp(0, maxTranslationY, yProgress) * translationYDirection;

    view.setScaleX(scale);
    view.setScaleY(scale);
    view.setTranslationX(translationX);
    view.setTranslationY(translationY);
    if (view instanceof ClippableRoundedCornerLayout) {
      ((ClippableRoundedCornerLayout) view)
          .updateCornerRadius(lerp(getExpandedCornerSize(), collapsedCornerSize, progress));
    }
  }

  @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void finishBackProgress(long duration, @NonNull View collapsedView) {
    AnimatorSet resetAnimator = createResetScaleAndTranslationAnimator(collapsedView);
    resetAnimator.setDuration(duration);
    resetAnimator.start();

    resetInitialValues();
  }

  @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void cancelBackProgress(@NonNull View collapsedView) {
    super.onCancelBackProgress();

    AnimatorSet cancelAnimatorSet = createResetScaleAndTranslationAnimator(collapsedView);
    if (view instanceof ClippableRoundedCornerLayout) {
      cancelAnimatorSet.playTogether(createCornerAnimator((ClippableRoundedCornerLayout) view));
    }
    cancelAnimatorSet.setDuration(cancelDuration);
    cancelAnimatorSet.start();

    resetInitialValues();
  }

  private void resetInitialValues() {
    initialTouchY = 0f;
    initialHideToClipBounds = null;
    initialHideFromClipBounds = null;
  }

  @NonNull
  private AnimatorSet createResetScaleAndTranslationAnimator(@NonNull View collapsedView) {
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(
        ObjectAnimator.ofFloat(view, View.SCALE_X, 1),
        ObjectAnimator.ofFloat(view, View.SCALE_Y, 1),
        ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0),
        ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0));
    animatorSet.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        collapsedView.setVisibility(View.VISIBLE);
      }
    });
    return animatorSet;
  }

  @NonNull
  private ValueAnimator createCornerAnimator(
      ClippableRoundedCornerLayout clippableRoundedCornerLayout) {
    ValueAnimator cornerAnimator =
        ValueAnimator.ofFloat(
            clippableRoundedCornerLayout.getCornerRadius(), getExpandedCornerSize());
    cornerAnimator.addUpdateListener(
        animation ->
            clippableRoundedCornerLayout.updateCornerRadius((Float) animation.getAnimatedValue()));
    return cornerAnimator;
  }

  public int getExpandedCornerSize() {
    if (expandedCornerSize == null) {
      expandedCornerSize = isAtTopOfScreen() ? getMaxDeviceCornerRadius() : 0;
    }
    return expandedCornerSize;
  }

  private boolean isAtTopOfScreen() {
    int[] location = new int[2];
    view.getLocationOnScreen(location);
    return location[1] == 0;
  }

  private int getMaxDeviceCornerRadius() {
    if (VERSION.SDK_INT >= VERSION_CODES.S) {
      final WindowInsets insets = view.getRootWindowInsets();
      if (insets != null) {
        return max(
            max(
                getRoundedCornerRadius(insets, RoundedCorner.POSITION_TOP_LEFT),
                getRoundedCornerRadius(insets, RoundedCorner.POSITION_TOP_RIGHT)),
            max(
                getRoundedCornerRadius(insets, RoundedCorner.POSITION_BOTTOM_LEFT),
                getRoundedCornerRadius(insets, RoundedCorner.POSITION_BOTTOM_RIGHT)));
      }
    }
    return 0;
  }

  @RequiresApi(VERSION_CODES.S)
  private int getRoundedCornerRadius(WindowInsets insets, int position) {
    final RoundedCorner roundedCorner = insets.getRoundedCorner(position);
    return roundedCorner != null ? roundedCorner.getRadius() : 0;
  }
}
