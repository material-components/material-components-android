/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.google.android.material.snackbar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.R;
import com.google.android.material.testapp.SnackbarActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class SnackbarTouchTest {

  @Rule
  public final ActivityTestRule<SnackbarActivity> activityTestRule =
      new ActivityTestRule<>(SnackbarActivity.class);

  private Snackbar snackbar;

  @Before
  public void setupAndShowSnackbar() {
    SnackbarActivity activity = activityTestRule.getActivity();
    ViewGroup container = activity.findViewById(R.id.col);
    snackbar =
        Snackbar.make(container, "Test Snackbar", Snackbar.LENGTH_INDEFINITE)
            .setAction("Do Action", mock(View.OnClickListener.class));

    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        CheckBox checkBox = new CheckBox(activity);
        checkBox.setText("test checkbox");
        container.addView(checkBox);
        checkBox.setLayoutParams(
            new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
      }
    });

    snackbar.show();
  }

  @Test
  public void testSnackbar_consumesClicks() {
    onView(is(snackbar.getView())).perform(click());

    onView(withClassName(endsWith("CheckBox"))).check(matches(isNotChecked()));
  }
}
