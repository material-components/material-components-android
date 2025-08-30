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

package com.google.android.material.textfield;

import com.google.android.material.R;

import static com.google.android.material.textfield.IconHelper.applyIconTint;
import static com.google.android.material.textfield.IconHelper.convertScaleType;
import static com.google.android.material.textfield.IconHelper.refreshIconDrawableState;
import static com.google.android.material.textfield.IconHelper.setCompatRippleBackgroundIfNeeded;
import static com.google.android.material.textfield.IconHelper.setIconMinSize;
import static com.google.android.material.textfield.IconHelper.setIconOnClickListener;
import static com.google.android.material.textfield.IconHelper.setIconOnLongClickListener;
import static com.google.android.material.textfield.IconHelper.setIconScaleType;
import static com.google.android.material.textfield.IconHelper.updateIconTooltip;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.TintTypedArray;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.StyleRes;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.widget.TextViewCompat;
import com.google.android.material.internal.CheckableImageButton;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;

/**
 * A compound layout that includes views that will be shown at the start of {@link TextInputLayout}
 * and their relevant rendering and presenting logic.
 */
@SuppressLint("ViewConstructor")
class StartCompoundLayout extends LinearLayout {

  private final TextInputLayout textInputLayout;

  private final TextView prefixTextView;
  @Nullable private CharSequence prefixText;

  private final CheckableImageButton startIconView;
  private ColorStateList startIconTintList;
  private PorterDuff.Mode startIconTintMode;
  private int startIconMinSize;
  @NonNull private ScaleType startIconScaleType;
  private OnLongClickListener startIconOnLongClickListener;

  private boolean hintExpanded;

  StartCompoundLayout(TextInputLayout textInputLayout, TintTypedArray a) {
    super(textInputLayout.getContext());

    this.textInputLayout = textInputLayout;

    setVisibility(GONE);
    setOrientation(HORIZONTAL);
    setLayoutParams(
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.START | Gravity.LEFT));

    final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
    startIconView =
        (CheckableImageButton)
            layoutInflater.inflate(R.layout.design_text_input_start_icon, this, false);
    setCompatRippleBackgroundIfNeeded(startIconView);

    prefixTextView = new AppCompatTextView(getContext());

    initStartIconView(a);
    initPrefixTextView(a);

    addView(startIconView);
    addView(prefixTextView);

