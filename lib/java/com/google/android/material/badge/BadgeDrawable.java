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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
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
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Px;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.PluralsRes;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.annotation.StyleableRes;
import androidx.annotation.XmlRes;
import com.google.android.material.drawable.DrawableUtils;
import com.google.android.material.internal.TextDrawableHelper;
import com.google.android.material.internal.TextDrawableHelper.TextDrawableDelegate;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.shape.CutCornerTreatment;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * BadgeDrawable contains all the layout and draw logic for a badge.
 *
 * <p>You can use {@code BadgeDrawable} to display dynamic information such as a number of pending
 * requests in a {@link com.google.android.material.bottomnavigation.BottomNavigationView}. To
 * create an instance of {@code BadgeDrawable}, use {@link #create(Context)} or {@link
 * #createFromResources(Context, int)}. How to add and display a {@code BadgeDrawable} on top of its
 * anchor view depends on the API level:
 *
 * <p>For API 18+ (APIs supported by {@link android.view.ViewOverlay})
 *
 * <ul>
 *   <li>Add {@code BadgeDrawable} as a {@link android.view.ViewOverlay} to the desired anchor view
 *       using BadgeUtils#attachBadgeDrawable(BadgeDrawable, View, FrameLayout) (This helper class
 *       is currently package private).
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
 *       BadgeUtils#attachBadgeDrawable(BadgeDrawable, View, FrameLayout) (This helper class is
 *       currently package private).
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
 * <p>By default, {@code BadgeDrawable} is aligned to the top and end edges of its anchor view (with
 * some offsets). Call #setBadgeGravity(int) to change it to one of the other supported modes.
 *
 * <p>Note: This is still under development and may not support the full range of customization
 * Material Android components generally support (e.g. themed attributes).
 */
public class BadgeDrawable extends Drawable implements TextDrawableDelegate {

  /** Position the badge can be set to. */
  @IntDef({
    TOP_END,
    TOP_START,
    BOTTOM_END,
    BOTTOM_START,
  })
  @Retention(RetentionPolicy.SOURCE)
  public @interface BadgeGravity {}

  /** The badge is positioned along the top and end edges of its anchor view */
  public static final int TOP_END = Gravity.TOP | Gravity.END;

  /** The badge is positioned along the top and start edges of its anchor view */
  public static final int TOP_START = Gravity.TOP | Gravity.START;

  /** The badge is positioned along the bottom and end edges of its anchor view */
  public static final int BOTTOM_END = Gravity.BOTTOM | Gravity.END;

  /** The badge is positioned along the bottom and start edges of its anchor view */
  public static final int BOTTOM_START = Gravity.BOTTOM | Gravity.START;

  /**
   * Maximum number of characters a badge supports displaying by default. It could be changed using
   * BadgeDrawable#setMaxBadgeCount.
   */
  private static final int DEFAULT_MAX_BADGE_CHARACTER_COUNT = 4;

  /** Value of -1 denotes a numberless badge. */
  private static final int BADGE_NUMBER_NONE = -1;

  /** Maximum value of number that can be displayed in a circular badge. */
  private static final int MAX_CIRCULAR_BADGE_NUMBER_COUNT = 9;

  @StyleRes private static final int DEFAULT_STYLE = R.style.Widget_MaterialComponents_Badge;
  @AttrRes private static final int DEFAULT_THEME_ATTR = R.attr.badgeStyle;

  /**
   * If the badge number exceeds the maximum allowed number, append this suffix to the max badge
   * number and display is as the badge text instead.
   */
  static final String DEFAULT_EXCEED_MAX_BADGE_NUMBER_SUFFIX = "+";

  @NonNull private final WeakReference<Context> contextRef;
  @NonNull private final MaterialShapeDrawable shapeDrawable;
  @NonNull private final TextDrawableHelper textDrawableHelper;
  @NonNull private final Rect badgeBounds;
  private final float minBadgeRadius;
  private final float minBadgeWithTextRadius;
  private float badgeWidePadding;
  private float badgeWideCutPadding;
  @NonNull private final SavedState savedState;

  private float badgeCenterX;
  private float badgeCenterY;
  private int maxBadgeNumber;
  private float halfBadgeWidth;
  private float halfBadgeHeight;

