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
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;
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
import android.widget.FrameLayout;
import java.lang.ref.WeakReference;

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
 * </pre>
 *
 * Note: This is still under development and may not support the full range of customization
 * Material Android components generally support (e.g. themed attributes).
 */
public class BadgeDrawable extends Drawable implements TextDrawableDelegate {
  /**
   * Maximum number of characters a badge supports displaying by default. It could be changed using
   * BadgeDrawable#setMaxBadgeCount.
   */
  private static final int DEFAULT_MAX_BADGE_CHARACTER_COUNT = 4;

  /** Value of -1 denotes a numberless badge. */
  private static final int BADGE_NUMBER_NONE = -1;

  /** Maximum value of number that can be displayed in a circular badge. */
  private static final int MAX_CIRCULAR_BADGE_NUMBER_COUNT = 99;

  /**
   * If the badge number exceeds the maximum allowed number, append this suffix to the max badge
   * number and display is as the badge text instead.
   */
  static final String DEFAULT_EXCEED_MAX_BADGE_NUMBER_SUFFIX = "+";

  private final WeakReference<Context> contextRef;
  private final MaterialShapeDrawable shapeDrawable;
  private final TextDrawableHelper textDrawableHelper;
  private final Rect badgeBounds;
  private final float badgeRadius;
  private final float badgeWithTextRadius;
  private final float badgeWidePadding;
  private final Rect tmpRect;
  private final SavedState savedState;

  private float badgeCenterX;
  private float badgeCenterY;
  private int maxBadgeNumber;

  /** A {@link Parcelable} implementation used to ensure the state of BadgeDrawable is saved. */
  public static final class SavedState implements Parcelable {

    @ColorInt private int backgroundColor;
    @ColorInt private int badgeTextColor;
    private int alpha = 255;
    private int number = BADGE_NUMBER_NONE;
    private int maxCharacterCount;
    private CharSequence contentDescriptionNumberless;
    @PluralsRes private int contentDescriptionQuantityStrings;

    public SavedState(Context context) {
      // If the badge text color attribute was not explicitly set, use the text color specified in
      // the TextAppearance.
      TextAppearance textAppearance =
          new TextAppearance(context, R.style.TextAppearance_MaterialComponents_Badge);
      badgeTextColor = textAppearance.textColor.getDefaultColor();
      contentDescriptionNumberless =
          context.getString(R.string.mtrl_badge_numberless_content_description);
      contentDescriptionQuantityStrings = R.plurals.mtrl_badge_content_description;
    }

    protected SavedState(Parcel in) {
      backgroundColor = in.readInt();
      badgeTextColor = in.readInt();
      alpha = in.readInt();
      number = in.readInt();
      maxCharacterCount = in.readInt();
      contentDescriptionNumberless = in.readString();
      contentDescriptionQuantityStrings = in.readInt();
    }

