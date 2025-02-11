/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.materialswitch;

import com.google.android.material.R;

import static androidx.core.graphics.ColorUtils.blendARGB;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import com.google.android.material.drawable.DrawableUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;

/**
 * A class that creates a Material Themed Switch. This class is intended to provide a brand new
 * Switch design and replace the obsolete
 * {@link com.google.android.material.switchmaterial.SwitchMaterial} class.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/Switch.md">component
 * developer guidance</a> and <a href="https://material.io/components/switch/overview">design
 * guidelines</a>.
 */
public class MaterialSwitch extends SwitchCompat {
  private static final int DEF_STYLE_RES = R.style.Widget_Material3_CompoundButton_MaterialSwitch;
  private static final int[] STATE_SET_WITH_ICON = { R.attr.state_with_icon };

  @Nullable private Drawable thumbDrawable;
  @Nullable private Drawable thumbIconDrawable;
  @Px private int thumbIconSize = DrawableUtils.INTRINSIC_SIZE;

  @Nullable private Drawable trackDrawable;
  @Nullable private Drawable trackDecorationDrawable;

  @Nullable private ColorStateList thumbTintList;
  @Nullable private ColorStateList thumbIconTintList;
  @NonNull private PorterDuff.Mode thumbIconTintMode;
  @Nullable private ColorStateList trackTintList;
  @Nullable private ColorStateList trackDecorationTintList;
  @NonNull private PorterDuff.Mode trackDecorationTintMode;

  private int[] currentStateUnchecked;
  private int[] currentStateChecked;

  public MaterialSwitch(@NonNull Context context) {
    this(context, null);
  }

