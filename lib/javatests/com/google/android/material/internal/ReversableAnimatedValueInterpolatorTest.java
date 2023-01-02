/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.internal;

import static org.junit.Assert.assertEquals;

import android.animation.TimeInterpolator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ReversableAnimatedValueInterpolatorTest {

  private final TimeInterpolator sourceInterpolator = input -> input;

  @Test
  public void
      givenUseSourceInterpolator_whenGetInterpolationZero_thenReturnsSourceInterpolatorValue() {
    TimeInterpolator resultInterpolator =
        ReversableAnimatedValueInterpolator.of(true, sourceInterpolator);

    assertEquals(0, resultInterpolator.getInterpolation(0), 0.01);
  }

  @Test
  public void
      givenUseSourceInterpolator_whenGetInterpolationHalf_thenReturnsSourceInterpolatorValue() {
    TimeInterpolator resultInterpolator =
        ReversableAnimatedValueInterpolator.of(true, sourceInterpolator);

    assertEquals(0.5, resultInterpolator.getInterpolation(0.5f), 0.01);
  }

  @Test
  public void
      givenUseSourceInterpolator_whenGetInterpolationOne_thenReturnsSourceInterpolatorValue() {
    TimeInterpolator resultInterpolator =
        ReversableAnimatedValueInterpolator.of(true, sourceInterpolator);

    assertEquals(1, resultInterpolator.getInterpolation(1), 0.01);
  }

  @Test
  public void
      givenNotUseSourceInterpolator_whenGetInterpolationZero_thenReturnsReversedInterpolatorValue() {
    TimeInterpolator resultInterpolator =
        ReversableAnimatedValueInterpolator.of(false, sourceInterpolator);

    assertEquals(1, resultInterpolator.getInterpolation(0), 0.01);
  }

  @Test
  public void
      givenNotUseSourceInterpolator_whenGetInterpolationHalf_thenReturnsReversedInterpolatorValue() {
    TimeInterpolator resultInterpolator =
        ReversableAnimatedValueInterpolator.of(false, sourceInterpolator);

    assertEquals(0.5, resultInterpolator.getInterpolation(0.5f), 0.01);
  }

  @Test
  public void
      givenNotUseSourceInterpolator_whenGetInterpolationOne_thenReturnsReversedInterpolatorValue() {
    TimeInterpolator resultInterpolator =
        ReversableAnimatedValueInterpolator.of(false, sourceInterpolator);

    assertEquals(0, resultInterpolator.getInterpolation(1), 0.01);
  }
}
