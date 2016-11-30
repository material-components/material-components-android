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

import android.support.annotation.NonNull;
import android.support.design.test.R;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.v4.widget.NestedScrollView;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

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
        // Avoid a deadlock (b/26160710)
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
        // Avoid a deadlock (b/26160710)
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
        // Avoid a deadlock (b/26160710)
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
                .perform(new GeneralSwipeAction(Swipe.FAST,
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
        // Avoid a deadlock (b/26160710)
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
        // TODO: Move this to setUp() when b/26160710 is fixed
        mCallback = new Callback(getBehavior());
        Espresso.registerIdlingResources(mCallback);
    }

    private void unregisterIdlingResourceCallback() {
        if (mCallback != null) {
            Espresso.unregisterIdlingResources(mCallback);
            mCallback = null;
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
