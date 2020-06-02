package com.google.android.material.slider;

import static com.google.android.material.slider.RtlTestUtils.checkAppSupportsRtl;
import static com.google.android.material.slider.RtlTestUtils.checkPlatformSupportsRtl;
import static com.google.android.material.slider.RtlTestUtils.applyRtlPseudoLocale;
import static com.google.common.truth.Truth.assertThat;

import android.view.KeyEvent;
import android.view.View;
import com.google.android.material.slider.KeyUtils.KeyEventBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Tests for key handling of {@link Slider} in right-to-left layout.
 */
@RunWith(RobolectricTestRunner.class)
public final class SliderKeyTestRtl extends SliderKeyTestCommon {

  @Before
  public void checkRtl() {
    checkPlatformSupportsRtl();
    checkAppSupportsRtl();
  }

  @Override
  public void createSlider() {
    applyRtlPseudoLocale();
    super.createSlider();
  }

  @Ignore("Fix RTL support for Robolectric tests.")
  @Test
  public void testThumbFocus_focusLeft_focusesFirstThumb() {
    slider.requestFocus(View.FOCUS_LEFT);

    assertThat(slider.getFocusedThumbIndex()).isEqualTo(0);
  }

  @Ignore("Fix RTL support for Robolectric tests.")
  @Test
  public void testThumbFocus_focusRight_focusesLastThumb() {
    slider.requestFocus(View.FOCUS_RIGHT);

    assertThat(slider.getFocusedThumbIndex()).isEqualTo(countTestValues() - 1);
  }

  @Ignore("Fix RTL support for Robolectric tests.")
  @Test
  public void testMoveThumbFocus_dPad_correctThumbHasFocus() {
    slider.requestFocus();

    KeyEventBuilder left = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_LEFT);
    KeyEventBuilder right = new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_RIGHT);

    // We start at far right in RTL so go left first, then go back right.
    sendKeyEventThereAndBack(left, right);
  }

  @Ignore("Fix RTL support for Robolectric tests.")
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
    assertThat(slider.getValues()).contains(0.0f);
    assertThat(slider.getValues()).doesNotContain(3.0f);
  }

  @Ignore("Fix RTL support for Robolectric tests.")
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
    assertThat(slider.getValues()).contains(23.0f);
    assertThat(slider.getValues()).doesNotContain(3.0f);
  }
}
