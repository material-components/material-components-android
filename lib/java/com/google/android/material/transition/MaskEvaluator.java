/*
 * Copyright 2020 The Android Open Source Project
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

package com.google.android.material.transition;

import static androidx.core.util.Preconditions.checkNotNull;
import static com.google.android.material.transition.TransitionUtils.lerp;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Op;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.RequiresApi;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.ShapeAppearancePathProvider;
import com.google.android.material.transition.MaterialContainerTransform.ProgressThresholds;

/**
 * A class which is responsible calculating the path which represents a container transform's
 * transforming container based on a progress between 0 and 1 as well as clipping a canvas to that
 * given path.
 */
@RequiresApi(VERSION_CODES.LOLLIPOP)
class MaskEvaluator {

  private final Path path = new Path();
  private final Path startPath = new Path();
  private final Path endPath = new Path();
  private final ShapeAppearancePathProvider pathProvider = new ShapeAppearancePathProvider();

  /** Update the mask used by this evaluator based on a given progress. */
  void evaluate(
      float progress,
      ShapeAppearanceModel startShapeAppearanceModel,
      ShapeAppearanceModel endShapeAppearanceModel,
      RectF currentStartBounds,
      RectF currentStartBoundsMasked,
      RectF currentEndBoundsMasked,
      ProgressThresholds shapeMaskThresholds) {

    // Animate shape appearance corner changes over range of `progress` & use this when
    // drawing the container background & images
    float shapeStartFraction = checkNotNull(shapeMaskThresholds.start);
    float shapeEndFraction = checkNotNull(shapeMaskThresholds.end);
    ShapeAppearanceModel currentShapeAppearanceModel =
        lerp(
            startShapeAppearanceModel,
            endShapeAppearanceModel,
            currentStartBounds,
            currentEndBoundsMasked,
            shapeStartFraction,
            shapeEndFraction,
            progress);

    pathProvider.calculatePath(currentShapeAppearanceModel, 1, currentStartBoundsMasked, startPath);
    pathProvider.calculatePath(currentShapeAppearanceModel, 1, currentEndBoundsMasked, endPath);

    // Union the two paths on API 23 and above. API 21 and 22 have problems with this
    // call and instead use the start and end paths to clip.
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      path.op(startPath, endPath, Op.UNION);
    }
  }

  /** Clip the given Canvas to the mask held by this evaluator. */
  void clip(Canvas canvas) {
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      canvas.clipPath(path);
    } else {
      canvas.clipPath(startPath);
      canvas.clipPath(endPath, Region.Op.UNION);
    }
  }
}
