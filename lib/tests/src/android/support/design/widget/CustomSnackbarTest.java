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

import static android.support.design.testutils.TestUtilsActions.setLayoutDirection;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.content.res.Resources;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.test.R;
import android.support.design.testutils.SnackbarUtils;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.MediumTest;
import android.support.v4.view.ViewCompat;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.LayoutInflater;

import org.junit.Before;
import org.junit.Test;

public class CustomSnackbarTest extends BaseInstrumentationTestCase<SnackbarActivity> {
    private static final String TITLE_TEXT = "Test title";
    private static final String SUBTITLE_TEXT = "Test subtitle";

    private CoordinatorLayout mCoordinatorLayout;

    private interface DismissAction {
        void dismiss(CustomSnackbar snackbar);
    }

    public CustomSnackbarTest() {
        super(SnackbarActivity.class);
    }

    @Before
    public void setup() {
        mCoordinatorLayout =
                (CoordinatorLayout) mActivityTestRule.getActivity().findViewById(R.id.col);
    }

    private void verifySnackbarContent(final CustomSnackbar snackbar, final String expectedTitle,
            final String expectedSubtitle) {
        // Show the snackbar
        SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown(snackbar);

        // Verify that we're showing the title
        withText(expectedTitle).matches(allOf(
                isDescendantOfA(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                isDescendantOfA(isAssignableFrom(CustomSnackbarMainContent.class)),
                isCompletelyDisplayed()));

        // Verify that we're showing the subtitle
        withText(expectedSubtitle).matches(allOf(
                isDescendantOfA(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                isDescendantOfA(isAssignableFrom(CustomSnackbarMainContent.class)),
                isCompletelyDisplayed()));

        // Dismiss the snackbar
        SnackbarUtils.dismissTransientBottomBarAndWaitUntilFullyDismissed(snackbar);
    }

    private CustomSnackbar makeCustomSnackbar() {
        final LayoutInflater inflater = LayoutInflater.from(mCoordinatorLayout.getContext());
        final CustomSnackbarMainContent content =
                (CustomSnackbarMainContent) inflater.inflate(
                        R.layout.custom_snackbar_include, mCoordinatorLayout, false);
        final BaseTransientBottomBar.ContentViewCallback contentViewCallback =
                new BaseTransientBottomBar.ContentViewCallback() {
                    @Override
                    public void animateContentIn(int delay, int duration) {
                        ViewCompat.setAlpha(content, 0f);
                        ViewCompat.animate(content).alpha(1f).setDuration(duration)
                                .setStartDelay(delay).start();
                    }

                    @Override
                    public void animateContentOut(int delay, int duration) {
                        ViewCompat.setAlpha(content, 1f);
                        ViewCompat.animate(content).alpha(0f).setDuration(duration)
                                .setStartDelay(delay).start();
                    }
                };
        return new CustomSnackbar(mCoordinatorLayout, content, contentViewCallback);
    }

    @Test
    @SmallTest
    public void testBasicContent() {
        // Verify different combinations of snackbar content (title / subtitle and action)
        // and duration

        final Resources res = mActivityTestRule.getActivity().getResources();

        // Short duration
        verifySnackbarContent(
                makeCustomSnackbar().setTitle(TITLE_TEXT)
                        .setSubtitle(SUBTITLE_TEXT).setDuration(Snackbar.LENGTH_SHORT),
                TITLE_TEXT, SUBTITLE_TEXT);

        // Long duration
        verifySnackbarContent(
                makeCustomSnackbar().setTitle(TITLE_TEXT)
                        .setSubtitle(SUBTITLE_TEXT).setDuration(Snackbar.LENGTH_LONG),
                TITLE_TEXT, SUBTITLE_TEXT);

        // Indefinite duration
        verifySnackbarContent(
                makeCustomSnackbar().setTitle(TITLE_TEXT)
                        .setSubtitle(SUBTITLE_TEXT).setDuration(Snackbar.LENGTH_INDEFINITE),
                TITLE_TEXT, SUBTITLE_TEXT);
    }

    private void verifyDismissCallback(final ViewInteraction interaction,
            @Nullable final ViewAction action, @Nullable final DismissAction dismissAction,
            final int length, @Snackbar.Callback.DismissEvent final int expectedEvent)
            throws Throwable {
        final BaseTransientBottomBar.BaseCallback mockCallback =
                mock(BaseTransientBottomBar.BaseCallback.class);
        final CustomSnackbar snackbar = makeCustomSnackbar().setTitle(TITLE_TEXT)
                .setSubtitle(SUBTITLE_TEXT).setDuration(length)
                .setCallback(mockCallback);

        // Note that unlike other tests around Snackbar that use Espresso's IdlingResources
        // to wait until the snackbar is shown
        // (SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown),
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
            mActivityTestRule.runOnUiThread(new Runnable() {
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
    public void testDismissViaSwipe() throws Throwable {
        verifyDismissCallback(
                onView(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                swipeRight(),
                null,
                Snackbar.LENGTH_LONG,
                Snackbar.Callback.DISMISS_EVENT_SWIPE);
    }

    @Test
    @MediumTest
    public void testDismissViaSwipeRtl() throws Throwable {
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
    public void testDismissViaApi() throws Throwable {
        verifyDismissCallback(
                onView(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                null,
                new DismissAction() {
                    @Override
                    public void dismiss(CustomSnackbar snackbar) {
                        snackbar.dismiss();
                    }
                },
                Snackbar.LENGTH_LONG,
                Snackbar.Callback.DISMISS_EVENT_MANUAL);
    }

    @Test
    @MediumTest
    public void testDismissViaTimeout() throws Throwable {
        verifyDismissCallback(
                onView(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                null,
                null,
                Snackbar.LENGTH_LONG,
                Snackbar.Callback.DISMISS_EVENT_TIMEOUT);
    }

    @Test
    @MediumTest
    public void testDismissViaAnotherSnackbar() throws Throwable {
        final CustomSnackbar anotherSnackbar =
                makeCustomSnackbar().setTitle("Different title")
                        .setSubtitle("Different subtitle").setDuration(Snackbar.LENGTH_SHORT);

        // Our dismiss action is to show another snackbar (and verify that the original snackbar
        // is now dismissed with CONSECUTIVE event)
        verifyDismissCallback(
                onView(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                null,
                new DismissAction() {
                    @Override
                    public void dismiss(CustomSnackbar snackbar) {
                        anotherSnackbar.show();
                    }
                },
                Snackbar.LENGTH_LONG,
                Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE);

        // And dismiss the second snackbar to get back to clean state
        SnackbarUtils.dismissTransientBottomBarAndWaitUntilFullyDismissed(anotherSnackbar);
    }
}
