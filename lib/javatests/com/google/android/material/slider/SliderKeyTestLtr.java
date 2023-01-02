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

package com.google.android.material.slider;

import static com.google.common.truth.Truth.assertThat;

import android.view.KeyEvent;
import android.view.View;
import com.google.android.material.slider.KeyUtils.KeyEventBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Tests for key handling of {@link Slider} in left-to-right layout.
 */
@RunWith(RobolectricTestRunner.class)
public final class SliderKeyTestLtr extends SliderKeyTestCommon {

  @Test
  public void testThumbFocus_focusLeft_focusesLastThumb() {
    slider.requestFocus(View.FOCUS_LEFT);

    assertThat(slider.getFocusedThumbIndex()).isEqualTo(countTestValues() - 1);
  }

  @Test
  public void testThumbFocus_focusRight_focusesFirstThumb() {
    slider.requestFocus(View.FOCUS_RIGHT);

    assertThat(slider.getFocusedThumbIndex()).isEqualTo(0);
  }

  @Test
  public void testMoveThumbFocus_dPad_correctThumbHasFocus() {
    slider.requestFocus();

    KeyEventBuilder right = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_RIGHT);
    KeyEventBuilder left = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_LEFT);

    sendKeyEventThereAndBack(right, left);
  }

  @Test
  public void testActivateThirdThumb_moveRight_movesThirdThumbRight() {
    slider.requestFocus();

    slider.setFocusedThumbIndex(2);

    activateFocusedThumb();

    KeyEventBuilder right = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_RIGHT);
    for (int i = 0; i < 20; i++) {
      right.dispatchEvent(slider);
    }

    assertThat(slider.getValues()).doesNotContain(3.0f);
  }

  @Test
  public void testActivateThirdThumb_moveLeft_movesThirdThumbLeft() {
    slider.requestFocus();

    slider.setFocusedThumbIndex(2);

    activateFocusedThumb();

    KeyEventBuilder left = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_LEFT);
    for (int i = 0; i < 3; i++) {
      left.dispatchEvent(slider);
    }

    assertThat(slider.getValues()).doesNotContain(3.0f);
  }
}
