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

package com.google.android.material.bottomsheet;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.content.DialogInterface;
import android.os.SystemClock;
import androidx.appcompat.widget.AppCompatTextView;
import android.view.View;
import android.widget.FrameLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.BottomSheetDialogActivity;
import com.google.android.material.testapp.R;
import java.util.concurrent.atomic.AtomicBoolean;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class BottomSheetDialogTest {

  @Rule
  public final ActivityTestRule<BottomSheetDialogActivity> activityTestRule =
      new ActivityTestRule<>(BottomSheetDialogActivity.class);

  private BottomSheetDialog dialog;

  @After
  public void tearDown() {
    if (dialog != null && dialog.isShowing()) {
      // Close the dialog
      Espresso.pressBack();
    }
  }

  @Test
  public void testBasicDialogSetup() throws Throwable {
    showDialog();
    // Confirms that the dialog is shown
    assertThat(dialog.isShowing(), is(true));
    FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
    assertThat(bottomSheet, is(notNullValue()));
    BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
    assertThat(behavior.isHideable(), is(true));
    assertThat(behavior, is(notNullValue()));
    // Modal bottom sheets have auto peek height by default.
    assertThat(behavior.getPeekHeight(), is(BottomSheetBehavior.PEEK_HEIGHT_AUTO));

    // Click outside the bottom sheet
    onView(ViewMatchers.withId(R.id.touch_outside)).perform(click());
    // Confirm that the dialog is no longer shown
    assertThat(dialog.isShowing(), is(false));
  }

  @Test
  public void testTouchInside() throws Throwable {
    showDialog();
    // Confirms that the dialog is shown
    assertThat(dialog.isShowing(), is(true));
    FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
    // The bottom sheet is not clickable
    assertNotNull(bottomSheet);
    assertThat(bottomSheet.isClickable(), is(false));
    // Click on the bottom sheet
    onView(ViewMatchers.withId(R.id.design_bottom_sheet)).perform(click());
    // Confirm that touch didn't fall through as outside touch
    assertThat(dialog.isShowing(), is(true));
  }

  @Test
  public void testClickContent() throws Throwable {
    final View.OnClickListener mockListener = mock(View.OnClickListener.class);
    showDialog();
    // Confirms that the dialog is shown
    assertThat(dialog.isShowing(), is(true));
    FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
    // Set up an OnClickListener to the content of the bottom sheet
    assertNotNull(bottomSheet);
    View child = bottomSheet.getChildAt(0);
    child.setOnClickListener(mockListener);
    // Click on the bottom sheet; since the whole sheet is occupied with its only child, this
    // clicks the child
    onView(ViewMatchers.withParent(ViewMatchers.withId(R.id.design_bottom_sheet)))
        .perform(click());
    verify(mockListener, times(1)).onClick(any(View.class));
  }

  @Test
  public void testShortDialog() throws Throwable {
    showDialog();
    // This ensures that the views are laid out before assertions below
    onView(ViewMatchers.withId(R.id.design_bottom_sheet))
        .perform(setTallPeekHeight())
        .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
    CoordinatorLayout coordinator = (CoordinatorLayout) bottomSheet.getParent();
    BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
    assertThat(bottomSheet, is(notNullValue()));
    assertThat(coordinator, is(notNullValue()));
    assertThat(behavior, is(notNullValue()));
    // This bottom sheet is shorter than the peek height
    assertThat(bottomSheet.getHeight(), is(lessThan(behavior.getPeekHeight())));
    // Confirm that the bottom sheet is bottom-aligned
    assertThat(bottomSheet.getTop(), is(coordinator.getHeight() - bottomSheet.getHeight()));
  }

  @Test
  public void testNonCancelableDialog() throws Throwable {
    showDialog();
    dialog.setCancelable(false);
    // Click outside the bottom sheet
    onView(ViewMatchers.withId(R.id.touch_outside)).perform(click());
    FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
    BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
    assertThat(behavior.isHideable(), is(false));
    assertThat(dialog.isShowing(), is(true));

    activityTestRule.runOnUiThread(
        () -> {
          dialog.cancel();
          assertThat(dialog.isShowing(), is(false));
        });
  }

  @Test
  public void testHideBottomSheet() throws Throwable {
    final AtomicBoolean canceled = new AtomicBoolean(false);
    showDialog();
    dialog.setOnCancelListener(dialogInterface -> canceled.set(true));
    onView(ViewMatchers.withId(R.id.design_bottom_sheet))
        .perform(setState(BottomSheetBehavior.STATE_HIDDEN));
    // The dialog should be canceled
    long start = System.currentTimeMillis();
    while (!canceled.get()) {
      SystemClock.sleep(31);
      if (System.currentTimeMillis() - start > 3000) {
        fail("Timed out while waiting for the dialog to be canceled.");
      }
    }
  }

  @Test
  @MediumTest
  public void testHideThenShow() throws Throwable {
    // Hide the bottom sheet and wait for the dialog to be canceled.
    final DialogInterface.OnCancelListener onCancelListener =
        mock(DialogInterface.OnCancelListener.class);
    showDialog();
    dialog.setOnCancelListener(onCancelListener);
    onView(ViewMatchers.withId(R.id.design_bottom_sheet))
        .perform(setState(BottomSheetBehavior.STATE_HIDDEN));
    verify(onCancelListener, timeout(3000)).onCancel(any(DialogInterface.class));
    // Reshow the same dialog instance and wait for the bottom sheet to be collapsed.
    final BottomSheetBehavior.BottomSheetCallback callback =
        mock(BottomSheetBehavior.BottomSheetCallback.class);
    BottomSheetBehavior.from(dialog.findViewById(R.id.design_bottom_sheet))
        .addBottomSheetCallback(callback);
    // Show the same dialog again.
    activityTestRule.runOnUiThread(() -> dialog.show());
    verify(callback, timeout(3000))
        .onStateChanged(any(View.class), eq(BottomSheetBehavior.STATE_SETTLING));
    verify(callback, timeout(3000))
        .onStateChanged(any(View.class), eq(BottomSheetBehavior.STATE_COLLAPSED));
  }

  private void showDialog() throws Throwable {
    activityTestRule.runOnUiThread(
        () -> {
          Context context = activityTestRule.getActivity();
          dialog = new BottomSheetDialog(context);
          AppCompatTextView text = new AppCompatTextView(context);
          StringBuilder builder = new StringBuilder();
          builder.append("It is fine today. ");
          text.setText(builder);
          dialog.setContentView(text);
          dialog.show();
        });
  }

  private static ViewAction setTallPeekHeight() {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isDisplayed();
      }

      @Override
      public String getDescription() {
        return "set tall peek height";
      }

      @Override
      public void perform(UiController uiController, View view) {
        BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(view);
        behavior.setPeekHeight(view.getHeight() + 100);
      }
    };
  }

  private static ViewAction setState(@BottomSheetBehavior.State final int state) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isBottomSheet();
      }

      @Override
      public String getDescription() {
        return "set state to " + state;
      }

      @Override
      public void perform(UiController uiController, View view) {
        BottomSheetBehavior.from(view).setState(state);
      }
    };
  }

  private static Matcher<View> isBottomSheet() {
    return new TypeSafeMatcher<View>() {
      @Override
      protected boolean matchesSafely(View view) {
        return BottomSheetBehavior.from(view) != null;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("is a bottom sheet");
      }
    };
  }
}
