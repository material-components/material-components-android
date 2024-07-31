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
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.PluralsRes;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.annotation.XmlRes;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.internal.TextDrawableHelper;
import com.google.android.material.internal.TextDrawableHelper.TextDrawableDelegate;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * {@code BadgeDrawable} contains all the layout and draw logic for a badge.
 *
 * <p>You can use {@code BadgeDrawable} to display dynamic information such as a number of pending
 * requests in a {@link com.google.android.material.bottomnavigation.BottomNavigationView}. To
 * create an instance of {@code BadgeDrawable}, use {@link #create(Context)} or {@link
 * #createFromResource(Context, int)}. How to add and display a {@code BadgeDrawable} on top of its
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
 * some offsets). Call {@link #setBadgeGravity(int)} to change it to {@link #TOP_START}, the other
 * supported mode. To adjust the badge's offsets w.r.t. the anchor's center, use {@link
 * BadgeDrawable#setHorizontalOffset(int)}, {@link BadgeDrawable#setVerticalOffset(int)}
 *
 * <p>Note: This is still under development and may not support the full range of customization
 * Material Android components generally support (e.g. themed attributes).
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/BadgeDrawable.md">component
 * developer guidance</a> and <a href="https://material.io/components/badges/overview">design
 * guidelines</a>.
 */
@OptIn(markerClass = com.google.android.material.badge.ExperimentalBadgeUtils.class)
public class BadgeDrawable extends Drawable implements TextDrawableDelegate {

  private static final String TAG = "Badge";

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

  /**
   * The badge is positioned along the bottom and end edges of its anchor view
   *
   * @deprecated Bottom badge gravities are deprecated in favor of top gravities; use {@link
   *     #TOP_START} or {@link #TOP_END} instead.
   */
  @Deprecated public static final int BOTTOM_END = Gravity.BOTTOM | Gravity.END;

  /**
   * The badge is positioned along the bottom and start edges of its anchor view
   *
   * @deprecated Bottom badge gravities are deprecated in favor of top gravities; use {@link
   *     #TOP_START} or {@link #TOP_END} instead.
   */
  @Deprecated public static final int BOTTOM_START = Gravity.BOTTOM | Gravity.START;

  @StyleRes private static final int DEFAULT_STYLE = R.style.Widget_MaterialComponents_Badge;
  @AttrRes private static final int DEFAULT_THEME_ATTR = R.attr.badgeStyle;

  /**
   * If the badge number exceeds the maximum allowed number, append this suffix to the max badge
   * number and display it as the badge text instead.
   */
  static final String DEFAULT_EXCEED_MAX_BADGE_NUMBER_SUFFIX = "+";

  /**
   * If the badge string exceeds the maximum allowed number of characters, append this suffix to the
   * truncated badge text and display it as the badge text instead.
   */
  static final String DEFAULT_EXCEED_MAX_BADGE_TEXT_SUFFIX = "\u2026";

  /**
   * The badge offset begins at the edge of the anchor.
   */
  static final int OFFSET_ALIGNMENT_MODE_EDGE = 0;

  /**
   * Follows the legacy offset alignment behavior. The horizontal offset begins at a variable
   * permanent inset from the edge of the anchor, and the vertical offset begins at the center
   * of the badge aligned with the edge of the anchor.
   */
  static final int OFFSET_ALIGNMENT_MODE_LEGACY = 1;

  /**
   * Determines where the badge offsets begin in reference to the anchor.
   *
   * @hide
   */
  @IntDef({OFFSET_ALIGNMENT_MODE_EDGE, OFFSET_ALIGNMENT_MODE_LEGACY})
  @Retention(RetentionPolicy.SOURCE)
  @interface OffsetAlignmentMode {}

  /**
   * The badge's edge is fixed at the start and grows towards the end.
   */
  public static final int BADGE_FIXED_EDGE_START = 0;

  /**
   * The badge's edge is fixed at the end and grows towards the start.
   */
  public static final int BADGE_FIXED_EDGE_END = 1;

  /**
   * Determines which edge of the badge is fixed, and which direction it grows towards.
   *
   * @hide
   */
  @IntDef({BADGE_FIXED_EDGE_START, BADGE_FIXED_EDGE_END})
  @Retention(RetentionPolicy.SOURCE)
  @interface BadgeFixedEdge {}

  /** A value to indicate that a badge radius has not been specified. */
  static final int BADGE_RADIUS_NOT_SPECIFIED = -1;

  /** A value to indicate that badge content should not be truncated. */
  public static final int BADGE_CONTENT_NOT_TRUNCATED = -2;

  /** The font scale threshold to changing the vertical offset of the badge. **/
  private static final float FONT_SCALE_THRESHOLD = .3F;

  @NonNull private final WeakReference<Context> contextRef;
  @NonNull private final MaterialShapeDrawable shapeDrawable;
  @NonNull private final TextDrawableHelper textDrawableHelper;
  @NonNull private final Rect badgeBounds;

  @NonNull private final BadgeState state;

  private float badgeCenterX;
  private float badgeCenterY;
  private int maxBadgeNumber;
  private float cornerRadius;
  private float halfBadgeWidth;
  private float halfBadgeHeight;

  // Need to keep a local reference in order to support updating badge gravity.
  @Nullable private WeakReference<View> anchorViewRef;
  @Nullable private WeakReference<FrameLayout> customBadgeParentRef;

  @NonNull
  BadgeState.State getSavedState() {
    return state.getOverridingState();
  }

  /** Creates an instance of {@code BadgeDrawable} with the provided {@link BadgeState.State}. */
  @NonNull
  static BadgeDrawable createFromSavedState(
      @NonNull Context context, @NonNull BadgeState.State savedState) {
    return new BadgeDrawable(context, 0, DEFAULT_THEME_ATTR, DEFAULT_STYLE, savedState);
  }

  /** Creates an instance of {@code BadgeDrawable} with default values. */
  @NonNull
  public static BadgeDrawable create(@NonNull Context context) {
    return new BadgeDrawable(context, 0, DEFAULT_THEME_ATTR, DEFAULT_STYLE, null);
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
    return new BadgeDrawable(context, id, DEFAULT_THEME_ATTR, DEFAULT_STYLE, null);
  }

  /**
   * Convenience wrapper method for {@link Drawable#setVisible(boolean, boolean)} with the {@code
   * restart} parameter hardcoded to false.
   */
  public void setVisible(boolean visible) {
    state.setVisible(visible);
    onVisibilityUpdated();
  }

  private void onVisibilityUpdated() {
    boolean visible = state.isVisible();
    setVisible(visible, /* restart= */ false);
  }

  /**
   * Sets this badge's fixed edge. The badge does not grow in the direction of the fixed edge.
   *
   * @param fixedEdge Constant representing a {@link BadgeFixedEdge} value. The two options are
   *     {@link #BADGE_FIXED_EDGE_START} and {@link #BADGE_FIXED_EDGE_END}.
   */
  public void setBadgeFixedEdge(@BadgeFixedEdge int fixedEdge) {
    if (state.badgeFixedEdge != fixedEdge) {
      state.badgeFixedEdge = fixedEdge;
      updateCenterAndBounds();
    }
  }

  private void restoreState() {
    onBadgeShapeAppearanceUpdated();
    onBadgeTextAppearanceUpdated();

    onMaxBadgeLengthUpdated();

    onBadgeContentUpdated();
    onAlphaUpdated();
    onBackgroundColorUpdated();
    onBadgeTextColorUpdated();
    onBadgeGravityUpdated();

    updateCenterAndBounds();
    onVisibilityUpdated();
  }

  private BadgeDrawable(
      @NonNull Context context,
      @XmlRes int badgeResId,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes,
      @Nullable BadgeState.State savedState) {
    this.contextRef = new WeakReference<>(context);
    ThemeEnforcement.checkMaterialTheme(context);
    badgeBounds = new Rect();

    textDrawableHelper = new TextDrawableHelper(/* delegate= */ this);
    textDrawableHelper.getTextPaint().setTextAlign(Paint.Align.CENTER);


    this.state = new BadgeState(context, badgeResId, defStyleAttr, defStyleRes, savedState);
    shapeDrawable =
        new MaterialShapeDrawable(
            ShapeAppearanceModel.builder(
                    context,
                    hasBadgeContent()
                        ? state.getBadgeWithTextShapeAppearanceResId()
                        : state.getBadgeShapeAppearanceResId(),
                    hasBadgeContent()
                        ? state.getBadgeWithTextShapeAppearanceOverlayResId()
                        : state.getBadgeShapeAppearanceOverlayResId())
                .build());
    restoreState();
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
    if (!(customBadgeParent instanceof FrameLayout)) {
      throw new IllegalArgumentException("customBadgeParent must be a FrameLayout");
    }
    updateBadgeCoordinates(anchorView, (FrameLayout) customBadgeParent);
  }

  /**
   * Calculates and updates this badge's center coordinates based on its anchor's bounds. Internally
   * also updates this {@code BadgeDrawable BadgeDrawable's} bounds, because they are dependent on
   * the center coordinates.
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
   * @param anchorView This badge's anchor.
   * @param customBadgeParent An optional parent view that will set this {@code BadgeDrawable} as
   *     its foreground.
   */
  public void updateBadgeCoordinates(
      @NonNull View anchorView, @Nullable FrameLayout customBadgeParent) {
    this.anchorViewRef = new WeakReference<>(anchorView);
    this.customBadgeParentRef = new WeakReference<>(customBadgeParent);

    updateAnchorParentToNotClip(anchorView);
    updateCenterAndBounds();
    invalidateSelf();
  }

  /** Returns a {@link FrameLayout} that will set this {@code BadgeDrawable} as its foreground. */
  @Nullable
  public FrameLayout getCustomBadgeParent() {
    return customBadgeParentRef != null ? customBadgeParentRef.get() : null;
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
    state.setBackgroundColor(backgroundColor);
    onBackgroundColorUpdated();
  }

  private void onBackgroundColorUpdated() {
    ColorStateList backgroundColorStateList = ColorStateList.valueOf(state.getBackgroundColor());
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
      state.setBadgeTextColor(badgeTextColor);
      onBadgeTextColorUpdated();
    }
  }

  private void onBadgeTextColorUpdated() {
    textDrawableHelper.getTextPaint().setColor(state.getBadgeTextColor());
    invalidateSelf();
  }

  /** Returns the {@link Locale} used to show badge numbers. */
  @NonNull
  public Locale getBadgeNumberLocale() {
    return state.getNumberLocale();
  }

  /** Sets the {@link Locale} used to show badge numbers. */
  public void setBadgeNumberLocale(@NonNull Locale locale) {
    if (!locale.equals(state.getNumberLocale())) {
      state.setNumberLocale(locale);
      invalidateSelf();
    }
  }

  /** Returns whether this badge will display a number. */
  public boolean hasNumber() {
    return !state.hasText() && state.hasNumber();
  }

  /**
   * Returns the badge's number. Only non-negative integer numbers will be returned because the
   * setter clamps negative values to 0.
   *
   * <p>WARNING: Do not call this method if you are planning to compare to BADGE_NUMBER_NONE
   *
   * @see #setNumber(int)
   * @attr ref com.google.android.material.R.styleable#Badge_number
   */
  public int getNumber() {
    return state.hasNumber() ? state.getNumber() : 0;
  }

  /**
   * Sets the badge's number. Only non-negative integer numbers are supported. If the number is
   * negative, it will be clamped to 0. The specified value will be displayed, unless its number of
   * digits exceeds {@code maxCharacterCount} in which case a truncated version will be shown.
   *
   * @param number This badge's number.
   * @attr ref com.google.android.material.R.styleable#Badge_number
   */
  public void setNumber(int number) {
    number = Math.max(0, number);
    if (this.state.getNumber() != number) {
      state.setNumber(number);
      onNumberUpdated();
    }
  }

  /** Clears the badge's number. */
  public void clearNumber() {
    if (state.hasNumber()) {
      state.clearNumber();
      onNumberUpdated();
    }
  }

  private void onNumberUpdated() {
    // The text has priority over the number so when the number changes, the badge is updated
    // only if there is no text.
    if (!hasText()) {
      onBadgeContentUpdated();
    }
  }

  /** Returns whether the badge will display a text. */
  public boolean hasText() {
    return state.hasText();
  }

  /**
   * Returns the badge's text.
   *
   * @see #setText(String)
   * @attr ref com.google.android.material.R.styleable#Badge_badgeText
   */
  @Nullable
  public String getText() {
    return state.getText();
  }

  /**
   * Sets the badge's text. The specified text will be displayed, unless its length exceeds {@code
   * maxCharacterCount} in which case a truncated version will be shown.
   *
   * @see #getText()
   * @attr ref com.google.android.material.R.styleable#Badge_badgeText
   */
  public void setText(@Nullable String text) {
    if (!TextUtils.equals(state.getText(), text)) {
      state.setText(text);
      onTextUpdated();
    }
  }

  /**
   * Clears the badge's text.
   */
  public void clearText() {
    if (state.hasText()) {
      state.clearText();
      onTextUpdated();
    }
  }

  private void onTextUpdated() {
    // The text has priority over the number so any text change updates the badge content.
    onBadgeContentUpdated();
  }

  /**
   * Returns this badge's max character count.
   *
   * @see #setMaxCharacterCount(int)
   * @attr ref com.google.android.material.R.styleable#Badge_maxCharacterCount
   */
  public int getMaxCharacterCount() {
    return state.getMaxCharacterCount();
  }

  /**
   * Sets this badge's max character count.
   *
   * @param maxCharacterCount This badge's max character count.
   * @attr ref com.google.android.material.R.styleable#Badge_maxCharacterCount
   */
  public void setMaxCharacterCount(int maxCharacterCount) {
    if (this.state.getMaxCharacterCount() != maxCharacterCount) {
      this.state.setMaxCharacterCount(maxCharacterCount);
      onMaxBadgeLengthUpdated();
    }
  }

  /**
   * Returns this badge's max number. If maxCharacterCount is set, it will override this number.
   *
   * @see #setMaxNumber(int)
   * @attr ref com.google.android.material.R.styleable#Badge_maxNumber
   */
  public int getMaxNumber() {
    return state.getMaxNumber();
  }

  /**
   * Sets this badge's max number. If maxCharacterCount is set, it will override this number.
   *
   * @param maxNumber This badge's max number.
   * @attr ref com.google.android.material.R.styleable#Badge_maxNumber
   */
  public void setMaxNumber(int maxNumber) {
    if (this.state.getMaxNumber() != maxNumber) {
      this.state.setMaxNumber(maxNumber);
      onMaxBadgeLengthUpdated();
    }
  }

  private void onMaxBadgeLengthUpdated() {
    updateMaxBadgeNumber();
    textDrawableHelper.setTextSizeDirty(true);
    updateCenterAndBounds();
    invalidateSelf();
  }

  @BadgeGravity
  public int getBadgeGravity() {
    return state.getBadgeGravity();
  }

  /**
   * Sets this badge's gravity with respect to its anchor view.
   *
   * @param gravity Constant representing one of the possible {@link BadgeGravity} values. There are
   *     two recommended gravities: {@link #TOP_START} and {@link #TOP_END}.
   */
  public void setBadgeGravity(@BadgeGravity int gravity) {
    if (gravity == BOTTOM_START || gravity == BOTTOM_END) {
      Log.w(TAG, "Bottom badge gravities are deprecated; please use a top gravity instead.");
    }
    if (state.getBadgeGravity() != gravity) {
      state.setBadgeGravity(gravity);
      onBadgeGravityUpdated();
    }
  }

  private void onBadgeGravityUpdated() {
    if (anchorViewRef != null && anchorViewRef.get() != null) {
      updateBadgeCoordinates(
          anchorViewRef.get(), customBadgeParentRef != null ? customBadgeParentRef.get() : null);
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
    return state.getAlpha();
  }

  @Override
  public void setAlpha(int alpha) {
    state.setAlpha(alpha);
    onAlphaUpdated();
  }

  private void onAlphaUpdated() {
    textDrawableHelper.getTextPaint().setAlpha(getAlpha());
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
    if (hasBadgeContent()) {
      drawBadgeContent(canvas);
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

  /**
   * Specifies the content description if the text is set for the badge. If the text is set for the
   * badge and the content description is {@code null}, the badge text will be used as the content
   * description by default.
   */
  public void setContentDescriptionForText(@Nullable CharSequence charSequence) {
    state.setContentDescriptionForText(charSequence);
  }

  /**
   * Specifies the content description if no text or number is set for the badge.
   */
  public void setContentDescriptionNumberless(CharSequence charSequence) {
    state.setContentDescriptionNumberless(charSequence);
  }

  /**
   * Specifies the content description if the number is set for the badge.
   */
  public void setContentDescriptionQuantityStringsResource(@PluralsRes int stringsResource) {
    state.setContentDescriptionQuantityStringsResource(stringsResource);
  }

  /**
   * Specifies the content description if the badge number exceeds the maximum value.
   */
  public void setContentDescriptionExceedsMaxBadgeNumberStringResource(
      @StringRes int stringsResource) {
    state.setContentDescriptionExceedsMaxBadgeNumberStringResource(stringsResource);
  }

  @Nullable
  public CharSequence getContentDescription() {
    if (!isVisible()) {
      return null;
    }
    if (hasText()) {
      return getTextContentDescription();
    } else if (hasNumber()) {
      return getNumberContentDescription();
    } else {
      return getEmptyContentDescription();
    }
  }

  @Nullable
  private String getNumberContentDescription() {
    if (state.getContentDescriptionQuantityStrings() != 0) {
      Context context = contextRef.get();
      if (context == null) {
        return null;
      }
      if (maxBadgeNumber == BADGE_CONTENT_NOT_TRUNCATED || getNumber() <= maxBadgeNumber) {
        return context
            .getResources()
            .getQuantityString(
                state.getContentDescriptionQuantityStrings(), getNumber(), getNumber());
      } else {
        return context.getString(
            state.getContentDescriptionExceedsMaxBadgeNumberStringResource(), maxBadgeNumber);
      }
    }
    return null;
  }

  @Nullable
  private CharSequence getTextContentDescription() {
    final CharSequence contentDescription = state.getContentDescriptionForText();
    if (contentDescription != null) {
      return contentDescription;
    } else {
      return getText();
    }
  }

  private CharSequence getEmptyContentDescription() {
    return state.getContentDescriptionNumberless();
  }

  /**
   * Sets how much (in pixels) horizontal padding to add to the badge when it has label contents.
   * Note that badges have a minimum width as specified by
   * com.google.android.material.R.styleable#Badge_badgeWidth.
   *
   * @param horizontalPadding badge's horizontal padding
   * @attr ref com.google.android.material.R.styleable#Badge_badgeWidePadding
   */
  public void setHorizontalPadding(@Px int horizontalPadding) {
    if (horizontalPadding != state.getBadgeHorizontalPadding()) {
      state.setBadgeHorizontalPadding(horizontalPadding);
      updateCenterAndBounds();
    }
  }

  /** Returns the badge horizontal padding in pixels. */
  @Px
  public int getHorizontalPadding() {
    return state.getBadgeHorizontalPadding();
  }

  /**
   * Sets how much (in pixels) vertical padding to add to the badge when it has label contents. Note
   * that badges have a minimum height as specified by
   * com.google.android.material.R.styleable#Badge_badgeHeight.
   *
   * @param verticalPadding badge's vertical padding
   * @attr ref com.google.android.material.R.styleable#Badge_badgeVerticalPadding
   */
  public void setVerticalPadding(@Px int verticalPadding) {
    if (verticalPadding != state.getBadgeVerticalPadding()) {
      state.setBadgeVerticalPadding(verticalPadding);
      updateCenterAndBounds();
    }
  }

  /** Returns the badge vertical padding in pixels. */
  @Px
  public int getVerticalPadding() {
    return state.getBadgeVerticalPadding();
  }

  /**
   * Sets how much (in pixels) to horizontally move this badge towards the center of its anchor.
   *
   * <p>This sets the horizontal offset for badges without text (dots) and with text.
   *
   * @param px badge's horizontal offset
   */
  public void setHorizontalOffset(int px) {
    setHorizontalOffsetWithoutText(px);
    setHorizontalOffsetWithText(px);
  }

  /**
   * Returns how much (in pixels) this badge is being horizontally offset towards the center of its
   * anchor.
   *
   * <p>This returns the horizontal offset for badges without text. If offset for badges with text
   * and without text are different consider using {@link #getHorizontalOffsetWithoutText} or {@link
   * #getHorizontalOffsetWithText}.
   */
  public int getHorizontalOffset() {
    return state.getHorizontalOffsetWithoutText();
  }

  /**
   * Sets how much (in pixels) to horizontally move this badge towards the center of its anchor when
   * this badge does not have text (is a dot).
   *
   * @param px badge's horizontal offset when the badge does not have text
   */
  public void setHorizontalOffsetWithoutText(@Px int px) {
    state.setHorizontalOffsetWithoutText(px);
    updateCenterAndBounds();
  }

  /**
   * Returns how much (in pixels) this badge is being horizontally offset towards the center of its
   * anchor when this badge does not have text (is a dot).
   */
  @Px
  public int getHorizontalOffsetWithoutText() {
    return state.getHorizontalOffsetWithoutText();
  }

  /**
   * Sets how much (in pixels) to horizontally move this badge towards the center of its anchor when
   * this badge has text.
   *
   * @param px badge's horizontal offset when the badge has text.
   */
  public void setHorizontalOffsetWithText(@Px int px) {
    state.setHorizontalOffsetWithText(px);
    updateCenterAndBounds();
  }

  /**
   * Returns how much (in pixels) this badge is being horizontally offset towards the center of its
   * anchor when this badge has text.
   */
  @Px
  public int getHorizontalOffsetWithText() {
    return state.getHorizontalOffsetWithText();
  }

  /**
   * Sets how much (in pixels) more (in addition to {@code savedState.horizontalOffset}) to
   * horizontally move this badge towards the center of its anchor. Currently used to adjust the
   * placement of badges on toolbar items.
   */
  void setAdditionalHorizontalOffset(int px) {
    state.setAdditionalHorizontalOffset(px);
    updateCenterAndBounds();
  }

  int getAdditionalHorizontalOffset() {
    return state.getAdditionalHorizontalOffset();
  }

  /**
   * Sets how much (in pixels) to vertically move this badge towards the center of its anchor.
   *
   * <p>This sets the vertical offset for badges both without text (dots) and with text.
   *
   * @param px badge's vertical offset
   */
  public void setVerticalOffset(int px) {
    setVerticalOffsetWithoutText(px);
    setVerticalOffsetWithText(px);
  }

  /**
   * Returns how much (in pixels) this badge is being vertically moved towards the center of its
   * anchor.
   *
   * <p>This returns the vertical offset for badges without text. If offset for badges with text and
   * without text are different consider using {@link #getVerticalOffsetWithoutText} or {@link
   * #getVerticalOffsetWithText}.
   */
  public int getVerticalOffset() {
    return state.getVerticalOffsetWithoutText();
  }

  /**
   * Sets how much (in pixels) to vertically move this badge towards the center of its anchor when
   * this badge does not have text (is a dot).
   *
   * @param px badge's vertical offset when the badge does not have text
   */
  public void setVerticalOffsetWithoutText(@Px int px) {
    state.setVerticalOffsetWithoutText(px);
    updateCenterAndBounds();
  }

  /**
   * Returns how much (in pixels) this badge is being vertically offset towards the center of its
   * anchor when this badge does not have text (is a dot).
   */
  @Px
  public int getVerticalOffsetWithoutText() {
    return state.getVerticalOffsetWithoutText();
  }

  /**
   * Sets how much (in pixels) to vertically move this badge towards the center of its anchor when
   * this badge has text.
   *
   * @param px badge's vertical offset when the badge has text.
   */
  public void setVerticalOffsetWithText(@Px int px) {
    state.setVerticalOffsetWithText(px);
    updateCenterAndBounds();
  }

  /**
   * Returns how much (in pixels) this badge is being vertically moved towards the center of its
   * anchor when the badge has text.
   */
  @Px
  public int getVerticalOffsetWithText() {
    return state.getVerticalOffsetWithText();
  }

  /**
   * Sets how much (in pixels) to vertically move this badge away the center of its anchor when this
   * badge has text and the font scale is at max size. This is in conjunction with the vertical
   * offset with text.
   *
   * @param px how much to move the badge's vertical offset away from the center by when the font is
   *     large.
   */
  public void setLargeFontVerticalOffsetAdjustment(@Px int px) {
    state.setLargeFontVerticalOffsetAdjustment(px);
    updateCenterAndBounds();
  }

  /**
   * Returns how much (in pixels) this badge is being vertically moved away the center of its
   * anchor when the badge has text and the font scale is at max. Note that this is not the total
   * vertical offset.
   */
  @Px
  public int getLargeFontVerticalOffsetAdjustment() {
    return state.getLargeFontVerticalOffsetAdjustment();
  }

  /**
   * Sets how much (in pixels) more (in addition to {@code savedState.verticalOffset}) to vertically
   * move this badge towards the center of its anchor. Currently used to adjust the placement of
   * badges on toolbar items.
   */
  void setAdditionalVerticalOffset(@Px int px) {
    state.setAdditionalVerticalOffset(px);
    updateCenterAndBounds();
  }

  @Px
  int getAdditionalVerticalOffset() {
    return state.getAdditionalVerticalOffset();
  }

  /**
   * Sets whether or not to auto adjust the badge placement to within the badge anchor's grandparent
   * view.
   *
   * @param autoAdjustToWithinGrandparentBounds whether or not to auto adjust to within the anchor's
   * grandparent view.
   * @deprecated Badges now automatically adjust their bounds within the first ancestor view that *
   * clips its children.
   */
  @Deprecated
  public void setAutoAdjustToWithinGrandparentBounds(boolean autoAdjustToWithinGrandparentBounds) {
    if (state.isAutoAdjustedToGrandparentBounds() == autoAdjustToWithinGrandparentBounds) {
      return;
    }
    state.setAutoAdjustToGrandparentBounds(autoAdjustToWithinGrandparentBounds);
    if (anchorViewRef != null && anchorViewRef.get() != null) {
      autoAdjustWithinGrandparentBounds(anchorViewRef.get());
    }
  }

  /**
   * Sets this badge's text appearance resource.
   *
   * @param id This badge's text appearance res id.
   * @attr ref com.google.android.material.R.styleable#Badge_badgeTextAppearance
   */
  public void setTextAppearance(@StyleRes int id) {
    state.setTextAppearanceResId(id);
    onBadgeTextAppearanceUpdated();
  }

  private void onBadgeTextAppearanceUpdated() {
    Context context = contextRef.get();
    if (context == null) {
      return;
    }
    TextAppearance textAppearance = new TextAppearance(context, state.getTextAppearanceResId());
    if (textDrawableHelper.getTextAppearance() == textAppearance) {
      return;
    }
    textDrawableHelper.setTextAppearance(textAppearance, context);
    onBadgeTextColorUpdated();
    updateCenterAndBounds();
    invalidateSelf();
  }

  /**
   * Sets this badge without text's shape appearance resource.
   *
   * @param id This badge's shape appearance res id when there is no text.
   * @attr ref com.google.android.material.R.styleable#Badge_badgeShapeAppearance
   */
  public void setBadgeWithoutTextShapeAppearance(@StyleRes int id) {
    state.setBadgeShapeAppearanceResId(id);
    onBadgeShapeAppearanceUpdated();
  }

  /**
   * Sets this badge without text's shape appearance overlay resource.
   *
   * @param id This badge's shape appearance overlay res id when there is no text.
   * @attr ref com.google.android.material.R.styleable#Badge_badgeShapeAppearanceOverlay
   */
  public void setBadgeWithoutTextShapeAppearanceOverlay(@StyleRes int id) {
    state.setBadgeShapeAppearanceOverlayResId(id);
    onBadgeShapeAppearanceUpdated();
  }

  /**
   * Sets this badge with text's shape appearance resource.
   *
   * @param id This badge's shape appearance res id when there is text.
   * @attr ref com.google.android.material.R.styleable#Badge_badgeWithTextShapeAppearance
   */
  public void setBadgeWithTextShapeAppearance(@StyleRes int id) {
    state.setBadgeWithTextShapeAppearanceResId(id);
    onBadgeShapeAppearanceUpdated();
  }

  /**
   * Sets this badge with text's shape appearance overlay resource.
   *
   * @param id This badge's shape appearance overlay res id when there is text.
   * @attr ref com.google.android.material.R.styleable#Badge_badgeWithTextShapeAppearanceOverlay
   */
  public void setBadgeWithTextShapeAppearanceOverlay(@StyleRes int id) {
    state.setBadgeWithTextShapeAppearanceOverlayResId(id);
    onBadgeShapeAppearanceUpdated();
  }

  private void onBadgeShapeAppearanceUpdated() {
    Context context = contextRef.get();
    if (context == null) {
      return;
    }
    shapeDrawable.setShapeAppearanceModel(
        ShapeAppearanceModel.builder(
                context,
                hasBadgeContent()
                    ? state.getBadgeWithTextShapeAppearanceResId()
                    : state.getBadgeShapeAppearanceResId(),
                hasBadgeContent()
                    ? state.getBadgeWithTextShapeAppearanceOverlayResId()
                    : state.getBadgeShapeAppearanceOverlayResId())
            .build());
    invalidateSelf();
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
    if (customBadgeParent != null) {
      // Calculates coordinates relative to the parent.
      customBadgeParent.offsetDescendantRectToMyCoords(anchorView, anchorRect);
    }

    calculateCenterAndBounds(anchorRect, anchorView);

    updateBadgeBounds(badgeBounds, badgeCenterX, badgeCenterY, halfBadgeWidth, halfBadgeHeight);

    // If there is a badge radius specified, override the corner size set by the shape appearance
    // with the badge radius.
    if (cornerRadius != BADGE_RADIUS_NOT_SPECIFIED) {
      shapeDrawable.setCornerSize(cornerRadius);
    }
    if (!tmpRect.equals(badgeBounds)) {
      shapeDrawable.setBounds(badgeBounds);
    }
  }

  private int getTotalVerticalOffsetForState() {
    int vOffset = state.getVerticalOffsetWithoutText();
    if (hasBadgeContent()) {
      vOffset = state.getVerticalOffsetWithText();
      Context context = contextRef.get();
      if (context != null) {
        float progress =
            AnimationUtils.lerp(0F, 1F,
                FONT_SCALE_THRESHOLD, 1F, MaterialResources.getFontScale(context) - 1F);
        vOffset =
            AnimationUtils.lerp(
                vOffset, vOffset - state.getLargeFontVerticalOffsetAdjustment(), progress);
      }
    }



    // If the offset alignment mode is at the edge of the anchor, we want to move the badge
    // so that its origin is at the edge.
    if (state.offsetAlignmentMode == OFFSET_ALIGNMENT_MODE_EDGE) {
      vOffset -= Math.round(halfBadgeHeight);
    }
    return vOffset + state.getAdditionalVerticalOffset();
  }

  private int getTotalHorizontalOffsetForState() {
    int hOffset =
        hasBadgeContent()
            ? state.getHorizontalOffsetWithText()
            : state.getHorizontalOffsetWithoutText();
    // If the offset alignment mode is legacy, then we want to add the legacy inset to the offset.
    if (state.offsetAlignmentMode == OFFSET_ALIGNMENT_MODE_LEGACY) {
      hOffset += hasBadgeContent() ? state.horizontalInsetWithText : state.horizontalInset;
    }
    return hOffset + state.getAdditionalHorizontalOffset();
  }

  private void calculateCenterAndBounds(@NonNull Rect anchorRect, @NonNull View anchorView) {
    cornerRadius = hasBadgeContent() ? state.badgeWithTextRadius : state.badgeRadius;
    if (cornerRadius != BADGE_RADIUS_NOT_SPECIFIED) {
      halfBadgeWidth = cornerRadius;
      halfBadgeHeight = cornerRadius;
    } else {
      halfBadgeWidth =
          Math.round(hasBadgeContent() ? state.badgeWithTextWidth / 2 : state.badgeWidth / 2);
      halfBadgeHeight =
          Math.round(hasBadgeContent() ? state.badgeWithTextHeight / 2 : state.badgeHeight / 2);
    }

    // If the badge has a number, we want to make sure that the badge is at least tall/wide
    // enough to encompass the text with padding.
    if (hasBadgeContent()) {
      String badgeContent = getBadgeContent();

      halfBadgeWidth =
          Math.max(
              halfBadgeWidth,
              textDrawableHelper.getTextWidth(badgeContent) / 2f
                  + state.getBadgeHorizontalPadding());

      halfBadgeHeight =
          Math.max(
              halfBadgeHeight,
              textDrawableHelper.getTextHeight(badgeContent) / 2f
                  + state.getBadgeVerticalPadding());

      // If the badge has text, it should at least have the same width as it does height
      halfBadgeWidth = Math.max(halfBadgeWidth, halfBadgeHeight);
    }

    int totalVerticalOffset = getTotalVerticalOffsetForState();

    switch (state.getBadgeGravity()) {
      case BOTTOM_END:
      case BOTTOM_START:
        badgeCenterY = anchorRect.bottom - totalVerticalOffset;
        break;
      case TOP_END:
      case TOP_START:
      default:
        badgeCenterY = anchorRect.top + totalVerticalOffset;
        break;
    }

    int totalHorizontalOffset = getTotalHorizontalOffsetForState();

    // Update the centerX based on the badge width and offset from start or end boundary of anchor.
    switch (state.getBadgeGravity()) {
      case BOTTOM_START:
      case TOP_START:
        badgeCenterX = state.badgeFixedEdge == BADGE_FIXED_EDGE_START
            ? (anchorView.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR
              ? anchorRect.left + halfBadgeWidth - (halfBadgeHeight * 2 - totalHorizontalOffset)
              : anchorRect.right - halfBadgeWidth + (halfBadgeHeight * 2 - totalHorizontalOffset))
            : (anchorView.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR
                ? anchorRect.left - halfBadgeWidth + totalHorizontalOffset
                : anchorRect.right + halfBadgeWidth - totalHorizontalOffset);
        break;
      case BOTTOM_END:
      case TOP_END:
      default:
        badgeCenterX = state.badgeFixedEdge == BADGE_FIXED_EDGE_START
            ? (anchorView.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR
                ? anchorRect.right + halfBadgeWidth - totalHorizontalOffset
                : anchorRect.left - halfBadgeWidth + totalHorizontalOffset)
            : (anchorView.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR
                ? anchorRect.right - halfBadgeWidth + (halfBadgeHeight * 2 - totalHorizontalOffset)
                : anchorRect.left + halfBadgeWidth - (halfBadgeHeight * 2 - totalHorizontalOffset));
        break;
    }

    if (state.isAutoAdjustedToGrandparentBounds()) {
      autoAdjustWithinGrandparentBounds(anchorView);
    } else {
      autoAdjustWithinViewBounds(anchorView, null);
    }
  }

  /**
   * Adjust the badge placement so it is within the specified ancestor view. If {@code ancestorView}
   * is null, it will default to adjusting to the first ancestor of {@code anchorView} that clips
   * its children.
   */
  private void autoAdjustWithinViewBounds(@NonNull View anchorView, @Nullable View ancestorView) {
    // The top of the badge may be cut off by the anchor view's ancestor view if clipChildren is
    // false (eg. in the case of the bottom navigation bar). If that is the case, we should adjust
    // the position of the badge.

    float totalAnchorYOffset;
    float totalAnchorXOffset;
    ViewParent anchorParent;
    // If there is a custom badge parent, we should use its coordinates instead of the anchor
    // view's parent.
    ViewParent customAnchorParent = getCustomBadgeParent();
    if (customAnchorParent == null) {
      totalAnchorYOffset = anchorView.getY();
      totalAnchorXOffset = anchorView.getX();
      anchorParent = anchorView.getParent();
    } else {
      totalAnchorYOffset = 0;
      totalAnchorXOffset = 0;
      anchorParent = customAnchorParent;
    }

    ViewParent currentViewParent = anchorParent;
    while (currentViewParent instanceof View && currentViewParent != ancestorView) {
      ViewParent viewGrandparent = currentViewParent.getParent();
      if (!(viewGrandparent instanceof ViewGroup)
          || ((ViewGroup) viewGrandparent).getClipChildren()) {
        break;
      }
      View currentViewGroup = (View) currentViewParent;
      totalAnchorYOffset += currentViewGroup.getY();
      totalAnchorXOffset += currentViewGroup.getX();
      currentViewParent = currentViewParent.getParent();
    }

    // If currentViewParent is not a View, all ancestor Views did not clip their children
    if (!(currentViewParent instanceof View)) {
      return;
    }

    float topCutOff = getTopCutOff(totalAnchorYOffset);
    float leftCutOff = getLeftCutOff(totalAnchorXOffset);
    float bottomCutOff =
        getBottomCutOff(((View) currentViewParent).getHeight(), totalAnchorYOffset);
    float rightCutOff = getRightCutoff(((View) currentViewParent).getWidth(), totalAnchorXOffset);

    // If there's any part of the badge that is cut off, we move the badge accordingly.
    if (topCutOff < 0) {
      badgeCenterY += Math.abs(topCutOff);
    }
    if (leftCutOff < 0) {
      badgeCenterX += Math.abs(leftCutOff);
    }
    if (bottomCutOff > 0) {
      badgeCenterY -= Math.abs(bottomCutOff);
    }
    if (rightCutOff > 0) {
      badgeCenterX -= Math.abs(rightCutOff);
    }
  }

  /** Adjust the badge placement so it is within its anchor's grandparent view. */
  private void autoAdjustWithinGrandparentBounds(@NonNull View anchorView) {
    // If there is a custom badge parent, we should use its coordinates instead of the anchor
    // view's parent.
    ViewParent customAnchor = getCustomBadgeParent();
    ViewParent anchorParent = null;
    if (customAnchor == null) {
      anchorParent = anchorView.getParent();
    } else {
      anchorParent = customAnchor;
    }
    if (anchorParent instanceof View && anchorParent.getParent() instanceof View) {
      autoAdjustWithinViewBounds(anchorView, (View) anchorParent.getParent());
    }
  }

  /**
   * Returns where the badge is relative to the top bound of the anchor's ancestor view. If the
   * value is negative, it is beyond the bounds of the anchor's ancestor view.
   *
   * @param totalAnchorYOffset the total X offset of the anchor in relation to the ancestor view it
   *     is adjusting its bounds to
   */
  private float getTopCutOff(float totalAnchorYOffset) {
    return badgeCenterY - halfBadgeHeight + totalAnchorYOffset;
  }

  /**
   * Returns where the badge is relative to the left bound of the anchor's ancestor view. If the
   * value is negative, it is beyond the bounds of the anchor's ancestor view.
   *
   * @param totalAnchorXOffset the total X offset of the anchor in relation to the ancestor view it
   *     is adjusting its bounds to
   */
  private float getLeftCutOff(float totalAnchorXOffset) {
    return badgeCenterX - halfBadgeWidth + totalAnchorXOffset;
  }

  /**
   * Returns where the badge is relative to the bottom bound of the anchor's ancestor view. If the
   * value is positive, it is beyond the bounds of the anchor's ancestor view.
   *
   * @param ancestorHeight the height of the ancestor view
   * @param totalAnchorYOffset the total Y offset of the anchor in relation to the ancestor view it
   *     is adjusting its bounds to
   */
  private float getBottomCutOff(float ancestorHeight, float totalAnchorYOffset) {
    return badgeCenterY + halfBadgeHeight - ancestorHeight + totalAnchorYOffset;
  }

  /**
   * Returns where the badge is relative to the right bound of the anchor's ancestor view. If the
   * value is positive, it is beyond the bounds of the anchor's ancestor view.
   *
   * @param ancestorWidth the width of the ancestor view
   * @param totalAnchorXOffset the total X offset of the anchor in relation to the ancestor view it
   *     is adjusting its bounds to
   */
  private float getRightCutoff(float ancestorWidth, float totalAnchorXOffset) {
    return badgeCenterX + halfBadgeWidth - ancestorWidth + totalAnchorXOffset;
  }

  private void drawBadgeContent(Canvas canvas) {
    String badgeContent = getBadgeContent();
    if (badgeContent != null) {
      Rect textBounds = new Rect();
      textDrawableHelper
          .getTextPaint()
          .getTextBounds(badgeContent, 0, badgeContent.length(), textBounds);

      // The text is centered horizontally using Paint.Align.Center. We calculate the correct
      // y-coordinate ourselves using textbounds.exactCenterY, but this can look askew at low
      // screen densities due to canvas.drawText rounding the coordinates to the nearest integer.
      // To mitigate this, we round the y-coordinate following these rules:
      // If the badge.bottom is <= 0, the text is drawn above its original origin (0,0) so
      // we round down the y-coordinate since we want to keep it above its new origin.
      // If the badge.bottom is positive, we round up for the opposite reason.
      float exactCenterY = badgeCenterY - textBounds.exactCenterY();
      canvas.drawText(
          badgeContent,
          badgeCenterX,
          textBounds.bottom <= 0 ? (int) exactCenterY : Math.round(exactCenterY),
          textDrawableHelper.getTextPaint());
    }
  }

  private boolean hasBadgeContent() {
    return hasText() || hasNumber();
  }

  @Nullable
  private String getBadgeContent() {
    if (hasText()) {
      return getTextBadgeText();
    } else if (hasNumber()) {
      return getNumberBadgeText();
    } else {
      return null;
    }
  }

  @Nullable
  private String getTextBadgeText() {
    String text = getText();
    final int maxCharacterCount = getMaxCharacterCount();
    if (maxCharacterCount == BADGE_CONTENT_NOT_TRUNCATED) {
      return text;
    }

    if (text != null && text.length() > maxCharacterCount) {
      Context context = contextRef.get();
      if (context == null) {
        return "";
      }

      text = text.substring(0, maxCharacterCount - 1);
      return String.format(
          context.getString(R.string.m3_exceed_max_badge_text_suffix),
          text,
          DEFAULT_EXCEED_MAX_BADGE_TEXT_SUFFIX);
    } else {
      return text;
    }
  }

  @NonNull
  private String getNumberBadgeText() {
    // If number exceeds max count, show badgeMaxCount+ instead of the number.
    if (maxBadgeNumber == BADGE_CONTENT_NOT_TRUNCATED || getNumber() <= maxBadgeNumber) {
      return NumberFormat.getInstance(state.getNumberLocale()).format(getNumber());
    } else {
      Context context = contextRef.get();
      if (context == null) {
        return "";
      }

      return String.format(
          state.getNumberLocale(),
          context.getString(R.string.mtrl_exceed_max_badge_number_suffix),
          maxBadgeNumber,
          DEFAULT_EXCEED_MAX_BADGE_NUMBER_SUFFIX);
    }
  }

  private void onBadgeContentUpdated() {
    textDrawableHelper.setTextSizeDirty(true);
    onBadgeShapeAppearanceUpdated();
    updateCenterAndBounds();
    invalidateSelf();
  }

  private void updateMaxBadgeNumber() {
    if (getMaxCharacterCount() != BADGE_CONTENT_NOT_TRUNCATED) {
      // If there exists a max character count, we set the maximum number a badge can have as the
      // largest number that has maxCharCount - 1 digits, which accounts for the `+` as a character.
      maxBadgeNumber = (int) Math.pow(10.0d, (double) getMaxCharacterCount() - 1) - 1;
    } else {
      maxBadgeNumber = getMaxNumber();
    }
  }
}

