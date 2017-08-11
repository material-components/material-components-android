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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.support.design.testapp.BottomSheetDialogActivity;
import android.support.design.testapp.R;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.FrameLayout;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import java.util.concurrent.atomic.AtomicBoolean;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class BottomSheetDialogTest {

  @Rule
  public final ActivityTestRule<BottomSheetDialogActivity> activityTestRule =
      new ActivityTestRule<>(BottomSheetDialogActivity.class);

  private BottomSheetDialog mDialog;

  @Test
  public void testBasicDialogSetup() throws Throwable {
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            showDialog();
            // Confirms that the dialog is shown
            assertThat(mDialog.isShowing(), is(true));
            FrameLayout bottomSheet = mDialog.findViewById(R.id.design_bottom_sheet);
            assertThat(bottomSheet, is(notNullValue()));
            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
            assertThat(behavior.isHideable(), is(true));
            assertThat(behavior, is(notNullValue()));
            // Modal bottom sheets have auto peek height by default.
            assertThat(behavior.getPeekHeight(), is(BottomSheetBehavior.PEEK_HEIGHT_AUTO));
          }
        });
    // Click outside the bottom sheet
    Espresso.onView(ViewMatchers.withId(R.id.touch_outside)).perform(ViewActions.click());
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            // Confirm that the dialog is no longer shown
            assertThat(mDialog.isShowing(), is(false));
          }
        });
  }

  @Test
  public void testTouchInside() throws Throwable {
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            showDialog();
            // Confirms that the dialog is shown
            assertThat(mDialog.isShowing(), is(true));
            FrameLayout bottomSheet = mDialog.findViewById(R.id.design_bottom_sheet);
            // The bottom sheet is not clickable
            assertNotNull(bottomSheet);
            assertThat(bottomSheet.isClickable(), is(false));
          }
        });
    // Click on the bottom sheet
    Espresso.onView(ViewMatchers.withId(R.id.design_bottom_sheet)).perform(ViewActions.click());
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            // Confirm that touch didn't fall through as outside touch
            assertThat(mDialog.isShowing(), is(true));
          }
        });
  }

  @Test
  public void testClickContent() throws Throwable {
    final View.OnClickListener mockListener = mock(View.OnClickListener.class);
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            showDialog();
            // Confirms that the dialog is shown
            assertThat(mDialog.isShowing(), is(true));
            FrameLayout bottomSheet = mDialog.findViewById(R.id.design_bottom_sheet);
            // Set up an OnClickListener to the content of the bottom sheet
            assertNotNull(bottomSheet);
            View child = bottomSheet.getChildAt(0);
            child.setOnClickListener(mockListener);
          }
        });
    // Click on the bottom sheet; since the whole sheet is occupied with its only child, this
    // clicks the child
    Espresso.onView(ViewMatchers.withParent(ViewMatchers.withId(R.id.design_bottom_sheet)))
        .perform(ViewActions.click());
    verify(mockListener, times(1)).onClick(any(View.class));
  }

  @Test
  public void testShortDialog() throws Throwable {
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            showDialog();
          }
        });
    // This ensures that the views are laid out before assertions below
    Espresso.onView(ViewMatchers.withId(R.id.design_bottom_sheet))
        .perform(setTallPeekHeight())
        .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            FrameLayout bottomSheet = mDialog.findViewById(R.id.design_bottom_sheet);
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
        });
  }

  @Test
  public void testNonCancelableDialog() throws Throwable {
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            showDialog();
            mDialog.setCancelable(false);
          }
        });
    // Click outside the bottom sheet
    Espresso.onView(ViewMatchers.withId(R.id.touch_outside)).perform(ViewActions.click());
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            FrameLayout bottomSheet = mDialog.findViewById(R.id.design_bottom_sheet);
            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
            assertThat(behavior.isHideable(), is(false));
            assertThat(mDialog.isShowing(), is(true));
            mDialog.cancel();
            assertThat(mDialog.isShowing(), is(false));
          }
        });
  }

  @Test
  public void testHideBottomSheet() throws Throwable {
    final AtomicBoolean canceled = new AtomicBoolean(false);
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            showDialog();
            mDialog.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                  @Override
                  public void onCancel(DialogInterface dialogInterface) {
                    canceled.set(true);
                  }
                });
          }
        });
    Espresso.onView(ViewMatchers.withId(R.id.design_bottom_sheet))
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
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            showDialog();
            mDialog.setOnCancelListener(onCancelListener);
          }
        });
    Espresso.onView(ViewMatchers.withId(R.id.design_bottom_sheet))
        .perform(setState(BottomSheetBehavior.STATE_HIDDEN));
    verify(onCancelListener, timeout(3000)).onCancel(any(DialogInterface.class));
    // Reshow the same dialog instance and wait for the bottom sheet to be collapsed.
    final BottomSheetBehavior.BottomSheetCallback callback =
        mock(BottomSheetBehavior.BottomSheetCallback.class);
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            BottomSheetBehavior.from(mDialog.findViewById(R.id.design_bottom_sheet))
                .setBottomSheetCallback(callback);
            mDialog.show(); // Show the same dialog again.
          }
        });
    verify(callback, timeout(3000))
        .onStateChanged(any(View.class), eq(BottomSheetBehavior.STATE_SETTLING));
    verify(callback, timeout(3000))
        .onStateChanged(any(View.class), eq(BottomSheetBehavior.STATE_COLLAPSED));
  }

  private void showDialog() {
    Context context = activityTestRule.getActivity();
    mDialog = new BottomSheetDialog(context);
    AppCompatTextView text = new AppCompatTextView(context);
    StringBuilder builder = new StringBuilder();
    builder.append("It is fine today. ");
    text.setText(builder);
    mDialog.setContentView(text);
    mDialog.show();
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
        BottomSheetBehavior behavior = BottomSheetBehavior.from(view);
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
