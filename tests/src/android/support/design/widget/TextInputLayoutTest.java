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

import static android.support.design.testutils.TestUtilsActions.setCompoundDrawablesRelative;
import static android.support.design.testutils.TestUtilsActions.setEnabled;
import static android.support.design.testutils.TestUtilsMatchers.withCompoundDrawable;
import static android.support.design.testutils.TextInputLayoutActions.setError;
import static android.support.design.testutils.TextInputLayoutActions.setErrorEnabled;
import static android.support.design.testutils.TextInputLayoutActions
        .setPasswordVisibilityToggleEnabled;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.test.R;
import android.support.test.annotation.UiThreadTest;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.v4.widget.TextViewCompat;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import org.junit.Test;

@SmallTest
public class TextInputLayoutTest extends BaseInstrumentationTestCase<TextInputLayoutActivity> {

    private static final String ERROR_MESSAGE_1 = "An error has occured";
    private static final String ERROR_MESSAGE_2 = "Some other error has occured";

    private static final String INPUT_TEXT = "Random input text";

    public TextInputLayoutTest() {
        super(TextInputLayoutActivity.class);
    }

    @Test
    public void testTypingTextCollapsesHint() {
        // Type some text
        onView(withId(R.id.textinput_edittext)).perform(typeText(INPUT_TEXT));
        // ...and check that the hint has collapsed
        onView(withId(R.id.textinput)).check(isHintExpanded(false));
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

    @Test
    public void testPasswordToggleClick() {
        // Type some text on the EditText
        onView(withId(R.id.textinput_edittext_pwd)).perform(typeText(INPUT_TEXT));

        final Activity activity = mActivityTestRule.getActivity();
        final EditText textInput = (EditText) activity.findViewById(R.id.textinput_edittext_pwd);

        // Assert that the password is disguised
        assertNotEquals(INPUT_TEXT, textInput.getLayout().getText().toString());

        // Now click the toggle button
        onView(withId(R.id.text_input_password_toggle)).perform(click());

        // And assert that the password is not disguised
        assertEquals(INPUT_TEXT, textInput.getLayout().getText().toString());
    }

    @Test
    public void testPasswordToggleDisable() {
        final Activity activity = mActivityTestRule.getActivity();
        final EditText textInput = (EditText) activity.findViewById(R.id.textinput_edittext_pwd);

        // Set some text on the EditText
        onView(withId(R.id.textinput_edittext_pwd))
                .perform(typeText(INPUT_TEXT));
        // Assert that the password is disguised
        assertNotEquals(INPUT_TEXT, textInput.getLayout().getText().toString());

        // Disable the password toggle
        onView(withId(R.id.textinput_password))
                .perform(setPasswordVisibilityToggleEnabled(false));

        // Check that the password toggle view is not visible
        onView(withId(R.id.text_input_password_toggle)).check(matches(not(isDisplayed())));
        // ...and that the password is disguised still
        assertNotEquals(INPUT_TEXT, textInput.getLayout().getText().toString());
    }

    @Test
    public void testPasswordToggleDisableWhenVisible() {
        final Activity activity = mActivityTestRule.getActivity();
        final EditText textInput = (EditText) activity.findViewById(R.id.textinput_edittext_pwd);

        // Type some text on the EditText
        onView(withId(R.id.textinput_edittext_pwd)).perform(typeText(INPUT_TEXT));
        // Assert that the password is disguised
        assertNotEquals(INPUT_TEXT, textInput.getLayout().getText().toString());

        // Now click the toggle button
        onView(withId(R.id.text_input_password_toggle)).perform(click());
        // Disable the password toggle
        onView(withId(R.id.textinput_password))
                .perform(setPasswordVisibilityToggleEnabled(false));

        // Check that the password is disguised again
        assertNotEquals(INPUT_TEXT, textInput.getLayout().getText().toString());
    }

    @Test
    public void testPasswordToggleMaintainsCompoundDrawables() {
        // Set a known set of test compound drawables on the EditText
        final Drawable start = new ColorDrawable(Color.RED);
        final Drawable top = new ColorDrawable(Color.GREEN);
        final Drawable end = new ColorDrawable(Color.BLUE);
        final Drawable bottom = new ColorDrawable(Color.BLACK);
        onView(withId(R.id.textinput_edittext_pwd))
                .perform(setCompoundDrawablesRelative(start, top, end, bottom));

        // Enable the password toggle and check that the start, top and bottom drawables are
        // maintained
        onView(withId(R.id.textinput_password))
                .perform(setPasswordVisibilityToggleEnabled(true));
        onView(withId(R.id.textinput_edittext_pwd))
                .check(matches(withCompoundDrawable(0, start)))
                .check(matches(withCompoundDrawable(1, top)))
                .check(matches(not(withCompoundDrawable(2, end))))
                .check(matches(withCompoundDrawable(3, bottom)));

        // Now disable the password toggle and check that all of the original compound drawables
        // are set
        onView(withId(R.id.textinput_password))
                .perform(setPasswordVisibilityToggleEnabled(false));
        onView(withId(R.id.textinput_edittext_pwd))
                .check(matches(withCompoundDrawable(0, start)))
                .check(matches(withCompoundDrawable(1, top)))
                .check(matches(withCompoundDrawable(2, end)))
                .check(matches(withCompoundDrawable(3, bottom)));
    }

    @Test
    public void testSetEnabledFalse() {
        // First click on the EditText, so that it is focused and the hint collapses...
        onView(withId(R.id.textinput_edittext)).perform(click());

        // Now disable the TextInputLayout and check that the hint expands
        onView(withId(R.id.textinput))
                .perform(setEnabled(false))
                .check(isHintExpanded(true));

        // Finally check that the EditText is no longer enabled
        onView(withId(R.id.textinput_edittext)).check(matches(not(isEnabled())));
    }

    @Test
    public void testSetEnabledFalseWithText() {
        // First set some text, then disable the TextInputLayout
        onView(withId(R.id.textinput_edittext))
                .perform(typeText(INPUT_TEXT));
        onView(withId(R.id.textinput)).perform(setEnabled(false));

        // Now check that the EditText is no longer enabled
        onView(withId(R.id.textinput_edittext)).check(matches(not(isEnabled())));
    }

    @UiThreadTest
    @Test
    public void testExtractUiHintSet() {
        final Activity activity = mActivityTestRule.getActivity();

        // Set a hint on the TextInputLayout
        final TextInputLayout layout = (TextInputLayout) activity.findViewById(R.id.textinput);
        layout.setHint(INPUT_TEXT);

        final EditText editText = (EditText) activity.findViewById(R.id.textinput_edittext);

        // Now manually pass in a EditorInfo to the EditText and make sure it updates the
        // hintText to our known value
        final EditorInfo info = new EditorInfo();
        editText.onCreateInputConnection(info);

        assertEquals(INPUT_TEXT, info.hintText);
    }

    /**
     * Regression test for b/31663756.
     */
    @UiThreadTest
    @Test
    public void testDrawableStateChanged() {
        final Activity activity = mActivityTestRule.getActivity();
        final TextInputLayout layout = (TextInputLayout) activity.findViewById(R.id.textinput);

        // Force a drawable state change.
        layout.drawableStateChanged();
    }

    @Test
    public void testMaintainsLeftRightCompoundDrawables() throws Throwable {
        final Activity activity = mActivityTestRule.getActivity();

        // Set a known set of test compound drawables on the EditText
        final Drawable left = new ColorDrawable(Color.RED);
        final Drawable top = new ColorDrawable(Color.GREEN);
        final Drawable right = new ColorDrawable(Color.BLUE);
        final Drawable bottom = new ColorDrawable(Color.BLACK);

        final TextInputEditText editText = new TextInputEditText(activity);
        editText.setCompoundDrawables(left, top, right, bottom);

        // Now add the EditText to a TextInputLayout
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextInputLayout til = (TextInputLayout)
                        activity.findViewById(R.id.textinput_noedittext);
                til.addView(editText);
            }
        });

        // Finally assert that all of the drawables are untouched
        final Drawable[] compoundDrawables = editText.getCompoundDrawables();
        assertSame(left, compoundDrawables[0]);
        assertSame(top, compoundDrawables[1]);
        assertSame(right, compoundDrawables[2]);
        assertSame(bottom, compoundDrawables[3]);
    }

    @Test
    public void testMaintainsStartEndCompoundDrawables() throws Throwable {
        final Activity activity = mActivityTestRule.getActivity();

        // Set a known set of test compound drawables on the EditText
        final Drawable start = new ColorDrawable(Color.RED);
        final Drawable top = new ColorDrawable(Color.GREEN);
        final Drawable end = new ColorDrawable(Color.BLUE);
        final Drawable bottom = new ColorDrawable(Color.BLACK);

        final TextInputEditText editText = new TextInputEditText(activity);
        TextViewCompat.setCompoundDrawablesRelative(editText, start, top, end, bottom);

        // Now add the EditText to a TextInputLayout
        mActivityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextInputLayout til = (TextInputLayout)
                        activity.findViewById(R.id.textinput_noedittext);
                til.addView(editText);
            }
        });

        // Finally assert that all of the drawables are untouched
        final Drawable[] compoundDrawables = TextViewCompat.getCompoundDrawablesRelative(editText);
        assertSame(start, compoundDrawables[0]);
        assertSame(top, compoundDrawables[1]);
        assertSame(end, compoundDrawables[2]);
        assertSame(bottom, compoundDrawables[3]);
    }

    static ViewAssertion isHintExpanded(final boolean expanded) {
        return new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                assertTrue(view instanceof TextInputLayout);
                assertEquals(expanded, ((TextInputLayout) view).isHintExpanded());
            }
        };
    }
}
