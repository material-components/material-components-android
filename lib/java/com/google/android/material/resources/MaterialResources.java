/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.material.resources;

import com.google.android.material.R;

import static android.util.TypedValue.COMPLEX_UNIT_MASK;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.TintTypedArray;
import android.util.TypedValue;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.annotation.StyleableRes;

/**
 * Utility methods to resolve resources for components.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class MaterialResources {

  /** Value of the system's x1.3 font scale size. */
  private static final float FONT_SCALE_1_3 = 1.3f;
  /** Value of the system's x2 font scale size. */
  private static final float FONT_SCALE_2_0 = 2f;

  private MaterialResources() {}

  /**
   * Returns the {@link ColorStateList} from the given {@link TypedArray} attributes. The resource
   * can include themeable attributes, regardless of API level.
   */
  @Nullable
  public static ColorStateList getColorStateList(
      @NonNull Context context, @NonNull TypedArray attributes, @StyleableRes int index) {
    if (attributes.hasValue(index)) {
      int resourceId = attributes.getResourceId(index, 0);
      if (resourceId != 0) {
        ColorStateList value = AppCompatResources.getColorStateList(context, resourceId);
        if (value != null) {
          return value;
        }
      }
    }

    return attributes.getColorStateList(index);
  }

  /**
   * Returns the {@link ColorStateList} from the given {@link TintTypedArray} attributes. The
   * resource can include themeable attributes, regardless of API level.
   */
  @Nullable
  public static ColorStateList getColorStateList(
      @NonNull Context context, @NonNull TintTypedArray attributes, @StyleableRes int index) {
    if (attributes.hasValue(index)) {
      int resourceId = attributes.getResourceId(index, 0);
      if (resourceId != 0) {
        ColorStateList value = AppCompatResources.getColorStateList(context, resourceId);
        if (value != null) {
          return value;
        }
      }
    }

    return attributes.getColorStateList(index);
  }

  /**
   * Returns the drawable object from the given attributes.
   *
   * <p>This method supports inflation of {@code <vector>} and {@code <animated-vector>} resources
   * on devices where platform support is not available.
   */
  @Nullable
  public static Drawable getDrawable(
      @NonNull Context context, @NonNull TypedArray attributes, @StyleableRes int index) {
    if (attributes.hasValue(index)) {
      int resourceId = attributes.getResourceId(index, 0);
      if (resourceId != 0) {
        Drawable value = AppCompatResources.getDrawable(context, resourceId);
        if (value != null) {
          return value;
        }
      }
    }
    return attributes.getDrawable(index);
  }

  /**
   * Returns a TextAppearanceSpan object from the given attributes.
   *
   * <p>You only need this if you are drawing text manually. Normally, TextView takes care of this.
   */
  @Nullable
  public static TextAppearance getTextAppearance(
      @NonNull Context context, @NonNull TypedArray attributes, @StyleableRes int index) {
    if (attributes.hasValue(index)) {
      int resourceId = attributes.getResourceId(index, 0);
      if (resourceId != 0) {
        return new TextAppearance(context, resourceId);
      }
    }
    return null;
  }

  /**
   * Retrieve a dimensional unit attribute at <var>index</var> for use as a size in raw pixels. A
   * size conversion involves rounding the base value, and ensuring that a non-zero base value is at
   * least one pixel in size.
   *
   * <p>This method will throw an exception if the attribute is defined but is not a dimension.
   *
   * @param context The Context the view is running in, through which the current theme, resources,
   *     etc can be accessed.
   * @param attributes array of typed attributes from which the dimension unit must be read.
   * @param index Index of attribute to retrieve.
   * @param defaultValue Value to return if the attribute is not defined or not a resource.
   * @return Attribute dimension value multiplied by the appropriate metric and truncated to integer
   *     pixels, or defaultValue if not defined.
   * @throws UnsupportedOperationException if the attribute is defined but is not a dimension.
   * @see TypedArray#getDimensionPixelSize(int, int)
   */
  public static int getDimensionPixelSize(
      @NonNull Context context,
      @NonNull TypedArray attributes,
      @StyleableRes int index,
      final int defaultValue) {
    TypedValue value = new TypedValue();
    if (!attributes.getValue(index, value) || value.type != TypedValue.TYPE_ATTRIBUTE) {
      return attributes.getDimensionPixelSize(index, defaultValue);
    }

    TypedArray styledAttrs = context.getTheme().obtainStyledAttributes(new int[] {value.data});
    int dimension = styledAttrs.getDimensionPixelSize(0, defaultValue);
    styledAttrs.recycle();
    return dimension;
  }

  /**
   * Returns whether the font scale size is at least {@link #FONT_SCALE_1_3}.
   */
  public static boolean isFontScaleAtLeast1_3(@NonNull Context context) {
    return context.getResources().getConfiguration().fontScale >= FONT_SCALE_1_3;
  }

  /**
   * Returns whether the font scale size is at least {@link #FONT_SCALE_2_0}.
   */
  public static boolean isFontScaleAtLeast2_0(@NonNull Context context) {
    return context.getResources().getConfiguration().fontScale >= FONT_SCALE_2_0;
  }

  /**
   * Returns the font scale size.
   */
  public static float getFontScale(@NonNull Context context) {
    return context.getResources().getConfiguration().fontScale;
  }

  /**
   * Return the {@code R.styleable.TextAppearance_android_textSize} value from a text appearance
   * style at its density scaled value only.
   *
   * If the text size from the text appearance is using dp, the density scaled value will be
   * returned. If the text size is using sp, the raw value will be scaled according to display
   * density but not font scale, as if it were a dp value.
   *
   * This is used for components that do not scale their text due to being space constrained and
   * instead offer alternative methods of showing scaled text.
   */
  public static int getUnscaledTextSize(
      @NonNull Context context, @StyleRes int textAppearance, int defValue) {
    if (textAppearance == 0) {
      return defValue;
    }

    TypedArray a =
        context.obtainStyledAttributes(
            textAppearance, androidx.appcompat.R.styleable.TextAppearance);
    TypedValue v = new TypedValue();
    boolean available =
        a.getValue(androidx.appcompat.R.styleable.TextAppearance_android_textSize, v);
    a.recycle();

    if (!available) {
      return defValue;
    }

    // If the resource is in scaled pixels (sp) manually unpack the resource and scale to density
    // but not font scale.
    if (getComplexUnit(v) == TypedValue.COMPLEX_UNIT_SP) {
      // Get the raw value. If text size is set to 14sp in the dimen file, this will return 14.
      // Scale the raw value using density and round to avoid truncating.
      return Math.round(
          TypedValue.complexToFloat(v.data) * context.getResources().getDisplayMetrics().density);
    }

    // If the resource is not is sp, return with regular resource system scaling.
    return TypedValue.complexToDimensionPixelSize(
        v.data, context.getResources().getDisplayMetrics());
  }

  public static int getUnscaledLineHeight(
      @NonNull Context context, @StyleRes int textAppearance, int defValue) {
    if (textAppearance == 0) {
      return defValue;
    }

    TypedArray a = context.obtainStyledAttributes(textAppearance,
        R.styleable.MaterialTextAppearance);
    TypedValue v = new TypedValue();
    boolean available = a.getValue(R.styleable.MaterialTextAppearance_lineHeight, v);
    if (!available) {
      available = a.getValue(R.styleable.MaterialTextAppearance_android_lineHeight, v);
    }
    a.recycle();

    if (!available) {
      return defValue;
    }

    if (getComplexUnit(v) == TypedValue.COMPLEX_UNIT_SP) {
      // Get the raw value. If the line height is set to 14sp in the dimen file, this will return
      // 14. Scale the raw value using density and round to avoid truncating.
      return Math.round(
          TypedValue.complexToFloat(v.data) * context.getResources().getDisplayMetrics().density);
    }

    // If the resource is not is sp, return with regular resource system scaling.
    return TypedValue.complexToDimensionPixelSize(
        v.data, context.getResources().getDisplayMetrics());
  }

  /**
   * Return the complex unit type for the given value.
   *
   * <p>This is a compat method of {@link TypedValue#getComplexUnit()}, which is only available on
   * API 22 and above.
   */
  private static int getComplexUnit(TypedValue tv) {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1) {
      return tv.getComplexUnit();
    } else {
      return (COMPLEX_UNIT_MASK & (tv.data >> TypedValue.COMPLEX_UNIT_SHIFT));
    }
  }

  /**
   * Returns the @StyleableRes index that contains value in the attributes array. If both indices
   * contain values, the first given index takes precedence and is returned.
   */
  @StyleableRes
  public static int getIndexWithValue(
      @NonNull TypedArray attributes, @StyleableRes int a, @StyleableRes int b) {
    if (attributes.hasValue(a)) {
      return a;
    }
    return b;
  }
}
