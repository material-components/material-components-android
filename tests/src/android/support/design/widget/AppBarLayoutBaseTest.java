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

import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.design.test.R;
import android.support.design.testutils.Shakespeare;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import static android.support.design.testutils.TestUtilsActions.setText;
import static android.support.design.testutils.TestUtilsActions.setTitle;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertEquals;

public abstract class AppBarLayoutBaseTest extends BaseDynamicCoordinatorLayoutTest {

    protected AppBarLayout mAppBar;

    protected CollapsingToolbarLayout mCollapsingToolbar;

    protected Toolbar mToolbar;

    protected TextView mTextView;

    protected float mDefaultElevationValue;

    protected static void performVerticalSwipeUpGesture(@IdRes int containerId, final int swipeX,
            final int swipeStartY, final int swipeAmountY) {
        onView(withId(containerId)).perform(new GeneralSwipeAction(
                Swipe.SLOW,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        return new float[] { swipeX, swipeStartY };
                    }
                },
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        return new float[] { swipeX, swipeStartY - swipeAmountY };
                    }
                }, Press.FINGER));
    }

    protected static void performVerticalSwipeDownGesture(@IdRes int containerId, final int swipeX,
            final int swipeStartY, final int swipeAmountY) {
        onView(withId(containerId)).perform(new GeneralSwipeAction(
                Swipe.SLOW,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        return new float[] { swipeX, swipeStartY };
                    }
                },
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        return new float[] { swipeX, swipeStartY + swipeAmountY };
                    }
                }, Press.FINGER));
    }

    @CallSuper
    protected void configureContent(final @LayoutRes int layoutResId,
            final @StringRes int titleResId) {
        onView(withId(R.id.coordinator_stub)).perform(inflateViewStub(layoutResId));

        mAppBar = (AppBarLayout) mCoordinatorLayout.findViewById(R.id.app_bar);
        mCollapsingToolbar =
                (CollapsingToolbarLayout) mAppBar.findViewById(R.id.collapsing_app_bar);
        mToolbar = (Toolbar) mAppBar.findViewById(R.id.toolbar);

        final AppCompatActivity activity = mActivityTestRule.getActivity();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activity.setSupportActionBar(mToolbar);
            }
        });

        final CharSequence activityTitle = activity.getString(titleResId);
        activity.setTitle(activityTitle);
        if (mCollapsingToolbar != null) {
            onView(withId(R.id.collapsing_app_bar)).perform(setTitle(activityTitle));
        }

        TextView dialog = (TextView) mCoordinatorLayout.findViewById(R.id.textview_dialogue);
        if (dialog != null) {
            onView(withId(R.id.textview_dialogue)).perform(
                    setText(TextUtils.concat(Shakespeare.DIALOGUE)));
        }

        mDefaultElevationValue = mAppBar.getResources()
                .getDimension(R.dimen.design_appbar_elevation);
    }

    protected void assertAppBarElevation(float expectedValue) {
        if (Build.VERSION.SDK_INT >= 21) {
            assertEquals(expectedValue, ViewCompat.getElevation(mAppBar), 0.05f);
        }
    }
}
