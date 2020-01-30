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
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class SliderHelper {
  private final LinearLayout container;
  private final Slider slider;

  public SliderHelper(Activity activity) {
    slider = new Slider(activity);

    // Makes sure getParent() won't return null.
    container = new LinearLayout(activity);
    container.setPadding(50, 50, 50, 50);
    container.setOrientation(LinearLayout.VERTICAL);
    // Prevents getContentView() dead loop.
    container.setId(android.R.id.content);
    // Adds slider to layout, and adds layout to activity.
    container.addView(slider, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
  }

  Slider getSlider() {
    return slider;
  }

  void addContentView(Activity activity) {
    activity.addContentView(container, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
  }

  static void touchSliderAtValue(Slider s, float value, int motionEventType) {
    float x = calculateXPositionFromValue(s, value);
    float y = s.getY() + s.getHeight() / 2;

    s.dispatchTouchEvent(
        MotionEvent.obtain(
            SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), motionEventType, x, y, 0));
  }

  static void dragSliderBetweenValues(Slider s, float start, float end, int eventCount) {
    touchSliderAtValue(s, start, MotionEvent.ACTION_DOWN);
    touchSliderBetweenValues(s, start, end, eventCount);
    touchSliderAtValue(s, end, MotionEvent.ACTION_UP);
  }

  static void startSliderDragBetweenValues(Slider s, float start, float end, int eventCount) {
    touchSliderAtValue(s, start, MotionEvent.ACTION_DOWN);
    touchSliderBetweenValues(s, start, end, eventCount);
  }

  static void endSliderDragBetweenValues(Slider s, float start, float end, int eventCount) {
    touchSliderBetweenValues(s, start, end, eventCount);
    touchSliderAtValue(s, end, MotionEvent.ACTION_UP);
  }

  static void touchSliderBetweenValues(Slider s, float start, float end, int eventCount) {
    for (int incremental = 0; incremental < eventCount; incremental++) {
      touchSliderAtValue(
          s,
          (start * (eventCount - incremental) + end * incremental) / eventCount,
          MotionEvent.ACTION_MOVE);
    }
  }

  static float calculateXPositionFromValue(Slider s, float value) {
    return s.getTrackSidePadding()
        + (value - s.getValueFrom()) * s.getTrackWidth() / (s.getValueTo() - s.getValueFrom());
  }
}
