/*
 * Copyright 2025 The Android Open Source Project
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
package com.google.android.material.dockedtoolbar;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.res.ColorStateList;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.internal.ViewUtils.RelativePadding;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * Provides an implementation of a docked toolbar.
 *
 * <p>Docked toolbars are pinned to the top or bottom and can be used to display contextual actions
 * relevant to the body content or the specific page.
 *
 * <p>The docked toolbar supports a custom {@link android.view.ViewGroup} child, and provides docked
 * toolbar styling such as background color, shape, etc.
 */
public class DockedToolbarLayout extends FrameLayout {

  private static final String TAG = DockedToolbarLayout.class.getSimpleName();
  private static final int DEF_STYLE_RES = R.style.Widget_Material3_DockedToolbar;
  private Boolean paddingTopSystemWindowInsets;
  private Boolean paddingBottomSystemWindowInsets;

  public DockedToolbarLayout(@NonNull Context context) {
    this(context, null);
  }

  public DockedToolbarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.dockedToolbarStyle);
  }

  public DockedToolbarLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    this(context, attrs, defStyleAttr, DEF_STYLE_RES);
  }

  public DockedToolbarLayout(
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
            context, attrs, R.styleable.DockedToolbar, defStyleAttr, defStyleRes);

    // Add a MaterialShapeDrawable as a background that supports tinting in every API level.
    if (attributes.hasValue(R.styleable.DockedToolbar_backgroundTint)) {
      @ColorInt
      int backgroundColor = attributes.getColor(R.styleable.DockedToolbar_backgroundTint, 0);

      ShapeAppearanceModel shapeAppearanceModel =
          ShapeAppearanceModel.builder(context, attrs, defStyleAttr, defStyleRes).build();
      MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
      materialShapeDrawable.setFillColor(ColorStateList.valueOf(backgroundColor));

      setBackground(materialShapeDrawable);
    }

    // Reading out if we are handling inset padding, so we can apply it to the content.
    if (attributes.hasValue(R.styleable.DockedToolbar_paddingTopSystemWindowInsets)) {
      paddingTopSystemWindowInsets =
          attributes.getBoolean(R.styleable.DockedToolbar_paddingTopSystemWindowInsets, true);
    }
    if (attributes.hasValue(R.styleable.DockedToolbar_paddingBottomSystemWindowInsets)) {
      paddingBottomSystemWindowInsets =
          attributes.getBoolean(R.styleable.DockedToolbar_paddingBottomSystemWindowInsets, true);
    }

    ViewUtils.doOnApplyWindowInsets(
        this,
        new ViewUtils.OnApplyWindowInsetsListener() {
          @NonNull
          @Override
          public WindowInsetsCompat onApplyWindowInsets(
              View view,
              @NonNull WindowInsetsCompat insets,
              @NonNull RelativePadding initialPadding) {
            if (paddingTopSystemWindowInsets != null
                && paddingBottomSystemWindowInsets != null
                && !paddingTopSystemWindowInsets
                && !paddingBottomSystemWindowInsets) {
              return insets;
            }
            Insets systemBarInsets =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                        | WindowInsetsCompat.Type.displayCutout()
                        | WindowInsetsCompat.Type.ime());
            int bottomInset = systemBarInsets.bottom;
            int topInset = systemBarInsets.top;
            int bottomPadding = 0;
            int topPadding = 0;

            ViewGroup.LayoutParams lp = view.getLayoutParams();
            // If the inset flags are not explicitly set, and the toolbar is inside a
            // CoordinatorLayout or a FrameLayout, we can use the gravity
            // to ascertain what padding should be automatically added.
            if (hasGravity(lp, Gravity.TOP) && paddingTopSystemWindowInsets == null && getFitsSystemWindows()) {
              topPadding = topInset;
            }
            if (hasGravity(lp, Gravity.BOTTOM) && paddingBottomSystemWindowInsets == null && getFitsSystemWindows()) {
              bottomPadding = bottomInset;
            }

            // If paddingTopSystemWindowInsets or paddingBottomSystemWindowInsets is explicitly
            // set, then insets should always be applied to the padding.
            if (paddingBottomSystemWindowInsets != null) {
              bottomPadding = paddingBottomSystemWindowInsets ? bottomInset : 0;
            }
            if (paddingTopSystemWindowInsets != null) {
              topPadding = paddingTopSystemWindowInsets ? topInset : 0;
            }
            initialPadding.top += topPadding;
            initialPadding.bottom += bottomPadding;
            initialPadding.applyToView(view);

            return insets;
          }
        });

    setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    attributes.recycle();
  }

  private boolean hasGravity(ViewGroup.LayoutParams lp, int gravity) {
    if (lp instanceof CoordinatorLayout.LayoutParams) {
      return (((CoordinatorLayout.LayoutParams) lp).gravity & gravity) == gravity;
    } else if (lp instanceof FrameLayout.LayoutParams) {
      return (((FrameLayout.LayoutParams) lp).gravity & gravity) == gravity;
    }
    return false;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
      int childCount = getChildCount();
      int newHeight =
          Math.max(
              getMeasuredHeight(),
              getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom());

      for (int i = 0; i < childCount; i++) {
        measureChild(
            getChildAt(i),
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY));
      }

      setMeasuredDimension(getMeasuredWidth(), newHeight);
    }
  }
}
