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

import com.google.android.material.test.R;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.color.utilities.Blend;
import com.google.android.material.color.utilities.Hct;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link MaterialColors}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public final class MaterialColorsTest {

  private static final int RED = 0xffff0000;
  private static final int BLUE = 0xff0000ff;

  private final Context context = ApplicationProvider.getApplicationContext();

  @Before
  public void themeApplicationContext() {
    context.setTheme(R.style.Theme_Material3_Light);
  }

  @Test
  public void harmonize_redToPrimary() {
    assertThat(MaterialColors.harmonizeWithPrimary(context, RED))
        .isEqualTo(
            Blend.harmonize(
                RED,
                MaterialColors.getColor(
                    context, R.attr.colorPrimary, MaterialColorsTest.class.getCanonicalName())));
  }

  @Test
  public void harmonize_redToBlue() {
    assertThat(MaterialColors.harmonize(RED, BLUE)).isEqualTo(Blend.harmonize(RED, BLUE));
  }

  @Test
  public void getColorRoles_withContext_lightTheme() {
    ColorRoles colorRoles = MaterialColors.getColorRoles(context, RED);

    assertThat(colorRoles.getAccent()).isEqualTo(getColorRole(RED, 40));
    assertThat(colorRoles.getOnAccent()).isEqualTo(getColorRole(RED, 100));
    assertThat(colorRoles.getAccentContainer()).isEqualTo(getColorRole(RED, 90));
    assertThat(colorRoles.getOnAccentContainer()).isEqualTo(getColorRole(RED, 10));
  }

  @Test
  public void getColorRoles_withContext_darkTheme() {
    context.setTheme(R.style.Theme_Material3_Dark);

    ColorRoles colorRoles = MaterialColors.getColorRoles(context, RED);

    assertThat(colorRoles.getAccent()).isEqualTo(getColorRole(RED, 80));
    assertThat(colorRoles.getOnAccent()).isEqualTo(getColorRole(RED, 20));
    assertThat(colorRoles.getAccentContainer()).isEqualTo(getColorRole(RED, 30));
    assertThat(colorRoles.getOnAccentContainer()).isEqualTo(getColorRole(RED, 90));
  }

  @Test
  public void getColorRoles_withBoolean_lightTheme() {
    ColorRoles colorRoles = MaterialColors.getColorRoles(RED, /* isLightTheme= */ true);

    assertThat(colorRoles.getAccent()).isEqualTo(getColorRole(RED, 40));
    assertThat(colorRoles.getOnAccent()).isEqualTo(getColorRole(RED, 100));
    assertThat(colorRoles.getAccentContainer()).isEqualTo(getColorRole(RED, 90));
    assertThat(colorRoles.getOnAccentContainer()).isEqualTo(getColorRole(RED, 10));
  }

  @Test
  public void getColorRoles_withBoolean_darkTheme() {
    context.setTheme(R.style.Theme_Material3_Dark);

    ColorRoles colorRoles = MaterialColors.getColorRoles(RED, /* isLightTheme= */ false);

    assertThat(colorRoles.getAccent()).isEqualTo(getColorRole(RED, 80));
    assertThat(colorRoles.getOnAccent()).isEqualTo(getColorRole(RED, 20));
    assertThat(colorRoles.getAccentContainer()).isEqualTo(getColorRole(RED, 30));
    assertThat(colorRoles.getOnAccentContainer()).isEqualTo(getColorRole(RED, 90));
  }

  private static int getColorRole(@ColorInt int color, @IntRange(from = 0, to = 100) int tone) {
    Hct hctColor = Hct.fromInt(color);
    hctColor.setTone(tone);
    return hctColor.toInt();
  }
}
