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

package com.google.android.material.bottomsheet;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.VIEW_INDEX_BOTTOM_SHEET;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.fail;

import android.content.Context;
import android.os.SystemClock;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.widget.NestedScrollView;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.MotionEvents;
import androidx.test.espresso.action.PrecisionDescriber;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.MediumTest;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.testapp.BottomSheetBehaviorActivity;
import com.google.android.material.testapp.R;
import com.google.android.material.testutils.AccessibilityUtils;
import com.google.android.material.testutils.DesignViewActions;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BottomSheetBehaviorTest {

  @Rule
  public final ActivityTestRule<BottomSheetBehaviorActivity> activityTestRule =
      new ActivityTestRule<>(BottomSheetBehaviorActivity.class);

  public static class Callback extends BottomSheetBehavior.BottomSheetCallback
      implements IdlingResource {

    private boolean isIdle;
    private boolean idleWhileSettling;

    private IdlingResource.ResourceCallback resourceCallback;

    public Callback(BottomSheetBehavior<?> behavior) {
      behavior.addBottomSheetCallback(this);
      int state = behavior.getState();
      isIdle = isIdleState(state);
    }

    @Override
    public void onStateChanged(@NonNull View bottomSheet, @BottomSheetBehavior.State int newState) {
      boolean wasIdle = isIdle;
      isIdle = isIdleState(newState);
      if (!wasIdle && isIdle && resourceCallback != null) {
        resourceCallback.onTransitionToIdle();
      }
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
      assertThat(slideOffset, is(greaterThanOrEqualTo(-1f)));
      assertThat(slideOffset, is(lessThanOrEqualTo(1f)));
    }

    @Override
    public String getName() {
      return Callback.class.getSimpleName();
    }

    @Override
    public boolean isIdleNow() {
      return isIdle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
      resourceCallback = callback;
    }

    public void setIdleWhileSettling(boolean idleWhileSettling) {
      this.idleWhileSettling = idleWhileSettling;
    }

    private boolean isIdleState(int state) {
      return state != BottomSheetBehavior.STATE_DRAGGING
          && (idleWhileSettling || state != BottomSheetBehavior.STATE_SETTLING);
    }
  }

  /** Wait for a FAB to change its visibility (either shown or hidden). */
  private static class OnVisibilityChangedListener
      extends FloatingActionButton.OnVisibilityChangedListener implements IdlingResource {

    private final boolean shown;
    private boolean isIdle;
    private ResourceCallback resourceCallback;

    OnVisibilityChangedListener(boolean shown) {
      this.shown = shown;
    }

    private void transitionToIdle() {
      if (!isIdle) {
        isIdle = true;
        if (resourceCallback != null) {
          resourceCallback.onTransitionToIdle();
        }
      }
    }

    @Override
    public void onShown(FloatingActionButton fab) {
      if (shown) {
        transitionToIdle();
      }
    }

    @Override
    public void onHidden(FloatingActionButton fab) {
      if (!shown) {
        transitionToIdle();
      }
    }

    @Override
    public String getName() {
      return OnVisibilityChangedListener.class.getSimpleName();
    }

    @Override
    public boolean isIdleNow() {
      return isIdle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
      this.resourceCallback = resourceCallback;
    }
  }

  /** This is like {@link GeneralSwipeAction}, but it does not send ACTION_UP at the end. */
  private static class DragAction implements ViewAction {

    private static final int STEPS = 10;
    private static final int DURATION = 100;

    private final CoordinatesProvider start;
    private final CoordinatesProvider end;
    private final PrecisionDescriber precisionDescriber;

    public DragAction(
        CoordinatesProvider start, CoordinatesProvider end, PrecisionDescriber precisionDescriber) {
      this.start = start;
      this.end = end;
      this.precisionDescriber = precisionDescriber;
    }

    @Override
    public Matcher<View> getConstraints() {
      return Matchers.any(View.class);
    }

    @Override
    public String getDescription() {
      return "drag";
    }

    @Override
    public void perform(UiController uiController, View view) {
      float[] precision = precisionDescriber.describePrecision();
      float[] start = this.start.calculateCoordinates(view);
      float[] end = this.end.calculateCoordinates(view);
      float[][] steps = interpolate(start, end, STEPS);
      int delayBetweenMovements = DURATION / steps.length;
      // Down
      MotionEvent downEvent = MotionEvents.sendDown(uiController, start, precision).down;
      try {
        for (int i = 0; i < steps.length; i++) {
          // Wait
          long desiredTime = downEvent.getDownTime() + (long) (delayBetweenMovements * i);
          long timeUntilDesired = desiredTime - SystemClock.uptimeMillis();
          if (timeUntilDesired > 10L) {
            uiController.loopMainThreadForAtLeast(timeUntilDesired);
          }
          // Move
          if (!MotionEvents.sendMovement(uiController, downEvent, steps[i])) {
            MotionEvents.sendCancel(uiController, downEvent);
            throw new RuntimeException("Cannot drag: failed to send a move event.");
          }
        }
        int duration = ViewConfiguration.getPressedStateDuration();
        if (duration > 0) {
          uiController.loopMainThreadForAtLeast((long) duration);
        }
      } finally {
        downEvent.recycle();
      }
    }

    private static float[][] interpolate(float[] start, float[] end, int steps) {
      if (1 >= start.length) {
        throw new IndexOutOfBoundsException(
            "1 is outside of start's bounds [" + start.length + "]");
      }
      if (1 >= end.length) {
        throw new IndexOutOfBoundsException("1 is outside of end's bounds [" + start.length + "]");
      }
      float[][] res = new float[steps][2];
      for (int i = 1; i < steps + 1; ++i) {
        res[i - 1][0] = start[0] + (end[0] - start[0]) * (float) i / ((float) steps + 2.0F);
        res[i - 1][1] = start[1] + (end[1] - start[1]) * (float) i / ((float) steps + 2.0F);
      }
      return res;
    }
  }

  private static class AddViewAction implements ViewAction {

    private final int layout;

    public AddViewAction(@LayoutRes int layout) {
      this.layout = layout;
    }

    @Override
    public Matcher<View> getConstraints() {
      return ViewMatchers.isAssignableFrom(ViewGroup.class);
    }

    @Override
    public String getDescription() {
      return "add view";
    }

    @Override
    public void perform(UiController uiController, View view) {
      ViewGroup parent = (ViewGroup) view;
      View child = LayoutInflater.from(view.getContext()).inflate(layout, parent, false);
      parent.addView(child);
    }
  }

  private Callback callback;

  @Test
  @SmallTest
  public void testInitialSetup() {
    BottomSheetBehavior<?> behavior = getBehavior();
    assertThat(behavior.getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
    assertThat(behavior.isFitToContents(), is(true));
    CoordinatorLayout coordinatorLayout = getCoordinatorLayout();
    ViewGroup bottomSheet = getBottomSheet();
    assertThat(bottomSheet.getTop(), is(coordinatorLayout.getHeight() - behavior.getPeekHeight()));
    assertAccessibilityActions(behavior, getBottomSheet());
  }

  @Test
  @MediumTest
  public void testSetStateExpandedToCollapsed() throws Throwable {
    checkSetState(BottomSheetBehavior.STATE_EXPANDED, ViewMatchers.isDisplayed());
    checkSetState(BottomSheetBehavior.STATE_COLLAPSED, ViewMatchers.isDisplayed());
  }

  @Test
  @MediumTest
  public void testSetStateHiddenToCollapsed() throws Throwable {
    checkSetState(BottomSheetBehavior.STATE_HIDDEN, not(ViewMatchers.isDisplayed()));
    checkSetState(BottomSheetBehavior.STATE_COLLAPSED, ViewMatchers.isDisplayed());
  }

  @Test
  @MediumTest
  public void testSetStateCollapsedToCollapsed() throws Throwable {
    checkSetState(BottomSheetBehavior.STATE_COLLAPSED, ViewMatchers.isDisplayed());
  }

  @Test
  @MediumTest
  public void testSwipeDownToCollapse() throws Throwable {
    checkSetState(BottomSheetBehavior.STATE_EXPANDED, ViewMatchers.isDisplayed());
    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        .perform(
            DesignViewActions.withCustomConstraints(
                new GeneralSwipeAction(
                    Swipe.FAST,
                    // Manually calculate the starting coordinates to make sure that the touch
                    // actually falls onto the view on Gingerbread
                    view -> {
                      int[] location = new int[2];
                      view.getLocationInWindow(location);
                      return new float[] {view.getWidth() / 2, location[1] + 1};
                    },
                    // Manually calculate the ending coordinates to make sure that the bottom
                    // sheet is collapsed, not hidden
                    view -> {
                      BottomSheetBehavior<?> behavior = getBehavior();
                      return new float[] {
                        // x: center of the bottom sheet
                        view.getWidth() / 2,
                        // y: just above the peek height
                        view.getHeight() - behavior.getPeekHeight()
                      };
                    },
                    Press.FINGER),
                ViewMatchers.isDisplayingAtLeast(5)));
    registerIdlingResourceCallback();
    try {
      Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
          .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
      assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
    } finally {
      unregisterIdlingResourceCallback();
    }
  }

  @Test
  @MediumTest
  public void testSwipeDownToCollapseFullyExpanded() throws Throwable {
    testSwipeDownToCollapse();
  }

  @Test
  @MediumTest
  public void testSwipeDownToHide() {
    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        .perform(
            DesignViewActions.withCustomConstraints(
                ViewActions.swipeDown(), ViewMatchers.isDisplayingAtLeast(5)));
    registerIdlingResourceCallback();
    try {
      Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
          .check(ViewAssertions.matches(not(ViewMatchers.isDisplayed())));
      assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_HIDDEN));
    } finally {
      unregisterIdlingResourceCallback();
    }
  }

  @Test
  @MediumTest
  public void testSwipeDownToHideFullyExpanded() throws Throwable {
    getBehavior().setFitToContents(false);
    testSwipeDownToHide();
  }

  @Test
  @MediumTest
  public void testSkipCollapsed() throws Throwable {
    assertAccessibilityActions(getBehavior(), getBottomSheet());
    getBehavior().setSkipCollapsed(true);
    assertAccessibilityActions(getBehavior(), getBottomSheet());
    checkSetState(BottomSheetBehavior.STATE_EXPANDED, ViewMatchers.isDisplayed());
    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        .perform(
            DesignViewActions.withCustomConstraints(
                new GeneralSwipeAction(
                    Swipe.FAST,
                    // Manually calculate the starting coordinates to make sure that the touch
                    // actually falls onto the view on Gingerbread
                    view -> {
                      int[] location = new int[2];
                      view.getLocationInWindow(location);
                      return new float[] {view.getWidth() / 2, location[1] + 1};
                    },
                    // Manually calculate the ending coordinates to make sure that the bottom
                    // sheet is collapsed, not hidden
                    view -> {
                      BottomSheetBehavior<?> behavior = getBehavior();
                      return new float[] {
                        // x: center of the bottom sheet
                        view.getWidth() / 2,
                        // y: just above the peek height
                        view.getHeight() - behavior.getPeekHeight()
                      };
                    },
                    Press.FINGER),
                ViewMatchers.isDisplayingAtLeast(5)));
    registerIdlingResourceCallback();
    try {
      Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
          .check(ViewAssertions.matches(not(ViewMatchers.isDisplayed())));
      assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_HIDDEN));
    } finally {
      unregisterIdlingResourceCallback();
    }
  }

  @Test
  @MediumTest
  public void testSkipCollapsedFullyExpanded() throws Throwable {
    getBehavior().setFitToContents(false);
    testSkipCollapsed();
  }

  private void testSkipCollapsed_smallSwipe(int expectedState, float swipeViewHeightPercentage)
      throws Throwable {
    getBehavior().setSkipCollapsed(true);
    checkSetState(BottomSheetBehavior.STATE_EXPANDED, ViewMatchers.isDisplayed());
    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        .perform(
            DesignViewActions.withCustomConstraints(
                new GeneralSwipeAction(
                    Swipe.SLOW,
                    // Manually calculate the starting coordinates to make sure that the touch
                    // actually falls onto the view on Gingerbread
                    view -> {
                      int[] location = new int[2];
                      view.getLocationInWindow(location);
                      return new float[] {view.getWidth() / 2, location[1] + 1};
                    },
                    // Manually calculate the ending coordinates to make sure that the bottom
                    // sheet is collapsed, not hidden
                    view -> {
                      return new float[] {
                        // x: center of the bottom sheet
                        view.getWidth() / 2,
                        // y: some percentage down the view
                        view.getHeight() * swipeViewHeightPercentage,
                      };
                    },
                    Press.FINGER),
                ViewMatchers.isDisplayingAtLeast(5)));
    registerIdlingResourceCallback();
    try {
      if (expectedState == BottomSheetBehavior.STATE_HIDDEN) {
        Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
            .check(ViewAssertions.matches(not(ViewMatchers.isDisplayed())));
      } else {
        Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
      }
      assertThat(getBehavior().getState(), is(expectedState));
    } finally {
      unregisterIdlingResourceCallback();
    }
  }

  @Test
  @MediumTest
  public void testSkipCollapsed_smallSwipe_remainsExpanded() throws Throwable {
    testSkipCollapsed_smallSwipe(
        BottomSheetBehavior.STATE_EXPANDED, /* swipeViewHeightPercentage = */ 0.5f);
  }

  @Test
  @MediumTest
  public void testSkipCollapsedFullyExpanded_smallSwipe_remainsExpanded() throws Throwable {
    getBehavior().setFitToContents(false);
    testSkipCollapsed_smallSwipe(
        BottomSheetBehavior.STATE_HALF_EXPANDED, /* swipeViewHeightPercentage = */ 0.5f);
  }

  @Test
  @MediumTest
  public void testSkipCollapsed_smallSwipePastThreshold_getsHidden() throws Throwable {
    testSkipCollapsed_smallSwipe(
        BottomSheetBehavior.STATE_HIDDEN, /* swipeViewHeightPercentage = */ 0.75f);
  }

  @Test
  @MediumTest
  public void testSkipCollapsedFullyExpanded_smallSwipePastThreshold_getsHidden() throws Throwable {
    getBehavior().setFitToContents(false);
    testSkipCollapsed_smallSwipe(
        BottomSheetBehavior.STATE_HIDDEN, /* swipeViewHeightPercentage = */ 0.75f);
  }

  @Test
  @MediumTest
  public void testSwipeUpToExpand() {
    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        .perform(
            DesignViewActions.withCustomConstraints(
                new GeneralSwipeAction(
                    Swipe.FAST,
                    GeneralLocation.VISIBLE_CENTER,
                    view -> new float[] {view.getWidth() / 2, 0},
                    Press.FINGER),
                ViewMatchers.isDisplayingAtLeast(5)));
    registerIdlingResourceCallback();
    try {
      Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
          .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
      assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_EXPANDED));
    } finally {
      unregisterIdlingResourceCallback();
    }
  }

  @Test
  @MediumTest
  public void testNoSwipeUpToExpand() {
    getBehavior().setDraggable(false);
    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        .perform(
            DesignViewActions.withCustomConstraints(
                new GeneralSwipeAction(
                    Swipe.FAST,
                    GeneralLocation.VISIBLE_CENTER,
                    view -> new float[] {view.getWidth() / 2, 0},
                    Press.FINGER),
                ViewMatchers.isDisplayingAtLeast(5)));

    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
  }

  @Test
  @MediumTest
  public void testNoSwipeDownToCollapse() throws Throwable {
    checkSetState(BottomSheetBehavior.STATE_EXPANDED, ViewMatchers.isDisplayed());
    getBehavior().setDraggable(false);
    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        .perform(
            DesignViewActions.withCustomConstraints(
                new GeneralSwipeAction(
                    Swipe.FAST,
                    // Manually calculate the starting coordinates to make sure that the touch
                    // actually falls onto the view on Gingerbread
                    view -> {
                      int[] location = new int[2];
                      view.getLocationInWindow(location);
                      return new float[] {view.getWidth() / 2, location[1] + 1};
                    },
                    // Manually calculate the ending coordinates to make sure that the bottom
                    // sheet is collapsed, not hidden
                    view -> {
                      BottomSheetBehavior<?> behavior = getBehavior();
                      return new float[] {
                        // x: center of the bottom sheet
                        view.getWidth() / 2,
                        // y: just above the peek height
                        view.getHeight() - behavior.getPeekHeight()
                      };
                    },
                    Press.FINGER),
                ViewMatchers.isDisplayingAtLeast(5)));

    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_EXPANDED));
  }

  @Test
  @MediumTest
  public void testNoDragging() {
    getBehavior().setDraggable(false);
    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        // Drag (and not release)
        .perform(
            new DragAction(
                GeneralLocation.VISIBLE_CENTER, GeneralLocation.TOP_CENTER, Press.FINGER))
        // Check that the bottom sheet is NOT in STATE_DRAGGING
        .check(
            (view, e) -> {
              assertThat(view, is(ViewMatchers.isDisplayed()));
              BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(view);
              assertThat(behavior.getState(), not(is(BottomSheetBehavior.STATE_DRAGGING)));
            });
  }

  @Test
  @MediumTest
  public void testDraggableChange() {
    getBehavior().setDraggable(false);
    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        // Drag (and not release)
        .perform(
            new DragAction(
                GeneralLocation.VISIBLE_CENTER, GeneralLocation.TOP_CENTER, Press.FINGER))
        // Check that the bottom sheet is NOT in STATE_DRAGGING
        .check(
            (view, e) -> {
              assertThat(view, is(ViewMatchers.isDisplayed()));
              BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(view);
              assertThat(behavior.getState(), not(is(BottomSheetBehavior.STATE_DRAGGING)));
            });
    getBehavior().setDraggable(true);
    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        // Drag (and not release)
        .perform(
            new DragAction(
                GeneralLocation.VISIBLE_CENTER, GeneralLocation.TOP_CENTER, Press.FINGER))
        // Check that the bottom sheet is in STATE_DRAGGING
        .check(
            (view, e) -> {
              assertThat(view, is(ViewMatchers.isDisplayed()));
              BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(view);
              assertThat(behavior.getState(), is(BottomSheetBehavior.STATE_DRAGGING));
            });
  }

  @Test
  @MediumTest
  public void testHalfExpandedToExpanded() throws Throwable {
    getBehavior().setFitToContents(false);
    checkSetState(BottomSheetBehavior.STATE_HALF_EXPANDED, ViewMatchers.isDisplayed());
    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        .perform(
            DesignViewActions.withCustomConstraints(
                new GeneralSwipeAction(
                    Swipe.FAST,
                    GeneralLocation.VISIBLE_CENTER,
                    view -> new float[] {view.getWidth() / 2, 0},
                    Press.FINGER),
                ViewMatchers.isDisplayingAtLeast(5)));
    registerIdlingResourceCallback();
    try {
      Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
          .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
      assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_EXPANDED));
    } finally {
      unregisterIdlingResourceCallback();
    }
  }

  @Test
  @MediumTest
  public void testCollapsedToExpanded() throws Throwable {
    getBehavior().setFitToContents(false);
    checkSetState(BottomSheetBehavior.STATE_COLLAPSED, ViewMatchers.isDisplayed());
    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        .perform(
            DesignViewActions.withCustomConstraints(
                new GeneralSwipeAction(
                    Swipe.FAST,
                    GeneralLocation.VISIBLE_CENTER,
                    view -> new float[] {view.getWidth() / 2, 0},
                    Press.FINGER),
                ViewMatchers.isDisplayingAtLeast(5)));
    registerIdlingResourceCallback();
    try {
      Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
          .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
      assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_EXPANDED));
    } finally {
      unregisterIdlingResourceCallback();
    }
  }

  @Test
  @SmallTest
  public void testSwitchFitSheetToContents() throws Throwable {
    getBehavior().setFitToContents(false);
    checkSetState(BottomSheetBehavior.STATE_HALF_EXPANDED, ViewMatchers.isDisplayed());
    activityTestRule.runOnUiThread(() -> getBehavior().setFitToContents(true));
    activityTestRule.runOnUiThread(
        () -> assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_EXPANDED)));
  }

  @Test
  @MediumTest
  public void testInvisible() throws Throwable {
    // Make the bottomsheet invisible
    activityTestRule.runOnUiThread(
        () -> {
          getBottomSheet().setVisibility(View.INVISIBLE);
          assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
        });
    // Swipe up as if to expand it
    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        .perform(
            DesignViewActions.withCustomConstraints(
                new GeneralSwipeAction(
                    Swipe.FAST,
                    GeneralLocation.VISIBLE_CENTER,
                    view -> new float[] {view.getWidth() / 2, 0},
                    Press.FINGER),
                not(ViewMatchers.isDisplayed())));
    // Check that the bottom sheet stays the same collapsed state
    activityTestRule.runOnUiThread(
        () -> assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_COLLAPSED)));
  }

  @Test
  @MediumTest
  public void testInvisibleThenVisible() throws Throwable {
    activityTestRule.runOnUiThread(
        () -> {
          // The bottom sheet is initially invisible
          getBottomSheet().setVisibility(View.INVISIBLE);
          // Then it becomes visible when the CoL is touched
          getCoordinatorLayout()
              .setOnTouchListener(
                  (view, e) -> {
                    if (e.getAction() == MotionEvent.ACTION_DOWN) {
                      getBottomSheet().setVisibility(View.VISIBLE);
                      return true;
                    }
                    return false;
                  });
          assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
        });
    // Drag over the CoL
    Espresso.onView(ViewMatchers.withId(R.id.coordinator))
        // Drag (and not release)
        .perform(
            new DragAction(GeneralLocation.BOTTOM_CENTER, GeneralLocation.TOP_CENTER, Press.FINGER))
        .check(
            (view, e) -> {
              // The bottom sheet should not react to the touch events
              assertThat(getBottomSheet(), is(ViewMatchers.isDisplayed()));
              assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
            });
  }

  @Test
  @MediumTest
  public void testNestedScroll() throws Throwable {
    final ViewGroup bottomSheet = getBottomSheet();
    final BottomSheetBehavior<?> behavior = getBehavior();
    final NestedScrollView scroll = new NestedScrollView(activityTestRule.getActivity());
    // Set up nested scrolling area
    activityTestRule.runOnUiThread(
        () -> {
          bottomSheet.addView(
              scroll,
              new ViewGroup.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
          View view = new View(activityTestRule.getActivity());
          // Make sure that the NestedScrollView is always scrollable
          view.setMinimumHeight(bottomSheet.getHeight() + 1000);
          scroll.addView(view);

          assertThat(behavior.getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
          // The scroll offset is 0 at first
          assertThat(scroll.getScrollY(), is(0));
        });
    // Swipe from the very bottom of the bottom sheet to the top edge of the screen so that the
    // scrolling content is also scrolled
    Espresso.onView(ViewMatchers.withId(R.id.coordinator))
        .perform(
            new GeneralSwipeAction(
                Swipe.SLOW,
                view -> new float[] {view.getWidth() / 2, view.getHeight() - 1},
                view -> new float[] {view.getWidth() / 2, 1},
                Press.FINGER));
    registerIdlingResourceCallback();
    try {
      Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
          .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
      activityTestRule.runOnUiThread(
          () -> {
            assertThat(behavior.getState(), is(BottomSheetBehavior.STATE_EXPANDED));
            // This confirms that the nested scrolling area was scrolled continuously after
            // the bottom sheet is expanded.
            assertThat(scroll.getScrollY(), is(not(0)));
          });
    } finally {
      unregisterIdlingResourceCallback();
    }
  }

  @Test
  @MediumTest
  public void testNestedScrollMultiple() throws Throwable {
    final ViewGroup bottomSheet = getBottomSheet();
    final BottomSheetBehavior<?> behavior = getBehavior();
    final NestedScrollView scroll1 = new NestedScrollView(activityTestRule.getActivity());
    final NestedScrollView scroll2 = new NestedScrollView(activityTestRule.getActivity());
    // Set up nested scrolling area
    activityTestRule.runOnUiThread(
        () -> {
          bottomSheet.addView(
              scroll1,
              new ViewGroup.LayoutParams(
                  bottomSheet.getWidth()/2, ViewGroup.LayoutParams.MATCH_PARENT));
          bottomSheet.addView(
              scroll2,
              new ViewGroup.LayoutParams(
                  bottomSheet.getWidth()/2, ViewGroup.LayoutParams.MATCH_PARENT));
          View view1 = new View(activityTestRule.getActivity());
          View view2 = new View(activityTestRule.getActivity());
          // Make sure that both NestedScrollViews are always scrollable
          view1.setMinimumHeight(bottomSheet.getHeight() + 1000);
          view2.setMinimumHeight(bottomSheet.getHeight() + 1000);
          scroll1.addView(view1);
          scroll2.addView(view2);

          assertThat(behavior.getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
          // The scroll offset is 0 at first
          assertThat(scroll1.getScrollY(), is(0));
          assertThat(scroll2.getScrollY(), is(0));
        });
    // Swipe down to up on left side to scroll the left scrollview
    Espresso.onView(ViewMatchers.withId(R.id.coordinator))
        .perform(
            new GeneralSwipeAction(
                Swipe.SLOW,
                view -> new float[] {0, view.getHeight() - 1},
                view -> new float[] {0, 1},
                Press.FINGER));
    registerIdlingResourceCallback();
    try {
      Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
          .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
      activityTestRule.runOnUiThread(
          () -> {
            assertThat(behavior.getState(), is(BottomSheetBehavior.STATE_EXPANDED));
            // This confirms that the nested scrolling area was scrolled continuously after
            // the bottom sheet is expanded.
            assertThat(scroll1.getScrollY(), is(not(0)));
            assertThat(scroll2.getScrollY(), is(0));
          });
    } finally {
      unregisterIdlingResourceCallback();
    }

    // Swipe up to down on right side to collapse bottom sheet
    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        .perform(
            DesignViewActions.withCustomConstraints(
                new GeneralSwipeAction(
                    Swipe.FAST,
                    // Manually calculate the starting coordinates to make sure that the touch
                    // actually falls onto the view on Gingerbread
                    view -> {
                      int[] location = new int[2];
                      view.getLocationInWindow(location);
                      return new float[] {view.getWidth() - 1, location[1] + 1};
                    },
                    // Manually calculate the ending coordinates to make sure that the bottom
                    // sheet is collapsed, not hidden
                    view -> {
                      return new float[] {
                          // x: right side of the bottom sheet
                          view.getWidth() - 1,
                          // y: just above the peek height
                          view.getHeight() - behavior.getPeekHeight()
                      };
                    },
                    Press.FINGER),
                ViewMatchers.isDisplayingAtLeast(5)));

    registerIdlingResourceCallback();
    try {
      activityTestRule.runOnUiThread(
          () -> {
            assertThat(behavior.getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
            assertThat(scroll1.getScrollY(), is(not(0)));
          });
    } finally {
      unregisterIdlingResourceCallback();
    }
  }

  @Test
  @MediumTest
  public void testDragOutside() {
    // Swipe up outside of the bottom sheet
    Espresso.onView(ViewMatchers.withId(R.id.coordinator))
        .perform(
            DesignViewActions.withCustomConstraints(
                new GeneralSwipeAction(
                    Swipe.FAST,
                    // Just above the bottom sheet
                    view ->
                        new float[] {
                          view.getWidth() / 2, view.getHeight() - getBehavior().getPeekHeight() - 9
                        },
                    // Top of the CoordinatorLayout
                    view -> new float[] {view.getWidth() / 2, 1},
                    Press.FINGER),
                ViewMatchers.isDisplayed()));
    registerIdlingResourceCallback();
    try {
      Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
          .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
      // The bottom sheet should remain collapsed
      assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
    } finally {
      unregisterIdlingResourceCallback();
    }
  }

  @Test
  @MediumTest
  public void testLayoutWhileDragging() {
    Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
        // Drag (and not release)
        .perform(
            new DragAction(
                GeneralLocation.VISIBLE_CENTER, GeneralLocation.TOP_CENTER, Press.FINGER))
        // Check that the bottom sheet is in STATE_DRAGGING
        .check(
            (view, e) -> {
              assertThat(view, is(ViewMatchers.isDisplayed()));
              BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(view);
              assertThat(behavior.getState(), is(BottomSheetBehavior.STATE_DRAGGING));
            })
        // Add a new view
        .perform(new AddViewAction(R.layout.frame_layout))
        // Check that the newly added view is properly laid out
        .check(
            (view, e) -> {
              ViewGroup parent = (ViewGroup) view;
              assertThat(parent.getChildCount(), is(1));
              View child = parent.getChildAt(0);
              assertThat(ViewCompat.isLaidOut(child), is(true));
            });
  }

  @Test
  @MediumTest
  public void testFabVisibility() {
    withFabVisibilityChange(
        false,
        () -> {
          try {
            checkSetState(BottomSheetBehavior.STATE_EXPANDED, ViewMatchers.isDisplayed());
          } catch (Throwable throwable) {
            fail(throwable.getMessage());
          }
        });
    withFabVisibilityChange(
        true,
        () -> {
          try {
            checkSetState(BottomSheetBehavior.STATE_COLLAPSED, ViewMatchers.isDisplayed());
          } catch (Throwable throwable) {
            fail(throwable.getMessage());
          }
        });
  }

  @Test
  @MediumTest
  public void testAutoPeekHeight() throws Throwable {
    activityTestRule.runOnUiThread(
        () -> getBehavior().setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO));
    activityTestRule.runOnUiThread(
        () -> {
          CoordinatorLayout col = getCoordinatorLayout();
          assertThat(
              getBottomSheet().getTop(),
              is(
                  Math.min(
                      col.getWidth() * 9 / 16,
                      col.getHeight() - getBehavior().getPeekHeightMin())));
        });
  }

  @Test
  @MediumTest
  public void testAutoPeekHeightHide() throws Throwable {
    activityTestRule.runOnUiThread(
        () -> {
          getBehavior().setHideable(true);
          getBehavior().setPeekHeight(0);
          getBehavior().setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
        });
    checkSetState(BottomSheetBehavior.STATE_HIDDEN, not(ViewMatchers.isDisplayed()));
  }

  @Test
  @MediumTest
  public void testDynamicContent() throws Throwable {
    registerIdlingResourceCallback();
    try {
      activityTestRule.runOnUiThread(
          () -> {
            ViewGroup.LayoutParams params = getBottomSheet().getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getBottomSheet().setLayoutParams(params);
            View view = new View(getBottomSheet().getContext());
            int size = getBehavior().getPeekHeight() * 2;
            getBottomSheet().addView(view, new ViewGroup.LayoutParams(size, size));
            assertThat(getBottomSheet().getChildCount(), is(1));
            // Shrink the content height.
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.height = (int) (size * 0.8);
            view.setLayoutParams(lp);
            // Immediately expand the bottom sheet.
            getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
          });
      Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
          .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
      assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_EXPANDED));
      // Make sure that the bottom sheet is not floating above the bottom.
      assertThat(getBottomSheet().getBottom(), is(getCoordinatorLayout().getBottom()));
    } finally {
      unregisterIdlingResourceCallback();
    }
  }

  @Test
  @MediumTest
  public void testExpandedPeekHeight() throws Throwable {
    activityTestRule.runOnUiThread(
        () -> {
          // Make the peek height as tall as the bottom sheet.
          BottomSheetBehavior<?> behavior = getBehavior();
          behavior.setPeekHeight(getBottomSheet().getHeight());
          assertThat(behavior.getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
        });
    // Both of these will not animate the sheet , but the state should be changed.
    checkSetState(BottomSheetBehavior.STATE_EXPANDED, ViewMatchers.isDisplayed());
    checkSetState(BottomSheetBehavior.STATE_COLLAPSED, ViewMatchers.isDisplayed());
  }

  @Test
  @MediumTest
  public void testCalculateSlideOffset() throws Throwable {
    checkSlideOffset(BottomSheetBehavior.STATE_EXPANDED, 1f);
    checkSlideOffset(BottomSheetBehavior.STATE_COLLAPSED, 0f);
    checkSlideOffset(BottomSheetBehavior.STATE_HIDDEN, -1f);
  }

  @Test
  @SmallTest
  public void testFindScrollingChildrenEnabled() {
    Context context = activityTestRule.getActivity();
    FrameLayout parent = new FrameLayout(context);

    NestedScrollView disabledParent = new NestedScrollView(context);
    disabledParent.setNestedScrollingEnabled(false);
    parent.addView(disabledParent);
    NestedScrollView enabledChild1 = new NestedScrollView(context);
    enabledChild1.setNestedScrollingEnabled(true);
    disabledParent.addView(enabledChild1);

    NestedScrollView enabledChild2 = new NestedScrollView(context);
    enabledChild2.setNestedScrollingEnabled(true);
    parent.addView(enabledChild2);

    getBehavior().populateScrollingChildren(parent);
    assertThat(getBehavior().nestedScrollingChildrenRef.get(0).get(), equalTo(enabledChild1));
    assertThat(getBehavior().nestedScrollingChildrenRef.get(1).get(), equalTo(enabledChild2));
  }

  @Test
  @SmallTest
  public void testWontFindScrollingChildrenInvisible() {
    Context context = activityTestRule.getActivity();
    FrameLayout parent = new FrameLayout(context);
    NestedScrollView invisibleChild = new NestedScrollView(context);
    invisibleChild.setNestedScrollingEnabled(true);
    invisibleChild.setVisibility(View.INVISIBLE);
    parent.addView(invisibleChild);

    getBehavior().populateScrollingChildren(parent);
    assertThat(getBehavior().nestedScrollingChildrenRef.isEmpty(), is(true));
  }

  private void checkSetState(final int state, Matcher<View> matcher) throws Throwable {
    registerIdlingResourceCallback();
    try {
      activityTestRule.runOnUiThread(() -> getBehavior().setState(state));
      Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
          .check(ViewAssertions.matches(matcher));
      assertThat(getBehavior().getState(), is(state));
      assertAccessibilityActions(getBehavior(), getBottomSheet());
    } finally {
      unregisterIdlingResourceCallback();
    }
  }

  private void checkSlideOffset(final int state, float slideOffset)
      throws Throwable {
    registerIdlingResourceCallback();
    try {
      activityTestRule.runOnUiThread(() -> getBehavior().setState(state));
      // An "always-passing" check to wait until UI thread is idle.
      Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
          .check(ViewAssertions.matches(ViewMatchers.withId(R.id.bottom_sheet)));
      assertThat(getBehavior().calculateSlideOffset(), is(slideOffset));
    } finally {
      unregisterIdlingResourceCallback();
    }
  }

  private void registerIdlingResourceCallback() {
    // This cannot be done in setUp(), or swiping action cannot be executed.
    callback = new Callback(getBehavior());
    Espresso.registerIdlingResources(callback);
  }

  private void unregisterIdlingResourceCallback() {
    if (callback != null) {
      Espresso.unregisterIdlingResources(callback);
      callback = null;
    }
  }

  private void withFabVisibilityChange(boolean shown, Runnable action) {
    OnVisibilityChangedListener listener = new OnVisibilityChangedListener(shown);
    CoordinatorLayout.LayoutParams lp =
        (CoordinatorLayout.LayoutParams) activityTestRule.getActivity().mFab.getLayoutParams();
    FloatingActionButton.Behavior behavior = (FloatingActionButton.Behavior) lp.getBehavior();
    behavior.setInternalAutoHideListener(listener);
    Espresso.registerIdlingResources(listener);
    try {
      action.run();
    } finally {
      Espresso.unregisterIdlingResources(listener);
    }
  }

  private static void assertAccessibilityActions(
      BottomSheetBehavior<?> behavior, ViewGroup bottomSheet) {
    int state = behavior.getState();
    boolean hasExpandAction =
        state == BottomSheetBehavior.STATE_COLLAPSED
            || state == BottomSheetBehavior.STATE_HALF_EXPANDED;
    boolean hasHalfExpandAction =
        state != BottomSheetBehavior.STATE_HALF_EXPANDED && !behavior.isFitToContents();
    boolean hasCollapseAction =
        state == BottomSheetBehavior.STATE_EXPANDED
            || state == BottomSheetBehavior.STATE_HALF_EXPANDED;
    boolean hasDismissAction = state != BottomSheetBehavior.STATE_HIDDEN && behavior.isHideable();
    assertThat(
        hasCustomAccessibilityAction(behavior.expandActionIds),
        equalTo(hasExpandAction));
    assertThat(
        hasCustomAccessibilityAction(behavior.expandHalfwayActionIds),
        equalTo(hasHalfExpandAction));
    assertThat(
        hasCustomAccessibilityAction(behavior.collapseActionIds),
        equalTo(hasCollapseAction));
    assertThat(
        AccessibilityUtils.hasAction(bottomSheet, AccessibilityNodeInfoCompat.ACTION_DISMISS),
        equalTo(hasDismissAction));
  }

  private static boolean hasCustomAccessibilityAction(SparseIntArray actionIds) {
    return actionIds.get(VIEW_INDEX_BOTTOM_SHEET, View.NO_ID) != View.NO_ID;
  }

  private ViewGroup getBottomSheet() {
    return activityTestRule.getActivity().mBottomSheet;
  }

  private BottomSheetBehavior<?> getBehavior() {
    return activityTestRule.getActivity().mBehavior;
  }

  private CoordinatorLayout getCoordinatorLayout() {
    return activityTestRule.getActivity().mCoordinatorLayout;
  }
}
