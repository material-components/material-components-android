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

import com.google.android.material.test.R;

import static com.google.android.material.internal.ViewUtils.dpToPx;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.graphics.RectF;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.TextLayoutMode;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link com.google.android.material.chip.Chip}. */
@TextLayoutMode(value = TextLayoutMode.Mode.LEGACY, issueId = "130377392")
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class ChipTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final int CHIP_LINES = 2;
  private static final float DELTA = 0.01f;
  private static final int MIN_SIZE_FOR_ALLY_DP = 48;

  private Chip chip;
  private AppCompatActivity activity;

  @Before
  public void themeApplicationContext() {
    ApplicationProvider.getApplicationContext()
        .setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar_Bridge);
    activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
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

    assertThat(getMinTouchTargetSize()).isWithin(DELTA).of(chip.getMeasuredWidth());
    assertThat(getMinTouchTargetSize()).isWithin(DELTA).of(chip.getMeasuredHeight());
  }

  @Test
  public void ensureMinTouchTargetFalse_isLessThan48dp() {

    setupAndMeasureChip(false);

    assertThat(getMinTouchTargetSize()).isNotWithin(DELTA).of(chip.getMeasuredWidth());

    assertThat(chip.getMeasuredWidth()).isLessThan((int) getMinTouchTargetSize());

    assertThat(chip.getMeasuredHeight()).isLessThan((int) getMinTouchTargetSize());
  }

  @Test
  public void testSetChipDrawableGetText() {
    Context context = ApplicationProvider.getApplicationContext();
    Chip resultChip = new Chip(context);
    ChipDrawable chipDrawable =
        ChipDrawable.createFromAttributes(
            context, null, 0, R.style.Widget_MaterialComponents_Chip_Choice);
    resultChip.setChipDrawable(chipDrawable);
    resultChip.setText("foo");
    assertThat(TextUtils.equals(resultChip.getText(), "foo")).isTrue();
  }

  @Test
  public void testHasCustomAccessibilityDelegate() {
    chip.setCloseIconResource(R.drawable.ic_mtrl_chip_close_circle);
    chip.setCloseIconVisible(true);
    chip.setOnCloseIconClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            /* Do something */
          }
        });
    AccessibilityDelegateCompat accessibilityDelegate = ViewCompat.getAccessibilityDelegate(chip);
    assertThat(accessibilityDelegate).isNotNull();
  }

  @Test
  public void testNoCustomAccessibilityDelegate() {
    chip.setCloseIconResource(R.drawable.ic_mtrl_chip_close_circle);
    AccessibilityDelegateCompat accessibilityDelegate = ViewCompat.getAccessibilityDelegate(chip);
    assertThat(accessibilityDelegate).isNull();
  }

  private static float getMinTouchTargetSize() {
    return dpToPx(ApplicationProvider.getApplicationContext(), MIN_SIZE_FOR_ALLY_DP);
  }

  private void setupAndMeasureChip(boolean shouldEnsureMinTouchTargetSize) {
    chip.setEnsureMinTouchTargetSize(shouldEnsureMinTouchTargetSize);
    int measureSpec =
        MeasureSpec.makeMeasureSpec((int) (getMinTouchTargetSize() * 2), MeasureSpec.AT_MOST);
    chip.measure(measureSpec, measureSpec);
  }

  @Test
  public void testZeroChipCornerRadius() {
    View inflated =
        activity.getLayoutInflater().inflate(R.layout.test_chip_zero_corner_radius, null);
    chip = inflated.findViewById(R.id.zero_corner_chip);
    RectF bounds = new RectF();
    bounds.bottom = 0;
    bounds.top = 100;
    bounds.left = 0;
    bounds.right = 100;
    assertThat(chip.getShapeAppearanceModel().getTopLeftCornerSize().getCornerSize(bounds))
        .isWithin(DELTA)
        .of(0);
    assertThat(chip.getShapeAppearanceModel().getTopRightCornerSize().getCornerSize(bounds))
        .isWithin(DELTA)
        .of(0);
    assertThat(chip.getShapeAppearanceModel().getBottomLeftCornerSize().getCornerSize(bounds))
        .isWithin(DELTA)
        .of(0);
    assertThat(chip.getShapeAppearanceModel().getBottomRightCornerSize().getCornerSize(bounds))
        .isWithin(DELTA)
        .of(0);
  }

  @Test
  public void getChipAccessibilityClassName_clickable_buttonName() {
    assertEquals("android.widget.Button", chip.getAccessibilityClassName().toString());
  }

  @Test
  public void getChipAccessibilityClassName_nonClickable_viewName() {
    chip.setClickable(false);
    assertEquals("android.view.View", chip.getAccessibilityClassName().toString());
  }
}
