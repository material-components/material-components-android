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


import static com.google.android.material.internal.ViewUtils.dpToPx;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link com.google.android.material.chip.Chip}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class ChipTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final int CHIP_LINES = 2;
  private static final double DELTA = 0.01;
  private static final int MIN_SIZE_FOR_ALLY_DP = 48;

  private Chip chip;

  @Before
  public void themeApplicationContext() {
    ApplicationProvider.getApplicationContext().setTheme(
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

  @Test
  public void ensureMinTouchTarget_is48dp() {
    setupAndMeasureChip(true);

    assertEquals(
        "Chip width: " + chip.getMeasuredWidth(),
        getMinTouchTargetSize(),
        chip.getMeasuredWidth(),
        DELTA);

    assertEquals(
        "Chip height: " + chip.getMeasuredHeight(),
        getMinTouchTargetSize(),
        chip.getMeasuredHeight(),
        DELTA);
  }

  @Test
  public void ensureMinTouchTargetFalse_isLessThan48dp() {

    setupAndMeasureChip(false);

    assertNotEquals(chip.getMeasuredWidth(), getMinTouchTargetSize(), DELTA);

    assertTrue(
        "Chip width: " + chip.getMeasuredWidth(),
        chip.getMeasuredWidth() < getMinTouchTargetSize());

    assertTrue(
        "Chip height: " + chip.getMeasuredHeight(),
        chip.getMeasuredHeight() < getMinTouchTargetSize());
  }

  private static float getMinTouchTargetSize() {
    return dpToPx(ApplicationProvider.getApplicationContext(), MIN_SIZE_FOR_ALLY_DP);
  }

  private void setupAndMeasureChip(boolean shouldEnsureMinTouchTargeSize) {
    chip.setEnsureMinTouchTargetSize(shouldEnsureMinTouchTargeSize);
    int measureSpec =
        MeasureSpec.makeMeasureSpec((int) (getMinTouchTargetSize() * 2), MeasureSpec.AT_MOST);
    chip.measure(measureSpec, measureSpec);
  }
}
