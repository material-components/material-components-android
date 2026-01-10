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

import com.google.android.material.test.R;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.appcompat.view.ContextThemeWrapper;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link MaterialThemeOverlay}. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.OLDEST_SDK)
public class MaterialThemeOverlayTest {

  private Context context;

  @Before
  public void setUp() {
    context = getInstrumentation().getTargetContext();
  }

  @Test
  public void wrap_hasCorrectAttributes_withThemeOverlay() {
    AttributeSet attributes = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.materialThemeOverlay, "@style/ThemeOverlayColorAccentRed")
        .build();

    assertAttributesWithColor(attributes, Color.RED);
  }

  @Test
  public void wrap_hasCorrectAttributes_withThemeOverlayAndAndroidTheme() {
    AttributeSet attributes = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.materialThemeOverlay, "@style/ThemeOverlayColorAccentRed")
        .addAttribute(android.R.attr.theme, "@style/AndroidThemeColorAccentYellow")
        .build();

    assertAttributesWithColor(attributes, Color.YELLOW);
  }

  @Test
  public void wrap_hasCorrectAttributes_withoutThemeOverlay() {
    AttributeSet attributes = Robolectric.buildAttributeSet().build();

    TypedValue tv = new TypedValue();
    context.getTheme().resolveAttribute(android.R.attr.colorAccent, tv, true);
    int initialColorAccent = tv.data;

    assertAttributesWithColor(attributes, initialColorAccent);
  }

  @Test
  public void wrap_doesNotWrapTwice_withSameAttributeSet() {
    AttributeSet attributes = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.materialThemeOverlay, "@style/ThemeOverlayColorAccentRed")
        .build();

    Context themedContext = MaterialThemeOverlay.wrap(context, attributes, 0, 0);
    Context themedContext2 = MaterialThemeOverlay
        .wrap(themedContext, attributes, 0, 0);

    assertThat(themedContext).isInstanceOf(ContextThemeWrapper.class);
    assertThat(themedContext).isSameInstanceAs(themedContext2);
  }

  private void assertAttributesWithColor(AttributeSet attributes, int color) {
    TypedValue tv = new TypedValue();
    Context themeContext = MaterialThemeOverlay.wrap(context, attributes, 0, 0);
    themeContext.getTheme().resolveAttribute(android.R.attr.colorAccent, tv, true);

    assertThat(tv.data).isEqualTo(color);
  }
}
