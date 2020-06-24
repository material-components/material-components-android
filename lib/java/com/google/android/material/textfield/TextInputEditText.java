/*
 * Copyright (C) 2016 The Android Open Source Project
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

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.internal.ManufacturerUtils;
import com.google.android.material.internal.ThemeEnforcement;

/**
 * A special sub-class of {@link android.widget.EditText} designed for use as a child of {@link
 * com.google.android.material.textfield.TextInputLayout}.
 *
 * <p>Using this class allows us to display a hint in the IME when in 'extract' mode and provides
 * accessibility support for {@link com.google.android.material.textfield.TextInputLayout}.
 */
public class TextInputEditText extends AppCompatEditText {

  private final Rect parentRect = new Rect();
  private boolean textInputLayoutFocusedRectEnabled;

  public TextInputEditText(@NonNull Context context) {
    this(context, null);
  }

  public TextInputEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.editTextStyle);
  }

  public TextInputEditText(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, 0), attrs, defStyleAttr);
    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.TextInputEditText,
            defStyleAttr,
            R.style.Widget_Design_TextInputEditText);

    setTextInputLayoutFocusedRectEnabled(
        attributes.getBoolean(R.styleable.TextInputEditText_textInputLayoutFocusedRectEnabled, false));

    attributes.recycle();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    // Meizu devices expect TextView#mHintLayout to be non-null if TextView#getHint() is non-null.
    // In order to avoid crashing, we force the creation of the layout by setting an empty non-null
    // hint.
    TextInputLayout layout = getTextInputLayout();
    if (layout != null
        && layout.isProvidingHint()
        && super.getHint() == null
        && ManufacturerUtils.isMeizuDevice()) {
      setHint("");
    }
  }

  @Nullable
  @Override
  public CharSequence getHint() {
    // Certain test frameworks expect the actionable element to expose its hint as a label. When
    // TextInputLayout is providing our hint, retrieve it from the parent layout.
    TextInputLayout layout = getTextInputLayout();
    if (layout != null && layout.isProvidingHint()) {
      return layout.getHint();
    }
    return super.getHint();
  }

  @Nullable
  @Override
  public InputConnection onCreateInputConnection(@NonNull EditorInfo outAttrs) {
    final InputConnection ic = super.onCreateInputConnection(outAttrs);
    if (ic != null && outAttrs.hintText == null) {
      // If we don't have a hint and our parent is a TextInputLayout, use its hint for the
      // EditorInfo. This allows us to display a hint in 'extract mode'.
      outAttrs.hintText = getHintFromLayout();
    }
    return ic;
  }

  @Nullable
  private TextInputLayout getTextInputLayout() {
    ViewParent parent = getParent();
    while (parent instanceof View) {
      if (parent instanceof TextInputLayout) {
        return (TextInputLayout) parent;
      }
      parent = parent.getParent();
    }
    return null;
  }

  @Nullable
  private CharSequence getHintFromLayout() {
    TextInputLayout layout = getTextInputLayout();
    return (layout != null) ? layout.getHint() : null;
  }

  /**
   * Whether the edit text should use the TextInputLayout's focused rectangle.
   */
  public void setTextInputLayoutFocusedRectEnabled(boolean textInputLayoutFocusedRectEnabled) {
    this.textInputLayoutFocusedRectEnabled = textInputLayoutFocusedRectEnabled;
  }

  /**
   * Whether the edit text is using the TextInputLayout's focused rectangle.
   */
  public boolean isTextInputLayoutFocusedRectEnabled() {
    return textInputLayoutFocusedRectEnabled;
  }

  @Override
  public void getFocusedRect(@Nullable Rect r) {
    super.getFocusedRect(r);
    TextInputLayout textInputLayout = getTextInputLayout();
    if (textInputLayout != null
        && textInputLayoutFocusedRectEnabled
        && r != null) {
      textInputLayout.getFocusedRect(parentRect);
      r.bottom = parentRect.bottom;
    }
  }

  @Override
  public boolean getGlobalVisibleRect(@Nullable Rect r, @Nullable Point globalOffset) {
    boolean result = super.getGlobalVisibleRect(r, globalOffset);
    TextInputLayout textInputLayout = getTextInputLayout();
    if (textInputLayout != null
        && textInputLayoutFocusedRectEnabled
        && r != null) {
      textInputLayout.getGlobalVisibleRect(parentRect, globalOffset);
      r.bottom = parentRect.bottom;
    }
    return result;
  }

  @Override
  public boolean requestRectangleOnScreen(@Nullable Rect rectangle) {
    boolean result = super.requestRectangleOnScreen(rectangle);
    TextInputLayout textInputLayout = getTextInputLayout();
    if (textInputLayout != null && textInputLayoutFocusedRectEnabled) {
      parentRect.set(
          0,
          textInputLayout.getHeight()
              - getResources().getDimensionPixelOffset(R.dimen.mtrl_edittext_rectangle_top_offset),
          textInputLayout.getWidth(),
          textInputLayout.getHeight());
      textInputLayout.requestRectangleOnScreen(parentRect, true);
    }
     return result;
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    TextInputLayout layout = getTextInputLayout();

    // In APIs < 23, some things set in the parent TextInputLayout's AccessibilityDelegate get
    // overwritten, so we set them here so that announcements are as expected.
    if (VERSION.SDK_INT < 23 && layout != null) {
      info.setText(getAccessibilityNodeInfoText(layout));
    }
  }

  @NonNull
  private String getAccessibilityNodeInfoText(@NonNull TextInputLayout layout) {
    CharSequence inputText = getText();
    CharSequence hintText = layout.getHint();
    boolean showingText = !TextUtils.isEmpty(inputText);
    boolean hasHint = !TextUtils.isEmpty(hintText);

    if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
      setLabelFor(R.id.textinput_helper_text);
    }

    String hint = hasHint ? hintText.toString() : "";

    if (showingText) {
      return inputText + (!TextUtils.isEmpty(hint) ? (", " + hint) : "");
    } else if (!TextUtils.isEmpty(hint)) {
      return hint;
    } else {
      return "";
    }
  }
}
