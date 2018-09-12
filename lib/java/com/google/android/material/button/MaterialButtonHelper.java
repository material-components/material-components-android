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

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.ripple.RippleUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
class MaterialButtonHelper {

  // This is a hacky workaround. Currently on certain devices/versions,
  // LayerDrawable will draw a black background underneath any layer with a non-opaque color,
  // unless we set the shape to be something that's not a perfect rectangle.
  private static final float CORNER_RADIUS_ADJUSTMENT = 0.00001F;
  private static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;
  private static final boolean IS_LOLLIPOP = VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;

  private final MaterialButton materialButton;

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

  private final Paint buttonStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Rect bounds = new Rect();
  private final RectF rectF = new RectF();

  @Nullable private GradientDrawable colorableBackgroundDrawableCompat;
  @Nullable private Drawable tintableBackgroundDrawableCompat;
  @Nullable private GradientDrawable rippleDrawableCompat;
  @Nullable private Drawable tintableRippleDrawableCompat;

  @Nullable private GradientDrawable backgroundDrawableLollipop;
  @Nullable private GradientDrawable strokeDrawableLollipop;
  @Nullable private GradientDrawable maskDrawableLollipop;

  private boolean backgroundOverwritten = false;

  public MaterialButtonHelper(MaterialButton button) {
    materialButton = button;
  }