  // Need to keep a local reference in order to support updating badge gravity.
  @Nullable private WeakReference<View> anchorViewRef;
  @Nullable private WeakReference<ViewGroup> customBadgeParentRef;

  /**
   * A {@link Parcelable} implementation used to ensure the state of BadgeDrawable is saved.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public static final class SavedState implements Parcelable {

    @ColorInt private int backgroundColor;
    @ColorInt private int badgeTextColor;
    private int alpha = 255;
    private int number = BADGE_NUMBER_NONE;
    private int maxCharacterCount;
    @Nullable private CharSequence contentDescriptionNumberless;
    @PluralsRes private int contentDescriptionQuantityStrings;
    @StringRes private int contentDescriptionExceedsMaxBadgeNumberRes;
    @BadgeGravity private int badgeGravity;

    @Dimension(unit = Dimension.PX)
    private int horizontalOffset;

    @Dimension(unit = Dimension.PX)
    private int verticalOffset;

    @StyleRes private int shapeAppearanceResId;
    @StyleRes private int shapeAppearanceOverlayResId;
    @Dimension private float badgeRadius;
    @Dimension private float badgeWithTextRadius;
    @ColorInt private int badgeStrokeColor;
    @Px private int badgeStrokeWidth;

    public SavedState(@NonNull Context context) {
      // If the badge text color attribute was not explicitly set, use the text color specified in
      // the TextAppearance.
      TextAppearance textAppearance =
          new TextAppearance(context, R.style.TextAppearance_MaterialComponents_Badge);
      badgeTextColor = textAppearance.textColor.getDefaultColor();
      contentDescriptionNumberless =
          context.getString(R.string.mtrl_badge_numberless_content_description);
      contentDescriptionQuantityStrings = R.plurals.mtrl_badge_content_description;
      contentDescriptionExceedsMaxBadgeNumberRes =
          R.string.mtrl_exceed_max_badge_number_content_description;
    }

    protected SavedState(@NonNull Parcel in) {
      backgroundColor = in.readInt();
      badgeTextColor = in.readInt();
      alpha = in.readInt();
      number = in.readInt();
      maxCharacterCount = in.readInt();
      contentDescriptionNumberless = in.readString();
      contentDescriptionQuantityStrings = in.readInt();
      badgeGravity = in.readInt();
      horizontalOffset = in.readInt();
      verticalOffset = in.readInt();
      badgeRadius = in.readFloat();
      badgeWithTextRadius = in.readFloat();
      badgeStrokeColor = in.readInt();
      badgeStrokeWidth = in.readInt();
      shapeAppearanceResId = in.readInt();
      shapeAppearanceOverlayResId = in.readInt();
    }

    public static final Creator<SavedState> CREATOR =
        new Creator<SavedState>() {
          @NonNull
          @Override
          public SavedState createFromParcel(@NonNull Parcel in) {
            return new SavedState(in);
          }

          @NonNull
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
    public void writeToParcel(@NonNull Parcel dest, int flags) {
      dest.writeInt(backgroundColor);
      dest.writeInt(badgeTextColor);
      dest.writeInt(alpha);
      dest.writeInt(number);
      dest.writeInt(maxCharacterCount);
      dest.writeString(contentDescriptionNumberless.toString());
      dest.writeInt(contentDescriptionQuantityStrings);
      dest.writeInt(badgeGravity);
      dest.writeInt(horizontalOffset);
      dest.writeInt(verticalOffset);
      dest.writeFloat(badgeRadius);
      dest.writeFloat(badgeWithTextRadius);
      dest.writeInt(badgeStrokeColor);
      dest.writeInt(badgeStrokeWidth);
      dest.writeInt(shapeAppearanceResId);
      dest.writeInt(shapeAppearanceOverlayResId);
    }
  }

  @NonNull
  public SavedState getSavedState() {
    return savedState;
  }

  /** Creates an instance of BadgeDrawable with the provided {@link SavedState}. */
  @NonNull
  static BadgeDrawable createFromSavedState(
      @NonNull Context context, @NonNull SavedState savedState) {
    BadgeDrawable badge = new BadgeDrawable(context);
    badge.restoreFromSavedState(savedState);
    return badge;
  }

