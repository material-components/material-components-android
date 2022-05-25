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
import org.robolectric.Robolectric;

/**
 * Tests for key handling of {@link Slider} both in left-to-right and right-to-left layouts.
 */
public abstract class SliderKeyTestCommon {

  private static final float SLIDER_VALUE_FROM = 0f;
  private static final float SLIDER_VALUE_TO = 100f;

  private static final Float[] values = new Float[]{1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f};

  protected Slider slider;

  protected int countTestValues() {
    return values.length;
  }

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

    assertThat(slider.getFocusedThumbIndex()).isEqualTo(countTestValues() - 1);
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

    sendKeyEventThereAndBack(tab, shiftTab);
  }

  @Test
  public void testMoveThumbFocus_plusMinus_correctThumbHasFocus() {
    slider.requestFocus();

    KeyEventBuilder plus = new KeyEventBuilder(KeyEvent.KEYCODE_PLUS);
    KeyEventBuilder minus = new KeyEventBuilder(KeyEvent.KEYCODE_MINUS);

    sendKeyEventThereAndBack(plus, minus);
  }

  @Test
  public void testMoveThumbFocus_equalsMinus_correctThumbHasFocus() {
    slider.requestFocus();

    // Numpad Plus == Shift + Equals, at least in AVD.
    KeyEventBuilder equals = new KeyEventBuilder(KeyEvent.KEYCODE_EQUALS);
    KeyEventBuilder minus = new KeyEventBuilder(KeyEvent.KEYCODE_MINUS);

    sendKeyEventThereAndBack(equals, minus);
  }

  @Test
  public void testFocusThirdThumb_clickCenterDPad_activatesThirdThumb() {
    slider.requestFocus();

    slider.setFocusedThumbIndex(2);

    clickDpadCenter(slider);

    assertThat(slider.getActiveThumbIndex()).isEqualTo(2);
  }

  protected void activateFocusedThumb() {
    int focusedThumbIndex = slider.getFocusedThumbIndex();
    if (focusedThumbIndex != -1) {
      // Clicking D-Pad in Slider isn't idempotent. Only do it here if we're changing focused thumb.
      if (focusedThumbIndex != slider.getActiveThumbIndex()) {
        clickDpadCenter(slider);
      }
    }
  }

  @Test
  public void testActivateThirdThumb_clickCenterDPad_deactivatesThirdThumb() {
    slider.requestFocus();

    slider.setFocusedThumbIndex(2);

    activateFocusedThumb();

    clickDpadCenter(slider);

    assertThat(slider.getActiveThumbIndex()).isEqualTo(-1);
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

    activateFocusedThumb();

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

    activateFocusedThumb();

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

    activateFocusedThumb();

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

    slider.setFocusedThumbIndex(countTestValues() - 1);

    KeyEventBuilder tab = new KeyEventBuilder(KeyEvent.KEYCODE_TAB);
    boolean handledDown = slider.dispatchKeyEvent(tab.buildDown());
    boolean handledUp = slider.dispatchKeyEvent(tab.buildUp());

    assertThat(handledDown).isFalse();
    assertThat(handledUp).isFalse();
  }

  @Test
  public void testActivateLastThumb_tab_unhandled() {
    slider.requestFocus();

    slider.setFocusedThumbIndex(countTestValues() - 1);

    activateFocusedThumb();

    KeyEventBuilder tab = new KeyEventBuilder(KeyEvent.KEYCODE_TAB);
    boolean handledDown = slider.dispatchKeyEvent(tab.buildDown());
    boolean handledUp = slider.dispatchKeyEvent(tab.buildUp());

    assertThat(handledDown).isFalse();
    assertThat(handledUp).isFalse();
  }

  protected void sendKeyEventThereAndBack(KeyEventBuilder there, KeyEventBuilder back) {
    for (int i = 1; i < countTestValues(); i++) {
      there.dispatchEvent(slider);
      assertWithMessage("moving right").that(slider.getFocusedThumbIndex()).isEqualTo(i);
    }

    for (int i = countTestValues() - 1; i > 0; i--) {
      back.dispatchEvent(slider);
      assertWithMessage("moving left").that(slider.getFocusedThumbIndex()).isEqualTo(i - 1);
    }
  }
}
