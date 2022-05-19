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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.graphics.drawable.AnimatedStateListDrawableCompat;
import androidx.appcompat.widget.DrawableUtils;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * A class that creates a Material Themed Switch. This class is intended to provide a brand new
 * Switch design and replace the obsolete
 * {@link com.google.android.material.switchmaterial.SwitchMaterial} class.
 */
public class MaterialSwitch extends SwitchCompat {
  private static final int DEF_STYLE_RES = R.style.Widget_Material3_CompoundButton_MaterialSwitch;

  @NonNull private final SwitchWidth switchWidth = SwitchWidth.create(this);
  @NonNull private final ThumbPosition thumbPosition = new ThumbPosition();

  @Nullable private Drawable trackDrawable;
  @Nullable private Drawable trackDecorationDrawable;

  @Nullable private ColorStateList thumbTintList;
  @Nullable private ColorStateList trackTintList;
  @Nullable private ColorStateList trackDecorationTintList;
  @NonNull private PorterDuff.Mode trackDecorationTintMode;

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

    thumbTintList = super.getThumbTintList();
    super.setThumbTintList(null); // Always use our custom tinting logic

    trackDrawable = super.getTrackDrawable();
    trackTintList = super.getTrackTintList();
    super.setTrackTintList(null); // Always use our custom tinting logic

    TintTypedArray attributes =
        ThemeEnforcement.obtainTintedStyledAttributes(
            context, attrs, R.styleable.MaterialSwitch, defStyleAttr, DEF_STYLE_RES);

    trackDecorationDrawable =
        attributes.getDrawable(R.styleable.MaterialSwitch_trackDecoration);
    trackDecorationTintList =
        attributes.getColorStateList(R.styleable.MaterialSwitch_trackDecorationTint);
    trackDecorationTintMode =
        DrawableUtils.parseTintMode(
            attributes.getInt(R.styleable.MaterialSwitch_trackDecorationTintMode, -1), Mode.SRC_IN);

    attributes.recycle();

