/*
 * Copyright (C) 2017 The Android Open Source Project
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

import static android.view.View.TRANSLATION_Y;
import static android.view.View.VISIBLE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.animation.AnimatorSetCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.Space;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for indicator views underneath the text input line in {@link
 * com.google.android.material.textfield.TextInputLayout}. This class controls helper and error views.
 */
final class IndicatorViewController {
  /*
   * TODO: Update placeholder values for caption animation.
   *
   */

  /** Duration for the caption's vertical translation animation. */
  private static final int CAPTION_TRANSLATE_Y_ANIMATION_DURATION = 217;

  /** Duration for the caption's opacity fade animation. */
  private static final int CAPTION_OPACITY_FADE_ANIMATION_DURATION = 167;

  /**
   * Values for indicator indices. Indicators are views below the text input area, like a caption
   * (error text or helper text) or a character counter.
   */
  @IntDef({COUNTER_INDEX, ERROR_INDEX, HELPER_INDEX})
  @Retention(RetentionPolicy.SOURCE)
  private @interface IndicatorIndex {}

  static final int ERROR_INDEX = 0;
  static final int HELPER_INDEX = 1;
  static final int COUNTER_INDEX = 2;

  /**
   * Values for caption display state constants. There is either an error displayed, helper text
   * displayed, or no caption.
   */
  @IntDef({CAPTION_STATE_NONE, CAPTION_STATE_ERROR, CAPTION_STATE_HELPER_TEXT})
  @Retention(RetentionPolicy.SOURCE)
  private @interface CaptionDisplayState {}

  private static final int CAPTION_STATE_NONE = 0;
  private static final int CAPTION_STATE_ERROR = 1;
  private static final int CAPTION_STATE_HELPER_TEXT = 2;

  private final Context context;
  private final TextInputLayout textInputView;

  private LinearLayout indicatorArea;
  private int indicatorsAdded;

  private FrameLayout captionArea;
  private int captionViewsAdded;
  @Nullable private Animator captionAnimator;
  private final float captionTranslationYPx;
  private int captionDisplayed;
  private int captionToShow;

  private CharSequence errorText;
  private boolean errorEnabled;
  private TextView errorView;
  private int errorTextAppearance;

  private CharSequence helperText;
  private boolean helperTextEnabled;
  private TextView helperTextView;
  private int helperTextTextAppearance;

  private Typeface typeface;

  public IndicatorViewController(TextInputLayout textInputView) {
    this.context = textInputView.getContext();
    this.textInputView = textInputView;
    this.captionTranslationYPx =
        context.getResources().getDimensionPixelSize(R.dimen.design_textinput_caption_translate_y);
  }

  void showHelper(final CharSequence helperText) {
    cancelCaptionAnimator();
    this.helperText = helperText;
    helperTextView.setText(helperText);

    // If helper is not already shown, show helper.
    if (captionDisplayed != CAPTION_STATE_HELPER_TEXT) {
      captionToShow = CAPTION_STATE_HELPER_TEXT;
    }
    updateCaptionViewsVisibility(
        captionDisplayed, captionToShow, shouldAnimateCaptionView(helperTextView, helperText));
  }

  void hideHelperText() {
    cancelCaptionAnimator();

    // Hide helper if it's shown.
    if (captionDisplayed == CAPTION_STATE_HELPER_TEXT) {
      captionToShow = CAPTION_STATE_NONE;
    }
    updateCaptionViewsVisibility(
        captionDisplayed, captionToShow, shouldAnimateCaptionView(helperTextView, null));
  }

  void showError(final CharSequence errorText) {
    cancelCaptionAnimator();
    this.errorText = errorText;
    errorView.setText(errorText);

    // If error is not already shown, show error.
    if (captionDisplayed != CAPTION_STATE_ERROR) {
      captionToShow = CAPTION_STATE_ERROR;
    }
    updateCaptionViewsVisibility(
        captionDisplayed, captionToShow, shouldAnimateCaptionView(errorView, errorText));
  }

  void hideError() {
    errorText = null;
    cancelCaptionAnimator();
    // Hide  error if it's shown.
    if (captionDisplayed == CAPTION_STATE_ERROR) {
      // If helper text is enabled and not empty, show helper text in place of the error.
      if (helperTextEnabled && !TextUtils.isEmpty(helperText)) {
        captionToShow = CAPTION_STATE_HELPER_TEXT;
      } else {
        // Otherwise, just hide the error.
        captionToShow = CAPTION_STATE_NONE;
      }
    }
    updateCaptionViewsVisibility(
        captionDisplayed, captionToShow, shouldAnimateCaptionView(errorView, null));
  }

