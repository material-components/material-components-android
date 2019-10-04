/*
 * Copyright 2019 The Android Open Source Project
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

import android.graphics.RectF;
import androidx.annotation.NonNull;

/** Allows clients to describe the size of a corner independently from a {@link CornerTreatment}. */
public interface CornerSize {
  /** Returns the corner size that should be used given the full bounds of the shape. */
  float getCornerSize(@NonNull RectF bounds);
}
