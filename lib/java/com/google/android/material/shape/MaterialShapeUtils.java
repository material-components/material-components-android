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

package com.google.android.material.shape;

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

/** Utility methods for {@link MaterialShapeDrawable} and related classes. */
@RestrictTo(Scope.LIBRARY_GROUP)
public class MaterialShapeUtils {

  static CornerTreatment createCornerTreatment(@CornerFamily int cornerFamily, int cornerSize) {
    switch (cornerFamily) {
      case CornerFamily.ROUNDED:
        return new RoundedCornerTreatment(cornerSize);
      case CornerFamily.CUT:
        return new CutCornerTreatment(cornerSize);
      default:
        return createDefaultCornerTreatment();
    }
  }

  static CornerTreatment createDefaultCornerTreatment() {
    return new RoundedCornerTreatment(0);
  }

  static EdgeTreatment createDefaultEdgeTreatment() {
    return new EdgeTreatment();
  }
}
