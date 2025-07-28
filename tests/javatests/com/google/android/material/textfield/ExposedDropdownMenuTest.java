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
import static androidx.test.espresso.accessibility.AccessibilityChecks.accessibilityAssertion;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.google.android.material.testutils.EditTextActions.setSingleLine;
import static com.google.android.material.testutils.TestUtilsActions.requestFocus;
import static com.google.android.material.testutils.TestUtilsActions.waitFor;
import static com.google.android.material.testutils.TextInputLayoutActions.clickIcon;
import static com.google.android.material.testutils.TextInputLayoutActions.setInputType;
import static com.google.android.material.testutils.TextInputLayoutActions.setRawInputType;
import static com.google.android.material.testutils.TextInputLayoutActions.skipAnimations;
import static com.google.android.material.testutils.TextInputLayoutMatchers.endIconHasContentDescription;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.InputType;
import android.view.KeyEvent;
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

  private static final String INPUT_TEXT = "I";

  @Rule
  public final ActivityTestRule<ExposedDropdownMenuActivity> activityTestRule =
      new ActivityTestRule<>(ExposedDropdownMenuActivity.class);


  @Test
  public void testMenuIsNonEditableWithInputTypeNone() {
    final Activity activity = activityTestRule.getActivity();
    final AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled);

    assertNull(editText.getKeyListener());
  }

  @Test
  public void testNonEditableMenu_hasLayerBackground() {
    Activity activity = activityTestRule.getActivity();

    AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled);

    assertThat(editText.getBackground(), instanceOf(LayerDrawable.class));
  }

  @Test
  public void testEditableMenu_doesNotHaveLayerBackground() {
    Activity activity = activityTestRule.getActivity();

    AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled_editable);

    assertThat(editText.getBackground(), not(instanceOf(LayerDrawable.class)));
  }

  @Test
  public void testSwitchingInputType_updatesBackground() {
    final Activity activity = activityTestRule.getActivity();
    AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled);

    // Switch to an editable type.
    onView(withId(R.id.edittext_filled)).perform(setInputType(InputType.TYPE_CLASS_TEXT));
    Drawable editableTypeBackground = editText.getBackground();
    // Switch back to uneditable.
    onView(withId(R.id.edittext_filled)).perform(setInputType(InputType.TYPE_NULL));

    // Assert background updated.
    assertThat(editableTypeBackground, not(instanceOf(LayerDrawable.class)));
    // Assert second switch updated the background back to an instance of LayerDrawable.
    assertThat(editText.getBackground(), instanceOf(LayerDrawable.class));
  }

  @Test
  public void testSwitchingRawInputType_updatesBackground() {
    final Activity activity = activityTestRule.getActivity();
    AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled);

    // Switch to an editable type.
    onView(withId(R.id.edittext_filled)).perform(setRawInputType(InputType.TYPE_CLASS_TEXT));
    Drawable editableTypeBackground = editText.getBackground();
    // Switch back to uneditable.
    onView(withId(R.id.edittext_filled)).perform(setRawInputType(InputType.TYPE_NULL));

    // Assert background updated.
    assertThat(editableTypeBackground, not(instanceOf(LayerDrawable.class)));
    // Assert second switch updated the background back to an instance of LayerDrawable.
    assertThat(editText.getBackground(), instanceOf(LayerDrawable.class));
  }

  @Test
  public void testSetSimpleItemSelectedColor_succeeds() {
    Activity activity = activityTestRule.getActivity();
    MaterialAutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled);

    editText.setSimpleItemSelectedColor(Color.BLUE);

    assertThat(editText.getSimpleItemSelectedColor(), is(Color.BLUE));
  }

  @Test
  public void testSetSimpleItemSelectedRippleColor_succeeds() {
    Activity activity = activityTestRule.getActivity();
    MaterialAutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled);

    editText.setSimpleItemSelectedRippleColor(ColorStateList.valueOf(Color.BLUE));

    assertThat(
        editText.getSimpleItemSelectedRippleColor(), is(ColorStateList.valueOf(Color.BLUE)));
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

  @Test
  public void testOnKeyDown_enterOnNonEditableField_showsDropDown() {
    final Activity activity = activityTestRule.getActivity();
    final AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled);

    onView(withId(R.id.edittext_filled)).perform(requestFocus());
    onView(withId(R.id.edittext_filled)).perform(pressKey(KeyEvent.KEYCODE_ENTER));
    onView(withId(R.id.filled_dropdown)).perform(skipAnimations());

    assertThat(editText.isPopupShowing(), is(true));
  }

  @Test
  public void testOnKeyDown_spaceOnNonEditableField_showsDropDown() {
    final Activity activity = activityTestRule.getActivity();
    final AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled);

    onView(withId(R.id.edittext_filled)).perform(requestFocus());
    onView(withId(R.id.edittext_filled)).perform(pressKey(KeyEvent.KEYCODE_SPACE));
    onView(withId(R.id.filled_dropdown)).perform(skipAnimations());

    assertThat(editText.isPopupShowing(), is(true));
  }

  @Test
  public void testOnKeyDown_enterOnNonEditableField_hidesDropDown() {
    final Activity activity = activityTestRule.getActivity();
    final AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled);

    onView(withId(R.id.filled_dropdown)).perform(click());
    onView(withId(R.id.filled_dropdown)).perform(skipAnimations());

    onView(withId(R.id.edittext_filled)).perform(requestFocus());
    onView(withId(R.id.edittext_filled)).perform(pressKey(KeyEvent.KEYCODE_ENTER));
    onView(withId(R.id.filled_dropdown)).perform(skipAnimations());

    assertThat(editText.isPopupShowing(), is(false));
  }

  @Test
  public void testOnKeyDown_spaceOnNonEditableField_hidesDropDown() {
    final Activity activity = activityTestRule.getActivity();
    final AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled);

    onView(withId(R.id.filled_dropdown)).perform(click());
    onView(withId(R.id.filled_dropdown)).perform(skipAnimations());

    onView(withId(R.id.edittext_filled)).perform(requestFocus());
    onView(withId(R.id.edittext_filled)).perform(pressKey(KeyEvent.KEYCODE_SPACE));
    onView(withId(R.id.filled_dropdown)).perform(skipAnimations());

    assertThat(editText.isPopupShowing(), is(false));
  }

  @Test
  public void testOnKeyDown_enterOnEditableMultiLineField_doesNotShowDropDown() {
    final Activity activity = activityTestRule.getActivity();
    final AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled_editable);

    onView(withId(R.id.edittext_filled_editable)).perform(requestFocus());
    onView(withId(R.id.edittext_filled_editable)).perform(pressKey(KeyEvent.KEYCODE_ENTER));
    onView(withId(R.id.filled_editable_dropdown)).perform(skipAnimations());

    assertThat(editText.isPopupShowing(), is(false));
  }

  @Test
  public void testOnKeyDown_enterOnEditableSingleLineField_showsDropDown() {
    final Activity activity = activityTestRule.getActivity();
    final AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled_editable);

    onView(withId(R.id.edittext_filled_editable)).perform(setSingleLine(true));

    onView(withId(R.id.edittext_filled_editable)).perform(requestFocus());
    onView(withId(R.id.edittext_filled_editable)).perform(pressKey(KeyEvent.KEYCODE_ENTER));
    onView(withId(R.id.filled_editable_dropdown)).perform(skipAnimations());

    assertThat(editText.isPopupShowing(), is(true));
  }

  @Test
  public void testOnKeyDown_enterOnEditableSingleLineField_hidesDropDown() {
    final Activity activity = activityTestRule.getActivity();
    final AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled_editable);

    onView(withId(R.id.edittext_filled_editable)).perform(setSingleLine(true));

    onView(withId(R.id.filled_editable_dropdown)).perform(click());
    onView(withId(R.id.filled_editable_dropdown)).perform(skipAnimations());

    onView(withId(R.id.edittext_filled_editable)).perform(requestFocus());
    onView(withId(R.id.edittext_filled_editable)).perform(pressKey(KeyEvent.KEYCODE_ENTER));
    onView(withId(R.id.filled_editable_dropdown)).perform(skipAnimations());

    assertThat(editText.isPopupShowing(), is(false));
  }

  @Test
  public void testOnKeyDown_spaceOnEditableField_doesNotShowDropDown() {
    final Activity activity = activityTestRule.getActivity();
    final AutoCompleteTextView editText = activity.findViewById(R.id.edittext_filled_editable);

    onView(withId(R.id.edittext_filled_editable)).perform(requestFocus());
    onView(withId(R.id.edittext_filled_editable)).perform(pressKey(KeyEvent.KEYCODE_SPACE));
    onView(withId(R.id.filled_editable_dropdown)).perform(skipAnimations());

    assertThat(editText.isPopupShowing(), is(false));
  }

  @Test
  public void shouldShowPopup_nonEditable_isTrueForEnterOrSpace() {
    final Activity activity = activityTestRule.getActivity();
    final MaterialAutoCompleteTextView nonEditable = activity.findViewById(R.id.edittext_filled);

    assertThat(nonEditable.shouldShowPopup(KeyEvent.KEYCODE_ENTER), is(true));
    assertThat(nonEditable.shouldShowPopup(KeyEvent.KEYCODE_DPAD_CENTER), is(true));
    assertThat(nonEditable.shouldShowPopup(KeyEvent.KEYCODE_SPACE), is(true));
    assertThat(nonEditable.shouldShowPopup(KeyEvent.KEYCODE_A), is(false));
  }

  @Test
  public void shouldShowPopup_editableMultiLine_isFalse() {
    final Activity activity = activityTestRule.getActivity();
    final MaterialAutoCompleteTextView editable =
        activity.findViewById(R.id.edittext_filled_editable);

    onView(withId(R.id.edittext_filled_editable)).perform(setSingleLine(false));

    assertThat(editable.shouldShowPopup(KeyEvent.KEYCODE_ENTER), is(false));
    assertThat(editable.shouldShowPopup(KeyEvent.KEYCODE_DPAD_CENTER), is(false));
    assertThat(editable.shouldShowPopup(KeyEvent.KEYCODE_SPACE), is(false));
  }

  @Test
  public void shouldShowPopup_editableSingleLine_isTrueForEnter() {
    final Activity activity = activityTestRule.getActivity();
    final MaterialAutoCompleteTextView editable =
        activity.findViewById(R.id.edittext_filled_editable);

    onView(withId(R.id.edittext_filled_editable)).perform(setSingleLine(true));

    assertThat(editable.shouldShowPopup(KeyEvent.KEYCODE_ENTER), is(true));
    assertThat(editable.shouldShowPopup(KeyEvent.KEYCODE_DPAD_CENTER), is(true));
    assertThat(editable.shouldShowPopup(KeyEvent.KEYCODE_SPACE), is(false));
  }
}
