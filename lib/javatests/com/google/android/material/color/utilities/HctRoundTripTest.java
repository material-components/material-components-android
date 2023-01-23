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

import androidx.test.filters.MediumTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@MediumTest
public final class HctRoundTripTest {
  @Test
  public void hctRoundTripTest() {
    // Assures that sRGB -> HCT -> sRGB returns the original hexadecimal.
    for (int i = 0; i <= 0x00FFFFFF; i++) {
      int color = 0xFF000000 | i;
      Hct hct = Hct.fromInt(color);
      int reconstructedFromHct = Hct.from(hct.getHue(), hct.getChroma(), hct.getTone()).toInt();

      assertEquals(color, reconstructedFromHct);
    }
  }
}
