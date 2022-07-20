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

package com.google.android.material.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.graphics.drawable.DrawableCompat;
import java.util.Arrays;

/**
 * Utils class for {@link Drawable}.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class DrawableUtils {

  private DrawableUtils() {}

  @Nullable
  public static Drawable createTintableDrawableIfNeeded(
      @Nullable Drawable drawable, @Nullable ColorStateList tintList, @Nullable Mode tintMode) {
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

  /**
   * Composites two drawables, returning a drawable instance of {@link LayerDrawable}, with the
   * second on top of the first. If any of the drawables is null, this method will return the other.
   *
   * @param bottomLayerDrawable the drawable to be on the first layer (bottom)
   * @param topLayerDrawable the drawable to be on the second layer (top)
   */
  @Nullable
  public static Drawable compositeTwoLayeredDrawable(
      @Nullable Drawable bottomLayerDrawable, @Nullable Drawable topLayerDrawable) {
    if (bottomLayerDrawable == null) {
      return topLayerDrawable;
    }
    if (topLayerDrawable == null) {
      return bottomLayerDrawable;
    }
    LayerDrawable drawable =
        new LayerDrawable(new Drawable[] {bottomLayerDrawable, topLayerDrawable});
    int topLayerNewWidth;
    int topLayerNewHeight;
    if (topLayerDrawable.getIntrinsicWidth() == -1 || topLayerDrawable.getIntrinsicHeight() == -1) {
      // If there's no intrinsic width or height, keep bottom layer's size.
      topLayerNewWidth = bottomLayerDrawable.getIntrinsicWidth();
      topLayerNewHeight = bottomLayerDrawable.getIntrinsicHeight();
    } else if (topLayerDrawable.getIntrinsicWidth() <= bottomLayerDrawable.getIntrinsicWidth()
        && topLayerDrawable.getIntrinsicHeight() <= bottomLayerDrawable.getIntrinsicHeight()) {
      // If the top layer is smaller than the bottom layer in both its width and height, keep top
      // layer's size.
      topLayerNewWidth = topLayerDrawable.getIntrinsicWidth();
      topLayerNewHeight = topLayerDrawable.getIntrinsicHeight();
    } else {
      float topLayerRatio =
          (float) topLayerDrawable.getIntrinsicWidth() / topLayerDrawable.getIntrinsicHeight();
      float bottomLayerRatio =
          (float) bottomLayerDrawable.getIntrinsicWidth()
              / bottomLayerDrawable.getIntrinsicHeight();
      if (topLayerRatio >= bottomLayerRatio) {
        // If the top layer is wider in ratio than the bottom layer, shrink it according to its
        // width.
        topLayerNewWidth = bottomLayerDrawable.getIntrinsicWidth();
        topLayerNewHeight = (int) (topLayerNewWidth / topLayerRatio);
      } else {
        // If the top layer is taller in ratio than the bottom layer, shrink it according to its
        // height.
        topLayerNewHeight = bottomLayerDrawable.getIntrinsicHeight();
        topLayerNewWidth = (int) (topLayerRatio * topLayerNewHeight);
      }
    }
    // Centers the top layer inside the bottom layer. Before M there's no layer gravity support, we
    // need to use layer insets to adjust the top layer position manually.
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      drawable.setLayerSize(1, topLayerNewWidth, topLayerNewHeight);
      drawable.setLayerGravity(1, Gravity.CENTER);
    } else {
      int horizontalInset = (bottomLayerDrawable.getIntrinsicWidth() - topLayerNewWidth) / 2;
      int verticalInset = (bottomLayerDrawable.getIntrinsicHeight() - topLayerNewHeight) / 2;
      drawable.setLayerInset(1, horizontalInset, verticalInset, horizontalInset, verticalInset);
    }
    return drawable;
  }

  /** Returns a new state that adds the checked state to the input state. */
  @NonNull
  public static int[] getCheckedState(@NonNull int[] state) {
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

  /** Returns a new state that removes the checked state from the input state. */
  @NonNull
  public static int[] getUncheckedState(@NonNull int[] state) {
    int[] newState = new int[state.length];
    int i = 0;
    for (int subState : state) {
      if (subState != android.R.attr.state_checked) {
        newState[i++] = subState;
      }
    }
    return newState;
  }
}
