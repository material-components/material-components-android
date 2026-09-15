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
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.appcompat.widget.TooltipCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.graphics.drawable.DrawableCompat;
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
    boolean iconClickable = iconView.hasOnClickListeners();
    boolean iconLongClickable = onLongClickListener != null;
    boolean iconFocusable = iconClickable || iconLongClickable;
    iconView.setFocusable(iconFocusable);
    iconView.setClickable(iconClickable);
    iconView.setPressable(iconClickable);
    // Pre-O, the tooltip is set via a long-click listener. If we have a custom OnClickListener but
    // no custom OnLongClickListener, do not set the view to not be long-clickable, so that the
    // tooltip can be shown.
    if (VERSION.SDK_INT >= VERSION_CODES.O || !iconFocusable || iconLongClickable) {
      iconView.setLongClickable(iconLongClickable);
    }
    iconView.setImportantForAccessibility(
        iconFocusable
            ? View.IMPORTANT_FOR_ACCESSIBILITY_YES
            : View.IMPORTANT_FOR_ACCESSIBILITY_NO);
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
        icon.setTintList(ColorStateList.valueOf(color));
      } else {
        icon.setTintList(iconTintList);
      }
      if (iconTintMode != null) {
        icon.setTintMode(iconTintMode);
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
    icon.setTintList(ColorStateList.valueOf(color));
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
    if (VERSION.SDK_INT < VERSION_CODES.M) {
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

  /**
   * Updates the tooltip for an icon, handling API-level-specific behavior.
   *
   * <p>The tooltip is only set if the icon is focusable.
   *
   * <p>On API 26 and above, this method calls {@link
   * android.view.View#setTooltipText(CharSequence)}. This is safe to use even with a custom {@link
   * OnLongClickListener}.
   *
   * <p>On API levels below 26, this method uses {@link TooltipCompat#setTooltipText(View,
   * CharSequence)}, but only if a custom {@link OnLongClickListener} has not been set. This is to
   * avoid overwriting a developer-provided long-press listener. Thus, a custom {@link
   * OnLongClickListener} will override the tooltip.
   */
  static void updateIconTooltip(
      @NonNull CheckableImageButton iconView,
      @Nullable OnLongClickListener onLongClickListener,
      @Nullable CharSequence tooltip) {
    final CharSequence tooltipText = iconView.isFocusable() ? tooltip : null;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      iconView.setTooltipText(tooltipText);
    } else if (onLongClickListener == null) {
      TooltipCompat.setTooltipText(iconView, tooltipText);
    }
  }
}
