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

import static android.os.Looper.getMainLooper;
import static com.google.android.material.slider.RtlTestUtils.applyRtlPseudoLocale;
import static com.google.android.material.slider.RtlTestUtils.checkAppSupportsRtl;
import static com.google.android.material.slider.RtlTestUtils.checkPlatformSupportsRtl;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.os.Build.VERSION_CODES;
import android.view.KeyEvent;
import android.view.View;
import androidx.annotation.RequiresApi;
import com.google.android.material.slider.KeyUtils.KeyEventBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import org.junit.Ignore;

/**
 * Tests for key handling of {@link Slider} in right-to-left layout.
 */
@RunWith(RobolectricTestRunner.class)
@Ignore("Fix RTL support for Robolectric tests.")
public final class SliderKeyTestRtl extends SliderKeyTestCommon {

  @Before
  public void checkRtl() {
    checkPlatformSupportsRtl();
    checkAppSupportsRtl();
  }

  @RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR1)
  @Override
  public void createSlider() {
    applyRtlPseudoLocale();
    super.createSlider();
    shadowOf(getMainLooper()).idle();
    assertThat(slider.isRtl()).isTrue();
  }

  @Test
  public void testThumbFocus_focusLeft_focusesFirstThumb() {
    slider.requestFocus(View.FOCUS_LEFT);

    assertThat(slider.getFocusedThumbIndex()).isEqualTo(0);
  }

  @Test
  public void testThumbFocus_focusRight_focusesLastThumb() {
    slider.requestFocus(View.FOCUS_RIGHT);
    shadowOf(getMainLooper()).idle();

    assertThat(slider.getFocusedThumbIndex()).isEqualTo(countTestValues() - 1);
  }

  @Test
  public void testMoveThumbFocus_dPad_correctThumbHasFocus() {
    slider.requestFocus();

    KeyEventBuilder left = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_LEFT);
    KeyEventBuilder right = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_RIGHT);

    // We start at far right in RTL so go left first, then go back right.
    sendKeyEventThereAndBack(left, right);
  }

  @Test
  public void testActivateThirdThumb_moveRight_movesThirdThumbRight() {
    slider.requestFocus();

    slider.setFocusedThumbIndex(2);

    activateFocusedThumb();

    KeyEventBuilder right = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_RIGHT);
    for (int i = 0; i < 3; i++) {
      right.dispatchEvent(slider);
    }

    // Moving right decrements in RTL.
    assertThat(slider.getValues()).doesNotContain(3.0f);
  }

  @Test
  public void testActivateThirdThumb_moveLeft_movesThirdThumbLeft() {
    slider.requestFocus();
    slider.setFocusedThumbIndex(2);

    activateFocusedThumb();

    KeyEventBuilder left = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_LEFT);
    for (int i = 0; i < 20; i++) {
      left.dispatchEvent(slider);
    }

    // Moving left increments in RTL.
    assertThat(slider.getValues()).doesNotContain(3.0f);
  }
}
