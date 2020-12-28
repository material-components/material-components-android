/*
 * Copyright 2019 The Android Open Source Project
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

import androidx.annotation.NonNull;

/**
 * Provides a mechanism to uniformly modify the {@link ShapeAppearanceModel} that backs a
 * component or {@link android.graphics.drawable.Drawable}'s shape.
 */
public interface Shapeable {

  /**
   * Sets the {@link ShapeAppearanceModel} that defines the shape.
   */
  void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel);

  /**
   * Returns the {@link ShapeAppearanceModel} used for the shape definition.
   *
   * <p>This {@link ShapeAppearanceModel} can be modified to change the shape.
   */
  @NonNull
  ShapeAppearanceModel getShapeAppearanceModel();
}