    public static final Creator<SavedState> CREATOR =
        new Creator<SavedState>() {
          @Override
          public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
          }

          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(backgroundColor);
      dest.writeInt(badgeTextColor);
      dest.writeInt(alpha);
      dest.writeInt(number);
      dest.writeInt(maxCharacterCount);
      dest.writeString(contentDescriptionNumberless.toString());
      dest.writeInt(contentDescriptionQuantityStrings);
    }
  }

  public SavedState getSavedState() {
    return savedState;
  }

  /** Creates an instance of BadgeDrawable with the provided {@link SavedState}. */
  public static BadgeDrawable createFromSavedState(
      Context context, @NonNull SavedState savedState) {
    BadgeDrawable badge = new BadgeDrawable(context);
    badge.restoreFromSavedState(savedState);
    return badge;
  }

  /** Creates an instance of BadgeDrawable with default values. */
  public static BadgeDrawable create(Context context) {
    return createFromAttributes(
        context, /* attrs= */ null, R.attr.badgeStyle, R.style.Widget_MaterialComponents_Badge);
  }

  /** Returns a BadgeDrawable from the given attributes. */
  public static BadgeDrawable createFromAttributes(
      @NonNull Context context,
      AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    BadgeDrawable badge = new BadgeDrawable(context);
    badge.loadDefaultStateFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    return badge;
  }

  /**
   * Convenience wrapper method for {@link Drawable#setVisible(boolean, boolean)} with the {@code
   * restart} parameter hardcoded to false.
   */
  public void setVisible(boolean visible) {
    setVisible(visible, /* restart= */ false);
  }

  private void restoreFromSavedState(SavedState savedState) {
    setMaxCharacterCount(savedState.maxCharacterCount);

    // Only set the badge number if it exists in the style.
    // Defaulting it to 0 means the badge will incorrectly show text when the user may want a
    // numberless badge.
    if (savedState.number != BADGE_NUMBER_NONE) {
      setNumber(savedState.number);
    }

    setBackgroundColor(savedState.backgroundColor);

    // Only set the badge text color if this attribute has explicitly been set, otherwise use the
    // text color specified in the TextAppearance.
    setBadgeTextColor(savedState.badgeTextColor);
  }

  private void loadDefaultStateFromAttributes(
      Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.Badge, defStyleAttr, defStyleRes);

    setMaxCharacterCount(
        a.getInt(R.styleable.Badge_maxCharacterCount, DEFAULT_MAX_BADGE_CHARACTER_COUNT));

    // Only set the badge number if it exists in the style.
    // Defaulting it to 0 means the badge will incorrectly show text when the user may want a
    // numberless badge.
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
    this.contextRef = new WeakReference<>(context);
    ThemeEnforcement.checkMaterialTheme(context);
    Resources res = context.getResources();
    tmpRect = new Rect();
    badgeBounds = new Rect();
    shapeDrawable = new MaterialShapeDrawable();

    badgeRadius = res.getDimensionPixelSize(R.dimen.mtrl_badge_radius);
    badgeWidePadding = res.getDimensionPixelSize(R.dimen.mtrl_badge_long_text_horizontal_padding);
    badgeWithTextRadius = res.getDimensionPixelSize(R.dimen.mtrl_badge_with_text_radius);

    textDrawableHelper = new TextDrawableHelper(/* delegate= */ this);
    textDrawableHelper.getTextPaint().setTextAlign(Paint.Align.CENTER);
    this.savedState = new SavedState(context);
    setTextAppearanceResource(R.style.TextAppearance_MaterialComponents_Badge);
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
    savedState.backgroundColor = backgroundColor;
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
    savedState.badgeTextColor = badgeTextColor;
    if (textDrawableHelper.getTextPaint().getColor() != badgeTextColor) {
      textDrawableHelper.getTextPaint().setColor(badgeTextColor);
      invalidateSelf();
    }
  }

  /** Returns whether this badge will display a number. */
  public boolean hasNumber() {
    return savedState.number != BADGE_NUMBER_NONE;
  }

  /**
   * Returns this badge's number. Only non-negative integer numbers will be returned because the
   * setter clamps negative values to 0.
   *
   * <p>WARNING: Do not call this method if you are planning to compare to BADGE_NUMBER_NONE
   *
   * @see #setNumber(int)
   * @attr ref com.google.android.material.R.styleable#Badge_number
   */
  public int getNumber() {
    if (!hasNumber()) {
      return 0;
    }
    return savedState.number;
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
    if (this.savedState.number != number) {
      this.savedState.number = number;
      textDrawableHelper.setTextWidthDirty(true);
      updateBounds();
      invalidateSelf();
    }
  }

  /** Resets any badge number so that a numberless badge will be displayed. */
  public void clearNumber() {
    savedState.number = BADGE_NUMBER_NONE;
    invalidateSelf();
  }

  /**
   * Returns this badge's max character count.
   *
   * @see #setMaxCharacterCount(int)
   * @attr ref com.google.android.material.R.styleable#Badge_maxCharacterCount
   */
  public int getMaxCharacterCount() {
    return savedState.maxCharacterCount;
  }

  /**
   * Sets this badge's max character count.
   *
   * @param maxCharacterCount This badge's max character count.
   * @attr ref com.google.android.material.R.styleable#Badge_maxCharacterCount
   */
  public void setMaxCharacterCount(int maxCharacterCount) {
    if (this.savedState.maxCharacterCount != maxCharacterCount) {
      this.savedState.maxCharacterCount = maxCharacterCount;
      updateMaxBadgeNumber();
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
    return savedState.alpha;
  }

  @Override
  public void setAlpha(int alpha) {
    this.savedState.alpha = alpha;
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
    if (hasNumber()) {
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

  public void setContentDescriptionNumberless(CharSequence charSequence) {
    savedState.contentDescriptionNumberless = charSequence;
  }

  public void setContentDescriptionQuantityStringsResource(@StringRes int stringsResource) {
    savedState.contentDescriptionQuantityStrings = stringsResource;
  }

  @Nullable
  public CharSequence getContentDescription() {
    if (!isVisible()) {
      return null;
    }
    if (hasNumber()) {
      if (savedState.contentDescriptionQuantityStrings > 0) {
        Context context = contextRef.get();
        if (context == null) {
          return null;
        }
        return context
            .getResources()
            .getQuantityString(
                savedState.contentDescriptionQuantityStrings, getNumber(), getNumber());
      } else {
        return null;
      }
    } else {
      return savedState.contentDescriptionNumberless;
    }
  }

  private void setTextAppearanceResource(@StyleRes int id) {
    Context context = contextRef.get();
    if (context == null) {
      return;
    }
    setTextAppearance(new TextAppearance(context, id));
  }

  private void setTextAppearance(@Nullable TextAppearance textAppearance) {
    if (textDrawableHelper.getTextAppearance() == textAppearance) {
      return;
    }
    Context context = contextRef.get();
    if (context == null) {
      return;
    }
    textDrawableHelper.setTextAppearance(textAppearance, context);
    updateBounds();
  }

  private void updateBounds() {
    float cornerRadius;
    tmpRect.set(badgeBounds);
    if (getNumber() <= MAX_CIRCULAR_BADGE_NUMBER_COUNT) {
      cornerRadius = !hasNumber() ? badgeRadius : badgeWithTextRadius;

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
    if (getNumber() <= maxBadgeNumber) {
      return Integer.toString(getNumber());
    } else {
      Context context = contextRef.get();
      if (context == null) {
        return "";
      }

      return context.getString(
          R.string.mtrl_exceed_max_badge_number_suffix,
          maxBadgeNumber,
          DEFAULT_EXCEED_MAX_BADGE_NUMBER_SUFFIX);
    }
  }

  private void updateMaxBadgeNumber() {
    maxBadgeNumber = (int) Math.pow(10.0d, (double) getMaxCharacterCount() - 1) - 1;
  }

  private void calculateBadgeCenterCoordinates(
      @NonNull View anchorView, @Nullable ViewGroup customBadgeParent) {
    Context context = contextRef.get();
    if (context == null) {
      return;
    }
    Resources res = context.getResources();
    Rect anchorRect = new Rect();
    // Returns the visible bounds of the anchor view.
    anchorView.getDrawingRect(anchorRect);
    anchorRect.top += res.getDimensionPixelSize(R.dimen.mtrl_badge_vertical_offset);
    if (customBadgeParent != null || BadgeUtils.USE_COMPAT_PARENT) {
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