  /** Creates an instance of BadgeDrawable with default values. */
  @NonNull
  public static BadgeDrawable create(@NonNull Context context) {
    return createFromAttributes(context, /* attrs= */ null, DEFAULT_THEME_ATTR, DEFAULT_STYLE);
  }

  /**
   * Returns a BadgeDrawable from the given XML resource. All attributes from {@link
   * R.styleable#Badge} and a custom <code>style</code> attribute are supported. A badge resource
   * may look like:
   *
   * <pre>{@code
   * <badge
   *     xmlns:app="http://schemas.android.com/apk/res-auto"
   *     style="@style/Widget.MaterialComponents.Badge"
   *     app:maxCharacterCount="2"/>
   * }</pre>
   */
  @NonNull
  public static BadgeDrawable createFromResource(@NonNull Context context, @XmlRes int id) {
    AttributeSet attrs = DrawableUtils.parseDrawableXml(context, id, "badge");
    @StyleRes int style = attrs.getStyleAttribute();
    if (style == 0) {
      style = DEFAULT_STYLE;
    }
    return createFromAttributes(context, attrs, DEFAULT_THEME_ATTR, style);
  }

  /** Returns a BadgeDrawable from the given attributes. */
  @NonNull
  private static BadgeDrawable createFromAttributes(
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

  private void restoreFromSavedState(@NonNull SavedState savedState) {
    setMaxCharacterCount(savedState.maxCharacterCount);

    // Only set the badge number if it exists in the style.
    // Defaulting it to 0 means the badge will incorrectly show text when the user may want a
    // numberless badge.
    if (savedState.number != BADGE_NUMBER_NONE) {
      setNumber(savedState.number);
    }

    initShapeAppearanceModel(contextRef.get(),
        savedState.shapeAppearanceResId, savedState.shapeAppearanceOverlayResId);

    setBackgroundColor(savedState.backgroundColor);

    // Only set the badge text color if this attribute has explicitly been set, otherwise use the
    // text color specified in the TextAppearance.
    setBadgeTextColor(savedState.badgeTextColor);

    setBadgeGravity(savedState.badgeGravity);

    setHorizontalOffset(savedState.horizontalOffset);
    setVerticalOffset(savedState.verticalOffset);
    setBadgeRadius(savedState.badgeRadius);
    setBadgeWithTextRadius(savedState.badgeWithTextRadius);
    setBadgeStrokeColor(savedState.badgeStrokeColor);
    setBadgeStrokeWidth(savedState.badgeStrokeWidth);
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

    initShapeAppearanceModel(context, attrs, defStyleAttr, defStyleRes);

    setBackgroundColor(readColorFromAttributes(context, a, R.styleable.Badge_backgroundColor));

    // Only set the badge text color if this attribute has explicitly been set, otherwise use the
    // text color specified in the TextAppearance.
    if (a.hasValue(R.styleable.Badge_badgeTextColor)) {
      setBadgeTextColor(readColorFromAttributes(context, a, R.styleable.Badge_badgeTextColor));
    }

    setBadgeGravity(a.getInt(R.styleable.Badge_badgeGravity, TOP_END));

    setHorizontalOffset(a.getDimensionPixelOffset(R.styleable.Badge_horizontalOffset, 0));
    setVerticalOffset(a.getDimensionPixelOffset(R.styleable.Badge_verticalOffset, 0));

    setBadgeRadius(a.getDimension(R.styleable.Badge_badgeRadius,0));
    setBadgeWithTextRadius(a.getDimension(R.styleable.Badge_badgeWithTextRadius,0));

    if (a.hasValue(R.styleable.Badge_badgeStrokeColor)){
      setBadgeStrokeColor(readColorFromAttributes(context, a, R.styleable.Badge_badgeStrokeColor));
    }
    setBadgeStrokeWidth(a.getDimensionPixelSize(R.styleable.Badge_badgeStrokeWidth,0));

    a.recycle();
  }

  private void initShapeAppearanceModel(Context context,AttributeSet attrs,
       @AttrRes int defStyleAttr, @StyleRes int defStyleRes){

    TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.MaterialShape, defStyleAttr, defStyleRes);

    int shapeAppearanceResId = a.getResourceId(R.styleable.MaterialShape_shapeAppearance, 0);
    int shapeAppearanceOverlayResId =
        a.getResourceId(R.styleable.MaterialShape_shapeAppearanceOverlay, 0);

    savedState.shapeAppearanceResId = shapeAppearanceResId;
    savedState.shapeAppearanceOverlayResId = shapeAppearanceOverlayResId;
    initShapeAppearanceModel(context, shapeAppearanceResId, shapeAppearanceOverlayResId);
  }

