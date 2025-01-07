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
import static java.lang.Math.max;

import android.content.Context;
import android.content.res.ColorStateList;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

  private static final String TAG = FloatingToolbarLayout.class.getSimpleName();
  private static final int DEF_STYLE_RES = R.style.Widget_Material3_FloatingToolbar;
  @Nullable private Behavior behavior;

  private boolean marginLeftSystemWindowInsets;
  private boolean marginTopSystemWindowInsets;
  private boolean marginRightSystemWindowInsets;
  private boolean marginBottomSystemWindowInsets;

  private int topInset = 0;
  private int leftInset = 0;
  private int rightInset = 0;
  private int bottomInset = 0;

  private final Runnable insetsRunnable =
      () -> {
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (!(lp instanceof MarginLayoutParams)) {
          Log.w(TAG, "Unable to update margins because layout params are not MarginLayoutParams");
          return;
        }

        int[] coords = new int[2];
        getLocationInWindow(coords);
        int x = coords[0];
        int y = coords[1];

        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (getDisplay() == null) {
          return;
        }
        getDisplay().getMetrics(displayMetrics);

        MarginLayoutParams marginLp = (MarginLayoutParams) lp;

        if (marginLeftSystemWindowInsets && x < leftInset) {
          marginLp.leftMargin = max(leftInset, marginLp.leftMargin);
        }

        if (marginRightSystemWindowInsets && x + getWidth() > displayMetrics.widthPixels - rightInset) {
          marginLp.rightMargin = max(rightInset, marginLp.rightMargin);
        }

        if (marginTopSystemWindowInsets && y < topInset) {
          marginLp.topMargin = max(topInset, marginLp.topMargin);
        }

        if (marginBottomSystemWindowInsets && y + getHeight() > displayMetrics.heightPixels - bottomInset) {
          marginLp.bottomMargin = max(bottomInset, marginLp.bottomMargin);
        }
        requestLayout();
      };

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
    marginTopSystemWindowInsets = attributes.getBoolean(R.styleable.FloatingToolbar_marginTopSystemWindowInsets, true);
    marginRightSystemWindowInsets = attributes.getBoolean(R.styleable.FloatingToolbar_marginRightSystemWindowInsets, true);
    marginBottomSystemWindowInsets = attributes.getBoolean(R.styleable.FloatingToolbar_marginBottomSystemWindowInsets, true);

    ViewCompat.setOnApplyWindowInsetsListener(
        this,
        new androidx.core.view.OnApplyWindowInsetsListener() {
          @NonNull
          @Override
          public WindowInsetsCompat onApplyWindowInsets(
              @NonNull View v, @NonNull WindowInsetsCompat insets) {
            if (!marginLeftSystemWindowInsets && !marginRightSystemWindowInsets
            && !marginTopSystemWindowInsets && !marginBottomSystemWindowInsets) {
              return insets;
            }
            Insets systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            bottomInset = systemBarInsets.bottom;
            topInset = systemBarInsets.top;
            rightInset = systemBarInsets.right;
            leftInset = systemBarInsets.left;
            v.removeCallbacks(insetsRunnable);
            v.post(insetsRunnable);
            return insets;
          }
        });

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
