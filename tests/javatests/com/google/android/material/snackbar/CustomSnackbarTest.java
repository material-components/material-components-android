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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.view.LayoutInflater;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.R;
import com.google.android.material.testapp.SnackbarActivity;
import com.google.android.material.testapp.custom.CustomSnackbar;
import com.google.android.material.testapp.custom.CustomSnackbarMainContent;
import com.google.android.material.testutils.SnackbarUtils;
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

  private CoordinatorLayout coordinatorLayout;

  private interface DismissAction {
    void dismiss(CustomSnackbar snackbar);
  }

  @Before
  public void setup() {
    coordinatorLayout = activityTestRule.getActivity().findViewById(R.id.col);
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
    final LayoutInflater inflater = LayoutInflater.from(coordinatorLayout.getContext());
    final CustomSnackbarMainContent content =
        (CustomSnackbarMainContent)
            inflater.inflate(R.layout.custom_snackbar_include, coordinatorLayout, false);
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
    return new CustomSnackbar(coordinatorLayout, content, contentViewCallback);
  }

  @Test
  @LargeTest
  public void testBasicContent() throws Throwable {
    // Verify different combinations of snackbar content (title / subtitle and action)
    // We can't test duration here because timing can be flaky when run in the emulator.

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
        () -> {
          if (action != null) {
            interaction.perform(action);
          } else if (dismissAction != null) {
            activityTestRule.runOnUiThread(() -> dismissAction.dismiss(snackbar));
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
  public void testDismissViaApi() throws Throwable {
    verifyDismissCallback(
        onView(isAssignableFrom(Snackbar.SnackbarLayout.class)),
        null,
        BaseTransientBottomBar::dismiss,
        Snackbar.LENGTH_INDEFINITE,
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
        snackbar -> anotherSnackbar.show(),
        Snackbar.LENGTH_INDEFINITE,
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
