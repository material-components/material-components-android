/*
 * Copyright 2024 The Android Open Source Project
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

package com.google.android.material.floatingtoolbar;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * Provides an implementation of a floating toolbar.
 *
 * <p>Floating toolbars float above the body content and can be used to display contextual actions
 * relevant to the body content or the specific page.
 *
 * <p>The floating toolbar supports a custom {@link android.view.ViewGroup} child. The toolbar
 * provides styling for the provided child to give a uniform "floating toolbar" appearance to the
 * {@link android.view.ViewGroup}.
 */
public class FloatingToolbarLayout extends FrameLayout {

  private static final String TAG = FloatingToolbarLayout.class.getSimpleName();
  private static final int DEF_STYLE_RES = R.style.Widget_Material3_FloatingToolbar;

  private boolean marginLeftSystemWindowInsets;
  private boolean marginTopSystemWindowInsets;
  private boolean marginRightSystemWindowInsets;
  private boolean marginBottomSystemWindowInsets;
  private Rect originalMargins;
  private int bottomMarginWindowInset;
  private int topMarginWindowInset;
  private int leftMarginWindowInset;
  private int rightMarginWindowInset;

  public FloatingToolbarLayout(@NonNull Context context) {
    this(context, null);
  }

  public FloatingToolbarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.floatingToolbarStyle);
  }

  public FloatingToolbarLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    this(context, attrs, defStyleAttr, DEF_STYLE_RES);
  }

  public FloatingToolbarLayout(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    super(wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr);

    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    /* Custom attributes */
    TintTypedArray attributes =
        ThemeEnforcement.obtainTintedStyledAttributes(
            context, attrs, R.styleable.FloatingToolbar, defStyleAttr, defStyleRes);

    // Add a MaterialShapeDrawable as a background that supports tinting in every API level.
    if (attributes.hasValue(R.styleable.FloatingToolbar_backgroundTint)) {
      @ColorInt
      int backgroundColor = attributes.getColor(R.styleable.FloatingToolbar_backgroundTint, 0);

      ShapeAppearanceModel shapeAppearanceModel =
          ShapeAppearanceModel.builder(context, attrs, defStyleAttr, defStyleRes).build();
      MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
      materialShapeDrawable.setFillColor(ColorStateList.valueOf(backgroundColor));

      setBackground(materialShapeDrawable);
    }

    // Reading out if we are handling inset margins, so we can apply it to the content.
    marginLeftSystemWindowInsets = attributes.getBoolean(R.styleable.FloatingToolbar_marginLeftSystemWindowInsets, true);
    // Top-aligned floating toolbars are not recommended, so a top inset margin is turned off by default
    marginTopSystemWindowInsets = attributes.getBoolean(R.styleable.FloatingToolbar_marginTopSystemWindowInsets, false);
    marginRightSystemWindowInsets = attributes.getBoolean(R.styleable.FloatingToolbar_marginRightSystemWindowInsets, true);
    marginBottomSystemWindowInsets = attributes.getBoolean(R.styleable.FloatingToolbar_marginBottomSystemWindowInsets, true);

    ViewCompat.setOnApplyWindowInsetsListener(
        this,
        new androidx.core.view.OnApplyWindowInsetsListener() {
          @NonNull
          @Override
          public WindowInsetsCompat onApplyWindowInsets(
              @NonNull View v, @NonNull WindowInsetsCompat insets) {
            if (!marginLeftSystemWindowInsets
                && !marginRightSystemWindowInsets
                && !marginTopSystemWindowInsets
                && !marginBottomSystemWindowInsets) {
              return insets;
            }
            Insets systemBarInsets =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                        | WindowInsetsCompat.Type.displayCutout()
                        | WindowInsetsCompat.Type.ime());
            bottomMarginWindowInset = systemBarInsets.bottom;
            topMarginWindowInset = systemBarInsets.top;
            rightMarginWindowInset = systemBarInsets.right;
            leftMarginWindowInset = systemBarInsets.left;

            updateMargins();

            return insets;
          }
        });

    attributes.recycle();
  }

  private void updateMargins() {
    ViewGroup.LayoutParams lp = getLayoutParams();
    if (originalMargins == null) {
      Log.w(TAG, "Unable to update margins because original view margins are not set");
      return;
    }

    int newLeftMargin =
        originalMargins.left + (marginLeftSystemWindowInsets ? leftMarginWindowInset : 0);
    int newRightMargin =
        originalMargins.right + (marginRightSystemWindowInsets ? rightMarginWindowInset : 0);
    int newTopMargin =
        originalMargins.top + (marginTopSystemWindowInsets ? topMarginWindowInset : 0);
    int newBottomMargin =
        originalMargins.bottom + (marginBottomSystemWindowInsets ? bottomMarginWindowInset : 0);

    MarginLayoutParams marginLp = (MarginLayoutParams) lp;
    boolean marginChanged =
        marginLp.bottomMargin != newBottomMargin
            || marginLp.leftMargin != newLeftMargin
            || marginLp.rightMargin != newRightMargin
            || marginLp.topMargin != newTopMargin;
    if (marginChanged) {
      marginLp.bottomMargin = newBottomMargin;
      marginLp.leftMargin = newLeftMargin;
      marginLp.rightMargin = newRightMargin;
      marginLp.topMargin = newTopMargin;
      requestLayout();
    }
  }

  @Override
  public void setLayoutParams(ViewGroup.LayoutParams params) {
    super.setLayoutParams(params);
    if (params instanceof MarginLayoutParams) {
      MarginLayoutParams marginParams = (MarginLayoutParams) params;
      originalMargins =
          new Rect(
              marginParams.leftMargin,
              marginParams.topMargin,
              marginParams.rightMargin,
              marginParams.bottomMargin);
      updateMargins();
    } else {
      originalMargins = null;
    }
  }
}
