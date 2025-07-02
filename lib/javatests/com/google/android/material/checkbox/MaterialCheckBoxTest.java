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

import com.google.android.material.test.R;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import androidx.core.widget.CompoundButtonCompat;
import com.google.android.material.checkbox.MaterialCheckBox.OnCheckedStateChangedListener;
import com.google.android.material.checkbox.MaterialCheckBox.OnErrorChangedListener;
import com.google.android.material.color.MaterialColors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class MaterialCheckBoxTest {

  private static final int[] STATE_CHECKED =
      new int[] {android.R.attr.state_enabled, android.R.attr.state_checked};
  private static final int[] STATE_ERROR =
      new int[] {android.R.attr.state_enabled, R.attr.state_error};
  private static final int[] STATE_UNCHECKED = new int[] {android.R.attr.state_enabled};

  private AppCompatActivity activity;
  private View checkboxes;
  private MaterialCheckBox materialCheckBox;
  private final OnCheckedChangeListener mockCheckedListener =
      Mockito.mock(OnCheckedChangeListener.class);
  private final OnErrorChangedListener mockErrorListener =
      Mockito.mock(OnErrorChangedListener.class);
  private final OnCheckedStateChangedListener mockStateListener =
      Mockito.mock(OnCheckedStateChangedListener.class);

  @Before
  public void createAndThemeApplicationContext() {
    activity = Robolectric.buildActivity(TestActivity.class).setup().get();
    checkboxes = activity.getLayoutInflater().inflate(R.layout.test_design_checkbox, null);
    materialCheckBox = checkboxes.findViewById(R.id.test_checkbox);
  }

  @Test
  public void testSetCheckedState_checked_succeeds() {
    materialCheckBox.setCheckedState(MaterialCheckBox.STATE_CHECKED);

    assertThat(materialCheckBox.isChecked()).isTrue();
    assertThat(materialCheckBox.getCheckedState()).isEqualTo(MaterialCheckBox.STATE_CHECKED);
  }

  @Test
  public void testSetCheckedState_unchecked_succeeds() {
    materialCheckBox.setCheckedState(MaterialCheckBox.STATE_CHECKED);
    assertThat(materialCheckBox.isChecked()).isTrue();

    materialCheckBox.setCheckedState(MaterialCheckBox.STATE_UNCHECKED);

    assertThat(materialCheckBox.isChecked()).isFalse();
    assertThat(materialCheckBox.getCheckedState()).isEqualTo(MaterialCheckBox.STATE_UNCHECKED);
  }

  @Test
  public void testSetCheckedState_indeterminate_succeeds() {
    materialCheckBox.setCheckedState(MaterialCheckBox.STATE_INDETERMINATE);

    assertThat(materialCheckBox.isChecked()).isFalse();
    assertThat(materialCheckBox.getCheckedState()).isEqualTo(MaterialCheckBox.STATE_INDETERMINATE);
  }

  @Test
  public void testSetCheckedState_checkedToIndeterminate_succeeds() {
    materialCheckBox.setCheckedState(MaterialCheckBox.STATE_CHECKED);

    materialCheckBox.setCheckedState(MaterialCheckBox.STATE_INDETERMINATE);

    assertThat(materialCheckBox.isChecked()).isFalse();
    assertThat(materialCheckBox.getCheckedState()).isEqualTo(MaterialCheckBox.STATE_INDETERMINATE);
  }

  @Test
  public void testSetCheckedState_checked_callsStateAndCheckedListeners() {
    materialCheckBox.addOnCheckedStateChangedListener(mockStateListener);
    materialCheckBox.setOnCheckedChangeListener(mockCheckedListener);

    materialCheckBox.setCheckedState(MaterialCheckBox.STATE_CHECKED);

    verify(mockStateListener)
        .onCheckedStateChangedListener(materialCheckBox, MaterialCheckBox.STATE_CHECKED);
    verify(mockCheckedListener).onCheckedChanged(materialCheckBox, /* isChecked= */ true);
  }

  @Test
  public void testSetCheckedState_unchecked_callsStateAndCheckedListeners() {
    materialCheckBox.setCheckedState(MaterialCheckBox.STATE_CHECKED);
    materialCheckBox.addOnCheckedStateChangedListener(mockStateListener);
    materialCheckBox.setOnCheckedChangeListener(mockCheckedListener);

    materialCheckBox.setCheckedState(MaterialCheckBox.STATE_UNCHECKED);

    verify(mockStateListener)
        .onCheckedStateChangedListener(materialCheckBox, MaterialCheckBox.STATE_UNCHECKED);
    verify(mockCheckedListener).onCheckedChanged(materialCheckBox, /* isChecked= */ false);
  }

  @Test
  public void testSetCheckedState_indeterminate_callsStateListener() {
    materialCheckBox.addOnCheckedStateChangedListener(mockStateListener);
    materialCheckBox.setOnCheckedChangeListener(mockCheckedListener);

    materialCheckBox.setCheckedState(MaterialCheckBox.STATE_INDETERMINATE);

    verify(mockStateListener)
        .onCheckedStateChangedListener(materialCheckBox, MaterialCheckBox.STATE_INDETERMINATE);
    verify(mockCheckedListener, never()).onCheckedChanged(materialCheckBox, /* isChecked= */ false);
  }

  @Test
  public void testSetChecked_succeeds() {
    materialCheckBox.setChecked(true);

    assertThat(materialCheckBox.isChecked()).isTrue();
    assertThat(materialCheckBox.getCheckedState()).isEqualTo(MaterialCheckBox.STATE_CHECKED);
  }

  @Test
  public void testSetUnchecked_succeeds() {
    materialCheckBox.setChecked(true);
    assertThat(materialCheckBox.isChecked()).isTrue();

    materialCheckBox.setChecked(false);

    assertThat(materialCheckBox.isChecked()).isFalse();
    assertThat(materialCheckBox.getCheckedState()).isEqualTo(MaterialCheckBox.STATE_UNCHECKED);
  }

  @Test
  public void testIndeterminate_onClick_becomesChecked() {
    materialCheckBox.setCheckedState(MaterialCheckBox.STATE_INDETERMINATE);

    materialCheckBox.performClick();

    assertThat(materialCheckBox.isChecked()).isTrue();
    assertThat(materialCheckBox.getCheckedState()).isEqualTo(MaterialCheckBox.STATE_CHECKED);
  }

  @Test
  public void testIndeterminate_setChecked_becomesChecked() {
    materialCheckBox.setCheckedState(MaterialCheckBox.STATE_INDETERMINATE);

    materialCheckBox.setChecked(true);

    assertThat(materialCheckBox.isChecked()).isTrue();
    assertThat(materialCheckBox.getCheckedState()).isEqualTo(MaterialCheckBox.STATE_CHECKED);
  }

  @Test
  public void testIndeterminate_setUnchecked_becomesUnchecked() {
    materialCheckBox.setCheckedState(MaterialCheckBox.STATE_INDETERMINATE);

    materialCheckBox.setChecked(false);

    assertThat(materialCheckBox.isChecked()).isFalse();
    assertThat(materialCheckBox.getCheckedState()).isEqualTo(MaterialCheckBox.STATE_UNCHECKED);
  }

  @Test
  public void testSetError_succeeds() {
    materialCheckBox.setErrorShown(true);

    assertThat(materialCheckBox.isErrorShown()).isTrue();
  }

  @Test
  public void testSetError_callsListener() {
    materialCheckBox.setErrorShown(false);
    materialCheckBox.addOnErrorChangedListener(mockErrorListener);

    materialCheckBox.setErrorShown(true);

    verify(mockErrorListener).onErrorChanged(materialCheckBox, /* errorShown= */ true);
  }

  @Test
  public void testSetError_withSameValue_doesNotCallListener() {
    materialCheckBox.setErrorShown(false);
    materialCheckBox.addOnErrorChangedListener(mockErrorListener);

    materialCheckBox.setErrorShown(false);

    verify(mockErrorListener, never()).onErrorChanged(materialCheckBox, /* errorShown= */ false);
  }

  @Test
  public void testSetErrorA11yLabel_succeeds() {
    materialCheckBox.setErrorAccessibilityLabel("error");

    assertThat(materialCheckBox.getErrorAccessibilityLabel()).isNotNull();
    assertThat(materialCheckBox.getErrorAccessibilityLabel().toString()).isEqualTo("error");
  }

  @Test
  @Config(sdk = Config.OLDEST_SDK)
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
    assertThat(buttonTintList.getColorForState(STATE_ERROR, Color.BLACK))
        .isEqualTo(MaterialColors.getColor(checkBox, R.attr.colorError));
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
