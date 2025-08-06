/*
 * Copyright 2022 The Android Open Source Project
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

package com.google.android.material.carousel;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;

import android.graphics.RectF;
import android.os.Build.VERSION_CODES;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import android.view.View.MeasureSpec;
import androidx.annotation.RequiresApi;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.shape.AbsoluteCornerSize;
import com.google.android.material.shape.ClampedCornerSize;
import com.google.android.material.shape.CornerSize;
import com.google.android.material.shape.CutCornerTreatment;
import com.google.android.material.shape.ShapeAppearanceModel;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link MaskableFrameLayout}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class MaskableFrameLayoutTest {

  @Rule public final MockitoRule mocks = MockitoJUnit.rule();

  @Mock private OnMaskChangedListener listener;

  @Test
  public void testSetMaskXPercentage_shouldTriggerMaskChangedListeners() {
    MaskableFrameLayout maskableFrameLayout = createMaskableFrameLayoutWithSize(100, 100);
    maskableFrameLayout.setOnMaskChangedListener(listener);

    maskableFrameLayout.setMaskXPercentage(.5F);

    RectF expected = new RectF(25F, 0F, 75F, 100F);
    verify(listener).onMaskChanged(expected);
  }

  @Test
  public void testShapeAppearanceWithAbsoluteCornerSizes_shouldBeClamped() {
    MaskableFrameLayout maskableFrameLayout = createMaskableFrameLayoutWithSize(50, 50);

    ShapeAppearanceModel model =
        new ShapeAppearanceModel.Builder().setAllCornerSizes(new AbsoluteCornerSize(200F)).build();
    maskableFrameLayout.setShapeAppearanceModel(model);
    maskableFrameLayout.setMaskRectF(new RectF(0F, 0F, 50F, 50F));
    CornerSize topRightCornerSize =
        maskableFrameLayout.getShapeAppearanceModel().getTopRightCornerSize();

    assertThat(topRightCornerSize).isInstanceOf(ClampedCornerSize.class);
    assertThat(topRightCornerSize.getCornerSize(maskableFrameLayout.getMaskRectF())).isEqualTo(25F);
  }

  @RequiresApi(api = VERSION_CODES.LOLLIPOP_MR1)
  @Config(sdk = VERSION_CODES.M)
  @Test
  public void testForceCompatClipping_shouldNotUseViewOutlineProvider() {
    MaskableFrameLayout maskableFrameLayout = createMaskableFrameLayoutWithSize(50, 50);
    ShapeAppearanceModel model = new ShapeAppearanceModel.Builder().setAllCornerSizes(10F).build();
    maskableFrameLayout.setMaskRectF(new RectF(0F, 0F, 50F, 50F));
    maskableFrameLayout.setShapeAppearanceModel(model);

    assertThat(maskableFrameLayout.getClipToOutline()).isTrue();
    maskableFrameLayout.setForceCompatClipping(true);
    assertThat(maskableFrameLayout.getClipToOutline()).isFalse();
  }

  @RequiresApi(api = VERSION_CODES.LOLLIPOP_MR1)
  @Config(sdk = VERSION_CODES.M)
  @Test
  public void testRoundedCornersApi22_usesViewOutlineProvider() {
    MaskableFrameLayout maskableFrameLayout = createMaskableFrameLayoutWithSize(50, 50);
    ShapeAppearanceModel model = new ShapeAppearanceModel.Builder().setAllCornerSizes(10F).build();
    maskableFrameLayout.setShapeAppearanceModel(model);
    maskableFrameLayout.setMaskRectF(new RectF(0F, 0F, 50F, 50F));

    assertThat(maskableFrameLayout.getClipToOutline()).isTrue();
  }

  @RequiresApi(api = VERSION_CODES.LOLLIPOP_MR1)
  @Config(sdk = VERSION_CODES.M)
  @Test
  public void testCutCornersApi22_doesNotUseViewOutlineProvider() {
    MaskableFrameLayout maskableFrameLayout = createMaskableFrameLayoutWithSize(50, 50);
    ShapeAppearanceModel model =
        new ShapeAppearanceModel.Builder()
            .setAllCornerSizes(10F)
            .setAllCorners(new CutCornerTreatment())
            .build();
    maskableFrameLayout.setShapeAppearanceModel(model);

    assertThat(maskableFrameLayout.getClipToOutline()).isFalse();
  }

  @RequiresApi(api = VERSION_CODES.TIRAMISU)
  @Config(sdk = VERSION_CODES.TIRAMISU)
  @Test
  public void testCutCornersApi33_usesViewOutlineProvider() {
    MaskableFrameLayout maskableFrameLayout = createMaskableFrameLayoutWithSize(50, 50);
    ShapeAppearanceModel model =
        new ShapeAppearanceModel.Builder()
            .setAllCornerSizes(10F)
            .setAllCorners(new CutCornerTreatment())
            .build();
    maskableFrameLayout.setShapeAppearanceModel(model);

    assertThat(maskableFrameLayout.getClipToOutline()).isTrue();
  }

  private static MaskableFrameLayout createMaskableFrameLayoutWithSize(int width, int height) {
    MaskableFrameLayout maskableFrameLayout =
        new MaskableFrameLayout(ApplicationProvider.getApplicationContext());
    maskableFrameLayout.setLayoutParams(new LayoutParams(width, height));
    maskableFrameLayout.measure(
        MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    maskableFrameLayout.layout(
        0, 0, maskableFrameLayout.getMeasuredWidth(), maskableFrameLayout.getMeasuredHeight());
    return maskableFrameLayout;
  }
}
