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

/*
 * NOTE: THIS CLASS IS AUTO-GENERATED FROM THE EQUIVALENT CLASS IN THE PARENT TRANSITION PACKAGE.
 * IT SHOULD NOT BE EDITED DIRECTLY.
 */
package com.google.android.material.transition.platform;

import android.graphics.Path;
import android.graphics.PointF;
import androidx.annotation.NonNull;
import android.transition.PathMotion;

/**
 * A Material {@link PathMotion} that results in a more dramatic curve than {@link
 * android.transition.ArcMotion}.
 *
 * <p>Use MaterialArcMotion in conjunction with {@link MaterialContainerTransform} via {@link
 * MaterialContainerTransform#setPathMotion(PathMotion)} to have the container move along a curved
 * path from its start position to its end position.
 */
@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
public final class MaterialArcMotion extends PathMotion {

  @NonNull
  @Override
  public Path getPath(float startX, float startY, float endX, float endY) {
    Path path = new Path();
    path.moveTo(startX, startY);

    PointF controlPoint = getControlPoint(startX, startY, endX, endY);
    path.quadTo(controlPoint.x, controlPoint.y, endX, endY);
    return path;
  }

  private static PointF getControlPoint(float startX, float startY, float endX, float endY) {
    if (startY > endY) {
      return new PointF(endX, startY);
    } else {
      return new PointF(startX, endY);
    }
  }
}
