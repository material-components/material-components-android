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

package com.google.android.material.color.utilities;

import static com.google.android.material.color.utilities.ArgbSubject.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class BlendTest {
  private static final int RED = 0xffff0000;
  private static final int BLUE = 0xff0000ff;
  private static final int GREEN = 0xff00ff00;
  private static final int YELLOW = 0xffffff00;

  @Test
  public void harmonize_redToBlue() {
    int blended = Blend.harmonize(RED, BLUE);
    assertThat(blended).isSameColorAs(0xffFB0057);
  }

  @Test
  public void harmonize_redToGreen() {
    int answer = Blend.harmonize(RED, GREEN);
    assertThat(answer).isSameColorAs(0xffD85600);
  }

  @Test
  public void harmonize_redToYellow() {
    int answer = Blend.harmonize(RED, YELLOW);
    assertThat(answer).isSameColorAs(0xffD85600);
  }

  @Test
  public void harmonize_blueToGreen() {
    int answer = Blend.harmonize(BLUE, GREEN);
    assertThat(answer).isSameColorAs(0xff0047A3);
  }

  @Test
  public void harmonize_blueToRed() {
    int answer = Blend.harmonize(BLUE, RED);
    assertThat(answer).isSameColorAs(0xff5700DC);
  }

  @Test
  public void harmonize_blueToYellow() {
    int answer = Blend.harmonize(BLUE, YELLOW);
    assertThat(answer).isSameColorAs(0xff0047A3);
  }

  @Test
  public void harmonize_greenToBlue() {
    int answer = Blend.harmonize(GREEN, BLUE);
    assertThat(answer).isSameColorAs(0xff00FC94);
  }

  @Test
  public void harmonize_greenToRed() {
    int answer = Blend.harmonize(GREEN, RED);
    assertThat(answer).isSameColorAs(0xffB1F000);
  }

  @Test
  public void harmonize_greenToYellow() {
    int answer = Blend.harmonize(GREEN, YELLOW);
    assertThat(answer).isSameColorAs(0xffB1F000);
  }

  @Test
  public void harmonize_yellowToBlue() {
    int answer = Blend.harmonize(YELLOW, BLUE);
    assertThat(answer).isSameColorAs(0xffEBFFBA);
  }

  @Test
  public void harmonize_yellowToGreen() {
    int answer = Blend.harmonize(YELLOW, GREEN);
    assertThat(answer).isSameColorAs(0xffEBFFBA);
  }

  @Test
  public void harmonize_yellowToRed() {
    int answer = Blend.harmonize(YELLOW, RED);
    assertThat(answer).isSameColorAs(0xffFFF6E3);
  }
}
