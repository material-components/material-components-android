/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.google.android.material.bottomsheet;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.CoordinatorLayoutActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class BottomSheetBehaviorTouchTest {

  private static final int PEEK_HEIGHT = 100;

  @Rule
  public final ActivityTestRule<CoordinatorLayoutActivity> activityTestRule =
      new ActivityTestRule<>(CoordinatorLayoutActivity.class);

  private FrameLayout bottomSheet;

  private BottomSheetBehavior<FrameLayout> behavior;

  private boolean down;

  private View.OnTouchListener onTouchListener =
      new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
          switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
              down = true;
              break;
          }
          return true;
        }
      };

  @Before
  public void setUpBottomSheet() {
    getInstrumentation()
        .runOnMainSync(
            () -> {
              CoordinatorLayoutActivity activity = activityTestRule.getActivity();
              activity.mContainer.setOnTouchListener(onTouchListener);
              bottomSheet = new FrameLayout(activity);
              CoordinatorLayout.LayoutParams params =
                  new CoordinatorLayout.LayoutParams(
                      CoordinatorLayout.LayoutParams.MATCH_PARENT,
                      CoordinatorLayout.LayoutParams.MATCH_PARENT);
              behavior = new BottomSheetBehavior<>();
              behavior.setPeekHeight(PEEK_HEIGHT);
              params.setBehavior(behavior);
              activity.mCoordinatorLayout.addView(bottomSheet, params);
            });
  }

  @Test
  public void testSetUp() {
    assertThat(bottomSheet, is(notNullValue()));
    assertThat(behavior, is(sameInstance(BottomSheetBehavior.from(bottomSheet))));
  }

  @Test
  public void testTouchCoordinatorLayout() {
    final CoordinatorLayoutActivity activity = activityTestRule.getActivity();
    down = false;
    Espresso.onView(sameInstance((View) activity.mCoordinatorLayout))
        .perform(ViewActions.click()) // Click outside the bottom sheet
        .check(
            (view, e) -> {
              assertThat(e, is(nullValue()));
              assertThat(view, is(notNullValue()));
              // Check that the touch event fell through to the container
              assertThat(down, is(true));
            });
  }
}
