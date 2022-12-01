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
import java.util.LinkedHashMap;
import java.util.Map;

// TODO(b/254603377): Use copybara to release material color utilities library directly to github.
/**
 * Creates a dictionary with keys of colors, and values of count of the color
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class QuantizerMap implements Quantizer {
  Map<Integer, Integer> colorToCount;

  @Override
  public QuantizerResult quantize(int[] pixels, int colorCount) {
    final Map<Integer, Integer> pixelByCount = new LinkedHashMap<>();
    for (int pixel : pixels) {
      final Integer currentPixelCount = pixelByCount.get(pixel);
      final int newPixelCount = currentPixelCount == null ? 1 : currentPixelCount + 1;
      pixelByCount.put(pixel, newPixelCount);
    }
    colorToCount = pixelByCount;
    return new QuantizerResult(pixelByCount);
  }

  public Map<Integer, Integer> getColorToCount() {
    return colorToCount;
  }
}
