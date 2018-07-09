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

package com.google.android.material.textfield;

import static com.google.android.material.testutils.TestUtilsActions.setCompoundDrawablesRelative;
import static com.google.android.material.testutils.TestUtilsActions.setEnabled;
import static com.google.android.material.testutils.TestUtilsMatchers.withCompoundDrawable;
import static com.google.android.material.testutils.TestUtilsMatchers.withTextColor;
import static com.google.android.material.testutils.TestUtilsMatchers.withTypeface;
import static com.google.android.material.testutils.TextInputLayoutActions.clickPasswordToggle;
import static com.google.android.material.testutils.TextInputLayoutActions.setBoxBackgroundColor;
import static com.google.android.material.testutils.TextInputLayoutActions.setBoxCornerRadii;
import static com.google.android.material.testutils.TextInputLayoutActions.setBoxStrokeColor;
import static com.google.android.material.testutils.TextInputLayoutActions.setCounterEnabled;
import static com.google.android.material.testutils.TextInputLayoutActions.setCounterMaxLength;
import static com.google.android.material.testutils.TextInputLayoutActions.setError;
import static com.google.android.material.testutils.TextInputLayoutActions.setErrorEnabled;
import static com.google.android.material.testutils.TextInputLayoutActions.setErrorTextAppearance;
import static com.google.android.material.testutils.TextInputLayoutActions.setHelperText;
import static com.google.android.material.testutils.TextInputLayoutActions.setHelperTextEnabled;
import static com.google.android.material.testutils.TextInputLayoutActions.setHint;
import static com.google.android.material.testutils.TextInputLayoutActions.setHintTextAppearance;
import static com.google.android.material.testutils.TextInputLayoutActions.setPasswordVisibilityToggleEnabled;
import static com.google.android.material.testutils.TextInputLayoutActions.setTypeface;
import static com.google.android.material.testutils.TextInputLayoutMatchers.doesNotShowPasswordToggle;
import static com.google.android.material.testutils.TextInputLayoutMatchers.passwordToggleHasContentDescription;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.AccessibilityChecks.accessibilityAssertion;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import com.google.android.material.testapp.R;
import com.google.android.material.testapp.TextInputLayoutActivity;
import com.google.android.material.testapp.base.RecreatableAppCompatActivity;
import com.google.android.material.testutils.ActivityUtils;
import com.google.android.material.testutils.TestUtils;
import com.google.android.material.testutils.ViewStructureImpl;
import android.support.v4.widget.TextViewCompat;
import android.text.TextPaint;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.support.test.annotation.UiThreadTest;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.filters.LargeTest;
import android.support.test.filters.MediumTest;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class TextInputLayoutTest {
  @Rule
  public final ActivityTestRule<TextInputLayoutActivity> activityTestRule =
      new ActivityTestRule<>(TextInputLayoutActivity.class);

  private static final String ERROR_MESSAGE_1 = "An error has occurred";
  private static final String ERROR_MESSAGE_2 = "Some other error has occurred";
  private static final String HELPER_MESSAGE_1 = "Helpful helper text";
  private static final String HELPER_MESSAGE_2 = "Some other helper text";
  private static final String HINT_TEXT = "Hint text";
  private static final String INPUT_TEXT = "Random input text";
  private static final Typeface CUSTOM_TYPEFACE = Typeface.SANS_SERIF;

  private static class TestTextInputLayout extends TextInputLayout {
    public int animateToExpansionFractionCount = 0;
    public float animateToExpansionFractionRecentValue = -1;

    public TestTextInputLayout(Context context) {
      super(context);
    }

    public TestTextInputLayout(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    public TestTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
    }

    @Override
    protected void animateToExpansionFraction(float target) {
      super.animateToExpansionFraction(target);
      animateToExpansionFractionRecentValue = target;
      animateToExpansionFractionCount++;
    }
  }

  @Test
  public void testTypingTextCollapsesHint() {
    // Type some text
    onView(withId(R.id.textinput_edittext)).perform(typeText(INPUT_TEXT));
    // ...and check that the hint has collapsed
    onView(withId(R.id.textinput)).check(isHintExpanded(false));
  }

  @Test
  public void testNoBoxNoCutout() {
    // Type some text on the non-box text field.
    onView(withId(R.id.textinput_edittext)).perform(typeText(INPUT_TEXT));
    // Check that there is no cutout.
    onView(withId(R.id.textinput)).check(isCutoutOpen(false));
  }

  @Test
  public void testFilledBoxHintCollapseNoCutout() {
    // Type some text on the filled box text field.
    onView(withId(R.id.textinput_edittext_filled)).perform(typeText(INPUT_TEXT));
    // Check that there is no cutout.
    onView(withId(R.id.textinput_box_filled)).check(isCutoutOpen(false));
  }

  @Test
  public void testOutlineBoxNoHintNoCutout() {
    // Type some text on the outline box without a hint.
    onView(withId(R.id.textinput_edittext_outline_no_hint)).perform(typeText(INPUT_TEXT));
    // Check that there is no cutout.
    onView(withId(R.id.textinput_box_outline_no_hint)).check(isCutoutOpen(false));
  }

  @Test
  public void testOutlineBoxHintCollapseCreatesCutout() {
    // Type some text on the outline box that has a hint.
    onView(withId(R.id.textinput_edittext_outline)).perform(typeText(INPUT_TEXT));
    // Check that the cutout is open.
    onView(withId(R.id.textinput_box_outline)).check(isCutoutOpen(true));
  }

  @Test
  public void testOutlineBoxHintExpandHidesCutout() {
    // Type some text on the outline box that has a hint.
    onView(withId(R.id.textinput_edittext_outline)).perform(typeText(INPUT_TEXT));
    // Remove the text so that the hint will go away when the text box loses focus.
    onView(withId(R.id.textinput_edittext_outline)).perform(clearText());
    // Type some text on another text field to remove focus from the outline box.
    onView(withId(R.id.textinput_edittext_filled)).perform(typeText(INPUT_TEXT));
    // Check that the cutout is closed.
    onView(withId(R.id.textinput_box_outline)).check(isCutoutOpen(false));
  }

  @Test
  public void testSetErrorEnablesErrorIsDisplayed() {
    onView(withId(R.id.textinput)).perform(setError(ERROR_MESSAGE_1));
    onView(withText(ERROR_MESSAGE_1)).check(matches(isDisplayed()));
  }

  @Test
  public void testSetHelperEnablesHelperIsDisplayed() {
    onView(withId(R.id.textinput)).perform(setHelperText(HELPER_MESSAGE_1));
    onView(withText(HELPER_MESSAGE_1)).check(matches(isDisplayed()));
  }

  @Test
  public void testSetHelperTextViaAttribute() {
    // The text input with id textinput_box_outline has the string textinput_helper set on it via
    // the helper text attribute. Check that the helper text is displayed via the attribute.
    String helperText =
        activityTestRule.getActivity().getResources().getString(R.string.textinput_helper);

    onView(withText(helperText)).check(matches(isDisplayed()));
  }

  @Test
  public void testSetHelperTextViaAttributeHelperDisabled() {
    // The text input with id textinput_box_filled has the string
    // textinput_helper_not_enabled set on it via the helper text attribute, but helperTextEnabled
    // is not set to true. Check that the helper text is displayed via the attribute, even when
    // helperTextEnabled is not explicitly set to true.
    String helperTextNotEnabled =
        activityTestRule
            .getActivity()
            .getResources()
            .getString(R.string.textinput_helper_not_enabled);

    onView(withText(helperTextNotEnabled)).check(matches(isDisplayed()));
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
  public void testDisabledHelperIsNotDisplayed() {
    // First show a helper, and then disable helper functionality
    onView(withId(R.id.textinput))
        .perform(setHelperText(HELPER_MESSAGE_1))
        .perform(setHelperTextEnabled(false));

    // Check that the helper is no longer there
    onView(withText(HELPER_MESSAGE_1)).check(doesNotExist());
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
  public void testSetHelperOnDisabledSetHelperIsDisplayed() {
    // First show a helper, and then disable helper functionality
    onView(withId(R.id.textinput))
        .perform(setHelperText(HELPER_MESSAGE_1))
        .perform(setHelperTextEnabled(false));

    // Now show a different helper message
    onView(withId(R.id.textinput)).perform(setHelperText(HELPER_MESSAGE_2));
    // And check that it is displayed
    onView(withText(HELPER_MESSAGE_2)).check(matches(isDisplayed()));
  }

  @Test
  public void testPasswordToggleClick() {
    // Type some text on the EditText
    onView(withId(R.id.textinput_edittext_pwd)).perform(typeText(INPUT_TEXT));

    final Activity activity = activityTestRule.getActivity();
    final EditText textInput = activity.findViewById(R.id.textinput_edittext_pwd);

    // Assert that the password is disguised
    assertNotEquals(INPUT_TEXT, textInput.getLayout().getText().toString());

    // Now click the toggle button
    onView(withId(R.id.textinput_password)).perform(clickPasswordToggle());

    // And assert that the password is not disguised
    assertEquals(INPUT_TEXT, textInput.getLayout().getText().toString());
  }

  @Test
  public void testPasswordToggleDisable() {
    final Activity activity = activityTestRule.getActivity();
    final EditText textInput = activity.findViewById(R.id.textinput_edittext_pwd);

    // Set some text on the EditText
    onView(withId(R.id.textinput_edittext_pwd)).perform(typeText(INPUT_TEXT));
    // Assert that the password is disguised
    assertNotEquals(INPUT_TEXT, textInput.getLayout().getText().toString());

    // Disable the password toggle
    onView(withId(R.id.textinput_password)).perform(setPasswordVisibilityToggleEnabled(false));

    // Check that the password toggle view is not visible
    onView(withId(R.id.textinput_password)).check(matches(doesNotShowPasswordToggle()));
    // ...and that the password is disguised still
    assertNotEquals(INPUT_TEXT, textInput.getLayout().getText().toString());
  }

  @Test
  public void testPasswordToggleDisableWhenVisible() {
    final Activity activity = activityTestRule.getActivity();
    final EditText textInput = activity.findViewById(R.id.textinput_edittext_pwd);

    // Type some text on the EditText
    onView(withId(R.id.textinput_edittext_pwd)).perform(typeText(INPUT_TEXT));
    // Assert that the password is disguised
    assertNotEquals(INPUT_TEXT, textInput.getLayout().getText().toString());

    // Now click the toggle button
    onView(withId(R.id.textinput_password)).perform(clickPasswordToggle());
    // Disable the password toggle
    onView(withId(R.id.textinput_password)).perform(setPasswordVisibilityToggleEnabled(false));

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
    onView(withId(R.id.textinput_password)).perform(setPasswordVisibilityToggleEnabled(true));
    onView(withId(R.id.textinput_edittext_pwd))
        .check(matches(withCompoundDrawable(0, start)))
        .check(matches(withCompoundDrawable(1, top)))
        .check(matches(not(withCompoundDrawable(2, end))))
        .check(matches(withCompoundDrawable(3, bottom)));

    // Now disable the password toggle and check that all of the original compound drawables
    // are set
    onView(withId(R.id.textinput_password)).perform(setPasswordVisibilityToggleEnabled(false));
    onView(withId(R.id.textinput_edittext_pwd))
        .check(matches(withCompoundDrawable(0, start)))
        .check(matches(withCompoundDrawable(1, top)))
        .check(matches(withCompoundDrawable(2, end)))
        .check(matches(withCompoundDrawable(3, bottom)));
  }

  @Test
  public void testPasswordToggleIsHiddenAfterReenable() {
    final Activity activity = activityTestRule.getActivity();
    final EditText textInput = activity.findViewById(R.id.textinput_edittext_pwd);

    // Type some text on the EditText and then click the toggle button
    onView(withId(R.id.textinput_edittext_pwd)).perform(typeText(INPUT_TEXT));
    onView(withId(R.id.textinput_password)).perform(clickPasswordToggle());

    // Disable the password toggle, and then re-enable it
    onView(withId(R.id.textinput_password))
        .perform(setPasswordVisibilityToggleEnabled(false))
        .perform(setPasswordVisibilityToggleEnabled(true));

    // Check that the password is disguised and the toggle button reflects the same state
    assertNotEquals(INPUT_TEXT, textInput.getLayout().getText().toString());
    onView(withId(R.id.textinput_password)).perform(clickPasswordToggle());
  }

  @Test
  public void testSetEnabledFalse() {
    // First click on the EditText, so that it is focused and the hint collapses...
    onView(withId(R.id.textinput_edittext)).perform(click());

    // Now disable the TextInputLayout and check that the hint expands
    onView(withId(R.id.textinput)).perform(setEnabled(false)).check(isHintExpanded(true));

    // Finally check that the EditText is no longer enabled
    onView(withId(R.id.textinput_edittext)).check(matches(not(isEnabled())));
  }

  @Test
  public void testSetEnabledFalseWithText() {
    // First set some text, then disable the TextInputLayout
    onView(withId(R.id.textinput_edittext)).perform(typeText(INPUT_TEXT));
    onView(withId(R.id.textinput)).perform(setEnabled(false));

    // Now check that the EditText is no longer enabled
    onView(withId(R.id.textinput_edittext)).check(matches(not(isEnabled())));
  }

  @UiThreadTest
  @Test
  public void testExtractUiHintSet() {
    final Activity activity = activityTestRule.getActivity();

    // Set a hint on the TextInputLayout
    final TextInputLayout layout = activity.findViewById(R.id.textinput);
    layout.setHint(INPUT_TEXT);

    final EditText editText = activity.findViewById(R.id.textinput_edittext);

    // Now manually pass in a EditorInfo to the EditText and make sure it updates the
    // hintText to our known value
    final EditorInfo info = new EditorInfo();
    editText.onCreateInputConnection(info);

    assertEquals(INPUT_TEXT, info.hintText);
  }

  @UiThreadTest
  @Test
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
  public void testDispatchProvideAutofillStructure() {
    final Activity activity = activityTestRule.getActivity();

    final TextInputLayout layout = activity.findViewById(R.id.textinput);

    final ViewStructureImpl structure = new ViewStructureImpl();
    layout.dispatchProvideAutofillStructure(structure, 0);

    assertEquals(2, structure.getChildCount()); // EditText and TextView

    // Asserts the structure.
    final ViewStructureImpl childStructure = structure.getChildAt(0);
    assertEquals(EditText.class.getName(), childStructure.getClassName());
    assertEquals("Hint to the user", childStructure.getHint());

    // Make sure the widget's hint was restored.
    assertEquals("Hint to the user", layout.getHint());
    final EditText editText = activity.findViewById(R.id.textinput_edittext);
    assertNull(editText.getHint());
  }
  @UiThreadTest
  @Test
  public void testDrawableStateChanged() {
    final Activity activity = activityTestRule.getActivity();
    final TextInputLayout layout = (TextInputLayout) activity.findViewById(R.id.textinput);

    // Force a drawable state change.
    layout.drawableStateChanged();
  }

  @UiThreadTest
  @Test
  public void testSaveRestoreStateAnimation() {
    final Activity activity = activityTestRule.getActivity();
    final TestTextInputLayout layout = new TestTextInputLayout(activity);
    layout.setId(R.id.textinputlayout);
    final TextInputEditText editText = new TextInputEditText(activity);
    editText.setText(INPUT_TEXT);
    editText.setId(R.id.textinputedittext);
    layout.addView(editText);

    SparseArray<Parcelable> container = new SparseArray<>();
    layout.saveHierarchyState(container);
    layout.restoreHierarchyState(container);
    assertEquals(
        "Expected no animations since we simply saved/restored state",
        0,
        layout.animateToExpansionFractionCount);

    editText.setText("");
    assertEquals(
        "Expected one call to animate because we cleared text in editText",
        1,
        layout.animateToExpansionFractionCount);
    assertEquals(0f, layout.animateToExpansionFractionRecentValue, 0f);

    container = new SparseArray<>();
    layout.saveHierarchyState(container);
    layout.restoreHierarchyState(container);
    assertEquals(
        "Expected no additional animations since we simply saved/restored state",
        1,
        layout.animateToExpansionFractionCount);
  }

  @UiThreadTest
  @Test
  public void testMaintainsLeftRightCompoundDrawables() throws Throwable {
    final Activity activity = activityTestRule.getActivity();

    // Set a known set of test compound drawables on the EditText
    final Drawable left = new ColorDrawable(Color.RED);
    final Drawable top = new ColorDrawable(Color.GREEN);
    final Drawable right = new ColorDrawable(Color.BLUE);
    final Drawable bottom = new ColorDrawable(Color.BLACK);

    final TextInputEditText editText = new TextInputEditText(activity);
    editText.setCompoundDrawables(left, top, right, bottom);

    // Now add the EditText to a TextInputLayout
    TextInputLayout til = activity.findViewById(R.id.textinput_noedittext);
    til.addView(editText);

    // Finally assert that all of the drawables are untouched
    final Drawable[] compoundDrawables = editText.getCompoundDrawables();
    assertSame(left, compoundDrawables[0]);
    assertSame(top, compoundDrawables[1]);
    assertSame(right, compoundDrawables[2]);
    assertSame(bottom, compoundDrawables[3]);
  }

  @UiThreadTest
  @Test
  public void testMaintainsStartEndCompoundDrawables() throws Throwable {
    final Activity activity = activityTestRule.getActivity();

    // Set a known set of test compound drawables on the EditText
    final Drawable start = new ColorDrawable(Color.RED);
    final Drawable top = new ColorDrawable(Color.GREEN);
    final Drawable end = new ColorDrawable(Color.BLUE);
    final Drawable bottom = new ColorDrawable(Color.BLACK);

    final TextInputEditText editText = new TextInputEditText(activity);
    TextViewCompat.setCompoundDrawablesRelative(editText, start, top, end, bottom);

    // Now add the EditText to a TextInputLayout
    TextInputLayout til = activity.findViewById(R.id.textinput_noedittext);
    til.addView(editText);

    // Finally assert that all of the drawables are untouched
    final Drawable[] compoundDrawables = TextViewCompat.getCompoundDrawablesRelative(editText);
    assertSame(start, compoundDrawables[0]);
    assertSame(top, compoundDrawables[1]);
    assertSame(end, compoundDrawables[2]);
    assertSame(bottom, compoundDrawables[3]);
  }

  @Test
  public void testPasswordToggleHasDefaultContentDescription() {
    // Check that the TextInputLayout says that it has a content description and that the
    // underlying toggle has content description as well
    onView(withId(R.id.textinput_password)).check(matches(passwordToggleHasContentDescription()));
  }

  /**
   * Simple test that uses AccessibilityChecks to check that the password toggle icon is
   * 'accessible'.
   */
  @Test
  public void testPasswordToggleIsAccessible() {
    onView(
            allOf(
                withId(R.id.text_input_password_toggle),
                isDescendantOfA(withId(R.id.textinput_password))))
        .check(accessibilityAssertion());
  }

  @Test
  public void testSetTypefaceUpdatesErrorView() {
    onView(withId(R.id.textinput))
        .perform(setErrorEnabled(true))
        .perform(setError(ERROR_MESSAGE_1))
        .perform(setTypeface(CUSTOM_TYPEFACE));

    // Check that the error message is updated
    onView(withText(ERROR_MESSAGE_1)).check(matches(withTypeface(CUSTOM_TYPEFACE)));
  }

  @Test
  public void testSetTypefaceUpdatesCharacterCountView() {
    // Turn on character counting
    onView(withId(R.id.textinput))
        .perform(setCounterEnabled(true), setCounterMaxLength(10))
        .perform(setTypeface(CUSTOM_TYPEFACE));

    // Check that the counter message is updated
    onView(withId(R.id.textinput_counter)).check(matches(withTypeface(CUSTOM_TYPEFACE)));
  }

  @Test
  public void testThemedColorStateListForErrorTextColor() {
    final Activity activity = activityTestRule.getActivity();
    final int textColor = TestUtils.getThemeAttrColor(activity, R.attr.colorAccent);

    onView(withId(R.id.textinput))
        .perform(setErrorEnabled(true))
        .perform(setError(ERROR_MESSAGE_1))
        .perform(setErrorTextAppearance(R.style.TextAppearanceWithThemedCslTextColor));

    onView(withText(ERROR_MESSAGE_1)).check(matches(withTextColor(textColor)));
  }

  @Test
  public void testHintIsErrorTextColorOnError() {
    final Activity activity = activityTestRule.getActivity();
    final TextInputLayout layout = activity.findViewById(R.id.textinput);

    onView(withId(R.id.textinput))
        .perform(setErrorEnabled(true))
        .perform(setError(ERROR_MESSAGE_1));

    @ColorInt int hintColor = layout.getHintCurrentCollapsedTextColor();
    @ColorInt int errorColor = layout.getErrorTextCurrentColor();

    assertEquals(hintColor, errorColor);
  }

  @Test
  public void testFocusMovesToEditTextWithPasswordEnabled() {
    // Focus the preceding EditText
    onView(withId(R.id.textinput_edittext)).perform(click()).check(matches(hasFocus()));

    // Then send a TAB to focus the next view
    getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_TAB);

    // And check that the EditText is focused
    onView(withId(R.id.textinput_edittext_pwd)).check(matches(hasFocus()));
  }

  @Test
  public void testTextSetViaAttributeCollapsedHint() {
    onView(withId(R.id.textinput_with_text)).check(isHintExpanded(false));
  }

  @Test
  @LargeTest
  public void testSaveAndRestorePasswordVisibility() throws Throwable {
    // Type some text on the EditText
    onView(withId(R.id.textinput_edittext_pwd)).perform(typeText(INPUT_TEXT));
    onView(withId(R.id.textinput_password)).check(isPasswordToggledVisible(false));

    // Toggle password to be shown as plain text
    onView(withId(R.id.textinput_password)).perform(clickPasswordToggle());
    onView(withId(R.id.textinput_password)).check(isPasswordToggledVisible(true));

    RecreatableAppCompatActivity activity = activityTestRule.getActivity();
    ActivityUtils.recreateActivity(activityTestRule, activity);
    ActivityUtils.waitForExecution(activityTestRule);

    // Check that the password is still toggled to be shown as plain text
    onView(withId(R.id.textinput_password)).check(isPasswordToggledVisible(true));
  }

  @Test
  public void testHintCollapsedHeightMeasuredFromBaseline() {
    final Activity activity = activityTestRule.getActivity();
    final TextInputLayout layout = activity.findViewById(R.id.textinput_box_outline);

    // Create a TextView and set it to a custom text and TextAppearance.
    TextView textView = new TextView(layout.getContext());
    textView.setText(HINT_TEXT);
    TextViewCompat.setTextAppearance(textView, R.style.TextMediumGreenStyle);

    // Create a TextPaint to measure the text's height from the baseline, and port over aspects of
    // the TextAppearance from the textView.
    TextPaint textPaint = new TextPaint();
    textPaint.setSubpixelText(true);
    textPaint.setColor(textView.getCurrentTextColor());
    textPaint.setTypeface(textView.getTypeface());
    textPaint.setTextSize(textView.getTextSize());

    // Set the same custom text and text appearance on the outline box's hint.
    onView(withId(R.id.textinput_box_outline))
        .perform(setHint(HINT_TEXT))
        .perform(setHintTextAppearance(R.style.TextMediumGreenStyle));

    // Check that the hint's collapsed height is the same as the TextPaint's height, measured from
    // the baseline (-ascent).
    assertEquals(layout.getHintCollapsedTextHeight(), -textPaint.ascent(), 0.01);
  }

  @Test
  public void testOutlineBoxStrokeChangesColor() {
    @ColorInt int cyan = Color.CYAN;
    @ColorInt int green = Color.GREEN;

    // Change the outline box's stroke color to cyan.
    onView(withId(R.id.textinput_box_outline)).perform(setBoxStrokeColor(cyan));
    // Check that the outline box's stroke color is cyan.
    onView(withId(R.id.textinput_box_outline)).check(isBoxStrokeColor(cyan));
    // Change the outline box's stroke color to green.
    onView(withId(R.id.textinput_box_outline)).perform(setBoxStrokeColor(green));
    // Check that the outline box's stroke color is green.
    onView(withId(R.id.textinput_box_outline)).check(isBoxStrokeColor(green));
  }

  @Test
  public void testOutlineBoxBackgroundChangesColor() {
    @ColorInt int blue = Color.BLUE;
    @ColorInt int yellow = Color.YELLOW;

    // Change the outline box's background color to blue.
    onView(withId(R.id.textinput_box_outline)).perform(setBoxBackgroundColor(blue));
    // Check that the outline box's background color is blue.
    onView(withId(R.id.textinput_box_outline)).check(isBoxBackgroundColor(blue));
    // Change the outline box's background color to yellow.
    onView(withId(R.id.textinput_box_outline)).perform(setBoxBackgroundColor(yellow));
    // Check that the outline box's background color is yellow.
    onView(withId(R.id.textinput_box_outline)).check(isBoxBackgroundColor(yellow));
  }

  @Test
  public void testFilledBoxBackgroundChangesColor() {
    @ColorInt int blue = Color.BLUE;
    @ColorInt int yellow = Color.YELLOW;

    // Change the filled box's background color to blue.
    onView(withId(R.id.textinput_box_filled)).perform(setBoxBackgroundColor(blue));
    // Check that the filled box's background color is blue.
    onView(withId(R.id.textinput_box_filled)).check(isBoxBackgroundColor(blue));
    // Change the filled box's background color to yellow.
    onView(withId(R.id.textinput_box_filled)).perform(setBoxBackgroundColor(yellow));
    // Check that the filled box's background color is yellow.
    onView(withId(R.id.textinput_box_filled)).check(isBoxBackgroundColor(yellow));
  }

  @Test
  public void testBoxTopEndCornerRadiusChangesWithFloatValue() {
    float cornerRadiusSmall =
        activityTestRule.getActivity().getResources().getDimension(R.dimen.corner_radius_small);
    float cornerRadiusMedium =
        activityTestRule.getActivity().getResources().getDimension(R.dimen.corner_radius_medium);

    // Set the filled box's corner radii to cornerRadiusSmall.
    onView(withId(R.id.textinput_box_filled))
        .perform(
            setBoxCornerRadii(
                cornerRadiusSmall, cornerRadiusSmall, cornerRadiusSmall, cornerRadiusSmall));
    // Check that the outline box's top end corner radius is cornerRadiusSmall.
    onView(withId(R.id.textinput_box_filled)).check(isBoxCornerRadiusTopEnd(cornerRadiusSmall));

    // Set the filled box's corner radius to cornerRadiusMedium.
    onView(withId(R.id.textinput_box_filled))
        .perform(
            setBoxCornerRadii(
                cornerRadiusMedium, cornerRadiusMedium, cornerRadiusMedium, cornerRadiusMedium));
    // Check that the outline box's top end corner radius is cornerRadiusMedium.
    onView(withId(R.id.textinput_box_filled)).check(isBoxCornerRadiusTopEnd(cornerRadiusMedium));
  }

  @Test
  public void testBoxTopEndCornerRadiusChangesWithResource() {
    float cornerRadiusSmall =
        activityTestRule.getActivity().getResources().getDimension(R.dimen.corner_radius_small);
    float cornerRadiusMedium =
        activityTestRule.getActivity().getResources().getDimension(R.dimen.corner_radius_medium);

    // Set the outline box's corner radii to cornerRadiusSmall.
    onView(withId(R.id.textinput_box_outline))
        .perform(
            setBoxCornerRadii(
                R.dimen.corner_radius_small,
                R.dimen.corner_radius_small,
                R.dimen.corner_radius_small,
                R.dimen.corner_radius_small));

    // Check that the outline box's top end corner radius is cornerRadiusSmall.
    onView(withId(R.id.textinput_box_outline)).check(isBoxCornerRadiusTopEnd(cornerRadiusSmall));
    // Set the outline box's corner radii to corner_radius_medium.
    onView(withId(R.id.textinput_box_outline))
        .perform(
            setBoxCornerRadii(
                R.dimen.corner_radius_medium,
                R.dimen.corner_radius_medium,
                R.dimen.corner_radius_medium,
                R.dimen.corner_radius_medium));
    // Check that the outline box's top end corner radius is cornerRadiusMedium.
    onView(withId(R.id.textinput_box_outline)).check(isBoxCornerRadiusTopEnd(cornerRadiusMedium));
  }

  private static ViewAssertion isHintExpanded(final boolean expanded) {
    return new ViewAssertion() {
      @Override
      public void check(View view, NoMatchingViewException noViewFoundException) {
        assertTrue(view instanceof TextInputLayout);
        assertEquals(expanded, ((TextInputLayout) view).isHintExpanded());
      }
    };
  }

  private static ViewAssertion isBoxStrokeColor(@ColorInt final int boxStrokeColor) {
    return new ViewAssertion() {
      @Override
      public void check(View view, NoMatchingViewException noViewFoundException) {
        assertTrue(view instanceof TextInputLayout);
        assertEquals(boxStrokeColor, ((TextInputLayout) view).getBoxStrokeColor());
      }
    };
  }

  private static ViewAssertion isBoxBackgroundColor(@ColorInt final int boxBackgroundColor) {
    return new ViewAssertion() {
      @Override
      public void check(View view, NoMatchingViewException noViewFoundException) {
        assertTrue(view instanceof TextInputLayout);
        assertEquals(boxBackgroundColor, ((TextInputLayout) view).getBoxBackgroundColor());
      }
    };
  }

  private static ViewAssertion isBoxCornerRadiusTopEnd(final float boxCornerRadiusTopEnd) {
    return new ViewAssertion() {
      @Override
      public void check(View view, NoMatchingViewException noViewFoundException) {
        assertTrue(view instanceof TextInputLayout);
        assertEquals(
            boxCornerRadiusTopEnd, ((TextInputLayout) view).getBoxCornerRadiusTopEnd(), 0.01);
      }
    };
  }

  private static ViewAssertion isCutoutOpen(final boolean open) {
    return new ViewAssertion() {
      @Override
      public void check(View view, NoMatchingViewException noViewFoundException) {
        assertTrue(view instanceof TextInputLayout);
        assertEquals(open, ((TextInputLayout) view).cutoutIsOpen());
      }
    };
  }

  static ViewAssertion isPasswordToggledVisible(final boolean isToggledVisible) {
    return new ViewAssertion() {
      @Override
      public void check(View view, NoMatchingViewException noViewFoundException) {
        assertTrue(view instanceof TextInputLayout);
        EditText editText = ((TextInputLayout) view).getEditText();
        TransformationMethod transformationMethod = editText.getTransformationMethod();
        if (isToggledVisible) {
          assertNull(transformationMethod);
        } else {
          assertEquals(PasswordTransformationMethod.getInstance(), transformationMethod);
        }
      }
    };
  }
}
