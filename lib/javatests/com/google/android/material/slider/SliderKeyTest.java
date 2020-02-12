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

import com.google.android.material.R;

import static com.google.android.material.slider.SliderHelper.activateFocusedThumb;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.slider.KeyUtils.KeyEventBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/** Tests for key handling of {@link Slider} */
@RunWith(RobolectricTestRunner.class)
public class SliderKeyTest {
  private static final float SLIDER_VALUE_FROM = 0f;
  private static final float SLIDER_VALUE_TO = 100f;

  private Slider slider;
  Float[] values = new Float[] {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f};

  @Before
  public void createSlider() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Bridge);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();

    // Creates slider and adds the listener.
    SliderHelper helper = new SliderHelper(activity);
    slider = helper.getSlider();

    helper.addContentView(activity);

    slider.setValueFrom(SLIDER_VALUE_FROM);
    slider.setValueTo(SLIDER_VALUE_TO);
    slider.setValues(values);
    slider.requestFocus();
  }

  @Test
  public void testThumbFocus_initialFocus_isFirstThumb() {
    assertThat(slider.getFocusedThumbIndex()).isEqualTo(0);
  }

  @Test
  public void testMoveThumbFocus_tab_correctThumbHasFocus() {
    KeyEventBuilder tab = new KeyEventBuilder(KeyEvent.KEYCODE_TAB);
    KeyEventBuilder shiftTab = new KeyEventBuilder(KeyEvent.KEYCODE_TAB, KeyEvent.META_SHIFT_ON);

    sendKeyEventLeftAndRight(tab, shiftTab);
  }

  @Test
  public void testMoveThumbFocus_dPad_correctThumbHasFocus() {
    KeyEventBuilder right = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_RIGHT);
    KeyEventBuilder left = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_LEFT);

    sendKeyEventLeftAndRight(right, left);
  }

  @Test
  public void testMoveThumbFocus_plusMinus_correctThumbHasFocus() {
    KeyEventBuilder plus = new KeyEventBuilder(KeyEvent.KEYCODE_PLUS);
    KeyEventBuilder minus = new KeyEventBuilder(KeyEvent.KEYCODE_MINUS);

    sendKeyEventLeftAndRight(plus, minus);
  }

  @Test
  public void testSelectThirdThumb_clickCenterDPad_activatesThirdThumb() {
    slider.setFocusedThumbIndex(2);

    activateFocusedThumb(slider);

    assertThat(slider.getActiveThumbIndex()).isEqualTo(2);
  }

  @Test
  public void testSelectThirdThumb_moveRight_movesThirdThumbRight() {
    slider.setFocusedThumbIndex(2);

    activateFocusedThumb(slider);

    KeyEventBuilder right = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_RIGHT);
    for (int i = 0; i < 20; i++) {
      right.dispatchEvent(slider);
    }

    assertThat(slider.getValues()).contains(23.0f);
    assertThat(slider.getValues()).doesNotContain(3.0f);
  }

  @Test
  public void testSelectThirdThumb_moveLeft_movesThirdThumbLeft() {
    slider.setFocusedThumbIndex(2);

    activateFocusedThumb(slider);

    KeyEventBuilder left = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_LEFT);
    for (int i = 0; i < 3; i++) {
      left.dispatchEvent(slider);
    }

    assertThat(slider.getValues()).contains(0.0f);
    assertThat(slider.getValues()).doesNotContain(3.0f);
  }

  private void sendKeyEventLeftAndRight(KeyEventBuilder right, KeyEventBuilder left) {
    for (int i = 1; i < values.length; i++) {
      right.dispatchEvent(slider);
      assertWithMessage("moving right").that(slider.getFocusedThumbIndex()).isEqualTo(i);
    }

    for (int i = values.length - 1; i > 0; i--) {
      left.dispatchEvent(slider);
      assertWithMessage("moving left").that(slider.getFocusedThumbIndex()).isEqualTo(i - 1);
    }
  }
}
