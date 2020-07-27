/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.material.button;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.ripple.RippleDrawableCompat;
import com.google.android.material.ripple.RippleUtils;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.Shapeable;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
class MaterialButtonHelper {

  private static final boolean IS_LOLLIPOP = VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
  private final MaterialButton materialButton;
  @NonNull private ShapeAppearanceModel shapeAppearanceModel;

  private int insetLeft;
  private int insetRight;
  private int insetTop;
  private int insetBottom;
  private int cornerRadius;
  private int strokeWidth;

  @Nullable private Mode backgroundTintMode;
  @Nullable private ColorStateList backgroundTint;
  @Nullable private ColorStateList strokeColor;
  @Nullable private ColorStateList rippleColor;

  @Nullable private Drawable maskDrawable;
  private boolean shouldDrawSurfaceColorStroke = false;
  private boolean backgroundOverwritten = false;
  private boolean cornerRadiusSet = false;
  private boolean checkable;
  private LayerDrawable rippleDrawable;
  private int elevation;

  MaterialButtonHelper(MaterialButton button, @NonNull ShapeAppearanceModel shapeAppearanceModel) {
    materialButton = button;
    this.shapeAppearanceModel = shapeAppearanceModel;
  }

  void loadFromAttributes(@NonNull TypedArray attributes) {
    insetLeft = attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetLeft, 0);
    insetRight =
        attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetRight, 0);
    insetTop = attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetTop, 0);
    insetBottom =
        attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetBottom, 0);

    // cornerRadius should override whatever corner radius is set in shapeAppearanceModel
    if (attributes.hasValue(R.styleable.MaterialButton_cornerRadius)) {
      cornerRadius = attributes.getDimensionPixelSize(R.styleable.MaterialButton_cornerRadius, -1);
      setShapeAppearanceModel(shapeAppearanceModel.withCornerSize(cornerRadius));
      cornerRadiusSet = true;
    }

    strokeWidth = attributes.getDimensionPixelSize(R.styleable.MaterialButton_strokeWidth, 0);

    backgroundTintMode =
        ViewUtils.parseTintMode(
            attributes.getInt(R.styleable.MaterialButton_backgroundTintMode, -1), Mode.SRC_IN);
    backgroundTint =
        MaterialResources.getColorStateList(
            materialButton.getContext(), attributes, R.styleable.MaterialButton_backgroundTint);
    strokeColor =
        MaterialResources.getColorStateList(
            materialButton.getContext(), attributes, R.styleable.MaterialButton_strokeColor);
    rippleColor =
        MaterialResources.getColorStateList(
            materialButton.getContext(), attributes, R.styleable.MaterialButton_rippleColor);

    checkable = attributes.getBoolean(R.styleable.MaterialButton_android_checkable, false);
    elevation = attributes.getDimensionPixelSize(R.styleable.MaterialButton_elevation, 0);

    // Store padding before setting background, since background overwrites padding values
    int paddingStart = ViewCompat.getPaddingStart(materialButton);
    int paddingTop = materialButton.getPaddingTop();
    int paddingEnd = ViewCompat.getPaddingEnd(materialButton);
    int paddingBottom = materialButton.getPaddingBottom();

    // Update materialButton's background without triggering setBackgroundOverwritten()
    if (attributes.hasValue(R.styleable.MaterialButton_android_background)) {
      setBackgroundOverwritten();
    } else {
      updateBackground();
    }
    // Set the stored padding values
    ViewCompat.setPaddingRelative(
        materialButton,
        paddingStart + insetLeft,
        paddingTop + insetTop,
        paddingEnd + insetRight,
        paddingBottom + insetBottom);
  }

  private void updateBackground() {
    materialButton.setInternalBackground(createBackground());
    MaterialShapeDrawable materialShapeDrawable = getMaterialShapeDrawable();
    if (materialShapeDrawable != null) {
      materialShapeDrawable.setElevation(elevation);
    }
  }

  /**
   * Method that is triggered when our initial background, created by {@link #createBackground()},
   * has been overwritten with a new background. Sets the {@link #backgroundOverwritten} flag, which
   * disables some of the functionality tied to our custom background.
   */
  void setBackgroundOverwritten() {
    backgroundOverwritten = true;
    // AppCompatButton re-applies any tint that was set when background is changed, so we must
    // pass our tints to AppCompatButton when background is overwritten.
    materialButton.setSupportBackgroundTintList(backgroundTint);
    materialButton.setSupportBackgroundTintMode(backgroundTintMode);
  }

  boolean isBackgroundOverwritten() {
    return backgroundOverwritten;
  }

  @NonNull
  private InsetDrawable wrapDrawableWithInset(Drawable drawable) {
    return new InsetDrawable(drawable, insetLeft, insetTop, insetRight, insetBottom);
  }

  void setSupportBackgroundTintList(@Nullable ColorStateList tintList) {
    if (backgroundTint != tintList) {
      backgroundTint = tintList;
      if (getMaterialShapeDrawable() != null) {
        DrawableCompat.setTintList(getMaterialShapeDrawable(), backgroundTint);
      }
    }
  }

  ColorStateList getSupportBackgroundTintList() {
    return backgroundTint;
  }

  void setSupportBackgroundTintMode(@Nullable Mode mode) {
    if (backgroundTintMode != mode) {
      backgroundTintMode = mode;
      if (getMaterialShapeDrawable() != null && backgroundTintMode != null) {
        DrawableCompat.setTintMode(getMaterialShapeDrawable(), backgroundTintMode);
      }
    }
  }

  Mode getSupportBackgroundTintMode() {
    return backgroundTintMode;
  }

  void setShouldDrawSurfaceColorStroke(boolean shouldDrawSurfaceColorStroke) {
    this.shouldDrawSurfaceColorStroke = shouldDrawSurfaceColorStroke;
    updateStroke();
  }

  /**
   * Create RippleDrawable background for Lollipop (API 21) and later API versions
   *
   * @return Drawable representing background for this button.
   */
  private Drawable createBackground() {
    MaterialShapeDrawable backgroundDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
    Context context = materialButton.getContext();
    backgroundDrawable.initializeElevationOverlay(context);
    DrawableCompat.setTintList(backgroundDrawable, backgroundTint);
    if (backgroundTintMode != null) {
      DrawableCompat.setTintMode(backgroundDrawable, backgroundTintMode);
    }
    backgroundDrawable.setStroke(strokeWidth, strokeColor);

    MaterialShapeDrawable surfaceColorStrokeDrawable =
        new MaterialShapeDrawable(shapeAppearanceModel);
    surfaceColorStrokeDrawable.setTint(Color.TRANSPARENT);
    surfaceColorStrokeDrawable.setStroke(
        strokeWidth,
        shouldDrawSurfaceColorStroke
            ? MaterialColors.getColor(materialButton, R.attr.colorSurface)
            : Color.TRANSPARENT);

    if (IS_LOLLIPOP) {
      maskDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
      DrawableCompat.setTint(maskDrawable, Color.WHITE);
      rippleDrawable =
          new RippleDrawable(
              RippleUtils.sanitizeRippleDrawableColor(rippleColor),
              wrapDrawableWithInset(
                  new LayerDrawable(
                      new Drawable[] {surfaceColorStrokeDrawable, backgroundDrawable})),
              maskDrawable);
      return rippleDrawable;
    } else {
      maskDrawable = new RippleDrawableCompat(shapeAppearanceModel);
      DrawableCompat.setTintList(
          maskDrawable, RippleUtils.sanitizeRippleDrawableColor(rippleColor));
      rippleDrawable =
          new LayerDrawable(
              new Drawable[] {surfaceColorStrokeDrawable, backgroundDrawable, maskDrawable});
      return wrapDrawableWithInset(rippleDrawable);
    }
  }

  void updateMaskBounds(int height, int width) {
    if (maskDrawable != null) {
      maskDrawable.setBounds(insetLeft, insetTop, width - insetRight, height - insetBottom);
    }
  }

  void setBackgroundColor(int color) {
    if (getMaterialShapeDrawable() != null) {
      getMaterialShapeDrawable().setTint(color);
    }
  }

  void setRippleColor(@Nullable ColorStateList rippleColor) {
    if (this.rippleColor != rippleColor) {
      this.rippleColor = rippleColor;
      if (IS_LOLLIPOP && materialButton.getBackground() instanceof RippleDrawable) {
        ((RippleDrawable) materialButton.getBackground())
            .setColor(RippleUtils.sanitizeRippleDrawableColor(rippleColor));
      } else if (!IS_LOLLIPOP && materialButton.getBackground() instanceof RippleDrawableCompat) {
        ((RippleDrawableCompat) materialButton.getBackground()).setTintList(
            RippleUtils.sanitizeRippleDrawableColor(rippleColor));
      }
    }
  }

  @Nullable
  ColorStateList getRippleColor() {
    return rippleColor;
  }

  void setStrokeColor(@Nullable ColorStateList strokeColor) {
    if (this.strokeColor != strokeColor) {
      this.strokeColor = strokeColor;
      updateStroke();
    }
  }

  @Nullable
  ColorStateList getStrokeColor() {
    return strokeColor;
  }

  void setStrokeWidth(int strokeWidth) {
    if (this.strokeWidth != strokeWidth) {
      this.strokeWidth = strokeWidth;
      updateStroke();
    }
  }

  int getStrokeWidth() {
    return strokeWidth;
  }

  private void updateStroke() {
    MaterialShapeDrawable materialShapeDrawable = getMaterialShapeDrawable();
    MaterialShapeDrawable surfaceColorStrokeDrawable = getSurfaceColorStrokeDrawable();
    if (materialShapeDrawable != null) {
      materialShapeDrawable.setStroke(strokeWidth, strokeColor);
      if (surfaceColorStrokeDrawable != null) {
        surfaceColorStrokeDrawable.setStroke(
            strokeWidth,
            shouldDrawSurfaceColorStroke
                ? MaterialColors.getColor(materialButton, R.attr.colorSurface)
                : Color.TRANSPARENT);
      }
    }
  }

  void setCornerRadius(int cornerRadius) {
    // If cornerRadius wasn't set in the style, it would have a default value of -1. Therefore, for
    // setCornerRadius(-1) to take effect, we need this cornerRadiusSet flag.
    if (!cornerRadiusSet || this.cornerRadius != cornerRadius) {
      this.cornerRadius = cornerRadius;
      cornerRadiusSet = true;

      setShapeAppearanceModel(shapeAppearanceModel.withCornerSize(cornerRadius));
    }
  }

  int getCornerRadius() {
    return cornerRadius;
  }

  @Nullable
  private MaterialShapeDrawable getMaterialShapeDrawable(boolean getSurfaceColorStrokeDrawable) {
    if (rippleDrawable != null && rippleDrawable.getNumberOfLayers() > 0) {
      if (IS_LOLLIPOP) {
        InsetDrawable insetDrawable = (InsetDrawable) rippleDrawable.getDrawable(0);
        LayerDrawable layerDrawable = (LayerDrawable) insetDrawable.getDrawable();
        return (MaterialShapeDrawable)
            layerDrawable.getDrawable(getSurfaceColorStrokeDrawable ? 0 : 1);
      } else {
        return (MaterialShapeDrawable)
            rippleDrawable.getDrawable(getSurfaceColorStrokeDrawable ? 0 : 1);
      }
    }

    return null;
  }

  @Nullable
  MaterialShapeDrawable getMaterialShapeDrawable() {
    return getMaterialShapeDrawable(false);
  }

  void setCheckable(boolean checkable) {
    this.checkable = checkable;
  }

  boolean isCheckable() {
    return checkable;
  }

  @Nullable
  private MaterialShapeDrawable getSurfaceColorStrokeDrawable() {
    return getMaterialShapeDrawable(true);
  }

  private void updateButtonShape(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    if (getMaterialShapeDrawable() != null) {
      getMaterialShapeDrawable().setShapeAppearanceModel(shapeAppearanceModel);
    }
    if (getSurfaceColorStrokeDrawable() != null) {
      getSurfaceColorStrokeDrawable().setShapeAppearanceModel(shapeAppearanceModel);
    }
    if (getMaskDrawable() != null) {
      getMaskDrawable().setShapeAppearanceModel(shapeAppearanceModel);
    }
  }

  @Nullable
  public Shapeable getMaskDrawable() {
    if (rippleDrawable != null && rippleDrawable.getNumberOfLayers() > 1) {
      if (rippleDrawable.getNumberOfLayers() > 2) {
        // This is a LayerDrawable with 3 layers, so return the mask layer
        return (Shapeable) rippleDrawable.getDrawable(2);
      }
      // This is a RippleDrawable, so return the mask layer
      return (Shapeable) rippleDrawable.getDrawable(1);
    }

    return null;
  }

  void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    this.shapeAppearanceModel = shapeAppearanceModel;
    updateButtonShape(shapeAppearanceModel);
  }

  @NonNull
  ShapeAppearanceModel getShapeAppearanceModel() {
    return this.shapeAppearanceModel;
  }

  public void setInsetBottom(@Dimension int newInsetBottom) {
    setVerticalInsets(insetTop, newInsetBottom);
  }

  public int getInsetBottom() {
    return insetBottom;
  }

  public void setInsetTop(@Dimension int newInsetTop) {
    setVerticalInsets(newInsetTop, insetBottom);
  }

  private void setVerticalInsets(@Dimension int newInsetTop, @Dimension int newInsetBottom) {
    // Store padding before setting background, since background overwrites padding values
    int paddingStart = ViewCompat.getPaddingStart(materialButton);
    int paddingTop = materialButton.getPaddingTop();
    int paddingEnd = ViewCompat.getPaddingEnd(materialButton);
    int paddingBottom = materialButton.getPaddingBottom();
    int oldInsetTop = insetTop;
    int oldInsetBottom = insetBottom;
    insetBottom = newInsetBottom;
    insetTop = newInsetTop;
    if (!backgroundOverwritten) {
      updateBackground();
    }
    // Set the stored padding values
    ViewCompat.setPaddingRelative(
        materialButton,
        paddingStart,
        paddingTop + newInsetTop - oldInsetTop,
        paddingEnd,
        paddingBottom + newInsetBottom - oldInsetBottom);
  }

  public int getInsetTop() {
    return insetTop;
  }

}
