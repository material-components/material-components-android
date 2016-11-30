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


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import android.os.SystemClock;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.test.R;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.MotionEvents;
import android.support.test.espresso.action.PrecisionDescriber;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.core.deps.guava.base.Preconditions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

public class BottomSheetBehaviorTest extends
        BaseInstrumentationTestCase<BottomSheetBehaviorActivity> {

    public static class Callback extends BottomSheetBehavior.BottomSheetCallback
            implements IdlingResource {

        private boolean mIsIdle;

        private IdlingResource.ResourceCallback mResourceCallback;

        public Callback(BottomSheetBehavior behavior) {
            behavior.setBottomSheetCallback(this);
            int state = behavior.getState();
            mIsIdle = isIdleState(state);
        }

        @Override
        public void onStateChanged(@NonNull View bottomSheet,
                @BottomSheetBehavior.State int newState) {
            boolean wasIdle = mIsIdle;
            mIsIdle = isIdleState(newState);
            if (!wasIdle && mIsIdle && mResourceCallback != null) {
                mResourceCallback.onTransitionToIdle();
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
            return mIsIdle;
        }

        @Override
        public void registerIdleTransitionCallback(IdlingResource.ResourceCallback callback) {
            mResourceCallback = callback;
        }

        private boolean isIdleState(int state) {
            return state != BottomSheetBehavior.STATE_DRAGGING &&
                    state != BottomSheetBehavior.STATE_SETTLING;
        }

    }

    /**
     * Wait for a FAB to change its visibility (either shown or hidden).
     */
    private static class OnVisibilityChangedListener extends
            FloatingActionButton.OnVisibilityChangedListener implements IdlingResource {

        private final boolean mShown;
        private boolean mIsIdle;
        private ResourceCallback mResourceCallback;

        OnVisibilityChangedListener(boolean shown) {
            mShown = shown;
        }

        private void transitionToIdle() {
            if (!mIsIdle) {
                mIsIdle = true;
                if (mResourceCallback != null) {
                    mResourceCallback.onTransitionToIdle();
                }
            }
        }

        @Override
        public void onShown(FloatingActionButton fab) {
            if (mShown) {
                transitionToIdle();
            }
        }

        @Override
        public void onHidden(FloatingActionButton fab) {
            if (!mShown) {
                transitionToIdle();
            }
        }

        @Override
        public String getName() {
            return OnVisibilityChangedListener.class.getSimpleName();
        }

        @Override
        public boolean isIdleNow() {
            return mIsIdle;
        }

        @Override
        public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
            mResourceCallback = resourceCallback;
        }
    }

    /**
     * This is like {@link GeneralSwipeAction}, but it does not send ACTION_UP at the end.
     */
    private static class DragAction implements ViewAction {

        private static final int STEPS = 10;
        private static final int DURATION = 100;

        private final CoordinatesProvider mStart;
        private final CoordinatesProvider mEnd;
        private final PrecisionDescriber mPrecisionDescriber;

        public DragAction(CoordinatesProvider start, CoordinatesProvider end,
                PrecisionDescriber precisionDescriber) {
            mStart = start;
            mEnd = end;
            mPrecisionDescriber = precisionDescriber;
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
            float[] precision = mPrecisionDescriber.describePrecision();
            float[] start = mStart.calculateCoordinates(view);
            float[] end = mEnd.calculateCoordinates(view);
            float[][] steps = interpolate(start, end, STEPS);
            int delayBetweenMovements = DURATION / steps.length;
            // Down
            MotionEvent downEvent = MotionEvents.sendDown(uiController, start, precision).down;
            try {
                for (int i = 0; i < steps.length; i++) {
                    // Wait
                    long desiredTime = downEvent.getDownTime() + (long)(delayBetweenMovements * i);
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
            Preconditions.checkElementIndex(1, start.length);
            Preconditions.checkElementIndex(1, end.length);
            float[][] res = new float[steps][2];
            for(int i = 1; i < steps + 1; ++i) {
                res[i - 1][0] = start[0] + (end[0] - start[0]) * (float)i / ((float)steps + 2.0F);
                res[i - 1][1] = start[1] + (end[1] - start[1]) * (float)i / ((float)steps + 2.0F);
            }
            return res;
        }
    }

    private static class AddViewAction implements ViewAction {

        private final int mLayout;

        public AddViewAction(@LayoutRes int layout) {
            mLayout = layout;
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
            View child = LayoutInflater.from(view.getContext()).inflate(mLayout, parent, false);
            parent.addView(child);
        }
    }

    private Callback mCallback;

    public BottomSheetBehaviorTest() {
        super(BottomSheetBehaviorActivity.class);
    }

    @Test
    @SmallTest
    public void testInitialSetup() {
        BottomSheetBehavior behavior = getBehavior();
        assertThat(behavior.getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
        CoordinatorLayout coordinatorLayout = getCoordinatorLayout();
        ViewGroup bottomSheet = getBottomSheet();
        assertThat(bottomSheet.getTop(),
                is(coordinatorLayout.getHeight() - behavior.getPeekHeight()));
    }

    @Test
    @MediumTest
    public void testSetStateExpandedToCollapsed() {
        checkSetState(BottomSheetBehavior.STATE_EXPANDED, ViewMatchers.isDisplayed());
        checkSetState(BottomSheetBehavior.STATE_COLLAPSED, ViewMatchers.isDisplayed());
    }

    @Test
    @MediumTest
    public void testSetStateHiddenToCollapsed() {
        checkSetState(BottomSheetBehavior.STATE_HIDDEN, not(ViewMatchers.isDisplayed()));
        checkSetState(BottomSheetBehavior.STATE_COLLAPSED, ViewMatchers.isDisplayed());
    }

    @Test
    @MediumTest
    public void testSetStateCollapsedToCollapsed() {
        checkSetState(BottomSheetBehavior.STATE_COLLAPSED, ViewMatchers.isDisplayed());
    }

    @Test
    @MediumTest
    public void testSwipeDownToCollapse() {
        checkSetState(BottomSheetBehavior.STATE_EXPANDED, ViewMatchers.isDisplayed());
        Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
                .perform(DesignViewActions.withCustomConstraints(new GeneralSwipeAction(
                        Swipe.FAST,
                        // Manually calculate the starting coordinates to make sure that the touch
                        // actually falls onto the view on Gingerbread
                        new CoordinatesProvider() {
                            @Override
                            public float[] calculateCoordinates(View view) {
                                int[] location = new int[2];
                                view.getLocationInWindow(location);
                                return new float[]{
                                        view.getWidth() / 2,
                                        location[1] + 1
                                };
                            }
                        },
                        // Manually calculate the ending coordinates to make sure that the bottom
                        // sheet is collapsed, not hidden
                        new CoordinatesProvider() {
                            @Override
                            public float[] calculateCoordinates(View view) {
                                BottomSheetBehavior behavior = getBehavior();
                                return new float[]{
                                        // x: center of the bottom sheet
                                        view.getWidth() / 2,
                                        // y: just above the peek height
                                        view.getHeight() - behavior.getPeekHeight()};
                            }
                        }, Press.FINGER), ViewMatchers.isDisplayingAtLeast(5)));
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
    public void testSwipeDownToHide() {
        Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
                .perform(DesignViewActions.withCustomConstraints(ViewActions.swipeDown(),
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
    public void testSkipCollapsed() {
        getBehavior().setSkipCollapsed(true);
        checkSetState(BottomSheetBehavior.STATE_EXPANDED, ViewMatchers.isDisplayed());
        Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
                .perform(DesignViewActions.withCustomConstraints(new GeneralSwipeAction(
                        Swipe.FAST,
                        // Manually calculate the starting coordinates to make sure that the touch
                        // actually falls onto the view on Gingerbread
                        new CoordinatesProvider() {
                            @Override
                            public float[] calculateCoordinates(View view) {
                                int[] location = new int[2];
                                view.getLocationInWindow(location);
                                return new float[]{
                                        view.getWidth() / 2,
                                        location[1] + 1
                                };
                            }
                        },
                        // Manually calculate the ending coordinates to make sure that the bottom
                        // sheet is collapsed, not hidden
                        new CoordinatesProvider() {
                            @Override
                            public float[] calculateCoordinates(View view) {
                                BottomSheetBehavior behavior = getBehavior();
                                return new float[]{
                                        // x: center of the bottom sheet
                                        view.getWidth() / 2,
                                        // y: just above the peek height
                                        view.getHeight() - behavior.getPeekHeight()};
                            }
                        }, Press.FINGER), ViewMatchers.isDisplayingAtLeast(5)));
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
    public void testSwipeUpToExpand() {
        Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
                .perform(DesignViewActions.withCustomConstraints(
                        new GeneralSwipeAction(Swipe.FAST,
                                GeneralLocation.VISIBLE_CENTER, new CoordinatesProvider() {
                            @Override
                            public float[] calculateCoordinates(View view) {
                                return new float[]{view.getWidth() / 2, 0};
                            }
                        }, Press.FINGER),
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
    public void testInvisible() {
        // Make the bottomsheet invisible
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                getBottomSheet().setVisibility(View.INVISIBLE);
                assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
            }
        });
        // Swipe up as if to expand it
        Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
                .perform(DesignViewActions.withCustomConstraints(
                        new GeneralSwipeAction(Swipe.FAST,
                                GeneralLocation.VISIBLE_CENTER, new CoordinatesProvider() {
                            @Override
                            public float[] calculateCoordinates(View view) {
                                return new float[]{view.getWidth() / 2, 0};
                            }
                        }, Press.FINGER),
                        not(ViewMatchers.isDisplayed())));
        // Check that the bottom sheet stays the same collapsed state
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
            }
        });
    }

    @Test
    @MediumTest
    public void testInvisibleThenVisible() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                // The bottom sheet is initially invisible
                getBottomSheet().setVisibility(View.INVISIBLE);
                // Then it becomes visible when the CoL is touched
                getCoordinatorLayout().setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent e) {
                        if (e.getAction() == MotionEvent.ACTION_DOWN) {
                            getBottomSheet().setVisibility(View.VISIBLE);
                            return true;
                        }
                        return false;
                    }
                });
                assertThat(getBehavior().getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
            }
        });
        // Drag over the CoL
        Espresso.onView(ViewMatchers.withId(R.id.coordinator))
                // Drag (and not release)
                .perform(new DragAction(
                        GeneralLocation.BOTTOM_CENTER,
                        GeneralLocation.TOP_CENTER,
                        Press.FINGER))
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException e) {
                        // The bottom sheet should not react to the touch events
                        assertThat(getBottomSheet(), is(ViewMatchers.isDisplayed()));
                        assertThat(getBehavior().getState(),
                                is(BottomSheetBehavior.STATE_COLLAPSED));
                    }
                });
    }

    @Test
    @MediumTest
    public void testNestedScroll() {
        final ViewGroup bottomSheet = getBottomSheet();
        final BottomSheetBehavior behavior = getBehavior();
        final NestedScrollView scroll = new NestedScrollView(mActivityTestRule.getActivity());
        // Set up nested scrolling area
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                bottomSheet.addView(scroll, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                TextView view = new TextView(mActivityTestRule.getActivity());
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 500; ++i) {
                    sb.append("It is fine today. ");
                }
                view.setText(sb);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Do nothing
                    }
                });
                scroll.addView(view);
                assertThat(behavior.getState(), is(BottomSheetBehavior.STATE_COLLAPSED));
                // The scroll offset is 0 at first
                assertThat(scroll.getScrollY(), is(0));
            }
        });
        // Swipe from the very bottom of the bottom sheet to the top edge of the screen so that the
        // scrolling content is also scrolled
        Espresso.onView(ViewMatchers.withId(R.id.coordinator))
                .perform(new GeneralSwipeAction(Swipe.SLOW,
                        new CoordinatesProvider() {
                            @Override
                            public float[] calculateCoordinates(View view) {
                                return new float[]{view.getWidth() / 2, view.getHeight() - 1};
                            }
                        },
                        new CoordinatesProvider() {
                            @Override
                            public float[] calculateCoordinates(View view) {
                                return new float[]{view.getWidth() / 2, 1};
                            }
                        }, Press.FINGER));
        registerIdlingResourceCallback();
        try {
            Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
            InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
                @Override
                public void run() {
                    assertThat(behavior.getState(), is(BottomSheetBehavior.STATE_EXPANDED));
                    // This confirms that the nested scrolling area was scrolled continuously after
                    // the bottom sheet is expanded.
                    assertThat(scroll.getScrollY(), is(not(0)));
                }
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
                .perform(DesignViewActions.withCustomConstraints(
                        new GeneralSwipeAction(Swipe.FAST,
                                // Just above the bottom sheet
                                new CoordinatesProvider() {
                                    @Override
                                    public float[] calculateCoordinates(View view) {
                                        return new float[]{
                                                view.getWidth() / 2,
                                                view.getHeight() - getBehavior().getPeekHeight() - 9
                                        };
                                    }
                                },
                                // Top of the CoordinatorLayout
                                new CoordinatesProvider() {
                                    @Override
                                    public float[] calculateCoordinates(View view) {
                                        return new float[]{view.getWidth() / 2, 1};
                                    }
                                }, Press.FINGER),
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
                .perform(new DragAction(
                        GeneralLocation.VISIBLE_CENTER,
                        GeneralLocation.TOP_CENTER,
                        Press.FINGER))
                // Check that the bottom sheet is in STATE_DRAGGING
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException e) {
                        assertThat(view, is(ViewMatchers.isDisplayed()));
                        BottomSheetBehavior behavior = BottomSheetBehavior.from(view);
                        assertThat(behavior.getState(), is(BottomSheetBehavior.STATE_DRAGGING));
                    }
                })
                // Add a new view
                .perform(new AddViewAction(R.layout.frame_layout))
                // Check that the newly added view is properly laid out
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException e) {
                        ViewGroup parent = (ViewGroup) view;
                        assertThat(parent.getChildCount(), is(1));
                        View child = parent.getChildAt(0);
                        assertThat(ViewCompat.isLaidOut(child), is(true));
                    }
                });
    }

    @Test
    public void testFabVisibility() {
        withFabVisibilityChange(false, new Runnable() {
            @Override
            public void run() {
                checkSetState(BottomSheetBehavior.STATE_EXPANDED, ViewMatchers.isDisplayed());
            }
        });
        withFabVisibilityChange(true, new Runnable() {
            @Override
            public void run() {
                checkSetState(BottomSheetBehavior.STATE_COLLAPSED, ViewMatchers.isDisplayed());
            }
        });
    }

    @Test
    public void testAutoPeekHeight() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                getBehavior().setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
            }
        });
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                CoordinatorLayout col = getCoordinatorLayout();
                assertThat(getBottomSheet().getTop(),
                        is(Math.min(col.getWidth() * 9 / 16,
                                col.getHeight() - getBehavior().getPeekHeightMin())));
            }
        });
    }

    @Test
    @MediumTest
    public void testAutoPeekHeightHide() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                getBehavior().setHideable(true);
                getBehavior().setPeekHeight(0);
                getBehavior().setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
            }
        });
        checkSetState(BottomSheetBehavior.STATE_HIDDEN, not(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testDynamicContent() {
        registerIdlingResourceCallback();
        try {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
                @Override
                public void run() {
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
                }
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

    private void checkSetState(final int state, Matcher<View> matcher) {
        registerIdlingResourceCallback();
        try {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
                @Override
                public void run() {
                    getBehavior().setState(state);
                }
            });
            Espresso.onView(ViewMatchers.withId(R.id.bottom_sheet))
                    .check(ViewAssertions.matches(matcher));
            assertThat(getBehavior().getState(), is(state));
        } finally {
            unregisterIdlingResourceCallback();
        }
    }

    private void registerIdlingResourceCallback() {
        // This cannot be done in setUp(), or swiping action cannot be executed.
        mCallback = new Callback(getBehavior());
        Espresso.registerIdlingResources(mCallback);
    }

    private void unregisterIdlingResourceCallback() {
        if (mCallback != null) {
            Espresso.unregisterIdlingResources(mCallback);
            mCallback = null;
        }
    }

    private void withFabVisibilityChange(boolean shown, Runnable action) {
        OnVisibilityChangedListener listener = new OnVisibilityChangedListener(shown);
        CoordinatorLayout.LayoutParams lp =
                (CoordinatorLayout.LayoutParams) mActivityTestRule.getActivity().mFab
                        .getLayoutParams();
        FloatingActionButton.Behavior behavior = (FloatingActionButton.Behavior) lp.getBehavior();
        behavior.setInternalAutoHideListener(listener);
        Espresso.registerIdlingResources(listener);
        try {
            action.run();
        } finally {
            Espresso.unregisterIdlingResources(listener);
        }
    }

    private ViewGroup getBottomSheet() {
        return mActivityTestRule.getActivity().mBottomSheet;
    }

    private BottomSheetBehavior getBehavior() {
        return mActivityTestRule.getActivity().mBehavior;
    }

    private CoordinatorLayout getCoordinatorLayout() {
        return mActivityTestRule.getActivity().mCoordinatorLayout;
    }

}