  private void initShapeAppearanceModel(Context context, int shapeAppearanceResId,
               int shapeAppearanceOverlayResId) {
    ShapeAppearanceModel shapeAppearanceModel =
        ShapeAppearanceModel
            .builder(context, shapeAppearanceResId, shapeAppearanceOverlayResId)
            .build();

    setShapeAppearanceModel(shapeAppearanceModel);
  }


  private static int readColorFromAttributes(
      Context context, @NonNull TypedArray a, @StyleableRes int index) {
    return MaterialResources.getColorStateList(context, a, index).getDefaultColor();
  }

  private BadgeDrawable(@NonNull Context context) {
    this.contextRef = new WeakReference<>(context);
    ThemeEnforcement.checkMaterialTheme(context);
    Resources res = context.getResources();
    badgeBounds = new Rect();
    shapeDrawable = new MaterialShapeDrawable();

    minBadgeRadius = res.getDimensionPixelSize(R.dimen.mtrl_badge_radius);
    minBadgeWithTextRadius = res.getDimensionPixelSize(R.dimen.mtrl_badge_with_text_radius);
    badgeWidePadding = res.getDimensionPixelSize(R.dimen.mtrl_badge_long_text_horizontal_padding);
    badgeWideCutPadding = badgeWidePadding + 12f;

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
    this.anchorViewRef = new WeakReference<>(anchorView);
    this.customBadgeParentRef = new WeakReference<>(customBadgeParent);
    updateCenterAndBounds();
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

  void setShapeAppearanceModel(ShapeAppearanceModel shapeAppearanceModel){
    shapeDrawable.setShapeAppearanceModel(shapeAppearanceModel);
    invalidateSelf();
  }

  ShapeAppearanceModel getShapeAppearanceModel(){
    return shapeDrawable.getShapeAppearanceModel();
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
      updateCenterAndBounds();
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
      updateCenterAndBounds();
      invalidateSelf();
    }
  }

  @BadgeGravity
  public int getBadgeGravity() {
    return savedState.badgeGravity;
  }

