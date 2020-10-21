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

package com.google.android.material.appbar;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.google.android.material.testutils.CollapsingToolbarLayoutActions.setContentScrimColor;
import static com.google.android.material.testutils.SwipeUtils.swipeDown;
import static com.google.android.material.testutils.SwipeUtils.swipeUp;
import static com.google.android.material.testutils.TestUtilsActions.setText;
import static com.google.android.material.testutils.TestUtilsActions.setTitle;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import android.graphics.Color;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.SystemClock;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.IntRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import com.google.android.material.internal.BaseDynamicCoordinatorLayoutTest;
import com.google.android.material.testapp.R;
import com.google.android.material.testutils.AccessibilityUtils;
import com.google.android.material.testutils.Shakespeare;
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
    activityTestRule.runOnUiThread(() -> activity.setSupportActionBar(mToolbar));

    final CharSequence activityTitle = activity.getString(titleResId);
    activityTestRule.runOnUiThread(() -> activity.setTitle(activityTitle));
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

  static Matcher<View> withScrimAlpha(final int alpha) {
    return new TypeSafeMatcher<View>(CollapsingToolbarLayout.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("CollapsingToolbarLayout has content scrim with alpha: " + alpha);
      }

      @Override
      protected boolean matchesSafely(View view) {
        return alpha == ((CollapsingToolbarLayout) view).getScrimAlpha();
      }
    };
  }

  protected void assertAccessibilityHasScrollForwardAction(boolean hasScrollForward) {
    if (VERSION.SDK_INT >= 21) {
      assertThat(
          AccessibilityUtils.hasAction(
              mCoordinatorLayout, AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD),
          equalTo(hasScrollForward));
    }
  }

  protected void assertAccessibilityHasScrollBackwardAction(boolean hasScrollBackward) {
    if (VERSION.SDK_INT >= 21) {
      assertThat(
          AccessibilityUtils.hasAction(
              mCoordinatorLayout, AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD),
          equalTo(hasScrollBackward));
    }
  }
}
