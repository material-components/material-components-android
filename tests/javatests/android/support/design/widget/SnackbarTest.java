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
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.testapp.R;
import android.support.design.testapp.SnackbarActivity;
import android.support.design.testutils.SnackbarUtils;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.View;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class SnackbarTest {
  @Rule
  public final ActivityTestRule<SnackbarActivity> activityTestRule =
      new ActivityTestRule<>(SnackbarActivity.class);

  private static final String MESSAGE_TEXT = "Test Message";
  private static final @StringRes int MESSAGE_ID = R.string.snackbar_text;
  private static final String ACTION_TEXT = "Action";
  private static final @StringRes int ACTION_ID = R.string.snackbar_action;

  private CoordinatorLayout mCoordinatorLayout;

  private interface DismissAction {
    void dismiss(Snackbar snackbar);
  }

  @Before
  public void setup() {
    mCoordinatorLayout = activityTestRule.getActivity().findViewById(R.id.col);
  }

  private void verifySnackbarContent(
      final Snackbar snackbar, final String expectedMessage, final String expectedAction)
      throws Throwable {
    // Show the snackbar
    SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown(snackbar);

    // Verify that we're showing the message
    withText(expectedMessage)
        .matches(
            allOf(
                isDescendantOfA(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                isCompletelyDisplayed()));

    // If the action is not empty, verify that we're showing it
    if (!TextUtils.isEmpty(expectedAction)) {
      withText(expectedAction)
          .matches(
              allOf(
                  isDescendantOfA(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                  isCompletelyDisplayed()));
    }

    // Dismiss the snackbar
    SnackbarUtils.dismissTransientBottomBarAndWaitUntilFullyDismissed(snackbar);
  }

  @Test
  public void testBasicContent() throws Throwable {
    // Verify different combinations of snackbar content (message and action) and duration

    final Resources res = activityTestRule.getActivity().getResources();
    final String resolvedMessage = res.getString(MESSAGE_ID);
    final String resolvedAction = res.getString(ACTION_ID);

    // String message and no action
    verifySnackbarContent(
        Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_SHORT), MESSAGE_TEXT, null);

    // Resource message and no action
    verifySnackbarContent(
        Snackbar.make(mCoordinatorLayout, MESSAGE_ID, Snackbar.LENGTH_LONG), resolvedMessage, null);

    // String message and string action
    verifySnackbarContent(
        Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_INDEFINITE)
            .setAction(ACTION_TEXT, mock(View.OnClickListener.class)),
        MESSAGE_TEXT,
        ACTION_TEXT);

    // String message and resource action
    verifySnackbarContent(
        Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_SHORT)
            .setAction(ACTION_ID, mock(View.OnClickListener.class)),
        MESSAGE_TEXT,
        resolvedAction);

    // Resource message and resource action
    verifySnackbarContent(
        Snackbar.make(mCoordinatorLayout, MESSAGE_ID, Snackbar.LENGTH_LONG)
            .setAction(ACTION_ID, mock(View.OnClickListener.class)),
        resolvedMessage,
        resolvedAction);
  }

  private void verifyDismissCallback(
      final ViewInteraction interaction,
      final @Nullable ViewAction action,
      final @Nullable DismissAction dismissAction,
      final int length,
      @Snackbar.Callback.DismissEvent final int expectedEvent)
      throws Throwable {
    final BaseTransientBottomBar.BaseCallback mockCallback =
        mock(BaseTransientBottomBar.BaseCallback.class);
    final Snackbar snackbar =
        Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, length)
            .setAction(ACTION_TEXT, mock(View.OnClickListener.class))
            .addCallback(mockCallback);

    // Show the snackbar
    SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown(snackbar);
    // Verify that our onShown has been called
    verify(mockCallback, times(1)).onShown(snackbar);
    // and that the snackbar is either shown or queued to be shown
    assertTrue(snackbar.isShownOrQueued());
    // and also check that we have the intended message / action displayed somewhere in
    // our hierarchy
    onView(withText(MESSAGE_TEXT)).check(matches(isCompletelyDisplayed()));
    onView(withText(ACTION_TEXT)).check(matches(isCompletelyDisplayed()));

    // Now perform the UI interaction
    SnackbarUtils.performActionAndWaitUntilFullyDismissed(
        snackbar,
        new SnackbarUtils.TransientBottomBarAction() {
          @Override
          public void perform() throws Throwable {
            if (action != null) {
              interaction.perform(action);
            } else if (dismissAction != null) {
              activityTestRule.runOnUiThread(
                  new Runnable() {
                    @Override
                    public void run() {
                      dismissAction.dismiss(snackbar);
                    }
                  });
            }
          }
        });

    // Verify that our onDismissed has been called
    verify(mockCallback, times(1)).onDismissed(snackbar, expectedEvent);
    verifyNoMoreInteractions(mockCallback);
    // and that the snackbar is neither shown nor queued to be shown
    assertFalse(snackbar.isShownOrQueued());
  }

  @Test
  public void testDismissViaActionClick() throws Throwable {
    verifyDismissCallback(
        onView(withId(R.id.snackbar_action)),
        click(),
        null,
        Snackbar.LENGTH_LONG,
        Snackbar.Callback.DISMISS_EVENT_ACTION);
  }

  @Test
  public void testDismissViaSwipe() throws Throwable {
    verifyDismissCallback(
        onView(isAssignableFrom(Snackbar.SnackbarLayout.class)),
        swipeRight(),
        null,
        Snackbar.LENGTH_LONG,
        Snackbar.Callback.DISMISS_EVENT_SWIPE);
  }

  @Test
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
  public void testDismissViaApi() throws Throwable {
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
  public void testDismissViaTimeout() throws Throwable {
    verifyDismissCallback(
        onView(isAssignableFrom(Snackbar.SnackbarLayout.class)),
        null,
        null,
        Snackbar.LENGTH_LONG,
        Snackbar.Callback.DISMISS_EVENT_TIMEOUT);
  }

  @Test
  public void testSwipeUpDismissesViaTimeout() throws Throwable {
    verifyDismissCallback(
        onView(isAssignableFrom(Snackbar.SnackbarLayout.class)),
        // This is a swipe up, from the middle center of the view, to above the view
        // (outside the bounds)
        new GeneralSwipeAction(
            Swipe.SLOW,
            new CoordinatesProvider() {
              @Override
              public float[] calculateCoordinates(View view) {
                final int[] loc = new int[2];
                view.getLocationOnScreen(loc);
                return new float[] {loc[0] + view.getWidth() / 2, loc[1] + view.getHeight() / 2};
              }
            },
            new CoordinatesProvider() {
              @Override
              public float[] calculateCoordinates(View view) {
                final int[] loc = new int[2];
                view.getLocationOnScreen(loc);
                return new float[] {loc[0] + view.getWidth() / 2, loc[1] - view.getHeight()};
              }
            },
            Press.FINGER),
        null,
        Snackbar.LENGTH_SHORT,
        Snackbar.Callback.DISMISS_EVENT_TIMEOUT);
  }

  @Test
  public void testDismissViaAnotherSnackbar() throws Throwable {
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
    SnackbarUtils.dismissTransientBottomBarAndWaitUntilFullyDismissed(anotherSnackbar);
  }

  @Test
  public void testActionClickListener() {
    final View.OnClickListener mockClickListener = mock(View.OnClickListener.class);
    final Snackbar snackbar =
        Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_SHORT)
            .setAction(ACTION_TEXT, mockClickListener);

    // Show the snackbar
    SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown(snackbar);
    // perform the action click
    onView(withId(R.id.snackbar_action)).perform(click());
    // and verify that our click listener has been called
    verify(mockClickListener, times(1)).onClick(any(View.class));
  }

  @Test
  public void testSetCallback() throws Throwable {
    final Snackbar snackbar =
        Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_INDEFINITE)
            .setAction(ACTION_TEXT, mock(View.OnClickListener.class));
    final Snackbar.Callback mockCallback = spy(new Snackbar.Callback());
    snackbar.setCallback(mockCallback);

    SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown(snackbar);
    verify(mockCallback, times(1)).onShown(snackbar);

    SnackbarUtils.dismissTransientBottomBarAndWaitUntilFullyDismissed(snackbar);
    verify(mockCallback, times(1))
        .onDismissed(snackbar, BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_MANUAL);
  }

  @Test
  public void testSingleCallback() throws Throwable {
    final Snackbar snackbar =
        Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_INDEFINITE)
            .setAction(ACTION_TEXT, mock(View.OnClickListener.class));
    final BaseTransientBottomBar.BaseCallback mockCallback1 =
        mock(BaseTransientBottomBar.BaseCallback.class);
    final BaseTransientBottomBar.BaseCallback mockCallback2 =
        mock(BaseTransientBottomBar.BaseCallback.class);
    snackbar.addCallback(mockCallback1);

    SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown(snackbar);
    verify(mockCallback1, times(1)).onShown(snackbar);
    verify(mockCallback2, never()).onShown(snackbar);

    SnackbarUtils.dismissTransientBottomBarAndWaitUntilFullyDismissed(snackbar);
    verify(mockCallback1, times(1))
        .onDismissed(snackbar, BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_MANUAL);
    verify(mockCallback2, never())
        .onDismissed(snackbar, BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_MANUAL);
  }

  @Test
  public void testMultipleCallbacks() throws Throwable {
    final Snackbar snackbar =
        Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_INDEFINITE)
            .setAction(ACTION_TEXT, mock(View.OnClickListener.class));
    final BaseTransientBottomBar.BaseCallback mockCallback1 =
        mock(BaseTransientBottomBar.BaseCallback.class);
    final BaseTransientBottomBar.BaseCallback mockCallback2 =
        mock(BaseTransientBottomBar.BaseCallback.class);
    snackbar.addCallback(mockCallback1);
    snackbar.addCallback(mockCallback2);

    SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown(snackbar);
    verify(mockCallback1, times(1)).onShown(snackbar);
    verify(mockCallback2, times(1)).onShown(snackbar);

    SnackbarUtils.dismissTransientBottomBarAndWaitUntilFullyDismissed(snackbar);
    verify(mockCallback1, times(1))
        .onDismissed(snackbar, BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_MANUAL);
    verify(mockCallback2, times(1))
        .onDismissed(snackbar, BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_MANUAL);
  }

  @Test
  public void testMultipleCallbacksWithRemoval() throws Throwable {
    final Snackbar snackbar =
        Snackbar.make(mCoordinatorLayout, MESSAGE_TEXT, Snackbar.LENGTH_INDEFINITE)
            .setAction(ACTION_TEXT, mock(View.OnClickListener.class));
    final BaseTransientBottomBar.BaseCallback mockCallback1 =
        mock(BaseTransientBottomBar.BaseCallback.class);
    final BaseTransientBottomBar.BaseCallback mockCallback2 =
        mock(BaseTransientBottomBar.BaseCallback.class);
    snackbar.addCallback(mockCallback1);
    snackbar.addCallback(mockCallback2);

    SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown(snackbar);
    verify(mockCallback1, times(1)).onShown(snackbar);
    verify(mockCallback2, times(1)).onShown(snackbar);

    snackbar.removeCallback(mockCallback2);

    SnackbarUtils.dismissTransientBottomBarAndWaitUntilFullyDismissed(snackbar);
    verify(mockCallback1, times(1))
        .onDismissed(snackbar, BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_MANUAL);
    verify(mockCallback2, never())
        .onDismissed(snackbar, BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_MANUAL);
  }
}
