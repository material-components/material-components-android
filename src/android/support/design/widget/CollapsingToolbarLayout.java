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
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.ColorRes;
import android.support.annotation.IntDef;
import android.support.design.R;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.Transformation;
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
 * <h3>Foreground scrim</h3>
 * A full-bleed scrim which is show or hidden when the scroll position has hit a certain threshold.
 * You can change the color via {@link #setForegroundScrimColor(int)}.
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
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_foregroundScrimColor
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleMargin
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleMarginStart
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleMarginEnd
 * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleMarginBottom
 */
public class CollapsingToolbarLayout extends FrameLayout {

    private static final int SCRIM_ANIMATION_DURATION = 600;

    private Toolbar mToolbar;
    private View mDummyView;

    private int mExpandedMarginLeft;
    private int mExpandedMarginRight;
    private int mExpandedMarginBottom;

    private final Rect mRect = new Rect();
    private final CollapsingTextHelper mCollapsingTextHelper;

    private int mForegroundScrimColor;
    private int mCurrentForegroundColor;
    private boolean mScrimIsShown;

    private AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener;

    public CollapsingToolbarLayout(Context context) {
        this(context, null);
    }

    public CollapsingToolbarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mCollapsingTextHelper = new CollapsingTextHelper(this);
        mCollapsingTextHelper.setExpandedTextVerticalGravity(Gravity.BOTTOM);
        mCollapsingTextHelper.setTextSizeInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CollapsingToolbarLayout, defStyleAttr,
                R.style.Widget_Design_CollapsingToolbar);

        mExpandedMarginLeft = mExpandedMarginRight = mExpandedMarginBottom =
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
        if (a.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginBottom)) {
            mExpandedMarginBottom = a.getDimensionPixelSize(
                    R.styleable.CollapsingToolbarLayout_expandedTitleMarginBottom, 0);
        }

        int tp = a.getResourceId(
                R.styleable.CollapsingToolbarLayout_expandedTitleTextAppearance,
                R.style.TextAppearance_AppCompat_Title);
        mCollapsingTextHelper.setExpandedTextAppearance(tp);

        tp = a.getResourceId(
                R.styleable.CollapsingToolbarLayout_collapsedTitleTextAppearance,
                R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);
        mCollapsingTextHelper.setCollapsedTextAppearance(tp);

        mForegroundScrimColor = a.getColor(
                R.styleable.CollapsingToolbarLayout_foregroundScrimColor, 0);

        a.recycle();

        setWillNotDraw(false);
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
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);

        if (child instanceof Toolbar) {
            mToolbar = (Toolbar) child;
            mDummyView = new View(getContext());
            mToolbar.addView(mDummyView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // If we don't have a toolbar, the scrim will be not be drawn in drawChild() below.
        // Instead, we draw it here, before our collapsing text.
        if (mToolbar == null && Color.alpha(mCurrentForegroundColor) > 0) {
            canvas.drawColor(mCurrentForegroundColor);
        }

        // Let the collapsing text helper draw it's text
        mCollapsingTextHelper.draw(canvas);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        // This is a little weird. Our scrim needs to be behind the Toolbar (if it is present),
        // but in front of any other children which are behind it. To do this we intercept the
        // drawChild() call, and draw our scrim first when drawing the toolbar
        if (child == mToolbar && Color.alpha(mCurrentForegroundColor) > 0) {
            canvas.drawColor(mCurrentForegroundColor);
        }

        // Carry on drawing the child...
        return super.drawChild(canvas, child, drawingTime);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // Update our child view offset helpers
        for (int i = 0, z = getChildCount(); i < z; i++) {
            getViewOffsetHelper(getChildAt(i)).onViewLayout();
        }

        // Now let the collapsing text helper update itself
        mCollapsingTextHelper.onLayout(changed, left, top, right, bottom);

        // Update the collapsed bounds by getting it's transformed bounds
        ViewGroupUtils.getDescendantRect(this, mDummyView, mRect);
        mCollapsingTextHelper.setCollapsedBounds(mRect.left, bottom - mRect.height(),
                mRect.right, bottom);
        // Update the expanded bounds
        mCollapsingTextHelper.setExpandedBounds(left + mExpandedMarginLeft, mDummyView.getBottom(),
                right - mExpandedMarginRight, bottom - mExpandedMarginBottom);

        // Finally, set our minimum height to enable proper AppBarLayout collapsing
        setMinimumHeight(mToolbar.getHeight());
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
     * Set the title to display
     *
     * @param title
     */
    public void setTitle(CharSequence title) {
        mCollapsingTextHelper.setText(title);
    }

    private void showScrim() {
        if (mScrimIsShown) return;

        Animation anim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                final int originalAlpha = Color.alpha(mForegroundScrimColor);
                mCurrentForegroundColor = ColorUtils.setAlphaComponent(mForegroundScrimColor,
                        AnimationUtils.lerp(0, originalAlpha, interpolatedTime));

                // We need to manually invalidate ourselves and the Toolbar to ensure the scrim
                // is drawn
                invalidate();
                if (mToolbar != null) {
                    mToolbar.invalidate();
                }
            }
        };
        anim.setDuration(SCRIM_ANIMATION_DURATION);
        anim.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
        startAnimation(anim);

        mScrimIsShown = true;
    }

    private void hideScrim() {
        if (!mScrimIsShown) return;

        Animation anim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                final int originalAlpha = Color.alpha(mForegroundScrimColor);
                mCurrentForegroundColor = ColorUtils.setAlphaComponent(mForegroundScrimColor,
                        AnimationUtils.lerp(originalAlpha, 0, interpolatedTime));

                // We need to manually invalidate ourselves and the Toolbar to ensure the scrim
                // is drawn
                invalidate();
                if (mToolbar != null) {
                    mToolbar.invalidate();
                }
            }
        };
        anim.setDuration(SCRIM_ANIMATION_DURATION);
        anim.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
        startAnimation(anim);

        mScrimIsShown = false;
    }

    /**
     * Set the color to use for the  foreground scrim. Providing a transparent color which disable
     * the scrim.
     *
     * @param color ARGB color to use
     *
     * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_foregroundScrimColor
     * @see #getForegroundScrimColor()
     */
    public void setForegroundScrimColor(int color) {
        mForegroundScrimColor = color;
    }

    /**
     * Set the color to use for the  foreground scrim from resources. Providing a transparent color
     * which disable the scrim.
     *
     * @param resId color resource id
     *
     * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_foregroundScrimColor
     * @see #getForegroundScrimColor()
     */
    public void setForegroundScrimColorResource(@ColorRes int resId) {
        mForegroundScrimColor = getResources().getColor(resId);
    }

    /**
     * Returns the color which is used for the foreground scrim.
     *
     * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_foregroundScrimColor
     * @see #setForegroundScrimColor(int)
     */
    public int getForegroundScrimColor() {
        return mForegroundScrimColor;
    }

    /**
     * Sets the text color and size for the collapsed title from the specified
     * TextAppearance resource.
     *
     * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_collapsedTitleTextAppearance
     */
    public void setCollapsedTitleTextAppearance(int resId) {
        mCollapsingTextHelper.setCollapsedTextAppearance(resId);
    }

    /**
     * Sets the text color of the collapsed title.
     *
     * @param color The new text color in ARGB format
     */
    public void setCollapsedTitleTextColor(int color) {
        mCollapsingTextHelper.setCollapsedTextColor(color);
    }

    /**
     * Sets the text color and size for the expanded title from the specified
     * TextAppearance resource.
     *
     * @attr ref android.support.design.R.styleable#CollapsingToolbarLayout_expandedTitleTextAppearance
     */
    public void setExpandedTitleTextAppearance(int resId) {
        mCollapsingTextHelper.setExpandedTextAppearance(resId);
    }

    /**
     * Sets the text color of the expanded title.
     *
     * @param color The new text color in ARGB format
     */
    public void setExpandedTitleColor(int color) {
        mCollapsingTextHelper.setExpandedTextColor(color);
    }

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
            int pinnedHeight = 0;

            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);

                switch (lp.mCollapseMode) {
                    case LayoutParams.COLLAPSE_MODE_PIN:
                        if (getHeight() + verticalOffset >= child.getHeight()) {
                            offsetHelper.setTopAndBottomOffset(-verticalOffset);
                        }
                        pinnedHeight += child.getHeight();
                        break;
                    case LayoutParams.COLLAPSE_MODE_PARALLAX:
                        offsetHelper.setTopAndBottomOffset(
                                Math.round(-verticalOffset * lp.mParallaxMult));
                        break;
                }
            }

            // Show or hide the scrim if needed
            if (Color.alpha(mForegroundScrimColor) > 0) {
                if (getHeight() + verticalOffset < getScrimTriggerOffset()) {
                    showScrim();
                } else {
                    hideScrim();
                }
            }

            // Update the collapsing text's fraction
            final int expandRange = getHeight() - ViewCompat.getMinimumHeight(
                    CollapsingToolbarLayout.this);
            mCollapsingTextHelper.setExpansionFraction(
                    Math.abs(verticalOffset) / (float) expandRange);

            if (pinnedHeight > 0 && (getHeight() + verticalOffset) == pinnedHeight) {
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
