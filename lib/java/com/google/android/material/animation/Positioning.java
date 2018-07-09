/*
 * Copyright 2017 The Android Open Source Project
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
package com.google.android.material.animation;

/** A representation of the positioning of a view relative to another. */
public class Positioning {

  /** The alignment between the dependency and the child. */
  public final int gravity;
  /**
   * The x adjustment of the child relative to the dependency. Positive values will adjust the child
   * to the right.
   */
  public final float xAdjustment;
  /**
   * The y adjustment of the child relative to the dependency. Positive values will adjust the child
   * to the bottom.
   */
  public final float yAdjustment;

  public Positioning(int gravity, float xAdjustment, float yAdjustment) {
    this.gravity = gravity;
    this.xAdjustment = xAdjustment;
    this.yAdjustment = yAdjustment;
  }
}
