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

package com.google.android.material.appbar;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;
import static java.lang.Math.max;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import com.google.android.material.drawable.DrawableUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ToolbarUtils;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;

/**
 * {@code MaterialToolbar} is a {@link Toolbar} that implements certain Material features, such as
 * elevation overlays for Dark Themes and centered titles.
 *
 * <p>Regarding the Dark Theme elevation overlays, it's important to note that the Material {@link
 * AppBarLayout} component also provides elevation overlay support, and operates under the
 * assumption that the child {@code Toolbar} does not have a background. While a {@code
 * MaterialToolbar} with a transparent background can be used within an {@link AppBarLayout}, in
 * terms of elevation overlays its main value comes into play with the standalone {@code Toolbar}
 * case, when using the {@code Widget.MaterialComponents.Toolbar.Surface} style with elevation.
 *
 * <p>To get started with the {@code MaterialToolbar} component, use {@code
 * com.google.android.material.appbar.MaterialToolbar} in your layout XML instead of {@code
 * androidx.appcompat.widget.Toolbar} or {@code Toolbar}. E.g.,:
 *
 * <pre>
 * &lt;com.google.android.material.appbar.MaterialToolbar
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;wrap_content&quot;/&gt;
 * </pre>
 */
public class MaterialToolbar extends Toolbar {

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_Toolbar;

  private static final ImageView.ScaleType[] LOGO_SCALE_TYPE_ARRAY = {
    ImageView.ScaleType.MATRIX,
    ImageView.ScaleType.FIT_XY,
    ImageView.ScaleType.FIT_START,
    ImageView.ScaleType.FIT_CENTER,
    ImageView.ScaleType.FIT_END,
    ImageView.ScaleType.CENTER,
    ImageView.ScaleType.CENTER_CROP,
    ImageView.ScaleType.CENTER_INSIDE
  };

  @Nullable private Integer navigationIconTint;
  private boolean titleCentered;
  private boolean subtitleCentered;

  @Nullable private ImageView.ScaleType logoScaleType;
  @Nullable private Boolean logoAdjustViewBounds;

  public MaterialToolbar(@NonNull Context context) {
    this(context, null);
  }

