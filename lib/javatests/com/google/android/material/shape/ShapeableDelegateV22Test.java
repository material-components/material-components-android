/*
 * Copyright (C) 2023 The Android Open Source Project
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

import static com.google.common.truth.Truth.assertThat;

import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import androidx.annotation.RequiresApi;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link ShapeableDelegateV22}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
@Config(sdk = VERSION_CODES.P)
@RequiresApi(api = VERSION_CODES.LOLLIPOP_MR1)
public class ShapeableDelegateV22Test {

  @Test
  public void singleAxisSymmetricalShape_offsettingDisabled_shouldUseCompatClipping() {
    int viewSize = 500;
    View view = createView(viewSize, viewSize);
    ShapeableDelegateV22 delegate = new ShapeableDelegateV22(view);

    delegate.onShapeAppearanceChanged(view, getSingleAxisSymmetricalShape());
    delegate.onMaskChanged(view, new RectF(0, 0, viewSize, viewSize));

    assertThat(delegate.shouldUseCompatClipping()).isTrue();
  }

  @Test
  public void singleAxisSymmetricalShape_offsettingEnabled_shouldNotUseCompatClipping() {
    int viewSize = 500;
    View view = createView(viewSize, viewSize);
    ShapeableDelegateV22 delegate = new ShapeableDelegateV22(view);

    delegate.onShapeAppearanceChanged(view, getSingleAxisSymmetricalShape());
    delegate.onMaskChanged(view, new RectF(0, 0, viewSize, viewSize));
    delegate.setOffsetZeroCornerEdgeBoundsEnabled(view, true);

    assertThat(delegate.shouldUseCompatClipping()).isFalse();
  }

  @Test
  @SuppressLint("VisibleForTests")
  public void offsetMaskBounds_shouldOffsetLeftByRightEdgeCornerSize() {
    int viewSize = 500;
    View view = createView(viewSize, viewSize);
    ShapeableDelegateV22 delegate = new ShapeableDelegateV22(view);

    ShapeAppearanceModel model =
        new ShapeAppearanceModel.Builder()
            .setTopRightCornerSize(10)
            .setBottomRightCornerSize(10)
            .setTopLeftCornerSize(0)
            .setBottomLeftCornerSize(0)
            .build();
    delegate.onShapeAppearanceChanged(view, model);
    delegate.onMaskChanged(view, new RectF(0, 0, viewSize, viewSize));
    delegate.setOffsetZeroCornerEdgeBoundsEnabled(view, true);

    assertThat(delegate.maskBounds).isEqualTo(new RectF(-10, 0, viewSize, viewSize));
    assertThat(delegate.getCornerRadius()).isEqualTo(10F);
  }

  @Test
  @SuppressLint("VisibleForTests")
  public void offsetMaskBounds_shouldOffsetTopByBottomEdgeCornerSize() {
    int viewSize = 500;
    View view = createView(viewSize, viewSize);
    ShapeableDelegateV22 delegate = new ShapeableDelegateV22(view);

    ShapeAppearanceModel model =
        new ShapeAppearanceModel.Builder()
            .setTopRightCornerSize(0)
            .setBottomRightCornerSize(10)
            .setTopLeftCornerSize(0)
            .setBottomLeftCornerSize(10)
            .build();
    delegate.onShapeAppearanceChanged(view, model);
    delegate.onMaskChanged(view, new RectF(0, 0, viewSize, viewSize));
    delegate.setOffsetZeroCornerEdgeBoundsEnabled(view, true);

    assertThat(delegate.maskBounds).isEqualTo(new RectF(0, -10, viewSize, viewSize));
    assertThat(delegate.getCornerRadius()).isEqualTo(10F);
  }

  @Test
  @SuppressLint("VisibleForTests")
  public void offsetMaskBounds_shouldOffsetRightByLeftEdgeCornerSize() {
    int viewSize = 500;
    View view = createView(viewSize, viewSize);
    ShapeableDelegateV22 delegate = new ShapeableDelegateV22(view);

    ShapeAppearanceModel model =
        new ShapeAppearanceModel.Builder()
            .setTopRightCornerSize(0)
            .setBottomRightCornerSize(0)
            .setTopLeftCornerSize(10)
            .setBottomLeftCornerSize(10)
            .build();
    delegate.onShapeAppearanceChanged(view, model);
    delegate.onMaskChanged(view, new RectF(0, 0, viewSize, viewSize));
    delegate.setOffsetZeroCornerEdgeBoundsEnabled(view, true);

    assertThat(delegate.maskBounds).isEqualTo(new RectF(0, 0, viewSize + 10, viewSize));
    assertThat(delegate.getCornerRadius()).isEqualTo(10F);
  }

  @Test
  @SuppressLint("VisibleForTests")
  public void offsetMaskBounds_shouldOffsetBottomByTopEdgeCornerSize() {
    int viewSize = 500;
    View view = createView(viewSize, viewSize);
    ShapeableDelegateV22 delegate = new ShapeableDelegateV22(view);

    ShapeAppearanceModel model =
        new ShapeAppearanceModel.Builder()
            .setTopRightCornerSize(10)
            .setBottomRightCornerSize(0)
            .setTopLeftCornerSize(10)
            .setBottomLeftCornerSize(0)
            .build();
    delegate.onShapeAppearanceChanged(view, model);
    delegate.onMaskChanged(view, new RectF(0, 0, viewSize, viewSize));
    delegate.setOffsetZeroCornerEdgeBoundsEnabled(view, true);

    assertThat(delegate.maskBounds).isEqualTo(new RectF(0, 0, viewSize, viewSize + 10));
    assertThat(delegate.getCornerRadius()).isEqualTo(10F);
  }

  @Test
  public void asymmetricalShape_shouldUseCompatClipping() {
    int viewSize = 500;
    View view = createView(viewSize, viewSize);
    ShapeableDelegateV22 delegate = new ShapeableDelegateV22(view);

    ShapeAppearanceModel model =
        new ShapeAppearanceModel.Builder()
            .setTopRightCornerSize(10)
            .setBottomRightCornerSize(20)
            .setTopLeftCornerSize(0)
            .setBottomLeftCornerSize(0)
            .build();
    delegate.onShapeAppearanceChanged(view, model);
    delegate.onMaskChanged(view, new RectF(0, 0, viewSize, viewSize));
    delegate.setOffsetZeroCornerEdgeBoundsEnabled(view, true);

    assertThat(delegate.shouldUseCompatClipping()).isTrue();
  }

  @Test
  public void cutCornerShape_shouldUseCompatClipping() {
    int viewSize = 500;
    View view = createView(viewSize, viewSize);
    ShapeableDelegateV22 delegate = new ShapeableDelegateV22(view);

    ShapeAppearanceModel model =
        new ShapeAppearanceModel.Builder()
            .setAllCorners(new CutCornerTreatment())
            .setTopRightCornerSize(10)
            .setBottomRightCornerSize(10)
            .setTopLeftCornerSize(0)
            .setBottomLeftCornerSize(0)
            .build();
    delegate.onShapeAppearanceChanged(view, model);
    delegate.onMaskChanged(view, new RectF(0, 0, viewSize, viewSize));
    delegate.setOffsetZeroCornerEdgeBoundsEnabled(view, true);

    assertThat(delegate.shouldUseCompatClipping()).isTrue();
  }

  private View createView(int width, int height) {
    View view = new View(ApplicationProvider.getApplicationContext());
    view.setLayoutParams(new LayoutParams(width, height));
    view.measure(
        MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
    return view;
  }

  private ShapeAppearanceModel getSingleAxisSymmetricalShape() {
    return new ShapeAppearanceModel.Builder()
        .setTopRightCornerSize(10)
        .setBottomRightCornerSize(10)
        .setTopLeftCornerSize(0)
        .setBottomLeftCornerSize(0)
        .build();
  }
}
