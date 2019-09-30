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

import android.animation.TypeEvaluator;
import android.graphics.Matrix;
import androidx.annotation.NonNull;

/**
 * Type evaluator for {@link Matrix} interpolation. Copied from
 * androidx.transition.TransitionUtils.MatrixEvaluator.
 */
public class MatrixEvaluator implements TypeEvaluator<Matrix> {
  private final float[] tempStartValues = new float[9];
  private final float[] tempEndValues = new float[9];
  private final Matrix tempMatrix = new Matrix();

  @NonNull
  @Override
  public Matrix evaluate(float fraction, @NonNull Matrix startValue, @NonNull Matrix endValue) {
    startValue.getValues(tempStartValues);
    endValue.getValues(tempEndValues);
    for (int i = 0; i < 9; i++) {
      float diff = tempEndValues[i] - tempStartValues[i];
      tempEndValues[i] = tempStartValues[i] + (fraction * diff);
    }
    tempMatrix.setValues(tempEndValues);
    return tempMatrix;
  }
}
