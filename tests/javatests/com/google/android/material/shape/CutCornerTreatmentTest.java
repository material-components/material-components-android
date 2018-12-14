/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.google.android.material.shape;

import static org.junit.Assert.assertTrue;

import android.graphics.Matrix;
import android.graphics.Path;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class CutCornerTreatmentTest {

  private static final float DEFAULT_ANGLE = 50;
  private static final float WIDTH = 1080;
  private static final float HEIGHT = 147;

  @Rule
  public final ActivityTestRule<AppCompatActivity> activityTestRule =
      new ActivityTestRule<>(AppCompatActivity.class);

  private final CutCornerTreatment cutCornerTreatment = new CutCornerTreatment(DEFAULT_ANGLE);
  private final ShapePath shapePath = new ShapePath();
  private final Path path = new Path();

  /**
   * Tests that the {@link CutCornerTreatment} doesn't have rounding error which can lead to
   * incorrectly calculating that the path is not convex.
   */
  @Test
  public void hasNoMatrixTransformationRoundingError() {
    ShapePath cornerPath = new ShapePath();
    cutCornerTreatment.getCornerPath(90, 1, cornerPath);
    Matrix edgeTransform = new Matrix();
    edgeTransform.setTranslate(DEFAULT_ANGLE, cornerPath.endY);

    path.moveTo(WIDTH, 0);
    path.lineTo(WIDTH, HEIGHT);
    path.lineTo(0, DEFAULT_ANGLE);
    cornerPath.applyToPath(new Matrix(), path);
    shapePath.reset(0, 0);
    shapePath.lineTo(WIDTH - DEFAULT_ANGLE, 0);
    shapePath.applyToPath(edgeTransform, path);

    assertTrue(path.isConvex());
  }
}
