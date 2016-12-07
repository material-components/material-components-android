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

package android.support.design.testutils;

import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;
import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;

public class TabLayoutActions {
    /**
     * Wires <code>TabLayout</code> to <code>ViewPager</code> content.
     */
    public static ViewAction setupWithViewPager(final @Nullable ViewPager viewPager) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayingAtLeast(90);
            }

            @Override
            public String getDescription() {
                return "Setup with ViewPager content";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                TabLayout tabLayout = (TabLayout) view;
                tabLayout.setupWithViewPager(viewPager);

                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    /**
     * Wires <code>TabLayout</code> to <code>ViewPager</code> content.
     */
    public static ViewAction setupWithViewPager(final @Nullable ViewPager viewPager,
            final boolean autoRefresh) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayingAtLeast(90);
            }

            @Override
            public String getDescription() {
                return "Setup with ViewPager content";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                TabLayout tabLayout = (TabLayout) view;
                tabLayout.setupWithViewPager(viewPager, autoRefresh);

                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    /**
     * Selects the specified tab in the <code>TabLayout</code>.
     */
    public static ViewAction selectTab(final int tabIndex) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayingAtLeast(90);
            }

            @Override
            public String getDescription() {
                return "Selects tab";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                TabLayout tabLayout = (TabLayout) view;
                tabLayout.getTabAt(tabIndex).select();

                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    /**
     * Sets the specified tab mode in the <code>TabLayout</code>.
     */
    public static ViewAction setTabMode(final int tabMode) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayingAtLeast(90);
            }

            @Override
            public String getDescription() {
                return "Sets tab mode";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                TabLayout tabLayout = (TabLayout) view;
                tabLayout.setTabMode(tabMode);

                uiController.loopMainThreadUntilIdle();
            }
        };
    }
}
