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

package com.google.android.material.badge;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import androidx.annotation.ColorInt;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.android.material.shape.MaterialShapeDrawable;
import android.text.TextPaint;

/**
 * BadgeDrawable contains all the layout and draw logic for a badge.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
public class BadgeDrawable extends MaterialShapeDrawable {

  // Value of -1 denotes an icon only badge.
  private static final int ICON_ONLY_BADGE_NUMBER = -1;

  private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

  private int number = ICON_ONLY_BADGE_NUMBER;
  private int maxCharacterCount;
  private int alpha = 255;

  // Getters and setters for attributes.

  /**
   * Returns this badge's background color.
   *
   * @see #setbackgroundColor(int)
   * @attr ref com.google.android.material.R.styleable#Badge_backgroundColor
   */
  @ColorInt
  public int getBackgroundColor() {
    return getFillColor().getDefaultColor();
  }

  /**
   * Sets this badge's background color.
   *
   * @param backgroundColor This badge's background color.
   * @attr ref com.google.android.material.R.styleable#Badge_backgroundColor
   */
  public void setBackgroundColor(@ColorInt int backgroundColor) {
    if (filledPaint.getColor() != backgroundColor) {
      setFillColor(ColorStateList.valueOf(backgroundColor));
      invalidateSelf();
    }
  }

  /**
   * Returns this badge's text color.
   *
   * @see #setBadgeTextColor(int)
   * @attr ref com.google.android.material.R.styleable#Badge_badgeTextColor
   */
  @ColorInt
  public int getBadgeTextColor() {
    return textPaint.getColor();
  }

  /**
   * Sets this badge's text color.
   *
   * @param badgeTextColor This badge's text color.
   * @attr ref com.google.android.material.R.styleable#Badge_badgeTextColor
   */
  public void setBadgeTextColor(@ColorInt int badgeTextColor) {
    if (textPaint.getColor() != badgeTextColor) {
      textPaint.setColor(badgeTextColor);
      invalidateSelf();
    }
  }

  /**
   * Returns this badge's number.
   *
   * @see #setNumber(int)
   * @attr ref com.google.android.material.R.styleable#Badge_number
   */
  public int getNumber() {
    return number;
  }

  /**
   * Sets this badge's number. Only non-negative integer numbers are supported. If the number is
   * negative, it will be clamped to 0. The specified value will be displayed, unless its number of
   * digits exceeds {@code maxCharacterCount} in which case a truncated version will be shown.
   *
   * @param number This badge's number.
   * @attr ref com.google.android.material.R.styleable#Badge_number
   */
  public void setNumber(int number) {
    number = Math.max(0, number);
    if (this.number != number) {
      this.number = number;
      invalidateSelf();
    }
  }

  /** Resets any badge number so that only an icon badge will be displayed. */
  public void clearBadgeValue() {
    number = ICON_ONLY_BADGE_NUMBER;
    invalidateSelf();
  }

  /**
   * Returns this badge's max character count.
   *
   * @see #setMaxCharacterCount(int)
   * @attr ref com.google.android.material.R.styleable#Badge_maxCharacterCount
   */
  public int getMaxCharacterCount() {
    return maxCharacterCount;
  }

  /**
   * Sets this badge's max character count.
   *
   * @param maxCharacterCount This badge's max character count.
   * @attr ref com.google.android.material.R.styleable#Badge_maxCharacterCount
   */
  public void setMaxCharacterCount(int maxCharacterCount) {
    if (this.maxCharacterCount != maxCharacterCount) {
      this.maxCharacterCount = maxCharacterCount;
      invalidateSelf();
    }
  }

  @Override
  public void setColorFilter(ColorFilter colorFilter) {
    // Intentionally empty.
  }

  @Override
  public int getAlpha() {
    return alpha;
  }

  @Override
  public void setAlpha(int alpha) {
    if (this.alpha != alpha) {
      this.alpha = alpha;
      textPaint.setAlpha(alpha);
      invalidateSelf();
    }
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void draw(Canvas canvas) {
    Rect bounds = getBounds();
    if (bounds.isEmpty() || getAlpha() == 0 || !isVisible()) {
      return;
    }
    super.draw(canvas);
    if (number >= 0) {
      drawText(canvas);
    }
  }

  private void drawText(Canvas canvas) {
    // TODO: Add logic.
  }
}
