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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.app.Instrumentation;
import android.support.design.testutils.CoordinatorLayoutUtils;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.Gravity;
import android.view.View;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@MediumTest
public class CoordinatorLayoutTest extends BaseInstrumentationTestCase<CoordinatorLayoutActivity> {

    public CoordinatorLayoutTest() {
        super(CoordinatorLayoutActivity.class);
    }

    @Test
    @SdkSuppress(minSdkVersion = 21)
    public void testSetFitSystemWindows() {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        final CoordinatorLayout col = mActivityTestRule.getActivity().mCoordinatorLayout;
        final View view = new View(col.getContext());

        // Create a mock which calls the default impl of onApplyWindowInsets()
        final CoordinatorLayout.Behavior<View> mockBehavior =
                mock(CoordinatorLayout.Behavior.class);
        doCallRealMethod().when(mockBehavior)
                .onApplyWindowInsets(same(col), same(view), any(WindowInsetsCompat.class));

        // Assert that the CoL is currently not set to fitSystemWindows
        assertFalse(col.getFitsSystemWindows());

        // Now add a view with our mocked behavior to the CoordinatorLayout
        view.setFitsSystemWindows(true);
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                final CoordinatorLayout.LayoutParams lp = col.generateDefaultLayoutParams();
                lp.setBehavior(mockBehavior);
                col.addView(view, lp);
            }
        });
        instrumentation.waitForIdleSync();

        // Now request some insets and wait for the pass to happen
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                col.requestApplyInsets();
            }
        });
        instrumentation.waitForIdleSync();

        // Verify that onApplyWindowInsets() has not been called
        verify(mockBehavior, never())
                .onApplyWindowInsets(same(col), same(view), any(WindowInsetsCompat.class));

        // Now enable fits system windows and wait for a pass to happen
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                col.setFitsSystemWindows(true);
            }
        });
        instrumentation.waitForIdleSync();

        // Verify that onApplyWindowInsets() has been called with some insets
        verify(mockBehavior, atLeastOnce())
                .onApplyWindowInsets(same(col), same(view), any(WindowInsetsCompat.class));
    }

    @Test
    public void testInsetDependency() {
        final CoordinatorLayout col = mActivityTestRule.getActivity().mCoordinatorLayout;

        final CoordinatorLayout.LayoutParams lpInsetLeft = col.generateDefaultLayoutParams();
        lpInsetLeft.insetEdge = Gravity.LEFT;

        final CoordinatorLayout.LayoutParams lpInsetRight = col.generateDefaultLayoutParams();
        lpInsetRight.insetEdge = Gravity.RIGHT;

        final CoordinatorLayout.LayoutParams lpInsetTop = col.generateDefaultLayoutParams();
        lpInsetTop.insetEdge = Gravity.TOP;

        final CoordinatorLayout.LayoutParams lpInsetBottom = col.generateDefaultLayoutParams();
        lpInsetBottom.insetEdge = Gravity.BOTTOM;

        final CoordinatorLayout.LayoutParams lpDodgeLeft = col.generateDefaultLayoutParams();
        lpDodgeLeft.dodgeInsetEdges = Gravity.LEFT;

        final CoordinatorLayout.LayoutParams lpDodgeLeftAndTop = col.generateDefaultLayoutParams();
        lpDodgeLeftAndTop.dodgeInsetEdges = Gravity.LEFT | Gravity.TOP;

        final CoordinatorLayout.LayoutParams lpDodgeAll = col.generateDefaultLayoutParams();
        lpDodgeAll.dodgeInsetEdges = Gravity.FILL;

        final View a = new View(col.getContext());
        final View b = new View(col.getContext());

        assertThat(dependsOn(lpDodgeLeft, lpInsetLeft, col, a, b), is(true));
        assertThat(dependsOn(lpDodgeLeft, lpInsetRight, col, a, b), is(false));
        assertThat(dependsOn(lpDodgeLeft, lpInsetTop, col, a, b), is(false));
        assertThat(dependsOn(lpDodgeLeft, lpInsetBottom, col, a, b), is(false));

        assertThat(dependsOn(lpDodgeLeftAndTop, lpInsetLeft, col, a, b), is(true));
        assertThat(dependsOn(lpDodgeLeftAndTop, lpInsetRight, col, a, b), is(false));
        assertThat(dependsOn(lpDodgeLeftAndTop, lpInsetTop, col, a, b), is(true));
        assertThat(dependsOn(lpDodgeLeftAndTop, lpInsetBottom, col, a, b), is(false));

        assertThat(dependsOn(lpDodgeAll, lpInsetLeft, col, a, b), is(true));
        assertThat(dependsOn(lpDodgeAll, lpInsetRight, col, a, b), is(true));
        assertThat(dependsOn(lpDodgeAll, lpInsetTop, col, a, b), is(true));
        assertThat(dependsOn(lpDodgeAll, lpInsetBottom, col, a, b), is(true));

        assertThat(dependsOn(lpInsetLeft, lpDodgeLeft, col, a, b), is(false));
    }

    private static boolean dependsOn(CoordinatorLayout.LayoutParams lpChild,
            CoordinatorLayout.LayoutParams lpDependency, CoordinatorLayout col,
            View child, View dependency) {
        child.setLayoutParams(lpChild);
        dependency.setLayoutParams(lpDependency);
        return lpChild.dependsOn(col, child, dependency);
    }

    @Test
    public void testInsetEdge() {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        final CoordinatorLayout col = mActivityTestRule.getActivity().mCoordinatorLayout;

        final View insetView = new View(col.getContext());
        final View dodgeInsetView = new View(col.getContext());
        final AtomicInteger originalTop = new AtomicInteger();

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                CoordinatorLayout.LayoutParams lpInsetView = col.generateDefaultLayoutParams();
                lpInsetView.width = CoordinatorLayout.LayoutParams.MATCH_PARENT;
                lpInsetView.height = 100;
                lpInsetView.gravity = Gravity.TOP | Gravity.LEFT;
                lpInsetView.insetEdge = Gravity.TOP;
                col.addView(insetView, lpInsetView);
                insetView.setBackgroundColor(0xFF0000FF);

                CoordinatorLayout.LayoutParams lpDodgeInsetView = col.generateDefaultLayoutParams();
                lpDodgeInsetView.width = 100;
                lpDodgeInsetView.height = 100;
                lpDodgeInsetView.gravity = Gravity.TOP | Gravity.LEFT;
                lpDodgeInsetView.dodgeInsetEdges = Gravity.TOP;
                col.addView(dodgeInsetView, lpDodgeInsetView);
                dodgeInsetView.setBackgroundColor(0xFFFF0000);
            }
        });
        instrumentation.waitForIdleSync();
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                List<View> dependencies = col.getDependencies(dodgeInsetView);
                assertThat(dependencies.size(), is(1));
                assertThat(dependencies.get(0), is(insetView));

                // Move the insetting view
                originalTop.set(dodgeInsetView.getTop());
                assertThat(originalTop.get(), is(insetView.getBottom()));
                ViewCompat.offsetTopAndBottom(insetView, 123);
            }
        });
        instrumentation.waitForIdleSync();
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                // Confirm that the dodging view was moved by the same size
                assertThat(dodgeInsetView.getTop() - originalTop.get(), is(123));
            }
        });
    }

    @Test
    public void testDependentViewChanged() {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        final CoordinatorLayout col = mActivityTestRule.getActivity().mCoordinatorLayout;

        // Add two views, A & B, where B depends on A
        final View viewA = new View(col.getContext());
        final CoordinatorLayout.LayoutParams lpA = col.generateDefaultLayoutParams();
        lpA.width = 100;
        lpA.height = 100;

        final View viewB = new View(col.getContext());
        final CoordinatorLayout.LayoutParams lpB = col.generateDefaultLayoutParams();
        lpB.width = 100;
        lpB.height = 100;
        final CoordinatorLayout.Behavior behavior =
                spy(new CoordinatorLayoutUtils.DependentBehavior(viewA));
        lpB.setBehavior(behavior);

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                col.addView(viewA, lpA);
                col.addView(viewB, lpB);
            }
        });
        instrumentation.waitForIdleSync();

        // Reset the Behavior since onDependentViewChanged may have already been called as part of
        // any layout/draw passes already
        reset(behavior);

        // Now offset view A
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                ViewCompat.offsetLeftAndRight(viewA, 20);
                ViewCompat.offsetTopAndBottom(viewA, 20);
            }
        });
        instrumentation.waitForIdleSync();

        // And assert that view B's Behavior was called appropriately
        verify(behavior, times(1)).onDependentViewChanged(col, viewB, viewA);
    }

    @Test
    public void testDependentViewRemoved() {
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        final CoordinatorLayout col = mActivityTestRule.getActivity().mCoordinatorLayout;

        // Add two views, A & B, where B depends on A
        final View viewA = new View(col.getContext());
        final View viewB = new View(col.getContext());
        final CoordinatorLayout.LayoutParams lpB = col.generateDefaultLayoutParams();
        final CoordinatorLayout.Behavior behavior =
                spy(new CoordinatorLayoutUtils.DependentBehavior(viewA));
        lpB.setBehavior(behavior);

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                col.addView(viewA);
                col.addView(viewB, lpB);
            }
        });
        instrumentation.waitForIdleSync();

        // Now remove view A
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                col.removeView(viewA);
            }
        });

        // And assert that View B's Behavior was called appropriately
        verify(behavior, times(1)).onDependentViewRemoved(col, viewB, viewA);
    }
}
