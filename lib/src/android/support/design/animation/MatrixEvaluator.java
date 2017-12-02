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
package android.support.design.animation;

import android.animation.TypeEvaluator;
import android.graphics.Matrix;

/**
 * Type evaluator for {@link Matrix} interpolation. Copied from
 * android.support.transition.TransitionUtils.MatrixEvaluator.
 */
public class MatrixEvaluator implements TypeEvaluator<Matrix> {
  private final float[] mTempStartValues = new float[9];
  private final float[] mTempEndValues = new float[9];
  private final Matrix mTempMatrix = new Matrix();

  @Override
  public Matrix evaluate(float fraction, Matrix startValue, Matrix endValue) {
    startValue.getValues(mTempStartValues);
    endValue.getValues(mTempEndValues);
    for (int i = 0; i < 9; i++) {
      float diff = mTempEndValues[i] - mTempStartValues[i];
      mTempEndValues[i] = mTempStartValues[i] + (fraction * diff);
    }
    mTempMatrix.setValues(mTempEndValues);
    return mTempMatrix;
  }
}
