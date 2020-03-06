/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.google.android.material.textfield;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.AccessibilityChecks.accessibilityAssertion;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.google.android.material.testutils.TestUtilsActions.waitFor;
import static com.google.android.material.testutils.TextInputLayoutActions.clickIcon;
import static com.google.android.material.testutils.TextInputLayoutActions.skipAnimations;
import static com.google.android.material.testutils.TextInputLayoutMatchers.endIconHasContentDescription;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;

import android.app.Activity;
import android.widget.AutoCompleteTextView;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.ExposedDropdownMenuActivity;
import com.google.android.material.testapp.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class ExposedDropdownMenuTest {
  @Rule
  public final ActivityTestRule<ExposedDropdownMenuActivity> activityTestRule =
      new ActivityTestRule<>(ExposedDropdownMenuActivity.class);

  private static final String INPUT_TEXT = "I";

  @Test
  public void testMenuIsNonEditableWithInputTypeNone() {
    final Activity activity = activityTestRule.getActivity();
    final AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled);

    assertNull(editText.getKeyListener());
  }

  @Test
  public void testEndIconClickShowsDropdownPopup() {
    final Activity activity = activityTestRule.getActivity();
    final AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled);

    onView(withId(R.id.filled_dropdown)).perform(clickIcon(true));
    onView(withId(R.id.filled_dropdown)).perform(skipAnimations());

    assertThat(editText.isPopupShowing(), is(true));
  }

  @Test
  public void testEndIconClickHidesDropdownPopup() {
    final Activity activity = activityTestRule.getActivity();
    final AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled);
    // Show dropdown.
    onView(withId(R.id.filled_dropdown)).perform(clickIcon(true));
    onView(withId(R.id.filled_dropdown)).perform(skipAnimations());

    // Hide dropdown.
    onView(withId(R.id.filled_dropdown)).perform(clickIcon(true));
    onView(withId(R.id.filled_dropdown)).perform(skipAnimations());

    assertThat(editText.isPopupShowing(), is(false));
  }

  @Test
  public void testLayoutClickShowsDropdownPopup() {
    final Activity activity = activityTestRule.getActivity();
    final AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled);

    onView(withId(R.id.filled_dropdown)).perform(click());
    onView(withId(R.id.filled_dropdown)).perform(skipAnimations());

    assertThat(editText.isPopupShowing(), is(true));
  }

  @Test
  public void testLayoutClickHidesDropdownPopup() {
    final Activity activity = activityTestRule.getActivity();
    final AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled);
    // Show dropdown.
    onView(withId(R.id.filled_dropdown)).perform(click());
    onView(withId(R.id.filled_dropdown)).perform(skipAnimations());


    // Hide dropdown.
    onView(withId(R.id.filled_dropdown)).perform(click());
    onView(withId(R.id.filled_dropdown)).perform(skipAnimations());

    assertThat(editText.isPopupShowing(), is(false));
  }

  @Test
  public void testTextInputShowsDropdownPopup() {
    final Activity activity = activityTestRule.getActivity();
    final AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled_editable);
    // Makes sure dropdown is not showing before entering input.
    onView(withId(R.id.filled_editable_dropdown)).perform(clickIcon(true));
    onView(withId(R.id.filled_editable_dropdown)).perform(clickIcon(true));
    onView(withId(R.id.filled_editable_dropdown)).perform(skipAnimations());

    onView(isRoot()).perform(waitFor(2000));
    onView(withId(R.id.edittext_filled_editable)).perform(typeTextIntoFocusedView(INPUT_TEXT));
    onView(isRoot()).perform(waitFor(2000));

    assertThat(editText.isPopupShowing(), is(true));
  }

  @Test
  public void testClearingTextInputHidesDropdownPopup() {
    final Activity activity = activityTestRule.getActivity();
    final AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled_editable);
    onView(withId(R.id.edittext_filled_editable)).perform(typeText(INPUT_TEXT));

    onView(withId(R.id.edittext_filled_editable)).perform(clearText());

    assertThat(editText.isPopupShowing(), is(false));
  }

  @Test
  public void testEndIconHasDefaultContentDescription() {
    onView(withId(R.id.filled_dropdown)).check(matches(endIconHasContentDescription()));
  }

  @Test
  public void testEndIconIsAccessible() {
    onView(allOf(withId(R.id.text_input_end_icon),
        withContentDescription(R.string.exposed_dropdown_menu_content_description),
        isDescendantOfA(withId(R.id.filled_dropdown))))
        .check(accessibilityAssertion());
  }
}
