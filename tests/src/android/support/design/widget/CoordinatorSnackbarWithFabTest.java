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

import android.os.Build;
import android.support.design.custom.TestFloatingBehavior;
import android.support.design.test.R;
import android.support.design.testutils.SnackbarUtils;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.v7.widget.AppCompatTextView;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.View;
import android.view.ViewGroup;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Test;

import static android.support.design.widget.DesignViewActions.setVisibility;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@MediumTest
public class CoordinatorSnackbarWithFabTest extends BaseDynamicCoordinatorLayoutTest {
    private static final String MESSAGE_TEXT = "Test Message";
    private static final String ACTION_TEXT = "Action";

    private Snackbar mSnackbar;

    @After
    public void teardown() {
        // Dismiss the snackbar to get back to clean state for the next test
        if (mSnackbar != null) {
            SnackbarUtils.dismissSnackbarAndWaitUntilFullyDismissed(mSnackbar);
        }
    }

    /**
     * Returns the location of our snackbar on the screen.
     */
    private static int[] getSnackbarLocationOnScreen() {
        final int[] location = new int[2];
        onView(isAssignableFrom(Snackbar.SnackbarLayout.class)).perform(new ViewAction() {
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
     * Helper method that verifies that the passed view is above the snackbar in the activity
     * window.
     */
    private static void verifySnackbarViewStacking(View view, int extraBottomMargin) {
        if (Build.VERSION.SDK_INT >= 11) {
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
    }

    @Test
    public void testBuiltInSliding() {
        onView(withId(R.id.coordinator_stub)).perform(
                inflateViewStub(R.layout.design_snackbar_with_fab));

        // Create and show a snackbar
        mSnackbar = Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_INDEFINITE)
                .setAction(ACTION_TEXT, mock(View.OnClickListener.class));
        SnackbarUtils.showSnackbarAndWaitUntilFullyShown(mSnackbar);

        // Take into account bottom padding and bottom margin to account for how drop shadow is
        // emulated on pre-Lollipop devices
        final FloatingActionButton fab =
                (FloatingActionButton) mCoordinatorLayout.findViewById(R.id.fab);
        verifySnackbarViewStacking(fab, fab.getPaddingBottom()
                - ((ViewGroup.MarginLayoutParams) fab.getLayoutParams()).bottomMargin);
    }

    @Test
    public void testBuiltInSlidingFromHiddenFab() {
        onView(withId(R.id.coordinator_stub)).perform(
                inflateViewStub(R.layout.design_snackbar_with_fab));
        onView(withId(R.id.fab)).perform(setVisibility(View.GONE));

        // Create and show a snackbar
        mSnackbar = Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_INDEFINITE)
                .setAction(ACTION_TEXT, mock(View.OnClickListener.class));
        SnackbarUtils.showSnackbarAndWaitUntilFullyShown(mSnackbar);

        // Take into account bottom padding and bottom margin to account for how drop shadow is
        // emulated on pre-Lollipop devices
        onView(withId(R.id.fab)).perform(setVisibility(View.VISIBLE));
        final FloatingActionButton fab =
                (FloatingActionButton) mCoordinatorLayout.findViewById(R.id.fab);
        verifySnackbarViewStacking(fab, fab.getPaddingBottom()
                - ((ViewGroup.MarginLayoutParams) fab.getLayoutParams()).bottomMargin);
    }

    @Test
    public void testBehaviorBasedSlidingFromLayoutAttribute() {
        // Use a layout in which an AppCompatTextView child has Behavior object configured via
        // layout_behavior XML attribute
        onView(withId(R.id.coordinator_stub)).perform(
                inflateViewStub(R.layout.design_snackbar_behavior_layout_attr));

        // Create and show a snackbar
        mSnackbar = Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_INDEFINITE)
                .setAction(ACTION_TEXT, mock(View.OnClickListener.class));
        SnackbarUtils.showSnackbarAndWaitUntilFullyShown(mSnackbar);

        final AppCompatTextView textView =
                (AppCompatTextView) mCoordinatorLayout.findViewById(R.id.text);
        verifySnackbarViewStacking(textView, 0);
    }

    @Test
    public void testBehaviorBasedSlidingFromClassAnnotation() {
        // Use a layout in which a custom child view has Behavior object configured via
        // annotation on the class that extends AppCompatTextView
        onView(withId(R.id.coordinator_stub)).perform(
                inflateViewStub(R.layout.design_snackbar_behavior_annotation));

        // Create and show a snackbar
        mSnackbar = Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_INDEFINITE)
                .setAction(ACTION_TEXT, mock(View.OnClickListener.class));
        SnackbarUtils.showSnackbarAndWaitUntilFullyShown(mSnackbar);

        final AppCompatTextView textView =
                (AppCompatTextView) mCoordinatorLayout.findViewById(R.id.text);
        verifySnackbarViewStacking(textView, 0);
    }

    @Test
    public void testBehaviorBasedSlidingFromRuntimeApiCall() {
        // Use a layout in which an AppCompatTextView child doesn't have any configured Behavior
        onView(withId(R.id.coordinator_stub)).perform(
                inflateViewStub(R.layout.design_snackbar_behavior_runtime));

        // and configure that Behavior at runtime by setting it on its LayoutParams
        final AppCompatTextView textView =
                (AppCompatTextView) mCoordinatorLayout.findViewById(R.id.text);
        final CoordinatorLayout.LayoutParams textViewLp =
                (CoordinatorLayout.LayoutParams) textView.getLayoutParams();
        textViewLp.setBehavior(new TestFloatingBehavior());

        // Create and show a snackbar
        mSnackbar = Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_INDEFINITE)
                .setAction(ACTION_TEXT, mock(View.OnClickListener.class));
        SnackbarUtils.showSnackbarAndWaitUntilFullyShown(mSnackbar);

        verifySnackbarViewStacking(textView, 0);
    }
}
