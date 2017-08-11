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

package android.support.design.widget;

import static android.support.design.testutils.CollapsingToolbarLayoutActions.setContentScrimColor;
import static android.support.design.testutils.SwipeUtils.swipeDown;
import static android.support.design.testutils.SwipeUtils.swipeUp;
import static android.support.design.testutils.TestUtilsActions.setText;
import static android.support.design.testutils.TestUtilsActions.setTitle;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

import android.graphics.Color;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.design.testapp.R;
import android.support.design.testutils.Shakespeare;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.TextView;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public abstract class AppBarLayoutBaseTest extends BaseDynamicCoordinatorLayoutTest {

  protected AppBarLayout mAppBar;

  protected CollapsingToolbarLayout mCollapsingToolbar;

  protected Toolbar mToolbar;

  protected TextView mTextView;

  protected float mDefaultElevationValue;

  protected static void performVerticalSwipeUpGesture(
      @IdRes int containerId, final int swipeX, final int swipeStartY, final int swipeAmountY) {
    onView(withId(containerId)).perform(swipeUp(swipeX, swipeStartY, swipeAmountY));
  }

  protected static void performVerticalSwipeDownGesture(
      @IdRes int containerId, final int swipeX, final int swipeStartY, final int swipeAmountY) {
    onView(withId(containerId)).perform(swipeDown(swipeX, swipeStartY, swipeAmountY));
  }

  @CallSuper
  protected void configureContent(@LayoutRes final int layoutResId, @StringRes final int titleResId)
      throws Throwable {
    onView(withId(R.id.coordinator_stub)).perform(inflateViewStub(layoutResId));

    mAppBar = mCoordinatorLayout.findViewById(R.id.app_bar);
    mCollapsingToolbar = mAppBar.findViewById(R.id.collapsing_app_bar);
    mToolbar = mAppBar.findViewById(R.id.toolbar);

    final AppCompatActivity activity = activityTestRule.getActivity();
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            activity.setSupportActionBar(mToolbar);
          }
        });

    final CharSequence activityTitle = activity.getString(titleResId);
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            activity.setTitle(activityTitle);
          }
        });
    getInstrumentation().waitForIdleSync();

    if (mCollapsingToolbar != null) {
      onView(withId(R.id.collapsing_app_bar))
          .perform(setTitle(activityTitle))
          .perform(setContentScrimColor(Color.MAGENTA));
    }

    TextView dialog = mCoordinatorLayout.findViewById(R.id.textview_dialogue);
    if (dialog != null) {
      onView(withId(R.id.textview_dialogue))
          .perform(setText(TextUtils.concat(Shakespeare.DIALOGUE)));
    }

    mDefaultElevationValue = mAppBar.getResources().getDimension(R.dimen.design_appbar_elevation);
  }

  protected void assertAppBarElevation(float expectedValue) {
    if (Build.VERSION.SDK_INT >= 21) {
      assertEquals(expectedValue, ViewCompat.getElevation(mAppBar), 0.05f);
    }
  }

  protected void assertScrimAlpha(@IntRange(from = 0, to = 255) int alpha) {
    SystemClock.sleep(300);
    onView(withId(R.id.collapsing_app_bar)).check(matches(withScrimAlpha(alpha)));
  }

  static Matcher withScrimAlpha(final int alpha) {
    return new TypeSafeMatcher<CollapsingToolbarLayout>() {
      @Override
      public void describeTo(Description description) {
        description.appendText("CollapsingToolbarLayout has content scrim with alpha: " + alpha);
      }

      @Override
      protected boolean matchesSafely(CollapsingToolbarLayout view) {
        return alpha == view.getScrimAlpha();
      }
    };
  }
}
