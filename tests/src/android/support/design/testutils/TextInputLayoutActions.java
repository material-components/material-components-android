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

import android.support.design.widget.TextInputLayout;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.Matcher;

public class TextInputLayoutActions {

    public static ViewAction setErrorEnabled(final boolean enabled) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TextInputLayout.class);
            }

            @Override
            public String getDescription() {
                return "Enables/disables the error";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                TextInputLayout layout = (TextInputLayout) view;
                layout.setErrorEnabled(enabled);

                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    public static ViewAction setError(final CharSequence error) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TextInputLayout.class);
            }

            @Override
            public String getDescription() {
                return "Sets the error";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                TextInputLayout layout = (TextInputLayout) view;
                layout.setError(error);

                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    public static ViewAction setPasswordVisibilityToggleEnabled(final boolean enabled) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TextInputLayout.class);
            }

            @Override
            public String getDescription() {
                return "Sets the error";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                TextInputLayout layout = (TextInputLayout) view;
                layout.setPasswordVisibilityToggleEnabled(enabled);

                uiController.loopMainThreadUntilIdle();
            }
        };
    }


}
