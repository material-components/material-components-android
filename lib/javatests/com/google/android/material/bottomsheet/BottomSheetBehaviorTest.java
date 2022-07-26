/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.google.android.material.bottomsheet;

import com.google.android.material.test.R;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.DEFAULT_SIGNIFICANT_VEL_THRESHOLD;
import static com.google.common.truth.Truth.assertThat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link com.google.android.material.bottomsheet.BottomSheetBehavior}. */
@RunWith(RobolectricTestRunner.class)
public class BottomSheetBehaviorTest {

  AppCompatActivity activity;

  @Before
  public void createActivity() {
    activity = Robolectric.buildActivity(TestActivity.class).setup().get();
  }

  @Test
  public void createBottomSheet_withDimenOffset_hasCorrectOffset() {
    AttributeSet attributes =
        Robolectric.buildAttributeSet()
            .addAttribute(R.attr.behavior_expandedOffset, "10dp")
            .addAttribute(R.attr.behavior_fitToContents, "false")
            .build();

    BottomSheetBehavior<View> behavior = new BottomSheetBehavior<>(activity, attributes);

    assertThat(behavior.getExpandedOffset()).isEqualTo(10);
  }

  @Test
  public void createBottomSheet_withIntegerRefOffset_hasCorrectOffset() {
    AttributeSet attributes =
        Robolectric.buildAttributeSet()
            .addAttribute(R.attr.behavior_expandedOffset, "@integer/abc_config_activityDefaultDur")
            .addAttribute(R.attr.behavior_fitToContents, "false")
            .build();

    BottomSheetBehavior<View> behavior = new BottomSheetBehavior<>(activity, attributes);

    assertThat(behavior.getExpandedOffset()).isEqualTo(220);
  }

  @Test
  public void setSignificantVelocityThreshold() {
    AttributeSet attributes =
        Robolectric.buildAttributeSet()
            .build();
    BottomSheetBehavior<View> behavior = new BottomSheetBehavior<>(activity, attributes);
    // Test the default value.
    assertThat(behavior.getSignificantVelocityThreshold())
        .isEqualTo(DEFAULT_SIGNIFICANT_VEL_THRESHOLD);

    int significantVelocityValue = 1;
    behavior.setSignificantVelocityThreshold(significantVelocityValue);

    assertThat(behavior.getSignificantVelocityThreshold()).isEqualTo(significantVelocityValue);
  }

  private static class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setTheme(R.style.Theme_AppCompat);
    }
  }
}
