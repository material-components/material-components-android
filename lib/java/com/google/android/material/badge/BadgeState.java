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

package com.google.android.material.badge;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.badge.BadgeDrawable.TOP_END;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.PluralsRes;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.annotation.StyleableRes;
import androidx.annotation.XmlRes;
import com.google.android.material.badge.BadgeDrawable.BadgeGravity;
import com.google.android.material.drawable.DrawableUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.resources.TextAppearance;
import java.util.Locale;

/**
 * Provides a {@link Parcelable} implementation ({@link State}) used to ensure the state of {@code
 * BadgeDrawable} is saved and restored properly, and the default values of the states are correctly
 * loaded during every restoration.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class BadgeState {
  /**
   * Maximum number of characters a badge supports displaying by default. It could be changed using
   * {@link BadgeDrawable#setMaxCharacterCount(int)}.
   */
  private static final int DEFAULT_MAX_BADGE_CHARACTER_COUNT = 4;

  private static final String BADGE_RESOURCE_TAG = "badge";

  private final State overridingState;
  private final State currentState = new State();

  final float badgeRadius;
  final float badgeWithTextRadius;
  final float badgeWidePadding;

  BadgeState(
      Context context,
      @XmlRes int badgeResId,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes,
      @Nullable State storedState) {
    if (storedState == null) {
      storedState = new State();
    }
    if (badgeResId != 0) {
      storedState.badgeResId = badgeResId;
    }

    TypedArray a = generateTypedArray(context, storedState.badgeResId, defStyleAttr, defStyleRes);

    Resources res = context.getResources();
    badgeRadius =
        a.getDimensionPixelSize(
            R.styleable.Badge_badgeRadius, res.getDimensionPixelSize(R.dimen.mtrl_badge_radius));
    badgeWidePadding =
        a.getDimensionPixelSize(
            R.styleable.Badge_badgeWidePadding,
            res.getDimensionPixelSize(R.dimen.mtrl_badge_long_text_horizontal_padding));
    badgeWithTextRadius =
        a.getDimensionPixelSize(
            R.styleable.Badge_badgeWithTextRadius,
            res.getDimensionPixelSize(R.dimen.mtrl_badge_with_text_radius));

    currentState.alpha = storedState.alpha == State.NOT_SET ? 255 : storedState.alpha;

    currentState.contentDescriptionNumberless =
        storedState.contentDescriptionNumberless == null
            ? context.getString(R.string.mtrl_badge_numberless_content_description)
            : storedState.contentDescriptionNumberless;

    currentState.contentDescriptionQuantityStrings =
        storedState.contentDescriptionQuantityStrings == 0
            ? R.plurals.mtrl_badge_content_description
            : storedState.contentDescriptionQuantityStrings;

    currentState.contentDescriptionExceedsMaxBadgeNumberRes =
        storedState.contentDescriptionExceedsMaxBadgeNumberRes == 0
            ? R.string.mtrl_exceed_max_badge_number_content_description
            : storedState.contentDescriptionExceedsMaxBadgeNumberRes;

    currentState.isVisible = storedState.isVisible == null || storedState.isVisible;

    currentState.maxCharacterCount =
        storedState.maxCharacterCount == State.NOT_SET
            ? a.getInt(R.styleable.Badge_maxCharacterCount, DEFAULT_MAX_BADGE_CHARACTER_COUNT)
            : storedState.maxCharacterCount;

    // Only set the badge number if it exists in the style.
    // Defaulting it to 0 means the badge will incorrectly show text when the user may want a
    // numberless badge.
    if (storedState.number != State.NOT_SET) {
      currentState.number = storedState.number;
    } else if (a.hasValue(R.styleable.Badge_number)) {
      currentState.number = a.getInt(R.styleable.Badge_number, 0);
    } else {
      currentState.number = State.BADGE_NUMBER_NONE;
    }

    currentState.backgroundColor =
        storedState.backgroundColor == null
            ? readColorFromAttributes(context, a, R.styleable.Badge_backgroundColor)
            : storedState.backgroundColor;

    // Only set the badge text color if this attribute has explicitly been set, otherwise use the
    // text color specified in the TextAppearance.
    if (storedState.badgeTextColor != null) {
      currentState.badgeTextColor = storedState.badgeTextColor;
    } else if (a.hasValue(R.styleable.Badge_badgeTextColor)) {
      currentState.badgeTextColor =
          readColorFromAttributes(context, a, R.styleable.Badge_badgeTextColor);
    } else {
      TextAppearance textAppearance =
          new TextAppearance(context, R.style.TextAppearance_MaterialComponents_Badge);
      currentState.badgeTextColor = textAppearance.getTextColor().getDefaultColor();
    }

    currentState.badgeGravity =
        storedState.badgeGravity == null
            ? a.getInt(R.styleable.Badge_badgeGravity, TOP_END)
            : storedState.badgeGravity;

    currentState.horizontalOffsetWithoutText =
        storedState.horizontalOffsetWithoutText == null
            ? a.getDimensionPixelOffset(R.styleable.Badge_horizontalOffset, 0)
            : storedState.horizontalOffsetWithoutText;

    currentState.verticalOffsetWithoutText =
        storedState.verticalOffsetWithoutText == null
            ? a.getDimensionPixelOffset(R.styleable.Badge_verticalOffset, 0)
            : storedState.verticalOffsetWithoutText;

    // Set the offsets when the badge has text. Default to using the badge "dot" offsets
    // (horizontalOffsetWithoutText and verticalOffsetWithoutText) if there is no offsets defined
    // for badges with text.
    currentState.horizontalOffsetWithText =
        storedState.horizontalOffsetWithText == null
            ? a.getDimensionPixelOffset(
                R.styleable.Badge_horizontalOffsetWithText,
                currentState.horizontalOffsetWithoutText)
            : storedState.horizontalOffsetWithText;

    currentState.verticalOffsetWithText =
        storedState.verticalOffsetWithText == null
            ? a.getDimensionPixelOffset(
                R.styleable.Badge_verticalOffsetWithText, currentState.verticalOffsetWithoutText)
            : storedState.verticalOffsetWithText;

    currentState.additionalHorizontalOffset =
        storedState.additionalHorizontalOffset == null ? 0 : storedState.additionalHorizontalOffset;

    currentState.additionalVerticalOffset =
        storedState.additionalVerticalOffset == null ? 0 : storedState.additionalVerticalOffset;

    a.recycle();

    if (storedState.numberLocale == null) {
      currentState.numberLocale =
          VERSION.SDK_INT >= VERSION_CODES.N
              ? Locale.getDefault(Locale.Category.FORMAT)
              : Locale.getDefault();
    } else {
      currentState.numberLocale = storedState.numberLocale;
    }

    overridingState = storedState;
  }

  private TypedArray generateTypedArray(
      Context context,
      @XmlRes int badgeResId,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    AttributeSet attrs = null;
    @StyleRes int style = 0;
    if (badgeResId != 0) {
      attrs = DrawableUtils.parseDrawableXml(context, badgeResId, BADGE_RESOURCE_TAG);
      style = attrs.getStyleAttribute();
    }
    if (style == 0) {
      style = defStyleRes;
    }

    return ThemeEnforcement.obtainStyledAttributes(
        context, attrs, R.styleable.Badge, defStyleAttr, style);
  }

  State getOverridingState() {
    return overridingState;
  }

  boolean isVisible() {
    return currentState.isVisible;
  }

  void setVisible(boolean visible) {
    overridingState.isVisible = visible;
    currentState.isVisible = visible;
  }

  boolean hasNumber() {
    return currentState.number != State.BADGE_NUMBER_NONE;
  }

  int getNumber() {
    return currentState.number;
  }

  void setNumber(int number) {
    overridingState.number = number;
    currentState.number = number;
  }

  void clearNumber() {
    setNumber(State.BADGE_NUMBER_NONE);
  }

  int getAlpha() {
    return currentState.alpha;
  }

  void setAlpha(int alpha) {
    overridingState.alpha = alpha;
    currentState.alpha = alpha;
  }

  int getMaxCharacterCount() {
    return currentState.maxCharacterCount;
  }

  void setMaxCharacterCount(int maxCharacterCount) {
    overridingState.maxCharacterCount = maxCharacterCount;
    currentState.maxCharacterCount = maxCharacterCount;
  }

  @ColorInt
  int getBackgroundColor() {
    return currentState.backgroundColor;
  }

  void setBackgroundColor(@ColorInt int backgroundColor) {
    overridingState.backgroundColor = backgroundColor;
    currentState.backgroundColor = backgroundColor;
  }

  @ColorInt
  int getBadgeTextColor() {
    return currentState.badgeTextColor;
  }

  void setBadgeTextColor(@ColorInt int badgeTextColor) {
    overridingState.badgeTextColor = badgeTextColor;
    currentState.badgeTextColor = badgeTextColor;
  }

  @BadgeGravity
  int getBadgeGravity() {
    return currentState.badgeGravity;
  }

  void setBadgeGravity(@BadgeGravity int badgeGravity) {
    overridingState.badgeGravity = badgeGravity;
    currentState.badgeGravity = badgeGravity;
  }

  @Dimension(unit = Dimension.PX)
  int getHorizontalOffsetWithoutText() {
    return currentState.horizontalOffsetWithoutText;
  }

  void setHorizontalOffsetWithoutText(@Dimension(unit = Dimension.PX) int offset) {
    overridingState.horizontalOffsetWithoutText = offset;
    currentState.horizontalOffsetWithoutText = offset;
  }

  @Dimension(unit = Dimension.PX)
  int getVerticalOffsetWithoutText() {
    return currentState.verticalOffsetWithoutText;
  }

  void setVerticalOffsetWithoutText(@Dimension(unit = Dimension.PX) int offset) {
    overridingState.verticalOffsetWithoutText = offset;
    currentState.verticalOffsetWithoutText = offset;
  }

  @Dimension(unit = Dimension.PX)
  int getHorizontalOffsetWithText() {
    return currentState.horizontalOffsetWithText;
  }

  void setHorizontalOffsetWithText(@Dimension(unit = Dimension.PX) int offset) {
    overridingState.horizontalOffsetWithText = offset;
    currentState.horizontalOffsetWithText = offset;
  }

  @Dimension(unit = Dimension.PX)
  int getVerticalOffsetWithText() {
    return currentState.verticalOffsetWithText;
  }

  void setVerticalOffsetWithText(@Dimension(unit = Dimension.PX) int offset) {
    overridingState.verticalOffsetWithText = offset;
    currentState.verticalOffsetWithText = offset;
  }

  @Dimension(unit = Dimension.PX)
  int getAdditionalHorizontalOffset() {
    return currentState.additionalHorizontalOffset;
  }

  void setAdditionalHorizontalOffset(@Dimension(unit = Dimension.PX) int offset) {
    overridingState.additionalHorizontalOffset = offset;
    currentState.additionalHorizontalOffset = offset;
  }

  @Dimension(unit = Dimension.PX)
  int getAdditionalVerticalOffset() {
    return currentState.additionalVerticalOffset;
  }

  void setAdditionalVerticalOffset(@Dimension(unit = Dimension.PX) int offset) {
    overridingState.additionalVerticalOffset = offset;
    currentState.additionalVerticalOffset = offset;
  }

  CharSequence getContentDescriptionNumberless() {
    return currentState.contentDescriptionNumberless;
  }

  void setContentDescriptionNumberless(CharSequence contentDescriptionNumberless) {
    overridingState.contentDescriptionNumberless = contentDescriptionNumberless;
    currentState.contentDescriptionNumberless = contentDescriptionNumberless;
  }

  @PluralsRes
  int getContentDescriptionQuantityStrings() {
    return currentState.contentDescriptionQuantityStrings;
  }

  void setContentDescriptionQuantityStringsResource(@PluralsRes int stringsResource) {
    overridingState.contentDescriptionQuantityStrings = stringsResource;
    currentState.contentDescriptionQuantityStrings = stringsResource;
  }

  @StringRes
  int getContentDescriptionExceedsMaxBadgeNumberStringResource() {
    return currentState.contentDescriptionExceedsMaxBadgeNumberRes;
  }

  void setContentDescriptionExceedsMaxBadgeNumberStringResource(@StringRes int stringsResource) {
    overridingState.contentDescriptionExceedsMaxBadgeNumberRes = stringsResource;
    currentState.contentDescriptionExceedsMaxBadgeNumberRes = stringsResource;
  }

  Locale getNumberLocale() {
    return currentState.numberLocale;
  }

  void setNumberLocale(Locale locale) {
    overridingState.numberLocale = locale;
    currentState.numberLocale = locale;
  }

  private static int readColorFromAttributes(
      Context context, @NonNull TypedArray a, @StyleableRes int index) {
    return MaterialResources.getColorStateList(context, a, index).getDefaultColor();
  }

  /**
   * Internal {@link Parcelable} state used to represent, save, and restore {@link BadgeDrawable}
   * states.
   */
  public static final class State implements Parcelable {
    /** Value of -1 denotes a numberless badge. */
    private static final int BADGE_NUMBER_NONE = -1;

    /** Value of -2 denotes a not-set state. */
    private static final int NOT_SET = -2;

    @XmlRes private int badgeResId;

    @ColorInt private Integer backgroundColor;
    @ColorInt private Integer badgeTextColor;
    private int alpha = 255;
    private int number = NOT_SET;
    private int maxCharacterCount = NOT_SET;
    private Locale numberLocale;

    @Nullable private CharSequence contentDescriptionNumberless;
    @PluralsRes private int contentDescriptionQuantityStrings;
    @StringRes private int contentDescriptionExceedsMaxBadgeNumberRes;

    @BadgeGravity private Integer badgeGravity;
    private Boolean isVisible = true;

    @Dimension(unit = Dimension.PX)
    private Integer horizontalOffsetWithoutText;

    @Dimension(unit = Dimension.PX)
    private Integer verticalOffsetWithoutText;

    @Dimension(unit = Dimension.PX)
    private Integer horizontalOffsetWithText;

    @Dimension(unit = Dimension.PX)
    private Integer verticalOffsetWithText;

    @Dimension(unit = Dimension.PX)
    private Integer additionalHorizontalOffset;

    @Dimension(unit = Dimension.PX)
    private Integer additionalVerticalOffset;

    public State() {}

    State(@NonNull Parcel in) {
      badgeResId = in.readInt();
      backgroundColor = (Integer) in.readSerializable();
      badgeTextColor = (Integer) in.readSerializable();
      alpha = in.readInt();
      number = in.readInt();
      maxCharacterCount = in.readInt();
      contentDescriptionNumberless = in.readString();
      contentDescriptionQuantityStrings = in.readInt();
      badgeGravity = (Integer) in.readSerializable();
      horizontalOffsetWithoutText = (Integer) in.readSerializable();
      verticalOffsetWithoutText = (Integer) in.readSerializable();
      horizontalOffsetWithText = (Integer) in.readSerializable();
      verticalOffsetWithText = (Integer) in.readSerializable();
      additionalHorizontalOffset = (Integer) in.readSerializable();
      additionalVerticalOffset = (Integer) in.readSerializable();
      isVisible = (Boolean) in.readSerializable();
      numberLocale = (Locale) in.readSerializable();
    }

    public static final Creator<State> CREATOR =
        new Creator<State>() {
          @NonNull
          @Override
          public BadgeState.State createFromParcel(@NonNull Parcel in) {
            return new State(in);
          }

          @NonNull
          @Override
          public State[] newArray(int size) {
            return new State[size];
          }
        };

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
      dest.writeInt(badgeResId);
      dest.writeSerializable(backgroundColor);
      dest.writeSerializable(badgeTextColor);
      dest.writeInt(alpha);
      dest.writeInt(number);
      dest.writeInt(maxCharacterCount);
      dest.writeString(
          contentDescriptionNumberless == null ? null : contentDescriptionNumberless.toString());
      dest.writeInt(contentDescriptionQuantityStrings);
      dest.writeSerializable(badgeGravity);
      dest.writeSerializable(horizontalOffsetWithoutText);
      dest.writeSerializable(verticalOffsetWithoutText);
      dest.writeSerializable(horizontalOffsetWithText);
      dest.writeSerializable(verticalOffsetWithText);
      dest.writeSerializable(additionalHorizontalOffset);
      dest.writeSerializable(additionalVerticalOffset);
      dest.writeSerializable(isVisible);
      dest.writeSerializable(numberLocale);
    }
  }
}
