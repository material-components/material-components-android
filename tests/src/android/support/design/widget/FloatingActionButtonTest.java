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

import static android.support.design.testutils.FloatingActionButtonActions.setBackgroundTintColor;
import static android.support.design.testutils.FloatingActionButtonActions.setImageResource;
import static android.support.design.testutils.FloatingActionButtonActions.setSize;
import static android.support.design.testutils.TestUtilsMatchers.withFabBackgroundFill;
import static android.support.design.testutils.TestUtilsMatchers.withFabContentHeight;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import android.graphics.Color;
import android.support.design.test.R;
import android.support.design.testutils.TestUtils;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;

@SmallTest
public class FloatingActionButtonTest
        extends BaseInstrumentationTestCase<FloatingActionButtonActivity> {

    public FloatingActionButtonTest() {
        super(FloatingActionButtonActivity.class);
    }

    @Test
    public void testDefaultBackgroundTint() {
        final int colorAccent = TestUtils.getThemeAttrColor(
                mActivityTestRule.getActivity(), R.attr.colorAccent);
        onView(withId(R.id.fab_standard))
                .check(matches(withFabBackgroundFill(colorAccent)));
    }

    @Test
    public void testSetTintOnDefaultBackgroundTint() {
        onView(withId(R.id.fab_standard))
                .perform(setBackgroundTintColor(Color.GREEN))
                .check(matches(withFabBackgroundFill(Color.GREEN)));
    }

    @Test
    public void testDeclaredBackgroundTint() {
        onView(withId(R.id.fab_tint))
                .check(matches(withFabBackgroundFill(Color.MAGENTA)));
    }

    @Test
    public void testSetTintOnDeclaredBackgroundTint() {
        onView(withId(R.id.fab_tint))
                .perform(setBackgroundTintColor(Color.GREEN))
                .check(matches(withFabBackgroundFill(Color.GREEN)));
    }

    @Test
    public void setVectorDrawableSrc() {
        onView(withId(R.id.fab_standard))
                .perform(setImageResource(R.drawable.vector_icon));
    }

    @Test
    public void testSetMiniSize() {
        final int miniSize = mActivityTestRule.getActivity().getResources()
                .getDimensionPixelSize(R.dimen.fab_mini_height);

        onView(withId(R.id.fab_standard))
                .perform(setSize(FloatingActionButton.SIZE_MINI))
                .check(matches(withFabContentHeight(miniSize)));
    }

    @Test
    public void testSetSizeToggle() {
        final int miniSize = mActivityTestRule.getActivity().getResources()
                .getDimensionPixelSize(R.dimen.fab_mini_height);
        final int normalSize = mActivityTestRule.getActivity().getResources()
                .getDimensionPixelSize(R.dimen.fab_normal_height);

        onView(withId(R.id.fab_standard))
                .perform(setSize(FloatingActionButton.SIZE_MINI))
                .check(matches(withFabContentHeight(miniSize)));

        onView(withId(R.id.fab_standard))
                .perform(setSize(FloatingActionButton.SIZE_NORMAL))
                .check(matches(withFabContentHeight(normalSize)));
    }

}
