/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.carousel;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link Arrangement}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public final class ArrangementTest {

  @Test
  public void test1L1M1S_noAdjustmentsMade() {
    float targetSmallSize = 56F;
    float targetLargeSize = 56F * 3F;
    float targetMediumSize = (targetLargeSize + targetSmallSize) / 2F;
    Arrangement arrangement =
        new Arrangement(
            /* priority= */ 1,
            /* targetSmallSize= */ targetSmallSize,
            /* minSmallSize= */ 40F,
            /* maxSmallSize= */ 56F,
            /* smallCount= */ 1,
            /* targetMediumSize= */ targetMediumSize,
            /* mediumCount= */ 1,
            /* targetLargeSize= */ targetLargeSize,
            /* largeCount= */ 1,
            /* availableSpace= */ targetLargeSize + targetMediumSize + targetSmallSize);

    assertThat(arrangement.largeSize).isEqualTo(targetLargeSize);
    assertThat(arrangement.mediumSize).isEqualTo(targetMediumSize);
    assertThat(arrangement.smallSize).isEqualTo(targetSmallSize);
  }

  @Test
  public void test1L1M1S_decreasesSmallSize() {
    float targetSmallSize = 56F;
    float targetLargeSize = 56F * 3F;
    float targetMediumSize = (targetLargeSize + targetSmallSize) / 2F;
    Arrangement arrangement =
        new Arrangement(
            /* priority= */ 1,
            /* targetSmallSize= */ targetSmallSize,
            /* minSmallSize= */ 40F,
            /* maxSmallSize= */ 56F,
            /* smallCount= */ 1,
            /* targetMediumSize= */ targetMediumSize,
            /* mediumCount= */ 1,
            /* targetLargeSize= */ targetLargeSize,
            /* largeCount= */ 1,
            /* availableSpace= */ targetLargeSize + targetMediumSize + targetSmallSize - 10F);

    assertThat(arrangement.largeSize).isEqualTo(targetLargeSize);
    assertThat(Math.round(arrangement.mediumSize)).isEqualTo(Math.round(targetMediumSize));
    assertThat(arrangement.smallSize).isEqualTo(targetSmallSize - 10F);
  }

  @Test
  public void test1L1M1S_increasesSmallSize() {
    float targetSmallSize = 40F;
    float targetLargeSize = 40F * 3F;
    float targetMediumSize = (targetLargeSize + targetSmallSize) / 2F;
    Arrangement arrangement =
        new Arrangement(
            /* priority= */ 1,
            /* targetSmallSize= */ targetSmallSize,
            /* minSmallSize= */ 40F,
            /* maxSmallSize= */ 56F,
            /* smallCount= */ 1,
            /* targetMediumSize= */ targetMediumSize,
            /* mediumCount= */ 1,
            /* targetLargeSize= */ targetLargeSize,
            /* largeCount= */ 1,
            /* availableSpace= */ targetLargeSize + targetMediumSize + targetSmallSize + 10F);

    assertThat(arrangement.largeSize).isEqualTo(targetLargeSize);
    assertThat(Math.round(arrangement.mediumSize)).isEqualTo(Math.round(targetMediumSize));
    assertThat(arrangement.smallSize).isEqualTo(targetSmallSize + 10F);
  }

  @Test
  public void test1L1M1S_decreasesMediumSize() {
    float targetSmallSize = 40F;
    float targetLargeSize = 40F * 3F;
    float targetMediumSize = (targetLargeSize + targetSmallSize) / 2F;
    float mediumAdjustment = targetMediumSize * .05F;
    Arrangement arrangement =
        new Arrangement(
            /* priority= */ 1,
            /* targetSmallSize= */ targetSmallSize,
            /* minSmallSize= */ 40F,
            /* maxSmallSize= */ 56F,
            /* smallCount= */ 1,
            /* targetMediumSize= */ targetMediumSize,
            /* mediumCount= */ 1,
            /* targetLargeSize= */ targetLargeSize,
            /* largeCount= */ 1,
            /* availableSpace= */ targetLargeSize
                + targetMediumSize
                + targetSmallSize
                - mediumAdjustment);

    assertThat(arrangement.largeSize).isEqualTo(targetLargeSize);
    assertThat(Math.round(arrangement.mediumSize))
        .isEqualTo(Math.round(targetMediumSize - mediumAdjustment));
    assertThat(arrangement.smallSize).isEqualTo(targetSmallSize);
  }

  @Test
  public void test1L1M1S_increasesMediumSize() {
    float targetSmallSize = 56F;
    float targetLargeSize = 56F * 3F;
    float targetMediumSize = (targetLargeSize + targetSmallSize) / 2F;
    float mediumAdjustment = targetMediumSize * .05F;
    Arrangement arrangement =
        new Arrangement(
            /* priority= */ 1,
            /* targetSmallSize= */ targetSmallSize,
            /* minSmallSize= */ 40F,
            /* maxSmallSize= */ 56F,
            /* smallCount= */ 1,
            /* targetMediumSize= */ targetMediumSize,
            /* mediumCount= */ 1,
            /* targetLargeSize= */ targetLargeSize,
            /* largeCount= */ 1,
            /* availableSpace= */ targetLargeSize
                + targetMediumSize
                + targetSmallSize
                + mediumAdjustment);

    assertThat(arrangement.largeSize).isEqualTo(targetLargeSize);
    assertThat(Math.round(arrangement.mediumSize))
        .isEqualTo(Math.round(targetMediumSize + mediumAdjustment));
    assertThat(arrangement.smallSize).isEqualTo(targetSmallSize);
  }

  @Test
  public void test1L1M2S_increasesSmallSize() {
    float targetSmallSize = 40F;
    float targetLargeSize = 40F * 3F;
    float targetMediumSize = (targetLargeSize + targetSmallSize) / 2F;
    float smallAdjustment = 10F;
    Arrangement arrangement =
        new Arrangement(
            /* priority= */ 1,
            /* targetSmallSize= */ targetSmallSize,
            /* minSmallSize= */ 40F,
            /* maxSmallSize= */ 56F,
            /* smallCount= */ 2,
            /* targetMediumSize= */ targetMediumSize,
            /* mediumCount= */ 1,
            /* targetLargeSize= */ targetLargeSize,
            /* largeCount= */ 1,
            /* availableSpace= */ targetLargeSize
                + targetMediumSize
                + (targetSmallSize * 2)
                + (smallAdjustment * 2));

    assertThat(arrangement.largeSize).isEqualTo(targetLargeSize);
    assertThat(Math.round(arrangement.mediumSize)).isEqualTo(Math.round(targetMediumSize));
    assertThat(arrangement.smallSize).isEqualTo(targetSmallSize + smallAdjustment);
  }

  @Test
  public void test1L1M2S_decreasesSmallSize() {
    float targetSmallSize = 56F;
    float targetLargeSize = 56F * 3F;
    float targetMediumSize = (targetLargeSize + targetSmallSize) / 2F;
    float smallAdjustment = 10F;
    Arrangement arrangement =
        new Arrangement(
            /* priority= */ 1,
            /* targetSmallSize= */ targetSmallSize,
            /* minSmallSize= */ 40F,
            /* maxSmallSize= */ 56F,
            /* smallCount= */ 2,
            /* targetMediumSize= */ targetMediumSize,
            /* mediumCount= */ 1,
            /* targetLargeSize= */ targetLargeSize,
            /* largeCount= */ 1,
            /* availableSpace= */ targetLargeSize
                + targetMediumSize
                + (targetSmallSize * 2)
                - (smallAdjustment * 2));

    assertThat(arrangement.largeSize).isEqualTo(targetLargeSize);
    assertThat(Math.round(arrangement.mediumSize)).isEqualTo(Math.round(targetMediumSize));
    assertThat(arrangement.smallSize).isEqualTo(targetSmallSize - smallAdjustment);
  }

  @Test
  public void test2L2M2S_increasesMediumSize() {
    float targetSmallSize = 56F;
    float targetLargeSize = 56F * 3F;
    float targetMediumSize = (targetLargeSize + targetSmallSize) / 2F;
    float mediumAdjustment = targetMediumSize * .05F;
    Arrangement arrangement =
        new Arrangement(
            /* priority= */ 1,
            /* targetSmallSize= */ targetSmallSize,
            /* minSmallSize= */ 40F,
            /* maxSmallSize= */ 56F,
            /* smallCount= */ 2,
            /* targetMediumSize= */ targetMediumSize,
            /* mediumCount= */ 2,
            /* targetLargeSize= */ targetLargeSize,
            /* largeCount= */ 2,
            /* availableSpace= */ (targetLargeSize * 2)
                + (targetMediumSize * 2)
                + (targetSmallSize * 2)
                + (mediumAdjustment * 2));

    assertThat(arrangement.largeSize).isEqualTo(targetLargeSize);
    assertThat(Math.round(arrangement.mediumSize))
        .isEqualTo(Math.round(targetMediumSize + mediumAdjustment));
    assertThat(arrangement.smallSize).isEqualTo(targetSmallSize);
  }

  @Test
  public void test2L2M2S_decreasesMediumSize() {
    float targetSmallSize = 40F;
    float targetLargeSize = 40F * 3F;
    float targetMediumSize = (targetLargeSize + targetSmallSize) / 2F;
    float mediumAdjustment = targetMediumSize * .05F;
    Arrangement arrangement =
        new Arrangement(
            /* priority= */ 1,
            /* targetSmallSize= */ targetSmallSize,
            /* minSmallSize= */ 40F,
            /* maxSmallSize= */ 56F,
            /* smallCount= */ 2,
            /* targetMediumSize= */ targetMediumSize,
            /* mediumCount= */ 2,
            /* targetLargeSize= */ targetLargeSize,
            /* largeCount= */ 2,
            /* availableSpace= */ (targetLargeSize * 2)
                + (targetMediumSize * 2)
                + (targetSmallSize * 2)
                - (mediumAdjustment * 2));

    assertThat(arrangement.largeSize).isEqualTo(targetLargeSize);
    assertThat(Math.round(arrangement.mediumSize))
        .isEqualTo(Math.round(targetMediumSize - mediumAdjustment));
    assertThat(arrangement.smallSize).isEqualTo(targetSmallSize);
  }
}
