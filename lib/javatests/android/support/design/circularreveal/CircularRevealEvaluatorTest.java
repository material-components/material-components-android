/*
 * Copyright 2017 The Android Open Source Project
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
package com.google.android.material.circularreveal;

import static com.google.common.truth.Truth.assertThat;

import com.google.android.material.circularreveal.CircularRevealWidget.CircularRevealEvaluator;
import com.google.android.material.circularreveal.CircularRevealWidget.RevealInfo;
import com.google.common.collect.Range;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link CircularRevealEvaluator}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class CircularRevealEvaluatorTest {

  private CircularRevealEvaluator evaluator;

  @Before
  public void setUp() {
    evaluator = new CircularRevealEvaluator();
  }

  @Test
  public void circularRevealEvaluatorInterpolatesCorrectly() {
    float startCenterX = 50;
    float startCenterY = 60;
    float startRadius = 70;
    RevealInfo start = new RevealInfo(startCenterX, startCenterY, startRadius);

    float endCenterX = 500;
    float endCenterY = 600;
    float endRadius = 700;
    RevealInfo end = new RevealInfo(endCenterX, endCenterY, endRadius);

    RevealInfo result;

    result = evaluator.evaluate(0f, start, end);
    assertThat(result.centerX).isWithin(0.0001f).of(startCenterX);
    assertThat(result.centerY).isWithin(0.0001f).of(startCenterY);
    assertThat(result.radius).isWithin(0.0001f).of(startRadius);

    result = evaluator.evaluate(.5f, start, end);
    assertThat(result.centerX).isIn(Range.open(startCenterX, endCenterX));
    assertThat(result.centerY).isIn(Range.open(startCenterY, endCenterY));
    assertThat(result.radius).isIn(Range.open(startRadius, endRadius));

    result = evaluator.evaluate(1f, start, end);
    assertThat(result.centerX).isWithin(0.0001f).of(endCenterX);
    assertThat(result.centerY).isWithin(0.0001f).of(endCenterY);
    assertThat(result.radius).isWithin(0.0001f).of(endRadius);
  }
}
