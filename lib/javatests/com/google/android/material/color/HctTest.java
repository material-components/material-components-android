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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class HctTest {
  private static final int RED = 0xffff0000;
  private static final int GREEN = 0xff00ff00;
  private static final int BLUE = 0xff0000ff;
  private static final int WHITE = 0xffffffff;
  private static final int BLACK = 0xff000000;

  @Test
  public void camFromArgb_red() {
    Cam16 cam = Cam16.fromInt(RED);

    assertEquals(27.408, cam.getHue(), 0.001);
    assertEquals(113.357, cam.getChroma(), 0.001);
    assertEquals(46.445, cam.getJ(), 0.001);
    assertEquals(89.494, cam.getM(), 0.001);
    assertEquals(91.889, cam.getS(), 0.001);
    assertEquals(105.988, cam.getQ(), 0.001);
  }

  @Test
  public void camFromArgb_green() {
    Cam16 cam = Cam16.fromInt(GREEN);

    assertEquals(142.139, cam.getHue(), 0.001);
    assertEquals(108.410, cam.getChroma(), 0.001);
    assertEquals(79.331, cam.getJ(), 0.001);
    assertEquals(85.587, cam.getM(), 0.001);
    assertEquals(78.604, cam.getS(), 0.001);
    assertEquals(138.520, cam.getQ(), 0.001);
  }

  @Test
  public void camFromArgb_blue() {
    Cam16 cam = Cam16.fromInt(BLUE);

    assertEquals(282.788, cam.getHue(), 0.001);
    assertEquals(87.230, cam.getChroma(), 0.001);
    assertEquals(25.465, cam.getJ(), 0.001);
    assertEquals(68.867, cam.getM(), 0.001);
    assertEquals(93.674, cam.getS(), 0.001);
    assertEquals(78.481, cam.getQ(), 0.001);
  }

  @Test
  public void camFromArgb_white() {
    Cam16 cam = Cam16.fromInt(WHITE);

    assertEquals(209.492, cam.getHue(), 0.001);
    assertEquals(2.869, cam.getChroma(), 0.001);
    assertEquals(100.0, cam.getJ(), 0.001);
    assertEquals(2.265, cam.getM(), 0.001);
    assertEquals(12.068, cam.getS(), 0.001);
    assertEquals(155.521, cam.getQ(), 0.001);
  }

  @Test
  public void camFromArgb_black() {
    Cam16 cam = Cam16.fromInt(BLACK);

    assertEquals(0.0, cam.getHue(), 0.001);
    assertEquals(0.0, cam.getChroma(), 0.001);
    assertEquals(0.0, cam.getJ(), 0.001);
    assertEquals(0.0, cam.getM(), 0.001);
    assertEquals(0.0, cam.getS(), 0.001);
    assertEquals(0.0, cam.getQ(), 0.001);
  }

  @Test
  public void camToArgbToCam_red() {
    Cam16 cam = Cam16.fromInt(RED);
    int argb = cam.getInt();
    assertEquals(RED, argb);
  }

  @Test
  public void camToArgbToCam_green() {
    Cam16 cam = Cam16.fromInt(GREEN);
    int argb = cam.getInt();
    assertEquals(GREEN, argb);
  }

  @Test
  public void camToArgbToCam_blue() {
    Cam16 cam = Cam16.fromInt(BLUE);
    int argb = cam.getInt();
    assertEquals(BLUE, argb);
  }

  @Test
  public void viewingConditions_default() {
    ViewingConditions vc = ViewingConditions.DEFAULT;

    assertEquals(0.184, vc.getN(), 0.001);
    assertEquals(29.981, vc.getAw(), 0.001);
    assertEquals(1.016, vc.getNbb(), 0.001);
    assertEquals(1.021, vc.getRgbD()[0], 0.001);
    assertEquals(0.986, vc.getRgbD()[1], 0.001);
    assertEquals(0.933, vc.getRgbD()[2], 0.001);
    assertEquals(0.789, vc.getFlRoot(), 0.001);
  }
}
