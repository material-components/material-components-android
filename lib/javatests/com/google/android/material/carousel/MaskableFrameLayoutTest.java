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

import static org.mockito.Mockito.verify;

import android.graphics.RectF;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import android.view.View.MeasureSpec;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
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