  public MaterialToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, androidx.appcompat.R.attr.toolbarStyle);
  }

  public MaterialToolbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    final TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.MaterialToolbar, defStyleAttr, DEF_STYLE_RES);

    if (a.hasValue(R.styleable.MaterialToolbar_navigationIconTint)) {
      setNavigationIconTint(a.getColor(R.styleable.MaterialToolbar_navigationIconTint, -1));
    }

    titleCentered = a.getBoolean(R.styleable.MaterialToolbar_titleCentered, false);
    subtitleCentered = a.getBoolean(R.styleable.MaterialToolbar_subtitleCentered, false);

    final int index = a.getInt(R.styleable.MaterialToolbar_logoScaleType, -1);
    if (index >= 0 && index < LOGO_SCALE_TYPE_ARRAY.length) {
      logoScaleType = LOGO_SCALE_TYPE_ARRAY[index];
    }

    if (a.hasValue(R.styleable.MaterialToolbar_logoAdjustViewBounds)) {
      logoAdjustViewBounds = a.getBoolean(R.styleable.MaterialToolbar_logoAdjustViewBounds, false);
    }

    a.recycle();

    initBackground(context);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    maybeCenterTitleViews();
    updateLogoImageView();
  }

  private void maybeCenterTitleViews() {
    if (!titleCentered && !subtitleCentered) {
      return;
    }

    TextView titleTextView = ToolbarUtils.getTitleTextView(this);
    TextView subtitleTextView = ToolbarUtils.getSubtitleTextView(this);
    if (titleTextView == null && subtitleTextView == null) {
      return;
    }

    Pair<Integer, Integer> titleBoundLimits =
        calculateTitleBoundLimits(titleTextView, subtitleTextView);

    if (titleCentered && titleTextView != null) {
      layoutTitleCenteredHorizontally(titleTextView, titleBoundLimits);
    }

    if (subtitleCentered && subtitleTextView != null) {
      layoutTitleCenteredHorizontally(subtitleTextView, titleBoundLimits);
    }
  }

  private Pair<Integer, Integer> calculateTitleBoundLimits(
      @Nullable TextView titleTextView, @Nullable TextView subtitleTextView) {
    int width = getMeasuredWidth();
    int midpoint = width / 2;
    int leftLimit = getPaddingLeft();
    int rightLimit = width - getPaddingRight();

    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (child.getVisibility() != GONE && child != titleTextView && child != subtitleTextView) {
        if (child.getRight() < midpoint && child.getRight() > leftLimit) {
          leftLimit = child.getRight();
        }
        if (child.getLeft() > midpoint && child.getLeft() < rightLimit) {
          rightLimit = child.getLeft();
        }
      }
    }

    return new Pair<>(leftLimit, rightLimit);
  }

  private void layoutTitleCenteredHorizontally(
      View titleView, Pair<Integer, Integer> titleBoundLimits) {
    int width = getMeasuredWidth();
    int titleWidth = titleView.getMeasuredWidth();

    int titleLeft = width / 2 - titleWidth / 2;
    int titleRight = titleLeft + titleWidth;

    int leftOverlap = max(titleBoundLimits.first - titleLeft, 0);
    int rightOverlap = max(titleRight - titleBoundLimits.second, 0);
    int overlap = max(leftOverlap, rightOverlap);

    if (overlap > 0) {
      titleLeft += overlap;
      titleRight -= overlap;
      titleWidth = titleRight - titleLeft;
      titleView.measure(
          MeasureSpec.makeMeasureSpec(titleWidth, MeasureSpec.EXACTLY),
          titleView.getMeasuredHeightAndState());
    }

    titleView.layout(titleLeft, titleView.getTop(), titleRight, titleView.getBottom());
  }

  private void updateLogoImageView() {
    ImageView logoImageView = ToolbarUtils.getLogoImageView(this);

    if (logoImageView != null) {
      if (logoAdjustViewBounds != null) {
        logoImageView.setAdjustViewBounds(logoAdjustViewBounds);
      }
      if (logoScaleType != null) {
        logoImageView.setScaleType(logoScaleType);
      }
    }
  }

  /**
   * Returns scale type of logo's ImageView
   *
   * @see #setLogoScaleType(ImageView.ScaleType). Default - null
   */
  @Nullable
  public ImageView.ScaleType getLogoScaleType() {
    return logoScaleType;
  }

  /** Sets ImageView.ScaleType for logo's ImageView. */
  public void setLogoScaleType(@NonNull ImageView.ScaleType logoScaleType) {
    if (this.logoScaleType != logoScaleType) {
      this.logoScaleType = logoScaleType;
      requestLayout();
    }
  }

  /**
   * Returns logo's ImageView adjustViewBounds
   *
   * @see #setLogoAdjustViewBounds(boolean). Default - false
   */
  public boolean isLogoAdjustViewBounds() {
    return logoAdjustViewBounds != null && logoAdjustViewBounds;
  }

  /** Sets ImageView.adjustViewBounds for logo's ImageView. */
  public void setLogoAdjustViewBounds(boolean logoAdjustViewBounds) {
    if (this.logoAdjustViewBounds == null || this.logoAdjustViewBounds != logoAdjustViewBounds) {
      this.logoAdjustViewBounds = logoAdjustViewBounds;
      requestLayout();
    }
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this);
  }

  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);

    MaterialShapeUtils.setElevation(this, elevation);
  }

  @Override
  public void setNavigationIcon(@Nullable Drawable drawable) {
    super.setNavigationIcon(maybeTintNavigationIcon(drawable));
  }

  /**
   * Sets the color of the toolbar's navigation icon.
   *
   * @see #setNavigationIcon
   */
  public void setNavigationIconTint(@ColorInt int navigationIconTint) {
    this.navigationIconTint = navigationIconTint;
    Drawable navigationIcon = getNavigationIcon();
    if (navigationIcon != null) {
      // Causes navigation icon to be tinted if needed.
      setNavigationIcon(navigationIcon);
    }
  }

  /**
   * Clears the tint list of the toolbar's navigation icon. E.g., if the navigation icon is an XML
   * based vector drawable, calling this method will clear the {@code android:tint}.
   *
   * @see #setNavigationIconTint(int)
   */
  public void clearNavigationIconTint() {
    this.navigationIconTint = null;
    Drawable navigationIcon = getNavigationIcon();
    if (navigationIcon != null) {
      Drawable wrappedNavigationIcon = DrawableCompat.wrap(navigationIcon.mutate());
      wrappedNavigationIcon.setTintList(null);
      setNavigationIcon(navigationIcon);
    }
  }

  /**
   * Gets the tint color of the toolbar's navigation icon, or null if no tint color has been set.
   *
   * @see #setNavigationIconTint(int)
   */
  @ColorInt
  @Nullable
  public Integer getNavigationIconTint() {
    return navigationIconTint;
  }

  /**
   * Sets whether the title text corresponding to the {@link #setTitle(int)} method should be
   * centered horizontally within the toolbar.
   *
   * <p>Note: it is not recommended to use centered titles in conjunction with a nested custom view,
   * as there may be positioning and overlap issues.
   */
  public void setTitleCentered(boolean titleCentered) {
    if (this.titleCentered != titleCentered) {
      this.titleCentered = titleCentered;
      requestLayout();
    }
  }

  /**
   * Returns whether the title text corresponding to the {@link #setTitle(int)} method should be
   * centered horizontally within the toolbar.
   *
   * @see #setTitleCentered(boolean)
   */
  public boolean isTitleCentered() {
    return titleCentered;
  }

  /**
   * Sets whether the subtitle text corresponding to the {@link #setSubtitle(int)} method should be
   * centered horizontally within the toolbar.
   *
   * <p>Note: it is not recommended to use centered titles in conjunction with a nested custom view,
   * as there may be positioning and overlap issues.
   */
  public void setSubtitleCentered(boolean subtitleCentered) {
    if (this.subtitleCentered != subtitleCentered) {
      this.subtitleCentered = subtitleCentered;
      requestLayout();
    }
  }

  /**
   * Returns whether the subtitle text corresponding to the {@link #setSubtitle(int)} method should
   * be centered horizontally within the toolbar.
   *
   * @see #setSubtitleCentered(boolean)
   */
  public boolean isSubtitleCentered() {
    return subtitleCentered;
  }

  private void initBackground(Context context) {
    Drawable background = getBackground();
    ColorStateList backgroundColorStateList =
        background == null
            ? ColorStateList.valueOf(Color.TRANSPARENT)
            : DrawableUtils.getColorStateListOrNull(background);

    if (backgroundColorStateList != null) {
      MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
      materialShapeDrawable.setFillColor(backgroundColorStateList);
      materialShapeDrawable.initializeElevationOverlay(context);
      materialShapeDrawable.setElevation(getElevation());
      setBackground(materialShapeDrawable);
    }
  }

  @Nullable
  private Drawable maybeTintNavigationIcon(@Nullable Drawable navigationIcon) {
    if (navigationIcon != null && navigationIconTint != null) {
      Drawable wrappedNavigationIcon = DrawableCompat.wrap(navigationIcon.mutate());
      wrappedNavigationIcon.setTint(navigationIconTint);
      return wrappedNavigationIcon;
    } else {
      return navigationIcon;
    }
  }
}
