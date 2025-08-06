/*
 * Copyright (C) 2024 The Android Open Source Project
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
package com.google.android.material.loadingindicator;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.StyleRes;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ThemeEnforcement;

/**
 * This class contains the parameters for drawing a loading indicator. The parameters reflect the
 * attributes defined in {@code R.styleable.LoadingIndicator}.
 */
public final class LoadingIndicatorSpec {

  boolean scaleToFit = false;
  @Px int indicatorSize;
  @Px int containerWidth;
  @Px int containerHeight;
  @NonNull int[] indicatorColors = new int[0];
  @ColorInt int containerColor;

  public LoadingIndicatorSpec(@NonNull Context context, @NonNull AttributeSet attrs) {
    this(context, attrs, R.attr.loadingIndicatorStyle);
  }

  public LoadingIndicatorSpec(
      @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes final int defStyleAttr) {
    this(context, attrs, defStyleAttr, LoadingIndicator.DEF_STYLE_RES);
  }

  public LoadingIndicatorSpec(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes final int defStyleAttr,
      @StyleRes final int defStyleRes) {

    // Loads default resources.
    @Px
    int defaultShapeSize =
        context.getResources().getDimensionPixelSize(R.dimen.m3_loading_indicator_shape_size);
    @Px
    int defaultContainerSize =
        context.getResources().getDimensionPixelSize(R.dimen.m3_loading_indicator_container_size);

    // Loads attributes.
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.LoadingIndicator, defStyleAttr, defStyleRes);
    indicatorSize =
        a.getDimensionPixelSize(R.styleable.LoadingIndicator_indicatorSize, defaultShapeSize);
    containerWidth =
        a.getDimensionPixelSize(R.styleable.LoadingIndicator_containerWidth, defaultContainerSize);
    containerHeight =
        a.getDimensionPixelSize(R.styleable.LoadingIndicator_containerHeight, defaultContainerSize);
    loadIndicatorColors(context, a);
    containerColor = a.getColor(R.styleable.LoadingIndicator_containerColor, Color.TRANSPARENT);
    a.recycle();
  }

  private void loadIndicatorColors(@NonNull Context context, @NonNull TypedArray typedArray) {
    if (!typedArray.hasValue(R.styleable.LoadingIndicator_indicatorColor)) {
      // Uses theme primary color for indicator if not provided in the attribute set.
      indicatorColors =
          new int[] {
            MaterialColors.getColor(context, androidx.appcompat.R.attr.colorPrimary, -1)
          };
      return;
    }

    TypedValue indicatorColorValue =
        typedArray.peekValue(R.styleable.LoadingIndicator_indicatorColor);

    if (indicatorColorValue.type != TypedValue.TYPE_REFERENCE) {
      indicatorColors =
          new int[] {typedArray.getColor(R.styleable.LoadingIndicator_indicatorColor, -1)};
      return;
    }

    indicatorColors =
        context
            .getResources()
            .getIntArray(typedArray.getResourceId(R.styleable.LoadingIndicator_indicatorColor, -1));
    if (indicatorColors.length == 0) {
      throw new IllegalArgumentException(
          "indicatorColors cannot be empty when indicatorColor is not used.");
    }
  }

  /**
   * Sets the scale specs to fit the given bound of the {@link LoadingIndicatorDrawable}.
   *
   * @param scaleToFit The new scaleToFit value.
   */
  public void setScaleToFit(boolean scaleToFit){
    this.scaleToFit = scaleToFit;
  }
}
