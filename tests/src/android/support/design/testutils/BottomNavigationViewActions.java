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

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;

import org.hamcrest.Matcher;


public class BottomNavigationViewActions {
    /**
     * Sets item icon tint list on the content of the bottom navigation view.
     */
    public static ViewAction setItemIconTintList(@Nullable final ColorStateList tint) {
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

                BottomNavigationView navigationView = (BottomNavigationView) view;
                navigationView.setItemIconTintList(tint);

                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    /**
     * Sets icon for the menu item of the navigation view.
     */
    public static ViewAction setIconForMenuItem(@IdRes final int menuItemId,
            final Drawable iconDrawable) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Set menu item icon";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                BottomNavigationView navigationView = (BottomNavigationView) view;
                navigationView.getMenu().findItem(menuItemId).setIcon(iconDrawable);

                uiController.loopMainThreadUntilIdle();
            }
        };
    }
}
