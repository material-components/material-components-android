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

package com.google.android.material.color.utilities;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.RestrictTo;

// TODO(b/254603377): Use copybara to release material color utilities library directly to github.
/**
 * An interface to allow use of different color spaces by quantizers.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public interface PointProvider {
  /** The four components in the color space of an sRGB color. */
  public double[] fromInt(int argb);

  /** The ARGB (i.e. hex code) representation of this color. */
  public int toInt(double[] point);

  /**
   * Squared distance between two colors. Distance is defined by scientific color spaces and
   * referred to as delta E.
   */
  public double distance(double[] a, double[] b);
}
