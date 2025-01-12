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

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.FloatRange;
import com.google.android.material.slider.KeyUtils.KeyEventBuilder;

public class SliderHelper {
  private final LinearLayout container;
  private final Slider slider;
  private final RangeSlider rangeSlider;
  private boolean simulateScrollableContainer;

  public SliderHelper(Activity activity) {
    slider = new Slider(activity);
    rangeSlider = new RangeSlider(activity);

    // Makes sure getParent() won't return null.
    container =
        new LinearLayout(activity) {
          @Override
          public boolean shouldDelayChildPressedState() {
            return simulateScrollableContainer;
          }

          @Override
          public boolean canScrollVertically(int direction) {
            return simulateScrollableContainer;
          }
        };
    container.setPadding(50, 50, 50, 50);
    container.setOrientation(LinearLayout.VERTICAL);
    // Prevents getContentView() dead loop.
    container.setId(android.R.id.content);
    // Adds slider to layout, and adds layout to activity.
    container.addView(slider, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));

    container.addView(rangeSlider, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
  }

  Slider getSlider() {
    return slider;
  }

  RangeSlider getRangeSlider() {
    return rangeSlider;
  }

  void addContentView(Activity activity) {
    activity.addContentView(container, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
  }

  static void touchSliderAtValue(BaseSlider<?, ?, ?> s, float value, int motionEventType) {
    touchSliderAtX(s, calculateXPositionFromValue(s, value), motionEventType);
  }

  /**
   * Performs a touch on the Slider at the specific {@code fraction} along the track with 0 being
   * the far left and 1 being the
   */
  static void touchSliderAtFraction(
      Slider s, @FloatRange(from = 0, to = 1) float fraction, int motionEventType) {
    float x = s.getTrackSidePadding() + fraction * s.getTrackWidth();
    touchSliderAtX(s, x, motionEventType);
  }

  static void touchSliderAtX(BaseSlider<?, ?, ?> s, float x, int motionEventType) {
    float y = s.getHeight() / 2f;

    s.dispatchTouchEvent(
        MotionEvent.obtain(
            SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), motionEventType, x, y, 0));
  }

  static void dragSliderBetweenValues(
      BaseSlider<?, ?, ?> s, float start, float end, int eventCount) {
    touchSliderAtValue(s, start, MotionEvent.ACTION_DOWN);
    touchSliderBetweenValues(s, start, end, eventCount);
    touchSliderAtValue(s, end, MotionEvent.ACTION_UP);
  }

  static void startSliderDragBetweenValues(RangeSlider s, float start, float end, int eventCount) {
    touchSliderAtValue(s, start, MotionEvent.ACTION_DOWN);
    touchSliderBetweenValues(s, start, end, eventCount);
  }

  static void endSliderDragBetweenValues(
      BaseSlider<?, ?, ?> s, float start, float end, int eventCount) {
    touchSliderBetweenValues(s, start, end, eventCount);
    touchSliderAtValue(s, end, MotionEvent.ACTION_UP);
  }

  static void touchSliderBetweenValues(
      BaseSlider<?, ?, ?> s, float start, float end, int eventCount) {
    float increment = (end - start) / eventCount;
    float currentIncrement = 0;
    for (int incremental = 0; incremental < eventCount; incremental++) {
      currentIncrement += increment;
      // make sure we reach end, without precision loss
      boolean lastIncrement = incremental == (eventCount - 1);
      touchSliderAtValue(
          s, lastIncrement ? end : start + currentIncrement, MotionEvent.ACTION_MOVE);
    }
  }

  static float calculateXPositionFromValue(BaseSlider<?, ?, ?> s, float value) {
    float x = (value - s.getValueFrom()) * s.getTrackWidth() / (s.getValueTo() - s.getValueFrom());
    if (s.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
      x = s.getTrackWidth() - x;
    }
    return s.getTrackSidePadding() + x;
  }

  static void clickDpadCenter(BaseSlider<?, ?, ?> s) {
    new KeyEventBuilder(KeyEvent.KEYCODE_DPAD_CENTER).dispatchEvent(s);
  }

  public void simulateScrollableContainer(boolean isScrollable) {
    simulateScrollableContainer = isScrollable;
  }
}
