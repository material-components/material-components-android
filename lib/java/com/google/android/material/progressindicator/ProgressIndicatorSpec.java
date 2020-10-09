/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.google.android.material.progressindicator;

import com.google.android.material.R;

import static java.lang.Math.min;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.annotation.AttrRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.annotation.StyleableRes;
import com.google.android.material.color.MaterialColors;

/** A spec class managing all attributes of {@link ProgressIndicator}. */
public final class ProgressIndicatorSpec {

  /** The type of the progress indicator, either {@code #LINEAR} or {@code #CIRCULAR}. */
  public int indicatorType;

  /** The size of the progress track and indicator. */
  public int indicatorSize;

  /**
   * When this is greater than 0, the corners of both the track and the indicator will be rounded
   * with this radius. If the radius is greater than half of the track width, an {@code
   * IllegalArgumentException} will be thrown during initialization.
   */
  public int indicatorCornerRadius;

  /**
   * The color array of the progress stroke. In determinate mode and single color indeterminate
   * mode, only the first item will be used. This field combines the attribute indicatorColor and
   * indicatorColors defined in the XML.
   */
  @NonNull public int[] indicatorColors;

  /**
   * The color used for the progress track. If not defined, it will be set to the indicatorColor and
   * apply the first disable alpha value from the theme.
   */
  public int trackColor;

  /**
   * Whether to inverse the progress direction. Linear positive directory is start-to-end; circular
   * positive directory is clockwise.
   */
  public boolean inverse;

  /**
   * How the progress indicator appears and disappears. {@see #GROW_MODE_NONE} {@see
   * #GROW_MODE_INCOMING} {@see #GROW_MODE_OUTGOING} {@see #GROW_MODE_BIDIRECTIONAL}
   */
  public int growMode;

  /** The extra space from the edge of the stroke to the edge of canvas. Ignored in linear mode. */
  public int circularInset;

  /** The radius of the outer bound of the circular progress stroke. Ignored in linear mode. */
  public int circularRadius;

  /**
   * The animation style used in indeterminate mode. The strokes in different colors are end-to-end
   * connected. Ignored for determinate mode and indeterminate mode with less than 3 colors.
   */
  public boolean linearSeamless;

  public void loadFromAttributes(
      @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    loadFromAttributes(context, attrs, defStyleAttr, ProgressIndicator.DEF_STYLE_RES);
  }

  public void loadFromAttributes(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    TypedArray a =
        context.obtainStyledAttributes(
            attrs, R.styleable.ProgressIndicator, defStyleAttr, defStyleRes);
    indicatorType = a.getInt(R.styleable.ProgressIndicator_indicatorType, ProgressIndicator.LINEAR);
    indicatorSize =
        getDimensionPixelSize(
            context,
            a,
            R.styleable.ProgressIndicator_indicatorSize,
            R.dimen.mtrl_progress_indicator_size);
    circularInset =
        getDimensionPixelSize(
            context,
            a,
            R.styleable.ProgressIndicator_circularInset,
            R.dimen.mtrl_progress_circular_inset);
    circularRadius =
        getDimensionPixelSize(
            context,
            a,
            R.styleable.ProgressIndicator_circularRadius,
            R.dimen.mtrl_progress_circular_radius);
    inverse = a.getBoolean(R.styleable.ProgressIndicator_inverse, false);
    growMode = a.getInt(R.styleable.ProgressIndicator_growMode, ProgressIndicator.GROW_MODE_NONE);

    // Loads the indicator colors.
    loadIndicatorColors(context, a);

    // Loads the track color.
    loadTrackColor(context, a);

    linearSeamless =
        a.getBoolean(R.styleable.ProgressIndicator_linearSeamless, true)
            && indicatorType == ProgressIndicator.LINEAR
            && indicatorColors.length >= 3;
    indicatorCornerRadius =
        min(
            a.getDimensionPixelSize(R.styleable.ProgressIndicator_indicatorCornerRadius, 0),
            indicatorSize / 2);

    a.recycle();

    validate();
  }

  public void validate() {
    if (indicatorType == ProgressIndicator.CIRCULAR && circularRadius < indicatorSize / 2) {
      // Throws an exception if circularRadius is less than half of the indicatorSize, which will
      // result in a part of the inner side of the indicator overshoots the center, and the visual
      // becomes undefined.
      throw new IllegalArgumentException(
          "The circularRadius cannot be less than half of the indicatorSize.");
    }
    if (linearSeamless && indicatorCornerRadius > 0) {
      // Throws an exception if trying to use cornered indicator for linear seamless mode.
      throw new IllegalArgumentException(
          "Rounded corners are not supported in linear seamless mode.");
    }
  }

