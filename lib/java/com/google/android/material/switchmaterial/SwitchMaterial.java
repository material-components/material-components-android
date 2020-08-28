/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.google.android.material.switchmaterial;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import androidx.appcompat.widget.SwitchCompat;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.elevation.ElevationOverlayProvider;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.progressindicator.CircularDrawingDelegate;
import com.google.android.material.progressindicator.CircularIndeterminateAnimatorDelegate;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.progressindicator.ProgressIndicatorSpec;

/**
 * A class that creates a Material Themed Switch.
 *
 * <p>This class uses attributes from the Material Theme to style a Switch. Excepting color changes,
 * it behaves identically to {@link SwitchCompat}. Your theme's {@code ?attr/colorControlActivated},
 * {@code ?attr/colorSurface}, and {@code ?attr/colorOnSurface} must be set. Because {@link
 * SwitchCompat} does not extend {@link android.widget.Switch}, you must explicitly declare {@link
 * SwitchMaterial} in your layout XML.
 */
public class SwitchMaterial extends SwitchCompat {

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_CompoundButton_Switch;
  private static final int DEF_PENDING_STYLE_RES =
      R.style.Widget_MaterialComponents_ProgressIndicator_Circular_Indeterminate;
  private static final int[][] ENABLED_CHECKED_PENDING_STATES =
      new int[][] {
        // Enabled, checked, not pending.
        new int[] {
          android.R.attr.state_enabled, android.R.attr.state_checked, -R.attr.state_pending
        },
        // Enabled, checked, pending.
        new int[] {
          android.R.attr.state_enabled, android.R.attr.state_checked, R.attr.state_pending
        },
        // Enabled, unchecked, not pending.
        new int[] {
          android.R.attr.state_enabled, -android.R.attr.state_checked, -R.attr.state_pending
        },
        // Enabled, unchecked, pending.
        new int[] {
          android.R.attr.state_enabled, -android.R.attr.state_checked, R.attr.state_pending
        },
        // Disabled, checked.
        new int[] {-android.R.attr.state_enabled, android.R.attr.state_checked},
        // Disabled, unchecked.
        new int[] {-android.R.attr.state_enabled, -android.R.attr.state_checked}
      };

  @NonNull private final ElevationOverlayProvider elevationOverlayProvider;

  @Nullable private ColorStateList materialThemeColorsThumbTintList;
  @Nullable private ColorStateList materialThemeColorsTrackTintList;
  private boolean useMaterialThemeColors;

  private boolean materialThemeColorsLoaded;
  private int materialThemeThumbCheckedColor;
  private int materialThemeThumbUncheckedColor;
  private int materialThemeThumbCheckedDisabledColor;
  private int materialThemeThumbUncheckedDisabledColor;
  private int materialThemeTrackCheckedColor;
  private int materialThemeTrackUncheckedColor;
  private int materialThemeTrackCheckedDisabledColor;
  private int materialThemeTrackUncheckedDisabledColor;

  private boolean pending;

  private IndeterminateDrawable pendingDrawable;

  private static final int[] PENDING_STATE_SET = {R.attr.state_pending};

  public SwitchMaterial(@NonNull Context context) {
    this(context, null);
  }

