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
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.ripple.RippleUtils;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
class MaterialButtonHelper {

  private static final float CORNER_RADIUS_ADJUSTMENT = 0.00001F;
  private static final boolean IS_LOLLIPOP = VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
  private final MaterialButton materialButton;
  private final ShapeAppearanceModel shapeAppearanceModel;

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

  @Nullable private MaterialShapeDrawable maskDrawable;
  private boolean backgroundOverwritten = false;
  private boolean cornerRadiusSet = false;
  private LayerDrawable rippleDrawable;

  MaterialButtonHelper(MaterialButton button, ShapeAppearanceModel shapeAppearanceModel) {
    materialButton = button;
    this.shapeAppearanceModel = shapeAppearanceModel;
  }

  void loadFromAttributes(TypedArray attributes) {
    insetLeft = attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetLeft, 0);
    insetRight =
        attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetRight, 0);
    insetTop = attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetTop, 0);
    insetBottom =
        attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetBottom, 0);

    // cornerRadius should override whatever corner radius is set in shapeAppearanceModel
    if (attributes.hasValue(R.styleable.MaterialButton_cornerRadius)) {
      cornerRadius = attributes.getDimensionPixelSize(R.styleable.MaterialButton_cornerRadius, -1);
      shapeAppearanceModel.setCornerRadius(cornerRadius);
      cornerRadiusSet = true;
    }

    adjustShapeAppearanceModelCornerRadius(shapeAppearanceModel, CORNER_RADIUS_ADJUSTMENT);

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

    // Store padding before setting background, since background overwrites padding values
    int paddingStart = ViewCompat.getPaddingStart(materialButton);
    int paddingTop = materialButton.getPaddingTop();
    int paddingEnd = ViewCompat.getPaddingEnd(materialButton);
    int paddingBottom = materialButton.getPaddingBottom();

    // Update materialButton's background without triggering setBackgroundOverwritten()
    materialButton.setInternalBackground(createBackground());

    // Set the stored padding values
    ViewCompat.setPaddingRelative(
        materialButton,
        paddingStart + insetLeft,
        paddingTop + insetTop,
        paddingEnd + insetRight,
        paddingBottom + insetBottom);
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

  /**
   * Create RippleDrawable background for Lollipop (API 21) and later API versions
   *
   * @return Drawable representing background for this button.
   */
  private Drawable createBackground() {
    MaterialShapeDrawable backgroundDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
    DrawableCompat.setTintList(backgroundDrawable, backgroundTint);
    if (backgroundTintMode != null) {
      DrawableCompat.setTintMode(backgroundDrawable, backgroundTintMode);
    }
    backgroundDrawable.setStroke(strokeWidth, strokeColor);
    maskDrawable = new MaterialShapeDrawable(shapeAppearanceModel);

    if (IS_LOLLIPOP) {
      if (strokeWidth > 0) {
        ShapeAppearanceModel temporaryAdjustedShapeAppearanceModel =
            new ShapeAppearanceModel(shapeAppearanceModel);
        adjustShapeAppearanceModelCornerRadius(
            temporaryAdjustedShapeAppearanceModel, strokeWidth / 2f);
        backgroundDrawable.setShapeAppearanceModel(temporaryAdjustedShapeAppearanceModel);
        maskDrawable.setShapeAppearanceModel(temporaryAdjustedShapeAppearanceModel);
      }

      DrawableCompat.setTint(maskDrawable, Color.WHITE);
      rippleDrawable =
          new RippleDrawable(
              RippleUtils.convertToRippleDrawableColor(rippleColor),
              wrapDrawableWithInset(backgroundDrawable),
              maskDrawable);
      return rippleDrawable;
    } else {
      DrawableCompat.setTintList(
          maskDrawable, RippleUtils.convertToRippleDrawableColor(rippleColor));
      rippleDrawable = new LayerDrawable(new Drawable[] {backgroundDrawable, maskDrawable});
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
            .setColor(RippleUtils.convertToRippleDrawableColor(rippleColor));
      } else if (!IS_LOLLIPOP && getMaskDrawable() != null) {
        DrawableCompat.setTintList(
            getMaskDrawable(), RippleUtils.convertToRippleDrawableColor(rippleColor));
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
    if (materialShapeDrawable != null) {
      materialShapeDrawable.setStroke(strokeWidth, strokeColor);
      if (IS_LOLLIPOP) {
        ShapeAppearanceModel temporaryShapeAppearance =
            new ShapeAppearanceModel(shapeAppearanceModel);
        adjustShapeAppearanceModelCornerRadius(temporaryShapeAppearance, strokeWidth / 2f);
        materialShapeDrawable.setShapeAppearanceModel(temporaryShapeAppearance);

        if (getMaskDrawable() != null) {
          getMaskDrawable().setShapeAppearanceModel(temporaryShapeAppearance);
        }
        // Some APIs don't unwrap the drawable correctly.
        if (maskDrawable != null) {
          maskDrawable.setShapeAppearanceModel(temporaryShapeAppearance);
        }
      }
    }
  }

  void setCornerRadius(int cornerRadius) {
    // If cornerRadius wasn't set in the style, it would have a default value of -1. Therefore, for
    // setCornerRadius(-1) to take effect, we need this cornerRadiusSet flag.
    if (!cornerRadiusSet || this.cornerRadius != cornerRadius) {
      this.cornerRadius = cornerRadius;
      cornerRadiusSet = true;
      shapeAppearanceModel.setCornerRadius(
          cornerRadius + CORNER_RADIUS_ADJUSTMENT + (strokeWidth / 2f));
      if (getMaterialShapeDrawable() != null) {
        getMaterialShapeDrawable().setShapeAppearanceModel(shapeAppearanceModel);
      }
      if (getMaskDrawable() != null) {
        getMaskDrawable().setShapeAppearanceModel(shapeAppearanceModel);
      }
    }
  }

  int getCornerRadius() {
    return cornerRadius;
  }

  private void adjustShapeAppearanceModelCornerRadius(
      ShapeAppearanceModel shapeAppearanceModel, float cornerRadiusAdjustment) {
    shapeAppearanceModel
        .getTopLeftCorner()
        .setCornerSize(
            shapeAppearanceModel.getTopLeftCorner().getCornerSize() + cornerRadiusAdjustment);
    shapeAppearanceModel
        .getTopRightCorner()
        .setCornerSize(
            shapeAppearanceModel.getTopRightCorner().getCornerSize() + cornerRadiusAdjustment);
    shapeAppearanceModel
        .getBottomRightCorner()
        .setCornerSize(
            shapeAppearanceModel.getBottomRightCorner().getCornerSize() + cornerRadiusAdjustment);
    shapeAppearanceModel
        .getBottomLeftCorner()
        .setCornerSize(
            shapeAppearanceModel.getBottomLeftCorner().getCornerSize() + cornerRadiusAdjustment);
  }

  @Nullable
  private MaterialShapeDrawable getMaterialShapeDrawable() {
    Drawable result = null;
    if (rippleDrawable != null && rippleDrawable.getNumberOfLayers() > 0) {
      result = rippleDrawable.getDrawable(0);
    }

    if (result instanceof MaterialShapeDrawable) {
      return (MaterialShapeDrawable) result;
    }

    if (result instanceof InsetDrawable) {
      InsetDrawable insetDrawable = (InsetDrawable) result;
      if (IS_LOLLIPOP) {
        return (MaterialShapeDrawable) insetDrawable.getDrawable();
      }
    }

    return null;
  }

  @Nullable
  public MaterialShapeDrawable getMaskDrawable() {
    if (rippleDrawable != null && rippleDrawable.getNumberOfLayers() > 1) {
      return (MaterialShapeDrawable) rippleDrawable.getDrawable(1);
    }

    return null;
  }
}
