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
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.R;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * CollapsingToolbarLayout is a wrapper for {@link Toolbar} which implements a collapsing app bar.
 * It is designed to be used as a direct child of a {@link AppBarLayout}.
 * CollapsingToolbarLayout contains the following features:
 *
 * <h3>Collapsing title</h3>
 * A title which is larger when the layout is fully visible but collapses and becomes smaller as
 * the layout is scrolled off screen. You can set the title to display via
 * {@link #setTitle(CharSequence)}. The title appearance can be tweaked via the
 * {@code collapsedTextAppearance} and {@code expandedTextAppearance} attributes.
 *
 * <h3>Content scrim</h3>
 * A full-bleed scrim which is show or hidden when the scroll position has hit a certain threshold.
 * You can change this via {@link #setContentScrim(Drawable)}.
 *
 * <h3>Status bar scrim</h3>
 * A scrim which is show or hidden behind the status bar when the scroll position has hit a certain
 * threshold. You can change this via {@link #setStatusBarScrim(Drawable)}. This only works
 * on {@link Build.VERSION_CODES#LOLLIPOP LOLLIPOP} devices when we set to fit system windows.
 *
 * <h3>Parallax scrolling children</h3>
 * Child views can opt to be scrolled within this layout in a parallax fashion.
 * See {@link LayoutParams#COLLAPSE_MODE_PARALLAX} and
 * {@link LayoutParams#setParallaxMultiplier(float)}.
 *
 * <h3>Pinned position children</h3>
 * Child views can opt to be pinned in space globally. This is useful when implementing a
 * collapsing as it allows the {@link Toolbar} to be fixed in place even though this layout is
 * moving. See {@link LayoutParams#COLLAPSE_MODE_PIN}.
 *
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_collapsedTitleTextAppearance
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleTextAppearance
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_contentScrim
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleMargin
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleMarginStart
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleMarginEnd
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleMarginBottom
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_statusBarScrim
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_toolbarId
 */
public class CollapsingToolbarLayout extends FrameLayout {

    private static final int SCRIM_ANIMATION_DURATION = 600;

    private boolean mRefreshToolbar = true;
    private int mToolbarId;
    private Toolbar mToolbar;
    private View mDummyView;

    private int mExpandedMarginLeft;
    private int mExpandedMarginTop;
    private int mExpandedMarginRight;
    private int mExpandedMarginBottom;

    private final Rect mTmpRect = new Rect();
    private final CollapsingTextHelper mCollapsingTextHelper;
    private boolean mCollapsingTitleEnabled;

    private Drawable mContentScrim;
    private Drawable mStatusBarScrim;
    private int mScrimAlpha;
    private boolean mScrimsAreShown;
    private ValueAnimatorCompat mScrimAnimator;

    private AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener;

    private int mCurrentOffset;

    private WindowInsetsCompat mLastInsets;

    public CollapsingToolbarLayout(Context context) {
        this(context, null);
    }

    public CollapsingToolbarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mCollapsingTextHelper = new CollapsingTextHelper(this);
        mCollapsingTextHelper.setTextSizeInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CollapsingToolbarLayout, defStyleAttr,
                R.style.Widget_Design_CollapsingToolbar);

        mCollapsingTextHelper.setExpandedTextGravity(
                a.getInt(R.styleable.CollapsingToolbarLayout_expandedTitleGravity,
                        GravityCompat.START | Gravity.BOTTOM));
        mCollapsingTextHelper.setCollapsedTextGravity(
                a.getInt(R.styleable.CollapsingToolbarLayout_collapsedTitleGravity,
                        GravityCompat.START | Gravity.CENTER_VERTICAL));

        mExpandedMarginLeft = mExpandedMarginTop = mExpandedMarginRight = mExpandedMarginBottom =
                a.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_expandedTitleMargin, 0);

        final boolean isRtl = ViewCompat.getLayoutDirection(this)
                == ViewCompat.LAYOUT_DIRECTION_RTL;
        if (a.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginStart)) {
            final int marginStart = a.getDimensionPixelSize(
                    R.styleable.CollapsingToolbarLayout_expandedTitleMarginStart, 0);
            if (isRtl) {
                mExpandedMarginRight = marginStart;
            } else {
                mExpandedMarginLeft = marginStart;
            }
        }
        if (a.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginEnd)) {
            final int marginEnd = a.getDimensionPixelSize(
                    R.styleable.CollapsingToolbarLayout_expandedTitleMarginEnd, 0);
            if (isRtl) {
                mExpandedMarginLeft = marginEnd;
            } else {
                mExpandedMarginRight = marginEnd;
            }
        }
        if (a.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginTop)) {
            mExpandedMarginTop = a.getDimensionPixelSize(
                    R.styleable.CollapsingToolbarLayout_expandedTitleMarginTop, 0);
        }
        if (a.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginBottom)) {
            mExpandedMarginBottom = a.getDimensionPixelSize(
                    R.styleable.CollapsingToolbarLayout_expandedTitleMarginBottom, 0);
        }

        mCollapsingTitleEnabled = a.getBoolean(
                R.styleable.CollapsingToolbarLayout_titleEnabled, true);
        setTitle(a.getText(R.styleable.CollapsingToolbarLayout_title));

        // First load the default text appearances
        mCollapsingTextHelper.setExpandedTextAppearance(
                R.style.TextAppearance_Design_CollapsingToolbar_Expanded);
        mCollapsingTextHelper.setCollapsedTextAppearance(
                R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);

        // Now overlay any custom text appearances
        if (a.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleTextAppearance)) {
            mCollapsingTextHelper.setExpandedTextAppearance(
                    a.getResourceId(
                            R.styleable.CollapsingToolbarLayout_expandedTitleTextAppearance, 0));
        }
        if (a.hasValue(R.styleable.CollapsingToolbarLayout_collapsedTitleTextAppearance)) {
            mCollapsingTextHelper.setCollapsedTextAppearance(
                    a.getResourceId(
                            R.styleable.CollapsingToolbarLayout_collapsedTitleTextAppearance, 0));

        }

        setContentScrim(a.getDrawable(R.styleable.CollapsingToolbarLayout_contentScrim));
        setStatusBarScrim(a.getDrawable(R.styleable.CollapsingToolbarLayout_statusBarScrim));

        mToolbarId = a.getResourceId(R.styleable.CollapsingToolbarLayout_toolbarId, -1);

        a.recycle();

        setWillNotDraw(false);

        ViewCompat.setOnApplyWindowInsetsListener(this,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                            WindowInsetsCompat insets) {
                        mLastInsets = insets;
                        requestLayout();
                        return insets.consumeSystemWindowInsets();
                    }
                });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Add an OnOffsetChangedListener if possible
        final ViewParent parent = getParent();
        if (parent instanceof AppBarLayout) {
            if (mOnOffsetChangedListener == null) {
                mOnOffsetChangedListener = new OffsetUpdateListener();
            }
            ((AppBarLayout) parent).addOnOffsetChangedListener(mOnOffsetChangedListener);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // Remove our OnOffsetChangedListener if possible and it exists
        final ViewParent parent = getParent();
        if (mOnOffsetChangedListener != null && parent instanceof AppBarLayout) {
            ((AppBarLayout) parent).removeOnOffsetChangedListener(mOnOffsetChangedListener);
        }

        super.onDetachedFromWindow();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // If we don't have a toolbar, the scrim will be not be drawn in drawChild() below.
        // Instead, we draw it here, before our collapsing text.
        ensureToolbar();
        if (mToolbar == null && mContentScrim != null && mScrimAlpha > 0) {
            mContentScrim.mutate().setAlpha(mScrimAlpha);
            mContentScrim.draw(canvas);
        }

        // Let the collapsing text helper draw it's text
        if (mCollapsingTitleEnabled) {
            mCollapsingTextHelper.draw(canvas);
        }

        // Now draw the status bar scrim
        if (mStatusBarScrim != null && mScrimAlpha > 0) {
            final int topInset = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
            if (topInset > 0) {
                mStatusBarScrim.setBounds(0, -mCurrentOffset, getWidth(),
                        topInset - mCurrentOffset);
                mStatusBarScrim.mutate().setAlpha(mScrimAlpha);
                mStatusBarScrim.draw(canvas);
            }
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        // This is a little weird. Our scrim needs to be behind the Toolbar (if it is present),
        // but in front of any other children which are behind it. To do this we intercept the
        // drawChild() call, and draw our scrim first when drawing the toolbar
        ensureToolbar();
        if (child == mToolbar && mContentScrim != null && mScrimAlpha > 0) {
            mContentScrim.mutate().setAlpha(mScrimAlpha);
            mContentScrim.draw(canvas);
        }

        // Carry on drawing the child...
        return super.drawChild(canvas, child, drawingTime);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mContentScrim != null) {
            mContentScrim.setBounds(0, 0, w, h);
        }
    }

    private void ensureToolbar() {
        if (!mRefreshToolbar) {
            return;
        }

        Toolbar fallback = null, selected = null;

        for (int i = 0, count = getChildCount(); i < count; i++) {
            final View child = getChildAt(i);
            if (child instanceof Toolbar) {
                if (mToolbarId != -1) {
                    // There's a toolbar id set so try and find it...
                    if (mToolbarId == child.getId()) {
                        // We found the primary Toolbar, use it
                        selected = (Toolbar) child;
                        break;
                    }
                    if (fallback == null) {
                        // We'll record the first Toolbar as our fallback
                        fallback = (Toolbar) child;
                    }
                } else {
                    // We don't have a id to check for so just use the first we come across
                    selected = (Toolbar) child;
                    break;
                }
            }
        }

        if (selected == null) {
            // If we didn't find a primary Toolbar, use the fallback
            selected = fallback;
        }

        mToolbar = selected;
        updateDummyView();
        mRefreshToolbar = false;
    }

    private void updateDummyView() {
        if (!mCollapsingTitleEnabled && mDummyView != null) {
            // If we have a dummy view and we have our title disabled, remove it from its parent
            final ViewParent parent = mDummyView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(mDummyView);
            }
        }
        if (mCollapsingTitleEnabled && mToolbar != null) {
            if (mDummyView == null) {
                mDummyView = new View(getContext());
            }
            if (mDummyView.getParent() == null) {
                mToolbar.addView(mDummyView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ensureToolbar();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // Update our child view offset helpers
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);

            if (mLastInsets != null && !ViewCompat.getFitsSystemWindows(child)) {
                final int insetTop = mLastInsets.getSystemWindowInsetTop();
                if (child.getTop() < insetTop) {
                    // If the child isn't set to fit system windows but is drawing within the inset
                    // offset it down
                    child.offsetTopAndBottom(insetTop);
                }
            }

            getViewOffsetHelper(child).onViewLayout();
        }

        // Update the collapsed bounds by getting it's transformed bounds
        if (mCollapsingTitleEnabled && mDummyView != null) {
            ViewGroupUtils.getDescendantRect(this, mDummyView, mTmpRect);
            mCollapsingTextHelper.setCollapsedBounds(mTmpRect.left, bottom - mTmpRect.height(),
                    mTmpRect.right, bottom);
            // Update the expanded bounds
            mCollapsingTextHelper.setExpandedBounds(
                    mExpandedMarginLeft,
                    mTmpRect.bottom + mExpandedMarginTop,
                    right - left - mExpandedMarginRight,
                    bottom - top - mExpandedMarginBottom);

            mCollapsingTextHelper.recalculate();
        }

        // Finally, set our minimum height to enable proper AppBarLayout collapsing
        if (mToolbar != null) {
            if (mCollapsingTitleEnabled && TextUtils.isEmpty(mCollapsingTextHelper.getText())) {
                // If we do not currently have a title, try and grab it from the Toolbar
                mCollapsingTextHelper.setText(mToolbar.getTitle());
            }
            setMinimumHeight(mToolbar.getHeight());
        }
    }

    private static ViewOffsetHelper getViewOffsetHelper(View view) {
        ViewOffsetHelper offsetHelper = (ViewOffsetHelper) view.getTag(R.id.view_offset_helper);
        if (offsetHelper == null) {
            offsetHelper = new ViewOffsetHelper(view);
            view.setTag(R.id.view_offset_helper, offsetHelper);
        }
        return offsetHelper;
    }

    /**
     * Sets the title to be displayed by this view, if enabled.
     *
     * @see #setTitleEnabled(boolean)
     * @see #getTitle()
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_title
     */
    public void setTitle(@Nullable CharSequence title) {
        mCollapsingTextHelper.setText(title);
    }

    /**
     * Returns the title currently being displayed by this view. If the title is not enabled, then
     * this will return {@code null}.
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_title
     */
    @Nullable
    public CharSequence getTitle() {
        return mCollapsingTitleEnabled ? mCollapsingTextHelper.getText() : null;
    }

    /**
     * Sets whether this view should display its own title.
     *
     * <p>The title displayed by this view will shrink and grow based on the scroll offset.</p>
     *
     * @see #setTitle(CharSequence)
     * @see #isTitleEnabled()
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_titleEnabled
     */
    public void setTitleEnabled(boolean enabled) {
        if (enabled != mCollapsingTitleEnabled) {
            mCollapsingTitleEnabled = enabled;
            updateDummyView();
            requestLayout();
        }
    }

    /**
     * Returns whether this view is currently displaying its own title.
     *
     * @see #setTitleEnabled(boolean)
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_titleEnabled
     */
    public boolean isTitleEnabled() {
        return mCollapsingTitleEnabled;
    }

    private void showScrim() {
        if (!mScrimsAreShown) {
            if (ViewCompat.isLaidOut(this) && !isInEditMode()) {
                animateScrim(255);
            } else {
                setScrimAlpha(255);
            }
            mScrimsAreShown = true;
        }
    }

    private void hideScrim() {
        if (mScrimsAreShown) {
            if (ViewCompat.isLaidOut(this) && !isInEditMode()) {
                animateScrim(0);
            } else {
                setScrimAlpha(0);
            }
            mScrimsAreShown = false;
        }
    }

    private void animateScrim(int targetAlpha) {
        ensureToolbar();
        if (mScrimAnimator == null) {
            mScrimAnimator = ViewUtils.createAnimator();
            mScrimAnimator.setDuration(SCRIM_ANIMATION_DURATION);
            mScrimAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            mScrimAnimator.setUpdateListener(new ValueAnimatorCompat.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimatorCompat animator) {
                    setScrimAlpha(animator.getAnimatedIntValue());
                }
            });
        } else if (mScrimAnimator.isRunning()) {
            mScrimAnimator.cancel();
        }

        mScrimAnimator.setIntValues(mScrimAlpha, targetAlpha);
        mScrimAnimator.start();
    }

    private void setScrimAlpha(int alpha) {
        if (alpha != mScrimAlpha) {
            final Drawable contentScrim = mContentScrim;
            if (contentScrim != null && mToolbar != null) {
                ViewCompat.postInvalidateOnAnimation(mToolbar);
            }
            mScrimAlpha = alpha;
            ViewCompat.postInvalidateOnAnimation(CollapsingToolbarLayout.this);
        }
    }

    /**
     * Set the drawable to use for the content scrim from resources. Providing null will disable
     * the scrim functionality.
     *
     * @param drawable the drawable to display
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
     * @see #getContentScrim()
     */
    public void setContentScrim(@Nullable Drawable drawable) {
        if (mContentScrim != drawable) {
            if (mContentScrim != null) {
                mContentScrim.setCallback(null);
            }

            mContentScrim = drawable;
            drawable.setBounds(0, 0, getWidth(), getHeight());
            drawable.setCallback(this);
            drawable.mutate().setAlpha(mScrimAlpha);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Set the color to use for the content scrim.
     *
     * @param color the color to display
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
     * @see #getContentScrim()
     */
    public void setContentScrimColor(@ColorInt int color) {
        setContentScrim(new ColorDrawable(color));
    }

    /**
     * Set the drawable to use for the content scrim from resources.
     *
     * @param resId drawable resource id
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
     * @see #getContentScrim()
     */
    public void setContentScrimResource(@DrawableRes int resId) {
        setContentScrim(ContextCompat.getDrawable(getContext(), resId));

    }

    /**
     * Returns the drawable which is used for the foreground scrim.
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
     * @see #setContentScrim(Drawable)
     */
    public Drawable getContentScrim() {
        return mContentScrim;
    }

    /**
     * Set the drawable to use for the status bar scrim from resources.
     * Providing null will disable the scrim functionality.
     *
     * <p>This scrim is only shown when we have been given a top system inset.</p>
     *
     * @param drawable the drawable to display
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
     * @see #getStatusBarScrim()
     */
    public void setStatusBarScrim(@Nullable Drawable drawable) {
        if (mStatusBarScrim != drawable) {
            if (mStatusBarScrim != null) {
                mStatusBarScrim.setCallback(null);
            }

            mStatusBarScrim = drawable;
            drawable.setCallback(this);
            drawable.mutate().setAlpha(mScrimAlpha);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Set the color to use for the status bar scrim.
     *
     * <p>This scrim is only shown when we have been given a top system inset.</p>
     *
     * @param color the color to display
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
     * @see #getStatusBarScrim()
     */
    public void setStatusBarScrimColor(@ColorInt int color) {
        setStatusBarScrim(new ColorDrawable(color));
    }

    /**
     * Set the drawable to use for the content scrim from resources.
     *
     * @param resId drawable resource id
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
     * @see #getStatusBarScrim()
     */
    public void setStatusBarScrimResource(@DrawableRes int resId) {
        setStatusBarScrim(ContextCompat.getDrawable(getContext(), resId));
    }

    /**
     * Returns the drawable which is used for the status bar scrim.
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
     * @see #setStatusBarScrim(Drawable)
     */
    public Drawable getStatusBarScrim() {
        return mStatusBarScrim;
    }

    /**
     * Sets the text color and size for the collapsed title from the specified
     * TextAppearance resource.
     *
     * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_collapsedTitleTextAppearance
     */
    public void setCollapsedTitleTextAppearance(@StyleRes int resId) {
        mCollapsingTextHelper.setCollapsedTextAppearance(resId);
    }

    /**
     * Sets the text color of the collapsed title.
     *
     * @param color The new text color in ARGB format
     */
    public void setCollapsedTitleTextColor(@ColorInt int color) {
        mCollapsingTextHelper.setCollapsedTextColor(color);
    }

    /**
     * Sets the horizontal alignment of the collapsed title and the vertical gravity that will
     * be used when there is extra space in the collapsed bounds beyond what is required for
     * the title itself.
     *
     * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_collapsedTitleGravity
     */
    public void setCollapsedTitleGravity(int gravity) {
        mCollapsingTextHelper.setExpandedTextGravity(gravity);
    }

    /**
     * Returns the horizontal and vertical alignment for title when collapsed.
     *
     * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_collapsedTitleGravity
     */
    public int getCollapsedTitleGravity() {
        return mCollapsingTextHelper.getCollapsedTextGravity();
    }

    /**
     * Sets the text color and size for the expanded title from the specified
     * TextAppearance resource.
     *
     * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleTextAppearance
     */
    public void setExpandedTitleTextAppearance(@StyleRes int resId) {
        mCollapsingTextHelper.setExpandedTextAppearance(resId);
    }

    /**
     * Sets the text color of the expanded title.
     *
     * @param color The new text color in ARGB format
     */
    public void setExpandedTitleColor(@ColorInt int color) {
        mCollapsingTextHelper.setExpandedTextColor(color);
    }

    /**
     * Sets the horizontal alignment of the expanded title and the vertical gravity that will
     * be used when there is extra space in the expanded bounds beyond what is required for
     * the title itself.
     *
     * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleGravity
     */
    public void setExpandedTitleGravity(int gravity) {
        mCollapsingTextHelper.setExpandedTextGravity(gravity);
    }

    /**
     * Returns the horizontal and vertical alignment for title when expanded.
     *
     * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleGravity
     */
    public int getExpandedTitleGravity() {
        return mCollapsingTextHelper.getExpandedTextGravity();
    }

    /**
     * The additional offset used to define when to trigger the scrim visibility change.
     */
    final int getScrimTriggerOffset() {
        return 2 * ViewCompat.getMinimumHeight(this);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected FrameLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {

        private static final float DEFAULT_PARALLAX_MULTIPLIER = 0.5f;

        /** @hide */
        @IntDef({
                COLLAPSE_MODE_OFF,
                COLLAPSE_MODE_PIN,
                COLLAPSE_MODE_PARALLAX
        })
        @Retention(RetentionPolicy.SOURCE)
        @interface CollapseMode {}

        /**
         * The view will act as normal with no collapsing behavior.
         */
        public static final int COLLAPSE_MODE_OFF = 0;

        /**
         * The view will pin in place until it reaches the bottom of the
         * {@link CollapsingToolbarLayout}.
         */
        public static final int COLLAPSE_MODE_PIN = 1;

        /**
         * The view will scroll in a parallax fashion. See {@link #setParallaxMultiplier(float)}
         * to change the multiplier used.
         */
        public static final int COLLAPSE_MODE_PARALLAX = 2;

        int mCollapseMode = COLLAPSE_MODE_OFF;
        float mParallaxMult = DEFAULT_PARALLAX_MULTIPLIER;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs,
                    R.styleable.CollapsingAppBarLayout_LayoutParams);
            mCollapseMode = a.getInt(
                    R.styleable.CollapsingAppBarLayout_LayoutParams_layout_collapseMode,
                    COLLAPSE_MODE_OFF);
            setParallaxMultiplier(a.getFloat(
                    R.styleable.CollapsingAppBarLayout_LayoutParams_layout_collapseParallaxMultiplier,
                    DEFAULT_PARALLAX_MULTIPLIER));
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(FrameLayout.LayoutParams source) {
            super(source);
        }

        /**
         * Set the collapse mode.
         *
         * @param collapseMode one of {@link #COLLAPSE_MODE_OFF}, {@link #COLLAPSE_MODE_PIN}
         *                     or {@link #COLLAPSE_MODE_PARALLAX}.
         */
        public void setCollapseMode(@CollapseMode int collapseMode) {
            mCollapseMode = collapseMode;
        }

        /**
         * Returns the requested collapse mode.
         *
         * @return the current mode. One of {@link #COLLAPSE_MODE_OFF}, {@link #COLLAPSE_MODE_PIN}
         * or {@link #COLLAPSE_MODE_PARALLAX}.
         */
        @CollapseMode
        public int getCollapseMode() {
            return mCollapseMode;
        }

        /**
         * Set the parallax scroll multiplier used in conjunction with
         * {@link #COLLAPSE_MODE_PARALLAX}. A value of {@code 0.0} indicates no movement at all,
         * {@code 1.0f} indicates normal scroll movement.
         *
         * @param multiplier the multiplier.
         *
         * @see #getParallaxMultiplier()
         */
        public void setParallaxMultiplier(float multiplier) {
            mParallaxMult = multiplier;
        }

        /**
         * Returns the parallax scroll multiplier used in conjunction with
         * {@link #COLLAPSE_MODE_PARALLAX}.
         *
         * @see #setParallaxMultiplier(float)
         */
        public float getParallaxMultiplier() {
            return mParallaxMult;
        }
    }

    private class OffsetUpdateListener implements AppBarLayout.OnOffsetChangedListener {
        @Override
        public void onOffsetChanged(AppBarLayout layout, int verticalOffset) {
            mCurrentOffset = verticalOffset;

            final int insetTop = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
            final int scrollRange = layout.getTotalScrollRange();

            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);

                switch (lp.mCollapseMode) {
                    case LayoutParams.COLLAPSE_MODE_PIN:
                        if (getHeight() - insetTop + verticalOffset >= child.getHeight()) {
                            offsetHelper.setTopAndBottomOffset(-verticalOffset);
                        }
                        break;
                    case LayoutParams.COLLAPSE_MODE_PARALLAX:
                        offsetHelper.setTopAndBottomOffset(
                                Math.round(-verticalOffset * lp.mParallaxMult));
                        break;
                }
            }

            // Show or hide the scrims if needed
            if (mContentScrim != null || mStatusBarScrim != null) {
                if (getHeight() + verticalOffset < getScrimTriggerOffset() + insetTop) {
                    showScrim();
                } else {
                    hideScrim();
                }
            }

            if (mStatusBarScrim != null && insetTop > 0) {
                ViewCompat.postInvalidateOnAnimation(CollapsingToolbarLayout.this);
            }

            // Update the collapsing text's fraction
            final int expandRange = getHeight() - ViewCompat.getMinimumHeight(
                    CollapsingToolbarLayout.this) - insetTop;
            mCollapsingTextHelper.setExpansionFraction(
                    Math.abs(verticalOffset) / (float) expandRange);

            if (Math.abs(verticalOffset) == scrollRange) {
                // If we have some pinned children, and we're offset to only show those views,
                // we want to be elevate
                ViewCompat.setElevation(layout, layout.getTargetElevation());
            } else {
                // Otherwise, we're inline with the content
                ViewCompat.setElevation(layout, 0f);
            }
        }
    }
}
