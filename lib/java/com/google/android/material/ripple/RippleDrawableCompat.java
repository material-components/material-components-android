/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.ripple;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.core.graphics.drawable.TintAwareDrawable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.Shapeable;

/**
 * A compat {@link Drawable} that is used to provide an overlay for pressed, focused, and hovered
 * states (only when in enabled). This is intended to be used pre-Lollipop.
 *
 * <p>This Drawable is a {@link MaterialShapeDrawable} so that it can be shaped to match a
 * MaterialShapeDrawable background.
 *
 * <p>Unlike the framework {@link android.graphics.drawable.RippleDrawable}, this will <b>not</b>
 * apply different alphas for pressed, focused, and hovered states and it does not provide a ripple
 * animation for the pressed state.
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class RippleDrawableCompat extends Drawable implements Shapeable, TintAwareDrawable {

  private RippleDrawableCompatState drawableState;

  /**
   * Creates a {@link RippleDrawableCompat} with the given shape that will only draw when enabled
   * and at leaste one of: presssed, focused, or hovered.
   *
   * @param shapeAppearanceModel The shape for the ripple.
   */
  public RippleDrawableCompat(ShapeAppearanceModel shapeAppearanceModel) {
    this(new RippleDrawableCompatState(new MaterialShapeDrawable(shapeAppearanceModel)));
  }

  private RippleDrawableCompat(RippleDrawableCompatState state) {
    super();
    this.drawableState = state;
  }

  @Override
  public void setTint(@ColorInt int tintColor) {
    drawableState.delegate.setTint(tintColor);
  }

  @Override
  public void setTintMode(@Nullable PorterDuff.Mode tintMode) {
    drawableState.delegate.setTintMode(tintMode);
  }

  @Override
  public void setTintList(@Nullable ColorStateList tintList) {
    drawableState.delegate.setTintList(tintList);
  }

  @Override
  public void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    drawableState.delegate.setShapeAppearanceModel(shapeAppearanceModel);
  }

  /**
   * Get the {@link ShapeAppearanceModel} containing the path that will be rendered in this
   * drawable.
   *
   * @return the current model.
   */
  @Override
  @NonNull
  public ShapeAppearanceModel getShapeAppearanceModel() {
    return drawableState.delegate.getShapeAppearanceModel();
  }

  /*
   * This is always stateful as it draws on the canvas only when enabled and (pressed, focused, or
   * hovered).
   */
  @Override
  public boolean isStateful() {
    return true;
  }

  @Override
  protected boolean onStateChange(@NonNull int[] stateSet) {
    boolean changed = super.onStateChange(stateSet);
    if (drawableState.delegate.setState(stateSet)) {
      changed = true;
    }
    boolean shouldDrawRipple = RippleUtils.shouldDrawRippleCompat(stateSet);
    // If shouldDrawRipple is changing, this needs to be redrawn even if the paint / tint values
    // are not changing in order to support setting a ColorStateList with a single color.
    if (drawableState.shouldDrawDelegate != shouldDrawRipple) {
      drawableState.shouldDrawDelegate = shouldDrawRipple;
      changed = true;
    }
    return changed;
  }

  @Override
  public void draw(Canvas canvas) {
    // Only draw the delegate Drawable when enabled and at least one of: pressed, focused, hovered.
    if (drawableState.shouldDrawDelegate) {
      drawableState.delegate.draw(canvas);
    }
  }

  @Override
  protected void onBoundsChange(@NonNull Rect bounds) {
    super.onBoundsChange(bounds);
    drawableState.delegate.setBounds(bounds);
  }

  @Nullable
  @Override
  public ConstantState getConstantState() {
    return drawableState;
  }

  @NonNull
  @Override
  public RippleDrawableCompat mutate() {
    RippleDrawableCompatState newDrawableState = new RippleDrawableCompatState(drawableState);
    drawableState = newDrawableState;
    return this;
  }

  @Override
  public void setAlpha(int alpha) {
    drawableState.delegate.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    drawableState.delegate.setColorFilter(colorFilter);
  }

  @Override
  public int getOpacity() {
    return drawableState.delegate.getOpacity();
  }

  /**
   * A {@link ConstantState} for {@link Ripple}
   *
   */
  static final class RippleDrawableCompatState extends ConstantState {

    @NonNull MaterialShapeDrawable delegate;
    boolean shouldDrawDelegate;

    public RippleDrawableCompatState(MaterialShapeDrawable delegate) {
      this.delegate = delegate;
      this.shouldDrawDelegate = false;
    }

    public RippleDrawableCompatState(@NonNull RippleDrawableCompatState orig) {
      this.delegate = (MaterialShapeDrawable) orig.delegate.getConstantState().newDrawable();
      this.shouldDrawDelegate = orig.shouldDrawDelegate;
    }

    @NonNull
    @Override
    public RippleDrawableCompat newDrawable() {
      return new RippleDrawableCompat(new RippleDrawableCompatState(this));
    }

    @Override
    public int getChangingConfigurations() {
      return 0;
    }
  }
}
