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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class ColorUtilsTest {

  @Test
  public void argbFromLab() {
    assertThat(ColorUtils.argbFromLab(0.0, 0.0, 0.0)).isEqualTo(0xff000000);
    assertThat(ColorUtils.argbFromLab(0.25, 0.0, 0.0)).isEqualTo(0xff010101);
    assertThat(ColorUtils.argbFromLab(0.5, 0.0, 0.0)).isEqualTo(0xff020202);
    assertThat(ColorUtils.argbFromLab(0.75, 0.0, 0.0)).isEqualTo(0xff030303);
    assertThat(ColorUtils.argbFromLab(1.0, 0.0, 0.0)).isEqualTo(0xff040404);
    assertThat(ColorUtils.argbFromLab(1.5, 0.0, 0.0)).isEqualTo(0xff050505);
    assertThat(ColorUtils.argbFromLab(2.0, 0.0, 0.0)).isEqualTo(0xff070707);
    assertThat(ColorUtils.argbFromLab(3.0, 0.0, 0.0)).isEqualTo(0xff0b0b0b);
    assertThat(ColorUtils.argbFromLab(4.0, 0.0, 0.0)).isEqualTo(0xff0e0e0e);
    assertThat(ColorUtils.argbFromLab(5.0, 0.0, 0.0)).isEqualTo(0xff111111);
    assertThat(ColorUtils.argbFromLab(6.0, 0.0, 0.0)).isEqualTo(0xff131313);
    assertThat(ColorUtils.argbFromLab(7.0, 0.0, 0.0)).isEqualTo(0xff151515);
    assertThat(ColorUtils.argbFromLab(8.0, 0.0, 0.0)).isEqualTo(0xff181818);
    assertThat(ColorUtils.argbFromLab(9.0, 0.0, 0.0)).isEqualTo(0xff191919);
    assertThat(ColorUtils.argbFromLab(10.0, 0.0, 0.0)).isEqualTo(0xff1b1b1b);
    assertThat(ColorUtils.argbFromLab(20.0, 0.0, 0.0)).isEqualTo(0xff303030);
    assertThat(ColorUtils.argbFromLab(30.0, 0.0, 0.0)).isEqualTo(0xff474747);
    assertThat(ColorUtils.argbFromLab(40.0, 0.0, 0.0)).isEqualTo(0xff5e5e5e);
    assertThat(ColorUtils.argbFromLab(50.0, 0.0, 0.0)).isEqualTo(0xff777777);
    assertThat(ColorUtils.argbFromLab(60.0, 0.0, 0.0)).isEqualTo(0xff919191);
    assertThat(ColorUtils.argbFromLab(70.0, 0.0, 0.0)).isEqualTo(0xffababab);
    assertThat(ColorUtils.argbFromLab(80.0, 0.0, 0.0)).isEqualTo(0xffc6c6c6);
    assertThat(ColorUtils.argbFromLab(90.0, 0.0, 0.0)).isEqualTo(0xffe2e2e2);
    assertThat(ColorUtils.argbFromLab(100.0, 0.0, 0.0)).isEqualTo(0xffffffff);
  }

  @Test
  public void labFromArgb() {
    assertThat(ColorUtils.labFromArgb(0xff000000)[0]).isWithin(0.001).of(0.0);
    assertThat(ColorUtils.labFromArgb(0xff010101)[0]).isWithin(0.001).of(0.2741748000656514);
    assertThat(ColorUtils.labFromArgb(0xff020202)[0]).isWithin(0.001).of(0.5483496001313029);
    assertThat(ColorUtils.labFromArgb(0xff030303)[0]).isWithin(0.001).of(0.8225244001969543);
    assertThat(ColorUtils.labFromArgb(0xff040404)[0]).isWithin(0.001).of(1.0966992002626057);
    assertThat(ColorUtils.labFromArgb(0xff050505)[0]).isWithin(0.001).of(1.3708740003282571);
    assertThat(ColorUtils.labFromArgb(0xff060606)[0]).isWithin(0.001).of(1.645048800393912);
    assertThat(ColorUtils.labFromArgb(0xff070707)[0]).isWithin(0.001).of(1.9192236004595635);
    assertThat(ColorUtils.labFromArgb(0xff080808)[0]).isWithin(0.001).of(2.193398400525215);
    assertThat(ColorUtils.labFromArgb(0xff0c0c0c)[0]).isWithin(0.001).of(3.3209754491182544);
    assertThat(ColorUtils.labFromArgb(0xff101010)[0]).isWithin(0.001).of(4.680444846419661);
    assertThat(ColorUtils.labFromArgb(0xff181818)[0]).isWithin(0.001).of(8.248186036170349);
    assertThat(ColorUtils.labFromArgb(0xff202020)[0]).isWithin(0.001).of(12.250030101522828);
    assertThat(ColorUtils.labFromArgb(0xff404040)[0]).isWithin(0.001).of(27.093413739449055);
    assertThat(ColorUtils.labFromArgb(0xff808080)[0]).isWithin(0.001).of(53.585013452169036);
    assertThat(ColorUtils.labFromArgb(0xffffffff)[0]).isWithin(0.001).of(100.0);
  }

  @Test
  public void argbFromLstar() {
    assertThat(ColorUtils.argbFromLstar(0.0)).isEqualTo(0xff000000);
    assertThat(ColorUtils.argbFromLstar(0.25)).isEqualTo(0xff010101);
    assertThat(ColorUtils.argbFromLstar(0.5)).isEqualTo(0xff020202);
    assertThat(ColorUtils.argbFromLstar(0.75)).isEqualTo(0xff030303);
    assertThat(ColorUtils.argbFromLstar(1.0)).isEqualTo(0xff040404);
    assertThat(ColorUtils.argbFromLstar(1.5)).isEqualTo(0xff050505);
    assertThat(ColorUtils.argbFromLstar(2.0)).isEqualTo(0xff070707);
    assertThat(ColorUtils.argbFromLstar(3.0)).isEqualTo(0xff0b0b0b);
    assertThat(ColorUtils.argbFromLstar(4.0)).isEqualTo(0xff0e0e0e);
    assertThat(ColorUtils.argbFromLstar(5.0)).isEqualTo(0xff111111);
    assertThat(ColorUtils.argbFromLstar(6.0)).isEqualTo(0xff131313);
    assertThat(ColorUtils.argbFromLstar(7.0)).isEqualTo(0xff151515);
    assertThat(ColorUtils.argbFromLstar(8.0)).isEqualTo(0xff181818);
    assertThat(ColorUtils.argbFromLstar(9.0)).isEqualTo(0xff191919);
    assertThat(ColorUtils.argbFromLstar(10.0)).isEqualTo(0xff1b1b1b);
    assertThat(ColorUtils.argbFromLstar(20.0)).isEqualTo(0xff303030);
    assertThat(ColorUtils.argbFromLstar(30.0)).isEqualTo(0xff474747);
    assertThat(ColorUtils.argbFromLstar(40.0)).isEqualTo(0xff5e5e5e);
    assertThat(ColorUtils.argbFromLstar(50.0)).isEqualTo(0xff777777);
    assertThat(ColorUtils.argbFromLstar(60.0)).isEqualTo(0xff919191);
    assertThat(ColorUtils.argbFromLstar(70.0)).isEqualTo(0xffababab);
    assertThat(ColorUtils.argbFromLstar(80.0)).isEqualTo(0xffc6c6c6);
    assertThat(ColorUtils.argbFromLstar(90.0)).isEqualTo(0xffe2e2e2);
    assertThat(ColorUtils.argbFromLstar(100.0)).isEqualTo(0xffffffff);
  }

  @Test
  public void lstarFromArgb() {
    assertThat(ColorUtils.lstarFromArgb(0xff000000)).isWithin(0.001).of(0.0);
    assertThat(ColorUtils.lstarFromArgb(0xff010101)).isWithin(0.001).of(0.2741748000656518);
    assertThat(ColorUtils.lstarFromArgb(0xff020202)).isWithin(0.001).of(0.5483496001313036);
    assertThat(ColorUtils.lstarFromArgb(0xff030303)).isWithin(0.001).of(0.8225244001969553);
    assertThat(ColorUtils.lstarFromArgb(0xff040404)).isWithin(0.001).of(1.0966992002626073);
    assertThat(ColorUtils.lstarFromArgb(0xff050505)).isWithin(0.001).of(1.3708740003282587);
    assertThat(ColorUtils.lstarFromArgb(0xff060606)).isWithin(0.001).of(1.6450488003939105);
    assertThat(ColorUtils.lstarFromArgb(0xff070707)).isWithin(0.001).of(1.9192236004595624);
    assertThat(ColorUtils.lstarFromArgb(0xff080808)).isWithin(0.001).of(2.1933984005252145);
    assertThat(ColorUtils.lstarFromArgb(0xff0c0c0c)).isWithin(0.001).of(3.3209754491182557);
    assertThat(ColorUtils.lstarFromArgb(0xff101010)).isWithin(0.001).of(4.680444846419663);
    assertThat(ColorUtils.lstarFromArgb(0xff181818)).isWithin(0.001).of(8.248186036170349);
    assertThat(ColorUtils.lstarFromArgb(0xff202020)).isWithin(0.001).of(12.250030101522828);
    assertThat(ColorUtils.lstarFromArgb(0xff404040)).isWithin(0.001).of(27.093413739449055);
    assertThat(ColorUtils.lstarFromArgb(0xff808080)).isWithin(0.001).of(53.585013452169036);
    assertThat(ColorUtils.lstarFromArgb(0xffffffff)).isWithin(0.001).of(100.0);
  }

  @Test
  public void yFromLstar() {
    assertThat(ColorUtils.yFromLstar(0.0)).isWithin(1e-5).of(0.0);
    assertThat(ColorUtils.yFromLstar(0.1)).isWithin(1e-5).of(0.0110705);
    assertThat(ColorUtils.yFromLstar(0.2)).isWithin(1e-5).of(0.0221411);
    assertThat(ColorUtils.yFromLstar(0.3)).isWithin(1e-5).of(0.0332116);
    assertThat(ColorUtils.yFromLstar(0.4)).isWithin(1e-5).of(0.0442822);
    assertThat(ColorUtils.yFromLstar(0.5)).isWithin(1e-5).of(0.0553528);
    assertThat(ColorUtils.yFromLstar(1.0)).isWithin(1e-5).of(0.1107056);
    assertThat(ColorUtils.yFromLstar(2.0)).isWithin(1e-5).of(0.2214112);
    assertThat(ColorUtils.yFromLstar(3.0)).isWithin(1e-5).of(0.3321169);
    assertThat(ColorUtils.yFromLstar(4.0)).isWithin(1e-5).of(0.4428225);
    assertThat(ColorUtils.yFromLstar(5.0)).isWithin(1e-5).of(0.5535282);
    assertThat(ColorUtils.yFromLstar(8.0)).isWithin(1e-5).of(0.8856451);
    assertThat(ColorUtils.yFromLstar(10.0)).isWithin(1e-5).of(1.1260199);
    assertThat(ColorUtils.yFromLstar(15.0)).isWithin(1e-5).of(1.9085832);
    assertThat(ColorUtils.yFromLstar(20.0)).isWithin(1e-5).of(2.9890524);
    assertThat(ColorUtils.yFromLstar(25.0)).isWithin(1e-5).of(4.4154767);
    assertThat(ColorUtils.yFromLstar(30.0)).isWithin(1e-5).of(6.2359055);
    assertThat(ColorUtils.yFromLstar(40.0)).isWithin(1e-5).of(11.2509737);
    assertThat(ColorUtils.yFromLstar(50.0)).isWithin(1e-5).of(18.4186518);
    assertThat(ColorUtils.yFromLstar(60.0)).isWithin(1e-5).of(28.1233342);
    assertThat(ColorUtils.yFromLstar(70.0)).isWithin(1e-5).of(40.7494157);
    assertThat(ColorUtils.yFromLstar(80.0)).isWithin(1e-5).of(56.6812907);
    assertThat(ColorUtils.yFromLstar(90.0)).isWithin(1e-5).of(76.3033539);
    assertThat(ColorUtils.yFromLstar(95.0)).isWithin(1e-5).of(87.6183294);
    assertThat(ColorUtils.yFromLstar(99.0)).isWithin(1e-5).of(97.4360239);
    assertThat(ColorUtils.yFromLstar(100.0)).isWithin(1e-5).of(100.0);
  }

  @Test
  public void lstarFromY() {
    assertThat(ColorUtils.lstarFromY(0.0)).isWithin(1e-5).of(0.0);
    assertThat(ColorUtils.lstarFromY(0.1)).isWithin(1e-5).of(0.9032962);
    assertThat(ColorUtils.lstarFromY(0.2)).isWithin(1e-5).of(1.8065925);
    assertThat(ColorUtils.lstarFromY(0.3)).isWithin(1e-5).of(2.7098888);
    assertThat(ColorUtils.lstarFromY(0.4)).isWithin(1e-5).of(3.6131851);
    assertThat(ColorUtils.lstarFromY(0.5)).isWithin(1e-5).of(4.5164814);
    assertThat(ColorUtils.lstarFromY(0.8856451)).isWithin(1e-5).of(8.0);
    assertThat(ColorUtils.lstarFromY(1.0)).isWithin(1e-5).of(8.9914424);
    assertThat(ColorUtils.lstarFromY(2.0)).isWithin(1e-5).of(15.4872443);
    assertThat(ColorUtils.lstarFromY(3.0)).isWithin(1e-5).of(20.0438970);
    assertThat(ColorUtils.lstarFromY(4.0)).isWithin(1e-5).of(23.6714419);
    assertThat(ColorUtils.lstarFromY(5.0)).isWithin(1e-5).of(26.7347653);
    assertThat(ColorUtils.lstarFromY(10.0)).isWithin(1e-5).of(37.8424304);
    assertThat(ColorUtils.lstarFromY(15.0)).isWithin(1e-5).of(45.6341970);
    assertThat(ColorUtils.lstarFromY(20.0)).isWithin(1e-5).of(51.8372115);
    assertThat(ColorUtils.lstarFromY(25.0)).isWithin(1e-5).of(57.0754208);
    assertThat(ColorUtils.lstarFromY(30.0)).isWithin(1e-5).of(61.6542222);
    assertThat(ColorUtils.lstarFromY(40.0)).isWithin(1e-5).of(69.4695307);
    assertThat(ColorUtils.lstarFromY(50.0)).isWithin(1e-5).of(76.0692610);
    assertThat(ColorUtils.lstarFromY(60.0)).isWithin(1e-5).of(81.8381891);
    assertThat(ColorUtils.lstarFromY(70.0)).isWithin(1e-5).of(86.9968642);
    assertThat(ColorUtils.lstarFromY(80.0)).isWithin(1e-5).of(91.6848609);
    assertThat(ColorUtils.lstarFromY(90.0)).isWithin(1e-5).of(95.9967686);
    assertThat(ColorUtils.lstarFromY(95.0)).isWithin(1e-5).of(98.0335184);
    assertThat(ColorUtils.lstarFromY(99.0)).isWithin(1e-5).of(99.6120372);
    assertThat(ColorUtils.lstarFromY(100.0)).isWithin(1e-5).of(100.0);
  }

  @Test
  public void yToLstarToY() {
    for (double y = 0.0; y <= 100.0; y += 0.1) {
      double lstar = ColorUtils.lstarFromY(y);
      double reconstructedY = ColorUtils.yFromLstar(lstar);
      assertThat(reconstructedY).isWithin(1e-8).of(y);
    }
  }

  @Test
  public void lstarToYToLstar() {
    for (double lstar = 0.0; lstar <= 100.0; lstar += 0.1) {
      double y = ColorUtils.yFromLstar(lstar);
      double reconstructedLstar = ColorUtils.lstarFromY(y);
      assertThat(reconstructedLstar).isWithin(1e-8).of(lstar);
    }
  }
}
