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

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;

/**
 * {@code MaterialToolbar} is a {@link Toolbar} that implements certain Material features, such as
 * elevation overlays for Dark Themes.
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

  @Nullable private Integer navigationIconTint;

  public MaterialToolbar(@NonNull Context context) {
    this(context, null);
  }

  public MaterialToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.toolbarStyle);
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

    a.recycle();

    initBackground(context);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this);
  }

  @RequiresApi(VERSION_CODES.LOLLIPOP)
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

  private void initBackground(Context context) {
    Drawable background = getBackground();
    if (background != null && !(background instanceof ColorDrawable)) {
      return;
    }
    MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
    int backgroundColor =
        background != null ? ((ColorDrawable) background).getColor() : Color.TRANSPARENT;
    materialShapeDrawable.setFillColor(ColorStateList.valueOf(backgroundColor));
    materialShapeDrawable.initializeElevationOverlay(context);
    materialShapeDrawable.setElevation(ViewCompat.getElevation(this));
    ViewCompat.setBackground(this, materialShapeDrawable);
  }

  @Nullable
  private Drawable maybeTintNavigationIcon(@Nullable Drawable navigationIcon) {
    if (navigationIcon != null && navigationIconTint != null) {
      Drawable wrappedNavigationIcon = DrawableCompat.wrap(navigationIcon);
      DrawableCompat.setTint(wrappedNavigationIcon, navigationIconTint);
      return wrappedNavigationIcon;
    } else {
      return navigationIcon;
    }
  }
}
