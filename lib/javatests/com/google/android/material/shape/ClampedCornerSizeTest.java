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

import android.graphics.RectF;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link ClampedCornerSize}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class ClampedCornerSizeTest {

  @Test
  public void oversizedCorner_shouldBeClampedToHalfShortestEdge() {
    ClampedCornerSize cornerSize =
        ClampedCornerSize.createFromCornerSize(new AbsoluteCornerSize(100F));

    RectF bounds = new RectF(0F, 0F, 50F, 50F);
    float size = cornerSize.getCornerSize(bounds);

    assertThat(size).isEqualTo(25F);
  }

  @Test
  public void validCornerSize_shouldNotBeAltered() {
    ClampedCornerSize cornerSize =
        ClampedCornerSize.createFromCornerSize(new AbsoluteCornerSize(100F));

    RectF bounds = new RectF(0, 0, 400F, 400F);
    float size = cornerSize.getCornerSize(bounds);

    assertThat(size).isEqualTo(100F);
  }
}
