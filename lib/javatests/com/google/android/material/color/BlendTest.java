/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.google.android.material.color;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class BlendTest {
  private static final int RED = 0xffff0000;
  private static final int BLUE = 0xff0000ff;

  @Test
  public void cam16ucs_redToBlue() {
    int blended = Blend.blendCam16Ucs(RED, BLUE, 0.8f);
    assertThat(blended).isEqualTo(0xff6440b4);
  }

  @Test
  public void hctHue_redToBlue() {
    int blended = Blend.blendHctHue(0xffff0000, 0xff0000ff, 0.8f);
    assertThat(blended).isEqualTo(0xFF925DFF);
  }

  @Test
  public void harmonzie_redToBlue() {
    int blended = Blend.harmonize(RED, BLUE);
    assertThat(blended).isEqualTo(0xFFFB005A);
  }
}
