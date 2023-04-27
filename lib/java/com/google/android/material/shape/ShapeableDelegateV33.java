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

import android.graphics.Outline;
import android.graphics.Path;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewOutlineProvider;
import androidx.annotation.DoNotInline;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * A {@link ShapeableDelegate} for API 33+ that uses {@link ViewOutlineProvider} to clip for all
 * shapes.
 *
 * <p>{@link Outline#setPath(Path)} was added in API 33 and allows using {@link
 * ViewOutlineProvider} to clip for all shapes.
 *
 */
@RequiresApi(VERSION_CODES.TIRAMISU)
class ShapeableDelegateV33 extends ShapeableDelegate {

  ShapeableDelegateV33(@NonNull View view) {
    initMaskOutlineProvider(view);
  }

  @Override
  boolean shouldUseCompatClipping() {
    return forceCompatClippingEnabled;
  }

  @Override
  void invalidateClippingMethod(@NonNull View view) {
    view.setClipToOutline(!shouldUseCompatClipping());
    if (shouldUseCompatClipping()) {
      view.invalidate();
    } else {
      view.invalidateOutline();
    }
  }

  @DoNotInline
  private void initMaskOutlineProvider(View view) {
    view.setOutlineProvider(
        new ViewOutlineProvider() {
          @Override
          public void getOutline(View view, Outline outline) {
            if (!shapePath.isEmpty()) {
              outline.setPath(shapePath);
            }
          }
        });
  }
}
