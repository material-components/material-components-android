/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.google.android.material.divider;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.content.ContextCompat;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;

/**
 * A Material divider view.
 *
 * <p>The divider will display the correct default Material colors without the use of a style flag
 * in a layout file. Make sure to set {@code android:layout_height="wrap_content"} to ensure that
 * the correct thickness is set for the divider.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/Divider.md">component
 * developer guidance</a> and <a href="https://material.io/components/divider/overview">design
 * guidelines</a>.
 */
public class MaterialDivider extends View {

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_MaterialDivider;

  @NonNull private final MaterialShapeDrawable dividerDrawable;
  private int thickness;
  @ColorInt private int color;
  private int insetStart;
  private int insetEnd;

  public MaterialDivider(@NonNull Context context) {
    this(context, null);
  }

  public MaterialDivider(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.materialDividerStyle);
  }

  public MaterialDivider(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();
    dividerDrawable = new MaterialShapeDrawable();

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.MaterialDivider, defStyleAttr, DEF_STYLE_RES);
    thickness =
        attributes.getDimensionPixelSize(
            R.styleable.MaterialDivider_dividerThickness,
            getResources().getDimensionPixelSize(R.dimen.material_divider_thickness));
    insetStart =
        attributes.getDimensionPixelOffset(R.styleable.MaterialDivider_dividerInsetStart, 0);
    insetEnd = attributes.getDimensionPixelOffset(R.styleable.MaterialDivider_dividerInsetEnd, 0);
    setDividerColor(
        MaterialResources.getColorStateList(
            context, attributes, R.styleable.MaterialDivider_dividerColor)
            .getDefaultColor());

    attributes.recycle();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int newThickness = getMeasuredHeight();
    if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
      if (thickness > 0 && newThickness != thickness) {
        newThickness = thickness;
      }
      setMeasuredDimension(getMeasuredWidth(), newThickness);
    }
  }

  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    super.onDraw(canvas);
    // Apply the insets.
    boolean isRtl = getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    int left = isRtl ? insetEnd : insetStart;
    int right = isRtl ? getWidth() - insetStart : getWidth() - insetEnd;
    dividerDrawable.setBounds(left, 0, right, getBottom() - getTop());
    dividerDrawable.draw(canvas);
  }

  /**
   * Sets the thickness of the divider. The divider's {@code android:layout_height} must be set to
   * {@code wrap_content} in order for this value to be respected.
   *
   * @param thickness The thickness value to be set.
   * @see #getDividerThickness()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerThickness
   */
  public void setDividerThickness(@Px int thickness) {
    if (this.thickness != thickness) {
      this.thickness = thickness;
      requestLayout();
    }
  }

  /**
   * Sets the thickness of the divider. The divider's {@code android:layout_height} must be set to
   * {@code wrap_content} in order for this value to be respected.
   *
   * @param thicknessId The id of the thickness dimension resource to be set.
   * @see #getDividerThickness()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerThickness
   */
  public void setDividerThicknessResource(@DimenRes int thicknessId) {
    setDividerThickness(getContext().getResources().getDimensionPixelSize(thicknessId));
  }

  /**
   * Returns the {@code app:dividerThickness} set on the divider.
   *
   * @see #setDividerThickness(int)
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerThickness
   */
  public int getDividerThickness() {
    return thickness;
  }

  /**
   * Sets the start inset of the divider.
   *
   * @param insetStart The start inset to be set.
   * @see #getDividerInsetStart()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerInsetStart
   */
  public void setDividerInsetStart(@Px int insetStart) {
    this.insetStart = insetStart;
  }

  /**
   * Sets the start inset of the divider.
   *
   * @param insetStartId The id of the inset dimension resource to be set.
   * @see #getDividerInsetStart()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerInsetStart
   */
  public void setDividerInsetStartResource(@DimenRes int insetStartId) {
    setDividerInsetStart(getContext().getResources().getDimensionPixelOffset(insetStartId));
  }

  /**
   * Returns the divider's start inset.
   *
   * @see #setDividerInsetStart(int)
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerInsetStart
   */
  @Px
  public int getDividerInsetStart() {
    return insetStart;
  }

  /**
   * Sets the end inset of the divider.
   *
   * @param insetEnd The end inset to be set.
   * @see #getDividerInsetEnd()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerInsetEnd
   */
  public void setDividerInsetEnd(@Px int insetEnd) {
    this.insetEnd = insetEnd;
  }

  /**
   * Sets the end inset of the divider.
   *
   * @param insetEndId The id of the inset dimension resource to be set.
   * @see #getDividerInsetEnd()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerInsetEnd
   */
  public void setDividerInsetEndResource(@DimenRes int insetEndId) {
    setDividerInsetEnd(getContext().getResources().getDimensionPixelOffset(insetEndId));
  }

  /**
   * Returns the divider's end inset.
   *
   * @see #setDividerInsetEnd(int)
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerInsetEnd
   */
  @Px
  public int getDividerInsetEnd() {
    return insetEnd;
  }

  /**
   * Sets the color of the divider.
   *
   * @param color The color to be set.
   * @see #getDividerColor()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerColor
   */
  public void setDividerColor(@ColorInt int color) {
    if (this.color != color) {
      this.color = color;
      dividerDrawable.setFillColor(ColorStateList.valueOf(color));
      invalidate();
    }
  }

  /**
   * Sets the color of the divider.
   *
   * @param colorId The id of the color resource to be set.
   * @see #getDividerColor()
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerColor
   */
  public void setDividerColorResource(@ColorRes int colorId) {
    setDividerColor(ContextCompat.getColor(getContext(), colorId));
  }

  /**
   * Returns the divider color.
   *
   * @see #setDividerColor(int)
   * @attr ref com.google.android.material.R.styleable#MaterialDivider_dividerColor
   */
  public int getDividerColor() {
    return color;
  }
}
