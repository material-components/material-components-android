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

import com.google.android.material.test.R;

import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.MINUTE;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link TimePickerTextInputKeyController} */
@RunWith(RobolectricTestRunner.class)
public class TimePickerTextInputKeyControllerTest {

  private ChipTextInputComboView hourInput;
  private ChipTextInputComboView minuteInput;
  private TimeModel timeModel;

  @Before
  public void createController() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Light);
    Activity activity = Robolectric.buildActivity(Activity.class).setup().start().get();
    activity.setTheme(R.style.ThemeOverlay_MaterialComponents_TimePicker);
    activity.setTheme(R.style.ThemeOverlay_MaterialComponents_TimePicker_Display_TextInputEditText);
    timeModel = new TimeModel();
    minuteInput = new ChipTextInputComboView(activity);
    hourInput = new ChipTextInputComboView(activity);

    TimePickerTextInputKeyController controller =
        new TimePickerTextInputKeyController(hourInput, minuteInput, timeModel);
    controller.bind();
  }

  @Test
  public void controller_sendImeAction_switchesToMinutes() {
    hourInput.getTextInput().getEditText().onEditorAction(EditorInfo.IME_ACTION_NEXT);
    shadowOf(getMainLooper()).idle();

    assertThat(timeModel.selection).isEqualTo(MINUTE);
  }

  @Test
  public void controller_pressingBackSpace_switchesToHour() {
    // first switch to minute view
    hourInput.getTextInput().getEditText().onEditorAction(EditorInfo.IME_ACTION_NEXT);
    EditText editText = minuteInput.getTextInput().getEditText();
    pressKeys(editText, KeyEvent.KEYCODE_DEL);
    shadowOf(getMainLooper()).idle();

    assertThat(timeModel.selection).isEqualTo(HOUR);
  }

  @Test
  public void controller_clearPrefilledText_shouldClearWhenSelection0() {
    EditText editText = hourInput.getTextInput().getEditText();
    editText.setText("00");
    editText.setSelection(0);
    pressKeys(editText, KeyEvent.KEYCODE_0);
    shadowOf(getMainLooper()).idle();

    assertThat(editText.getText().length()).isEqualTo(0);
  }

  @Test
  public void controller_clearPrefilledText_shouldNotClearWhenSelection1() {
    EditText editText = hourInput.getTextInput().getEditText();
    editText.setText("00");
    editText.setSelection(1);
    pressKeys(editText, KeyEvent.KEYCODE_0);
    shadowOf(getMainLooper()).idle();

    assertThat(editText.getText().length()).isEqualTo(2);
  }

  @Test
  public void controller_clearPrefilledText_shouldNotClearWhenSelection2() {
    EditText editText = hourInput.getTextInput().getEditText();
    editText.setText("00");
    editText.setSelection(2);
    pressKeys(editText, KeyEvent.KEYCODE_0);
    shadowOf(getMainLooper()).idle();

    assertThat(editText.getText().length()).isEqualTo(2);
  }

  @Test
  public void controller_clearPrefilledText_shouldNotClearWhenPartialText() {
    EditText editText = hourInput.getTextInput().getEditText();
    editText.setText("0");
    editText.setSelection(0);
    pressKeys(editText, KeyEvent.KEYCODE_0);
    shadowOf(getMainLooper()).idle();

    assertThat(editText.getText().length()).isEqualTo(1);
  }

  @Test
  public void controller_clearPrefilledText_shouldNotClearWhenNotDigit() {
    EditText editText = hourInput.getTextInput().getEditText();
    editText.setText("0");
    editText.setSelection(0);
    pressKeys(editText, KeyEvent.KEYCODE_DPAD_RIGHT);
    shadowOf(getMainLooper()).idle();

    assertThat(editText.getText().length()).isEqualTo(1);
  }

  @Test
  public void afterTextChanged_validHourInput_formatsText() {
    EditText editText = hourInput.getTextInput().getEditText();
    editText.setText("1");
    shadowOf(getMainLooper()).idle();

    assertThat(hourInput.getChipText().toString()).isEqualTo("01");
  }

  @Test
  public void afterTextChanged_invalidHourInput_resetsToDefault() {
    EditText editText = hourInput.getTextInput().getEditText();
    editText.setText("1");
    editText.setText("+");
    shadowOf(getMainLooper()).idle();

    assertThat(hourInput.getChipText().toString()).isEqualTo("00");
  }

  @Test
  public void afterTextChanged_validMinuteInput_formatsText() {
    EditText editText = minuteInput.getTextInput().getEditText();
    editText.setText("1");
    shadowOf(getMainLooper()).idle();

    assertThat(minuteInput.getChipText().toString()).isEqualTo("01");
  }

  @Test
  public void afterTextChanged_invalidMinuteInput_resetsToDefault() {
    EditText editText = minuteInput.getTextInput().getEditText();
    editText.setText("1");
    editText.setText("+");
    shadowOf(getMainLooper()).idle();

    assertThat(minuteInput.getChipText().toString()).isEqualTo("00");
  }

  private static void pressKeys(EditText editText, int... keycodes) {
    for (int key : keycodes) {
      editText.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, key, 0));
      editText.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_UP, key, 0));
    }
  }
}
