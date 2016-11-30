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

import android.content.res.Resources;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.test.R;
import android.support.design.testutils.SnackbarUtils;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.v4.view.ViewCompat;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.TextUtils;
import android.view.View;
import org.junit.Before;
import org.junit.Test;

import static android.support.design.testutils.TestUtilsActions.setLayoutDirection;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SnackbarTest extends BaseInstrumentationTestCase<SnackbarActivity> {
    private static final String MESSAGE_TEXT = "Test Message";
    private static final @StringRes int MESSAGE_ID = R.string.snackbar_text;
    private static final String ACTION_TEXT = "Action";
    private static final @StringRes int ACTION_ID = R.string.snackbar_action;

    private CoordinatorLayout mCoordinatorLayout;

    private interface DismissAction {
        void dismiss(Snackbar snackbar);
    }

    public SnackbarTest() {
        super(SnackbarActivity.class);
    }

    @Before
    public void setup() {
        mCoordinatorLayout =
                (CoordinatorLayout) mActivityTestRule.getActivity().findViewById(R.id.col);
    }

    private void verifySnackbarContent(final Snackbar snackbar, final String expectedMessage,
            final String expectedAction) {
        // Show the snackbar
        SnackbarUtils.showSnackbarAndWaitUntilFullyShown(snackbar);

        // Verify that we're showing the message
        withText(expectedMessage).matches(allOf(
                isDescendantOfA(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                isCompletelyDisplayed()));

        // If the action is not empty, verify that we're showing it
        if (!TextUtils.isEmpty(expectedAction)) {
            withText(expectedAction).matches(allOf(
                    isDescendantOfA(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                    isCompletelyDisplayed()));
        }

        // Dismiss the snackbar
        SnackbarUtils.dismissSnackbarAndWaitUntilFullyDismissed(snackbar);
    }

    @Test
    @SmallTest
    public void testBasicContent() {
        // Verify different combinations of snackbar content (message and action) and duration

        final Resources res = mActivityTestRule.getActivity().getResources();
        final String resolvedMessage = res.getString(MESSAGE_ID);
        final String resolvedAction = res.getString(ACTION_ID);

        // String message and no action
        verifySnackbarContent(
                Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_SHORT),
                MESSAGE_TEXT, null);

        // Resource message and no action
        verifySnackbarContent(
                Snackbar.make(mCoordinatorLayout, MESSAGE_ID, Snackbar.LENGTH_LONG),
                resolvedMessage, null);

        // String message and string action
        verifySnackbarContent(
                Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_INDEFINITE)
                        .setAction(ACTION_TEXT, mock(View.OnClickListener.class)),
                MESSAGE_TEXT, ACTION_TEXT);

        // String message and resource action
        verifySnackbarContent(
                Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_SHORT)
                        .setAction(ACTION_ID, mock(View.OnClickListener.class)),
                MESSAGE_TEXT, resolvedAction);

        // Resource message and resource action
        verifySnackbarContent(
                Snackbar.make(mCoordinatorLayout, MESSAGE_ID, Snackbar.LENGTH_LONG)
                        .setAction(ACTION_ID, mock(View.OnClickListener.class)),
                resolvedMessage, resolvedAction);
    }

    private void verifyDismissCallback(final ViewInteraction interaction,
            final @Nullable ViewAction action, final @Nullable DismissAction dismissAction,
            final int length, @Snackbar.Callback.DismissEvent final int expectedEvent) {
        final Snackbar.Callback mockCallback = mock(Snackbar.Callback.class);
        final Snackbar snackbar = Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, length)
                .setAction(ACTION_TEXT, mock(View.OnClickListener.class))
                .setCallback(mockCallback);

        // Note that unlike other tests around Snackbar that use Espresso's IdlingResources
        // to wait until the snackbar is shown (SnackbarUtils.showSnackbarAndWaitUntilFullyShown),
        // here we want to verify our callback has been called with onShown after snackbar is shown
        // and with onDismissed after snackbar is dismissed.

        // Now show the Snackbar
        snackbar.show();
        // sleep for the animation
        SystemClock.sleep(Snackbar.ANIMATION_DURATION + 50);

        // Now perform the UI interaction
        if (action != null) {
            interaction.perform(action);
        } else if (dismissAction != null) {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
                @Override
                public void run() {
                    dismissAction.dismiss(snackbar);
                }
            });
        }
        // wait until the Snackbar has been removed from the view hierarchy
        while (snackbar.isShownOrQueued()) {
            SystemClock.sleep(20);
        }
        // and verify that our callback was invoked with onShown and onDismissed
        verify(mockCallback, times(1)).onShown(snackbar);
        verify(mockCallback, times(1)).onDismissed(snackbar, expectedEvent);
        verifyNoMoreInteractions(mockCallback);
    }

    @Test
    @MediumTest
    public void testDismissViaActionClick() {
        verifyDismissCallback(
                onView(withId(R.id.snackbar_action)),
                click(),
                null,
                Snackbar.LENGTH_LONG,
                Snackbar.Callback.DISMISS_EVENT_ACTION);
    }

    @Test
    @MediumTest
    public void testDismissViaSwipe() {
        verifyDismissCallback(
                onView(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                swipeRight(),
                null,
                Snackbar.LENGTH_LONG,
                Snackbar.Callback.DISMISS_EVENT_SWIPE);
    }

    @Test
    @MediumTest
    public void testDismissViaSwipeRtl() {
        onView(withId(R.id.col)).perform(setLayoutDirection(ViewCompat.LAYOUT_DIRECTION_RTL));
        if (ViewCompat.getLayoutDirection(mCoordinatorLayout) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            // On devices that support RTL layout, the start-to-end dismiss swipe is done
            // with swipeLeft() action
            verifyDismissCallback(
                    onView(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                    swipeLeft(),
                    null,
                    Snackbar.LENGTH_LONG,
                    Snackbar.Callback.DISMISS_EVENT_SWIPE);
        }
    }

    @Test
    @MediumTest
    public void testDismissViaApi() {
        verifyDismissCallback(
                onView(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                null,
                new DismissAction() {
                    @Override
                    public void dismiss(Snackbar snackbar) {
                        snackbar.dismiss();
                    }
                },
                Snackbar.LENGTH_LONG,
                Snackbar.Callback.DISMISS_EVENT_MANUAL);
    }

    @Test
    @MediumTest
    public void testDismissViaTimeout() {
        verifyDismissCallback(
                onView(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                null,
                null,
                Snackbar.LENGTH_LONG,
                Snackbar.Callback.DISMISS_EVENT_TIMEOUT);
    }

    @Test
    @MediumTest
    public void testDismissViaAnotherSnackbar() {
        final Snackbar anotherSnackbar =
                Snackbar.make(mCoordinatorLayout, "A different message", Snackbar.LENGTH_SHORT);

        // Our dismiss action is to show another snackbar (and verify that the original snackbar
        // is now dismissed with CONSECUTIVE event)
        verifyDismissCallback(
                onView(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                null,
                new DismissAction() {
                    @Override
                    public void dismiss(Snackbar snackbar) {
                        anotherSnackbar.show();
                    }
                },
                Snackbar.LENGTH_LONG,
                Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE);

        // And dismiss the second snackbar to get back to clean state
        SnackbarUtils.dismissSnackbarAndWaitUntilFullyDismissed(anotherSnackbar);
    }

    @Test
    @MediumTest
    public void testActionClickListener() {
        final View.OnClickListener mockClickListener = mock(View.OnClickListener.class);
        final Snackbar snackbar =
                Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_SHORT)
                    .setAction(ACTION_TEXT, mockClickListener);

        // Show the snackbar
        SnackbarUtils.showSnackbarAndWaitUntilFullyShown(snackbar);
        // perform the action click
        onView(withId(R.id.snackbar_action)).perform(click());
        // and verify that our click listener has been called
        verify(mockClickListener, times(1)).onClick(any(View.class));
    }
}
