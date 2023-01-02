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

import android.animation.TypeEvaluator;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * A {@link TypeEvaluator} that allows for interpolation between two {@link Rect Rect's}.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class RectEvaluator implements TypeEvaluator<Rect> {

  private final Rect rect;

  public RectEvaluator(@NonNull Rect rect) {
    this.rect = rect;
  }

  @Override
  public Rect evaluate(float fraction, @NonNull Rect startValue, @NonNull Rect endValue) {
    int left = startValue.left + (int) ((endValue.left - startValue.left) * fraction);
    int top = startValue.top + (int) ((endValue.top - startValue.top) * fraction);
    int right = startValue.right + (int) ((endValue.right - startValue.right) * fraction);
    int bottom = startValue.bottom + (int) ((endValue.bottom - startValue.bottom) * fraction);
    rect.set(left, top, right, bottom);
    return rect;
  }
}
