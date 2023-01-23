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

import static com.google.common.truth.Truth.assertThat;

import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class QuantizerMapTest {
  private static final int RED = 0xffff0000;
  private static final int GREEN = 0xff00ff00;
  private static final int BLUE = 0xff0000ff;
  private static final QuantizerMap quantizer = new QuantizerMap();

  @Test
  public void quantize_1R() {
    Map<Integer, Integer> answer = quantizer.quantize(new int[] {RED}, 128).colorToCount;

    assertThat(answer).hasSize(1);
    assertThat(answer).containsEntry(RED, 1);
  }

  @Test
  public void quantize_1G() {
    Map<Integer, Integer> answer = quantizer.quantize(new int[] {GREEN}, 128).colorToCount;

    assertThat(answer).hasSize(1);
    assertThat(answer).containsEntry(GREEN, 1);
  }

  @Test
  public void quantize_1B() {
    Map<Integer, Integer> answer = quantizer.quantize(new int[] {BLUE}, 128).colorToCount;

    assertThat(answer).hasSize(1);
    assertThat(answer).containsEntry(BLUE, 1);
  }

  @Test
  public void quantize_5B() {
    Map<Integer, Integer> answer =
        quantizer.quantize(new int[] {BLUE, BLUE, BLUE, BLUE, BLUE}, 128).colorToCount;

    assertThat(answer).hasSize(1);
    assertThat(answer).containsEntry(BLUE, 5);
  }

  @Test
  public void quantize_2R3G() {
    Map<Integer, Integer> answer =
        quantizer.quantize(new int[] {RED, RED, GREEN, GREEN, GREEN}, 128).colorToCount;

    assertThat(answer).hasSize(2);
    assertThat(answer).containsEntry(RED, 2);
    assertThat(answer).containsEntry(GREEN, 3);
  }

  @Test
  public void quantize_1R1G1B() {
    Map<Integer, Integer> answer =
        quantizer.quantize(new int[] {RED, GREEN, BLUE}, 128).colorToCount;

    assertThat(answer).hasSize(3);
    assertThat(answer).containsEntry(RED, 1);
    assertThat(answer).containsEntry(GREEN, 1);
    assertThat(answer).containsEntry(BLUE, 1);
  }
}
