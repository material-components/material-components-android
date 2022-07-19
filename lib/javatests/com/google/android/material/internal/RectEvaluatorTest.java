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

import android.graphics.Rect;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class RectEvaluatorTest {

  @Test
  public void givenRectEvaluator_whenEvaluateZero_thenUpdatesAndReturnsRect() {
    Rect startValue = new Rect(0, 0, 0, 0);
    Rect endValue = new Rect(10, 20, 30, 40);
    Rect input = new Rect();

    Rect actual = new RectEvaluator(input).evaluate(0, startValue, endValue);

    Rect expected = new Rect(0, 0, 0, 0);
    assertEquals(expected, actual);
    assertEquals(expected, input);
  }

  @Test
  public void givenRectEvaluator_whenEvaluateHalf_thenUpdatesAndReturnsRect() {
    Rect startValue = new Rect(0, 0, 0, 0);
    Rect endValue = new Rect(10, 20, 30, 40);
    Rect input = new Rect();

    Rect actual = new RectEvaluator(input).evaluate(0.5f, startValue, endValue);

    Rect expected = new Rect(5, 10, 15, 20);
    assertEquals(expected, actual);
    assertEquals(expected, input);
  }

  @Test
  public void givenRectEvaluator_whenEvaluateOne_thenUpdatesAndReturnsRect() {
    Rect startValue = new Rect(0, 0, 0, 0);
    Rect endValue = new Rect(10, 20, 30, 40);
    Rect input = new Rect();

    Rect actual = new RectEvaluator(input).evaluate(1, startValue, endValue);

    Rect expected = new Rect(10, 20, 30, 40);
    assertEquals(expected, actual);
    assertEquals(expected, input);
  }
}
