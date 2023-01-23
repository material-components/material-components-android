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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class ScoreTest {

  @Test
  public void score_picksMostChromatic() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xFF0000FF, 1);
    colorsToPopulation.put(0xFFFF0000, 1);
    colorsToPopulation.put(0xFF00FF00, 1);

    List<Integer> scores = Score.score(colorsToPopulation);

    assertThat(scores.get(0)).isEqualTo(0xffff0000);
    assertThat(scores.get(1)).isEqualTo(0xff00ff00);
    assertThat(scores.get(2)).isEqualTo(0xff0000ff);
  }

  @Test
  public void score_usesGblueFallback() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xFF000000, 1);

    List<Integer> scores = Score.score(colorsToPopulation);

    assertThat(scores).containsExactly(0xff4285f4);
  }

  @Test
  public void score_dedupesNearbyHues() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xff008772, 1);
    colorsToPopulation.put(0xff318477, 1);

    List<Integer> scores = Score.score(colorsToPopulation);

    assertThat(scores).containsExactly(0xff008772);
  }
}