    refreshTrackDrawable();
  }

  // TODO(b/227338106): remove this workaround and move to use setEnforceSwitchWidth(false) after
  //                    AppCompat 1.6.0-stable is released.
  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    switchWidth.set(getSwitchMinWidth());
  }

  @Override
  public void invalidate() {
    // ThumbPosition update will trigger invalidate(), update thumb/track tint here.
    if (thumbPosition != null) {
      // This may happen when super classes' constructors call this method.
      updateDrawableTints();
    }
    super.invalidate();
  }

  // TODO(b/227338106): remove this workaround and move to use setEnforceSwitchWidth(false) after
  //                    AppCompat 1.6.0-stable is released.
  @Override
  public int getCompoundPaddingLeft() {
    if (!ViewUtils.isLayoutRtl(this)) {
      return super.getCompoundPaddingLeft();
    }
    // Compound paddings are used during onMeasure() to decide the component width, at that time
    // the switch width is not overridden yet so we need to adjust the value to make measurement
    // right. This can be removed after the workaround is removed.
    return super.getCompoundPaddingLeft() - switchWidth.get() + getSwitchMinWidth();
  }

  // TODO(b/227338106): remove this workaround and move to use setEnforceSwitchWidth(false) after
  //                    AppCompat 1.6.0-stable is released.
  @Override
  public int getCompoundPaddingRight() {
    if (ViewUtils.isLayoutRtl(this)) {
      return super.getCompoundPaddingRight();
    }
    // Compound paddings are used during onMeasure() to decide the component width, at that time
    // the switch width is not overridden yet so we need to adjust the value to make measurement
    // right. This can be removed after the workaround is removed.
    return super.getCompoundPaddingRight() - switchWidth.get() + getSwitchMinWidth();
  }

  @Override
  public void setThumbTintList(@Nullable ColorStateList tint) {
    thumbTintList = tint;
    invalidate();
  }

  @Override
  @Nullable
  public ColorStateList getThumbTintList() {
    return thumbTintList;
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
  public void setTrackTintList(@Nullable ColorStateList tint) {
    trackTintList = tint;
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
   * mode using {@link DrawableCompat#setTintList(Drawable, ColorStateList)}.
   *
   * @param tint the tint to apply, may be {@code null} to clear tint
   *
   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_trackDecorationTint
   */
  public void setTrackDecorationTintList(@Nullable ColorStateList tint) {
    trackDecorationTintList = tint;
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

  // TODO(b/227338106): remove this workaround to use super.getThumbPosition() directly after
  //                    AppCompat 1.6.0-stable is released.
  private float getThumbPos() {
    return thumbPosition.get();
  }

  private void refreshTrackDrawable() {
    trackDrawable =
        createTintableDrawableIfNeeded(trackDrawable, trackTintList, getTrackTintMode());
    trackDecorationDrawable =
        createTintableDrawableIfNeeded(
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
    if (thumbTintList == null && trackTintList == null && trackDecorationTintList == null) {
      // Early return to avoid heavy operation.
      return;
    }

    float thumbPosition = getThumbPos();

    int[] currentState = getDrawableState();
    int[] currentStateUnchecked = getUncheckedState(currentState);
    int[] currentStateChecked = getCheckedState(currentState);

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

    if (thumbTintList != null) {
      setInterpolatedDrawableTintIfPossible(
          getThumbDrawable(),
          thumbTintList,
          currentStateUnchecked,
          currentStateChecked,
          thumbPosition);
    }
  }

  /** Returns a new state that removes the checked state from the input state. */
  private static int[] getUncheckedState(int[] state) {
    int[] newState = new int[state.length];
    int i = 0;
    for (int subState : state) {
      if (subState != android.R.attr.state_checked) {
        newState[i++] = subState;
      }
    }
    return newState;
  }

  /** Returns a new state that adds the checked state to the input state. */
  private static int[] getCheckedState(int[] state) {
    for (int i = 0; i < state.length; i++) {
      if (state[i] == android.R.attr.state_checked) {
        return state;
      } else if (state[i] == 0) {
        int[] newState = state.clone();
        newState[i] = android.R.attr.state_checked;
        return newState;
      }
    }
    int[] newState = Arrays.copyOf(state, state.length + 1);
    newState[state.length] = android.R.attr.state_checked;
    return newState;
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
    // TODO(b/232529333): remove this workaround after updating AppCompat version to 1.6.
    if (drawable instanceof AnimatedStateListDrawableCompat
        && VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
      DrawableCompat.setTintList(
          drawable,
          ColorStateList.valueOf(
              blendARGB(
                  tint.getColorForState(stateUnchecked, 0),
                  tint.getColorForState(stateChecked, 0),
                  thumbPosition)));
      return;
    }
    DrawableCompat.setTint(
        drawable,
        blendARGB(
            tint.getColorForState(stateUnchecked, 0),
            tint.getColorForState(stateChecked, 0),
            thumbPosition));
  }

  private static Drawable createTintableDrawableIfNeeded(
      Drawable drawable, ColorStateList tintList, Mode tintMode) {
    if (drawable == null) {
      return null;
    }
    if (tintList != null) {
      drawable = DrawableCompat.wrap(drawable).mutate();
      if (tintMode != null) {
        DrawableCompat.setTintMode(drawable, tintMode);
      }
    }
    return drawable;
  }

  // TODO(b/227338106): remove this workaround and move to use setEnforceSwitchWidth(false) after
  //                    AppCompat 1.6.0-stable is released.
  @SuppressLint("PrivateApi")
  private static final class SwitchWidth {

    @NonNull private final MaterialSwitch materialSwitch;
    @Nullable private final Field switchWidthField;

    @NonNull
    static SwitchWidth create(@NonNull MaterialSwitch materialSwitch) {
      return new SwitchWidth(materialSwitch, createSwitchWidthField());
    }

    private SwitchWidth(@NonNull MaterialSwitch materialSwitch, @Nullable Field switchWidthField) {
      this.materialSwitch = materialSwitch;
      this.switchWidthField = switchWidthField;
    }

    int get() {
      try {
        if (switchWidthField != null) {
          return switchWidthField.getInt(materialSwitch);
        }
      } catch (IllegalAccessException e) {
        // Fall through
      }
      // Return getSwitchMinWidth() so no width adjustment will be done.
      return materialSwitch.getSwitchMinWidth();
    }

    void set(int switchWidth) {
      try {
        if (switchWidthField != null) {
          switchWidthField.setInt(materialSwitch, switchWidth);
        }
      } catch (IllegalAccessException e) {
        // Fall through
      }
    }

    @Nullable
    private static Field createSwitchWidthField() {
      try {
        Field switchWidthField = SwitchCompat.class.getDeclaredField("mSwitchWidth");
        switchWidthField.setAccessible(true);
        return switchWidthField;
      } catch (NoSuchFieldException | SecurityException e) {
        return null;
      }
    }
  }

  // TODO(b/227338106): remove this workaround to use super.getThumbPosition() directly after
  //                    AppCompat 1.6.0-stable is released.
  @SuppressLint("PrivateApi")
  private final class ThumbPosition {
    private final Field thumbPositionField;

    private ThumbPosition() {
      thumbPositionField = createThumbPositionField();
    }

    float get() {
      try {
        if (thumbPositionField != null) {
          return thumbPositionField.getFloat(MaterialSwitch.this);
        }
      } catch (IllegalAccessException e) {
        // Fall through
      }
      return isChecked() ? 1 : 0;
    }

    private Field createThumbPositionField() {
      try {
        Field thumbPositionField = SwitchCompat.class.getDeclaredField("mThumbPosition");
        thumbPositionField.setAccessible(true);
        return thumbPositionField;
      } catch (Exception e) {
        return null;
      }
    }
  }
}
