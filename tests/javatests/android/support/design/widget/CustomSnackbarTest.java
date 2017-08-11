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
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.support.annotation.Nullable;
import android.support.design.testapp.R;
import android.support.design.testapp.SnackbarActivity;
import android.support.design.testapp.custom.CustomSnackbar;
import android.support.design.testapp.custom.CustomSnackbarMainContent;
import android.support.design.testutils.SnackbarUtils;
import android.support.test.filters.LargeTest;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CustomSnackbarTest {
  @Rule
  public final ActivityTestRule<SnackbarActivity> activityTestRule =
      new ActivityTestRule<>(SnackbarActivity.class);

  private static final String TITLE_TEXT = "Test title";
  private static final String SUBTITLE_TEXT = "Test subtitle";

  private CoordinatorLayout mCoordinatorLayout;

  private interface DismissAction {
    void dismiss(CustomSnackbar snackbar);
  }

  @Before
  public void setup() {
    mCoordinatorLayout = activityTestRule.getActivity().findViewById(R.id.col);
  }

  private void verifySnackbarContent(
      final CustomSnackbar snackbar, final String expectedTitle, final String expectedSubtitle)
      throws Throwable {
    // Show the snackbar
    SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown(snackbar);

    // Verify that we're showing the title
    withText(expectedTitle)
        .matches(
            allOf(
                isDescendantOfA(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                isDescendantOfA(isAssignableFrom(CustomSnackbarMainContent.class)),
                isCompletelyDisplayed()));

    // Verify that we're showing the subtitle
    withText(expectedSubtitle)
        .matches(
            allOf(
                isDescendantOfA(isAssignableFrom(Snackbar.SnackbarLayout.class)),
                isDescendantOfA(isAssignableFrom(CustomSnackbarMainContent.class)),
                isCompletelyDisplayed()));

    // Dismiss the snackbar
    SnackbarUtils.dismissTransientBottomBarAndWaitUntilFullyDismissed(snackbar);
  }

  private CustomSnackbar makeCustomSnackbar() {
    final LayoutInflater inflater = LayoutInflater.from(mCoordinatorLayout.getContext());
    final CustomSnackbarMainContent content =
        (CustomSnackbarMainContent)
            inflater.inflate(R.layout.custom_snackbar_include, mCoordinatorLayout, false);
    final BaseTransientBottomBar.ContentViewCallback contentViewCallback =
        new BaseTransientBottomBar.ContentViewCallback() {
          @Override
          public void animateContentIn(int delay, int duration) {
            content.setAlpha(0f);
            content.animate().alpha(1f).setDuration(duration).setStartDelay(delay).start();
          }

          @Override
          public void animateContentOut(int delay, int duration) {
            content.setAlpha(1f);
            content.animate().alpha(0f).setDuration(duration).setStartDelay(delay).start();
          }
        };
    return new CustomSnackbar(mCoordinatorLayout, content, contentViewCallback);
  }

  @Test
  @LargeTest
  public void testBasicContent() throws Throwable {
    // Verify different combinations of snackbar content (title / subtitle and action)
    // and duration

    // Short duration
    verifySnackbarContent(
        makeCustomSnackbar()
            .setTitle(TITLE_TEXT)
            .setSubtitle(SUBTITLE_TEXT)
            .setDuration(Snackbar.LENGTH_SHORT),
        TITLE_TEXT,
        SUBTITLE_TEXT);

    // Long duration
    verifySnackbarContent(
        makeCustomSnackbar()
            .setTitle(TITLE_TEXT)
            .setSubtitle(SUBTITLE_TEXT)
            .setDuration(Snackbar.LENGTH_LONG),
        TITLE_TEXT,
        SUBTITLE_TEXT);

    // Indefinite duration
    verifySnackbarContent(
        makeCustomSnackbar()
            .setTitle(TITLE_TEXT)
            .setSubtitle(SUBTITLE_TEXT)
            .setDuration(Snackbar.LENGTH_INDEFINITE),
        TITLE_TEXT,
        SUBTITLE_TEXT);
  }

  private void verifyDismissCallback(
      final ViewInteraction interaction,
      @Nullable final ViewAction action,
      @Nullable final DismissAction dismissAction,
      final int length,
      @Snackbar.Callback.DismissEvent final int expectedEvent)
      throws Throwable {
    final BaseTransientBottomBar.BaseCallback mockCallback =
        mock(BaseTransientBottomBar.BaseCallback.class);
    final CustomSnackbar snackbar =
        makeCustomSnackbar()
            .setTitle(TITLE_TEXT)
            .setSubtitle(SUBTITLE_TEXT)
            .setDuration(length)
            .addCallback(mockCallback);

    // Show the snackbar
    SnackbarUtils.showTransientBottomBarAndWaitUntilFullyShown(snackbar);
    // Verify that our onShown has been called
    verify(mockCallback, times(1)).onShown(snackbar);
    // and that the snackbar is either shown or queued to be shown
    assertTrue(snackbar.isShownOrQueued());
    // and also check that we have the intended title / subtitle displayed somewhere in
    // our hierarchy
    onView(withText(TITLE_TEXT)).check(matches(isCompletelyDisplayed()));
    onView(withText(SUBTITLE_TEXT)).check(matches(isCompletelyDisplayed()));

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
        makeCustomSnackbar()
            .setTitle("Different title")
            .setSubtitle("Different subtitle")
            .setDuration(Snackbar.LENGTH_SHORT);

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

  @Test
  @MediumTest
  public void testMultipleCallbacks() throws Throwable {
    final CustomSnackbar snackbar =
        makeCustomSnackbar()
            .setTitle(TITLE_TEXT)
            .setSubtitle(SUBTITLE_TEXT)
            .setDuration(Snackbar.LENGTH_INDEFINITE);
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
}
