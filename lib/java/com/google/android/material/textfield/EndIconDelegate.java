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

import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener;
import android.widget.EditText;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import com.google.android.material.internal.CheckableImageButton;
import com.google.android.material.textfield.TextInputLayout.BoxBackgroundMode;

/**
 * End icon delegate abstract class.
 *
 * Each end icon delegate class that extends this class will provide the default configuration for
 * a specific {@link TextInputLayout} {@link TextInputLayout.EndIconMode}.
 */
abstract class EndIconDelegate {

  final TextInputLayout textInputLayout;
  final EndCompoundLayout endLayout;
  final Context context;
  final CheckableImageButton endIconView;

  EndIconDelegate(@NonNull EndCompoundLayout endLayout) {
    this.textInputLayout = endLayout.textInputLayout;
    this.endLayout = endLayout;
    this.context = endLayout.getContext();
    this.endIconView = endLayout.getEndIconView();
  }

  /** Called when the associated end icon mode is set. */
  void setUp() {}
  ;

  /** Called when the associated end icon mode is unset. */
  void tearDown() {}

  /** Returns the icon resource ID that should be used. */
  @DrawableRes
  int getIconDrawableResId() {
    return 0;
  }

  /** Returns the string resource ID that should be used as the content description. */
  @StringRes
  int getIconContentDescriptionResId() {
    return 0;
  }

  /**
   * Returns true if the end icon should be checkable.
   *
   * @see TextInputLayout#setEndIconCheckable(boolean)
   */
  boolean isIconCheckable() {
    return false;
  }

  /**
   * Returns true if the end icon should be checked. You will need to override
   * {@link #isIconCheckable()} to make this method work.
   */
  boolean isIconChecked() {
    return false;
  }

  /**
   * Returns true if the end icon should be activable.
   */
  boolean isIconActivable() {
    return false;
  }

  /**
   * Returns true if the end icon should be activated. You will need to override
   * {@link #isIconActivable()} to make this method work.
   *
   * @see TextInputLayout#setEndIconActivated(boolean)
   */
  boolean isIconActivated() {
    return false;
  }

  /**
   * Whether the end icon should be tinted with the error color when the {@link TextInputLayout} is
   * in error mode.
   */
  boolean shouldTintIconOnError() {
    return false;
  }

  /**
   * Whether the {@link BoxBackgroundMode} of the {@link TextInputLayout} is supported.
   *
   * @param boxBackgroundMode the text input's box background mode
   */
  boolean isBoxBackgroundModeSupported(@BoxBackgroundMode int boxBackgroundMode) {
    return true;
  }

  /**
   * This method should be implemented if the icon needs special handling of it's visibility
   * behavior when there is a suffix being displayed.
   *
   * @param visible whether the icon should be set to visible
   */
  void onSuffixVisibilityChanged(boolean visible) {}

  /**
   * Override this method to provide an {@link OnClickListener} to handle click events of the end
   * icon.
   */
  OnClickListener getOnIconClickListener() {
    return null;
  }

  /**
   * Override this method to provide an {@link OnFocusChangeListener} to handle focus change events
   * of the edit text.
   */
  OnFocusChangeListener getOnEditTextFocusChangeListener() {
    return null;
  }

  /**
   * Override this method to provide an {@link OnFocusChangeListener} to handle focus change events
   * of the end icon.
   */
  OnFocusChangeListener getOnIconViewFocusChangeListener() {
    return null;
  }

  /**
   * Override this method to provide a {@link TouchExplorationStateChangeListener} to handle touch
   * exploration state changes of the end icon.
   */
  TouchExplorationStateChangeListener getTouchExplorationStateChangeListener() {
    return null;
  }

  /** This method will be called when the edit text of the text input layout is attached. */
  void onEditTextAttached(@Nullable EditText editText) {}

  /**
   * This method will be called before the input text is going to be changed.
   *
   * @see android.text.TextWatcher#beforeTextChanged(CharSequence, int, int, int)
   */
  void beforeEditTextChanged(CharSequence s, int start, int count, int after) {}

  /**
   * This method will be called after the input text is changed.
   *
   * @see android.text.TextWatcher#afterTextChanged(Editable)
   */
  void afterEditTextChanged(Editable s) {}

  /**
   * This method will be called when the associated {@link TextInputLayout} is initializing the
   * accessibility node info.
   */
  void onInitializeAccessibilityNodeInfo(View host, @NonNull AccessibilityNodeInfoCompat info) {}

  /**
   * This method will be called when the associated {@link TextInputLayout} is populating a
   * accessibility event.
   */
  void onPopulateAccessibilityEvent(View host, @NonNull AccessibilityEvent event) {}

  final void refreshIconState() {
    endLayout.refreshIconState(/* force= */ false);
  }
}
