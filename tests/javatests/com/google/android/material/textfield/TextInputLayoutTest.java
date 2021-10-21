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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.android.material.testutils.TestUtilsActions.setEnabled;
import static com.google.android.material.testutils.TestUtilsMatchers.withTextColor;
import static com.google.android.material.testutils.TestUtilsMatchers.withTypeface;
import static com.google.android.material.testutils.TextInputLayoutActions.setBoxBackgroundColor;
import static com.google.android.material.testutils.TextInputLayoutActions.setBoxCornerRadii;
import static com.google.android.material.testutils.TextInputLayoutActions.setBoxStrokeColor;
import static com.google.android.material.testutils.TextInputLayoutActions.setBoxStrokeErrorColor;
import static com.google.android.material.testutils.TextInputLayoutActions.setBoxStrokeWidth;
import static com.google.android.material.testutils.TextInputLayoutActions.setBoxStrokeWidthFocused;
import static com.google.android.material.testutils.TextInputLayoutActions.setCounterEnabled;
import static com.google.android.material.testutils.TextInputLayoutActions.setCounterMaxLength;
import static com.google.android.material.testutils.TextInputLayoutActions.setError;
import static com.google.android.material.testutils.TextInputLayoutActions.setErrorContentDescription;
import static com.google.android.material.testutils.TextInputLayoutActions.setErrorEnabled;
import static com.google.android.material.testutils.TextInputLayoutActions.setErrorTextAppearance;
import static com.google.android.material.testutils.TextInputLayoutActions.setExpandedHintEnabled;
import static com.google.android.material.testutils.TextInputLayoutActions.setHelperText;
import static com.google.android.material.testutils.TextInputLayoutActions.setHelperTextEnabled;
import static com.google.android.material.testutils.TextInputLayoutActions.setHint;
import static com.google.android.material.testutils.TextInputLayoutActions.setHintTextAppearance;
import static com.google.android.material.testutils.TextInputLayoutActions.setPlaceholderText;
import static com.google.android.material.testutils.TextInputLayoutActions.setTypeface;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import androidx.core.widget.TextViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.test.annotation.UiThreadTest;
import androidx.test.espresso.ViewAssertion;
import androidx.test.filters.MediumTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.R;
import com.google.android.material.testapp.TextInputLayoutActivity;
import com.google.android.material.testutils.TestUtils;
import com.google.android.material.testutils.ViewStructureImpl;
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
  private static final String PLACEHOLDER_TEXT = "This text is holding the place";
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
  public void testHintIsCollapsedIfExpandedHintNotEnabled() {
    // Set expandedHintEnabled to false.
    onView(withId(R.id.textinput_box_filled)).perform(setExpandedHintEnabled(false));

    // Check the hint is collapsed.
    onView(withId(R.id.textinput_box_filled)).check(isHintExpanded(false));
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
  public void testSetPlaceholderText() {
    onView(withId(R.id.textinput_box_outline)).perform(setPlaceholderText(PLACEHOLDER_TEXT));

    // Click on the EditText so that the hint collapses and the placeholder text is shown.
    onView(withId(R.id.textinput_edittext_outline)).perform(click());

    // Check that the placeholder text is displayed.
    onView(withText(PLACEHOLDER_TEXT)).check(matches(isDisplayed()));
  }

  @Test
  public void testSetPlaceholderTextViaAttribute() {
    // The text input with id textinput_box_outline has the string textinput_placeholder set on it
    // via the placeholder text attribute. Check that the placeholder text is displayed via the
    // attribute.
    String placeholderText =
        activityTestRule.getActivity().getResources().getString(R.string.textinput_placeholder);

    // Click on the EditText so that the hint collapses and the placeholder text is shown.
    onView(withId(R.id.textinput_edittext_outline)).perform(click());

    // Check that the placeholder text is displayed.
    onView(withText(placeholderText)).check(matches(isDisplayed()));
  }

  @Test
  public void testSetErrorNullDoesNotHideHelperText() {
    // Show helper text in layout with error enabled
    onView(withId(R.id.textinput)).perform(setHelperText(HELPER_MESSAGE_1));

    // Set error to null
    onView(withId(R.id.textinput)).perform(setError(null));

    // Check helper text is still visible
    onView(withText(HELPER_MESSAGE_1)).check(matches(isDisplayed()));
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

    assertEquals(INPUT_TEXT, info.hintText.toString());
  }

  @UiThreadTest
  @Test
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
  public void testDispatchProvideAutofillStructure() {
    final Activity activity = activityTestRule.getActivity();

    final TextInputLayout layout = activity.findViewById(R.id.textinput_layout_without_hint);

    final ViewStructureImpl structure = new ViewStructureImpl();
    layout.dispatchProvideAutofillStructure(structure, 0);

    // 1 x EditText, 3 x TextView (prefix/suffix/placeholder).
    assertEquals(4, structure.getChildCount());

    // Asserts the structure.
    ViewStructureImpl childStructure = structure.getChildAt(1);
    assertEquals(EditText.class.getName(), childStructure.getClassName());
    assertEquals("Nested hint", childStructure.getHint().toString());

    // Make sure the widget's hint was restored.
    assertEquals("Nested hint", layout.getHint().toString());
    final EditText editText = activity.findViewById(R.id.textinput_edittext_with_hint);
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
  public void testSavedState() throws Throwable {
    final Activity activity = activityTestRule.getActivity();
    final TextInputLayout layout = (TextInputLayout) activity.findViewById(R.id.textinput);
    layout.setHint(HINT_TEXT);
    layout.setHelperText(HELPER_MESSAGE_1);
    layout.setPlaceholderText(PLACEHOLDER_TEXT);

    // Save the current state
    SparseArray<Parcelable> container = new SparseArray<>();
    layout.saveHierarchyState(container);

    // Restore the state into a fresh text field
    activityTestRule.runOnUiThread(
        () -> {
          final TextInputLayout testLayout = new TestTextInputLayout(activity);
          testLayout.setId(R.id.textinput);
          final TextInputEditText testEditText = new TextInputEditText(activity);
          testEditText.setId(R.id.textinputedittext);
          testLayout.addView(testEditText);
          testLayout.restoreHierarchyState(container);

          // Check hint, helper and placeholder text were restored
          assertEquals(HINT_TEXT, testLayout.getHint().toString());
          assertEquals(HELPER_MESSAGE_1, testLayout.getHelperText().toString());
          assertEquals(PLACEHOLDER_TEXT, testLayout.getPlaceholderText().toString());
        });
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
  public void testSetErrorContentDescription() {
    String errorContentDesc = "Error content description";
    // Set error and error content description.
    onView(withId(R.id.textinput))
        .perform(setErrorEnabled(true))
        .perform(setError(ERROR_MESSAGE_1))
        .perform(setErrorContentDescription(errorContentDesc));

    final Activity activity = activityTestRule.getActivity();
    final TextInputLayout textInputLayout =
        activity.findViewById(R.id.textinput);

    // Assert the error content description is as expected.
    assertEquals(errorContentDesc, textInputLayout.getErrorContentDescription().toString());
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
  public void testTextSetViaAttributeCollapsedHint() {
    onView(withId(R.id.textinput_with_text)).check(isHintExpanded(false));
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
  public void testSetBoxStrokeWidth() {
    final Activity activity = activityTestRule.getActivity();
    final TextInputLayout layout = activity.findViewById(R.id.textinput_box_outline);

    // Set stroke width
    onView(withId(R.id.textinput_box_outline)).perform(setBoxStrokeWidth(10));

    // Assert stroke width is correct
    assertEquals(10, layout.getBoxStrokeWidth());
  }

  @Test
  public void testSetBoxStrokeFocusedWidth() {
    final Activity activity = activityTestRule.getActivity();
    final TextInputLayout layout = activity.findViewById(R.id.textinput_box_outline);

    // Set stroke width
    onView(withId(R.id.textinput_box_outline)).perform(setBoxStrokeWidthFocused(10));

    // Assert stroke width is correct
    assertEquals(10, layout.getBoxStrokeWidthFocused());
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
  public void testOutlineBoxStrokeChangesErrorColor() {
    ColorStateList cyan = new ColorStateList(new int[][] {new int[] {}}, new int[] {Color.CYAN});
    ColorStateList green = new ColorStateList(new int[][] {new int[] {}}, new int[] {Color.GREEN});
    onView(withId(R.id.textinput_box_outline)).perform(setError(ERROR_MESSAGE_1));

    // Change the outline box's stroke error color to cyan.
    onView(withId(R.id.textinput_box_outline)).perform(setBoxStrokeErrorColor(cyan));
    // Check that the outline box's stroke error color is cyan.
    onView(withId(R.id.textinput_box_outline)).check(isBoxStrokeErrorColor(cyan));
    // Change the outline box's stroke error color to green.
    onView(withId(R.id.textinput_box_outline)).perform(setBoxStrokeErrorColor(green));
    // Check that the outline box's stroke error color is green.
    onView(withId(R.id.textinput_box_outline)).check(isBoxStrokeErrorColor(green));
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

  @UiThreadTest
  @Test
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
  public void hintSetOnNestedEditText_propagateInnerHintToAutofillProvider() {
    final Activity activity = activityTestRule.getActivity();
    TextInputLayout textInputLayout = activity.findViewById(R.id.textinput_layout_without_hint);
    ViewStructureImpl structure = new ViewStructureImpl();

    textInputLayout.dispatchProvideAutofillStructure(structure, /* flags= */ 0);

    final ViewStructureImpl editText = structure.getChildAt(1);
    assertEquals(EditText.class.getName(), editText.getClassName());
    assertEquals(structure.getAutofillId(), textInputLayout.getAutofillId());
    assertEquals("Nested hint", editText.getHint().toString());
  }

  @UiThreadTest
  @Test
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
  public void hintSetOnOuterLayout_propagateOuterHintToAutofillProvider() {
    final Activity activity = activityTestRule.getActivity();
    TextInputLayout textInputLayout = activity.findViewById(R.id.textinput_layout_with_hint);
    ViewStructureImpl structure = new ViewStructureImpl();

    textInputLayout.dispatchProvideAutofillStructure(structure, /* flags= */ 0);

    final ViewStructureImpl editText = structure.getChildAt(1);
    assertEquals(EditText.class.getName(), editText.getClassName());
    assertEquals(structure.getAutofillId(), textInputLayout.getAutofillId());
    assertEquals("Outer hint", editText.getHint().toString());
  }

  private static ViewAssertion isHintExpanded(final boolean expanded) {
    return (view, noViewFoundException) -> {
      assertTrue(view instanceof TextInputLayout);
      assertEquals(expanded, ((TextInputLayout) view).isHintExpanded());
    };
  }

  private static ViewAssertion isBoxStrokeColor(@ColorInt final int boxStrokeColor) {
    return (view, noViewFoundException) -> {
      assertTrue(view instanceof TextInputLayout);
      assertEquals(boxStrokeColor, ((TextInputLayout) view).getBoxStrokeColor());
    };
  }

  private static ViewAssertion isBoxStrokeErrorColor(final ColorStateList boxStrokeColor) {
    return (view, noViewFoundException) -> {
      assertTrue(view instanceof TextInputLayout);
      assertEquals(boxStrokeColor, ((TextInputLayout) view).getBoxStrokeErrorColor());
    };
  }

  private static ViewAssertion isBoxBackgroundColor(@ColorInt final int boxBackgroundColor) {
    return (view, noViewFoundException) -> {
      assertTrue(view instanceof TextInputLayout);
      assertEquals(boxBackgroundColor, ((TextInputLayout) view).getBoxBackgroundColor());
    };
  }

  private static ViewAssertion isBoxCornerRadiusTopEnd(final float boxCornerRadiusTopEnd) {
    return (view, noViewFoundException) -> {
      assertTrue(view instanceof TextInputLayout);
      assertEquals(
          boxCornerRadiusTopEnd, ((TextInputLayout) view).getBoxCornerRadiusTopEnd(), 0.01);
    };
  }

  private static ViewAssertion isCutoutOpen(final boolean open) {
    return (view, noViewFoundException) -> {
      assertTrue(view instanceof TextInputLayout);
      assertEquals(open, ((TextInputLayout) view).cutoutIsOpen());
    };
  }
}
