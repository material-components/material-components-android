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
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.design.R;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * AppBarLayout is a vertical {@link LinearLayout} which implements many of the features of
 * material designs app bar concept, namely scrolling gestures.
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
 * AppBarLayout also requires a separate scrolling sibling in order to know when to scroll.
 * The binding is done through the {@link ScrollingViewBehavior} behavior class, meaning that you
 * should set your scrolling view's behavior to be an instance of {@link ScrollingViewBehavior}.
 * A string resource containing the full class name is available.
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

    private static final int PENDING_ACTION_NONE = 0x0;
    private static final int PENDING_ACTION_EXPANDED = 0x1;
    private static final int PENDING_ACTION_COLLAPSED = 0x2;
    private static final int PENDING_ACTION_ANIMATE_ENABLED = 0x4;

    /**
     * Interface definition for a callback to be invoked when an {@link AppBarLayout}'s vertical
     * offset changes.
     */
    public interface OnOffsetChangedListener {
        /**
         * Called when the {@link AppBarLayout}'s layout offset has been changed. This allows
         * child views to implement custom behavior based on the offset (for instance pinning a
         * view at a certain y value).
         *
         * @param appBarLayout the {@link AppBarLayout} which offset has changed
         * @param verticalOffset the vertical offset for the parent {@link AppBarLayout}, in px
         */
        void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset);
    }

    private static final int INVALID_SCROLL_RANGE = -1;

    private int mTotalScrollRange = INVALID_SCROLL_RANGE;
    private int mDownPreScrollRange = INVALID_SCROLL_RANGE;
    private int mDownScrollRange = INVALID_SCROLL_RANGE;

    boolean mHaveChildWithInterpolator;

    private float mTargetElevation;

    private int mPendingAction = PENDING_ACTION_NONE;

    private WindowInsetsCompat mLastInsets;

    private final List<OnOffsetChangedListener> mListeners;

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
        if (a.hasValue(R.styleable.AppBarLayout_expanded)) {
            setExpanded(a.getBoolean(R.styleable.AppBarLayout_expanded, false));
        }
        a.recycle();

        // Use the bounds view outline provider so that we cast a shadow, even without a background
        ViewUtils.setBoundsViewOutlineProvider(this);

        mListeners = new ArrayList<>();

        ViewCompat.setElevation(this, mTargetElevation);

        ViewCompat.setOnApplyWindowInsetsListener(this,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                            WindowInsetsCompat insets) {
                        setWindowInsets(insets);
                        return insets.consumeSystemWindowInsets();
                    }
                });
    }

    /**
     * Add a listener that will be called when the offset of this {@link AppBarLayout} changes.
     *
     * @param listener The listener that will be called when the offset changes.]
     *
     * @see #removeOnOffsetChangedListener(OnOffsetChangedListener)
     */
    public void addOnOffsetChangedListener(OnOffsetChangedListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    /**
     * Remove the previously added {@link OnOffsetChangedListener}.
     *
     * @param listener the listener to remove.
     */
    public void removeOnOffsetChangedListener(OnOffsetChangedListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
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

    /**
     * Sets whether this {@link AppBarLayout} is expanded or not, animating if it has already
     * been laid out.
     *
     * <p>As with {@link AppBarLayout}'s scrolling, this method relies on this layout being a
     * direct child of a {@link CoordinatorLayout}.</p>
     *
     * @param expanded true if the layout should be fully expanded, false if it should
     *                 be fully collapsed
     *
     * @attr ref android.support.design.R.styleable#AppBarLayout_expanded
     */
    public void setExpanded(boolean expanded) {
        setExpanded(expanded, ViewCompat.isLaidOut(this));
    }

    /**
     * Sets whether this {@link AppBarLayout} is expanded or not.
     *
     * <p>As with {@link AppBarLayout}'s scrolling, this method relies on this layout being a
     * direct child of a {@link CoordinatorLayout}.</p>
     *
     * @param expanded true if the layout should be fully expanded, false if it should
     *                 be fully collapsed
     * @param animate Whether to animate to the new state
     *
     * @attr ref android.support.design.R.styleable#AppBarLayout_expanded
     */
    public void setExpanded(boolean expanded, boolean animate) {
        mPendingAction = (expanded ? PENDING_ACTION_EXPANDED : PENDING_ACTION_COLLAPSED)
                | (animate ? PENDING_ACTION_ANIMATE_ENABLED : 0);
        requestLayout();
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
     * Returns the scroll range of all children.
     *
     * @return the scroll range in px
     */
    public final int getTotalScrollRange() {
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
                range += childHeight + lp.topMargin + lp.bottomMargin;

                if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                    // For a collapsing scroll, we to take the collapsed height into account.
                    // We also break straight away since later views can't scroll beneath
                    // us
                    range -= ViewCompat.getMinimumHeight(child);
                    break;
                }
            } else {
                // As soon as a view doesn't have the scroll flag, we end the range calculation.
                // This is because views below can not scroll under a fixed view.
                break;
            }
        }
        final int top = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
        return mTotalScrollRange = (range - top);
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
                // First take the margin into account
                range += lp.topMargin + lp.bottomMargin;
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
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int childHeight = ViewCompat.isLaidOut(child)
                    ? child.getHeight()
                    : child.getMeasuredHeight();
            childHeight += lp.topMargin + lp.bottomMargin;

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
        final int topInset = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
        final int minHeight = ViewCompat.getMinimumHeight(this);
        if (minHeight != 0) {
            // If this layout has a min height, use it (doubled)
            return (minHeight * 2) + topInset;
        }

        // Otherwise, we'll use twice the min height of our last child
        final int childCount = getChildCount();
        return childCount >= 1
                ? (ViewCompat.getMinimumHeight(getChildAt(childCount - 1)) * 2) + topInset
                : 0;
    }

    /**
     * Set the elevation value to use when this {@link AppBarLayout} should be elevated
     * above content.
     * <p>
     * This method does not do anything itself. A typical use for this method is called from within
     * an {@link OnOffsetChangedListener} when the offset has changed in such a way to require an
     * elevation change.
     *
     * @param elevation the elevation value to use.
     *
     * @see ViewCompat#setElevation(View, float)
     */
    public void setTargetElevation(float elevation) {
        mTargetElevation = elevation;
    }

    /**
     * Returns the elevation value to use when this {@link AppBarLayout} should be elevated
     * above content.
     */
    public float getTargetElevation() {
        return mTargetElevation;
    }

    int getPendingAction() {
        return mPendingAction;
    }

    void resetPendingAction() {
        mPendingAction = PENDING_ACTION_NONE;
    }

    private void setWindowInsets(WindowInsetsCompat insets) {
        // Invalidate the total scroll range...
        mTotalScrollRange = INVALID_SCROLL_RANGE;
        mLastInsets = insets;

        // Now dispatch them to our children
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            insets = ViewCompat.dispatchApplyWindowInsets(child, insets);
            if (insets.isConsumed()) {
                break;
            }
        }
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
         * @attr ref android.support.design.R.styleable#AppBarLayout_LayoutParams_layout_scrollFlags
         */
        public void setScrollFlags(@ScrollFlags int flags) {
            mScrollFlags = flags;
        }

        /**
         * Returns the scrolling flags.
         *
         * @see #setScrollFlags(int)
         *
         * @attr ref android.support.design.R.styleable#AppBarLayout_LayoutParams_layout_scrollFlags
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
         * @attr ref android.support.design.R.styleable#AppBarLayout_LayoutParams_layout_scrollInterpolator
         * @see #getScrollInterpolator()
         */
        public void setScrollInterpolator(Interpolator interpolator) {
            mScrollInterpolator = interpolator;
        }

        /**
         * Returns the {@link Interpolator} being used for scrolling the view associated with this
         * {@link LayoutParams}. Null indicates 'normal' 1-to-1 scrolling.
         *
         * @attr ref android.support.design.R.styleable#AppBarLayout_LayoutParams_layout_scrollInterpolator
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
        private static final int INVALID_POINTER = -1;
        private static final int INVALID_POSITION = -1;

        private int mOffsetDelta;

        private boolean mSkipNestedPreScroll;
        private Runnable mFlingRunnable;
        private ScrollerCompat mScroller;

        private ValueAnimatorCompat mAnimator;

        private int mOffsetToChildIndexOnLayout = INVALID_POSITION;
        private boolean mOffsetToChildIndexOnLayoutIsMinHeight;
        private float mOffsetToChildIndexOnLayoutPerc;

        private boolean mIsBeingDragged;
        private int mActivePointerId = INVALID_POINTER;
        private int mLastMotionY;
        private int mTouchSlop = -1;

        private WeakReference<View> mLastNestedScrollingChildRef;

        public Behavior() {}

        public Behavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onStartNestedScroll(CoordinatorLayout parent, AppBarLayout child,
                View directTargetChild, View target, int nestedScrollAxes) {
            // Return true if we're nested scrolling vertically, and we have scrollable children
            // and the scrolling view is big enough to scroll
            final boolean started = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0
                    && child.hasScrollableChildren()
                    && parent.getHeight() - directTargetChild.getHeight() <= child.getHeight();

            if (started && mAnimator != null) {
                // Cancel any offset animation
                mAnimator.cancel();
            }

            // A new nested scroll has started so clear out the previous ref
            mLastNestedScrollingChildRef = null;

            return started;
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
            // Keep a reference to the previous nested scrolling child
            mLastNestedScrollingChildRef = new WeakReference<>(target);
        }

        @Override
        public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child,
                MotionEvent ev) {
            if (mTouchSlop < 0) {
                mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
            }

            final int action = ev.getAction();

            // Shortcut since we're being dragged
            if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
                return true;
            }

            switch (MotionEventCompat.getActionMasked(ev)) {
                case MotionEvent.ACTION_MOVE: {
                    final int activePointerId = mActivePointerId;
                    if (activePointerId == INVALID_POINTER) {
                        // If we don't have a valid id, the touch down wasn't on content.
                        break;
                    }
                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
                    if (pointerIndex == -1) {
                        break;
                    }

                    final int y = (int) MotionEventCompat.getY(ev, pointerIndex);
                    final int yDiff = Math.abs(y - mLastMotionY);
                    if (yDiff > mTouchSlop) {
                        mIsBeingDragged = true;
                        mLastMotionY = y;
                    }
                    break;
                }

                case MotionEvent.ACTION_DOWN: {
                    mIsBeingDragged = false;
                    final int x = (int) ev.getX();
                    final int y = (int) ev.getY();
                    if (parent.isPointInChildBounds(child, x, y) && canDragAppBarLayout()) {
                        mLastMotionY = y;
                        mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    }
                    break;
                }

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mIsBeingDragged = false;
                    mActivePointerId = INVALID_POINTER;
                    break;
            }

            return mIsBeingDragged;
        }

        @Override
        public boolean onTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
            if (mTouchSlop < 0) {
                mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
            }

            int x = (int) ev.getX();
            int y = (int) ev.getY();

            switch (MotionEventCompat.getActionMasked(ev)) {
                case MotionEvent.ACTION_DOWN:
                    if (parent.isPointInChildBounds(child, x, y) && canDragAppBarLayout()) {
                        mLastMotionY = y;
                        mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    } else {
                        return false;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    final int activePointerIndex = MotionEventCompat.findPointerIndex(ev,
                            mActivePointerId);
                    if (activePointerIndex == -1) {
                        return false;
                    }

                    y = (int) MotionEventCompat.getY(ev, activePointerIndex);

                    int dy = mLastMotionY - y;
                    if (!mIsBeingDragged && Math.abs(dy) > mTouchSlop) {
                        mIsBeingDragged = true;
                        if (dy > 0) {
                            dy -= mTouchSlop;
                        } else {
                            dy += mTouchSlop;
                        }
                    }

                    if (mIsBeingDragged) {
                        mLastMotionY = y;
                        // We're being dragged so scroll the ABL
                        scroll(parent, child, dy, -child.getDownNestedScrollRange(), 0);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mIsBeingDragged = false;
                    mActivePointerId = INVALID_POINTER;
                    break;
            }

            return true;
        }

        @Override
        public boolean onNestedFling(final CoordinatorLayout coordinatorLayout,
                final AppBarLayout child, View target, float velocityX, float velocityY,
                boolean consumed) {
            if (!consumed) {
                // It has been consumed so let's fling ourselves
                return fling(coordinatorLayout, child, -child.getTotalScrollRange(), 0, -velocityY);
            } else {
                // If we're scrolling up and the child also consumed the fling. We'll fake scroll
                // upto our 'collapsed' offset
                int targetScroll;
                if (velocityY < 0) {
                    // We're scrolling down
                    targetScroll = -child.getTotalScrollRange()
                            + child.getDownNestedPreScrollRange();

                    if (getTopBottomOffsetForScrollingSibling() > targetScroll) {
                        // If we're currently expanded more than the target scroll, we'll return false
                        // now. This is so that we don't 'scroll' the wrong way.
                        return false;
                    }
                } else {
                    // We're scrolling up
                    targetScroll = -child.getUpNestedPreScrollRange();

                    if (getTopBottomOffsetForScrollingSibling() < targetScroll) {
                        // If we're currently expanded less than the target scroll, we'll return
                        // false now. This is so that we don't 'scroll' the wrong way.
                        return false;
                    }
                }

                if (getTopBottomOffsetForScrollingSibling() != targetScroll) {
                    animateOffsetTo(coordinatorLayout, child, targetScroll);
                    return true;
                }
            }

            return false;
        }

        private void animateOffsetTo(final CoordinatorLayout coordinatorLayout,
                final AppBarLayout child, int offset) {
            if (mAnimator == null) {
                mAnimator = ViewUtils.createAnimator();
                mAnimator.setInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);
                mAnimator.setUpdateListener(new ValueAnimatorCompat.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimatorCompat animator) {
                        setAppBarTopBottomOffset(coordinatorLayout, child,
                                animator.getAnimatedIntValue());
                    }
                });
            } else {
                mAnimator.cancel();
            }

            mAnimator.setIntValues(getTopBottomOffsetForScrollingSibling(), offset);
            mAnimator.start();
        }

        private boolean fling(CoordinatorLayout coordinatorLayout, AppBarLayout layout, int minOffset,
                int maxOffset, float velocityY) {
            if (mFlingRunnable != null) {
                layout.removeCallbacks(mFlingRunnable);
            }

            if (mScroller == null) {
                mScroller = ScrollerCompat.create(layout.getContext());
            }

            mScroller.fling(
                    0, getTopBottomOffsetForScrollingSibling(), // curr
                    0, Math.round(velocityY), // velocity.
                    0, 0, // x
                    minOffset, maxOffset); // y

            if (mScroller.computeScrollOffset()) {
                mFlingRunnable = new FlingRunnable(coordinatorLayout, layout);
                ViewCompat.postOnAnimation(layout, mFlingRunnable);
                return true;
            } else {
                mFlingRunnable = null;
                return false;
            }
        }

        private class FlingRunnable implements Runnable {
            private final CoordinatorLayout mParent;
            private final AppBarLayout mLayout;

            FlingRunnable(CoordinatorLayout parent, AppBarLayout layout) {
                mParent = parent;
                mLayout = layout;
            }

            @Override
            public void run() {
                if (mLayout != null && mScroller != null && mScroller.computeScrollOffset()) {
                    setAppBarTopBottomOffset(mParent, mLayout, mScroller.getCurrY());

                    // Post ourselves so that we run on the next animation
                    ViewCompat.postOnAnimation(mLayout, this);
                }
            }
        }

        @Override
        public boolean onLayoutChild(CoordinatorLayout parent, AppBarLayout abl,
                int layoutDirection) {
            boolean handled = super.onLayoutChild(parent, abl, layoutDirection);

            final int pendingAction = abl.getPendingAction();
            if (pendingAction != PENDING_ACTION_NONE) {
                final boolean animate = (pendingAction & PENDING_ACTION_ANIMATE_ENABLED) != 0;
                if ((pendingAction & PENDING_ACTION_COLLAPSED) != 0) {
                    final int offset = -abl.getUpNestedPreScrollRange();
                    if (animate) {
                        animateOffsetTo(parent, abl, offset);
                    } else {
                        setAppBarTopBottomOffset(parent, abl, offset);
                    }
                } else if ((pendingAction & PENDING_ACTION_EXPANDED) != 0) {
                    if (animate) {
                        animateOffsetTo(parent, abl, 0);
                    } else {
                        setAppBarTopBottomOffset(parent, abl, 0);
                    }
                }
                // Finally reset the pending state
                abl.resetPendingAction();
            } else if (mOffsetToChildIndexOnLayout >= 0) {
                View child = abl.getChildAt(mOffsetToChildIndexOnLayout);
                int offset = -child.getBottom();
                if (mOffsetToChildIndexOnLayoutIsMinHeight) {
                    offset += ViewCompat.getMinimumHeight(child);
                } else {
                    offset += Math.round(child.getHeight() * mOffsetToChildIndexOnLayoutPerc);
                }
                setTopAndBottomOffset(offset);
                mOffsetToChildIndexOnLayout = INVALID_POSITION;
            }

            // Make sure we update the elevation
            dispatchOffsetUpdates(abl);

            return handled;
        }

        private int scroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout,
                int dy, int minOffset, int maxOffset) {
            return setAppBarTopBottomOffset(coordinatorLayout, appBarLayout,
                    getTopBottomOffsetForScrollingSibling() - dy, minOffset, maxOffset);
        }

        private boolean canDragAppBarLayout() {
            if (mLastNestedScrollingChildRef != null) {
                final View view = mLastNestedScrollingChildRef.get();
                return view != null && view.isShown() && !ViewCompat.canScrollVertically(view, -1);
            }
            return false;
        }

        final int setAppBarTopBottomOffset(CoordinatorLayout coordinatorLayout,
                AppBarLayout appBarLayout, int newOffset) {
            return setAppBarTopBottomOffset(coordinatorLayout, appBarLayout, newOffset,
                    Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        final int setAppBarTopBottomOffset(CoordinatorLayout coordinatorLayout,
                AppBarLayout appBarLayout, int newOffset, int minOffset, int maxOffset) {
            final int curOffset = getTopBottomOffsetForScrollingSibling();
            int consumed = 0;

            if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
                // If we have some scrolling range, and we're currently within the min and max
                // offsets, calculate a new offset
                newOffset = MathUtils.constrain(newOffset, minOffset, maxOffset);

                if (curOffset != newOffset) {
                    final int interpolatedOffset = appBarLayout.hasChildWithInterpolator()
                            ? interpolateOffset(appBarLayout, newOffset)
                            : newOffset;

                    boolean offsetChanged = setTopAndBottomOffset(interpolatedOffset);

                    // Update how much dy we have consumed
                    consumed = curOffset - newOffset;
                    // Update the stored sibling offset
                    mOffsetDelta = newOffset - interpolatedOffset;

                    if (!offsetChanged && appBarLayout.hasChildWithInterpolator()) {
                        // If the offset hasn't changed and we're using an interpolated scroll
                        // then we need to keep any dependent views updated. CoL will do this for
                        // us when we move, but we need to do it manually when we don't (as an
                        // interpolated scroll may finish early).
                        coordinatorLayout.dispatchDependentViewsChanged(appBarLayout);
                    }

                    // Dispatch the updates to any listeners
                    dispatchOffsetUpdates(appBarLayout);
                }
            }

            return consumed;
        }

        private void dispatchOffsetUpdates(AppBarLayout layout) {
            final List<OnOffsetChangedListener> listeners = layout.mListeners;

            // Iterate backwards through the list so that most recently added listeners
            // get the first chance to decide
            for (int i = 0, z = listeners.size(); i < z; i++) {
                final OnOffsetChangedListener listener = listeners.get(i);
                if (listener != null) {
                    listener.onOffsetChanged(layout, getTopAndBottomOffset());
                }
            }
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
                            // We're set to scroll so add the child's height plus margin
                            childScrollableHeight += child.getHeight() + childLp.topMargin
                                    + childLp.bottomMargin;

                            if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                                // For a collapsing scroll, we to take the collapsed height
                                // into account.
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
            return getTopAndBottomOffset() + mOffsetDelta;
        }

        @Override
        public Parcelable onSaveInstanceState(CoordinatorLayout parent, AppBarLayout appBarLayout) {
            final Parcelable superState = super.onSaveInstanceState(parent, appBarLayout);
            final int offset = getTopAndBottomOffset();

            // Try and find the first visible child...
            for (int i = 0, count = appBarLayout.getChildCount(); i < count; i++) {
                View child = appBarLayout.getChildAt(i);
                final int visBottom = child.getBottom() + offset;

                if (child.getTop() + offset <= 0 && visBottom >= 0) {
                    final SavedState ss = new SavedState(superState);
                    ss.firstVisibleChildIndex = i;
                    ss.firstVisibileChildAtMinimumHeight =
                            visBottom == ViewCompat.getMinimumHeight(child);
                    ss.firstVisibileChildPercentageShown = visBottom / (float) child.getHeight();
                    return ss;
                }
            }

            // Else we'll just return the super state
            return superState;
        }

        @Override
        public void onRestoreInstanceState(CoordinatorLayout parent, AppBarLayout appBarLayout,
                Parcelable state) {
            if (state instanceof SavedState) {
                final SavedState ss = (SavedState) state;
                super.onRestoreInstanceState(parent, appBarLayout, ss.getSuperState());
                mOffsetToChildIndexOnLayout = ss.firstVisibleChildIndex;
                mOffsetToChildIndexOnLayoutPerc = ss.firstVisibileChildPercentageShown;
                mOffsetToChildIndexOnLayoutIsMinHeight = ss.firstVisibileChildAtMinimumHeight;
            } else {
                super.onRestoreInstanceState(parent, appBarLayout, state);
                mOffsetToChildIndexOnLayout = INVALID_POSITION;
            }
        }

        protected static class SavedState extends View.BaseSavedState {
            int firstVisibleChildIndex;
            float firstVisibileChildPercentageShown;
            boolean firstVisibileChildAtMinimumHeight;

            public SavedState(Parcel source) {
                super(source);
                firstVisibleChildIndex = source.readInt();
                firstVisibileChildPercentageShown = source.readFloat();
                firstVisibileChildAtMinimumHeight = source.readByte() != 0;
            }

            public SavedState(Parcelable superState) {
                super(superState);
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                super.writeToParcel(dest, flags);
                dest.writeInt(firstVisibleChildIndex);
                dest.writeFloat(firstVisibileChildPercentageShown);
                dest.writeByte((byte) (firstVisibileChildAtMinimumHeight ? 1 : 0));
            }

            public static final Parcelable.Creator<SavedState> CREATOR =
                    new Parcelable.Creator<SavedState>() {
                        @Override
                        public SavedState createFromParcel(Parcel source) {
                            return new SavedState(source);
                        }

                        @Override
                        public SavedState[] newArray(int size) {
                            return new SavedState[size];
                        }
                    };
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
            final int childLpHeight = child.getLayoutParams().height;
            if (childLpHeight == LayoutParams.MATCH_PARENT
                    || childLpHeight == LayoutParams.WRAP_CONTENT) {
                // If the child's height is set to match_parent/wrap_content then measure it
                // with the maximum visible height

                final List<View> dependencies = parent.getDependencies(child);
                if (dependencies.isEmpty()) {
                    // If we don't have any dependencies, return false
                    return false;
                }

                final AppBarLayout appBar = findFirstAppBarLayout(dependencies);
                if (appBar != null && ViewCompat.isLaidOut(appBar)) {
                    if (ViewCompat.getFitsSystemWindows(appBar)) {
                        // If the AppBarLayout is fitting system windows then we need to also,
                        // otherwise we'll get CoL's compatible layout functionality
                        ViewCompat.setFitsSystemWindows(child, true);
                    }

                    int availableHeight = MeasureSpec.getSize(parentHeightMeasureSpec);
                    if (availableHeight == 0) {
                        // If the measure spec doesn't specify a size, use the current height
                        availableHeight = parent.getHeight();
                    }
                    final int height = availableHeight - appBar.getMeasuredHeight()
                            + appBar.getTotalScrollRange();
                    final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                            childLpHeight == LayoutParams.MATCH_PARENT
                                    ? MeasureSpec.EXACTLY
                                    : MeasureSpec.AT_MOST);

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
                    setTopAndBottomOffset(dependency.getHeight() - mOverlayTop + appBarOffset);
                }
            }
            return false;
        }

        /**
         * Set the distance that this view should overlap any {@link AppBarLayout}.
         *
         * @param overlayTop the distance in px
         */
        public void setOverlayTop(int overlayTop) {
            mOverlayTop = overlayTop;
        }

        /**
         * Returns the distance that this view should overlap any {@link AppBarLayout}.
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
