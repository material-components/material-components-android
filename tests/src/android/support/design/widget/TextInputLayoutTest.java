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

import static android.support.design.testutils.TextInputLayoutActions.setError;
import static android.support.design.testutils.TextInputLayoutActions.setErrorEnabled;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.not;

import android.support.design.test.R;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;

@SmallTest
public class TextInputLayoutTest extends BaseInstrumentationTestCase<TextInputLayoutActivity> {

    private static final String ERROR_MESSAGE_1 = "An error has occured";
    private static final String ERROR_MESSAGE_2 = "Some other error has occured";

    public TextInputLayoutTest() {
        super(TextInputLayoutActivity.class);
    }

    @Test
    public void testSetErrorEnablesErrorIsDisplayed() {
        onView(withId(R.id.textinput)).perform(setError(ERROR_MESSAGE_1));
        onView(withText(ERROR_MESSAGE_1)).check(matches(isDisplayed()));
    }

    @Test
    public void testDisabledErrorIsNotDisplayed() {
        // First show an error, and then disable error functionality
        onView(withId(R.id.textinput))
                .perform(setError(ERROR_MESSAGE_1))
                .perform(setErrorEnabled(false));

        // Check that the error is no longer there
        onView(withText(ERROR_MESSAGE_1)).check(doesNotExist());
    }

    @Test
    public void testSetErrorOnDisabledSetErrorIsDisplayed() {
        // First show an error, and then disable error functionality
        onView(withId(R.id.textinput))
                .perform(setError(ERROR_MESSAGE_1))
                .perform(setErrorEnabled(false));

        // Now show a different error message
        onView(withId(R.id.textinput)).perform(setError(ERROR_MESSAGE_2));
        // And check that it is displayed
        onView(withText(ERROR_MESSAGE_2)).check(matches(isDisplayed()));
    }

}
