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

import com.google.android.material.R;

import static com.google.android.material.badge.BadgeUtils.DEFAULT_MAX_BADGE_CHARACTER_COUNT;
import static com.google.android.material.badge.BadgeUtils.ICON_ONLY_BADGE_NUMBER;
import static com.google.android.material.badge.BadgeUtils.MAX_CIRCULAR_BADGE_NUMBER_COUNT;
import static com.google.android.material.badge.BadgeUtils.updateBadgeBounds;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.annotation.StyleableRes;
import com.google.android.material.internal.TextDrawableHelper;
import com.google.android.material.internal.TextDrawableHelper.TextDrawableDelegate;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.shape.MaterialShapeDrawable;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * BadgeDrawable contains all the layout and draw logic for a badge.
 *
 * <p>You can use {@code BadgeDrawable} to display dynamic information such as a number of pending
 * requests in a {@link com.google.android.material.bottomnavigation.BottomNavigationView}. To create an
 * instance of {@code BadgeDrawable}, use {@link #create(Context)} or {@link
 * #createFromAttributes(Context, AttributeSet, int, int)}. How to add and display a {@code
 * BadgeDrawable} on top of its anchor view depends on the API level:
 *
 * <p>For API 18+ (APIs supported by {@link android.view.ViewOverlay})
 *
 * <ul>
 *   <li>Add {@code BadgeDrawable} as a {@link android.view.ViewOverlay} to the desired anchor view
 *       using {@link BadgeUtils#attachBadgeDrawable(BadgeDrawable, View, FrameLayout)}.
 *   <li>Update the {@code BadgeDrawable BadgeDrawable's} coordinates (center and bounds) based on
 *       its anchor view using {@link #updateBadgeCoordinates(View, ViewGroup)}.
 * </ul>
 *
 * <pre>
 * BadgeDrawable badgeDrawable = BadgeDrawable.create(context);
 * BadgeUtils.attachBadgeDrawable(badgeDrawable, anchor, null);
 * badgeDrawable.updateBadgeCoordinates(anchor, null);
 * </pre>
 *
 * <p>For Pre API-18
 *
 * <ul>
 *   <li>Set {@code BadgeDrawable} as the foreground of the anchor view's FrameLayout ancestor using
 *       {@link BadgeUtils#attachBadgeDrawable(BadgeDrawable, View, FrameLayout)}.
 *   <li>Update the {@code BadgeDrawable BadgeDrawable's} coordinates (center and bounds) based on
 *       its anchor view (relative to its FrameLayout ancestor's coordinate space), using {@link
 *       #updateBadgeCoordinates(View, ViewGroup)}.
 * </ul>
 *
 * <pre>
 * BadgeDrawable badgeDrawable = BadgeDrawable.create(context);
 * BadgeUtils.attachBadgeDrawable(badgeDrawable, anchor, anchorFrameLayoutParent);
 * badgeDrawable.updateBadgeCoordinates(anchor, anchorFrameLayoutParent);
 * </pre>
 */
public class BadgeDrawable extends Drawable implements TextDrawableDelegate {
  private final Context context;
  private final MaterialShapeDrawable shapeDrawable;
  private final TextDrawableHelper textDrawableHelper;
  private final Rect badgeBounds;
  private final float iconOnlyRadius;
  private final float badgeWithTextRadius;
  private final float badgeWidePadding;
  private final Rect tmpRect;

  private float badgeCenterX;
  private float badgeCenterY;
  private int number = ICON_ONLY_BADGE_NUMBER;
  private int maxCharacterCount;
  private int alpha = 255;
  private int maxBadgeNumber;
  private boolean maxBadgeNumberDirty = true;

  /** Creates an instance of BadgeDrawable with default values. */
  public static BadgeDrawable create(Context context) {
    return createFromAttributes(
        context, /* attrs= */ null, /* defStyleAttr= */ 0, R.style.Widget_MaterialComponents_Badge);
  }

  /** Returns a BadgeDrawable from the given attributes. */
  public static BadgeDrawable createFromAttributes(
      @NonNull Context context,
      AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    BadgeDrawable badge = new BadgeDrawable(context);
    badge.loadFromAttributes(attrs, defStyleAttr, defStyleRes);
    return badge;
  }

  private void loadFromAttributes(
      AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.Badge, defStyleAttr, defStyleRes);

    setMaxCharacterCount(
        a.getInt(R.styleable.Badge_maxCharacterCount, DEFAULT_MAX_BADGE_CHARACTER_COUNT));

    setTextAppearanceResource(R.style.TextAppearance_MaterialComponents_Badge);

    // Only set the badge number if it exists in the style.
    // Defaulting it to 0 means the badge will incorrectly show text when the user may want an icon
    // only badge.
    if (a.hasValue(R.styleable.Badge_number)) {
      setNumber(a.getInt(R.styleable.Badge_number, 0));
    }

    setBackgroundColor(readColorFromAttributes(context, a, R.styleable.Badge_backgroundColor));

    // Only set the badge text color if this attribute has explicitly been set, otherwise use the
    // text color specified in the TextAppearance.
    if (a.hasValue(R.styleable.Badge_badgeTextColor)) {
      setBadgeTextColor(readColorFromAttributes(context, a, R.styleable.Badge_badgeTextColor));
    }

    a.recycle();
  }

  private static int readColorFromAttributes(
      Context context, TypedArray a, @StyleableRes int index) {
    return MaterialResources.getColorStateList(context, a, index).getDefaultColor();
  }

  private BadgeDrawable(Context context) {
    this.context = context;
    ThemeEnforcement.checkMaterialTheme(context);
    Resources res = context.getResources();
    tmpRect = new Rect();
    badgeBounds = new Rect();
    shapeDrawable = new MaterialShapeDrawable();

    iconOnlyRadius = res.getDimensionPixelSize(R.dimen.mtrl_badge_icon_only_radius);
    badgeWidePadding = res.getDimensionPixelSize(R.dimen.mtrl_badge_long_text_horizontal_padding);
    badgeWithTextRadius = res.getDimensionPixelSize(R.dimen.mtrl_badge_with_text_radius);

    textDrawableHelper = new TextDrawableHelper();
    textDrawableHelper.getTextPaint().setTextAlign(Paint.Align.CENTER);
  }

  /**
   * Calculates and updates this badge's center coordinates based on its anchor's bounds. Internally
   * also updates this BadgeDrawable's bounds, because they are dependent on the center coordinates.
   * For pre API-18, coordinates will be calculated relative to {@code customBadgeParent} because
   * the BadgeDrawable will be set as the parent's foreground.
   *
   * @param anchorView This badge's anchor.
   * @param customBadgeParent An optional parent view that will set this BadgeDrawable as its
   *     foreground.
   */
  public void updateBadgeCoordinates(
      @NonNull View anchorView, @Nullable ViewGroup customBadgeParent) {
    calculateBadgeCenterCoordinates(anchorView, customBadgeParent);
    updateBounds();
    invalidateSelf();
  }

  /**
   * Returns this badge's background color.
   *
   * @see #setBackgroundColor(int)
   * @attr ref com.google.android.material.R.styleable#Badge_backgroundColor
   */
  @ColorInt
  public int getBackgroundColor() {
    return shapeDrawable.getFillColor().getDefaultColor();
  }

  /**
   * Sets this badge's background color.
   *
   * @param backgroundColor This badge's background color.
   * @attr ref com.google.android.material.R.styleable#Badge_backgroundColor
   */
  public void setBackgroundColor(@ColorInt int backgroundColor) {
    ColorStateList backgroundColorStateList = ColorStateList.valueOf(backgroundColor);
    if (shapeDrawable.getFillColor() != backgroundColorStateList) {
      shapeDrawable.setFillColor(backgroundColorStateList);
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
    return textDrawableHelper.getTextPaint().getColor();
  }

  /**
   * Sets this badge's text color.
   *
   * @param badgeTextColor This badge's text color.
   * @attr ref com.google.android.material.R.styleable#Badge_badgeTextColor
   */
  public void setBadgeTextColor(@ColorInt int badgeTextColor) {
    if (textDrawableHelper.getTextPaint().getColor() != badgeTextColor) {
      textDrawableHelper.getTextPaint().setColor(badgeTextColor);
      invalidateSelf();
    }
  }

  /**
   * Returns this badge's number. Only non-negative integer numbers will be returned because the
   * setter clamps negative values to 0.
   *
   * <p> WARNING: Do not call this method if you are planning to compare to ICON_ONLY_BADGE_NUMBER
   * 
   * @see #setNumber(int)
   * @attr ref com.google.android.material.R.styleable#Badge_number
   */
  public int getNumber() {
    if (number == ICON_ONLY_BADGE_NUMBER) {
      return 0;
    }
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
      textDrawableHelper.setTextWidthDirty(true);
      updateBounds();
      invalidateSelf();
    }
  }

  /** Resets any badge number so that only an icon badge will be displayed. */
  public void clearBadgeNumber() {
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
      textDrawableHelper.setTextWidthDirty(true);
      updateBounds();
      invalidateSelf();
    }
  }

  @Override
  public boolean isStateful() {
    return false;
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
    this.alpha = alpha;
    textDrawableHelper.getTextPaint().setAlpha(alpha);
    invalidateSelf();
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  /** Returns the height at which the badge would like to be laid out. */
  @Override
  public int getIntrinsicHeight() {
    return badgeBounds.height();
  }

  /** Returns the width at which the badge would like to be laid out. */
  @Override
  public int getIntrinsicWidth() {
    return badgeBounds.width();
  }

  @Override
  public void draw(Canvas canvas) {
    Rect bounds = getBounds();
    if (bounds.isEmpty() || getAlpha() == 0 || !isVisible()) {
      return;
    }
    shapeDrawable.draw(canvas);
    if (number != ICON_ONLY_BADGE_NUMBER) {
      drawText(canvas);
    }
  }

  // Implements the TextDrawableHelper.TextDrawableDelegate interface.
  @Override
  public void onTextSizeChange() {
    invalidateSelf();
  }

  @Override
  public boolean onStateChange(int[] state) {
    return super.onStateChange(state);
  }

  private void setTextAppearanceResource(@StyleRes int id) {
    setTextAppearance(new TextAppearance(context, id));
  }

  private void setTextAppearance(@Nullable TextAppearance textAppearance) {
    if (textDrawableHelper.getTextAppearance() == textAppearance) {
      return;
    }
    textDrawableHelper.setTextAppearance(textAppearance, context);
    updateBounds();
  }

  private void updateBounds() {
    float cornerRadius;
    tmpRect.set(badgeBounds);
    if (getNumber() <= MAX_CIRCULAR_BADGE_NUMBER_COUNT) {
      cornerRadius = (number == ICON_ONLY_BADGE_NUMBER) ? iconOnlyRadius : badgeWithTextRadius;
      updateBadgeBounds(badgeBounds, badgeCenterX, badgeCenterY, cornerRadius, cornerRadius);
    } else {
      cornerRadius = badgeWithTextRadius;
      float halfBadgeWidth =
          textDrawableHelper.getTextWidth(getBadgeText()) / 2f + badgeWidePadding;
      updateBadgeBounds(badgeBounds, badgeCenterX, badgeCenterY, halfBadgeWidth, cornerRadius);
    }
    shapeDrawable.setCornerRadius(cornerRadius);
    if (!tmpRect.equals(badgeBounds)) {
      shapeDrawable.setBounds(badgeBounds);
    }
  }

  private void drawText(Canvas canvas) {
    Rect textBounds = new Rect();
    String countText = getBadgeText();
    textDrawableHelper.getTextPaint().getTextBounds(countText, 0, countText.length(), textBounds);
    canvas.drawText(
        countText,
        badgeCenterX,
        badgeCenterY + textBounds.height() / 2,
        textDrawableHelper.getTextPaint());
  }

  private String getBadgeText() {
    // If number exceeds max count, show badgeMaxCount+ instead of the number.
    int maxBadgeNumber = getMaxBadgeNumber();
    if (getNumber() <= maxBadgeNumber) {
      return Integer.toString(getNumber());
    } else {
      return context.getString(
          R.string.mtrl_exceed_max_badge_number_suffix,
          maxBadgeNumber,
          BadgeUtils.DEFAULT_EXCEED_MAX_BADGE_NUMBER_SUFFIX);
    }
  }

  private int getMaxBadgeNumber() {
    if (!maxBadgeNumberDirty) {
      return maxBadgeNumber;
    }
    maxBadgeNumber = (int) Math.pow(10.0d, (double) getMaxCharacterCount() - 1) - 1;
    maxBadgeNumberDirty = false;
    return maxBadgeNumber;
  }

  private void calculateBadgeCenterCoordinates(
      @NonNull View anchorView, @Nullable ViewGroup customBadgeParent) {
    Resources res = context.getResources();
    Rect anchorRect = new Rect();
    // Returns the visible bounds of the anchor view.
    anchorView.getDrawingRect(anchorRect);
    anchorRect.top += res.getDimensionPixelSize(R.dimen.mtrl_badge_vertical_offset);
    if (customBadgeParent != null || VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN_MR2) {
      // Calculates coordinates relative to the parent.
      ViewGroup viewGroup =
          customBadgeParent == null ? (ViewGroup) anchorView.getParent() : customBadgeParent;
      viewGroup.offsetDescendantRectToMyCoords(anchorView, anchorRect);
    }

    badgeCenterX =
        ViewCompat.getLayoutDirection(anchorView) == View.LAYOUT_DIRECTION_LTR
            ? anchorRect.right
            : anchorRect.left;
    badgeCenterY = anchorRect.top;
  }
}
