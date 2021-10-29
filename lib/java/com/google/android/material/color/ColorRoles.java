/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.google.android.material.color;

import androidx.annotation.ColorInt;

/**
 * Each accent color (primary, secondary and tertiary), is provided as a group of four supplementary
 * color roles with different luminance which can be used in the UI to define emphasis and to
 * provide a greater flexibility in expression.
 */
public final class ColorRoles {

  private final int accent;
  private final int onAccent;
  private final int accentContainer;
  private final int onAccentContainer;

  ColorRoles(
      @ColorInt int accent,
      @ColorInt int onAccent,
      @ColorInt int accentContainer,
      @ColorInt int onAccentContainer) {
    this.accent = accent;
    this.onAccent = onAccent;
    this.accentContainer = accentContainer;
    this.onAccentContainer = onAccentContainer;
  }

  /** Returns the accent color, used as the main color from the color role. */
  @ColorInt
  public int getAccent() {
    return accent;
  }

  /**
   * Returns the on_accent color, used for content such as icons and text on top of the Accent
   * color.
   */
  @ColorInt
  public int getOnAccent() {
    return onAccent;
  }

  /** Returns the accent_container color, used with less emphasis than the accent color. */
  @ColorInt
  public int getAccentContainer() {
    return accentContainer;
  }

  /**
   * Returns the on_accent_container color, used for content such as icons and text on top of the
   * accent_container color.
   */
  @ColorInt
  public int getOnAccentContainer() {
    return onAccentContainer;
  }
}
