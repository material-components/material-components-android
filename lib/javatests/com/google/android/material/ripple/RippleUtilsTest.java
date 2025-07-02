/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.google.android.material.ripple;

import static com.google.common.truth.Truth.assertThat;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.ExpectedLogMessagesRule;
import org.robolectric.shadows.ShadowLog;

/** Unit tests for {@link RippleUtils}. */
@RunWith(RobolectricTestRunner.class)
public class RippleUtilsTest {

  @Rule
  public final ExpectedLogMessagesRule logged = new ExpectedLogMessagesRule();

  @Test
  public void testValidateColor_null_returnsTransparent() {
    assertThat(RippleUtils.sanitizeRippleDrawableColor(null))
        .isEqualTo(ColorStateList.valueOf(Color.TRANSPARENT));
  }

  @Test
  public void testValidateColor_transparent_returnsSelf_noWarning() {
    assertThat(RippleUtils.sanitizeRippleDrawableColor(ColorStateList.valueOf(Color.TRANSPARENT)))
        .isEqualTo(ColorStateList.valueOf(Color.TRANSPARENT));
  }

  @Test
  public void testValidateColor_solidColor_returnsSelf_noWarning() {
    ColorStateList rippleColor = ColorStateList.valueOf(Color.BLUE);
    assertThat(RippleUtils.sanitizeRippleDrawableColor(rippleColor)).isEqualTo(rippleColor);
  }

  @Test
  @Config(sdk = VERSION_CODES.M)
  public void testValidateColor_transparentDefaultColor_returnsSelf_lollipopMr1_logsWarning() {
    ColorStateList rippleColor = createTransparentDefaultColor();
    assertThat(RippleUtils.sanitizeRippleDrawableColor(rippleColor)).isEqualTo(rippleColor);
    logged.expectLogMessage(
        Log.WARN, RippleUtils.LOG_TAG, RippleUtils.TRANSPARENT_DEFAULT_COLOR_WARNING);
  }

  @Test
  @Config(sdk = VERSION_CODES.O_MR1)
  public void testValidateColor_transparentDefaultColor_returnsSelf_oreoMr1_logsWarning() {
    ColorStateList rippleColor = createTransparentDefaultColor();
    assertThat(RippleUtils.sanitizeRippleDrawableColor(rippleColor)).isEqualTo(rippleColor);
    logged.expectLogMessage(
        Log.WARN, RippleUtils.LOG_TAG, RippleUtils.TRANSPARENT_DEFAULT_COLOR_WARNING);
  }

  @Test
  @Config(sdk = VERSION_CODES.P)
  public void testValidateColor_transparentDefaultColor_returnsSelf_pie_noWarning() {
    ColorStateList rippleColor = createTransparentDefaultColor();
    assertThat(RippleUtils.sanitizeRippleDrawableColor(rippleColor)).isEqualTo(rippleColor);
    assertThat(ShadowLog.getLogsForTag(RippleUtils.LOG_TAG)).isEmpty();
  }

  private ColorStateList createTransparentDefaultColor() {
    return new ColorStateList(
        new int[][] {new int[] {android.R.attr.state_pressed}, new int[]{}},
        new int[] {Color.BLUE, Color.TRANSPARENT});
  }
}
