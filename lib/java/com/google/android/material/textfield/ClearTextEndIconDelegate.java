/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.google.android.material.textfield;

import com.google.android.material.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.text.Editable;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.motion.MotionUtils;

/** Default initialization of the clear text end icon {@link TextInputLayout.EndIconMode}. */
class ClearTextEndIconDelegate extends EndIconDelegate {

  private static final int DEFAULT_ANIMATION_FADE_DURATION = 100;
  private static final int DEFAULT_ANIMATION_SCALE_DURATION = 150;
  private static final float ANIMATION_SCALE_FROM_VALUE = 0.8f;
  private final int animationFadeDuration;
  private final int animationScaleDuration;
  @NonNull private final TimeInterpolator animationFadeInterpolator;
  @NonNull private final TimeInterpolator animationScaleInterpolator;

  @Nullable
  private EditText editText;

  private final OnClickListener onIconClickListener = view -> {
    if (editText == null) {
      return;
    }
    Editable text = editText.getText();
    if (text != null) {
      text.clear();
    }
    refreshIconState();
  };

  private final OnFocusChangeListener onFocusChangeListener =
      (view, hasFocus) -> animateIcon(shouldBeVisible());

  private AnimatorSet iconInAnim;
  private ValueAnimator iconOutAnim;

  ClearTextEndIconDelegate(@NonNull EndCompoundLayout endLayout) {
    super(endLayout);
    animationFadeDuration =
        MotionUtils.resolveThemeDuration(
            endLayout.getContext(), R.attr.motionDurationShort3, DEFAULT_ANIMATION_FADE_DURATION);
    animationScaleDuration =
        MotionUtils.resolveThemeDuration(
            endLayout.getContext(), R.attr.motionDurationShort3, DEFAULT_ANIMATION_SCALE_DURATION);
    animationFadeInterpolator =
        MotionUtils.resolveThemeInterpolator(
            endLayout.getContext(),
            R.attr.motionEasingLinearInterpolator,
            AnimationUtils.LINEAR_INTERPOLATOR);
    animationScaleInterpolator =
        MotionUtils.resolveThemeInterpolator(
            endLayout.getContext(),
            R.attr.motionEasingEmphasizedInterpolator,
            AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
  }

  @Override
  void setUp() {
    initAnimators();
  }

  @Override
  void tearDown() {
    if (editText != null) {
      editText.post(() ->
        // Make sure icon view is visible.
        animateIcon(/* show= */ true));
    }
  }

  @Override
  int getIconDrawableResId() {
    return R.drawable.mtrl_ic_cancel;
  }

  @Override
  int getIconContentDescriptionResId() {
    return R.string.clear_text_end_icon_content_description;
  }

  @Override
  void onSuffixVisibilityChanged(boolean visible) {
    if (endLayout.getSuffixText() == null) {
      return;
    }
    animateIcon(visible);
  }

  @Override
  OnClickListener getOnIconClickListener() {
    return onIconClickListener;
  }

  @Override
  public void onEditTextAttached(@Nullable EditText editText) {
    this.editText = editText;
    textInputLayout.setEndIconVisible(shouldBeVisible());
  }

  @Override
  void afterEditTextChanged(@NonNull Editable s) {
    if (endLayout.getSuffixText() != null) {
      return;
    }
    animateIcon(shouldBeVisible());
  }

  @Override
  OnFocusChangeListener getOnEditTextFocusChangeListener() {
    return onFocusChangeListener;
  }

  @Override
  OnFocusChangeListener getOnIconViewFocusChangeListener() {
    return onFocusChangeListener;
  }

  private void animateIcon(boolean show) {
    boolean shouldSkipAnimation = endLayout.isEndIconVisible() == show;
    if (show && !iconInAnim.isRunning()) {
      iconOutAnim.cancel();
      iconInAnim.start();
      if (shouldSkipAnimation) {
        iconInAnim.end();
      }
    } else if (!show) {
      iconInAnim.cancel();
      iconOutAnim.start();
      if (shouldSkipAnimation) {
        iconOutAnim.end();
      }
    }
  }

  private void initAnimators() {
    ValueAnimator scaleAnimator = getScaleAnimator();
    ValueAnimator fadeAnimator = getAlphaAnimator(0, 1);
    iconInAnim = new AnimatorSet();
    iconInAnim.playTogether(scaleAnimator, fadeAnimator);
    iconInAnim.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            endLayout.setEndIconVisible(true);
          }
        });
    iconOutAnim = getAlphaAnimator(1, 0);
    iconOutAnim.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            endLayout.setEndIconVisible(false);
          }
        });
  }

  private ValueAnimator getAlphaAnimator(float... values) {
    ValueAnimator animator = ValueAnimator.ofFloat(values);
    animator.setInterpolator(animationFadeInterpolator);
    animator.setDuration(animationFadeDuration);
    animator.addUpdateListener(animation -> {
      float alpha = (float) animation.getAnimatedValue();
      endIconView.setAlpha(alpha);
    });

    return animator;
  }

  private ValueAnimator getScaleAnimator() {
    ValueAnimator animator = ValueAnimator.ofFloat(ANIMATION_SCALE_FROM_VALUE, 1);
    animator.setInterpolator(animationScaleInterpolator);
    animator.setDuration(animationScaleDuration);
    animator.addUpdateListener(animation -> {
      float scale = (float) animation.getAnimatedValue();
      endIconView.setScaleX(scale);
      endIconView.setScaleY(scale);
    });
    return animator;
  }

  private boolean shouldBeVisible() {
    return editText != null
        && (editText.hasFocus() || endIconView.hasFocus())
        && editText.getText().length() > 0;
  }
}
