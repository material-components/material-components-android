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

package com.google.android.material.snackbar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.google.android.material.testutils.DesignViewActions.setVisibility;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import androidx.appcompat.widget.AppCompatTextView;
import android.view.View;
import android.view.ViewGroup;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.filters.MediumTest;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.internal.BaseDynamicCoordinatorLayoutTest;
import com.google.android.material.testapp.R;
import com.google.android.material.testapp.custom.TestFloatingBehavior;
import com.google.android.material.testutils.SnackbarUtils;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class CoordinatorSnackbarWithFabTest extends BaseDynamicCoordinatorLayoutTest {
  private static final String MESSAGE_TEXT = "Test Message";
  private static final String ACTION_TEXT = "Action";

  private Snackbar snackbar;

  @After
  public void teardown() throws Throwable {
    // Dismiss the snackbar to get back to clean state for the next test
    if (snackbar != null) {
      SnackbarUtils.dismissTransientBottomBarAndWaitUntilFullyDismissed(snackbar);
    }
  }

  /** Returns the location of our snackbar on the screen. */
  private static int[] getSnackbarLocationOnScreen() {
    final int[] location = new int[2];
    onView(isAssignableFrom(Snackbar.SnackbarLayout.class))
        .perform(
            new ViewAction() {
              @Override
              public Matcher<View> getConstraints() {
                return isEnabled();
              }

              @Override
              public String getDescription() {
                return "Snackbar matcher";
              }

              @Override
              public void perform(UiController uiController, View view) {
                view.getLocationOnScreen(location);
              }
            });
    return location;
  }

  /**
   * Helper method that verifies that the passed view is above the snackbar in the activity window.
   */
  private static void verifySnackbarViewStacking(View view, int extraBottomMargin) {
    // Get location of snackbar in window
    final int[] snackbarOnScreenXY = getSnackbarLocationOnScreen();
    // Get location of passed view in window
    final int[] viewOnScreenXY = new int[2];
    view.getLocationOnScreen(viewOnScreenXY);

    // Compute the bottom visible edge of the view
    int viewBottom = viewOnScreenXY[1] + view.getHeight() - extraBottomMargin;
    int snackbarTop = snackbarOnScreenXY[1];
    // and verify that our view is above the snackbar
    assertTrue(viewBottom <= snackbarTop);
  }

  @Test
  public void testBuiltInSliding() {
    onView(withId(R.id.coordinator_stub))
        .perform(inflateViewStub(R.layout.design_snackbar_with_fab));

    // Create and show a snackbar
    snackbar =
        Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_INDEFINITE)
            .setAction(ACTION_TEXT, mock(View.OnClickListener.class));
    SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown(snackbar);

    // Take into account bottom padding and bottom margin to account for how drop shadow is
    // emulated on pre-Lollipop devices
    final View fab = mCoordinatorLayout.findViewById(R.id.fab);
    verifySnackbarViewStacking(
        fab,
        fab.getPaddingBottom()
            - ((ViewGroup.MarginLayoutParams) fab.getLayoutParams()).bottomMargin);
  }

  @Test
  public void testBuiltInSlidingFromHiddenFab() {
    onView(withId(R.id.coordinator_stub))
        .perform(inflateViewStub(R.layout.design_snackbar_with_fab));
    onView(withId(R.id.fab)).perform(setVisibility(View.GONE));

    // Create and show a snackbar
    snackbar =
        Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_INDEFINITE)
            .setAction(ACTION_TEXT, mock(View.OnClickListener.class));
    SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown(snackbar);

    // Take into account bottom padding and bottom margin to account for how drop shadow is
    // emulated on pre-Lollipop devices
    onView(withId(R.id.fab)).perform(setVisibility(View.VISIBLE));
    final View fab = mCoordinatorLayout.findViewById(R.id.fab);
    verifySnackbarViewStacking(
        fab,
        fab.getPaddingBottom()
            - ((ViewGroup.MarginLayoutParams) fab.getLayoutParams()).bottomMargin);
  }

  @Test
  public void testBehaviorBasedSlidingFromLayoutAttribute() {
    // Use a layout in which an AppCompatTextView child has Behavior object configured via
    // layout_behavior XML attribute
    onView(withId(R.id.coordinator_stub))
        .perform(inflateViewStub(R.layout.design_snackbar_behavior_layout_attr));

    // Create and show a snackbar
    snackbar =
        Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_INDEFINITE)
            .setAction(ACTION_TEXT, mock(View.OnClickListener.class));
    SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown(snackbar);

    final AppCompatTextView textView = mCoordinatorLayout.findViewById(R.id.text);
    verifySnackbarViewStacking(textView, 0);
  }

  @Test
  public void testBehaviorBasedSlidingFromClassAnnotation() {
    // Use a layout in which a custom child view has Behavior object configured via
    // annotation on the class that extends AppCompatTextView
    onView(withId(R.id.coordinator_stub))
        .perform(inflateViewStub(R.layout.design_snackbar_behavior_annotation));

    // Create and show a snackbar
    snackbar =
        Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_INDEFINITE)
            .setAction(ACTION_TEXT, mock(View.OnClickListener.class));
    SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown(snackbar);

    final AppCompatTextView textView = mCoordinatorLayout.findViewById(R.id.text);
    verifySnackbarViewStacking(textView, 0);
  }

  @Test
  public void testBehaviorBasedSlidingFromRuntimeApiCall() {
    // Use a layout in which an AppCompatTextView child doesn't have any configured Behavior
    onView(withId(R.id.coordinator_stub))
        .perform(inflateViewStub(R.layout.design_snackbar_behavior_runtime));

    // and configure that Behavior at runtime by setting it on its LayoutParams
    final AppCompatTextView textView = mCoordinatorLayout.findViewById(R.id.text);
    final CoordinatorLayout.LayoutParams textViewLp =
        (CoordinatorLayout.LayoutParams) textView.getLayoutParams();
    textViewLp.setBehavior(new TestFloatingBehavior());

    // Create and show a snackbar
    snackbar =
        Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_INDEFINITE)
            .setAction(ACTION_TEXT, mock(View.OnClickListener.class));
    SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown(snackbar);

    verifySnackbarViewStacking(textView, 0);
  }
}
