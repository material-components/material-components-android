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
  public void score_prioritizeChroma() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xff000000, 1);
    colorsToPopulation.put(0xffffffff, 1);
    colorsToPopulation.put(0xff0000ff, 1);

    List<Integer> scores = Score.score(colorsToPopulation, 4);

    assertThat(scores).containsExactly(0xff0000ff);
  }

  @Test
  public void score_prioritizeChromaWhenProportionsEqual() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xffff0000, 1);
    colorsToPopulation.put(0xff00ff00, 1);
    colorsToPopulation.put(0xff0000ff, 1);

    List<Integer> scores = Score.score(colorsToPopulation, 4);

    assertThat(scores).containsExactly(0xffff0000, 0xff00ff00, 0xff0000ff).inOrder();
  }

  @Test
  public void score_generatesGblueWhenNoColorsAvailable() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xff000000, 1);

    List<Integer> scores = Score.score(colorsToPopulation, 4);

    assertThat(scores).containsExactly(0xff4285f4);
  }

  @Test
  public void score_dedupesNearbyHues() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xff008772, 1); // H 180 C 42 T 50
    colorsToPopulation.put(0xff318477, 1); // H 184 C 35 T 50

    List<Integer> scores = Score.score(colorsToPopulation, 4);

    assertThat(scores).containsExactly(0xff008772);
  }

  @Test
  public void score_maximizesHueDistance() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xff008772, 1); // H 180 C 42 T 50
    colorsToPopulation.put(0xff008587, 1); // H 198 C 50 T 50
    colorsToPopulation.put(0xff007ebc, 1); // H 245 C 50 T 50

    List<Integer> scores = Score.score(colorsToPopulation, 2);

    assertThat(scores).containsExactly(0xff007ebc, 0xff008772).inOrder();
  }

  @Test
  public void score_generatedScenarioOne() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xff7ea16d, 67);
    colorsToPopulation.put(0xffd8ccae, 67);
    colorsToPopulation.put(0xff835c0d, 49);

    List<Integer> scores = Score.score(colorsToPopulation, 3, 0xff8d3819, false);

    assertThat(scores).containsExactly(0xff7ea16d, 0xffd8ccae, 0xff835c0d).inOrder();
  }

  @Test
  public void score_generatedScenarioTwo() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xffd33881, 14);
    colorsToPopulation.put(0xff3205cc, 77);
    colorsToPopulation.put(0xff0b48cf, 36);
    colorsToPopulation.put(0xffa08f5d, 81);

    List<Integer> scores = Score.score(colorsToPopulation, 4, 0xff7d772b, true);

    assertThat(scores).containsExactly(0xff3205cc, 0xffa08f5d, 0xffd33881).inOrder();
  }

  @Test
  public void score_generatedScenarioThree() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xffbe94a6, 23);
    colorsToPopulation.put(0xffc33fd7, 42);
    colorsToPopulation.put(0xff899f36, 90);
    colorsToPopulation.put(0xff94c574, 82);

    List<Integer> scores = Score.score(colorsToPopulation, 3, 0xffaa79a4, true);

    assertThat(scores).containsExactly(0xff94c574, 0xffc33fd7, 0xffbe94a6).inOrder();
  }

  @Test
  public void score_generatedScenarioFour() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xffdf241c, 85);
    colorsToPopulation.put(0xff685859, 44);
    colorsToPopulation.put(0xffd06d5f, 34);
    colorsToPopulation.put(0xff561c54, 27);
    colorsToPopulation.put(0xff713090, 88);

    List<Integer> scores = Score.score(colorsToPopulation, 5, 0xff58c19c, false);

    assertThat(scores).containsExactly(0xffdf241c, 0xff561c54).inOrder();
  }

  @Test
  public void score_generatedScenarioFive() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xffbe66f8, 41);
    colorsToPopulation.put(0xff4bbda9, 88);
    colorsToPopulation.put(0xff80f6f9, 44);
    colorsToPopulation.put(0xffab8017, 43);
    colorsToPopulation.put(0xffe89307, 65);

    List<Integer> scores = Score.score(colorsToPopulation, 3, 0xff916691, false);

    assertThat(scores).containsExactly(0xffab8017, 0xff4bbda9, 0xffbe66f8).inOrder();
  }

  @Test
  public void score_generatedScenarioSix() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xff18ea8f, 93);
    colorsToPopulation.put(0xff327593, 18);
    colorsToPopulation.put(0xff066a18, 53);
    colorsToPopulation.put(0xfffa8a23, 74);
    colorsToPopulation.put(0xff04ca1f, 62);

    List<Integer> scores = Score.score(colorsToPopulation, 2, 0xff4c377a, false);

    assertThat(scores).containsExactly(0xff18ea8f, 0xfffa8a23).inOrder();
  }

  @Test
  public void score_generatedScenarioSeven() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xff2e05ed, 23);
    colorsToPopulation.put(0xff153e55, 90);
    colorsToPopulation.put(0xff9ab220, 23);
    colorsToPopulation.put(0xff153379, 66);
    colorsToPopulation.put(0xff68bcc3, 81);

    List<Integer> scores = Score.score(colorsToPopulation, 2, 0xfff588dc, true);

    assertThat(scores).containsExactly(0xff2e05ed, 0xff9ab220).inOrder();
  }

  @Test
  public void score_generatedScenarioEight() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xff816ec5, 24);
    colorsToPopulation.put(0xff6dcb94, 19);
    colorsToPopulation.put(0xff3cae91, 98);
    colorsToPopulation.put(0xff5b542f, 25);

    List<Integer> scores = Score.score(colorsToPopulation, 1, 0xff84b0fd, false);

    assertThat(scores).containsExactly(0xff3cae91);
  }

  @Test
  public void score_generatedScenarioNine() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xff206f86, 52);
    colorsToPopulation.put(0xff4a620d, 96);
    colorsToPopulation.put(0xfff51401, 85);
    colorsToPopulation.put(0xff2b8ebf, 3);
    colorsToPopulation.put(0xff277766, 59);

    List<Integer> scores = Score.score(colorsToPopulation, 3, 0xff02b415, true);

    assertThat(scores).containsExactly(0xfff51401, 0xff4a620d, 0xff2b8ebf).inOrder();
  }

  @Test
  public void score_generatedScenarioTen() {
    Map<Integer, Integer> colorsToPopulation = new HashMap<>();
    colorsToPopulation.put(0xff8b1d99, 54);
    colorsToPopulation.put(0xff27effe, 43);
    colorsToPopulation.put(0xff6f558d, 2);
    colorsToPopulation.put(0xff77fdf2, 78);

    List<Integer> scores = Score.score(colorsToPopulation, 4, 0xff5e7a10, true);

    assertThat(scores).containsExactly(0xff27effe, 0xff8b1d99, 0xff6f558d).inOrder();
  }
}
