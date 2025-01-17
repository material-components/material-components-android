/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.drawable;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import androidx.appcompat.graphics.drawable.DrawableWrapperCompat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

/**
 * An extension of {@link DrawableWrapperCompat} that will take a given Drawable and scale it by the
 * specified width and height.
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class ScaledDrawableWrapper extends DrawableWrapperCompat {
  private ScaledDrawableWrapperState state;
  private boolean mutated;

  public ScaledDrawableWrapper(@NonNull Drawable drawable, int width, int height) {
    super(drawable);
    state = new ScaledDrawableWrapperState(getConstantStateFrom(drawable), width, height);
  }

  @Nullable
  private ConstantState getConstantStateFrom(@Nullable Drawable drawable) {
    return drawable != null ? drawable.getConstantState() : null;
  }

  @Override
  public int getIntrinsicWidth() {
    return state.width;
  }

  @Override
  public int getIntrinsicHeight() {
    return state.height;
  }

  @Override
  public void setDrawable(@Nullable Drawable drawable) {
    super.setDrawable(drawable);

    if (state != null) {
      state.wrappedDrawableState = getConstantStateFrom(drawable);
      mutated = false;
    }
  }

  @Nullable
  @Override
  public ConstantState getConstantState() {
    return state.canConstantState() ? state : null;
  }

  @NonNull
  @Override
  public Drawable mutate() {
    if (!mutated && super.mutate() == this) {
      Drawable drawable = getDrawable();
      if (drawable != null) {
        drawable.mutate();
      }

      state =
          new ScaledDrawableWrapperState(getConstantStateFrom(drawable), state.width, state.height);
      mutated = true;
    }

    return this;
  }

  private static final class ScaledDrawableWrapperState extends ConstantState {
    private ConstantState wrappedDrawableState;
    private final int width;
    private final int height;

    ScaledDrawableWrapperState(
        @Nullable ConstantState wrappedDrawableState, int width, int height) {
      this.wrappedDrawableState = wrappedDrawableState;
      this.width = width;
      this.height = height;
    }

    @NonNull
    @Override
    public Drawable newDrawable() {
      Drawable newWrappedDrawable = wrappedDrawableState.newDrawable();
      return new ScaledDrawableWrapper(newWrappedDrawable, width, height);
    }

    @NonNull
    @Override
    public Drawable newDrawable(@Nullable Resources res) {
      Drawable newWrappedDrawable = wrappedDrawableState.newDrawable(res);
      return new ScaledDrawableWrapper(newWrappedDrawable, width, height);
    }

    @NonNull
    @Override
    public Drawable newDrawable(@Nullable Resources res, @Nullable Resources.Theme theme) {
      Drawable newWrappedDrawable = wrappedDrawableState.newDrawable(res, theme);
      return new ScaledDrawableWrapper(newWrappedDrawable, width, height);
    }

    @Override
    public int getChangingConfigurations() {
      return wrappedDrawableState != null ? wrappedDrawableState.getChangingConfigurations() : 0;
    }

    boolean canConstantState() {
      return wrappedDrawableState != null;
    }
  }
}
