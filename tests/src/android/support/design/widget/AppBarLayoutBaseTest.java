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

import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.design.test.R;
import android.support.design.testutils.Shakespeare;
import android.support.test.InstrumentationRegistry;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.TextView;

import static android.support.design.testutils.TestUtilsActions.setText;
import static android.support.design.testutils.TestUtilsActions.setTitle;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public abstract class AppBarLayoutBaseTest extends BaseDynamicCoordinatorLayoutTest {

    protected AppBarLayout mAppBar;

    protected CollapsingToolbarLayout mCollapsingToolbar;

    protected Toolbar mToolbar;

    protected TextView mTextView;

    protected void configureContent(final @LayoutRes int layoutResId,
            final @StringRes int titleResId) {
        onView(withId(R.id.coordinator_stub)).perform(inflateViewStub(layoutResId));

        mAppBar = (AppBarLayout) mCoordinatorLayout.findViewById(R.id.app_bar);
        mCollapsingToolbar =
                (CollapsingToolbarLayout) mAppBar.findViewById(R.id.collapsing_app_bar);
        mToolbar = (Toolbar) mCollapsingToolbar.findViewById(R.id.toolbar);

        final AppCompatActivity activity = mActivityTestRule.getActivity();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activity.setSupportActionBar(mToolbar);
            }
        });

        if (mCollapsingToolbar != null) {
            onView(withId(R.id.collapsing_app_bar)).perform(
                    setTitle(activity.getString(titleResId)));
        }

        TextView dialog = (TextView) mCoordinatorLayout.findViewById(R.id.textview_dialogue);
        if (dialog != null) {
            onView(withId(R.id.textview_dialogue)).perform(
                    setText(TextUtils.concat(Shakespeare.DIALOGUE)));
        }
    }
}
