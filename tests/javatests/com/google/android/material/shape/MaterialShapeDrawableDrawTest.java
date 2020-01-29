/*
 * Copyright (C) 2019 The Android Open Source Project
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

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class MaterialShapeDrawableDrawTest {

  private static final int SHAPE_SIZE = 300;

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private final MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();

  @Before
  public void setMaterialShapeDrawableBounds() {
    materialShapeDrawable.setBounds(0, 0, SHAPE_SIZE, SHAPE_SIZE);
  }

  @Test
  public void createNewDrawable_notRoundRect_pathIsCalculated() {
    ShapeAppearanceModel shapeAppearanceModel =
        ShapeAppearanceModel.builder().setTopLeftCorner(CornerFamily.CUT, 10).build();
    materialShapeDrawable.setShapeAppearanceModel(shapeAppearanceModel);
    Drawable copy = copyMaterialShapeDrawable();

    Canvas canvasMock = mock(Canvas.class);
    copy.draw(canvasMock);
    ArgumentCaptor<Path> pathArgument = ArgumentCaptor.forClass(Path.class);
    verify(canvasMock).drawPath(pathArgument.capture(), any(Paint.class));

    assertFalse("Trying to draw but path is empty", pathArgument.getValue().isEmpty());
  }

  @Test
  public void newDrawableFromContstantState() {
    Drawable copy = copyMaterialShapeDrawable();

    copy.draw(new Canvas());
  }

  @Test
  public void createNewDrawable_withStroke() {
    materialShapeDrawable.setStroke(10, 0);
    Drawable copy = copyMaterialShapeDrawable();

    copy.draw(new Canvas());
  }

  @Test
  public void createNewDrawable_withFill() {
    materialShapeDrawable.setFillColor(ColorStateList.valueOf(0));
    Drawable copy = copyMaterialShapeDrawable();

    copy.draw(new Canvas());
  }

  @Test
  public void canDrawNewDrawableFromConstantState_withFill() {
    materialShapeDrawable.setFillColor(ColorStateList.valueOf(0));
    Drawable copy = copyMaterialShapeDrawable();

    copy.draw(new Canvas());
  }

  @Test
  public void testInfinitelyLargeEdge_throwsExceptionWhileDrawingShadow() {
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
      // This test crashes the emulator, so lets skip it.
      return;
    }

    materialShapeDrawable.setElevation(5.0f);
    materialShapeDrawable.setShadowCompatibilityMode(
        MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS);
    materialShapeDrawable.setShapeAppearanceModel(
        ShapeAppearanceModel.builder()
            .setTopEdge(new TriangleEdgeTreatment(Float.POSITIVE_INFINITY, true))
            .build());

    thrown.expect(RuntimeException.class);
    materialShapeDrawable.draw(new Canvas());
  }

  private Drawable copyMaterialShapeDrawable() {
    // Do a draw first to simulate copying from a drawable that has been used.
    materialShapeDrawable.draw(new Canvas());
    return materialShapeDrawable.getConstantState().newDrawable();
  }
}
