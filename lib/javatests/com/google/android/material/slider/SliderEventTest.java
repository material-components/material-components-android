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

import static com.google.android.material.slider.SliderHelper.dragSliderBetweenValues;
import static com.google.android.material.slider.SliderHelper.touchSliderAtValue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;

import android.os.SystemClock;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MotionEvent;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.slider.Slider.OnChangeListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.LooperMode;

/** Tests for events of {@link com.google.android.material.slider.Slider} */
@LooperMode(LEGACY)
@RunWith(RobolectricTestRunner.class)
public class SliderEventTest {

  private static final float FLOAT_ERROR = 1e-4f;

  private static final float SLIDER_VALUE_FROM = 0f;
  private static final float SLIDER_VALUE_TO = 100f;
  private static final float SLIDER_VALUE_RANGE = SLIDER_VALUE_TO - SLIDER_VALUE_FROM;
  private static final float SLIDER_STEP_VALUE = 1f;

  private AppCompatActivity activity;
  private SliderHelper helper;
  private Slider slider;
  private OnChangeListener mockOnChangeListener;
  private RangeSlider rangeSlider;
  private RangeSlider.OnChangeListener mockRangeOnChangeListener;

  @Before
  public void createSlider() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Bridge);
    activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();

    // Creates slider and adds the listener.
    helper = new SliderHelper(activity);
    slider = helper.getSlider();
    slider.setValueFrom(SLIDER_VALUE_FROM);
    slider.setValueTo(SLIDER_VALUE_TO);
    slider.setStepSize(SLIDER_STEP_VALUE);

    rangeSlider = helper.getRangeSlider();
    rangeSlider.setValueFrom(SLIDER_VALUE_FROM);
    rangeSlider.setValueTo(SLIDER_VALUE_TO);
    rangeSlider.setStepSize(SLIDER_STEP_VALUE);

    Class<RangeSlider.OnChangeListener> onChangeListenerClass = RangeSlider.OnChangeListener.class;
    mockRangeOnChangeListener = mock(onChangeListenerClass);
    mockOnChangeListener = mock(OnChangeListener.class);
    slider.addOnChangeListener(mockOnChangeListener);
    rangeSlider.addOnChangeListener(mockRangeOnChangeListener);
  }

  @Test
  public void testSliderSingleClickCurrentPosition_ListenerShouldNotBeCalled() {
    // Lays out slider.
    helper.addContentView(activity);

    float currentValue = slider.getValue();
    // Click pressed.
    touchSliderAtValue(slider, currentValue, MotionEvent.ACTION_DOWN);
    // Click released.
    touchSliderAtValue(slider, currentValue, MotionEvent.ACTION_UP);
    // Listener should not be called since value is not changed.
    verify(mockOnChangeListener, never())
        .onValueChange(eq(slider), eq(SLIDER_VALUE_FROM), eq(true));
  }

  @Test
  public void testSliderSingleClickDifferentPosition_ListenerShouldBeCalled() {
    // Lays out slider.
    helper.addContentView(activity);

    // Click pressed.
    touchSliderAtValue(slider, SLIDER_VALUE_FROM + SLIDER_VALUE_RANGE / 2, MotionEvent.ACTION_DOWN);
    // Click released.
    touchSliderAtValue(slider, SLIDER_VALUE_FROM + SLIDER_VALUE_RANGE / 2, MotionEvent.ACTION_UP);
    // Listener should be called once.
    verify(mockOnChangeListener, times(1))
        .onValueChange(
            eq(slider),
            AdditionalMatchers.eq(SLIDER_VALUE_FROM + SLIDER_VALUE_RANGE / 2, FLOAT_ERROR),
            eq(true));
  }

  @Test
  public void testSliderDrag_multipleThumbs_ListenerShouldBeCalled() {
    rangeSlider.setValues(SLIDER_VALUE_FROM, SLIDER_VALUE_FROM + 10);
    testSliderDrag_ListenerShouldBeCalled();
  }

  @Test
  public void testSliderDrag_ListenerShouldBeCalled() {
    // Lays out slider.
    helper.addContentView(activity);

    // Drag starts from one quarter to the left end to the middle.
    dragSliderBetweenValues(
        slider, SLIDER_VALUE_FROM + SLIDER_VALUE_RANGE / 4, SLIDER_VALUE_TO / 2, 100);

    // Verifies listener calls.
    for (int value = 25; value <= 50; value++) {
      verify(mockOnChangeListener, times(1))
          .onValueChange(eq(slider), AdditionalMatchers.eq((float) value, FLOAT_ERROR), eq(true));
    }
  }

  @Test
  public void testSliderSetValue_ListenerShouldBeCalledOnce() {
    // Lays out slider.
    helper.addContentView(activity);

    // Sets value twice.
    slider.setValue(SLIDER_VALUE_FROM + SLIDER_VALUE_RANGE / 2);
    slider.setValue(SLIDER_VALUE_FROM + SLIDER_VALUE_RANGE / 2);

    // Verifies listener calls.
    verify(mockOnChangeListener, times(1))
        .onValueChange(eq(slider), eq(SLIDER_VALUE_FROM + SLIDER_VALUE_RANGE / 2), eq(false));
  }

  @Test
  public void testSliderSetValueBeforeLaidOut_ListenerShouldBeCalledOnce() {
    // Sets value before laid out.
    slider.setValue(SLIDER_VALUE_FROM + SLIDER_VALUE_RANGE / 2);
    // Verifies listener is called once.
    verify(mockOnChangeListener, times(1))
        .onValueChange(eq(slider), eq(SLIDER_VALUE_FROM + SLIDER_VALUE_RANGE / 2), eq(false));

    // Lays out slider.
    helper.addContentView(activity);

    // Verifies listener is not called again.
    verify(mockOnChangeListener, times(1))
        .onValueChange(eq(slider), eq(SLIDER_VALUE_FROM + SLIDER_VALUE_RANGE / 2), eq(false));
  }

  @Test
  public void testSliderSetMultipleValues_ListenerShouldBeCalledOncePerValue() {
    float[] values = new float[] {SLIDER_VALUE_FROM, SLIDER_VALUE_FROM + SLIDER_VALUE_RANGE / 2};
    rangeSlider.setValues(values[0], values[1]);
    rangeSlider.setValues(values[0], values[1]);
    // Verifies listener is called once for each value.
    verify(mockRangeOnChangeListener, times(1))
        .onValueChange(eq(rangeSlider), eq(values[0]), eq(false));
    verify(mockRangeOnChangeListener, times(1))
        .onValueChange(eq(rangeSlider), eq(values[1]), eq(false));
  }

  @Test
  public void testSliderInScrollingContainer_userPerformsScroll_sliderIsNotUpdated() {
    helper.addContentView(activity);
    helper.simulateScrollableContainer(true);

    slider.dispatchTouchEvent(
        MotionEvent.obtain(
            SystemClock.uptimeMillis(),
            SystemClock.uptimeMillis(),
            MotionEvent.ACTION_DOWN,
            slider.getWidth() / 2f,
            0,
            0));
    // Swipe halfway down the view.
    slider.dispatchTouchEvent(
        MotionEvent.obtain(
            SystemClock.uptimeMillis(),
            SystemClock.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            slider.getWidth() / 2f,
            slider.getHeight() / 4f,
            0));
    slider.dispatchTouchEvent(
        MotionEvent.obtain(
            SystemClock.uptimeMillis(),
            SystemClock.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            slider.getWidth() / 2f,
            slider.getHeight() / 2f,
            0));
    slider.dispatchTouchEvent(
        MotionEvent.obtain(
            SystemClock.uptimeMillis(),
            SystemClock.uptimeMillis(),
            MotionEvent.ACTION_UP,
            slider.getWidth() / 2f,
            slider.getHeight() / 2f,
            0));

    verify(mockOnChangeListener, never()).onValueChange(eq(slider), anyFloat(), anyBoolean());
  }

  @Test
  public void testSliderInScrollingContainer_userPerformsTap_sliderIsUpdated() {
    helper.addContentView(activity);
    helper.simulateScrollableContainer(true);
    float value = 40f;

    // Perform tap (UP and DOWN) at the same spot without MOVE.
    touchSliderAtValue(slider, value, MotionEvent.ACTION_DOWN);
    touchSliderAtValue(slider, value, MotionEvent.ACTION_UP);

    verify(mockOnChangeListener, times(1)).onValueChange(eq(slider), eq(value), eq(true));
  }
}
