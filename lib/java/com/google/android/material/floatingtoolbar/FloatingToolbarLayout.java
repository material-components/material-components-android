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
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior;
import com.google.android.material.behavior.HideViewOnScrollBehavior;
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
public class FloatingToolbarLayout extends FrameLayout implements AttachedBehavior {
  private static final int DEF_STYLE_RES = R.style.Widget_Material3_FloatingToolbar;
  @Nullable private Behavior behavior;

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

    attributes.recycle();
  }

  @Override
  @NonNull
  public Behavior getBehavior() {
    if (behavior == null) {
      behavior = new Behavior();
    }
    return behavior;
  }

  /**
   * Behavior designed for use with {@link FloatingToolbarLayout} instances. Its main function is to
   * hide the {@link FloatingToolbarLayout} view when scrolling. Supports scrolling the floating
   * toolbar off of either the right, bottom or left edge of the screen.
   */
  public static class Behavior extends HideViewOnScrollBehavior<FloatingToolbarLayout> {}
}
