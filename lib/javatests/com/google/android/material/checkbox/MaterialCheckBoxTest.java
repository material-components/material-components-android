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

package com.google.android.material.checkbox;

import com.google.android.material.R;

import static com.google.common.truth.Truth.assertThat;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.core.widget.CompoundButtonCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import com.google.android.material.color.MaterialColors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class MaterialCheckBoxTest {

  private static final int[] STATE_CHECKED =
      new int[] {android.R.attr.state_enabled, android.R.attr.state_checked};
  private static final int[] STATE_UNCHECKED = new int[] {android.R.attr.state_enabled};

  private AppCompatActivity activity;
  private View checkboxes;

  @Before
  public void createAndThemeApplicationContext() {
    activity = Robolectric.buildActivity(TestActivity.class).setup().get();
    checkboxes = activity.getLayoutInflater().inflate(R.layout.test_design_checkbox, null);
  }

  @Test
  @Config(sdk = VERSION_CODES.LOLLIPOP)
  public void testThemeableAppButtonTint() {
    testThemeableButtonTint((CheckBox) checkboxes.findViewById(R.id.test_checkbox_app_button_tint));
  }

  /**
   * Tests for {@link MaterialCheckBox} and the android:buttonTint attribute. This should only be
   * run for API 22+.
   */
  @Test
  @Config(sdk = VERSION_CODES.M)
  public void testThemeableAndroidButtonTint() {
    testThemeableButtonTint(
        (CheckBox) checkboxes.findViewById(R.id.test_checkbox_android_button_tint));
  }

  /**
   * Checks that the {@link MaterialCheckBox} buttonTint matches {@link
   * R.color#checkbox_themeable_attribute_color}.
   */
  static void testThemeableButtonTint(CheckBox checkBox) {
    ColorStateList buttonTintList = CompoundButtonCompat.getButtonTintList(checkBox);
    assertThat(buttonTintList.getColorForState(STATE_CHECKED, Color.BLACK))
        .isEqualTo(MaterialColors.getColor(checkBox, R.attr.colorControlActivated));
    assertThat(buttonTintList.getColorForState(STATE_UNCHECKED, Color.BLACK))
        .isEqualTo(MaterialColors.getColor(checkBox, R.attr.colorOnSurface));
  }

  private static class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setTheme(R.style.Theme_MaterialComponents_Light);
    }
  }
}
