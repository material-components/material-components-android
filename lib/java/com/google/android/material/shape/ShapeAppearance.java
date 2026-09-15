/*
 * Copyright 2025 The Android Open Source Project
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

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

/**
 * Provides a Shape representation modelling corners and edges that is used by {@link
 * MaterialShapeDrawable}. This can be one of {@link ShapeAppearanceModel} or {@link
 * StateListShapeAppearanceModel}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public interface ShapeAppearance {

  /**
   * Returns a new {@link ShapeAppearanceModel} with the same edges and corners as the default
   * shape, but with the corner size for all corners updated.
   */
  @NonNull ShapeAppearanceModel withCornerSize(float cornerSize);

  /**
   * Returns a new {@link ShapeAppearanceModel} with the same edges and corners as the default
   * shape, but with the corner size for all corners updated.
   */
  @NonNull ShapeAppearanceModel withCornerSize(@NonNull CornerSize cornerSize);

  /**
   * Whether or not the ShapeAppearance provides different shapes for states.
   */
  boolean isStateful();

  /**
   * Returns the shape as a {@link ShapeAppearanceModel} of the ShapeAppearance, or the
   * default shape if there are multiple shapes for different states.
   */
  @NonNull ShapeAppearanceModel getDefaultShape();

  /**
   * Returns a {@link ShapeAppearanceModel} for the given state set.
   */
  @NonNull ShapeAppearanceModel getShapeForState(@NonNull int[] stateSet);

  /**
   * Returns a list of the {@link ShapeAppearanceModel} of all states. If this
   * ShapeAppearance is stateless, this will be a list of just the default shape.
   */
  @NonNull ShapeAppearanceModel[] getShapeAppearanceModels();
}
