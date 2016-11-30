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

import static android.support.design.testutils.TestUtilsMatchers.withFabBackgroundFill;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.design.test.R;
import android.support.design.testutils.TestUtils;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;

public class FloatingActionButtonTest
        extends BaseInstrumentationTestCase<FloatingActionButtonActivity> {

    public FloatingActionButtonTest() {
        super(FloatingActionButtonActivity.class);
    }

    @Test
    @SmallTest
    public void testDefaultBackgroundColor() {
        checkBackgroundColor(R.id.fab_standard,
                TestUtils.getThemeAttrColor(mActivityTestRule.getActivity(), R.attr.colorAccent));
    }

    @Test
    @SmallTest
    public void testTintedBackgroundColor() {
        checkBackgroundColor(R.id.fab_tint, 0xFFFF00FF);
    }

    private void checkBackgroundColor(@IdRes int id, @ColorInt int exceptedColor) {
        onView(withId(id)).check(matches(withFabBackgroundFill(exceptedColor)));
    }

}
