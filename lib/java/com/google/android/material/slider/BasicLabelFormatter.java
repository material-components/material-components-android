/*
 * Copyright 2020 The Android Open Source Project
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

package com.google.android.material.slider;

import androidx.annotation.NonNull;
import java.util.Locale;

/**
 * A simple implementation of the {@link LabelFormatter} interface, that limits the number
 * displayed inside a discrete slider's bubble to three digits, and a single-character suffix that
 * denotes magnitude (e.g.: 1.5K, 2.2M, 1.3B, 2T).
 */
public final class BasicLabelFormatter implements LabelFormatter {

  private static final long TRILLION = 1000000000000L;
  private static final int BILLION = 1000000000;
  private static final int MILLION = 1000000;
  private static final int THOUSAND = 1000;

  @NonNull
  @Override
  public String getFormattedValue(float value) {
    if (value >= TRILLION) {
      return String.format(Locale.US, "%.1fT", value / TRILLION);
    } else if (value >= BILLION) {
      return String.format(Locale.US, "%.1fB", value / BILLION);
    } else if (value >= MILLION) {
      return String.format(Locale.US, "%.1fM", value / MILLION);
    } else if (value >= THOUSAND) {
      return String.format(Locale.US, "%.1fK", value / THOUSAND);
    }

    return String.format(Locale.US, "%.0f", value);
  }
}
