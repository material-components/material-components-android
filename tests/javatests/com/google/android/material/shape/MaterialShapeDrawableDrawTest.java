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
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class MaterialShapeDrawableDrawTest {

  private static final int SHAPE_SIZE = 300;

  private final ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel();
  private final MaterialShapeDrawable materialShapeDrawable =
      new MaterialShapeDrawable(shapeAppearanceModel);

  @Before
  public void setMaterialShapeDrawableBounds() {
    materialShapeDrawable.setBounds(0, 0, SHAPE_SIZE, SHAPE_SIZE);
  }

  @Test
  public void createNewDrawable_notRoundRect_pathIsCalculated() {
    shapeAppearanceModel.setTopLeftCorner(CornerFamily.CUT, 10);
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

  private Drawable copyMaterialShapeDrawable() {
    // Do a draw first to simulate copying from a drawable that has been used.
    materialShapeDrawable.draw(new Canvas());
    return materialShapeDrawable.getConstantState().newDrawable();
  }
}
