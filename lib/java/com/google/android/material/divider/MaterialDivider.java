/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.divider;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.Px;

import com.google.android.material.R;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

public class MaterialDivider extends View {


  public MaterialDivider(Context context) {
    this(context, null);
  }

  public MaterialDivider(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.dividerStyle);
  }

  boolean isRtlEnabled;

  AttributeSet initialAttrs;
  int initialDefStyleAttr;

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_Divider;

  public MaterialDivider(Context context, AttributeSet attrs, int defStyleAttr) {

    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    initialAttrs = attrs;
    initialDefStyleAttr = defStyleAttr;
    isRtlEnabled = ViewUtils.isLayoutRtl(this);

    float alpha = attrs.getAttributeFloatValue("http://schemas.android.com/apk/res/android", "alpha", -1);
    int color = attrs.getAttributeIntValue(android.R.attr.background, -1);

    final TypedArray dividerStyleAttrs = getDividerStyleAttrs();
    if (alpha == -1) {
      alpha = dividerStyleAttrs.getFloat(ATTR_ALPHA, -1);
    }
    if (color == -1) {
      color = dividerStyleAttrs.getColor(ATTR_COLOR, -1);
    }

    setAlpha(alpha);
    setColor(color);
  }

  int[] ATTRS = {R.attr.dividerAlpha, R.attr.dividerColor, R.attr.dividerInset, R.attr.dividerSize};
  private static final int ATTR_ALPHA = 0;
  private static final int ATTR_COLOR = 1;
  private static final int ATTR_INSET = 2;
  private static final int ATTR_SIZE = 3;

  private TypedArray getDividerStyleAttrs() {
    TypedValue dividerStyle = new TypedValue();
    getContext().getTheme().resolveAttribute(R.attr.dividerStyle, dividerStyle, true);
    return getContext().obtainStyledAttributes(dividerStyle.resourceId, ATTRS);
  }

  //Runs when this view's layoutParams are available
  @Override
  protected void onAttachedToWindow() {

    super.onAttachedToWindow();
    initializeOrientation();
    TypedArray dividerStyleAttrs = getDividerStyleAttrs();
    TypedArray styledAttributes =
        ThemeEnforcement.obtainStyledAttributes(getContext(), initialAttrs, R.styleable.MaterialDivider, initialDefStyleAttr, DEF_STYLE_RES);
    LayoutParams layoutParams = getLayoutParams();

    if (orientation != INVALID) {
      int size = orientation == VERTICAL ? layoutParams.height : layoutParams.width;
      if (size == WRAP_CONTENT) {
        size = (int) dividerStyleAttrs.getDimension(ATTR_SIZE, -1);
        setSize(size);
      }
    }

    if (orientation == VERTICAL) {
      int inset = (int) styledAttributes.getDimension(R.styleable.MaterialDivider_inset, -1);
      if (inset == -1) {
        inset = (int) dividerStyleAttrs.getDimension(ATTR_INSET, -1);
      }
      setInset(inset);
    }

    dividerStyleAttrs.recycle();
    styledAttributes.recycle();
  }

  private static final int INVALID = -1;
  private static final int HORIZONTAL = 0;
  private static final int VERTICAL = 1;
  private int orientation;

  private void initializeOrientation() {
    LayoutParams layoutParams = getLayoutParams();
    if (layoutParams.width == MATCH_PARENT && layoutParams.height == MATCH_PARENT) {
      orientation = INVALID;
    } else if (layoutParams.width == MATCH_PARENT) {
      orientation = VERTICAL;
    } else if (layoutParams.height == MATCH_PARENT) {
      orientation = HORIZONTAL;
    }
  }

  @Dimension
  int size = 0;

  public @Dimension int getSize() {
    return size;
  }

  public void setSize(@Dimension int size) {
    this.size = size;
    LayoutParams layoutParams = getLayoutParams();

    if (orientation == INVALID) {
      throw new UnsupportedOperationException("Material dividers only support resizing for horizontal and vertical dividers. Either android.R.attr#layout_width or android.R.attr#layout_height needs to be set to MATCH_PARENT");
    } else if (orientation == VERTICAL) {
      layoutParams.height = size;
    } else {
      layoutParams.width = size;
    }

    setLayoutParams(layoutParams);
    requestLayout();
  }

  @ColorInt int color = 0;

  public @ColorInt int getColor() {
    return color;
  }

  public void setColor(@ColorInt int color) {
    this.color = color;
    setBackgroundColor(color);
  }

  @Dimension int inset = 0;

  public @Dimension int getInset() {
    return inset;
  }


  public void setInset(@Dimension final int inset) {

    this.inset = inset;
    if (orientation != VERTICAL) {
      throw new UnsupportedOperationException("Material dividers only supports insetting for vertical dividers. Please set android.R.attr#layout_width to MATCH_PARENT to define a vertical divider");
    }

    int insetLeft = isRtlEnabled ? 0 : inset;
    int insetRight = isRtlEnabled ? inset : 0;

    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
    marginLayoutParams.setMargins(insetLeft, 0, insetRight, 0);
    setLayoutParams(marginLayoutParams);
    requestLayout();
  }
}
