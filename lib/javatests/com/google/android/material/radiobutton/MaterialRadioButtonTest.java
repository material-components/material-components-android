/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.material.radiobutton;

import com.google.android.material.test.R;

import static com.google.common.truth.Truth.assertThat;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import androidx.core.widget.CompoundButtonCompat;
import com.google.android.material.color.MaterialColors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class MaterialRadioButtonTest {

  private static final int[] STATE_CHECKED =
      new int[] {android.R.attr.state_enabled, android.R.attr.state_checked};
  private static final int[] STATE_UNCHECKED = new int[] {android.R.attr.state_enabled};

  private View radioButtons;

  @Before
  public void createAndThemeApplicationContext() {
    AppCompatActivity activity = Robolectric.buildActivity(TestActivity.class).setup().get();
    radioButtons = activity.getLayoutInflater().inflate(R.layout.test_design_radiobutton, null);
  }

  @Test
  @Config(sdk = Config.OLDEST_SDK)
  public void testThemeableAppButtonTint() {
    testThemeableButtonTint(
        (RadioButton) radioButtons.findViewById(R.id.test_radiobutton_app_button_tint));
  }

  /**
   * Tests for {@link MaterialRadioButton} and the android:buttonTint attribute. This should only be
   * run for API 22+.
   */
  @Test
  @Config(sdk = VERSION_CODES.M)
  public void testThemeableAndroidButtonTint() {
    testThemeableButtonTint(
        (RadioButton) radioButtons.findViewById(R.id.test_radiobutton_android_button_tint));
  }

  /**
   * Checks that the {@link MaterialRadioButton} buttonTint matches {@link
   * R.color#radiobutton_themeable_attribute_color}.
   */
  static void testThemeableButtonTint(RadioButton radioButton) {
    ColorStateList buttonTintList = CompoundButtonCompat.getButtonTintList(radioButton);
    assertThat(buttonTintList.getColorForState(STATE_CHECKED, Color.BLACK))
        .isEqualTo(MaterialColors.getColor(radioButton, R.attr.colorControlActivated));
    assertThat(buttonTintList.getColorForState(STATE_UNCHECKED, Color.BLACK))
        .isEqualTo(MaterialColors.getColor(radioButton, R.attr.colorOnSurface));
  }

  private static class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setTheme(R.style.Theme_MaterialComponents_Light);
    }
  }
}
