/*
 * Copyright 2017 The Android Open Source Project
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
package com.google.android.material.animation;

import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Property;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.WeakHashMap;

/**
 * Compat property for {@link Drawable#getAlpha()} and {@link Drawable#setAlpha(int)} for pre-K
 * devices.
 */
public class DrawableAlphaProperty extends Property<Drawable, Integer> {

  /**
   * A compat Property wrapper around {@link Drawable#getAlpha()} and {@link
   * Drawable#setAlpha(int)}.
   */
  public static final Property<Drawable, Integer> DRAWABLE_ALPHA_COMPAT =
      new DrawableAlphaProperty();

  private final WeakHashMap<Drawable, Integer> alphaCache = new WeakHashMap<>();

  private DrawableAlphaProperty() {
    super(Integer.class, "drawableAlphaCompat");
  }

  @Nullable
  @Override
  public Integer get(@NonNull Drawable object) {
    if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
      return object.getAlpha();
    }
    if (alphaCache.containsKey(object)) {
      return alphaCache.get(object);
    }
    return 0xFF;
  }

  @Override
  public void set(@NonNull Drawable object, @NonNull Integer value) {
    if (VERSION.SDK_INT < VERSION_CODES.KITKAT) {
      alphaCache.put(object, value);
    }

    object.setAlpha(value);
  }
}