  public MaterialSwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.materialSwitchStyle);
  }

  public MaterialSwitch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    thumbDrawable = super.getThumbDrawable();
    thumbTintList = super.getThumbTintList();
    super.setThumbTintList(null); // Always use our custom tinting logic

    trackDrawable = super.getTrackDrawable();
    trackTintList = super.getTrackTintList();
    super.setTrackTintList(null); // Always use our custom tinting logic

    TintTypedArray attributes =
        ThemeEnforcement.obtainTintedStyledAttributes(
            context, attrs, R.styleable.MaterialSwitch, defStyleAttr, DEF_STYLE_RES);

    thumbIconDrawable = attributes.getDrawable(R.styleable.MaterialSwitch_thumbIcon);
    thumbIconSize = attributes.getDimensionPixelSize(
        R.styleable.MaterialSwitch_thumbIconSize, DrawableUtils.INTRINSIC_SIZE);

    thumbIconTintList = attributes.getColorStateList(R.styleable.MaterialSwitch_thumbIconTint);
    thumbIconTintMode =
        ViewUtils.parseTintMode(
            attributes.getInt(R.styleable.MaterialSwitch_thumbIconTintMode, -1), Mode.SRC_IN);

    trackDecorationDrawable =
        attributes.getDrawable(R.styleable.MaterialSwitch_trackDecoration);
    trackDecorationTintList =
        attributes.getColorStateList(R.styleable.MaterialSwitch_trackDecorationTint);
    trackDecorationTintMode =
        ViewUtils.parseTintMode(
            attributes.getInt(R.styleable.MaterialSwitch_trackDecorationTintMode, -1), Mode.SRC_IN);

    attributes.recycle();

    setEnforceSwitchWidth(false);

    refreshThumbDrawable();
    refreshTrackDrawable();
  }

  @Override
  public void invalidate() {
    updateDrawableTints();
    super.invalidate();
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

    if (thumbIconDrawable != null) {
      mergeDrawableStates(drawableState, STATE_SET_WITH_ICON);
    }

    currentStateUnchecked = DrawableUtils.getUncheckedState(drawableState);
    currentStateChecked = DrawableUtils.getCheckedState(drawableState);

    return drawableState;
  }

  @Override
  public void setThumbDrawable(@Nullable Drawable drawable) {
    thumbDrawable = drawable;
    refreshThumbDrawable();
  }

  @Override
  @Nullable
  public Drawable getThumbDrawable() {
    return thumbDrawable;
  }

  @Override
  public void setThumbTintList(@Nullable ColorStateList tintList) {
    thumbTintList = tintList;
    refreshThumbDrawable();
  }

  @Override
  @Nullable
  public ColorStateList getThumbTintList() {
    return thumbTintList;
  }

  @Override
  public void setThumbTintMode(@Nullable PorterDuff.Mode tintMode) {
    super.setThumbTintMode(tintMode);
    refreshThumbDrawable();
  }

  /**
   * Sets the drawable used for the thumb icon that will be drawn upon the thumb.
   *
   * @param resId Resource ID of a thumb icon drawable
   *
   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_thumbIcon
   */
  public void setThumbIconResource(@DrawableRes int resId) {
    setThumbIconDrawable(AppCompatResources.getDrawable(getContext(), resId));
  }

  /**
   * Sets the drawable used for the thumb icon that will be drawn upon the thumb.
   *
   * @param icon Thumb icon drawable
   *
   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_thumbIcon
   */
  public void setThumbIconDrawable(@Nullable Drawable icon) {
    thumbIconDrawable = icon;
    refreshThumbDrawable();
  }

  /**
   * Gets the drawable used for the thumb icon that will be drawn upon the thumb.
   *
   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_thumbIcon
   */
  @Nullable
  public Drawable getThumbIconDrawable() {
    return thumbIconDrawable;
  }

  /**
   * Sets the size of the thumb icon.
   *
   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_thumbIconSize
   */
  public void setThumbIconSize(@Px final int size) {
    if (thumbIconSize != size) {
      thumbIconSize = size;
      refreshThumbDrawable();
    }
  }

  /**
   * Returns the size of the thumb icon.
   *
   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_thumbIconSize
   */
  @Px
  public int getThumbIconSize() {
    return thumbIconSize;
  }

  /**
   * Applies a tint to the thumb icon drawable. Does not modify the current
   * tint mode, which is {@link PorterDuff.Mode#SRC_IN} by default.
   * <p>
   * Subsequent calls to {@link #setThumbIconDrawable(Drawable)} will
   * automatically mutate the drawable and apply the specified tint and tint
   * mode using {@link Drawable#setTintList(ColorStateList)}.
   *
   * @param tintList the tint to apply, may be {@code null} to clear tint
   *
   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_thumbIconTint
   */
  public void setThumbIconTintList(@Nullable ColorStateList tintList) {
    thumbIconTintList = tintList;
    refreshThumbDrawable();
  }

  /**
   * Returns the tint applied to the thumb icon drawable
   *
   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_thumbIconTint
   */
  @Nullable
  public ColorStateList getThumbIconTintList() {
    return thumbIconTintList;
  }

  /**
   * Specifies the blending mode used to apply the tint specified by
   * {@link #setThumbIconTintList(ColorStateList)}} to the thumb icon drawable.
   * The default mode is {@link PorterDuff.Mode#SRC_IN}.
   *
   * @param tintMode the blending mode used to apply the tint

   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_thumbIconTintMode
   */
  public void setThumbIconTintMode(@NonNull PorterDuff.Mode tintMode) {
    thumbIconTintMode = tintMode;
    refreshThumbDrawable();
  }

  /**
   * Returns the blending mode used to apply the tint to the thumb icon drawable
   *
   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_thumbIconTintMode
   */
  @NonNull
  public PorterDuff.Mode getThumbIconTintMode() {
    return thumbIconTintMode;
  }

  @Override
  public void setTrackDrawable(@Nullable Drawable track) {
    trackDrawable = track;
    refreshTrackDrawable();
  }

  @Override
  @Nullable
  public Drawable getTrackDrawable() {
    return trackDrawable;
  }

  @Override
  public void setTrackTintList(@Nullable ColorStateList tintList) {
    trackTintList = tintList;
    refreshTrackDrawable();
  }

  @Override
  @Nullable
  public ColorStateList getTrackTintList() {
    return trackTintList;
  }

  @Override
  public void setTrackTintMode(@Nullable PorterDuff.Mode tintMode) {
    super.setTrackTintMode(tintMode);
    refreshTrackDrawable();
  }

  /**
   * Set the drawable used for the track decoration that will be drawn upon the track.
   *
   * @param resId Resource ID of a track decoration drawable
   *
   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_trackDecoration
   */
  public void setTrackDecorationResource(@DrawableRes int resId) {
    setTrackDecorationDrawable(AppCompatResources.getDrawable(getContext(), resId));
  }

  /**
   * Set the drawable used for the track decoration that will be drawn upon the track.
   *
   * @param trackDecoration Track decoration drawable
   *
   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_trackDecoration
   */
  public void setTrackDecorationDrawable(@Nullable Drawable trackDecoration) {
    trackDecorationDrawable = trackDecoration;
    refreshTrackDrawable();
  }

  /**
   * Get the drawable used for the track decoration that will be drawn upon the track.
   *
   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_trackDecoration
   */
  @Nullable
  public Drawable getTrackDecorationDrawable() {
    return trackDecorationDrawable;
  }

  /**
   * Applies a tint to the track decoration drawable. Does not modify the current
   * tint mode, which is {@link PorterDuff.Mode#SRC_IN} by default.
   *
   * <p>Subsequent calls to {@link #setTrackDecorationDrawable(Drawable)} will
   * automatically mutate the drawable and apply the specified tint and tint
   * mode using {@link Drawable#setTintList(ColorStateList)}.
   *
   * @param tintList the tint to apply, may be {@code null} to clear tint
   *
   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_trackDecorationTint
   */
  public void setTrackDecorationTintList(@Nullable ColorStateList tintList) {
    trackDecorationTintList = tintList;
    refreshTrackDrawable();
  }

  /**
   * Returns the tint applied to the track decoration drawable
   *
   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_trackDecorationTint
   */
  @Nullable
  public ColorStateList getTrackDecorationTintList() {
    return trackDecorationTintList;
  }

  /**
   * Specifies the blending mode used to apply the tint specified by
   * {@link #setTrackDecorationTintList(ColorStateList)}} to the track decoration drawable.
   * The default mode is {@link PorterDuff.Mode#SRC_IN}.
   *
   * @param tintMode the blending mode used to apply the tint

   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_trackDecorationTintMode
   */
  public void setTrackDecorationTintMode(@NonNull PorterDuff.Mode tintMode) {
    trackDecorationTintMode = tintMode;
    refreshTrackDrawable();
  }

  /**
   * Returns the blending mode used to apply the tint to the track decoration drawable
   *
   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_trackDecorationTintMode
   */
  @NonNull
  public PorterDuff.Mode getTrackDecorationTintMode() {
    return trackDecorationTintMode;
  }

  private void refreshThumbDrawable() {
    thumbDrawable =
        DrawableUtils.createTintableDrawableIfNeeded(
            thumbDrawable, thumbTintList, getThumbTintMode());
    thumbIconDrawable =
        DrawableUtils.createTintableDrawableIfNeeded(
            thumbIconDrawable, thumbIconTintList, thumbIconTintMode);

    updateDrawableTints();

    super.setThumbDrawable(DrawableUtils.compositeTwoLayeredDrawable(
        thumbDrawable, thumbIconDrawable, thumbIconSize, thumbIconSize));

    refreshDrawableState();
  }

  private void refreshTrackDrawable() {
    trackDrawable =
        DrawableUtils.createTintableDrawableIfNeeded(
            trackDrawable, trackTintList, getTrackTintMode());
    trackDecorationDrawable =
        DrawableUtils.createTintableDrawableIfNeeded(
            trackDecorationDrawable, trackDecorationTintList, trackDecorationTintMode);

    updateDrawableTints();

    Drawable finalTrackDrawable;
    if (trackDrawable != null && trackDecorationDrawable != null) {
      finalTrackDrawable =
          new LayerDrawable(new Drawable[]{ trackDrawable, trackDecorationDrawable});
    } else if (trackDrawable != null) {
      finalTrackDrawable = trackDrawable;
    } else {
      finalTrackDrawable = trackDecorationDrawable;
    }
    if (finalTrackDrawable != null) {
      setSwitchMinWidth(finalTrackDrawable.getIntrinsicWidth());
    }
    super.setTrackDrawable(finalTrackDrawable);
  }

  private void updateDrawableTints() {
    if (thumbTintList == null
        && thumbIconTintList == null
        && trackTintList == null
        && trackDecorationTintList == null) {
      // Early return to avoid heavy operation.
      return;
    }

    float thumbPosition = getThumbPosition();

    if (thumbTintList != null) {
      setInterpolatedDrawableTintIfPossible(
          thumbDrawable, thumbTintList, currentStateUnchecked, currentStateChecked, thumbPosition);
    }

    if (thumbIconTintList != null) {
      setInterpolatedDrawableTintIfPossible(
          thumbIconDrawable,
          thumbIconTintList,
          currentStateUnchecked,
          currentStateChecked,
          thumbPosition);
    }

    if (trackTintList != null) {
      setInterpolatedDrawableTintIfPossible(
          trackDrawable, trackTintList, currentStateUnchecked, currentStateChecked, thumbPosition);
    }

    if (trackDecorationTintList != null) {
      setInterpolatedDrawableTintIfPossible(
          trackDecorationDrawable,
          trackDecorationTintList,
          currentStateUnchecked,
          currentStateChecked,
          thumbPosition);
    }
  }

  /**
   * Tints the given drawable with the interpolated color according to the provided thumb position
   * between unchecked and checked states. The reference color in unchecked and checked states will
   * be retrieved from the given {@link ColorStateList} according to the provided states.
   */
  private static void setInterpolatedDrawableTintIfPossible(
      @Nullable Drawable drawable,
      @Nullable ColorStateList tint,
      @NonNull int[] stateUnchecked,
      @NonNull int[] stateChecked,
      float thumbPosition) {
    if (drawable == null || tint == null) {
      return;
    }

    drawable.setTint(blendARGB(
        tint.getColorForState(stateUnchecked, 0),
        tint.getColorForState(stateChecked, 0),
        thumbPosition));
  }
}
