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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class DislikeTest {

  @Test
  public void monkSkinToneScaleColorsLiked() {
    // From https://skintone.google#/get-started
    int[] monkSkinToneScaleColors =
        new int[] {
          0xfff6ede4,
          0xfff3e7db,
          0xfff7ead0,
          0xffeadaba,
          0xffd7bd96,
          0xffa07e56,
          0xff825c43,
          0xff604134,
          0xff3a312a,
          0xff292420,
        };
    for (int color : monkSkinToneScaleColors) {
      assertFalse(DislikeAnalyzer.isDisliked(Hct.fromInt(color)));
    }
  }

  @Test
  public void bileColorsDisliked() {
    int[] unlikable =
        new int[] {
          0xff95884B, 0xff716B40, 0xffB08E00, 0xff4C4308, 0xff464521,
        };
    for (int color : unlikable) {
      assertTrue(DislikeAnalyzer.isDisliked(Hct.fromInt(color)));
    }
  }

  @Test
  public void bileColorsBecameLikable() {
    int[] unlikable =
        new int[] {
          0xff95884B, 0xff716B40, 0xffB08E00, 0xff4C4308, 0xff464521,
        };
    for (int color : unlikable) {
      Hct hct = Hct.fromInt(color);
      assertTrue(DislikeAnalyzer.isDisliked(hct));
      Hct likable = DislikeAnalyzer.fixIfDisliked(hct);
      assertFalse(DislikeAnalyzer.isDisliked(likable));
    }
  }

  @Test
  public void tone67NotDisliked() {
    Hct color = Hct.from(100.0, 50.0, 67.0);
    assertFalse(DislikeAnalyzer.isDisliked(color));
    assertEquals(color.toInt(), DislikeAnalyzer.fixIfDisliked(color).toInt());
  }
}
