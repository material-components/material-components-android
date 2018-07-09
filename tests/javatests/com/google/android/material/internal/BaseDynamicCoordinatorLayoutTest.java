/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.google.android.material.internal;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.any;

import android.support.annotation.LayoutRes;
import com.google.android.material.testapp.DynamicCoordinatorLayoutActivity;
import com.google.android.material.testapp.R;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewStub;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.rule.ActivityTestRule;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Rule;

/** Base class for tests that are exercising various aspects of {@link CoordinatorLayout}. */
public abstract class BaseDynamicCoordinatorLayoutTest {

  @Rule
  public final ActivityTestRule<DynamicCoordinatorLayoutActivity> activityTestRule =
      new ActivityTestRule<>(DynamicCoordinatorLayoutActivity.class);

  protected CoordinatorLayout mCoordinatorLayout;

  @After
  public void tearDown() {
    getInstrumentation()
        .runOnMainSync(
            new Runnable() {
              @Override
              public void run() {
                // Now that the test is done, replace the activity content view with ViewStub so
                // that it's ready to be replaced for the next test.
                final DynamicCoordinatorLayoutActivity activity = activityTestRule.getActivity();
                activity.setContentView(R.layout.dynamic_coordinator_layout);
                mCoordinatorLayout = null;
              }
            });
  }

  /** Matches views that have parents. */
  private Matcher<View> hasParent() {
    return new TypeSafeMatcher<View>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("has parent");
      }

      @Override
      public boolean matchesSafely(View view) {
        return view.getParent() != null;
      }
    };
  }

  /** Inflates the <code>ViewStub</code> with the passed layout resource. */
  protected ViewAction inflateViewStub(final @LayoutRes int layoutResId) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return allOf(isAssignableFrom(ViewStub.class), hasParent());
      }

      @Override
      public String getDescription() {
        return "Inflates view stub";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        ViewStub viewStub = (ViewStub) view;
        viewStub.setLayoutResource(layoutResId);
        viewStub.inflate();

        mCoordinatorLayout = activityTestRule.getActivity().findViewById(viewStub.getInflatedId());

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  protected ViewAction setLayoutDirection(final int layoutDir) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return any(View.class);
      }

      @Override
      public String getDescription() {
        return "Sets layout direction";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        ViewCompat.setLayoutDirection(view, layoutDir);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }
}
