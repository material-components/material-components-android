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

package com.google.android.material.theme;

import com.google.android.material.R;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import androidx.annotation.AttrRes;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.color.MaterialColors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for the Material themes. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class ThemeTest {

  private static final String TAG = ThemeTest.class.getSimpleName();

  private final Context context = ApplicationProvider.getApplicationContext();

  @Test
  public void givenLightTheme_whenResolveColorPrimarySurface_equalsColorPrimary() {
    context.setTheme(R.style.Theme_MaterialComponents_Light);

    assertThat(getColor(R.attr.colorPrimarySurface)).isEqualTo(getColor(R.attr.colorPrimary));
  }

  @Test
  public void givenLightTheme_whenResolveColorOnPrimarySurface_equalsColorOnPrimary() {
    context.setTheme(R.style.Theme_MaterialComponents_Light);

    assertThat(getColor(R.attr.colorOnPrimarySurface)).isEqualTo(getColor(R.attr.colorOnPrimary));
  }

  @Test
  public void givenDarkTheme_whenResolveColorPrimarySurface_equalsColorSurface() {
    context.setTheme(R.style.Theme_MaterialComponents);

    assertThat(getColor(R.attr.colorPrimarySurface)).isEqualTo(getColor(R.attr.colorSurface));
  }

  @Test
  public void givenDarkTheme_whenResolveColorOnPrimarySurface_equalsColorOnSurface() {
    context.setTheme(R.style.Theme_MaterialComponents);

    assertThat(getColor(R.attr.colorOnPrimarySurface)).isEqualTo(getColor(R.attr.colorOnSurface));
  }

  private int getColor(@AttrRes int colorAttributeResId) {
    return MaterialColors.getColor(context, colorAttributeResId, TAG);
  }
}
