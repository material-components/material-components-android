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

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.app.ActionBar.LayoutParams;
import android.os.SystemClock;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import androidx.test.core.app.ApplicationProvider;
import com.google.android.material.slider.Slider.OnChangeListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/** Tests for events of {@link com.google.android.material.slider.Slider} */
@RunWith(RobolectricTestRunner.class)
public class SliderEventTest {

  private static final float FLOAT_ERROR = 1e-4f;

  private static final float SLIDER_VALUE_FROM = 0f;
  private static final float SLIDER_VALUE_TO = 100f;
  private static final float SLIDER_VALUE_RANGE = SLIDER_VALUE_TO - SLIDER_VALUE_FROM;
  private static final float SLIDER_STEP_VALUE = 1f;

  private Slider slider;
  private OnChangeListener mockOnChangeListener;

  @Before
  public void createSlider() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Bridge);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();

    // Creates slider and adds the listener.
    slider = new Slider(activity);
    slider.setValueFrom(SLIDER_VALUE_FROM);
    slider.setValueTo(SLIDER_VALUE_TO);

    mockOnChangeListener = mock(OnChangeListener.class);
    slider.addOnChangeListener(mockOnChangeListener);

    // Makes sure getParent() won't return null.
    LinearLayout container = new LinearLayout(activity);
    container.setPadding(50, 50, 50, 50);
    container.setOrientation(LinearLayout.VERTICAL);
    // Prevents getContentView() dead loop.
    container.setId(android.R.id.content);
    // Adds slider to layout, and adds layout to activity.
    container.addView(slider, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    activity.addContentView(container, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));

    // Changes the step size.
    slider.setStepSize(SLIDER_STEP_VALUE);
  }

  @Test
  public void testSliderSingleClickCurrentPosition_ListenerShouldNotBeCalled() {
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
  public void testSliderDrag_ListenerShouldBeCalled() {
    // Drag starts from one quarter to the left end.
    touchSliderAtValue(slider, SLIDER_VALUE_FROM + SLIDER_VALUE_RANGE / 4, MotionEvent.ACTION_DOWN);
    // Drags to the center.
    for (int incremental = 1; incremental <= 100; incremental++) {
      touchSliderAtValue(
          slider,
          SLIDER_VALUE_FROM + SLIDER_VALUE_RANGE * (25 + incremental / 4) / 100,
          MotionEvent.ACTION_MOVE);
    }
    // Drag released.
    touchSliderAtValue(slider, SLIDER_VALUE_TO / 2, MotionEvent.ACTION_UP);
    // Verifies listener calls.
    for (int value = 25; value <= 50; value++) {
      verify(mockOnChangeListener, times(1))
          .onValueChange(eq(slider), AdditionalMatchers.eq((float) value, FLOAT_ERROR), eq(true));
    }
  }

  @Test
  public void testSliderSetValue_OnlySetOnce() {
    // Sets value twice.
    slider.setValue(SLIDER_VALUE_FROM + SLIDER_VALUE_RANGE / 2);
    slider.setValue(SLIDER_VALUE_FROM + SLIDER_VALUE_RANGE / 2);

    // Verifies listener calls.
    verify(mockOnChangeListener, times(1))
        .onValueChange(eq(slider), eq(SLIDER_VALUE_FROM + SLIDER_VALUE_RANGE / 2), eq(false));
  }

  private static void touchSliderAtValue(Slider s, float value, int motionEventType) {
    float x =
        s.getTrackSidePadding()
            + (value - s.getValueFrom()) / (s.getValueTo() - s.getValueFrom()) * s.getTrackWidth();
    float y = s.getY() + s.getHeight() / 2;

    s.dispatchTouchEvent(
        MotionEvent.obtain(
            SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), motionEventType, x, y, 0));
  }
}