  /**
   * Sets this badge's gravity with respect to its anchor view.
   *
   * @param gravity Constant representing one of 4 possible {@link BadgeGravity} values.
   */
  public void setBadgeGravity(@BadgeGravity int gravity) {
    if (savedState.badgeGravity != gravity) {
      savedState.badgeGravity = gravity;
      if (anchorViewRef != null && anchorViewRef.get() != null) {
        updateBadgeCoordinates(
            anchorViewRef.get(), customBadgeParentRef != null ? customBadgeParentRef.get() : null);
      }
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
  public void draw(@NonNull Canvas canvas) {
    Rect bounds = getBounds();
    if (bounds.isEmpty() || getAlpha() == 0 || !isVisible()) {
      return;
    }
    shapeDrawable.draw(canvas);
    if (hasNumber()) {
      drawText(canvas);
    }
  }

  /**
   * Implements the TextDrawableHelper.TextDrawableDelegate interface.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
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

  public void setContentDescriptionQuantityStringsResource(@PluralsRes int stringsResource) {
    savedState.contentDescriptionQuantityStrings = stringsResource;
  }

  public void setContentDescriptionExceedsMaxBadgeNumberStringResource(
      @StringRes int stringsResource) {
    savedState.contentDescriptionExceedsMaxBadgeNumberRes = stringsResource;
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
        if (getNumber() <= maxBadgeNumber) {
          return context
              .getResources()
              .getQuantityString(
                  savedState.contentDescriptionQuantityStrings, getNumber(), getNumber());
        } else {
          return context.getString(
              savedState.contentDescriptionExceedsMaxBadgeNumberRes, maxBadgeNumber);
        }
      } else {
        return null;
      }
    } else {
      return savedState.contentDescriptionNumberless;
    }
  }

  /**
   * Sets how much (in pixels) to horizontally move this badge towards the center of its anchor.
   *
   * @param px badge's horizontal offset
   */
  public void setHorizontalOffset(int px) {
    savedState.horizontalOffset = px;
    updateCenterAndBounds();
  }

  /**
   * Returns how much (in pixels) this badge is being horizontally offset towards the center of its
   * anchor.
   */
  public int getHorizontalOffset() {
    return savedState.horizontalOffset;
  }

  /**
   * Sets how much (in pixels) to vertically move this badge towards the center of its anchor.
   *
   * @param px badge's vertical offset
   */
  public void setVerticalOffset(int px) {
    savedState.verticalOffset = px;
    updateCenterAndBounds();
  }

  /**
   * Returns how much (in pixels) this badge is being vertically moved towards the center of its
   * anchor.
   */
  public int getVerticalOffset() {
    return savedState.verticalOffset;
  }

  /**
   * Sets the badge's radius
   *
   * @param badgeRadius badge's radius
   * @attr ref com.google.android.material.R.styleable#Badge_badgeRadius
   * @see #setBadgeRadiusResource(int)
   * @see #getBadgeRadius()
   */
  public void setBadgeRadius(float badgeRadius) {
    savedState.badgeRadius = badgeRadius;
    updateCenterAndBounds();
  }

  /**
   * Sets the badge's radius using a dimension resource
   *
   * @param badgeRadiusResourceId Badge's radius dimension resource.
   * @attr ref com.google.android.material.R.styleable#Badge_badgeRadius
   * @see #setBadgeRadius(float)
   * @see #getBadgeRadius()
   */
  public void setBadgeRadiusResource(@DimenRes int badgeRadiusResourceId) {
    if (badgeRadiusResourceId != 0) {
      setBadgeRadius(contextRef.get().getResources().getDimension(badgeRadiusResourceId));
    }
  }

  /**
   * Returns the badge radius
   *
   * @return badge's radius
   * @attr ref com.google.android.material.R.styleable#Badge_badgeRadius
   * @see #setBadgeRadius(float)
   * @see #setBadgeRadiusResource(int)
   */
  public float getBadgeRadius() {
    return savedState.badgeRadius;
  }

  /**
   * Sets the badge with text radius using a dimension resource
   *
   * @param badgeWithTextRadius badge's with text radius dimension resource.
   * @attr ref com.google.android.material.R.styleable#Badge_badgeWithTextRadius
   * @see #setBadgeWithTextRadiusResource(int)
   * @see #getBadgeWithTextRadius()
   */
  public void setBadgeWithTextRadius(float badgeWithTextRadius) {
    savedState.badgeWithTextRadius = badgeWithTextRadius;
    updateCenterAndBounds();
  }

  /**
   * Sets the badge radius with text
   *
   * @param badgeWithTextRadiusResourceId badge's radius with text
   * @attr ref com.google.android.material.R.styleable#Badge_badgeWithTextRadius
   * @see #setBadgeWithTextRadius(float)
   * @see #getBadgeWithTextRadius()
   */
  public void setBadgeWithTextRadiusResource(@DimenRes int badgeWithTextRadiusResourceId) {
    if (badgeWithTextRadiusResourceId != 0) {
      setBadgeWithTextRadius(contextRef.get().getResources().getDimension(badgeWithTextRadiusResourceId));
    }
  }

  /**
   * Returns the badge with text radius
   *
   * @return badge with text radius
   * @attr ref com.google.android.material.R.styleable#Badge_badgeWithTextRadius
   * @see #setBadgeWithTextRadius(float)
   * @see #setBadgeWithTextRadiusResource(int)
   */
  public float getBadgeWithTextRadius() {
    return savedState.badgeWithTextRadius;
  }

  /**
   * Sets the badge stroke color
   *
   * @param badgeStrokeColor badge's radius
   * @attr ref com.google.android.material.R.styleable#Badge_badgeStrokeColor
   * @see #setBadgeStrokeColor(int)
   * @see #getBadgeStrokeColor()
   */
  public void setBadgeStrokeColor(@ColorInt int badgeStrokeColor) {
    savedState.badgeStrokeColor = badgeStrokeColor;
    ColorStateList colorStateList = ColorStateList.valueOf(badgeStrokeColor);
    if (shapeDrawable.getStrokeColor() != colorStateList) {
      shapeDrawable.setStrokeColor(colorStateList);
      invalidateSelf();
    }
  }

  /**
   * Sets the badge stroke color
   *
   * @param badgeStrokeColorResourceId Color resource to use for the stroke.
   * @attr ref com.google.android.material.R.styleable#MaterialButton_strokeColor
   * @see #setBadgeStrokeColor(int)
   * @see #getBadgeStrokeColor()
   */
  public void setBadgeStrokeColorResource(@ColorRes int badgeStrokeColorResourceId) {
    if (badgeStrokeColorResourceId != 0) {
      ColorStateList colorStateList = ContextCompat.getColorStateList(contextRef.get(), badgeStrokeColorResourceId);
      savedState.badgeStrokeColor = colorStateList.getDefaultColor();
      if (shapeDrawable.getStrokeColor() != colorStateList) {
        shapeDrawable.setStrokeColor(colorStateList);
        invalidateSelf();
      }
    }
  }

  /**
   * Returns this badge's stroke color.
   *
   * @see #setBadgeStrokeColor(int)
   * @attr ref com.google.android.material.R.styleable#Badge_badgeStrokeColor
   * @see #setBadgeStrokeColor(int)
   * @see #setBadgeStrokeColorResource(int)
   */
  @ColorInt
  public int getBadgeStrokeColor() {
    return shapeDrawable.getStrokeColor().getDefaultColor();
  }

  /**
   * Sets the badge stroke width
   *
   * @param badgeStrokeWidth badge's stroke width
   * @attr ref com.google.android.material.R.styleable#Badge_badgeStrokeWidth
   * @see #getBadgeStrokeWidth()
   * @see #setBadgeStrokeWidthResource(int)
   */
  public void setBadgeStrokeWidth(@Px int badgeStrokeWidth) {
    savedState.badgeStrokeWidth = badgeStrokeWidth;
    if (shapeDrawable.getStrokeWidth() != badgeStrokeWidth) {
      shapeDrawable.setStrokeWidth(badgeStrokeWidth);
      invalidateSelf();
    }
  }

  /**
   * Sets the badge stroke width
   *
   * @param badgeStrokeWidthResourceId badge's stroke width dimension resource
   * @attr ref com.google.android.material.R.styleable#Badge_badgeStrokeWidth
   * @see #setBadgeStrokeWidth(int)
   * @see #getBadgeStrokeWidth()
   */
  public void setBadgeStrokeWidthResource(@DimenRes int badgeStrokeWidthResourceId) {
    if (badgeStrokeWidthResourceId != 0) {
      setBadgeStrokeWidth(contextRef.get().getResources().getDimensionPixelSize(badgeStrokeWidthResourceId));
    }
  }

  /**
   * Returns this badge's stroke width.
   *
   * @see #setBadgeStrokeWidth(int)
   * @attr ref com.google.android.material.R.styleable#Badge_badgeStrokeWidth
   * @see #setBadgeStrokeWidth(int)
   * @see #setBadgeStrokeWidthResource(int)
   */
  @Dimension
  public float getBadgeStrokeWidth() {
    return shapeDrawable.getStrokeWidth();
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
    updateCenterAndBounds();
  }

  private void updateCenterAndBounds() {
    Context context = contextRef.get();
    View anchorView = anchorViewRef != null ? anchorViewRef.get() : null;
    if (context == null || anchorView == null) {
      return;
    }
    Rect tmpRect = new Rect();
    tmpRect.set(badgeBounds);

    Rect anchorRect = new Rect();
    // Retrieves the visible bounds of the anchor view.
    anchorView.getDrawingRect(anchorRect);

    ViewGroup customBadgeParent = customBadgeParentRef != null ? customBadgeParentRef.get() : null;
    if (customBadgeParent != null || BadgeUtils.USE_COMPAT_PARENT) {
      // Calculates coordinates relative to the parent.
      ViewGroup viewGroup =
          customBadgeParent == null ? (ViewGroup) anchorView.getParent() : customBadgeParent;
      viewGroup.offsetDescendantRectToMyCoords(anchorView, anchorRect);
    }

    calculateCenterAndBounds(context, anchorRect, anchorView);

    updateBadgeBounds(badgeBounds, badgeCenterX, badgeCenterY, halfBadgeWidth, halfBadgeHeight);

    //shapeDrawable.setCornerSize(cornerRadius);
    if (!tmpRect.equals(badgeBounds)) {
      shapeDrawable.setBounds(badgeBounds);
    }
  }

  private void calculateCenterAndBounds(
      @NonNull Context context, @NonNull Rect anchorRect, @NonNull View anchorView) {
    switch (savedState.badgeGravity) {
      case BOTTOM_END:
      case BOTTOM_START:
        badgeCenterY = anchorRect.bottom - savedState.verticalOffset;
        break;
      case TOP_END:
      case TOP_START:
      default:
        badgeCenterY = anchorRect.top + savedState.verticalOffset;
        break;
    }

    if (getNumber() <= MAX_CIRCULAR_BADGE_NUMBER_COUNT) {
      halfBadgeHeight = !hasNumber() ? getHalfBadgeHeight(minBadgeRadius, getBadgeRadius()) :
          getHalfBadgeHeight(minBadgeWithTextRadius, getBadgeWithTextRadius());
    } else {
      halfBadgeHeight = getHalfBadgeHeight(minBadgeWithTextRadius, getBadgeWithTextRadius());
    }
    String badgeText = getBadgeText();
    halfBadgeWidth = !hasNumber() ? getHalfBadgeWidth(minBadgeRadius, getBadgeRadius()) :
          Math.max( halfBadgeHeight, textDrawableHelper.getTextWidth(badgeText) / 2f + getPaddingWidth(getNumber()));

    int inset =
        context
            .getResources()
            .getDimensionPixelSize(
                hasNumber()
                    ? R.dimen.mtrl_badge_text_horizontal_edge_offset
                    : R.dimen.mtrl_badge_horizontal_edge_offset);
    // Update the centerX based on the badge width and 'inset' from start or end boundary of anchor.
    switch (savedState.badgeGravity) {
      case BOTTOM_START:
      case TOP_START:
        badgeCenterX =
            ViewCompat.getLayoutDirection(anchorView) == View.LAYOUT_DIRECTION_LTR
                ? anchorRect.left - halfBadgeWidth + inset + savedState.horizontalOffset
                : anchorRect.right + halfBadgeWidth - inset - savedState.horizontalOffset;
        break;
      case BOTTOM_END:
      case TOP_END:
      default:
        badgeCenterX =
            ViewCompat.getLayoutDirection(anchorView) == View.LAYOUT_DIRECTION_LTR
                ? anchorRect.right + halfBadgeWidth - inset - savedState.horizontalOffset
                : anchorRect.left - halfBadgeWidth + inset + savedState.horizontalOffset;
        break;
    }
  }

  private float getPaddingWidth(int number) {
    if (getShapeAppearanceModel().getTopLeftCorner() instanceof CutCornerTreatment
      || getShapeAppearanceModel().getTopRightCorner() instanceof CutCornerTreatment
      || getShapeAppearanceModel().getBottomLeftCorner() instanceof CutCornerTreatment
      || getShapeAppearanceModel().getBottomRightCorner() instanceof CutCornerTreatment){
      return badgeWideCutPadding;
    }
    return  badgeWidePadding;
  }

  private float getHalfBadgeHeight(float minBadgeRadius, float badgeRadius){
    return  (badgeRadius >= minBadgeRadius) ? badgeRadius : minBadgeRadius;
  }

  private float getHalfBadgeWidth(float minBadgeRadius, float badgeRadius){
    return  (badgeRadius >= minBadgeRadius) ? badgeRadius : minBadgeRadius;
  }

  private void drawText(Canvas canvas) {
    Rect textBounds = new Rect();
    String badgeText = getBadgeText();
    textDrawableHelper.getTextPaint().getTextBounds(badgeText, 0, badgeText.length(), textBounds);
    canvas.drawText(
        badgeText,
        badgeCenterX,
        badgeCenterY + textBounds.height() / 2,
        textDrawableHelper.getTextPaint());
  }

  @NonNull
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

}
