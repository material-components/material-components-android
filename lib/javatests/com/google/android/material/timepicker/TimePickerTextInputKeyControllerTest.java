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

import com.google.android.material.R;

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

  private static void pressKeys(EditText editText, int... keycodes) {
    for (int key : keycodes) {
      editText.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, key, 0));
      editText.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_UP, key, 0));
    }
  }
}
