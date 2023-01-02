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

package com.google.android.material.textfield;

import static com.google.android.material.internal.ViewUtils.dpToPx;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import com.google.android.material.internal.CheckableImageButton;
import com.google.android.material.ripple.RippleUtils;
import java.util.Arrays;

class IconHelper {

  private IconHelper() {}

  static void setIconOnClickListener(
      @NonNull CheckableImageButton iconView,
      @Nullable OnClickListener onClickListener,
      @Nullable OnLongClickListener onLongClickListener) {
    iconView.setOnClickListener(onClickListener);
    setIconClickable(iconView, onLongClickListener);
  }

  static void setIconOnLongClickListener(
      @NonNull CheckableImageButton iconView, @Nullable OnLongClickListener onLongClickListener) {
    iconView.setOnLongClickListener(onLongClickListener);
    setIconClickable(iconView, onLongClickListener);
  }

  private static void setIconClickable(
      @NonNull CheckableImageButton iconView, @Nullable OnLongClickListener onLongClickListener) {
    boolean iconClickable = ViewCompat.hasOnClickListeners(iconView);
    boolean iconLongClickable = onLongClickListener != null;
    boolean iconFocusable = iconClickable || iconLongClickable;
    iconView.setFocusable(iconFocusable);
    iconView.setClickable(iconClickable);
    iconView.setPressable(iconClickable);
    iconView.setLongClickable(iconLongClickable);
    ViewCompat.setImportantForAccessibility(
        iconView,
        iconFocusable
            ? ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES
            : ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
  }

  /**
   * Applies the given icon tint according to the merged view state of the host text input layout
   * and the icon view.
   */
  static void applyIconTint(
      @NonNull TextInputLayout textInputLayout,
      @NonNull CheckableImageButton iconView,
      ColorStateList iconTintList,
      PorterDuff.Mode iconTintMode) {
    Drawable icon = iconView.getDrawable();
    if (icon != null) {
      icon = DrawableCompat.wrap(icon).mutate();
      if (iconTintList != null && iconTintList.isStateful()) {
        // Make sure the right color for the current state is applied.
        int color =
            iconTintList.getColorForState(
                mergeIconState(textInputLayout, iconView), iconTintList.getDefaultColor());
        DrawableCompat.setTintList(icon, ColorStateList.valueOf(color));
      } else {
        DrawableCompat.setTintList(icon, iconTintList);
      }
      if (iconTintMode != null) {
        DrawableCompat.setTintMode(icon, iconTintMode);
      }
    }

    if (iconView.getDrawable() != icon) {
      iconView.setImageDrawable(icon);
    }
  }

  /**
   * Refresh the icon tint according to the new drawable state.
   */
  static void refreshIconDrawableState(
      @NonNull TextInputLayout textInputLayout,
      @NonNull CheckableImageButton iconView,
      ColorStateList colorStateList) {
    Drawable icon = iconView.getDrawable();
    if (iconView.getDrawable() == null || colorStateList == null || !colorStateList.isStateful()) {
      return;
    }

    int color =
        colorStateList.getColorForState(
            mergeIconState(textInputLayout, iconView), colorStateList.getDefaultColor());

    icon = DrawableCompat.wrap(icon).mutate();
    DrawableCompat.setTintList(icon, ColorStateList.valueOf(color));
    iconView.setImageDrawable(icon);
  }

  private static int[] mergeIconState(
      @NonNull TextInputLayout textInputLayout,
      @NonNull CheckableImageButton iconView) {
    int[] textInputStates = textInputLayout.getDrawableState();
    int[] iconStates = iconView.getDrawableState();

    int index = textInputStates.length;
    int[] states = Arrays.copyOf(textInputStates, textInputStates.length + iconStates.length);

    System.arraycopy(iconStates, 0, states, index, iconStates.length);

    return states;
  }

  static void setCompatRippleBackgroundIfNeeded(@NonNull CheckableImageButton iconView) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP
        && VERSION.SDK_INT <= VERSION_CODES.LOLLIPOP_MR1) {
      // Note that this is aligned with ?attr/actionBarItemBackground on API 23+, which sets ripple
      // radius to 20dp. Therefore we set the padding here to (48dp [view size] - 20dp * 2) / 2.
      iconView.setBackground(
          RippleUtils.createOvalRippleLollipop(
              iconView.getContext(), (int) dpToPx(iconView.getContext(), 4)));
    }
  }

  /** Sets the minimum size for the icon. */
  static void setIconMinSize(@NonNull CheckableImageButton iconView, @Px int iconSize) {
    iconView.setMinimumWidth(iconSize);
    iconView.setMinimumHeight(iconSize);
  }

  static void setIconScaleType(
      @NonNull CheckableImageButton iconView, @NonNull ImageView.ScaleType scaleType) {
    iconView.setScaleType(scaleType);
  }

  static ImageView.ScaleType convertScaleType(int scaleType) {
    switch (scaleType) {
      case 0:
        return ImageView.ScaleType.FIT_XY;
      case 1:
        return ImageView.ScaleType.FIT_START;
      case 2:
        return ImageView.ScaleType.FIT_CENTER;
      case 3:
        return ImageView.ScaleType.FIT_END;
      case 5:
        return ImageView.ScaleType.CENTER_CROP;
      case 6:
        return ImageView.ScaleType.CENTER_INSIDE;
      default:
        return ImageView.ScaleType.CENTER;
    }
  }
}