    startIconView.setOnFocusableChangedListener(
        (v, focusable) ->
            updateIconTooltip(
                startIconView, startIconOnLongClickListener, getStartIconContentDescription()));
  }

  private void initStartIconView(TintTypedArray a) {

    if (MaterialResources.isFontScaleAtLeast1_3(getContext())) {
      ViewGroup.MarginLayoutParams lp =
          (ViewGroup.MarginLayoutParams) startIconView.getLayoutParams();
      lp.setMarginEnd(0);
    }
    setStartIconOnClickListener(null);
    setStartIconOnLongClickListener(null);
    // Default tint for a start icon or value specified by user.
    if (a.hasValue(R.styleable.TextInputLayout_startIconTint)) {
      startIconTintList =
          MaterialResources.getColorStateList(
              getContext(), a, R.styleable.TextInputLayout_startIconTint);
    }
    // Default tint mode for a start icon or value specified by user.
    if (a.hasValue(R.styleable.TextInputLayout_startIconTintMode)) {
      startIconTintMode =
          ViewUtils.parseTintMode(
              a.getInt(R.styleable.TextInputLayout_startIconTintMode, -1), null);
    }
    // Set up start icon if any.
    if (a.hasValue(R.styleable.TextInputLayout_startIconDrawable)) {
      setStartIconDrawable(a.getDrawable(R.styleable.TextInputLayout_startIconDrawable));
      if (a.hasValue(R.styleable.TextInputLayout_startIconContentDescription)) {
        setStartIconContentDescription(
            a.getText(R.styleable.TextInputLayout_startIconContentDescription));
      }
      setStartIconCheckable(a.getBoolean(R.styleable.TextInputLayout_startIconCheckable, true));
    }
    setStartIconMinSize(
        a.getDimensionPixelSize(
            R.styleable.TextInputLayout_startIconMinSize,
            getResources().getDimensionPixelSize(R.dimen.mtrl_min_touch_target_size)));
    if (a.hasValue(R.styleable.TextInputLayout_startIconScaleType)) {
      setStartIconScaleType(
          convertScaleType(a.getInt(R.styleable.TextInputLayout_startIconScaleType, -1)));
    }
  }

  private void initPrefixTextView(TintTypedArray a) {
    prefixTextView.setVisibility(GONE);
    // Set up prefix view.
    prefixTextView.setId(R.id.textinput_prefix_text);
    prefixTextView.setLayoutParams(
        new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    prefixTextView.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);

    setPrefixTextAppearance(a.getResourceId(R.styleable.TextInputLayout_prefixTextAppearance, 0));
    if (a.hasValue(R.styleable.TextInputLayout_prefixTextColor)) {
      setPrefixTextColor(a.getColorStateList(R.styleable.TextInputLayout_prefixTextColor));
    }
    setPrefixText(a.getText(R.styleable.TextInputLayout_prefixText));
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    updatePrefixTextViewPadding();
  }

  @NonNull
  TextView getPrefixTextView() {
    return prefixTextView;
  }

  void setPrefixText(@Nullable final CharSequence prefixText) {
    this.prefixText = TextUtils.isEmpty(prefixText) ? null : prefixText;
    prefixTextView.setText(prefixText);
    updateVisibility();
  }

  /**
   * Returns the prefix text that was set to be displayed with {@link #setPrefixText(CharSequence)},
   * or <code>null</code> if there is no prefix text.
   *
   * @see #setPrefixText(CharSequence)
   */
  @Nullable
  CharSequence getPrefixText() {
    return prefixText;
  }

  void setPrefixTextColor(@NonNull ColorStateList prefixTextColor) {
    prefixTextView.setTextColor(prefixTextColor);
  }

  @Nullable
  ColorStateList getPrefixTextColor() {
    return prefixTextView.getTextColors();
  }

  void setPrefixTextAppearance(@StyleRes int prefixTextAppearance) {
    TextViewCompat.setTextAppearance(prefixTextView, prefixTextAppearance);
  }

  void setStartIconDrawable(@Nullable Drawable startIconDrawable) {
    startIconView.setImageDrawable(startIconDrawable);
    if (startIconDrawable != null) {
      applyIconTint(textInputLayout, startIconView, startIconTintList, startIconTintMode);
      setStartIconVisible(true);
      refreshStartIconDrawableState();
    } else {
      setStartIconVisible(false);
      setStartIconOnClickListener(null);
      setStartIconOnLongClickListener(null);
      setStartIconContentDescription(null);
    }
  }

  @Nullable
  Drawable getStartIconDrawable() {
    return startIconView.getDrawable();
  }

  void setStartIconOnClickListener(@Nullable OnClickListener startIconOnClickListener) {
    setIconOnClickListener(startIconView, startIconOnClickListener, startIconOnLongClickListener);
  }

  void setStartIconOnLongClickListener(
      @Nullable OnLongClickListener startIconOnLongClickListener) {
    this.startIconOnLongClickListener = startIconOnLongClickListener;
    setIconOnLongClickListener(startIconView, startIconOnLongClickListener);
  }

  void setStartIconVisible(boolean visible) {
    if (isStartIconVisible() != visible) {
      startIconView.setVisibility(visible ? View.VISIBLE : View.GONE);
      updatePrefixTextViewPadding();
      updateVisibility();
    }
  }

  boolean isStartIconVisible() {
    return startIconView.getVisibility() == View.VISIBLE;
  }

  void refreshStartIconDrawableState() {
    refreshIconDrawableState(textInputLayout, startIconView, startIconTintList);
  }

  void setStartIconCheckable(boolean startIconCheckable) {
    startIconView.setCheckable(startIconCheckable);
  }

  boolean isStartIconCheckable() {
    return startIconView.isCheckable();
  }

  void setStartIconContentDescription(@Nullable CharSequence startIconContentDescription) {
    if (getStartIconContentDescription() != startIconContentDescription) {
      startIconView.setContentDescription(startIconContentDescription);
      updateIconTooltip(startIconView, startIconOnLongClickListener, startIconContentDescription);
    }
  }

  @Nullable
  CharSequence getStartIconContentDescription() {
    return startIconView.getContentDescription();
  }

  void setStartIconTintList(@Nullable ColorStateList startIconTintList) {
    if (this.startIconTintList != startIconTintList) {
      this.startIconTintList = startIconTintList;
      applyIconTint(textInputLayout, startIconView, startIconTintList, startIconTintMode);
    }
  }

  void setStartIconTintMode(@Nullable PorterDuff.Mode startIconTintMode) {
    if (this.startIconTintMode != startIconTintMode) {
      this.startIconTintMode = startIconTintMode;
      applyIconTint(textInputLayout, startIconView, startIconTintList, this.startIconTintMode);
    }
  }

  void setStartIconMinSize(@Px int iconSize) {
    if (iconSize < 0) {
      throw new IllegalArgumentException("startIconSize cannot be less than 0");
    }
    if (iconSize != startIconMinSize) {
      startIconMinSize = iconSize;
      setIconMinSize(startIconView, iconSize);
    }
  }

  int getStartIconMinSize() {
    return startIconMinSize;
  }

  void setStartIconScaleType(@NonNull ScaleType startIconScaleType) {
    this.startIconScaleType = startIconScaleType;
    setIconScaleType(startIconView, startIconScaleType);
  }

  @NonNull
  ScaleType getStartIconScaleType() {
    return startIconScaleType;
  }

  void setupAccessibilityNodeInfo(@NonNull AccessibilityNodeInfoCompat info) {
    if (prefixTextView.getVisibility() == VISIBLE) {
      info.setLabelFor(prefixTextView);
      info.setTraversalAfter(prefixTextView);
    } else {
      info.setTraversalAfter(startIconView);
    }
  }

  void updatePrefixTextViewPadding() {
    EditText editText = textInputLayout.editText;
    if (editText == null) {
      return;
    }
    int startPadding = isStartIconVisible() ? 0 : editText.getPaddingStart();
    prefixTextView.setPaddingRelative(
        startPadding,
        editText.getCompoundPaddingTop(),
        getContext()
            .getResources()
            .getDimensionPixelSize(R.dimen.material_input_text_to_prefix_suffix_padding),
        editText.getCompoundPaddingBottom());
  }

  int getPrefixTextStartOffset() {
    int startIconOffset;
    if (isStartIconVisible()) {
      startIconOffset =
          startIconView.getMeasuredWidth()
              + ((MarginLayoutParams) startIconView.getLayoutParams()).getMarginEnd();
    } else {
      startIconOffset = 0;
    }
    return getPaddingStart()
        + prefixTextView.getPaddingStart()
        + startIconOffset;
  }

  void onHintStateChanged(boolean hintExpanded) {
    this.hintExpanded = hintExpanded;
    updateVisibility();
  }

  private void updateVisibility() {
    // Set startLayout to visible if start icon or prefix text is present.
    int prefixTextVisibility = (prefixText != null && !hintExpanded) ? VISIBLE : GONE;
    boolean shouldBeVisible =
        startIconView.getVisibility() == VISIBLE || prefixTextVisibility == VISIBLE;
    setVisibility(shouldBeVisible ? VISIBLE : GONE);
    // Set prefix visibility after updating layout's visibility so screen readers correctly announce
    // when prefix text appears.
    prefixTextView.setVisibility(prefixTextVisibility);
    textInputLayout.updateDummyDrawables();
  }
}
