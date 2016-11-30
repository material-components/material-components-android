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
import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SnackbarTest extends BaseInstrumentationTestCase<SnackbarActivity> {
    private static final String MESSAGE_TEXT = "Test Message";
    private static final String ACTION_TEXT = "Action";

    private CoordinatorLayout mCoordinatorLayout;

    public SnackbarTest() {
        super(SnackbarActivity.class);
    }

    @Before
    public void setup() {
        mCoordinatorLayout =
                (CoordinatorLayout) mActivityTestRule.getActivity().findViewById(R.id.col);
    }

    private void verifyDismissCallback(final ViewInteraction interaction, final ViewAction action,
            final int length, @Snackbar.Callback.DismissEvent final int expectedEvent) {
        final Snackbar.Callback mockCallback = mock(Snackbar.Callback.class);
        final Snackbar snackbar = Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, length)
                .setAction(ACTION_TEXT, mock(View.OnClickListener.class))
                .setCallback(mockCallback);

        // Now show the Snackbar
        snackbar.show();
        // Sleep for the animation
        SystemClock.sleep(Snackbar.ANIMATION_DURATION + 50);
        // ...and perform the UI interaction
        interaction.perform(action);
        // Now wait until the Snackbar has been removed from the view hierarchy
        while (snackbar.isShownOrQueued()) {
            SystemClock.sleep(20);
        }

        verify(mockCallback, times(1)).onShown(snackbar);
        verify(mockCallback, times(1)).onDismissed(snackbar, expectedEvent);
        verifyNoMoreInteractions(mockCallback);
    }

    @Test
    @MediumTest
    public void testActionClickDismiss() {
        verifyDismissCallback(
                onView(withId(R.id.snackbar_action)),
                ViewActions.click(),
                Snackbar.LENGTH_LONG,
                Snackbar.Callback.DISMISS_EVENT_ACTION);
    }

    @Test
    @MediumTest
    public void testSwipeDismissCallback() {
        verifyDismissCallback(
                onView(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                ViewActions.swipeRight(),
                Snackbar.LENGTH_LONG,
                Snackbar.Callback.DISMISS_EVENT_SWIPE);
    }

    @Test
    @MediumTest
    public void testActionClickListener() {
        final View.OnClickListener mockClickListener = mock(View.OnClickListener.class);
        final Snackbar snackbar =
                Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_SHORT)
                    .setAction(ACTION_TEXT, mockClickListener);
        // Now show the Snackbar
        snackbar.show();
        // Sleep for the animation
        SystemClock.sleep(Snackbar.ANIMATION_DURATION + 50);
        // Perform the action click
        onView(withId(R.id.snackbar_action)).perform(ViewActions.click());

        verify(mockClickListener, times(1)).onClick(any(View.class));
    }
}
