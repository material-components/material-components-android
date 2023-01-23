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

package com.google.android.material.color.utilities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class ContrastTest {

  @Test
  public void lighter_impossibleRatioErrors() {
    assertEquals(-1.0, Contrast.lighter(90.0, 10.0), 0.001);
  }

  @Test
  public void lighter_outOfBoundsInputAboveErrors() {
    assertEquals(-1.0, Contrast.lighter(110.0, 2.0), 0.001);
  }

  @Test
  public void lighter_outOfBoundsInputBelowErrors() {
    assertEquals(-1.0, Contrast.lighter(-10.0, 2.0), 0.001);
  }

  @Test
  public void lighterUnsafe_returnsMaxTone() {
    assertEquals(100, Contrast.lighterUnsafe(100.0, 2.0), 0.001);
  }

  @Test
  public void darker_impossibleRatioErrors() {
    assertEquals(-1.0, Contrast.darker(10.0, 20.0), 0.001);
  }

  @Test
  public void darker_outOfBoundsInputAboveErrors() {
    assertEquals(-1.0, Contrast.darker(110.0, 2.0), 0.001);
  }

  @Test
  public void darker_outOfBoundsInputBelowErrors() {
    assertEquals(-1.0, Contrast.darker(-10.0, 2.0), 0.001);
  }

  @Test
  public void darkerUnsafe_returnsMinTone() {
    assertEquals(0.0, Contrast.darkerUnsafe(0.0, 2.0), 0.001);
  }
}
