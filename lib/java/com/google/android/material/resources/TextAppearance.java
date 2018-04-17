/*
 * Copyright 2017 The Android Open Source Project
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

package com.google.android.material.resources;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.FontRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.StyleRes;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextPaint;
import android.util.Log;

/**
 * Utility class that contains the data from parsing a TextAppearance style resource.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class TextAppearance {

  private static final String TAG = "TextAppearance";

  // Enums from AppCompatTextHelper.
  private static final int TYPEFACE_SANS = 1;
  private static final int TYPEFACE_SERIF = 2;
  private static final int TYPEFACE_MONOSPACE = 3;

  public final float textSize;
  @Nullable public final ColorStateList textColor;
  @Nullable public final ColorStateList textColorHint;
  @Nullable public final ColorStateList textColorLink;
  public final int textStyle;
  public final int typeface;
  @Nullable public final String fontFamily;
  public final boolean textAllCaps;
  @Nullable public final ColorStateList shadowColor;
  public final float shadowDx;
  public final float shadowDy;
  public final float shadowRadius;

  @FontRes private final int fontFamilyResourceId;

  private boolean fontResolved = false;
  @Nullable private Typeface font;

  /** Parses the given TextAppearance style resource. */
  public TextAppearance(Context context, @StyleRes int id) {
    TypedArray a = context.obtainStyledAttributes(id, R.styleable.TextAppearance);

    textSize = a.getDimension(R.styleable.TextAppearance_android_textSize, 0f);
    textColor =
        MaterialResources.getColorStateList(
            context, a, R.styleable.TextAppearance_android_textColor);
    textColorHint =
        MaterialResources.getColorStateList(
            context, a, R.styleable.TextAppearance_android_textColorHint);
    textColorLink =
        MaterialResources.getColorStateList(
            context, a, R.styleable.TextAppearance_android_textColorLink);
    textStyle = a.getInt(R.styleable.TextAppearance_android_textStyle, Typeface.NORMAL);
    typeface = a.getInt(R.styleable.TextAppearance_android_typeface, TYPEFACE_SANS);
    int fontFamilyIndex =
        MaterialResources.getIndexWithValue(
            a,
            R.styleable.TextAppearance_fontFamily,
            R.styleable.TextAppearance_android_fontFamily);
    fontFamilyResourceId = a.getResourceId(fontFamilyIndex, 0);
    fontFamily = a.getString(fontFamilyIndex);
    textAllCaps = a.getBoolean(R.styleable.TextAppearance_textAllCaps, false);
    shadowColor =
        MaterialResources.getColorStateList(
            context, a, R.styleable.TextAppearance_android_shadowColor);
    shadowDx = a.getFloat(R.styleable.TextAppearance_android_shadowDx, 0);
    shadowDy = a.getFloat(R.styleable.TextAppearance_android_shadowDy, 0);
    shadowRadius = a.getFloat(R.styleable.TextAppearance_android_shadowRadius, 0);

    a.recycle();
  }

  /**
   * Returns the font Typeface resolved from the fontFamily, style, and typeface.
   *
   * @see android.support.v7.widget.AppCompatTextHelper
   */
  @NonNull
  public Typeface getFont(Context context) {
    if (fontResolved) {
      return font;
    }

    // 1. Try resolving fontFamily as a font resource.
    if (!context.isRestricted()) {
      try {
        font = ResourcesCompat.getFont(context, fontFamilyResourceId);
        if (font != null) {
          font = Typeface.create(font, textStyle);
        }
      } catch (UnsupportedOperationException | Resources.NotFoundException e) {
        // Expected if it is not a font resource.
      } catch (Exception e) {
        Log.d(TAG, "Error loading font " + fontFamily, e);
      }
    }

    // 2. Try resolving fontFamily as a string name.
    if (font == null) {
      font = Typeface.create(fontFamily, textStyle);
    }

    // 3. Try resolving typeface.
    if (font == null) {
      switch (typeface) {
        case TYPEFACE_SANS:
          font = Typeface.SANS_SERIF;
          break;
        case TYPEFACE_SERIF:
          font = Typeface.SERIF;
          break;
        case TYPEFACE_MONOSPACE:
          font = Typeface.MONOSPACE;
          break;
        default:
          font = Typeface.DEFAULT;
          break;
      }
      if (font != null) {
        font = Typeface.create(font, textStyle);
      }
    }

    fontResolved = true;
    return font;
  }

  /**
   * Applies the attributes that affect drawing from TextAppearance to the given TextPaint. Note
   * that not all attributes can be applied to the TextPaint.
   *
   * @see android.text.style.TextAppearanceSpan#updateDrawState(TextPaint)
   */
  public void updateDrawState(Context context, TextPaint textPaint) {
    updateMeasureState(context, textPaint);

    textPaint.setColor(
        textColor != null
            ? textColor.getColorForState(textPaint.drawableState, textColor.getDefaultColor())
            : Color.BLACK);
    textPaint.setShadowLayer(
        shadowRadius,
        shadowDx,
        shadowDy,
        shadowColor != null
            ? shadowColor.getColorForState(textPaint.drawableState, shadowColor.getDefaultColor())
            : Color.TRANSPARENT);
  }

  /**
   * Applies the attributes that affect measurement from TextAppearance to the given TextPaint. Note
   * that not all attributes can be applied to the TextPaint.
   *
   * @see android.text.style.TextAppearanceSpan#updateMeasureState(TextPaint)
   */
  public void updateMeasureState(Context context, TextPaint textPaint) {
    Typeface tf = getFont(context);

    textPaint.setTypeface(tf);

    int fake = textStyle & ~tf.getStyle();
    textPaint.setFakeBoldText((fake & Typeface.BOLD) != 0);
    textPaint.setTextSkewX((fake & Typeface.ITALIC) != 0 ? -0.25f : 0f);

    textPaint.setTextSize(textSize);
  }
}
