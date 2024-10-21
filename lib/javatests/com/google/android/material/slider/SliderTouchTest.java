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

import com.google.android.material.test.R;

import static com.google.android.material.slider.SliderHelper.calculateXPositionFromValue;
import static com.google.android.material.slider.SliderHelper.dragSliderBetweenValues;
import static com.google.android.material.slider.SliderHelper.endSliderDragBetweenValues;
import static com.google.android.material.slider.SliderHelper.startSliderDragBetweenValues;
import static com.google.android.material.slider.SliderHelper.touchSliderAtValue;
import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertTrue;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;

import androidx.appcompat.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.LooperMode;

/** Tests for touch handling of {@link Slider} */
@LooperMode(LEGACY)
@RunWith(RobolectricTestRunner.class)
public class SliderTouchTest {
  private static final float SLIDER_VALUE_FROM = 0f;
  private static final float SLIDER_VALUE_TO = 100f;
  private static final float VALUE_SMALL_DIFF = 0.1f;

  private RangeSlider slider;
  private int scaledTouchSlop;

  @Before
  public void createSlider() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Bridge);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    scaledTouchSlop = ViewConfiguration.get(activity).getScaledTouchSlop();

    // Creates slider and adds the listener.
    SliderHelper helper = new SliderHelper(activity);
    slider = helper.getRangeSlider();
    slider.setValueFrom(SLIDER_VALUE_FROM);
    slider.setValueTo(SLIDER_VALUE_TO);
    slider.setStepSize(VALUE_SMALL_DIFF);

    helper.addContentView(activity);
  }

  @Before
  public void assertPreconditions() {
    assertTrue(
        "The small amount moved must be less than the scaled touch slop",
        calculateXPositionFromValue(slider, 0.0f)
                - calculateXPositionFromValue(slider, VALUE_SMALL_DIFF)
            < scaledTouchSlop);
  }

  @Test
  public void testTwoCloseValues_touchInCenter_correctThumbMoves() {
    slider.setValues(20.0f, 20.0f + VALUE_SMALL_DIFF);

    // Touch in center of both thumbs and drag slightly towards one thumb.
    startSliderDragBetweenValues(
        slider, 20.0f + VALUE_SMALL_DIFF / 4, 20.0f + 3 * VALUE_SMALL_DIFF / 4, 10);

    // Move far in the other direction.
    endSliderDragBetweenValues(slider, 20.0f + 3 * VALUE_SMALL_DIFF / 4, 0.0f, 30);

    // Verify only first thumb moved.
    assertThat(slider.getValues()).contains(20.0f + VALUE_SMALL_DIFF);
    assertThat(slider.getValues()).contains(0.0f);
  }

  @Test
  public void testThreeCloseValues_touchBetweenFirstTwo_MiddleThumbMoves() {
    // Set three thumbs within 2x touch slop.
    slider.setValues(20.0f, 20.0f + VALUE_SMALL_DIFF, 20.0f + VALUE_SMALL_DIFF * 2);

    // Touch down between the first 2 and move right past the 3rd.
    dragSliderBetweenValues(slider, 20.0f + VALUE_SMALL_DIFF / 2 + .0001f, 25f, 40);

    // Verify the middle thumb moved
    assertThat(slider.getValues()).contains(20.0f);
    assertThat(slider.getValues()).contains(20.0f + VALUE_SMALL_DIFF * 2);
    assertThat(slider.getValues()).contains(20.0f + VALUE_SMALL_DIFF * 2);
  }

  @Test
  public void testThreeCloseValues_touchOnRight_RightThumbMoves() {
    // Set three thumbs within 2x touch slop.
    slider.setValues(20.0f, 20.0f + VALUE_SMALL_DIFF, 20.0f + VALUE_SMALL_DIFF * 2);

    // Touch down on the far right
    touchSliderAtValue(slider, 41.0f, MotionEvent.ACTION_DOWN);

    // Verify the right thumb moved
    assertThat(slider.getValues()).contains(20.0f);
    assertThat(slider.getValues()).contains(20.1f);
    assertThat(slider.getValues()).contains(41.0f);
  }

  @Test
  public void test2thumbs_sameValue_rightThumbMoves() {
    // Move right to move the
    slider.setValues(20.0f, 20.0f);

    // Touch down on the far right
    dragSliderBetweenValues(slider, 19.99f, 50.0f, 5);

    // Verify the right thumb moved
    assertThat(slider.getValues()).contains(50.0f);
  }

  @Test
  public void test2thumbs_sameValue_leftThumbMoves() {
    slider.setValues(20.0f, 20.0f);

    // Touch down on the far left
    dragSliderBetweenValues(slider, 20.01f, 0f, 100);

    // Verify the right thumb moved
    assertThat(slider.getValues()).contains(0f);
  }

  @Test
  public void test2thumbs_minSeparation_leftThumbDoesNotMove() {
    slider.setValues(20.0f, 40.0f);
    slider.setMinSeparationValue(20.0f);

    // Slide left thumb towards right thumb
    dragSliderBetweenValues(slider, 20.01f, 40.0f, 100);

    // Verify the left thumb didn't move
    assertThat(slider.getValues()).contains(20.0f);
    assertThat(slider.getValues()).contains(40.0f);
  }

  @Test
  public void test2thumbs_minSeparation_rightThumbDoesNotMove() {
    slider.setValues(20.0f, 40.0f);
    slider.setMinSeparationValue(20.0f);

    // Slide right thumb towards left thumb
    dragSliderBetweenValues(slider, 39.99f, 20.0f, 100);

    // Verify the right thumb didn't move
    assertThat(slider.getValues()).contains(20.0f);
    assertThat(slider.getValues()).contains(40.0f);
  }

  @Test
  public void test2thumbs_minSeparation_stepSize_leftThumbMovesUntilSeparation() {
    slider.setValues(20.0f, 80.0f);
    slider.setMinSeparationValue(30.0f);
    slider.setStepSize(10.0f);

    // Slide left thumb towards right thumb
    dragSliderBetweenValues(slider, 20.01f, 80.0f, 3);

    // Verify the left thumb stopped at set separation
    assertThat(slider.getValues()).contains(50.0f);
    assertThat(slider.getValues()).contains(80.0f);
  }

  @Test
  public void test2thumbs_minSeparation_stepSize_rightThumbMovesUntilSeparation() {
    slider.setValues(20.0f, 80.0f);
    slider.setMinSeparationValue(30.0f);
    slider.setStepSize(10.0f);

    // Slide right thumb towards left thumb
    dragSliderBetweenValues(slider, 79.99f, 20.0f, 3);

    // Verify the right thumb stopped at set separation
    assertThat(slider.getValues()).contains(20.0f);
    assertThat(slider.getValues()).contains(50.0f);
  }

  @Test
  public void test3thumbs_minSeparation_middleThumbDoesNotMoveLeft() {
    slider.setValues(20.0f, 40.0f, 60.0f);
    slider.setMinSeparationValue(20.0f);

    // Slide middle thumb towards left thumb
    dragSliderBetweenValues(slider, 39.99f, 20.0f, 100);

    // Verify the middle thumb didn't move
    assertThat(slider.getValues()).contains(20.0f);
    assertThat(slider.getValues()).contains(40.0f);
    assertThat(slider.getValues()).contains(60.0f);
  }

  @Test
  public void test3thumbs_minSeparation_middleThumbDoesNotMoveRight() {
    slider.setValues(20.0f, 40.0f, 60.0f);
    slider.setMinSeparationValue(20.0f);

    // Slide middle thumb towards right thumb
    dragSliderBetweenValues(slider, 40.01f, 60.0f, 100);

    // Verify the middle thumb didn't move
    assertThat(slider.getValues()).contains(20.0f);
    assertThat(slider.getValues()).contains(40.0f);
    assertThat(slider.getValues()).contains(60.0f);
  }
}
