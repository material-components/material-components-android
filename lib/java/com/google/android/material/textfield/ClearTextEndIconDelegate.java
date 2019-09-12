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
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.textfield.TextInputLayout.OnEditTextAttachedListener;

/** Default initialization of the clear text end icon {@link TextInputLayout.EndIconMode}. */
class ClearTextEndIconDelegate extends EndIconDelegate {

  private static final int ANIMATION_FADE_DURATION = 100;
  private static final int ANIMATION_SCALE_DURATION = 150;
  private static final float ANIMATION_SCALE_FROM_VALUE = 0.8f;

  private final TextWatcher clearTextEndIconTextWatcher =
      new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(@NonNull Editable s) {
          if (hasText(s)) {
            if (!textInputLayout.isEndIconVisible()) {
              iconOutAnim.cancel();
              iconInAnim.start();
            }
          } else {
            iconInAnim.cancel();
            iconOutAnim.start();
          }
        }
      };
  private final OnEditTextAttachedListener clearTextOnEditTextAttachedListener =
      new OnEditTextAttachedListener() {
        @Override
        public void onEditTextAttached(@NonNull TextInputLayout textInputLayout) {
          EditText editText = textInputLayout.getEditText();
          textInputLayout.setEndIconVisible(hasText(editText.getText()));
          // Make sure there's always only one clear text text watcher added
          textInputLayout.setEndIconCheckable(false);
          editText.removeTextChangedListener(clearTextEndIconTextWatcher);
          editText.addTextChangedListener(clearTextEndIconTextWatcher);
        }
      };

  private AnimatorSet iconInAnim;
  private ValueAnimator iconOutAnim;

  ClearTextEndIconDelegate(@NonNull TextInputLayout textInputLayout) {
    super(textInputLayout);
  }

  @Override
  void initialize() {
    textInputLayout.setEndIconDrawable(
        AppCompatResources.getDrawable(context, R.drawable.mtrl_ic_cancel));
    textInputLayout.setEndIconContentDescription(
        textInputLayout.getResources().getText(R.string.clear_text_end_icon_content_description));
    textInputLayout.setEndIconOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            textInputLayout.getEditText().setText(null);
          }
        });
    textInputLayout.addOnEditTextAttachedListener(clearTextOnEditTextAttachedListener);
    initAnimators();
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
            textInputLayout.setEndIconVisible(true);
          }
        });
    iconOutAnim = getAlphaAnimator(1, 0);
    iconOutAnim.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            textInputLayout.setEndIconVisible(false);
          }
        });
  }

  private ValueAnimator getAlphaAnimator(float... values) {
    ValueAnimator animator = ValueAnimator.ofFloat(values);
    animator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    animator.setDuration(ANIMATION_FADE_DURATION);
    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(@NonNull ValueAnimator animation) {
            float alpha = (float) animation.getAnimatedValue();
            endIconView.setAlpha(alpha);
          }
        });

    return animator;
  }

  private ValueAnimator getScaleAnimator() {
    ValueAnimator animator = ValueAnimator.ofFloat(ANIMATION_SCALE_FROM_VALUE, 1);
    animator.setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
    animator.setDuration(ANIMATION_SCALE_DURATION);
    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(@NonNull ValueAnimator animation) {
            float scale = (float) animation.getAnimatedValue();
            endIconView.setScaleX(scale);
            endIconView.setScaleY(scale);
          }
        });
    return animator;
  }

  private static boolean hasText(@NonNull Editable editable) {
    return editable.length() > 0;
  }
}
