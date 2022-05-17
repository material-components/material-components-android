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

package com.google.android.material.timepicker;

import com.google.android.material.R;

import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/** Tests for touch handling of {@link ClockHandView} */
@RunWith(RobolectricTestRunner.class)
public class ClockHandViewTouchTest {

  private ClockHandView clockHandView;

  @Before
  public void createClockFace() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Light);
    Activity activity = Robolectric.buildActivity(Activity.class).setup().start().get();
    clockHandView = new ClockHandView(activity);
    clockHandView.setCircleRadius(100);
    LinearLayout container = new LinearLayout(activity);
    container.setPadding(50, 50, 50, 50);
    container.setOrientation(LinearLayout.VERTICAL);
    container.setId(android.R.id.content);
    container.addView(clockHandView, new LayoutParams(300, 300));

    activity.setContentView(container);
  }

  @Test
  public void clockFace_noTouch_noHandRotation() {
    shadowOf(getMainLooper()).idle();
    assertThat(clockHandView.getHandRotation()).isEqualTo(0f);
  }

  @Test
  public void clockFace_touch_hasCorrectRotation() {
    shadowOf(getMainLooper()).idle();
    // touch slider in the middle right (3pm) should be 90 degrees
    clockHandView.dispatchTouchEvent(
        MotionEvent.obtain(
            SystemClock.uptimeMillis(),
            SystemClock.uptimeMillis(),
            MotionEvent.ACTION_DOWN,
            /* x= */ 290,
            /* y= */ 150,
            /* metaState= */ 0));

    clockHandView.dispatchTouchEvent(
        MotionEvent.obtain(
            SystemClock.uptimeMillis(),
            SystemClock.uptimeMillis(),
            MotionEvent.ACTION_MOVE,
            /* x= */ 290,
            /* y= */ 150,
            /* metaState= */ 0));

    shadowOf(getMainLooper()).idle();

    assertThat(clockHandView.getHandRotation()).isEqualTo(90f);
  }
}
