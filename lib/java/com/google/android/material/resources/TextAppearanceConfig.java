/*
 * Copyright 2018 The Android Open Source Project
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

package com.google.android.material.resources;

import com.google.android.material.R;

/** Utility for configuring TextAppearance. */
public class TextAppearanceConfig {

  private static boolean shouldLoadFontSynchronously;

  /**
   * Specifies whether font resources should be loaded synchronously. By default, they are loaded
   * asynchronously to avoid ANR. Preload font resources and set this to true in emulator /
   * instrumentation tests to avoid flakiness.
   */
  public static void setShouldLoadFontSynchronously(boolean flag) {
    shouldLoadFontSynchronously = flag;
  }

  /** Returns flag indicating whether font resources should be loaded synchronously. */
  public static boolean shouldLoadFontSynchronously() {
    return shouldLoadFontSynchronously;
  }
}
