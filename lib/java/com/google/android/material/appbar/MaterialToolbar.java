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

import static com.google.android.material.internal.ThemeEnforcement.createThemedContext;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
import androidx.core.view.ViewCompat;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;

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

  public MaterialToolbar(@NonNull Context context) {
    this(context, null);
  }

  public MaterialToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.toolbarStyle);
  }

  public MaterialToolbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(createThemedContext(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

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
}
