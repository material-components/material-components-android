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

import android.os.SystemClock;
import android.support.design.test.R;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.View;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SnackbarBucketTests extends BaseInstrumentationTestCase<SnackbarBucketTestsActivity> {

    private static final String MESSAGE_TEXT = "Test Message";
    private static final String ACTION_TEXT = "Action";

    public SnackbarBucketTests() {
        super(SnackbarBucketTestsActivity.class);
    }

    @Test
    @MediumTest
    public void testActionClickDismiss() {
        testDismissCallback(
                onView(withId(R.id.snackbar_action)),
                ViewActions.click(),
                Snackbar.LENGTH_LONG,
                Snackbar.Callback.DISMISS_EVENT_ACTION);
    }

    @Test
    @MediumTest
    public void testSwipeDismissCallback() {
        testDismissCallback(
                onView(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                ViewActions.swipeRight(),
                Snackbar.LENGTH_LONG,
                Snackbar.Callback.DISMISS_EVENT_SWIPE);
    }

    @Test
    @MediumTest
    public void testActionClickListener() {
        final AtomicBoolean clicked = new AtomicBoolean();

        final Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), MESSAGE_TEXT,
                Snackbar.LENGTH_SHORT)
                .setAction(ACTION_TEXT, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clicked.set(true);
                    }
                });
        // Now show the Snackbar
        snackbar.show();
        // Sleep for the animation
        SystemClock.sleep(Snackbar.ANIMATION_DURATION + 50);
        // Perform the action click
        onView(withId(R.id.snackbar_action)).perform(ViewActions.click());

        assertTrue("Action click listener called", clicked.get());
    }

    private void testDismissCallback(final ViewInteraction interaction, final ViewAction action,
            final int length, @Snackbar.Callback.DismissEvent final int expectedEvent) {
        final ArrayList<Integer> dismissEvents = new ArrayList<>();

        final Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), MESSAGE_TEXT, length)
                .setAction(ACTION_TEXT, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // no-op
                    }
                })
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, @DismissEvent int event) {
                        dismissEvents.add(event);
                    }
                });

        // Now show the Snackbar
        snackbar.show();
        // Sleep for the animation
        SystemClock.sleep(Snackbar.ANIMATION_DURATION + 50);
        // ...and the perform the UI interaction
        interaction.perform(action);
        // Now wait until the Snackbar has been removed from the view hierarchy
        while (snackbar.isShownOrQueued()) {
            SystemClock.sleep(20);
        }

        assertTrue("onDismissed has not been called once only. Events: " + dismissEvents,
                dismissEvents.size() == 1);
        assertEquals(expectedEvent, (int) dismissEvents.get(0));
    }

    private CoordinatorLayout getCoordinatorLayout() {
        return mActivityTestRule.getActivity().mCoordinatorLayout;
    }
}