  public SwitchMaterial(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.switchStyle);
  }

  public SwitchMaterial(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    elevationOverlayProvider = new ElevationOverlayProvider(context);

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.SwitchMaterial, defStyleAttr, DEF_STYLE_RES);

    useMaterialThemeColors =
        attributes.getBoolean(R.styleable.SwitchMaterial_useMaterialThemeColors, false);
    pending = attributes.getBoolean(R.styleable.SwitchMaterial_state_pending, false);

    attributes.recycle();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (useMaterialThemeColors && getThumbTintList() == null) {
      setThumbTintList(getMaterialThemeColorsThumbTintList());
    }
    if (useMaterialThemeColors && getTrackTintList() == null) {
      setTrackTintList(getMaterialThemeColorsTrackTintList());
    }
    if (getThumbDrawable() != null && pendingDrawable == null) {
      Rect thumbPadding = new Rect();
      getThumbDrawable().getPadding(thumbPadding);
      float thumbWidth =
          getThumbDrawable().getIntrinsicWidth() - thumbPadding.left - thumbPadding.right;

      ProgressIndicatorSpec progressIndicatorSpec = new ProgressIndicatorSpec();
      progressIndicatorSpec.loadFromAttributes(getContext(), null, -1, DEF_PENDING_STYLE_RES);
      progressIndicatorSpec.circularInset = 0;
      progressIndicatorSpec.circularRadius = (int) (thumbWidth / 2 * 0.7);
      progressIndicatorSpec.indicatorSize = (int) ViewUtils.dpToPx(getContext(), 2);

      progressIndicatorSpec.indicatorColors = new int[] {materialThemeThumbCheckedColor};
      pendingDrawable =
          new IndeterminateDrawable(
              getContext(),
              progressIndicatorSpec,
              new CircularDrawingDelegate(),
              new CircularIndeterminateAnimatorDelegate());
      pendingDrawable.setVisible(pending, false);
    }
  }

  @Override
  public void onDraw(@NonNull Canvas canvas) {
    super.onDraw(canvas);
    if (pendingDrawable.isVisible()) {
      Rect thumbBounds = getThumbDrawable().getBounds();
      // Moves the pending drawable to correctly overlap on the thumb drawable. Because the thumb
      // drawable has the origin at the top-left corner of the view; the pending drawable
      // (IndeterminateDrawable with CircularDrawingDelegate) has the origin at its own top-left
      // corner.
      canvas.translate(
          (thumbBounds.left + thumbBounds.right - pendingDrawable.getIntrinsicWidth()) / 2f,
          (thumbBounds.top + thumbBounds.bottom - pendingDrawable.getIntrinsicHeight()) / 2f);
      pendingDrawable.setBounds(thumbBounds);
      pendingDrawable.draw(canvas);
      invalidate();
    }
  }

  @Override
  @NonNull
  protected int[] onCreateDrawableState(int extraSpace) {
    final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
    if (isPending()) {
      mergeDrawableStates(drawableState, PENDING_STATE_SET);
    }
    return drawableState;
  }

  /**
   * Sets the enabled state of this Switch. Any checked state with pending state ON will be
   * discarded, meaning the opposite state will be set.
   *
   * @param enabled True if this view is enabled, false otherwise.
   */
  @Override
  public void setEnabled(boolean enabled) {
    if (enabled) {
      setPending(false);
    } else {
      setChecked(pending ^ isChecked());
    }
    super.setEnabled(enabled);
  }

  @Override
  public void setChecked(boolean b) {
    super.setChecked(b);
    updatePendingDrawableColor();
  }

  /**
   * Sets the pending state of this Switch.
   *
   * @param pending true to show the pending drawable; otherwise, false.
   */
  public void setPending(boolean pending) {
    if (this.pending != pending) {
      this.pending = pending;
      updatePendingDrawableColor();
      pendingDrawable.setVisible(pending, false);
      refreshDrawableState();
    }
  }

  /** Returns the pending state of this Switch. */
  public boolean isPending() {
    return pending;
  }

  /**
   * Forces the {@link SwitchMaterial} to use colors from a Material Theme. Overrides any specified
   * tint list for the track and thumb. If set to false, sets the tints to null. Use {@link
   * SwitchCompat#setTrackTintList(ColorStateList)} and {@link
   * SwitchCompat#setThumbTintList(ColorStateList)} to change tints.
   */
  public void setUseMaterialThemeColors(boolean useMaterialThemeColors) {
    this.useMaterialThemeColors = useMaterialThemeColors;
    if (useMaterialThemeColors) {
      setThumbTintList(getMaterialThemeColorsThumbTintList());
      setTrackTintList(getMaterialThemeColorsTrackTintList());
    } else {
      setThumbTintList(null);
      setTrackTintList(null);
    }
  }

  /** Returns true if this {@link SwitchMaterial} defaults to colors from a Material Theme. */
  public boolean isUseMaterialThemeColors() {
    return useMaterialThemeColors;
  }

  private void updatePendingDrawableColor() {
    if (pendingDrawable != null) {
      pendingDrawable.setIndicatorColors(
          new int[] {
            isChecked() ? materialThemeThumbCheckedColor : materialThemeThumbUncheckedColor
          });
    }
  }

  private ColorStateList getMaterialThemeColorsThumbTintList() {
    if (materialThemeColorsThumbTintList == null) {
      if (!materialThemeColorsLoaded) {
        loadMaterialThemeColors();
      }
      int[] switchThumbColorsList = new int[ENABLED_CHECKED_PENDING_STATES.length];
      switchThumbColorsList[0] = materialThemeThumbCheckedColor;
      switchThumbColorsList[1] = materialThemeThumbUncheckedColor;
      switchThumbColorsList[2] = materialThemeThumbUncheckedColor;
      switchThumbColorsList[3] = materialThemeThumbCheckedColor;
      switchThumbColorsList[4] = materialThemeThumbCheckedDisabledColor;
      switchThumbColorsList[5] = materialThemeThumbUncheckedDisabledColor;
      materialThemeColorsThumbTintList =
          new ColorStateList(ENABLED_CHECKED_PENDING_STATES, switchThumbColorsList);
    }
    return materialThemeColorsThumbTintList;
  }

  private ColorStateList getMaterialThemeColorsTrackTintList() {
    if (materialThemeColorsTrackTintList == null) {
      if (!materialThemeColorsLoaded) {
        loadMaterialThemeColors();
      }
      int[] switchTrackColorsList = new int[ENABLED_CHECKED_PENDING_STATES.length];
      switchTrackColorsList[0] = materialThemeTrackCheckedColor;
      switchTrackColorsList[1] = materialThemeTrackUncheckedColor;
      switchTrackColorsList[2] = materialThemeTrackUncheckedColor;
      switchTrackColorsList[3] = materialThemeTrackCheckedColor;
      switchTrackColorsList[4] = materialThemeTrackCheckedDisabledColor;
      switchTrackColorsList[5] = materialThemeTrackUncheckedDisabledColor;
      materialThemeColorsTrackTintList =
          new ColorStateList(ENABLED_CHECKED_PENDING_STATES, switchTrackColorsList);
    }
    return materialThemeColorsTrackTintList;
  }

  private void loadMaterialThemeColors() {
    int colorSurface = MaterialColors.getColor(this, R.attr.colorSurface);
    int colorControlActivated = MaterialColors.getColor(this, R.attr.colorControlActivated);
    int colorOnSurface = MaterialColors.getColor(this, R.attr.colorOnSurface);
    float thumbElevation = getResources().getDimension(R.dimen.mtrl_switch_thumb_elevation);
    if (elevationOverlayProvider.isThemeElevationOverlayEnabled()) {
      thumbElevation += ViewUtils.getParentAbsoluteElevation(this);
    }
    // Colors for the thumb.
    materialThemeThumbCheckedColor =
        MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_FULL);
    materialThemeThumbUncheckedColor =
        elevationOverlayProvider.compositeOverlayIfNeeded(colorSurface, thumbElevation);
    materialThemeThumbCheckedDisabledColor =
        MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_DISABLED);
    materialThemeThumbUncheckedDisabledColor = materialThemeThumbUncheckedColor;
    // Colors for the track.
    materialThemeTrackCheckedColor =
        MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_MEDIUM);
    materialThemeTrackUncheckedColor =
        MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_LOW);
    materialThemeTrackCheckedDisabledColor =
        MaterialColors.layer(
            colorSurface, colorControlActivated, MaterialColors.ALPHA_DISABLED_LOW);
    materialThemeTrackUncheckedDisabledColor =
        MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED_LOW);

    materialThemeColorsLoaded = true;
  }
}
