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
import java.util.Map;

// TODO(b/254603377): Use copybara to release material color utilities library directly to github.
/**
 * Represents result of a quantizer run
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class QuantizerResult {
  public final Map<Integer, Integer> colorToCount;

  QuantizerResult(Map<Integer, Integer> colorToCount) {
    this.colorToCount = colorToCount;
  }
}