  /**
   * Returns a dimension in pixels from attributes if available; otherwise, the default resource
   * value. This method doesn't recycle the {@link TypedArray} argument.
   *
   * @param context The current active context.
   * @param typedArray The TypedArray object of the attributes.
   * @param resId The styleable id of the attribute.
   * @param defaultResId The resource id of the default value.
   */
  @Dimension
  private static int getDimensionPixelSize(
      @NonNull Context context,
      @NonNull TypedArray typedArray,
      @StyleableRes int resId,
      @DimenRes int defaultResId) {
    return typedArray.getDimensionPixelSize(
        resId, context.getResources().getDimensionPixelSize(defaultResId));
  }

  /**
   * Loads the indicatorColors from attributes if existing; otherwise, uses indicatorColor attribute
   * or theme primary color for the indicator. This method doesn't recycle the {@link TypedArray}
   * argument.
   *
   * @param context The current active context.
   * @param typedArray The TypedArray object of the attributes.
   * @throws IllegalArgumentException if both indicatorColors and indicatorColor exist in attribute
   *     set.
   * @throws IllegalArgumentException if indicatorColor doesn't exist and indicatorColors is empty.
   */
  private void loadIndicatorColors(@NonNull Context context, @NonNull TypedArray typedArray) {
    if (typedArray.hasValue(R.styleable.ProgressIndicator_indicatorColors)) {
      indicatorColors =
          context
              .getResources()
              .getIntArray(
                  typedArray.getResourceId(R.styleable.ProgressIndicator_indicatorColors, -1));
      if (typedArray.hasValue(R.styleable.ProgressIndicator_indicatorColor)) {
        throw new IllegalArgumentException(
            "Attributes indicatorColors and indicatorColor cannot be used at the same time.");
      }
      if (indicatorColors.length == 0) {
        throw new IllegalArgumentException(
            "indicatorColors cannot be empty when indicatorColor is not used.");
      }
    } else if (typedArray.hasValue(R.styleable.ProgressIndicator_indicatorColor)) {
      TypedValue indicatorColorValue =
          typedArray.peekValue(R.styleable.ProgressIndicator_indicatorColor);
      if (indicatorColorValue.type == TypedValue.TYPE_REFERENCE) {
        indicatorColors =
            context
                .getResources()
                .getIntArray(
                    typedArray.getResourceId(R.styleable.ProgressIndicator_indicatorColor, -1));
        if (indicatorColors.length == 0) {
          throw new IllegalArgumentException(
              "indicatorColors cannot be empty when indicatorColor is not used.");
        }
      } else {
        indicatorColors =
            new int[] {typedArray.getColor(R.styleable.ProgressIndicator_indicatorColor, -1)};
      }
    } else {
      // Uses theme primary color for indicator if not provided in the attribute set.
      indicatorColors = new int[] {MaterialColors.getColor(context, R.attr.colorPrimary, -1)};
    }
  }

  /**
   * Loads the trackColor from attributes if existing; otherwise, uses the first value in {@link
   * #indicatorColors} applying the alpha value for disable items from theme. This method doesn't
   * recycle the {@link TypedArray} argument.
   *
   * @param context The current active context.
   * @param typedArray The TypedArray object of the attributes.
   */
  private void loadTrackColor(@NonNull Context context, @NonNull TypedArray typedArray) {
    if (typedArray.hasValue(R.styleable.ProgressIndicator_trackColor)) {
      trackColor = typedArray.getColor(R.styleable.ProgressIndicator_trackColor, -1);
    } else {
      trackColor = indicatorColors[0];

      TypedArray disabledAlphaArray =
          context.getTheme().obtainStyledAttributes(new int[] {android.R.attr.disabledAlpha});
      float defaultOpacity = disabledAlphaArray.getFloat(0, ProgressIndicator.DEFAULT_OPACITY);
      disabledAlphaArray.recycle();

      int trackAlpha = (int) (ProgressIndicator.MAX_ALPHA * defaultOpacity);
      trackColor = MaterialColors.compositeARGBWithAlpha(trackColor, trackAlpha);
    }
  }
}