  /**
   * Check if the caption view should animate. Only animate the caption view if we're enabled, laid
   * out, and have a different caption message.
   *
   * @param captionView The view that contains text for the caption underneath the text input area
   * @param captionText The text for the caption view
   * @return Whether the view should animate when setting the caption
   */
  private boolean shouldAnimateCaptionView(
      TextView captionView, @Nullable final CharSequence captionText) {
    return ViewCompat.isLaidOut(textInputView)
        && textInputView.isEnabled()
        && (captionToShow != captionDisplayed
            || captionView == null
            || !TextUtils.equals(captionView.getText(), captionText));
  }

  private void updateCaptionViewsVisibility(
      final @CaptionDisplayState int captionToHide,
      final @CaptionDisplayState int captionToShow,
      boolean animate) {

    if (animate) {
      final AnimatorSet captionAnimator = new AnimatorSet();
      this.captionAnimator = captionAnimator;
      List<Animator> captionAnimatorList = new ArrayList<>();

      createCaptionAnimators(
          captionAnimatorList,
          helperTextEnabled,
          helperTextView,
          CAPTION_STATE_HELPER_TEXT,
          captionToHide,
          captionToShow);

      createCaptionAnimators(
          captionAnimatorList,
          errorEnabled,
          errorView,
          CAPTION_STATE_ERROR,
          captionToHide,
          captionToShow);

      AnimatorSetCompat.playTogether(captionAnimator, captionAnimatorList);
      final TextView captionViewToHide = getCaptionViewFromDisplayState(captionToHide);
      final TextView captionViewToShow = getCaptionViewFromDisplayState(captionToShow);

      captionAnimator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
              captionDisplayed = captionToShow;
              IndicatorViewController.this.captionAnimator = null;
              if (captionViewToHide != null) {
                captionViewToHide.setVisibility(View.INVISIBLE);
                if (captionToHide == CAPTION_STATE_ERROR && errorView != null) {
                  errorView.setText(null);
                }
              }
            }

            @Override
            public void onAnimationStart(Animator animator) {
              if (captionViewToShow != null) {
                captionViewToShow.setVisibility(VISIBLE);
              }
            }
          });
      captionAnimator.start();
    } else {
      setCaptionViewVisibilities(captionToHide, captionToShow);
    }
    textInputView.updateEditTextBackground();
    textInputView.updateLabelState(animate);
    textInputView.updateTextInputBoxState();
  }

  private void setCaptionViewVisibilities(
      @CaptionDisplayState int captionToHide, @CaptionDisplayState int captionToShow) {
    if (captionToHide == captionToShow) {
      return;
    }

    if (captionToShow != CAPTION_STATE_NONE) {
      TextView captionViewToShow = getCaptionViewFromDisplayState(captionToShow);
      if (captionViewToShow != null) {
        captionViewToShow.setVisibility(VISIBLE);
        captionViewToShow.setAlpha(1f);
      }
    }

    if (captionToHide != CAPTION_STATE_NONE) {
      TextView captionViewDisplayed = getCaptionViewFromDisplayState(captionToHide);
      if (captionViewDisplayed != null) {
        captionViewDisplayed.setVisibility(View.INVISIBLE);
        // Only set the caption text to null if it's the error.
        if (captionToHide == CAPTION_STATE_ERROR) {
          captionViewDisplayed.setText(null);
        }
      }
    }
    captionDisplayed = captionToShow;
  }

  private void createCaptionAnimators(
      List<Animator> captionAnimatorList,
      boolean captionEnabled,
      TextView captionView,
      @CaptionDisplayState int captionState,
      @CaptionDisplayState int captionToHide,
      @CaptionDisplayState int captionToShow) {
    // If caption view is null or not enabled, do nothing.
    if (captionView == null || !captionEnabled) {
      return;
    }
    // If the caption view should be shown, set alpha to 1f.
    if ((captionState == captionToShow) || (captionState == captionToHide)) {
      captionAnimatorList.add(
          createCaptionOpacityAnimator(captionView, captionToShow == captionState));
      if (captionToShow == captionState) {
        captionAnimatorList.add(createCaptionTranslationYAnimator(captionView));
      }
    }
  }

  private ObjectAnimator createCaptionOpacityAnimator(TextView captionView, boolean display) {
    float endValue = display ? 1f : 0f;
    ObjectAnimator opacityAnimator = ObjectAnimator.ofFloat(captionView, View.ALPHA, endValue);
    opacityAnimator.setDuration(CAPTION_OPACITY_FADE_ANIMATION_DURATION);
    opacityAnimator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    return opacityAnimator;
  }

  private ObjectAnimator createCaptionTranslationYAnimator(TextView captionView) {
    ObjectAnimator translationYAnimator =
        ObjectAnimator.ofFloat(captionView, TRANSLATION_Y, -captionTranslationYPx, 0f);
    translationYAnimator.setDuration(CAPTION_TRANSLATE_Y_ANIMATION_DURATION);
    translationYAnimator.setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
    return translationYAnimator;
  }

  void cancelCaptionAnimator() {
    if (captionAnimator != null) {
      captionAnimator.cancel();
    }
  }

  boolean isCaptionView(@IndicatorIndex int index) {
    return index == ERROR_INDEX || index == HELPER_INDEX;
  }

  @Nullable
  private TextView getCaptionViewFromDisplayState(@CaptionDisplayState int captionDisplayState) {
    switch (captionDisplayState) {
      case CAPTION_STATE_ERROR:
        return errorView;
      case CAPTION_STATE_HELPER_TEXT:
        return helperTextView;
      default: // No caption displayed, fall out and return null.
    }
    return null;
  }

  void adjustIndicatorPadding() {
    if (canAdjustIndicatorPadding()) {
      // Add padding to the indicators so that they match the EditText
      ViewCompat.setPaddingRelative(
          indicatorArea,
          ViewCompat.getPaddingStart(textInputView.getEditText()),
          0,
          ViewCompat.getPaddingEnd(textInputView.getEditText()),
          0);
    }
  }

  private boolean canAdjustIndicatorPadding() {
    return indicatorArea != null && textInputView.getEditText() != null;
  }

  void addIndicator(TextView indicator, @IndicatorIndex int index) {
    if (indicatorArea == null && captionArea == null) {
      indicatorArea = new LinearLayout(context);
      indicatorArea.setOrientation(LinearLayout.HORIZONTAL);
      textInputView.addView(indicatorArea, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

      captionArea = new FrameLayout(context);
      indicatorArea.addView(
          captionArea,
          -1,
          new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

      final Space spacer = new Space(context);
      final LayoutParams spacerLp = new LinearLayout.LayoutParams(0, 0, 1f);
      indicatorArea.addView(spacer, spacerLp);

      if (textInputView.getEditText() != null) {
        adjustIndicatorPadding();
      }
    }

    if (isCaptionView(index)) {
      captionArea.setVisibility(VISIBLE);
      captionArea.addView(indicator);
      captionViewsAdded++;
    } else {
      indicatorArea.addView(indicator, index);
    }
    indicatorArea.setVisibility(VISIBLE);
    indicatorsAdded++;
  }

  void removeIndicator(TextView indicator, @IndicatorIndex int index) {
    if (indicatorArea == null) {
      return;
    }

    if (isCaptionView(index) && captionArea != null) {
      captionViewsAdded--;
      setViewGroupGoneIfEmpty(captionArea, captionViewsAdded);
      captionArea.removeView(indicator);
    } else {
      indicatorArea.removeView(indicator);
    }
    indicatorsAdded--;
    setViewGroupGoneIfEmpty(indicatorArea, indicatorsAdded);
  }

  private void setViewGroupGoneIfEmpty(ViewGroup viewGroup, int indicatorsAdded) {
    if (indicatorsAdded == 0) {
      viewGroup.setVisibility(View.GONE);
    }
  }

  void setErrorEnabled(boolean enabled) {
    // If the enabled state is the same as before, do nothing.
    if (errorEnabled == enabled) {
      return;
    }

    // Otherwise, adjust enabled state.
    cancelCaptionAnimator();

    if (enabled) {
      errorView = new AppCompatTextView(context);
      errorView.setId(R.id.textinput_error);
      if (typeface != null) {
        errorView.setTypeface(typeface);
      }
      setErrorTextAppearance(errorTextAppearance);
      errorView.setVisibility(View.INVISIBLE);
      ViewCompat.setAccessibilityLiveRegion(errorView, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
      addIndicator(errorView, ERROR_INDEX);
    } else {
      hideError();
      removeIndicator(errorView, ERROR_INDEX);
      errorView = null;
      textInputView.updateEditTextBackground();
      textInputView.updateTextInputBoxState();
    }
    errorEnabled = enabled;
  }

  boolean isErrorEnabled() {
    return errorEnabled;
  }

  boolean isHelperTextEnabled() {
    return helperTextEnabled;
  }

  void setHelperTextEnabled(boolean enabled) {
    // If the enabled state is the same as before, do nothing.
    if (helperTextEnabled == enabled) {
      return;
    }

    // Otherwise, adjust enabled state.
    cancelCaptionAnimator();

    if (enabled) {
      helperTextView = new AppCompatTextView(context);
      helperTextView.setId(R.id.textinput_helper_text);
      if (typeface != null) {
        helperTextView.setTypeface(typeface);
      }
      helperTextView.setVisibility(View.INVISIBLE);
      ViewCompat.setAccessibilityLiveRegion(
          helperTextView, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
      setHelperTextAppearance(helperTextTextAppearance);
      addIndicator(helperTextView, HELPER_INDEX);
    } else {
      hideHelperText();
      removeIndicator(helperTextView, HELPER_INDEX);
      helperTextView = null;
      textInputView.updateEditTextBackground();
      textInputView.updateTextInputBoxState();
    }
    helperTextEnabled = enabled;
  }

  boolean errorIsDisplayed() {
    return isCaptionStateError(captionDisplayed);
  }

  boolean errorShouldBeShown() {
    return isCaptionStateError(captionToShow);
  }

  private boolean isCaptionStateError(@CaptionDisplayState int captionState) {
    return captionState == CAPTION_STATE_ERROR
        && errorView != null
        && !TextUtils.isEmpty(errorText);
  }

  boolean helperTextIsDisplayed() {
    return isCaptionStateHelperText(captionDisplayed);
  }

  boolean helperTextShouldBeShown() {
    return isCaptionStateHelperText(captionToShow);
  }

  private boolean isCaptionStateHelperText(@CaptionDisplayState int captionState) {
    return captionState == CAPTION_STATE_HELPER_TEXT
        && helperTextView != null
        && !TextUtils.isEmpty(helperText);
  }

  CharSequence getErrorText() {
    return errorText;
  }

  CharSequence getHelperText() {
    return helperText;
  }

  @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
  void setTypefaces(Typeface typeface) {
    if (typeface != this.typeface) {
      this.typeface = typeface;
      setTextViewTypeface(errorView, typeface);
      setTextViewTypeface(helperTextView, typeface);
    }
  }

  private void setTextViewTypeface(@Nullable TextView captionView, Typeface typeface) {
    if (captionView != null) {
      captionView.setTypeface(typeface);
    }
  }

  @ColorInt
  int getErrorViewCurrentTextColor() {
    return errorView != null ? errorView.getCurrentTextColor() : -1;
  }

  @Nullable
  ColorStateList getErrorViewTextColors() {
    return errorView != null ? errorView.getTextColors() : null;
  }

  void setErrorViewTextColor(@Nullable ColorStateList textColors) {
    if (errorView != null) {
      errorView.setTextColor(textColors);
    }
  }

  void setErrorTextAppearance(@StyleRes int resId) {
    errorTextAppearance = resId;
    if (errorView != null) {
      textInputView.setTextAppearanceCompatWithErrorFallback(errorView, resId);
    }
  }

  @ColorInt
  int getHelperTextViewCurrentTextColor() {
    return helperTextView != null ? helperTextView.getCurrentTextColor() : -1;
  }

  @Nullable
  ColorStateList getHelperTextViewColors() {
    return helperTextView != null ? helperTextView.getTextColors() : null;
  }

  void setHelperTextViewTextColor(@Nullable ColorStateList textColors) {
    if (helperTextView != null) {
      helperTextView.setTextColor(textColors);
    }
  }

  void setHelperTextAppearance(@StyleRes int resId) {
    helperTextTextAppearance = resId;
    if (helperTextView != null) {
      TextViewCompat.setTextAppearance(helperTextView, resId);
    }
  }
}
