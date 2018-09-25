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

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Path;
import android.support.v7.app.AppCompatActivity;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class MaterialShapeDrawableOutlineTest {

  private static final float DEFAULT_ANGLE = 50;
  private static final int SHAPE_SIZE = 300;

  @Rule
  public final ActivityTestRule<AppCompatActivity> activityTestRule =
      new ActivityTestRule<>(AppCompatActivity.class);

  private final ShapePathModel shapePathModel = new ShapePathModel();
  private final MaterialShapeDrawable materialShapeDrawable =
      new MaterialShapeDrawable(shapePathModel);
  private final Outline outline = new Outline();

  @Before
  public void setMaterialShapeDrawableBounds() {
    materialShapeDrawable.setBounds(0, 0, SHAPE_SIZE, SHAPE_SIZE);
  }

  @Test
  public void roundedCorners_isConvex() throws Exception {
    shapePathModel.setAllCorners(new RoundedCornerTreatment(DEFAULT_ANGLE));
    materialShapeDrawable.draw(new Canvas());

    materialShapeDrawable.getOutline(outline);
    assertPathIsConvex(outline);
  }

  @Test
  public void cutCorners_isConvex() throws Exception {
    shapePathModel.setAllCorners(new CutCornerTreatment(DEFAULT_ANGLE));
    materialShapeDrawable.draw(new Canvas());

    materialShapeDrawable.getOutline(outline);

    assertPathIsConvex(outline);
  }

  @Test
  public void asymmetricalCorners_isConvex() throws Exception {
    shapePathModel.setTopLeftCorner(new RoundedCornerTreatment(DEFAULT_ANGLE));
    shapePathModel.setTopRightCorner(new CutCornerTreatment(DEFAULT_ANGLE));
    shapePathModel.setBottomRightCorner(new RoundedCornerTreatment(DEFAULT_ANGLE));
    shapePathModel.setBottomLeftCorner(new CutCornerTreatment(DEFAULT_ANGLE));
    materialShapeDrawable.draw(new Canvas());

    materialShapeDrawable.getOutline(outline);

    assertPathIsConvex(outline);
  }

  @Test
  public void asymmetricalCornersAlternate_isConvex() throws Exception {
    shapePathModel.setTopLeftCorner(new CutCornerTreatment(DEFAULT_ANGLE));
    shapePathModel.setTopRightCorner(new RoundedCornerTreatment(DEFAULT_ANGLE));
    shapePathModel.setBottomRightCorner(new CutCornerTreatment(DEFAULT_ANGLE));
    shapePathModel.setBottomLeftCorner(new RoundedCornerTreatment(DEFAULT_ANGLE));
    materialShapeDrawable.draw(new Canvas());

    materialShapeDrawable.getOutline(outline);

    assertPathIsConvex(outline);
  }

  private static void assertPathIsConvex(Outline outline) throws Exception {
    Path path = getPath(outline);
    assertNotNull(
        "MaterialShapeDrawable didn't set path on the outline, so it must be convex.", path);
    assertThat(path.isConvex(), is(true));
  }

  private static Path getPath(Outline outline) throws Exception {
    return (Path) Outline.class.getDeclaredField("mPath").get(outline);
  }
}
