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

import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_CLICKED;
import static com.google.android.material.textfield.EditTextUtils.isEditable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.motion.MotionUtils;
import com.google.android.material.textfield.TextInputLayout.BoxBackgroundMode;

/** Default initialization of the exposed dropdown menu {@link TextInputLayout.EndIconMode}. */
class DropdownMenuEndIconDelegate extends EndIconDelegate {

  private static final int DEFAULT_ANIMATION_FADE_OUT_DURATION = 50;
  private static final int DEFAULT_ANIMATION_FADE_IN_DURATION = 67;
  private final int animationFadeOutDuration;
  private final int animationFadeInDuration;
  @NonNull private final TimeInterpolator animationFadeInterpolator;

  @Nullable
  private AutoCompleteTextView autoCompleteTextView;

  private final OnClickListener onIconClickListener = view -> showHideDropdown();

  private final OnFocusChangeListener onEditTextFocusChangeListener = (view, hasFocus) -> {
    editTextHasFocus = hasFocus;
    refreshIconState();
    if (!hasFocus) {
      setEndIconChecked(false);
      dropdownPopupDirty = false;
    }
  };

  private final TouchExplorationStateChangeListener touchExplorationStateChangeListener =
      (boolean enabled) -> {
        if (autoCompleteTextView != null && !isEditable(autoCompleteTextView)) {
          endIconView.setImportantForAccessibility(
              enabled ? View.IMPORTANT_FOR_ACCESSIBILITY_NO : View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
      };

  private boolean editTextHasFocus;
  private boolean dropdownPopupDirty;
  private boolean isEndIconChecked;
  private long dropdownPopupActivatedAt = Long.MAX_VALUE;
  @Nullable private AccessibilityManager accessibilityManager;
  private ValueAnimator fadeOutAnim;
  private ValueAnimator fadeInAnim;

  DropdownMenuEndIconDelegate(@NonNull EndCompoundLayout endLayout) {
    super(endLayout);
    animationFadeInDuration =
        MotionUtils.resolveThemeDuration(
            endLayout.getContext(),
            R.attr.motionDurationShort3,
            DEFAULT_ANIMATION_FADE_IN_DURATION);
    animationFadeOutDuration =
        MotionUtils.resolveThemeDuration(
            endLayout.getContext(),
            R.attr.motionDurationShort3,
            DEFAULT_ANIMATION_FADE_OUT_DURATION);
    animationFadeInterpolator =
        MotionUtils.resolveThemeInterpolator(
            endLayout.getContext(),
            R.attr.motionEasingLinearInterpolator,
            AnimationUtils.LINEAR_INTERPOLATOR);
  }

  @Override
  void setUp() {
    initAnimators();
    accessibilityManager =
        (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
  }

  @SuppressLint("ClickableViewAccessibility") // There's an accessibility delegate that handles
  // interactions with the dropdown menu.
  @Override
  void tearDown() {
    if (autoCompleteTextView != null) {
      // Remove any listeners set on the edit text.
      autoCompleteTextView.setOnTouchListener(null);
      autoCompleteTextView.setOnDismissListener(null);
    }
  }

  @Override
  public TouchExplorationStateChangeListener getTouchExplorationStateChangeListener() {
    return touchExplorationStateChangeListener;
  }

  @Override
  int getIconDrawableResId() {
    return R.drawable.mtrl_dropdown_arrow;
  }

  @Override
  int getIconContentDescriptionResId() {
    return R.string.exposed_dropdown_menu_content_description;
  }

  @Override
  boolean isIconCheckable() {
    return true;
  }

  @Override
  boolean isIconChecked() {
    return isEndIconChecked;
  }

  @Override
  boolean isIconActivable() {
    return true;
  }

  @Override
  boolean isIconActivated() {
    return editTextHasFocus;
  }

  @Override
  boolean shouldTintIconOnError() {
    return true;
  }

  @Override
  boolean isBoxBackgroundModeSupported(@BoxBackgroundMode int boxBackgroundMode) {
    return boxBackgroundMode != TextInputLayout.BOX_BACKGROUND_NONE;
  }

  @Override
  OnClickListener getOnIconClickListener() {
    return onIconClickListener;
  }

  @Override
  public void onEditTextAttached(@Nullable EditText editText) {
    this.autoCompleteTextView = castAutoCompleteTextViewOrThrow(editText);
    setUpDropdownShowHideBehavior();
    textInputLayout.setErrorIconDrawable(null);
    if (!isEditable(editText) && accessibilityManager.isTouchExplorationEnabled()) {
      endIconView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
    }
    textInputLayout.setEndIconVisible(true);
  }

  @Override
  public void afterEditTextChanged(Editable s) {
    // Don't show dropdown list if we're in a11y mode and the menu is editable.
    if (accessibilityManager.isTouchExplorationEnabled()
        && isEditable(autoCompleteTextView)
        && !endIconView.hasFocus()) {
      autoCompleteTextView.dismissDropDown();
    }
    autoCompleteTextView.post(() -> {
      boolean isPopupShowing = autoCompleteTextView.isPopupShowing();
      setEndIconChecked(isPopupShowing);
      dropdownPopupDirty = isPopupShowing;
    });
  }

  @Override
  OnFocusChangeListener getOnEditTextFocusChangeListener() {
    return onEditTextFocusChangeListener;
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(
      View host, @NonNull AccessibilityNodeInfoCompat info) {
    // The non-editable exposed dropdown menu behaves like a Spinner.
    if (!isEditable(autoCompleteTextView)) {
      info.setClassName(Spinner.class.getName());
    }
    if (info.isShowingHintText()) {
      // Set hint text to null so TalkBack doesn't announce the label twice when there is no
      // item selected.
      info.setHintText(null);
    }
  }

  @SuppressLint("WrongConstant")
  @Override
  public void onPopulateAccessibilityEvent(View host, @NonNull AccessibilityEvent event) {
    if (!accessibilityManager.isEnabled() || isEditable(autoCompleteTextView)) {
      return;
    }
    // TODO(b/256138189): Find better workaround, back gesture should call
    // AutoCompleteTextView.OnDismissListener.
    boolean invalidState =
        (event.getEventType() == AccessibilityEventCompat.TYPE_VIEW_ACCESSIBILITY_FOCUSED
                || event.getEventType() == AccessibilityEventCompat.CONTENT_CHANGE_TYPE_PANE_TITLE)
            && isEndIconChecked
            && !autoCompleteTextView.isPopupShowing();
    // If dropdown is non editable, layout click is what triggers showing/hiding the popup
    // list. Otherwise, arrow icon alone is what triggers it.
    if (event.getEventType() == TYPE_VIEW_CLICKED || invalidState) {
      showHideDropdown();
      updateDropdownPopupDirty();
    }
  }

  private void showHideDropdown() {
    if (autoCompleteTextView == null) {
      return;
    }
    if (isDropdownPopupActive()) {
      dropdownPopupDirty = false;
    }
    if (!dropdownPopupDirty) {
      setEndIconChecked(!isEndIconChecked);
      if (isEndIconChecked) {
        autoCompleteTextView.requestFocus();
        autoCompleteTextView.showDropDown();
      } else {
        autoCompleteTextView.dismissDropDown();
      }
    } else {
      dropdownPopupDirty = false;
    }
  }

  @SuppressLint("ClickableViewAccessibility") // There's an accessibility delegate that handles
  // interactions with the dropdown menu.
  private void setUpDropdownShowHideBehavior() {
    // Set whole layout clickable.
    autoCompleteTextView.setOnTouchListener((view, event) -> {
      if (event.getAction() == MotionEvent.ACTION_UP) {
        if (isDropdownPopupActive()) {
          dropdownPopupDirty = false;
        }
        showHideDropdown();
        updateDropdownPopupDirty();
      }
      return false;
    });
    autoCompleteTextView.setOnDismissListener(() -> {
      updateDropdownPopupDirty();
      setEndIconChecked(false);
    });
    autoCompleteTextView.setThreshold(0);
  }

  private boolean isDropdownPopupActive() {
    long activeFor = SystemClock.uptimeMillis() - dropdownPopupActivatedAt;
    return activeFor < 0 || activeFor > 300;
  }

  @NonNull
  private static AutoCompleteTextView castAutoCompleteTextViewOrThrow(EditText editText) {
    if (!(editText instanceof AutoCompleteTextView)) {
      throw new RuntimeException(
          "EditText needs to be an AutoCompleteTextView if an Exposed Dropdown Menu is being"
              + " used.");
    }

    return (AutoCompleteTextView) editText;
  }

  private void updateDropdownPopupDirty() {
    dropdownPopupDirty = true;
    dropdownPopupActivatedAt = SystemClock.uptimeMillis();
  }

  private void setEndIconChecked(boolean checked) {
    if (isEndIconChecked != checked) {
      isEndIconChecked = checked;
      fadeInAnim.cancel();
      fadeOutAnim.start();
    }
  }

  private void initAnimators() {
    fadeInAnim = getAlphaAnimator(animationFadeInDuration, 0, 1);
    fadeOutAnim = getAlphaAnimator(animationFadeOutDuration, 1, 0);
    fadeOutAnim.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            refreshIconState();
            fadeInAnim.start();
          }
        });
  }

  private ValueAnimator getAlphaAnimator(int duration, float... values) {
    ValueAnimator animator = ValueAnimator.ofFloat(values);
    animator.setInterpolator(animationFadeInterpolator);
    animator.setDuration(duration);
    animator.addUpdateListener(animation -> {
      float alpha = (float) animation.getAnimatedValue();
      endIconView.setAlpha(alpha);
    });

    return animator;
  }
}
