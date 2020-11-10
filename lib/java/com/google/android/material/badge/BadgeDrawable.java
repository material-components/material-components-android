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
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
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
import com.google.android.material.shape.MaterialShapeDrawable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;

/**
 * {@code BadgeDrawable} contains all the layout and draw logic for a badge.
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
 *       is currently experimental).
 *   <li>Update the {@code BadgeDrawable BadgeDrawable's} coordinates (center and bounds) based on
 *       its anchor view using {@link #updateBadgeCoordinates(View, FrameLayout)}.
 * </ul>
 *
 * <pre>
 * BadgeDrawable badgeDrawable = BadgeDrawable.create(context);
 * badgeDrawable.setVisible(true);
 * BadgeUtils.attachBadgeDrawable(badgeDrawable, anchor);
 * </pre>
 *
 * <p>For Pre API-18
 *
 * <ul>
 *   <li>Set {@code BadgeDrawable} as the foreground of the anchor view's {@code FrameLayout}
 *       ancestor using {@link BadgeUtils#attachBadgeDrawable(BadgeDrawable, View, FrameLayout)}
 *       (This helper class is currently experimental).
 *   <li>Update the {@code BadgeDrawable BadgeDrawable's} coordinates (center and bounds) based on
 *       its anchor view (relative to its {@code FrameLayout} ancestor's coordinate space), using
 *       {@link #updateBadgeCoordinates(View, FrameLayout)}.
 * </ul>
 *
 * Option 1: {@code BadgeDrawable} will dynamically create and wrap the anchor view in a {@code
 * FrameLayout}, then insert the {@code FrameLayout} into the anchor view original position in the
 * view hierarchy. Same syntax as API 18+
 *
 * <pre>
 * BadgeDrawable badgeDrawable = BadgeDrawable.create(context);
 * badgeDrawable.setVisible(true);
 * BadgeUtils.attachBadgeDrawable(badgeDrawable, anchor);
 * </pre>
 *
 * Option 2: If you do not want {@code BadgeDrawable} to modify your view hierarchy, you can specify
 * a {@code FrameLayout} to display the badge instead.
 *
 * <pre>
 * BadgeDrawable badgeDrawable = BadgeDrawable.create(context);
 * BadgeUtils.attachBadgeDrawable(badgeDrawable, anchor, anchorFrameLayoutParent);
 * </pre>
 *
 * <p>By default, {@code BadgeDrawable} is aligned to the top and end edges of its anchor view (with
 * some offsets). Call {@link #setBadgeGravity(int)} to change it to one of the other supported
 * modes. To adjust the badge's offsets w.r.t. the anchor's center, use {@link
 * BadgeDrawable#setHoriziontalOffset(int)}, {@link BadgeDrawable#setVerticalOffset(int)}
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
   * {@link BadgeDrawable#setMaxBadgeCount(int)}.
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
  private final float badgeRadius;
  private final float badgeWithTextRadius;
  private final float badgeWidePadding;
  @NonNull private final SavedState savedState;

  private float badgeCenterX;
  private float badgeCenterY;
  private int maxBadgeNumber;
  private float cornerRadius;
  private float halfBadgeWidth;
  private float halfBadgeHeight;

  // Need to keep a local reference in order to support updating badge gravity.
  @Nullable private WeakReference<View> anchorViewRef;
  @Nullable private WeakReference<FrameLayout> customBadgeParentRef;

  /**
   * A {@link Parcelable} implementation used to ensure the state of {@code BadgeDrawable} is saved.
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
    private boolean isVisible;

    @Dimension(unit = Dimension.PX)
    private int horizontalOffset;

    @Dimension(unit = Dimension.PX)
    private int verticalOffset;

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
      isVisible = true;
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
      isVisible = in.readInt() != 0;
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
      dest.writeInt(isVisible ? 1 : 0);
    }
  }

  @NonNull
  public SavedState getSavedState() {
    return savedState;
  }

  /** Creates an instance of {@code BadgeDrawable} with the provided {@link SavedState}. */
  @NonNull
  static BadgeDrawable createFromSavedState(
      @NonNull Context context, @NonNull SavedState savedState) {
    BadgeDrawable badge = new BadgeDrawable(context);
    badge.restoreFromSavedState(savedState);
    return badge;
  }

  /** Creates an instance of {@code BadgeDrawable} with default values. */
  @NonNull
  public static BadgeDrawable create(@NonNull Context context) {
    return createFromAttributes(context, /* attrs= */ null, DEFAULT_THEME_ATTR, DEFAULT_STYLE);
  }

  /**
   * Returns a {@code BadgeDrawable} from the given XML resource. All attributes from {@link
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

  /** Returns a {@code BadgeDrawable} from the given attributes. */
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
    savedState.isVisible = visible;
    // When hiding a badge in pre-API 18, invalidate the custom parent in order to trigger a draw
    // pass to remove this badge from its foreground.
    if (BadgeUtils.USE_COMPAT_PARENT && getCustomBadgeParent() != null && !visible) {
      ((ViewGroup) getCustomBadgeParent().getParent()).invalidate();
    }
  }

  private void restoreFromSavedState(@NonNull SavedState savedState) {
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

    setBadgeGravity(savedState.badgeGravity);

    setHorizontalOffset(savedState.horizontalOffset);
    setVerticalOffset(savedState.verticalOffset);
    setVisible(savedState.isVisible);
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

    setBadgeGravity(a.getInt(R.styleable.Badge_badgeGravity, TOP_END));

    setHorizontalOffset(a.getDimensionPixelOffset(R.styleable.Badge_horizontalOffset, 0));
    setVerticalOffset(a.getDimensionPixelOffset(R.styleable.Badge_verticalOffset, 0));

    a.recycle();
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
   * also updates this {@code BadgeDrawable BadgeDrawable's} bounds, because they are dependent on
   * the center coordinates. For pre API-18, coordinates will be calculated relative to {@code
   * customBadgeParent} because the {@code BadgeDrawable} will be set as the parent's foreground.
   *
   * @param anchorView This badge's anchor.
   * @param customBadgeParent An optional parent view that will set this {@code BadgeDrawable} as
   *     its foreground.
   * @deprecated use {@link BadgeDrawable#updateBadgeCoordinates(View, FrameLayout)} instead.
   */
  @Deprecated
  public void updateBadgeCoordinates(
      @NonNull View anchorView, @Nullable ViewGroup customBadgeParent) {
    if (customBadgeParent instanceof FrameLayout == false) {
      throw new IllegalArgumentException("customBadgeParent must be a FrameLayout");
    }
    updateBadgeCoordinates(anchorView, (FrameLayout) customBadgeParent);
  }

  /**
   * Calculates and updates this badge's center coordinates based on its anchor's bounds. Internally
   * also updates this {@code BadgeDrawable BadgeDrawable's} bounds, because they are dependent on
   * the center coordinates.
   *
   * <p>For pre API-18, optionally wrap the anchor in a {@code FrameLayout} (if it's not done
   * already) that will be inserted into the anchor's view hierarchy and calculate the badge's
   * coordinates the parent {@code FrameLayout} because the {@code BadgeDrawable} will be set as the
   * parent's foreground.
   *
   * @param anchorView This badge's anchor.
   */
  public void updateBadgeCoordinates(@NonNull View anchorView) {
    updateBadgeCoordinates(anchorView, null);
  }

  /**
   * Calculates and updates this badge's center coordinates based on its anchor's bounds. Internally
   * also updates this {@code BadgeDrawable BadgeDrawable's} bounds, because they are dependent on
   * the center coordinates.
   *
   * <p>For pre API-18, if no {@code customBadgeParent} is specified, optionally wrap the anchor in
   * a {@code FrameLayout} (if it's not done already) that will be inserted into the anchor's view
   * hierarchy and calculate the badge's coordinates the parent {@code FrameLayout} because the
   * {@code BadgeDrawable} will be set as the parent's foreground.
   *
   * @param anchorView This badge's anchor.
   * @param customBadgeParent An optional parent view that will set this {@code BadgeDrawable} as
   *     its foreground.
   */
  public void updateBadgeCoordinates(
      @NonNull View anchorView, @Nullable FrameLayout customBadgeParent) {
    this.anchorViewRef = new WeakReference<>(anchorView);

    if (BadgeUtils.USE_COMPAT_PARENT && customBadgeParent == null) {
      tryWrapAnchorInCompatParent(anchorView);
    } else {
      this.customBadgeParentRef = new WeakReference<>(customBadgeParent);
    }
    if (!BadgeUtils.USE_COMPAT_PARENT) {
      updateAnchorParentToNotClip(anchorView);
    }
    updateCenterAndBounds();
    invalidateSelf();
  }

  /** Returns a {@link FrameLayout} that will set this {@code BadgeDrawable} as its foreground. */
  @Nullable
  public FrameLayout getCustomBadgeParent() {
    return customBadgeParentRef != null ? customBadgeParentRef.get() : null;
  }

  /**
   * ViewOverlay is not supported below api 18, wrap the anchor view in a {@code FrameLayout} in
   * order to support scrolling.
   */
  private void tryWrapAnchorInCompatParent(final View anchorView) {
    ViewGroup anchorViewParent = (ViewGroup) anchorView.getParent();
    if ((anchorViewParent != null && anchorViewParent.getId() == R.id.mtrl_anchor_parent)
        || (customBadgeParentRef != null && customBadgeParentRef.get() == anchorViewParent)) {
      return;
    }
    // Must call this before wrapping the anchor in a FrameLayout.
    updateAnchorParentToNotClip(anchorView);

    // Create FrameLayout and configure it to wrap the anchor.
    final FrameLayout frameLayout = new FrameLayout(anchorView.getContext());
    frameLayout.setId(R.id.mtrl_anchor_parent);
    frameLayout.setClipChildren(false);
    frameLayout.setClipToPadding(false);
    frameLayout.setLayoutParams(anchorView.getLayoutParams());
    frameLayout.setMinimumWidth(anchorView.getWidth());
    frameLayout.setMinimumHeight(anchorView.getHeight());

    int anchorIndex = anchorViewParent.indexOfChild(anchorView);
    anchorViewParent.removeViewAt(anchorIndex);
    anchorView.setLayoutParams(
        new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    frameLayout.addView(anchorView);
    anchorViewParent.addView(frameLayout, anchorIndex);
    customBadgeParentRef = new WeakReference<>(frameLayout);

    // Update the badge's coordinates after the FrameLayout has been added to the view hierarchy and
    // has a size.
    frameLayout.post(
        new Runnable() {
          @Override
          public void run() {
            updateBadgeCoordinates(anchorView, frameLayout);
          }
        });
  }

  private static void updateAnchorParentToNotClip(View anchorView) {
    ViewGroup anchorViewParent = (ViewGroup) anchorView.getParent();
    anchorViewParent.setClipChildren(false);
    anchorViewParent.setClipToPadding(false);
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

    shapeDrawable.setCornerSize(cornerRadius);
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
      cornerRadius = !hasNumber() ? badgeRadius : badgeWithTextRadius;
      halfBadgeHeight = cornerRadius;
      halfBadgeWidth = cornerRadius;
    } else {
      cornerRadius = badgeWithTextRadius;
      halfBadgeHeight = cornerRadius;
      String badgeText = getBadgeText();
      halfBadgeWidth = textDrawableHelper.getTextWidth(badgeText) / 2f + badgeWidePadding;
    }

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
      return NumberFormat.getInstance().format(getNumber());
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
