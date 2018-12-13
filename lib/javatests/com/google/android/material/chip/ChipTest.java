/*
 * Copyright 2018 The Android Open Source Project
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
package com.google.android.material.chip;

import com.google.android.material.R;


import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link com.google.android.material.chip.Chip}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class ChipTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final int CHIP_LINES = 2;
  private Chip chip;

  @Before
  public void themeApplicationContext() {
    RuntimeEnvironment.application.setTheme(
        R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    View inflated = activity.getLayoutInflater().inflate(R.layout.test_action_chip, null);
    chip = inflated.findViewById(R.id.chip);
  }

  @Test
  public void testSetCompoundDrawablesLeftRight_throwsException() {
    thrown.expect(UnsupportedOperationException.class);
    chip.setCompoundDrawables(chip.getCloseIcon(), null, chip.getCloseIcon(), null);
  }

  @Test
  public void testSetCompoundDrawablesWithIntrinsicBoundsResIdLeftRight_throwsException() {
    thrown.expect(UnsupportedOperationException.class);
    chip.setCompoundDrawablesWithIntrinsicBounds(
        R.drawable.ic_mtrl_chip_close_circle, 0, R.drawable.ic_mtrl_chip_close_circle, 0);
  }

  @Test
  public void testSetCompoundDrawablesWithIntrinsicBoundsLeftRight_throwsException() {
    thrown.expect(UnsupportedOperationException.class);
    chip.setCompoundDrawablesWithIntrinsicBounds(
        chip.getCloseIcon(), null, chip.getCloseIcon(), null);
  }

  @Test
  public void testSetCompoundDrawablesRelativeLeftRight_throwsException() {
    thrown.expect(UnsupportedOperationException.class);
    chip.setCompoundDrawablesRelative(chip.getCloseIcon(), null, chip.getCloseIcon(), null);
  }

  @Test
  public void testSetCompoundDrawablesRelativeWithIntrinsicBoundsResIdLeftRight_throwsException() {
    thrown.expect(UnsupportedOperationException.class);

    chip.setCompoundDrawablesRelativeWithIntrinsicBounds(
        R.drawable.ic_mtrl_chip_close_circle, 0, R.drawable.ic_mtrl_chip_close_circle, 0);
  }

  @Test
  public void testSetCompoundDrawablesRelativeWithIntrinsicBoundsLeftRight_throwsException() {
    thrown.expect(UnsupportedOperationException.class);

    chip.setCompoundDrawablesRelativeWithIntrinsicBounds(
        chip.getCloseIcon(), null, chip.getCloseIcon(), null);
  }

  @Test
  public void testSetEllipsizeMarquee_throwsException() {
    thrown.expect(UnsupportedOperationException.class);
    chip.setEllipsize(TruncateAt.MARQUEE);
  }

  @Test
  public void testSetSingleLineFalse_throwsException() {
    thrown.expect(UnsupportedOperationException.class);
    chip.setSingleLine(false);
  }

  @Test
  public void testSetLinesMultiple_throwsException() {
    thrown.expect(UnsupportedOperationException.class);
    chip.setLines(CHIP_LINES);
  }

  @Test
  public void testSetMinLinesMultiple_throwsException() {
    thrown.expect(UnsupportedOperationException.class);
    chip.setMinLines(CHIP_LINES);
  }

  @Test
  public void testSetMaxLinesMultiple_throwsException() {
    thrown.expect(UnsupportedOperationException.class);
    chip.setMaxLines(CHIP_LINES);
  }
}
