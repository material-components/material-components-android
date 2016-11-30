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

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;

import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;

import org.hamcrest.Matcher;

public class FloatingActionButtonActions {

    public static ViewAction setBackgroundTintColor(@ColorInt final int color) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(FloatingActionButton.class);
            }

            @Override
            public String getDescription() {
                return "Sets FloatingActionButton background tint";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                final FloatingActionButton fab = (FloatingActionButton) view;
                fab.setBackgroundTintList(ColorStateList.valueOf(color));

                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    public static ViewAction setImageResource(@DrawableRes final int resId) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(FloatingActionButton.class);
            }

            @Override
            public String getDescription() {
                return "Sets FloatingActionButton image resource";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                final FloatingActionButton fab = (FloatingActionButton) view;
                fab.setImageResource(resId);

                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    public static ViewAction setSize(@FloatingActionButton.Size final int size) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(FloatingActionButton.class);
            }

            @Override
            public String getDescription() {
                return "Sets FloatingActionButton size";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                final FloatingActionButton fab = (FloatingActionButton) view;
                fab.setSize(size);

                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    public static ViewAction setLayoutGravity(final int gravity) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return "Sets Views layout_gravity";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                CoordinatorLayout.LayoutParams lp =
                        (CoordinatorLayout.LayoutParams) view.getLayoutParams();
                lp.gravity = gravity;
                view.requestLayout();

                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    public static ViewAction hideThenShow(final int animDuration) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(FloatingActionButton.class);
            }

            @Override
            public String getDescription() {
                return "Calls hide() then show()";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                FloatingActionButton fab = (FloatingActionButton) view;
                fab.hide();
                fab.show();

                uiController.loopMainThreadForAtLeast(animDuration + 100);
            }
        };
    }

    public static ViewAction showThenHide(final int animDuration) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(FloatingActionButton.class);
            }

            @Override
            public String getDescription() {
                return "Calls show() then hide()";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                FloatingActionButton fab = (FloatingActionButton) view;
                fab.show();
                fab.hide();

                uiController.loopMainThreadForAtLeast(animDuration + 50);
            }
        };
    }

}
