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

package android.support.design.widget;

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
import android.support.design.R;
import android.support.design.animation.AnimationUtils;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for indicator views underneath the text input line in {@link
 * android.support.design.widget.TextInputLayout}. This class controls helper and error views.
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
  private @interface IndicatorIndex {}

  static final int ERROR_INDEX = 0;
  static final int HELPER_INDEX = 1;
  static final int COUNTER_INDEX = 2;

  /**
   * Values for caption display state constants. There is either an error displayed, helper text
   * displayed, or no caption.
   */
  @IntDef({CAPTION_STATE_NONE, CAPTION_STATE_ERROR, CAPTION_STATE_HELPER_TEXT})
  private @interface CaptionDisplayState {}

  private static final int CAPTION_STATE_NONE = 0;
  private static final int CAPTION_STATE_ERROR = 1;
  private static final int CAPTION_STATE_HELPER_TEXT = 2;

  private final Context context;
  private final TextInputLayout textInputView;

  private LinearLayout mIndicatorArea;
  private int mIndicatorsAdded;

  private FrameLayout mCaptionArea;
  private int mCaptionViewsAdded;
  @Nullable private Animator mCaptionAnimator;
  private final float mCaptionTranslationYPx;
  private int mCaptionDisplayed;
  private int mCaptionToShow;

  private CharSequence mErrorText;
  private boolean mErrorEnabled;
  private TextView mErrorView;
  private int mErrorTextAppearance;

  private CharSequence mHelperText;
  private boolean mHelperTextEnabled;
  private TextView mHelperTextView;
  private int mHelperTextTextAppearance;

  private Typeface mTypeface;

  public IndicatorViewController(TextInputLayout textInputView) {
    this.context = textInputView.getContext();
    this.textInputView = textInputView;
    this.mCaptionTranslationYPx =
        context.getResources().getDimensionPixelSize(R.dimen.design_textinput_caption_translate_y);
  }

  void showHelper(final CharSequence helperText) {
    cancelCaptionAnimator();
    mHelperText = helperText;
    mHelperTextView.setText(helperText);

    // If helper is not already shown, show helper.
    if (mCaptionDisplayed != CAPTION_STATE_HELPER_TEXT) {
      mCaptionToShow = CAPTION_STATE_HELPER_TEXT;
    }
    updateCaptionViewsVisibility(
        mCaptionDisplayed, mCaptionToShow, shouldAnimateCaptionView(mHelperTextView, helperText));
  }

  void hideHelperText() {
    cancelCaptionAnimator();

    // Hide helper if it's shown.
    if (mCaptionDisplayed == CAPTION_STATE_HELPER_TEXT) {
      mCaptionToShow = CAPTION_STATE_NONE;
    }
    updateCaptionViewsVisibility(
        mCaptionDisplayed, mCaptionToShow, shouldAnimateCaptionView(mHelperTextView, null));
  }

  void showError(final CharSequence errorText) {
    cancelCaptionAnimator();
    mErrorText = errorText;
    mErrorView.setText(errorText);

    // If error is not already shown, show error.
    if (mCaptionDisplayed != CAPTION_STATE_ERROR) {
      mCaptionToShow = CAPTION_STATE_ERROR;
    }
    updateCaptionViewsVisibility(
        mCaptionDisplayed, mCaptionToShow, shouldAnimateCaptionView(mErrorView, errorText));
  }

  void hideError() {
    mErrorText = null;
    cancelCaptionAnimator();
    // Hide  error if it's shown.
    if (mCaptionDisplayed == CAPTION_STATE_ERROR) {
      // If helper text is enabled and not empty, show helper text in place of the error.
      if (mHelperTextEnabled && !TextUtils.isEmpty(mHelperText)) {
        mCaptionToShow = CAPTION_STATE_HELPER_TEXT;
      } else {
        // Otherwise, just hide the error.
        mCaptionToShow = CAPTION_STATE_NONE;
      }
    }
    updateCaptionViewsVisibility(
        mCaptionDisplayed, mCaptionToShow, shouldAnimateCaptionView(mErrorView, null));
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
        && (mCaptionToShow != mCaptionDisplayed
            || captionView == null
            || !TextUtils.equals(captionView.getText(), captionText));
  }

  private void updateCaptionViewsVisibility(
      final @CaptionDisplayState int captionToHide,
      final @CaptionDisplayState int captionToShow,
      boolean animate) {

    if (animate) {
      final AnimatorSet captionAnimator = new AnimatorSet();
      mCaptionAnimator = captionAnimator;
      List<Animator> captionAnimatorList = new ArrayList<>();

      createCaptionAnimators(
          captionAnimatorList,
          mHelperTextEnabled,
          mHelperTextView,
          CAPTION_STATE_HELPER_TEXT,
          captionToHide,
          captionToShow);

      createCaptionAnimators(
          captionAnimatorList,
          mErrorEnabled,
          mErrorView,
          CAPTION_STATE_ERROR,
          captionToHide,
          captionToShow);

      captionAnimator.playTogether(captionAnimatorList);
      final TextView captionViewToHide = getCaptionViewFromDisplayState(captionToHide);
      final TextView captionViewToShow = getCaptionViewFromDisplayState(captionToShow);

      captionAnimator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
              mCaptionDisplayed = captionToShow;
              mCaptionAnimator = null;
              if (captionViewToHide != null) {
                captionViewToHide.setVisibility(View.INVISIBLE);
                if (captionToHide == CAPTION_STATE_ERROR && mErrorView != null) {
                  mErrorView.setText(null);
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
    mCaptionDisplayed = captionToShow;
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
        ObjectAnimator.ofFloat(captionView, TRANSLATION_Y, -mCaptionTranslationYPx, 0f);
    translationYAnimator.setDuration(CAPTION_TRANSLATE_Y_ANIMATION_DURATION);
    translationYAnimator.setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
    return translationYAnimator;
  }

  void cancelCaptionAnimator() {
    if (mCaptionAnimator != null) {
      mCaptionAnimator.cancel();
    }
  }

  boolean isCaptionView(@IndicatorIndex int index) {
    return index == ERROR_INDEX || index == HELPER_INDEX;
  }

  @Nullable
  private TextView getCaptionViewFromDisplayState(@CaptionDisplayState int captionDisplayState) {
    switch (captionDisplayState) {
      case CAPTION_STATE_ERROR:
        return mErrorView;
      case CAPTION_STATE_HELPER_TEXT:
        return mHelperTextView;
      default: // No caption displayed, fall out and return null.
    }
    return null;
  }

  void adjustIndicatorPadding() {
    if (canAdjustIndicatorPadding()) {
      // Add padding to the indicators so that they match the EditText
      ViewCompat.setPaddingRelative(
          mIndicatorArea,
          ViewCompat.getPaddingStart(textInputView.getEditText()),
          0,
          ViewCompat.getPaddingEnd(textInputView.getEditText()),
          textInputView.getEditText().getPaddingBottom());
    }
  }

  private boolean canAdjustIndicatorPadding() {
    return mIndicatorArea != null && textInputView.getEditText() != null;
  }

  void addIndicator(TextView indicator, @IndicatorIndex int index) {
    if (mIndicatorArea == null && mCaptionArea == null) {
      mIndicatorArea = new LinearLayout(context);
      mIndicatorArea.setOrientation(LinearLayout.HORIZONTAL);
      textInputView.addView(mIndicatorArea, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

      mCaptionArea = new FrameLayout(context);
      mIndicatorArea.addView(
          mCaptionArea,
          -1,
          new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

      final Space spacer = new Space(context);
      final LayoutParams spacerLp = new LinearLayout.LayoutParams(0, 0, 1f);
      mIndicatorArea.addView(spacer, spacerLp);

      if (textInputView.getEditText() != null) {
        adjustIndicatorPadding();
      }
    }

    if (isCaptionView(index)) {
      mCaptionArea.setVisibility(VISIBLE);
      mCaptionArea.addView(indicator);
      mCaptionViewsAdded++;
    } else {
      mIndicatorArea.addView(indicator, index);
    }
    mIndicatorArea.setVisibility(VISIBLE);
    mIndicatorsAdded++;
  }

  void removeIndicator(TextView indicator, @IndicatorIndex int index) {
    if (mIndicatorArea == null) {
      return;
    }

    if (isCaptionView(index) && mCaptionArea != null) {
      mCaptionViewsAdded--;
      setViewGroupGoneIfEmpty(mCaptionArea, mCaptionViewsAdded);
      mCaptionArea.removeView(indicator);
    } else {
      mIndicatorArea.removeView(indicator);
    }
    mIndicatorsAdded--;
    setViewGroupGoneIfEmpty(mIndicatorArea, mIndicatorsAdded);
  }

  private void setViewGroupGoneIfEmpty(ViewGroup viewGroup, int indicatorsAdded) {
    if (indicatorsAdded == 0) {
      viewGroup.setVisibility(View.GONE);
    }
  }

  void setErrorEnabled(boolean enabled) {
    // If the enabled state is the same as before, do nothing.
    if (mErrorEnabled == enabled) {
      return;
    }

    // Otherwise, adjust enabled state.
    cancelCaptionAnimator();

    if (enabled) {
      mErrorView = new AppCompatTextView(context);
      mErrorView.setId(R.id.textinput_error);
      if (mTypeface != null) {
        mErrorView.setTypeface(mTypeface);
      }
      setErrorTextAppearance(mErrorTextAppearance);
      mErrorView.setVisibility(View.INVISIBLE);
      ViewCompat.setAccessibilityLiveRegion(
          mErrorView, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
      addIndicator(mErrorView, ERROR_INDEX);
    } else {
      hideError();
      removeIndicator(mErrorView, ERROR_INDEX);
      mErrorView = null;
      textInputView.updateEditTextBackground();
    }
    mErrorEnabled = enabled;
  }

  boolean isErrorEnabled() {
    return mErrorEnabled;
  }

  boolean isHelperTextEnabled() {
    return mHelperTextEnabled;
  }

  void setHelperTextEnabled(boolean enabled) {
    // If the enabled state is the same as before, do nothing.
    if (mHelperTextEnabled == enabled) {
      return;
    }

    // Otherwise, adjust enabled state.
    cancelCaptionAnimator();

    if (enabled) {
      mHelperTextView = new AppCompatTextView(context);
      mHelperTextView.setId(R.id.textinput_helper_text);
      if (mTypeface != null) {
        mHelperTextView.setTypeface(mTypeface);
      }
      mHelperTextView.setVisibility(View.INVISIBLE);
      ViewCompat.setAccessibilityLiveRegion(
          mHelperTextView, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
      setHelperTextAppearance(mHelperTextTextAppearance);
      addIndicator(mHelperTextView, HELPER_INDEX);
    } else {
      hideHelperText();
      removeIndicator(mHelperTextView, HELPER_INDEX);
      mHelperTextView = null;
      textInputView.updateEditTextBackground();
    }
    mHelperTextEnabled = enabled;
  }

  boolean errorIsDisplayed() {
    return mCaptionDisplayed == CAPTION_STATE_ERROR
        && mErrorView != null
        && !TextUtils.isEmpty(mErrorText);
  }

  boolean errorShouldBeShown() {
    return mCaptionToShow == CAPTION_STATE_ERROR
        && mErrorView != null
        && !TextUtils.isEmpty(mErrorText);
  }

  CharSequence getErrorText() {
    return mErrorText;
  }

  CharSequence getHelperText() {
    return mHelperText;
  }

  @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
  void setTypefaces(Typeface typeface) {
    if (typeface != mTypeface) {
      mTypeface = typeface;
      setTextViewTypeface(mErrorView, typeface);
      setTextViewTypeface(mHelperTextView, typeface);
    }
  }

  private void setTextViewTypeface(@Nullable TextView captionView, Typeface typeface) {
    if (captionView != null) {
      captionView.setTypeface(typeface);
    }
  }

  @ColorInt
  int getErrorViewCurrentTextColor() {
    return mErrorView != null ? mErrorView.getCurrentTextColor() : -1;
  }

  @Nullable
  ColorStateList getErrorViewTextColors() {
    return mErrorView != null ? mErrorView.getTextColors() : null;
  }

  void setErrorTextAppearance(@StyleRes int resId) {
    mErrorTextAppearance = resId;
    if (mErrorView != null) {
      textInputView.setTextAppearanceCompatWithErrorFallback(mErrorView, resId);
    }
  }

  void setHelperTextAppearance(@StyleRes int resId) {
    mHelperTextTextAppearance = resId;
    if (mHelperTextView != null) {
      TextViewCompat.setTextAppearance(mHelperTextView, resId);
    }
  }
}
