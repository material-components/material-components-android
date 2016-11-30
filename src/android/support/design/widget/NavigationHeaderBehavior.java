/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.content.Context;
import android.support.design.R;
import android.support.design.widget.CoordinatorLayout.Behavior;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

/**
 * The {@link Behavior} for the {@link NavigationView} header. This handles scrolling the
 * header view with the list that displays the menu in {@link NavigationView}.
 */
public final class NavigationHeaderBehavior extends HeaderBehavior<View> {

    public NavigationHeaderBehavior() {
        super();
    }

    public NavigationHeaderBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View header, View dependency) {
        return dependency instanceof RecyclerView;
    }

    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View header,
            View directTargetChild, View target, int nestedScrollAxes) {
        // Return true if we're nested scrolling vertically, and the scrolling view is big enough
        // to scroll.
        boolean canTargetScroll = ViewCompat.canScrollVertically(target, -1)
                || ViewCompat.canScrollVertically(target,1);
        final boolean started = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0
                && coordinatorLayout.getHeight() - target.getHeight() <= header.getHeight()
                && canTargetScroll;
        return started;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View header, View target,
            int dx, int dy, int[] consumed) {
        if (dy > 0) {
            // We're scrolling up
            consumed[1] = scroll(coordinatorLayout, header, dy, -header.getMeasuredHeight(), 0);
        }
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View header, View target,
            int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (dyUnconsumed < 0) {
            // If the scrolling view is scrolling down but not consuming, it's probably at
            // the top of it's content, scroll the header down.
            scroll(coordinatorLayout, header, dyUnconsumed, -header.getMeasuredHeight(), 0);
        }
    }

    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, View header, View target,
            float velocityX, float velocityY, boolean consumed) {
        if (!consumed) {
            // It hasn't been consumed so let's fling ourselves
            return fling(coordinatorLayout, header, -header.getMeasuredHeight(), 0, -velocityY);
        }
        return false;
    }

    /**
     * Defines the behavior for the scrolling view used to back {@link NavigationView}.
     */
    public static class MenuViewBehavior extends HeaderScrollingViewBehavior {

        public MenuViewBehavior() {
        }

        public MenuViewBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, View menu, View dependency) {
            // We depend on the header container
            return (dependency.getId() == R.id.navigation_header_container);
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, View menu,
                View dependency) {
            final CoordinatorLayout.Behavior behavior =
                    ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();
            if (behavior instanceof NavigationHeaderBehavior) {
                // Offset the menu so that it is below the header.
                final int headerOffset = ((NavigationHeaderBehavior) behavior)
                        .getTopAndBottomOffset();
                setTopAndBottomOffset(dependency.getHeight() + headerOffset);
            }
            return false;
        }

        @Override
        protected View findFirstDependency(List<View> views) {
            for (int i = 0, z = views.size(); i < z; i++) {
                View view = views.get(i);
                if (view.getId() == R.id.navigation_header_container) {
                    return view;
                }
            }
            return null;
        }
    }
}