  public void loadFromAttributes(TypedArray attributes) {
    insetLeft = attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetLeft, 0);
    insetRight =
        attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetRight, 0);
    insetTop = attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetTop, 0);
    insetBottom =
        attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetBottom, 0);

    cornerRadius = attributes.getDimensionPixelSize(R.styleable.MaterialButton_cornerRadius, 0);
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

    buttonStrokePaint.setStyle(Style.STROKE);
    buttonStrokePaint.setStrokeWidth(strokeWidth);
    buttonStrokePaint.setColor(
        strokeColor != null
            ? strokeColor.getColorForState(materialButton.getDrawableState(), Color.TRANSPARENT)
            : Color.TRANSPARENT);

    // Store padding before setting background, since background overwrites padding values
    int paddingStart = ViewCompat.getPaddingStart(materialButton);
    int paddingTop = materialButton.getPaddingTop();
    int paddingEnd = ViewCompat.getPaddingEnd(materialButton);
    int paddingBottom = materialButton.getPaddingBottom();

    // Update materialButton's background without triggering setBackgroundOverwritten()
    materialButton.setInternalBackground(
        IS_LOLLIPOP ? createBackgroundLollipop() : createBackgroundCompat());

    // Set the stored padding values
    ViewCompat.setPaddingRelative(
        materialButton,
        paddingStart + insetLeft,
        paddingTop + insetTop,
        paddingEnd + insetRight,
        paddingBottom + insetBottom);
  }

  /**
   * Method that is triggered when our initial background, created by {@link
   * #createBackgroundCompat()} or {@link #createBackgroundLollipop()}, has been overwritten with a
   * new background. Sets the {@link #backgroundOverwritten} flag, which disables some of the
   * functionality tied to our custom background.
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

  /** Manually draw stroke on top of background for Kit Kat (API 19) and earlier versions */
  void drawStroke(@Nullable Canvas canvas) {
    if (canvas != null && strokeColor != null && strokeWidth > 0) {
      bounds.set(materialButton.getBackground().getBounds());
      rectF.set(
          bounds.left + (strokeWidth / 2f) + insetLeft,
          bounds.top + (strokeWidth / 2f) + insetTop,
          bounds.right - (strokeWidth / 2f) - insetRight,
          bounds.bottom - (strokeWidth / 2f) - insetBottom);
      // We need to adjust stroke's corner radius so that the corners of the background are not
      // drawn outside stroke
      float strokeCornerRadius = cornerRadius - strokeWidth / 2f;
      canvas.drawRoundRect(rectF, strokeCornerRadius, strokeCornerRadius, buttonStrokePaint);
    }
  }

  /**
   * Create background for KitKat (API 19) and earlier API versions
   *
   * @return Drawable representing background for this button.
   */
  private Drawable createBackgroundCompat() {
    colorableBackgroundDrawableCompat = new GradientDrawable();
    colorableBackgroundDrawableCompat.setCornerRadius(cornerRadius + CORNER_RADIUS_ADJUSTMENT);
    colorableBackgroundDrawableCompat.setColor(DEFAULT_BACKGROUND_COLOR);

    tintableBackgroundDrawableCompat = DrawableCompat.wrap(colorableBackgroundDrawableCompat);
    DrawableCompat.setTintList(tintableBackgroundDrawableCompat, backgroundTint);
    if (backgroundTintMode != null) {
      DrawableCompat.setTintMode(tintableBackgroundDrawableCompat, backgroundTintMode);
    }

    rippleDrawableCompat = new GradientDrawable();
    rippleDrawableCompat.setCornerRadius(cornerRadius + CORNER_RADIUS_ADJUSTMENT);
    rippleDrawableCompat.setColor(Color.WHITE);

    tintableRippleDrawableCompat = DrawableCompat.wrap(rippleDrawableCompat);
    DrawableCompat.setTintList(
        tintableRippleDrawableCompat, RippleUtils.convertToRippleDrawableColor(rippleColor));

    return wrapDrawableWithInset(
        new LayerDrawable(
            new Drawable[] {tintableBackgroundDrawableCompat, tintableRippleDrawableCompat}));
  }

  private InsetDrawable wrapDrawableWithInset(Drawable drawable) {
    return new InsetDrawable(drawable, insetLeft, insetTop, insetRight, insetBottom);
  }

  void setSupportBackgroundTintList(@Nullable ColorStateList tintList) {
    if (backgroundTint != tintList) {
      backgroundTint = tintList;
      if (IS_LOLLIPOP) {
        updateTintAndTintModeLollipop();
      } else if (tintableBackgroundDrawableCompat != null) {
        DrawableCompat.setTintList(tintableBackgroundDrawableCompat, backgroundTint);
      }
    }
  }

  ColorStateList getSupportBackgroundTintList() {
    return backgroundTint;
  }

  void setSupportBackgroundTintMode(@Nullable Mode mode) {
    if (backgroundTintMode != mode) {
      backgroundTintMode = mode;
      if (IS_LOLLIPOP) {
        updateTintAndTintModeLollipop();
      } else if (tintableBackgroundDrawableCompat != null && backgroundTintMode != null) {
        DrawableCompat.setTintMode(tintableBackgroundDrawableCompat, backgroundTintMode);
      }
    }
  }

  Mode getSupportBackgroundTintMode() {
    return backgroundTintMode;
  }

  private void updateTintAndTintModeLollipop() {
    if (backgroundDrawableLollipop != null) {
      DrawableCompat.setTintList(backgroundDrawableLollipop, backgroundTint);
      if (backgroundTintMode != null) {
        DrawableCompat.setTintMode(backgroundDrawableLollipop, backgroundTintMode);
      }
    }
  }

  /**
   * Create RippleDrawable background for Lollipop (API 21) and later API versions
   *
   * @return Drawable representing background for this button.
   */
  @TargetApi(VERSION_CODES.LOLLIPOP)
  private Drawable createBackgroundLollipop() {
    backgroundDrawableLollipop = new GradientDrawable();
    backgroundDrawableLollipop.setCornerRadius(cornerRadius + CORNER_RADIUS_ADJUSTMENT);
    backgroundDrawableLollipop.setColor(DEFAULT_BACKGROUND_COLOR);

    updateTintAndTintModeLollipop();

    strokeDrawableLollipop = new GradientDrawable();
    strokeDrawableLollipop.setCornerRadius(cornerRadius + CORNER_RADIUS_ADJUSTMENT);
    strokeDrawableLollipop.setColor(Color.TRANSPARENT);
    strokeDrawableLollipop.setStroke(strokeWidth, strokeColor);

    LayerDrawable layerDrawable =
        new LayerDrawable(new Drawable[] {backgroundDrawableLollipop, strokeDrawableLollipop});

    InsetDrawable bgInsetDrawable = wrapDrawableWithInset(layerDrawable);

    maskDrawableLollipop = new GradientDrawable();
    maskDrawableLollipop.setCornerRadius(cornerRadius + CORNER_RADIUS_ADJUSTMENT);
    maskDrawableLollipop.setColor(Color.WHITE);

    return new MaterialButtonBackgroundDrawable(
        RippleUtils.convertToRippleDrawableColor(rippleColor),
        bgInsetDrawable,
        maskDrawableLollipop);
  }

  void updateMaskBounds(int height, int width) {
    if (maskDrawableLollipop != null) {
      maskDrawableLollipop.setBounds(insetLeft, insetTop, width - insetRight, height - insetBottom);
    }
  }

  void setBackgroundColor(int color) {
    if (IS_LOLLIPOP && backgroundDrawableLollipop != null) {
      backgroundDrawableLollipop.setColor(color);
    } else if (!IS_LOLLIPOP && colorableBackgroundDrawableCompat != null) {
      colorableBackgroundDrawableCompat.setColor(color);
    }
  }

  void setRippleColor(@Nullable ColorStateList rippleColor) {
    if (this.rippleColor != rippleColor) {
      this.rippleColor = rippleColor;
      if (IS_LOLLIPOP && materialButton.getBackground() instanceof RippleDrawable) {
        ((RippleDrawable) materialButton.getBackground())
            .setColor(RippleUtils.convertToRippleDrawableColor(rippleColor));
      } else if (!IS_LOLLIPOP && tintableRippleDrawableCompat != null) {
        DrawableCompat.setTintList(
            tintableRippleDrawableCompat, RippleUtils.convertToRippleDrawableColor(rippleColor));
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
      buttonStrokePaint.setColor(
          strokeColor != null
              ? strokeColor.getColorForState(materialButton.getDrawableState(), Color.TRANSPARENT)
              : Color.TRANSPARENT);
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
      buttonStrokePaint.setStrokeWidth(strokeWidth);
      updateStroke();
    }
  }

  int getStrokeWidth() {
    return strokeWidth;
  }

  private void updateStroke() {
    if (IS_LOLLIPOP && strokeDrawableLollipop != null) {
      // TODO: Stroke on API 21 results in strange width, even after unwrapping stroke drawable
      // TODO: Changing stroke width on strokeDrawableLollipop results in stroke being clipped
      materialButton.setInternalBackground(createBackgroundLollipop());
    } else if (!IS_LOLLIPOP) {
      // Force redraw of stroke
      materialButton.invalidate();
    }
  }

  void setCornerRadius(int cornerRadius) {
    if (this.cornerRadius != cornerRadius) {
      this.cornerRadius = cornerRadius;
      if (IS_LOLLIPOP
          && backgroundDrawableLollipop != null
          && strokeDrawableLollipop != null
          && maskDrawableLollipop != null) {
        // TODO: Setting corner radius on API 21 does not work without unwrapping drawables
        if (VERSION.SDK_INT == VERSION_CODES.LOLLIPOP) {
          unwrapBackgroundDrawable().setCornerRadius(cornerRadius + CORNER_RADIUS_ADJUSTMENT);
          unwrapStrokeDrawable().setCornerRadius(cornerRadius + CORNER_RADIUS_ADJUSTMENT);
        }
        backgroundDrawableLollipop.setCornerRadius(cornerRadius + CORNER_RADIUS_ADJUSTMENT);
        strokeDrawableLollipop.setCornerRadius(cornerRadius + CORNER_RADIUS_ADJUSTMENT);
        maskDrawableLollipop.setCornerRadius(cornerRadius + CORNER_RADIUS_ADJUSTMENT);
      } else if (!IS_LOLLIPOP
          && colorableBackgroundDrawableCompat != null
          && rippleDrawableCompat != null) {
        colorableBackgroundDrawableCompat.setCornerRadius(cornerRadius + CORNER_RADIUS_ADJUSTMENT);
        rippleDrawableCompat.setCornerRadius(cornerRadius + CORNER_RADIUS_ADJUSTMENT);
        // Force redraw of stroke
        materialButton.invalidate();
      }
    }
  }

  int getCornerRadius() {
    return cornerRadius;
  }

  @Nullable
  private GradientDrawable unwrapStrokeDrawable() {
    if (IS_LOLLIPOP && materialButton.getBackground() != null) {
      RippleDrawable background = (RippleDrawable) materialButton.getBackground();
      InsetDrawable insetDrawable = (InsetDrawable) background.getDrawable(0);
      LayerDrawable layerDrawable = (LayerDrawable) insetDrawable.getDrawable();
      return (GradientDrawable) layerDrawable.getDrawable(1);
    } else {
      return null;
    }
  }

  @Nullable
  private GradientDrawable unwrapBackgroundDrawable() {
    if (IS_LOLLIPOP && materialButton.getBackground() != null) {
      RippleDrawable background = (RippleDrawable) materialButton.getBackground();
      InsetDrawable insetDrawable = (InsetDrawable) background.getDrawable(0);
      LayerDrawable layerDrawable = (LayerDrawable) insetDrawable.getDrawable();
      return (GradientDrawable) layerDrawable.getDrawable(0);
    } else {
      return null;
    }
  }
}
