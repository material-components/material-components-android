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

import static com.google.android.material.color.utilities.ArgbSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class TemperatureCacheTest {

  @Test
  public void testRawTemperature() {
    final Hct blueHct = Hct.fromInt(0xff0000ff);
    final double blueTemp = TemperatureCache.rawTemperature(blueHct);
    assertThat(blueTemp).isWithin(0.001).of(-1.393);

    final Hct redHct = Hct.fromInt(0xffff0000);
    final double redTemp = TemperatureCache.rawTemperature(redHct);
    assertThat(redTemp).isWithin(0.001).of(2.351);

    final Hct greenHct = Hct.fromInt(0xff00ff00);
    final double greenTemp = TemperatureCache.rawTemperature(greenHct);
    assertThat(greenTemp).isWithin(0.001).of(-0.267);

    final Hct whiteHct = Hct.fromInt(0xffffffff);
    final double whiteTemp = TemperatureCache.rawTemperature(whiteHct);
    assertThat(whiteTemp).isWithin(0.001).of(-0.5);

    final Hct blackHct = Hct.fromInt(0xff000000);
    final double blackTemp = TemperatureCache.rawTemperature(blackHct);
    assertThat(blackTemp).isWithin(0.001).of(-0.5);
  }

  @Test
  public void testComplement() {
    final int blueComplement =
        new TemperatureCache(Hct.fromInt(0xff0000ff)).getComplement().toInt();
    assertThat(0xff9D0002).isSameColorAs(blueComplement);

    final int redComplement = new TemperatureCache(Hct.fromInt(0xffff0000)).getComplement().toInt();
    assertThat(0xff007BFC).isSameColorAs(redComplement);

    final int greenComplement =
        new TemperatureCache(Hct.fromInt(0xff00ff00)).getComplement().toInt();
    assertThat(0xffFFD2C9).isSameColorAs(greenComplement);

    final int whiteComplement =
        new TemperatureCache(Hct.fromInt(0xffffffff)).getComplement().toInt();
    assertThat(0xffffffff).isSameColorAs(whiteComplement);

    final int blackComplement =
        new TemperatureCache(Hct.fromInt(0xff000000)).getComplement().toInt();
    assertThat(0xff000000).isSameColorAs(blackComplement);
  }

  @Test
  public void testAnalogous() {
    final List<Hct> blueAnalogous =
        new TemperatureCache(Hct.fromInt(0xff0000ff)).getAnalogousColors();
    assertThat(0xff00590C).isSameColorAs(blueAnalogous.get(0).toInt());
    assertThat(0xff00564E).isSameColorAs(blueAnalogous.get(1).toInt());
    assertThat(0xff0000ff).isSameColorAs(blueAnalogous.get(2).toInt());
    assertThat(0xff6700CC).isSameColorAs(blueAnalogous.get(3).toInt());
    assertThat(0xff81009F).isSameColorAs(blueAnalogous.get(4).toInt());

    final List<Hct> redAnalogous =
        new TemperatureCache(Hct.fromInt(0xffff0000)).getAnalogousColors();
    assertThat(0xffF60082).isSameColorAs(redAnalogous.get(0).toInt());
    assertThat(0xffFC004C).isSameColorAs(redAnalogous.get(1).toInt());
    assertThat(0xffff0000).isSameColorAs(redAnalogous.get(2).toInt());
    assertThat(0xffD95500).isSameColorAs(redAnalogous.get(3).toInt());
    assertThat(0xffAF7200).isSameColorAs(redAnalogous.get(4).toInt());

    final List<Hct> greenAnalogous =
        new TemperatureCache(Hct.fromInt(0xff00ff00)).getAnalogousColors();
    assertThat(0xffCEE900).isSameColorAs(greenAnalogous.get(0).toInt());
    assertThat(0xff92F500).isSameColorAs(greenAnalogous.get(1).toInt());
    assertThat(0xff00ff00).isSameColorAs(greenAnalogous.get(2).toInt());
    assertThat(0xff00FD6F).isSameColorAs(greenAnalogous.get(3).toInt());
    assertThat(0xff00FAB3).isSameColorAs(greenAnalogous.get(4).toInt());

    final List<Hct> blackAnalogous =
        new TemperatureCache(Hct.fromInt(0xff000000)).getAnalogousColors();
    assertThat(0xff000000).isSameColorAs(blackAnalogous.get(0).toInt());
    assertThat(0xff000000).isSameColorAs(blackAnalogous.get(1).toInt());
    assertThat(0xff000000).isSameColorAs(blackAnalogous.get(2).toInt());
    assertThat(0xff000000).isSameColorAs(blackAnalogous.get(3).toInt());
    assertThat(0xff000000).isSameColorAs(blackAnalogous.get(4).toInt());

    final List<Hct> whiteAnalogous =
        new TemperatureCache(Hct.fromInt(0xffffffff)).getAnalogousColors();
    assertThat(0xffffffff).isSameColorAs(whiteAnalogous.get(0).toInt());
    assertThat(0xffffffff).isSameColorAs(whiteAnalogous.get(1).toInt());
    assertThat(0xffffffff).isSameColorAs(whiteAnalogous.get(2).toInt());
    assertThat(0xffffffff).isSameColorAs(whiteAnalogous.get(3).toInt());
    assertThat(0xffffffff).isSameColorAs(whiteAnalogous.get(4).toInt());
  }
}
