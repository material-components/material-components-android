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

package android.support.design.testutils;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;
import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;

public class NavigationViewActions {
    /**
     * Sets item text appearance on the content of the navigation view.
     */
    public static ViewAction setItemTextAppearance(final @StyleRes int resId) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Set item text appearance";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                NavigationView navigationView = (NavigationView) view;
                navigationView.setItemTextAppearance(resId);

                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    /**
     * Sets item text color on the content of the navigation view.
     */
    public static ViewAction setItemTextColor(final ColorStateList textColor) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Set item text color";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                NavigationView navigationView = (NavigationView) view;
                navigationView.setItemTextColor(textColor);

                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    /**
     * Sets item background on the content of the navigation view.
     */
    public static ViewAction setItemBackground(final Drawable itemBackground) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Set item background";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                NavigationView navigationView = (NavigationView) view;
                navigationView.setItemBackground(itemBackground);

                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    /**
     * Sets item background on the content of the navigation view.
     */
    public static ViewAction setItemBackgroundResource(final @DrawableRes int resId) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Set item background";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                NavigationView navigationView = (NavigationView) view;
                navigationView.setItemBackgroundResource(resId);

                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    /**
     * Sets item icon tint list on the content of the navigation view.
     */
    public static ViewAction setItemIconTintList(final @Nullable ColorStateList tint) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Set item icon tint list";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                NavigationView navigationView = (NavigationView) view;
                navigationView.setItemIconTintList(tint);

                uiController.loopMainThreadUntilIdle();
            }
        };
    }
}
