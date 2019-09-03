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

package com.google.android.material.shape;

import androidx.annotation.NonNull;
import android.util.Log;

/**
 * A basic corner treatment (a single point which does not affect the shape).
 *
 * <p>If you create your own custom corner treatment and want to use {@link
 * ShapeAppearanceModel.Builder#setCornerRadius(float)} which updates the size of the corner in the
 * builder, you will need to override {@link #withSize(float)} to return an instance of your custom
 * corner treatment for the given size.
 *
 * <p>Note: For corner treatments which result in a concave shape, the parent view must disable
 * clipping of children by calling {@link android.view.ViewGroup#setClipChildren(boolean)}, or by
 * setting `android:clipChildren="false"` in xml. `clipToPadding` may also need to be false if there
 * is any padding on the parent that could intersect the shadow.
 */
public class CornerTreatment {

  private static final String TAG = "CornerTreatment";

  private final float cornerSize;

  public CornerTreatment() {
    // Default Constructor has no size. Using this treatment for all corners will draw a square
    this.cornerSize = 0;
  }

  protected CornerTreatment(float cornerSize) {
    // Most CornerTreatments have a concept of corner size. This constructor is exposed for
    // extending classes.
    this.cornerSize = cornerSize;
  }

  /**
   * Generates a {@link ShapePath} for this corner treatment.
   *
   * <p>CornerTreatments are assumed to have an origin of (0, 0) (i.e. they represent the top-left
   * corner), and are automatically rotated and scaled as necessary when applied to other corners.
   *
   * @param angle the angle of the corner, typically 90 degrees.
   * @param interpolation the interpolation of the corner treatment. Ranges between 0 (none) and 1
   *     (fully) interpolated. Custom corner treatments can implement interpolation to support shape
   *     transition between two arbitrary states. Typically, a value of 0 indicates that the custom
   *     corner treatment is not rendered (i.e. that it is a 90 degree angle), and a value of 1
   *     indicates that the treatment is fully rendered. Animation between these two values can
   *     "heal" or "reveal" a corner treatment.
   * @param shapePath the {@link ShapePath} that this treatment should write to.
   */
  public void getCornerPath(float angle, float interpolation, ShapePath shapePath) {}

  public float getCornerSize() {
    return cornerSize;
  }

  /**
   * Returns a new instance of this {@link CornerTreatment} for the given cornerSize. Extending
   * classes should override this method to return an instance of their custom class. This is used
   * by the builder when calling a method to set the size of the corner treatment such as {@link
   * ShapeAppearanceModel.Builder#setCornerRadius(float)}.
   */
  @NonNull
  public CornerTreatment withSize(float cornerSize) {
    return new CornerTreatment(cornerSize);
  }

  /**
   * Checks that the {@link CornerTreatment} returned by calling withSize() is the class we expect.
   */
  @NonNull
  public static CornerTreatment withSizeAndCornerClassCheck(
      @NonNull CornerTreatment treatment, float cornerSize) {
    CornerTreatment updatedTreatment = treatment.withSize(cornerSize);
    if (!updatedTreatment.getClass().equals(treatment.getClass())) {
      Log.w(
          TAG, "CornerTreatments should override withSize() to return an instance of their class");
    }
    return updatedTreatment;
  }
}
