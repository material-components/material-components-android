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

import static com.google.android.material.slider.SliderHelper.clickDpadCenter;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import androidx.customview.widget.ExploreByTouchHelper;
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
  }

  @Test
  public void testThumbFocus_initialFocus_isFirstThumb() {
    slider.requestFocus();

    assertThat(slider.getFocusedThumbIndex()).isEqualTo(0);
  }

  @Test
  public void testThumbFocus_focusForward_focusesFirstThumb() {
    slider.requestFocus(View.FOCUS_FORWARD);

    assertThat(slider.getFocusedThumbIndex()).isEqualTo(0);
  }

  @Test
  public void testThumbFocus_focusBackward_focusesLastThumb() {
    slider.requestFocus(View.FOCUS_BACKWARD);

    assertThat(slider.getFocusedThumbIndex()).isEqualTo(values.length - 1);
  }

  @Test
  public void testThumbFocus_focusLeft_focusesLastThumb() {
    slider.requestFocus(View.FOCUS_LEFT);

    assertThat(slider.getFocusedThumbIndex()).isEqualTo(values.length - 1);
  }

  @Test
  public void testThumbFocus_focusRight_focusesFirstThumb() {
    slider.requestFocus(View.FOCUS_RIGHT);

    assertThat(slider.getFocusedThumbIndex()).isEqualTo(0);
  }

  @Test
  public void testThumbFocus_reacquiredFocusDown_focusesPreviouslyFocusedThumb() {
    slider.requestFocus();
    slider.setFocusedThumbIndex(2);
    slider.clearFocus();

    slider.requestFocus(View.FOCUS_DOWN);

    assertThat(slider.getFocusedThumbIndex()).isEqualTo(2);
  }

  @Test
  public void testThumbFocus_reacquiredFocusUp_focusesPreviouslyFocusedThumb() {
    slider.requestFocus();
    slider.setFocusedThumbIndex(2);
    slider.clearFocus();

    slider.requestFocus(View.FOCUS_UP);

    assertThat(slider.getFocusedThumbIndex()).isEqualTo(2);
  }

  @Test
  public void testThumbFocus_clearFocus_clearsVirtualViewFocus() {
    slider.requestFocus();

    slider.clearFocus();

    assertThat(slider.getAccessibilityFocusedVirtualViewId())
        .isEqualTo(ExploreByTouchHelper.INVALID_ID);
  }

  @Test
  public void testMoveThumbFocus_tab_correctThumbHasFocus() {
    slider.requestFocus();

    KeyEventBuilder tab = new KeyEventBuilder(KeyEvent.KEYCODE_TAB);
    KeyEventBuilder shiftTab = new KeyEventBuilder(KeyEvent.KEYCODE_TAB, KeyEvent.META_SHIFT_ON);

    sendKeyEventLeftAndRight(tab, shiftTab);
  }

  @Test
  public void testMoveThumbFocus_dPad_correctThumbHasFocus() {
    slider.requestFocus();

    KeyEventBuilder right = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_RIGHT);
    KeyEventBuilder left = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_LEFT);

    sendKeyEventLeftAndRight(right, left);
  }

  @Test
  public void testMoveThumbFocus_plusMinus_correctThumbHasFocus() {
    slider.requestFocus();

    KeyEventBuilder plus = new KeyEventBuilder(KeyEvent.KEYCODE_PLUS);
    KeyEventBuilder minus = new KeyEventBuilder(KeyEvent.KEYCODE_MINUS);

    sendKeyEventLeftAndRight(plus, minus);
  }

  @Test
  public void testFocusThirdThumb_clickCenterDPad_activatesThirdThumb() {
    slider.requestFocus();

    slider.setFocusedThumbIndex(2);

    clickDpadCenter(slider);

    assertThat(slider.getActiveThumbIndex()).isEqualTo(2);
  }

  private void activateFocusedThumb(Slider s) {
    int focusedThumbIndex = s.getFocusedThumbIndex();
    if (focusedThumbIndex != -1) {
      // Clicking D-Pad in Slider isn't idempotent. Only do it here if we're changing focused thumb.
      if (focusedThumbIndex != s.getActiveThumbIndex()) {
        clickDpadCenter(s);
      }
    }
  }

  @Test
  public void testActivateThirdThumb_clickCenterDPad_deactivatesThirdThumb() {
    slider.requestFocus();

    slider.setFocusedThumbIndex(2);

    activateFocusedThumb(slider);

    clickDpadCenter(slider);

    assertThat(slider.getActiveThumbIndex()).isEqualTo(-1);
  }

  @Test
  public void testActivateThirdThumb_moveRight_movesThirdThumbRight() {
    slider.requestFocus();

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
  public void testActivateThirdThumb_moveLeft_movesThirdThumbLeft() {
    slider.requestFocus();

    slider.setFocusedThumbIndex(2);

    activateFocusedThumb(slider);

    KeyEventBuilder left = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_LEFT);
    for (int i = 0; i < 3; i++) {
      left.dispatchEvent(slider);
    }

    assertThat(slider.getValues()).contains(0.0f);
    assertThat(slider.getValues()).doesNotContain(3.0f);
  }

  @Test
  public void testFocusDefaultThumb_clickUpDPad_unhandled() {
    slider.requestFocus();

    KeyEventBuilder up = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_UP);
    boolean handledDown = slider.dispatchKeyEvent(up.buildDown());
    boolean handledUp = slider.dispatchKeyEvent(up.buildUp());

    assertThat(handledDown).isFalse();
    assertThat(handledUp).isFalse();
  }

  @Test
  public void testActivateDefaultThumb_clickUpDPad_unhandled() {
    slider.requestFocus();

    activateFocusedThumb(slider);

    KeyEventBuilder up = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_UP);
    boolean handledDown = slider.dispatchKeyEvent(up.buildDown());
    boolean handledUp = slider.dispatchKeyEvent(up.buildUp());

    assertThat(handledDown).isFalse();
    assertThat(handledUp).isFalse();
  }

  @Test
  public void testFocusDefaultThumb_clickDownDPad_unhandled() {
    slider.requestFocus();

    KeyEventBuilder down = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_DOWN);
    boolean handledDown = slider.dispatchKeyEvent(down.buildDown());
    boolean handledUp = slider.dispatchKeyEvent(down.buildUp());

    assertThat(handledDown).isFalse();
    assertThat(handledUp).isFalse();
  }

  @Test
  public void testActivateDefaultThumb_clickDownDPad_unhandled() {
    slider.requestFocus();

    activateFocusedThumb(slider);

    KeyEventBuilder down = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_DOWN);
    boolean handledDown = slider.dispatchKeyEvent(down.buildDown());
    boolean handledUp = slider.dispatchKeyEvent(down.buildUp());

    assertThat(handledDown).isFalse();
    assertThat(handledUp).isFalse();
  }

  @Test
  public void testFocusFirstThumb_shiftTab_unhandled() {
    slider.requestFocus();

    slider.setFocusedThumbIndex(0);

    KeyEventBuilder tab = new KeyEventBuilder(KeyEvent.KEYCODE_TAB);
    tab.meta = KeyEvent.META_SHIFT_ON;
    boolean handledDown = slider.dispatchKeyEvent(tab.buildDown());
    boolean handledUp = slider.dispatchKeyEvent(tab.buildUp());

    assertThat(handledDown).isFalse();
    assertThat(handledUp).isFalse();
  }

  @Test
  public void testActivateFirstThumb_shiftTab_unhandled() {
    slider.requestFocus();

    slider.setFocusedThumbIndex(0);

    activateFocusedThumb(slider);

    KeyEventBuilder tab = new KeyEventBuilder(KeyEvent.KEYCODE_TAB);
    tab.meta = KeyEvent.META_SHIFT_ON;
    boolean handledDown = slider.dispatchKeyEvent(tab.buildDown());
    boolean handledUp = slider.dispatchKeyEvent(tab.buildUp());

    assertThat(handledDown).isFalse();
    assertThat(handledUp).isFalse();
  }

  @Test
  public void testFocusLastThumb_tab_unhandled() {
    slider.requestFocus();

    slider.setFocusedThumbIndex(values.length - 1);

    KeyEventBuilder tab = new KeyEventBuilder(KeyEvent.KEYCODE_TAB);
    boolean handledDown = slider.dispatchKeyEvent(tab.buildDown());
    boolean handledUp = slider.dispatchKeyEvent(tab.buildUp());

    assertThat(handledDown).isFalse();
    assertThat(handledUp).isFalse();
  }

  @Test
  public void testActivateLastThumb_tab_unhandled() {
    slider.requestFocus();

    slider.setFocusedThumbIndex(values.length - 1);

    activateFocusedThumb(slider);

    KeyEventBuilder tab = new KeyEventBuilder(KeyEvent.KEYCODE_TAB);
    boolean handledDown = slider.dispatchKeyEvent(tab.buildDown());
    boolean handledUp = slider.dispatchKeyEvent(tab.buildUp());

    assertThat(handledDown).isFalse();
    assertThat(handledUp).isFalse();
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
