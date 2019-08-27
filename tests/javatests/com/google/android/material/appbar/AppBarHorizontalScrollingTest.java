/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.material.appbar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.AppBarHorizontalScrollingActivity;
import com.google.android.material.testapp.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testing that if we have a {@link AppBarLayout} child that intercepts touch events (such as {@link
 * HorizontalScrollView} that handles horizontal swipes), that does not interfere with event
 * handling after the event sequence is no longer being intercepted by that child.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class AppBarHorizontalScrollingTest {
  @Rule
  public final ActivityTestRule<AppBarHorizontalScrollingActivity> activityTestRule =
      new ActivityTestRule<>(AppBarHorizontalScrollingActivity.class);

  @Test
  public void testScrollAndClick() throws Throwable {
    final Activity activity = activityTestRule.getActivity();
    final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

    final Button button = activity.findViewById(R.id.button);
    final View.OnClickListener mockClickListener = mock(View.OnClickListener.class);
    button.setOnClickListener(mockClickListener);

    // Emulate a click on the button to verify that the registered listener is invoked
    // prior to performing a horizontal swipe across the app ba

    final int[] buttonXY = new int[2];
    button.getLocationOnScreen(buttonXY);
    final int buttonWidth = button.getWidth();
    final int buttonHeight = button.getHeight();
    final float emulatedTapX = buttonXY[0] + buttonWidth / 2.0f;
    final float emulatedTapY = buttonXY[1] + buttonHeight / 2.0f;

    emulateButtonClick(instrumentation, emulatedTapX, emulatedTapY);
    verify(mockClickListener).onClick(button);
    reset(mockClickListener);

    final HorizontalScrollView hsv = activity.findViewById(R.id.hsv);
    final int scrollXBefore = hsv.getScrollX();
    // Now scroll / swipe horizontally across our scrollable content in the app bar
    onView(withId(R.id.app_bar)).perform(swipeLeft());
    assertTrue("Horizontal scroll performed", hsv.getScrollX() > scrollXBefore);

    // And emulate another click on the button to verify that the registered listener is still
    // invoked immediately after performing the horizontal swipe across the app bar
    emulateButtonClick(instrumentation, emulatedTapX, emulatedTapY);
    verify(mockClickListener).onClick(button);
  }

  private void emulateButtonClick(
      Instrumentation instrumentation, float emulatedTapX, float emulatedTapY) {
    // Note that the reason to not use Espresso's click() view action is so that we can
    // faithfully emulate what was happening in the reported bug. We don't want the events
    // to be sent directly to the button, but rather be processed by the parent coordinator
    // layout, so that we reproduce what is happening as the events are processed at the level
    // of that parent.

    // Inject DOWN event
    long downTime = SystemClock.uptimeMillis();
    MotionEvent eventDown =
        MotionEvent.obtain(
            downTime, downTime, MotionEvent.ACTION_DOWN, emulatedTapX, emulatedTapY, 1);
    instrumentation.sendPointerSync(eventDown);

    // Inject MOVE event
    long moveTime = SystemClock.uptimeMillis();
    MotionEvent eventMove =
        MotionEvent.obtain(
            moveTime, moveTime, MotionEvent.ACTION_MOVE, emulatedTapX, emulatedTapY, 1);
    instrumentation.sendPointerSync(eventMove);

    // Inject UP event
    long upTime = SystemClock.uptimeMillis();
    MotionEvent eventUp =
        MotionEvent.obtain(upTime, upTime, MotionEvent.ACTION_UP, emulatedTapX, emulatedTapY, 1);
    instrumentation.sendPointerSync(eventUp);

    // Wait for the system to process all events in the queue
    instrumentation.waitForIdleSync();
  }
}
