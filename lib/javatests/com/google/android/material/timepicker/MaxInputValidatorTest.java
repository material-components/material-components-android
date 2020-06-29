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

import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_1;
import static android.view.KeyEvent.KEYCODE_2;
import static android.view.KeyEvent.KEYCODE_3;
import static android.view.KeyEvent.KEYCODE_DEL;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Robolectric.buildActivity;

import android.app.Activity;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.widget.EditText;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MaxInputValidatorTest {
  private EditText editText;

  @Before
  public void setUp() {
    editText = new EditText(buildActivity(Activity.class).create().get());
    editText.setFilters(new InputFilter[] {new MaxInputValidator(10)});
  }

  @Test
  public void editText_hasCorrectText_whenNumberIsGreater() {
    pressKeys(KEYCODE_1, KEYCODE_2);

    assertThat(editText.getText().toString()).isEqualTo("1");
  }

  @Test
  public void editText_hasCorrectText_whenNumberIsSmaller() {
    pressKeys(KEYCODE_0, KEYCODE_2);

    assertThat(editText.getText().toString()).isEqualTo("02");
  }

  @Test
  public void editText_hasCorrectText_withDelete() {
    pressKeys(KEYCODE_2, KEYCODE_DEL, KEYCODE_3);

    assertThat(editText.getText().toString()).isEqualTo("3");
  }

  private void pressKeys(int... keycodes) {
    for (int key : keycodes) {
      editText.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, key, 0));
      editText.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_UP, key, 0));
    }
  }
}
