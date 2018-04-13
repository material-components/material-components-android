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

package android.support.design.button;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Background drawable used by {@link MaterialButton} on API 21+.
 *
 * <p>This class enables a workaround for API 21. On API 21, {@link
 * android.support.v7.widget.AppCompatButton} calls {@link #setColorFilter(ColorFilter)} instead of
 * {@link #setTintList(ColorStateList)}, since certain drawables (e.g. {@link GradientDrawable})
 * don't implement tinting in API 21. However, setting a color filter on the entire {@link
 * RippleDrawable} was resulting in losing our stroke and ripple colors, since they would also be
 * tinted.
 *
 * <p>The workaround in {@link #setColorFilter(ColorFilter)} unwraps the {@link RippleDrawable} and
 * sets the color filter on just the internal {@link GradientDrawable}. This workaround depends on
 * the background drawable structure in {@link MaterialButton} staying consistent, so that we can
 * unwrap the {@link GradientDrawable} properly.
 */
@TargetApi(VERSION_CODES.LOLLIPOP)
class MaterialButtonBackgroundDrawable extends RippleDrawable {

  /**
   * Creates a new ripple drawable with the specified ripple color and optional content and mask
   * drawables.
   *
   * @param color The ripple color
   * @param content The content drawable, may be {@code null}
   * @param mask The mask drawable, may be {@code null}
   */
  MaterialButtonBackgroundDrawable(
      @NonNull ColorStateList color, @Nullable InsetDrawable content, @Nullable Drawable mask) {
    super(color, content, mask);
  }

  @Override
  public void setColorFilter(ColorFilter colorFilter) {
    // TODO: Might need to add special case for Mode.ADD
    if (getDrawable(0) != null) {
      InsetDrawable insetDrawable = (InsetDrawable) getDrawable(0);
      LayerDrawable layerDrawable = (LayerDrawable) insetDrawable.getDrawable();
      GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.getDrawable(0);
      gradientDrawable.setColorFilter(colorFilter);
    }
  }
}
