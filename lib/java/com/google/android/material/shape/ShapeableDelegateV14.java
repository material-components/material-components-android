/*
 * Copyright 2023 The Android Open Source Project
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

package com.google.android.material.shape;

import android.view.View;
import androidx.annotation.NonNull;

/**
 * A {@link ShapeableDelegate} implementation for API 14-20 that always clips using canvas
 * clipping.
 */
class ShapeableDelegateV14 extends ShapeableDelegate {

  @Override
  boolean shouldUseCompatClipping() {
    return true;
  }

  @Override
  void invalidateClippingMethod(@NonNull View view) {
    if (shapeAppearanceModel == null || maskBounds.isEmpty()) {
      return;
    }

    if (shouldUseCompatClipping()) {
      view.invalidate();
    }
  }
}
