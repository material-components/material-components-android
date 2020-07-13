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

package com.google.android.material.timepicker;

import static com.google.common.truth.Truth.assertThat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ActivityScenario.ActivityAction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Unit tests for {@link TimePickerDialog} */
@RunWith(RobolectricTestRunner.class)
public class TimePickerDialogUnitTest {

  @Test
  public void restoreSavedInstanceState_loadsSavedState() {

    try (ActivityScenario<AppCompatActivity> scenario =
        ActivityScenario.launch(AppCompatActivity.class)) {

      scenario.onActivity(
          new ActivityAction<AppCompatActivity>() {
            @Override
            public void perform(AppCompatActivity activity) {
              TimePickerDialog timePickerDialog = TimePickerDialog.newInstance();
              timePickerDialog.setHour(10);
              timePickerDialog.setMinute(47);
              timePickerDialog.show(activity.getSupportFragmentManager(), "");
            }
          });

      // Re launch the activity
      scenario.recreate();
      scenario.onActivity(
          new ActivityAction<AppCompatActivity>() {
            @Override
            public void perform(AppCompatActivity activity) {
              TimePickerDialog timePickerDialog =
                  (TimePickerDialog) activity.getSupportFragmentManager().getFragments().get(0);
              assertThat(timePickerDialog.getMinute()).isEqualTo(47);
              assertThat(timePickerDialog.getHour()).isEqualTo(10);
            }
          });
    }
  }
}
