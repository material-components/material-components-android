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
import static com.google.android.material.badge.BadgeDrawable.BADGE_CONTENT_NOT_TRUNCATED;
import static com.google.android.material.badge.BadgeDrawable.BADGE_FIXED_EDGE_START;
import static com.google.android.material.badge.BadgeDrawable.BADGE_RADIUS_NOT_SPECIFIED;
import static com.google.android.material.badge.BadgeDrawable.OFFSET_ALIGNMENT_MODE_LEGACY;
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
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.annotation.StyleableRes;
import androidx.annotation.XmlRes;
import com.google.android.material.badge.BadgeDrawable.BadgeFixedEdge;
import com.google.android.material.badge.BadgeDrawable.BadgeGravity;
import com.google.android.material.badge.BadgeDrawable.OffsetAlignmentMode;
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

  private static final String BADGE_RESOURCE_TAG = "badge";

  private final State overridingState;
  private final State currentState = new State();

  final float badgeRadius;
  final float badgeWithTextRadius;
  final float badgeWidth;
  final float badgeHeight;
  final float badgeWithTextWidth;
  final float badgeWithTextHeight;
  final int horizontalInset;
  final int horizontalInsetWithText;

  @OffsetAlignmentMode
  int offsetAlignmentMode;

  @BadgeFixedEdge
  int badgeFixedEdge;

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
        a.getDimensionPixelSize(R.styleable.Badge_badgeRadius, BADGE_RADIUS_NOT_SPECIFIED);

    horizontalInset =
        context
            .getResources()
            .getDimensionPixelSize(R.dimen.mtrl_badge_horizontal_edge_offset);

    horizontalInsetWithText =
        context
            .getResources()
            .getDimensionPixelSize(R.dimen.mtrl_badge_text_horizontal_edge_offset);

    badgeWithTextRadius =
        a.getDimensionPixelSize(R.styleable.Badge_badgeWithTextRadius, BADGE_RADIUS_NOT_SPECIFIED);
    badgeWidth =
        a.getDimension(R.styleable.Badge_badgeWidth, res.getDimension(R.dimen.m3_badge_size));
    badgeWithTextWidth =
        a.getDimension(
            R.styleable.Badge_badgeWithTextWidth,
            res.getDimension(R.dimen.m3_badge_with_text_size));
    badgeHeight =
        a.getDimension(R.styleable.Badge_badgeHeight, res.getDimension(R.dimen.m3_badge_size));
    badgeWithTextHeight =
        a.getDimension(
            R.styleable.Badge_badgeWithTextHeight,
            res.getDimension(R.dimen.m3_badge_with_text_size));

    offsetAlignmentMode =
        a.getInt(R.styleable.Badge_offsetAlignmentMode, OFFSET_ALIGNMENT_MODE_LEGACY);

    badgeFixedEdge =
        a.getInt(R.styleable.Badge_badgeFixedEdge, BADGE_FIXED_EDGE_START);

    currentState.alpha = storedState.alpha == State.NOT_SET ? 255 : storedState.alpha;

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

    if (storedState.text != null) {
      currentState.text = storedState.text;
    } else if (a.hasValue(R.styleable.Badge_badgeText)) {
      currentState.text = a.getString(R.styleable.Badge_badgeText);
    }

    currentState.contentDescriptionForText = storedState.contentDescriptionForText;

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
            ? a.getInt(R.styleable.Badge_maxCharacterCount, BADGE_CONTENT_NOT_TRUNCATED)
            : storedState.maxCharacterCount;

    currentState.maxNumber =
        storedState.maxNumber == State.NOT_SET
            ? a.getInt(R.styleable.Badge_maxNumber, BADGE_CONTENT_NOT_TRUNCATED)
            : storedState.maxNumber;

    currentState.badgeShapeAppearanceResId =
        storedState.badgeShapeAppearanceResId == null
            ? a.getResourceId(
                R.styleable.Badge_badgeShapeAppearance,
                R.style.ShapeAppearance_M3_Sys_Shape_Corner_Full)
            : storedState.badgeShapeAppearanceResId;

    currentState.badgeShapeAppearanceOverlayResId =
        storedState.badgeShapeAppearanceOverlayResId == null
            ? a.getResourceId(R.styleable.Badge_badgeShapeAppearanceOverlay, 0)
            : storedState.badgeShapeAppearanceOverlayResId;

    currentState.badgeWithTextShapeAppearanceResId =
        storedState.badgeWithTextShapeAppearanceResId == null
            ? a.getResourceId(
                R.styleable.Badge_badgeWithTextShapeAppearance,
                R.style.ShapeAppearance_M3_Sys_Shape_Corner_Full)
            : storedState.badgeWithTextShapeAppearanceResId;

    currentState.badgeWithTextShapeAppearanceOverlayResId =
        storedState.badgeWithTextShapeAppearanceOverlayResId == null
            ? a.getResourceId(R.styleable.Badge_badgeWithTextShapeAppearanceOverlay, 0)
            : storedState.badgeWithTextShapeAppearanceOverlayResId;

    currentState.backgroundColor =
        storedState.backgroundColor == null
            ? readColorFromAttributes(context, a, R.styleable.Badge_backgroundColor)
            : storedState.backgroundColor;

    currentState.badgeTextAppearanceResId =
        storedState.badgeTextAppearanceResId == null
            ? a.getResourceId(
                R.styleable.Badge_badgeTextAppearance,
                R.style.TextAppearance_MaterialComponents_Badge)
            : storedState.badgeTextAppearanceResId;

    // Only set the badge text color if this attribute has explicitly been set, otherwise use the
    // text color specified in the TextAppearance.
    if (storedState.badgeTextColor != null) {
      currentState.badgeTextColor = storedState.badgeTextColor;
    } else if (a.hasValue(R.styleable.Badge_badgeTextColor)) {
      currentState.badgeTextColor =
          readColorFromAttributes(context, a, R.styleable.Badge_badgeTextColor);
    } else {
      TextAppearance textAppearance =
          new TextAppearance(context, currentState.badgeTextAppearanceResId);
      currentState.badgeTextColor = textAppearance.getTextColor().getDefaultColor();
    }

    currentState.badgeGravity =
        storedState.badgeGravity == null
            ? a.getInt(R.styleable.Badge_badgeGravity, TOP_END)
            : storedState.badgeGravity;

    currentState.badgeHorizontalPadding =
        storedState.badgeHorizontalPadding == null
            ? a.getDimensionPixelSize(
                R.styleable.Badge_badgeWidePadding,
                res.getDimensionPixelSize(R.dimen.mtrl_badge_long_text_horizontal_padding))
            : storedState.badgeHorizontalPadding;
    currentState.badgeVerticalPadding =
        storedState.badgeVerticalPadding == null
            ? a.getDimensionPixelSize(
                R.styleable.Badge_badgeVerticalPadding,
                res.getDimensionPixelSize(R.dimen.m3_badge_with_text_vertical_padding))
            : storedState.badgeVerticalPadding;

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

    currentState.largeFontVerticalOffsetAdjustment =
        storedState.largeFontVerticalOffsetAdjustment == null
            ? a.getDimensionPixelOffset(
            R.styleable.Badge_largeFontVerticalOffsetAdjustment, 0)
            : storedState.largeFontVerticalOffsetAdjustment;

    currentState.additionalHorizontalOffset =
        storedState.additionalHorizontalOffset == null ? 0 : storedState.additionalHorizontalOffset;

    currentState.additionalVerticalOffset =
        storedState.additionalVerticalOffset == null ? 0 : storedState.additionalVerticalOffset;

    currentState.autoAdjustToWithinGrandparentBounds =
        storedState.autoAdjustToWithinGrandparentBounds == null
            ? a.getBoolean(R.styleable.Badge_autoAdjustToWithinGrandparentBounds, false)
            : storedState.autoAdjustToWithinGrandparentBounds;

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

  boolean hasText() {
    return currentState.text != null;
  }

  String getText() {
    return currentState.text;
  }

  void setText(String text) {
    overridingState.text = text;
    currentState.text = text;
  }

  void clearText() {
    setText(null);
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

  int getMaxNumber() {
    return currentState.maxNumber;
  }

  void setMaxNumber(int maxNumber) {
    overridingState.maxNumber = maxNumber;
    currentState.maxNumber = maxNumber;
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

  @StyleRes
  int getTextAppearanceResId() {
    return currentState.badgeTextAppearanceResId;
  }

  void setTextAppearanceResId(@StyleRes int textAppearanceResId) {
    overridingState.badgeTextAppearanceResId = textAppearanceResId;
    currentState.badgeTextAppearanceResId = textAppearanceResId;
  }

  int getBadgeShapeAppearanceResId() {
    return currentState.badgeShapeAppearanceResId;
  }

  void setBadgeShapeAppearanceResId(int shapeAppearanceResId) {
    overridingState.badgeShapeAppearanceResId = shapeAppearanceResId;
    currentState.badgeShapeAppearanceResId = shapeAppearanceResId;
  }

  int getBadgeShapeAppearanceOverlayResId() {
    return currentState.badgeShapeAppearanceOverlayResId;
  }

  void setBadgeShapeAppearanceOverlayResId(int shapeAppearanceOverlayResId) {
    overridingState.badgeShapeAppearanceOverlayResId = shapeAppearanceOverlayResId;
    currentState.badgeShapeAppearanceOverlayResId = shapeAppearanceOverlayResId;
  }

  int getBadgeWithTextShapeAppearanceResId() {
    return currentState.badgeWithTextShapeAppearanceResId;
  }

  void setBadgeWithTextShapeAppearanceResId(int shapeAppearanceResId) {
    overridingState.badgeWithTextShapeAppearanceResId = shapeAppearanceResId;
    currentState.badgeWithTextShapeAppearanceResId = shapeAppearanceResId;
  }

  int getBadgeWithTextShapeAppearanceOverlayResId() {
    return currentState.badgeWithTextShapeAppearanceOverlayResId;
  }

  void setBadgeWithTextShapeAppearanceOverlayResId(int shapeAppearanceOverlayResId) {
    overridingState.badgeWithTextShapeAppearanceOverlayResId = shapeAppearanceOverlayResId;
    currentState.badgeWithTextShapeAppearanceOverlayResId = shapeAppearanceOverlayResId;
  }

  @BadgeGravity
  int getBadgeGravity() {
    return currentState.badgeGravity;
  }

  void setBadgeGravity(@BadgeGravity int badgeGravity) {
    overridingState.badgeGravity = badgeGravity;
    currentState.badgeGravity = badgeGravity;
  }

  @Px
  int getBadgeHorizontalPadding() {
    return currentState.badgeHorizontalPadding;
  }

  void setBadgeHorizontalPadding(@Px int horizontalPadding) {
    overridingState.badgeHorizontalPadding = horizontalPadding;
    currentState.badgeHorizontalPadding = horizontalPadding;
  }

  @Px
  int getBadgeVerticalPadding() {
    return currentState.badgeVerticalPadding;
  }

  void setBadgeVerticalPadding(@Px int verticalPadding) {
    overridingState.badgeVerticalPadding = verticalPadding;
    currentState.badgeVerticalPadding = verticalPadding;
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
  int getLargeFontVerticalOffsetAdjustment() {
    return currentState.largeFontVerticalOffsetAdjustment;
  }

  void setLargeFontVerticalOffsetAdjustment(@Dimension(unit = Dimension.PX) int offsetAdjustment) {
    overridingState.largeFontVerticalOffsetAdjustment = offsetAdjustment;
    currentState.largeFontVerticalOffsetAdjustment = offsetAdjustment;
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

  CharSequence getContentDescriptionForText() {
    return currentState.contentDescriptionForText;
  }

  void setContentDescriptionForText(CharSequence contentDescription) {
    overridingState.contentDescriptionForText = contentDescription;
    currentState.contentDescriptionForText = contentDescription;
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

  /** Deprecated; badges now adjust to within bounds of first ancestor that clips its children */
  @Deprecated
  boolean isAutoAdjustedToGrandparentBounds() {
    return currentState.autoAdjustToWithinGrandparentBounds;
  }

  /** Deprecated; badges now adjust to within bounds of first ancestor that clips its children */
  @Deprecated
  void setAutoAdjustToGrandparentBounds(boolean autoAdjustToGrandparentBounds) {
    overridingState.autoAdjustToWithinGrandparentBounds = autoAdjustToGrandparentBounds;
    currentState.autoAdjustToWithinGrandparentBounds = autoAdjustToGrandparentBounds;
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
    @StyleRes private Integer badgeTextAppearanceResId;

    @StyleRes private Integer badgeShapeAppearanceResId;
    @StyleRes private Integer badgeShapeAppearanceOverlayResId;
    @StyleRes private Integer badgeWithTextShapeAppearanceResId;
    @StyleRes private Integer badgeWithTextShapeAppearanceOverlayResId;

    private int alpha = 255;

    @Nullable private String text;
    private int number = NOT_SET;
    private int maxCharacterCount = NOT_SET;
    private int maxNumber = NOT_SET;
    private Locale numberLocale;

    @Nullable private CharSequence contentDescriptionForText;
    @Nullable private CharSequence contentDescriptionNumberless;
    @PluralsRes private int contentDescriptionQuantityStrings;
    @StringRes private int contentDescriptionExceedsMaxBadgeNumberRes;

    @BadgeGravity private Integer badgeGravity;
    private Boolean isVisible = true;

    @Px private Integer badgeHorizontalPadding;

    @Px private Integer badgeVerticalPadding;

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

    @Dimension(unit = Dimension.PX)
    private Integer largeFontVerticalOffsetAdjustment;

    private Boolean autoAdjustToWithinGrandparentBounds;

    @BadgeFixedEdge private Integer badgeFixedEdge;

    public State() {}

    State(@NonNull Parcel in) {
      badgeResId = in.readInt();
      backgroundColor = (Integer) in.readSerializable();
      badgeTextColor = (Integer) in.readSerializable();
      badgeTextAppearanceResId = (Integer) in.readSerializable();
      badgeShapeAppearanceResId = (Integer) in.readSerializable();
      badgeShapeAppearanceOverlayResId = (Integer) in.readSerializable();
      badgeWithTextShapeAppearanceResId = (Integer) in.readSerializable();
      badgeWithTextShapeAppearanceOverlayResId = (Integer) in.readSerializable();
      alpha = in.readInt();
      text = in.readString();
      number = in.readInt();
      maxCharacterCount = in.readInt();
      maxNumber = in.readInt();
      contentDescriptionForText = in.readString();
      contentDescriptionNumberless = in.readString();
      contentDescriptionQuantityStrings = in.readInt();
      badgeGravity = (Integer) in.readSerializable();
      badgeHorizontalPadding = (Integer) in.readSerializable();
      badgeVerticalPadding = (Integer) in.readSerializable();
      horizontalOffsetWithoutText = (Integer) in.readSerializable();
      verticalOffsetWithoutText = (Integer) in.readSerializable();
      horizontalOffsetWithText = (Integer) in.readSerializable();
      verticalOffsetWithText = (Integer) in.readSerializable();
      largeFontVerticalOffsetAdjustment = (Integer) in.readSerializable();
      additionalHorizontalOffset = (Integer) in.readSerializable();
      additionalVerticalOffset = (Integer) in.readSerializable();
      isVisible = (Boolean) in.readSerializable();
      numberLocale = (Locale) in.readSerializable();
      autoAdjustToWithinGrandparentBounds = (Boolean) in.readSerializable();
      badgeFixedEdge = (Integer) in.readSerializable();
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
      dest.writeSerializable(badgeTextAppearanceResId);
      dest.writeSerializable(badgeShapeAppearanceResId);
      dest.writeSerializable(badgeShapeAppearanceOverlayResId);
      dest.writeSerializable(badgeWithTextShapeAppearanceResId);
      dest.writeSerializable(badgeWithTextShapeAppearanceOverlayResId);
      dest.writeInt(alpha);
      dest.writeString(text);
      dest.writeInt(number);
      dest.writeInt(maxCharacterCount);
      dest.writeInt(maxNumber);
      dest.writeString(
          contentDescriptionForText != null ? contentDescriptionForText.toString() : null);
      dest.writeString(
          contentDescriptionNumberless != null ? contentDescriptionNumberless.toString() : null);
      dest.writeInt(contentDescriptionQuantityStrings);
      dest.writeSerializable(badgeGravity);
      dest.writeSerializable(badgeHorizontalPadding);
      dest.writeSerializable(badgeVerticalPadding);
      dest.writeSerializable(horizontalOffsetWithoutText);
      dest.writeSerializable(verticalOffsetWithoutText);
      dest.writeSerializable(horizontalOffsetWithText);
      dest.writeSerializable(verticalOffsetWithText);
      dest.writeSerializable(largeFontVerticalOffsetAdjustment);
      dest.writeSerializable(additionalHorizontalOffset);
      dest.writeSerializable(additionalVerticalOffset);
      dest.writeSerializable(isVisible);
      dest.writeSerializable(numberLocale);
      dest.writeSerializable(autoAdjustToWithinGrandparentBounds);
      dest.writeSerializable(badgeFixedEdge);
    }
  }
}
