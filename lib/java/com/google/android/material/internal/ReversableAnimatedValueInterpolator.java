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

import android.animation.TimeInterpolator;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * A {@link TimeInterpolator} that reverses the animated values of the provided source {@link
 * TimeInterpolator}.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ReversableAnimatedValueInterpolator implements TimeInterpolator {

  private final TimeInterpolator sourceInterpolator;

  public ReversableAnimatedValueInterpolator(@NonNull TimeInterpolator sourceInterpolator) {
    this.sourceInterpolator = sourceInterpolator;
  }

  /**
   * A helper method that returns the source {@link TimeInterpolator} unaffected or a reversed
   * version of the source {@link TimeInterpolator}, depending on the value of the {@code
   * useSourceInterpolator} parameter.
   */
  @NonNull
  public static TimeInterpolator of(
      boolean useSourceInterpolator, @NonNull TimeInterpolator sourceInterpolator) {
    if (useSourceInterpolator) {
      return sourceInterpolator;
    } else {
      return new ReversableAnimatedValueInterpolator(sourceInterpolator);
    }
  }

  @Override
  public float getInterpolation(float input) {
    return 1 - sourceInterpolator.getInterpolation(input);
  }
}
