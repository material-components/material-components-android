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
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.design.R;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * AppBarLayout is a vertical {@link LinearLayout} which implements many of the features of
 * Material Design's App bar concept, namely scrolling gestures.
 * <p>
 * Children should provide their desired scrolling behavior through
 * {@link LayoutParams#setScrollFlags(int)} and the associated layout xml attribute:
 * {@code app:layout_scrollFlags}.
 *
 * <p>
 * This view depends heavily on being used as a direct child within a {@link CoordinatorLayout}.
 * If you use AppBarLayout within a different {@link ViewGroup}, most of it's functionality will
 * not work.
 * <p>
 * AppBarLayout also requires a separate scrolling sibling in order to. The binding is done through
 * the {@link ScrollingViewBehavior} beahior class, meaning that you should set your scrolling
 * view's behavior to be an instance of {@link ScrollingViewBehavior}. A string resource containing
 * the full class name is available.
 *
 * <pre>
 * &lt;android.support.design.widget.CoordinatorLayout
 *         xmlns:android=&quot;http://schemas.android.com/apk/res/android&quot;
 *         xmlns:app=&quot;http://schemas.android.com/apk/res-auto&quot;
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;match_parent&quot;&gt;
 *
 *     &lt;android.support.v4.widget.NestedScrollView
 *             android:layout_width=&quot;match_parent&quot;
 *             android:layout_height=&quot;match_parent&quot;
 *             app:layout_behavior=&quot;@string/appbar_scrolling_view_behavior&quot;&gt;
 *
 *         &lt;!-- Your scrolling content --&gt;
 *
 *     &lt;/android.support.v4.widget.NestedScrollView&gt;
 *
 *     &lt;android.support.design.widget.AppBarLayout
 *             android:layout_height=&quot;wrap_content&quot;
 *             android:layout_width=&quot;match_parent&quot;&gt;
 *
 *         &lt;android.support.v7.widget.Toolbar
 *                 ...
 *                 app:layout_scrollFlags=&quot;scroll|enterAlways&quot;/&gt;
 *
 *         &lt;android.support.design.widget.TabLayout
 *                 ...
 *                 app:layout_scrollFlags=&quot;scroll|enterAlways&quot;/&gt;
 *
 *     &lt;/android.support.design.widget.AppBarLayout&gt;
 *
 * &lt;/android.support.design.widget.CoordinatorLayout&gt;
 * </pre>
 *
 * @see <a href="http://www.google.com/design/spec/layout/structure.html#structure-app-bar">
 *     http://www.google.com/design/spec/layout/structure.html#structure-app-bar</a>
 */
@CoordinatorLayout.DefaultBehavior(AppBarLayout.Behavior.class)
public class AppBarLayout extends LinearLayout {

    /**
     * Interface which allows an implementing child {@link View} of this {@link AppBarLayout} to
     * receive offset updates, and provide extra information.
     */
    public interface AppBarLayoutChild {

        /** @hide */
        @IntDef({
                STATE_ELEVATED_ABOVE,
                STATE_ELEVATED_INLINE
        })
        @Retention(RetentionPolicy.SOURCE)
        @interface ElevatedState {}

        /**
         * The {@link AppBarLayout} should be elevated above any scrolling content, and this cast
         * a shadow.
         *
         * @see #onOffsetUpdate(int)
         */
        int STATE_ELEVATED_ABOVE = 1;

        /**
         * The {@link AppBarLayout} should not be elevated above any scrolling content.
         *
         * @see #onOffsetUpdate(int)
         */
        int STATE_ELEVATED_INLINE = 0;

        /**
         * Called when the {@link AppBarLayout}'s layout offset has been changed. This allows
         * child views to implement custom behavior based on the offset (for instance pinning a
         * view at a certain y value).
         *
         * <p>You can influence the elevation of the {@link AppBarLayout} by returning one of
         * {@link #STATE_ELEVATED_INLINE} or {@link #STATE_ELEVATED_ABOVE}.
         *
         * @param verticalOffset the vertical offset for the parent {@link AppBarLayout}, in px
         *
         * @return one of {@link #STATE_ELEVATED_INLINE} or {@link #STATE_ELEVATED_ABOVE}.
         */
        @ElevatedState
        int onOffsetUpdate(int verticalOffset);
    }

    private static final int INVALID_SCROLL_RANGE = -1;

    private int mTotalScrollRange = INVALID_SCROLL_RANGE;
    private int mDownPreScrollRange = INVALID_SCROLL_RANGE;
    private int mDownScrollRange = INVALID_SCROLL_RANGE;

    boolean mHaveChildWithInterpolator;

    private float mTargetElevation;

    public AppBarLayout(Context context) {
        this(context, null);
    }

    public AppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AppBarLayout,
                0, R.style.Widget_Design_AppBarLayout);
        mTargetElevation = a.getDimensionPixelSize(R.styleable.AppBarLayout_elevation, 0);
        setBackgroundDrawable(a.getDrawable(R.styleable.AppBarLayout_android_background));
        a.recycle();

        // Use the bounds view outline provider so that we cast a shadow, even without a background
        ViewUtils.setBoundsViewOutlineProvider(this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        // Invalidate the scroll ranges
        mTotalScrollRange = INVALID_SCROLL_RANGE;
        mDownPreScrollRange = INVALID_SCROLL_RANGE;
        mDownPreScrollRange = INVALID_SCROLL_RANGE;

        mHaveChildWithInterpolator = false;
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            final LayoutParams childLp = (LayoutParams) child.getLayoutParams();
            final Interpolator interpolator = childLp.getScrollInterpolator();

            if (interpolator != null) {
                mHaveChildWithInterpolator = true;
                break;
            }
        }
    }

    @Override
    public void setOrientation(int orientation) {
        if (orientation != VERTICAL) {
            throw new IllegalArgumentException("AppBarLayout is always vertical and does"
                    + " not support horizontal orientation");
        }
        super.setOrientation(orientation);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof LinearLayout.LayoutParams) {
            return new LayoutParams((LinearLayout.LayoutParams) p);
        } else if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }

    final boolean hasChildWithInterpolator() {
        return mHaveChildWithInterpolator;
    }

    /**
     * Return the scroll range of any children
     *
     * @return the scroll range in px
     */
    final int getTotalScrollRange() {
        if (mTotalScrollRange != INVALID_SCROLL_RANGE) {
            return mTotalScrollRange;
        }

        int range = 0;
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int childHeight = ViewCompat.isLaidOut(child)
                    ? child.getHeight()
                    : child.getMeasuredHeight();
            final int flags = lp.mScrollFlags;

            if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
                // We're set to scroll so add the child's height
                range += childHeight;

                if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                    // For a collapsing scroll, we to take the collapsed height into account.
                    // We also return the range straight away since later views can't scroll beneath
                    // us
                    return range - ViewCompat.getMinimumHeight(child);
                }
            } else {
                // As soon as a view doesn't have the scroll flag, we end the range calculation.
                // This is because views below can not scroll under a fixed view.
                break;
            }
        }
        return mTotalScrollRange = range;
    }

    final boolean hasScrollableChildren() {
        return getTotalScrollRange() != 0;
    }

    /**
     * Return the scroll range when scrolling up from a nested pre-scroll.
     */
    final int getUpNestedPreScrollRange() {
        return getTotalScrollRange();
    }

    /**
     * Return the scroll range when scrolling down from a nested pre-scroll.
     */
    final int getDownNestedPreScrollRange() {
        if (mDownPreScrollRange != INVALID_SCROLL_RANGE) {
            // If we already have a valid value, return it
            return mDownPreScrollRange;
        }

        int range = 0;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int childHeight = ViewCompat.isLaidOut(child)
                    ? child.getHeight()
                    : child.getMeasuredHeight();
            final int flags = lp.mScrollFlags;

            if ((flags & LayoutParams.FLAG_QUICK_RETURN) == LayoutParams.FLAG_QUICK_RETURN) {
                // The view has the quick return flag combination...
                if ((flags & LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED) != 0) {
                    // If they're set to enter collapsed, use the minimum height
                    range += ViewCompat.getMinimumHeight(child);
                } else {
                    // Else use the full height
                    range += childHeight;
                }
            } else if (range > 0) {
                // If we've hit an non-quick return scrollable view, and we've already hit a
                // quick return view, return now
                break;
            }
        }
        return mDownPreScrollRange = range;
    }

    /**
     * Return the scroll range when scrolling down from a nested scroll.
     */
    final int getDownNestedScrollRange() {
        if (mDownScrollRange != INVALID_SCROLL_RANGE) {
            // If we already have a valid value, return it
            return mDownScrollRange;
        }

        int range = 0;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int childHeight = ViewCompat.isLaidOut(child)
                    ? child.getHeight()
                    : child.getMeasuredHeight();

            final int flags = lp.mScrollFlags;

            if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
                // We're set to scroll so add the child's height
                range += childHeight;

                if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                    // For a collapsing exit scroll, we to take the collapsed height into account.
                    // We also return the range straight away since later views can't scroll
                    // beneath us
                    return range - ViewCompat.getMinimumHeight(child);
                }
            } else {
                // As soon as a view doesn't have the scroll flag, we end the range calculation.
                // This is because views below can not scroll under a fixed view.
                break;
            }
        }
        return mDownScrollRange = range;
    }

    final int getMinimumHeightForVisibleOverlappingContent() {
        final int minHeight = ViewCompat.getMinimumHeight(this);
        if (minHeight != 0) {
            // If this layout has a min height, use it (doubled)
            return minHeight * 2;
        }

        // Otherwise, we'll use twice the min height of our last child
        final int childCount = getChildCount();
        return childCount >= 1
                ? ViewCompat.getMinimumHeight(getChildAt(childCount - 1)) * 2
                : 0;
    }

    /**
     * The elevation value to use when {@link AppBarLayout} is elevated above content.
     */
    final float getTargetElevation() {
        return mTargetElevation;
    }

    public static class LayoutParams extends LinearLayout.LayoutParams {

        /** @hide */
        @IntDef(flag=true, value={
                SCROLL_FLAG_SCROLL,
                SCROLL_FLAG_EXIT_UNTIL_COLLAPSED,
                SCROLL_FLAG_ENTER_ALWAYS,
                SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED
        })
        @Retention(RetentionPolicy.SOURCE)
        public @interface ScrollFlags {}

        /**
         * The view will be scroll in direct relation to scroll events. This flag needs to be
         * set for any of the other flags to take effect. If any sibling views
         * before this one do not have this flag, then this value has no effect.
         */
        public static final int SCROLL_FLAG_SCROLL = 0x1;

        /**
         * When exiting (scrolling off screen) the view will be scrolled until it is
         * 'collapsed'. The collapsed height is defined by the view's minimum height.
         *
         * @see ViewCompat#getMinimumHeight(View)
         * @see View#setMinimumHeight(int)
         */
        public static final int SCROLL_FLAG_EXIT_UNTIL_COLLAPSED = 0x2;

        /**
         * When entering (scrolling on screen) the view will scroll on any downwards
         * scroll event, regardless of whether the scrolling view is also scrolling. This
         * is commonly referred to as the 'quick return' pattern.
         */
        public static final int SCROLL_FLAG_ENTER_ALWAYS = 0x4;

        /**
         * An additional flag for 'enterAlways' which modifies the returning view to
         * only initially scroll back to it's collapsed height. Once the scrolling view has
         * reached the end of it's scroll range, the remainder of this view will be scrolled
         * into view. The collapsed height is defined by the view's minimum height.
         *
         * @see ViewCompat#getMinimumHeight(View)
         * @see View#setMinimumHeight(int)
         */
        public static final int SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED = 0x8;

        /**
         * Internal flag which allows quick checking of 'quick return'
         */
        static final int FLAG_QUICK_RETURN = SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS;

        int mScrollFlags = SCROLL_FLAG_SCROLL;
        Interpolator mScrollInterpolator;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.AppBarLayout_LayoutParams);
            mScrollFlags = a.getInt(R.styleable.AppBarLayout_LayoutParams_layout_scrollFlags, 0);
            if (a.hasValue(R.styleable.AppBarLayout_LayoutParams_layout_scrollInterpolator)) {
                int resId = a.getResourceId(
                        R.styleable.AppBarLayout_LayoutParams_layout_scrollInterpolator, 0);
                mScrollInterpolator = android.view.animation.AnimationUtils.loadInterpolator(
                        c, resId);
            }
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, float weight) {
            super(width, height, weight);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(LinearLayout.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            mScrollFlags = source.mScrollFlags;
            mScrollInterpolator = source.mScrollInterpolator;
        }

        /**
         * Set the scrolling flags.
         *
         * @param flags bitwise int of {@link #SCROLL_FLAG_SCROLL},
         *             {@link #SCROLL_FLAG_EXIT_UNTIL_COLLAPSED}, {@link #SCROLL_FLAG_ENTER_ALWAYS}
         *             and {@link #SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED}.
         *
         * @see #getScrollFlags()
         *
         * @attr ref android.support.design.R.styleable.AppBarLayout_LayoutParams_layout_scrollFlags
         */
        public void setScrollFlags(@ScrollFlags int flags) {
            mScrollFlags = flags;
        }

        /**
         * Returns the scrolling flags.
         *
         * @see #setScrollFlags(int)
         *
         * @attr ref android.support.design.R.styleable.AppBarLayout_LayoutParams_layout_scrollFlags
         */
        @ScrollFlags
        public int getScrollFlags() {
            return mScrollFlags;
        }

        /**
         * Set the interpolator to when scrolling the view associated with this
         * {@link LayoutParams}.
         *
         * @param interpolator the interpolator to use, or null to use normal 1-to-1 scrolling.
         *
         * @attr ref android.support.design.R.styleable.AppBarLayout_LayoutParams_layout_scrollInterpolator
         * @see #getScrollInterpolator()
         */
        public void setScrollInterpolator(Interpolator interpolator) {
            mScrollInterpolator = interpolator;
        }

        /**
         * Returns the {@link Interpolator} being used for scrolling the view associated with this
         * {@link LayoutParams}. Null indicates 'normal' 1-to-1 scrolling.
         *
         * @attr ref android.support.design.R.styleable.AppBarLayout_LayoutParams_layout_scrollInterpolator
         * @see #setScrollInterpolator(Interpolator)
         */
        public Interpolator getScrollInterpolator() {
            return mScrollInterpolator;
        }
    }

    /**
     * The default {@link Behavior} for {@link AppBarLayout}. Implements the necessary nested
     * scroll handling with offsetting.
     */
    public static class Behavior extends ViewOffsetBehavior<AppBarLayout> {
        private int mSiblingOffsetTop;

        private boolean mSkipNestedPreScroll;

        public Behavior() {}

        public Behavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child,
                View directTargetChild, View target, int nestedScrollAxes) {
            // Return true if we're nested scrolling vertically and we have scrollable children
            return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0
                    && child.hasScrollableChildren();
        }

        @Override
        public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child,
                View target, int dx, int dy, int[] consumed) {
            if (dy != 0 && !mSkipNestedPreScroll) {
                int min, max;
                if (dy < 0) {
                    // We're scrolling down
                    min = -child.getTotalScrollRange();
                    max = min + child.getDownNestedPreScrollRange();
                } else {
                    // We're scrolling up
                    min = -child.getUpNestedPreScrollRange();
                    max = 0;
                }
                consumed[1] = scroll(coordinatorLayout, child, dy, min, max);
            }
        }

        @Override
        public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child,
                View target, int dxConsumed, int dyConsumed,
                int dxUnconsumed, int dyUnconsumed) {
            if (dyUnconsumed < 0) {
                // If the scrolling view is scrolling down but not consuming, it's probably be at
                // the top of it's content
                scroll(coordinatorLayout, child, dyUnconsumed,
                        -child.getDownNestedScrollRange(), 0);
                // Set the expanding flag so that onNestedPreScroll doesn't handle any events
                mSkipNestedPreScroll = true;
            } else {
                // As we're no longer handling nested scrolls, reset the skip flag
                mSkipNestedPreScroll = false;
            }
        }

        @Override
        public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child,
                View target) {
            // Reset the skip flag
            mSkipNestedPreScroll = false;
        }

        @Override
        public boolean onLayoutChild(CoordinatorLayout parent, AppBarLayout child,
                int layoutDirection) {
            boolean handled = super.onLayoutChild(parent, child, layoutDirection);

            // Make sure we update the elevation
            final int elevationState = dispatchOffsetUpdates(child);
            checkElevation(child, getTopAndBottomOffset(), elevationState);

            return handled;
        }

        private int scroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout,
                int dy, int minOffset, int maxOffset) {
            return setAppBarTopBottomOffset(coordinatorLayout, appBarLayout,
                    mSiblingOffsetTop - dy, minOffset, maxOffset);
        }

        private int setAppBarTopBottomOffset(CoordinatorLayout coordinatorLayout,
                AppBarLayout appBarLayout, int newOffset, int minOffset, int maxOffset) {
            final int curOffset = mSiblingOffsetTop;
            int consumed = 0;

            if (minOffset != 0) {
                newOffset = MathUtils.constrain(newOffset, minOffset, maxOffset);

                if (curOffset != newOffset) {
                    boolean offsetChanged = setTopAndBottomOffset(
                            appBarLayout.hasChildWithInterpolator()
                                    ? interpolateOffset(appBarLayout, newOffset)
                                    : newOffset);
                    // Update how much dy we have consumed
                    consumed = curOffset - newOffset;
                    // Update the stored sibling offset
                    mSiblingOffsetTop = newOffset;

                    if (!offsetChanged && appBarLayout.hasChildWithInterpolator()) {
                        // If the offset hasn't changed and we're using an interpolated scroll
                        // then we need to keep any dependent views updated. CoL will do this for
                        // us when we move, but we need to do it manually when we don't (as an
                        // interpolated scroll may finish early).
                        coordinatorLayout.dispatchDependentViewsChanged(appBarLayout);
                    }

                    // Dispatch the updates to any AppBarLayoutChild children
                    final int childState = dispatchOffsetUpdates(appBarLayout);
                    checkElevation(appBarLayout, newOffset, childState);
                }
            }

            return consumed;
        }

        private void checkElevation(AppBarLayout appBarLayout, int offset, int childState) {
            if (appBarLayout.getHeight() + offset == 0) {
                // If we're not visible, clear out the elevation
                ViewCompat.setElevation(appBarLayout, 0f);
            } else {
                if (childState == AppBarLayoutChild.STATE_ELEVATED_ABOVE) {
                    ViewCompat.setElevation(appBarLayout, appBarLayout.getTargetElevation());
                } else {
                    ViewCompat.setElevation(appBarLayout, 0f);
                }
            }
        }

        private int dispatchOffsetUpdates(AppBarLayout layout) {
            for (int i = 0, z = layout.getChildCount(); i < z; i++) {
                View child = layout.getChildAt(i);
                if (child instanceof AppBarLayoutChild) {
                    final int childState = ((AppBarLayoutChild) child)
                            .onOffsetUpdate(getTopAndBottomOffset());

                    if (childState == AppBarLayoutChild.STATE_ELEVATED_INLINE) {
                        return childState;
                    }
                }
            }

            return AppBarLayoutChild.STATE_ELEVATED_ABOVE;
        }

        private int interpolateOffset(AppBarLayout layout, final int offset) {
            final int absOffset = Math.abs(offset);

            for (int i = 0, z = layout.getChildCount(); i < z; i++) {
                final View child = layout.getChildAt(i);
                final AppBarLayout.LayoutParams childLp = (LayoutParams) child.getLayoutParams();
                final Interpolator interpolator = childLp.getScrollInterpolator();

                if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
                    if (interpolator != null) {
                        int childScrollableHeight = 0;
                        final int flags = childLp.getScrollFlags();
                        if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
                            // We're set to scroll so add the child's height
                            childScrollableHeight += child.getHeight();
                            if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                                // For a collapsing scroll, we to take the collapsed height into account.
                                childScrollableHeight -= ViewCompat.getMinimumHeight(child);
                            }
                        }

                        if (childScrollableHeight > 0) {
                            final int offsetForView = absOffset - child.getTop();
                            final int interpolatedDiff = Math.round(childScrollableHeight *
                                    interpolator.getInterpolation(
                                            offsetForView / (float) childScrollableHeight));

                            return Integer.signum(offset) * (child.getTop() + interpolatedDiff);
                        }
                    }

                    // If we get to here then the view on the offset isn't suitable for interpolated
                    // scrolling. So break out of the loop
                    break;
                }
            }

            return offset;
        }

        final int getTopBottomOffsetForScrollingSibling() {
            return mSiblingOffsetTop;
        }
    }

    /**
     * Behavior which should be used by {@link View}s which can scroll vertically and support
     * nested scrolling to automatically scroll any {@link AppBarLayout} siblings.
     */
    public static class ScrollingViewBehavior extends ViewOffsetBehavior<View> {
        private int mOverlayTop;

        public ScrollingViewBehavior() {}

        public ScrollingViewBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);

            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.ScrollingViewBehavior_Params);
            mOverlayTop = a.getDimensionPixelSize(
                    R.styleable.ScrollingViewBehavior_Params_behavior_overlapTop, 0);
            a.recycle();
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
            // We depend on any AppBarLayouts
            return dependency instanceof AppBarLayout;
        }

        @Override
        public boolean onMeasureChild(CoordinatorLayout parent, View child,
                int parentWidthMeasureSpec, int widthUsed,
                int parentHeightMeasureSpec, int heightUsed) {
            if (child.getLayoutParams().height == LayoutParams.MATCH_PARENT) {
                // If the child's height is set to match_parent then it with it's maximum visible
                // visible height

                final List<View> dependencies = parent.getDependencies(child);
                if (dependencies.isEmpty()) {
                    // If we don't have any dependencies, return false
                    return false;
                }

                final AppBarLayout appBar = findFirstAppBarLayout(dependencies);
                if (appBar != null) {
                    if (appBar.getMeasuredWidth() == 0 || appBar.getMeasuredHeight() == 0) {
                        // If the AppBar hasn't been measured yet, we need to do it now
                        parent.onMeasureChild(appBar, parentWidthMeasureSpec,
                                widthUsed, parentHeightMeasureSpec, heightUsed);
                    }

                    final int scrollRange = appBar.getTotalScrollRange();
                    final int height = MeasureSpec.getSize(parentHeightMeasureSpec)
                            - appBar.getMeasuredHeight() + scrollRange;
                    final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                            MeasureSpec.AT_MOST);

                    // Now measure the scrolling child with the correct height
                    parent.onMeasureChild(child, parentWidthMeasureSpec,
                            widthUsed, heightMeasureSpec, heightUsed);

                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, View child,
                View dependency) {
            final CoordinatorLayout.Behavior behavior =
                    ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();
            if (behavior instanceof Behavior) {
                // Offset the child so that it is below the app-bar (with any overlap)

                final int appBarOffset = ((Behavior) behavior)
                        .getTopBottomOffsetForScrollingSibling();
                final int expandedMax = dependency.getHeight() - mOverlayTop;
                final int collapsedMin = parent.getHeight() - child.getHeight();

                if (mOverlayTop != 0 && dependency instanceof AppBarLayout) {
                    // If we have an overlap top, and the dependency is an AppBarLayout, we control
                    // the offset ourselves based on the appbar's scroll progress. This is so that
                    // the scroll happens sequentially rather than linearly
                    final int scrollRange = ((AppBarLayout) dependency).getTotalScrollRange();
                    setTopAndBottomOffset(AnimationUtils.lerp(expandedMax, collapsedMin,
                            Math.abs(appBarOffset) / (float) scrollRange));
                } else {
                    setTopAndBottomOffset(MathUtils.constrain(
                            dependency.getHeight() - mOverlayTop + appBarOffset,
                            collapsedMin, expandedMax));
                }
            }
            return false;
        }

        /**
         * Set the distance that this view should overlap any {@link AppBarLayout}.
         *
         * @param overlayTop the distance in px
         *
         * @attr ref android.support.design.R.styleable.ScrollingViewBehavior_LayoutParams_layout_overlapTop
         */
        public void setOverlayTop(int overlayTop) {
            mOverlayTop = overlayTop;
        }

        /**
         * Returns the distance that this view should overlap any {@link AppBarLayout}.
         *
         * @attr ref android.support.design.R.styleable.ScrollingViewBehavior_LayoutParams_layout_overlapTop
         */
        public int getOverlayTop() {
            return mOverlayTop;
        }

        private static AppBarLayout findFirstAppBarLayout(List<View> views) {
            for (int i = 0, z = views.size(); i < z; i++) {
                View view = views.get(i);
                if (view instanceof AppBarLayout) {
                    return (AppBarLayout) view;
                }
            }
            return null;
        }
    }
}
