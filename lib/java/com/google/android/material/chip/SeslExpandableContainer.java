/*
 * Copyright 2024 The Android Open Source Project
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

package com.google.android.material.chip;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.text.TextUtilsCompat;
import com.google.android.material.R;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */
public class SeslExpandableContainer extends FrameLayout {
    public boolean mChipGroupInitialized;
    public boolean mExpanded;
    public final SeslExpansionButton mExpansionButton;
    public final int mExpansionButtonContainerId;
    public boolean mFadeAnimation;
    public boolean mFloatChangeAllowed;
    public final boolean mIsRtl;
    public boolean mPaddingAllowed;
    public final View mPaddingView;
    public final HorizontalScrollView mScrollView;
    public int mScrollViewPos;
    public final LinearLayout mScrollingChipsContainer;

    public SeslExpandableContainer(Context context) {
        this(context, null);
    }

    public SeslExpandableContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, -1);
    }

    public SeslExpandableContainer(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, -1);
    }

    public SeslExpandableContainer(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        mExpanded = false;
        mPaddingAllowed = true;
        mScrollViewPos = 0;
        mFloatChangeAllowed = true;
        mIsRtl = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == 1;
        View container = LayoutInflater.from(context).inflate(R.layout.sesl_expandable_container, (ViewGroup) null);
        mScrollView = container.findViewById(R.id.sesl_scroll_view);
        setOnScrollChangeListener();
        mScrollingChipsContainer = container.findViewById(R.id.sesl_scrolling_chips_container);
        mPaddingView = container.findViewById(R.id.sesl_padding_view);
        addView(container);
        mExpansionButtonContainerId = View.generateViewId();
        mExpansionButton = new SeslExpansionButton(context);
        initExpansionButtonLayout(context);
        addExpansionButtonContainer(context);
    }

    public int getScrollContentsWidth() {
        int width;
        if (mExpanded) {
            return 0;
        }
        int i = 0;
        for (int i2 = 0; i2 < mScrollingChipsContainer.getChildCount(); i2++) {
            View childAt = mScrollingChipsContainer.getChildAt(i2);
            if (childAt.getVisibility() == View.VISIBLE) {
                if (childAt instanceof SeslChipGroup) {
                    width = ((SeslChipGroup) childAt).getTotalWidth();
                } else {
                    width = childAt.getWidth();
                }
                i += width;
            }
        }
        return i;
    }

    public View getPaddingView() {
        return mPaddingView;
    }

    @Override
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        refreshLayout();
    }

    public final void addExpansionButtonContainer(Context context) {
        RelativeLayout rl = new RelativeLayout(context);
        rl.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        rl.setId(mExpansionButtonContainerId);
        if (mIsRtl) {
            rl.setGravity(Gravity.LEFT);
        } else {
            rl.setGravity(Gravity.RIGHT);
        }
        addView(rl);
        rl.addView(mExpansionButton);
    }

    public final void initExpansionButtonLayout(Context context) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        lp.setMargins(0, context.getResources().getDimensionPixelSize(R.dimen.expansion_button_margin_top), 0, 0);
        mExpansionButton.setLayoutParams(lp);
        mExpansionButton.setBackground(context.getDrawable(R.drawable.sesl_expansion_button_background));
        mExpansionButton.setImageDrawable(context.getDrawable(R.drawable.sesl_expansion_button_foreground));
        mExpansionButton.setAutomaticDisappear(true);
        mExpansionButton.setExpanded(false);
        mExpansionButton.setFloated(true);
        mExpansionButton.setVisibility(View.GONE);
    }

    public final void refreshLayout() {
        setLayoutTransition(null);
        int i = 1;
        if (mExpanded) {
            if (mScrollingChipsContainer.getChildCount() > 0) {
                mExpansionButton.setAutomaticDisappear(false);
                mScrollViewPos = mScrollView.getScrollX();
                int childCount = mScrollingChipsContainer.getChildCount();
                View[] viewArr = new View[childCount];
                getAddedChildrens(mScrollingChipsContainer, viewArr, mIsRtl);
                int totalHeight = 0;
                for (int i3 = 0; i3 < childCount; i3++) {
                    View view = viewArr[i3];
                    if (!mPaddingAllowed || view.getId() != mPaddingView.getId()) {
                        mScrollingChipsContainer.removeView(view);
                        addView(view, i);
                        totalHeight += view.getHeight();
                        i++;
                    }
                }
                mScrollView.setVisibility(View.GONE);
                if (mExpansionButton.getVisibility() == View.VISIBLE || totalHeight <= 0) {
                    return;
                }
                mExpansionButton.setVisibility(View.VISIBLE);
            }
        } else if (getChildCount() > 2) {
            mExpansionButton.setAutomaticDisappear(true);
            mScrollView.setVisibility(View.VISIBLE);
            int childCount2 = getChildCount();
            View[] viewArr2 = new View[childCount2];
            getAddedChildrens(this, viewArr2, mIsRtl);
            int i4 = 0;
            for (int i5 = 0; i5 < childCount2; i5++) {
                View view2 = viewArr2[i5];
                if (!mChipGroupInitialized && (view2 instanceof SeslChipGroup)) {
                    ((SeslChipGroup) view2).setMaxChipWidth(getWidth());
                    mChipGroupInitialized = true;
                }
                int id = view2.getId();
                if (id != mScrollView.getId() && id != mExpansionButtonContainerId && id != mPaddingView.getId()) {
                    removeView(view2);
                    mScrollingChipsContainer.addView(view2, i4);
                    i4++;
                }
            }
            mScrollView.scrollTo(mScrollViewPos, 0);
            updateScrollExpansionButton();
        }
    }

    public static void getAddedChildrens(ViewGroup viewGroup, View[] viewArr, boolean z) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            viewArr[i] = viewGroup.getChildAt(i);
        }
        if (z) {
            Collections.reverse(Arrays.asList(viewArr));
        }
    }

    public final void updateScrollExpansionButton() {
        int scrollContentsWidth = getScrollContentsWidth();
        int width = getWidth() + 10;
        int width2 = mPaddingView.getWidth();
        if (mPaddingAllowed) {
            if ((mPaddingView.getVisibility() == View.VISIBLE && scrollContentsWidth - width2 > width)
                    || ( mPaddingView.getVisibility() == View.GONE && scrollContentsWidth > width) ) {
                if (mExpansionButton.getVisibility() != View.VISIBLE) {
                    mExpansionButton.setVisibility(View.VISIBLE);
                }
                setExpansionButton();
            } else if (mExpansionButton.getVisibility() == View.VISIBLE) {
                mExpansionButton.setVisibility(View.INVISIBLE);
            }
        } else if (scrollContentsWidth > width) {
            if (mExpansionButton.getVisibility() != View.VISIBLE) {
                mExpansionButton.setVisibility(View.VISIBLE);
            }
            setExpansionButton();
        } else if (mExpansionButton.getVisibility() == View.VISIBLE) {
            mExpansionButton.setVisibility(View.INVISIBLE);
        }
        updateScrollExpansionButtonFloat();
    }

    public final void updateScrollExpansionButtonFloat() {
        if (mFloatChangeAllowed && mScrollView.getVisibility() == View.VISIBLE) {
            if (!mPaddingAllowed || shouldFloat()) {
                mExpansionButton.setFloated(true);
            } else {
                mExpansionButton.setFloated(false);
            }
        }
    }

    public final boolean shouldFloat() {
        return (mIsRtl
                && mScrollView.getScrollX() > getPaddingView().getWidth() / 2)
                || ( !mIsRtl && mScrollView.getScrollX() < getScrollContentsWidth() - getWidth() );
    }

    public final void setExpansionButton() {
        mExpansionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                if (mFadeAnimation) {
                    fadeAnimation();
                    return;
                }
                mExpanded = !mExpanded;
                refreshLayout();
                post(SeslExpandableContainer.this::setExpanded);
            }
        });
    }


    public /* synthetic */ void setExpanded() {
        mExpansionButton.setExpanded(mExpanded);
    }

    public final void fadeAnimation() {
        (mExpanded ? getChildAt(1) : mScrollingChipsContainer).animate().setDuration(100L).alpha(0.0f).setListener(new Animator.AnimatorListener() { // from class: com.google.android.material.chip.SeslExpandableContainer.1
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {
            }

            @Override
            public void onAnimationStart(@NonNull Animator animator) {
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                if (!mExpanded) {
                   mScrollingChipsContainer.setAlpha(1.0f);
                } else {
                   getChildAt(1).setAlpha(1.0f);
                }
               postFadeAnimation();
            }
        });
    }

    public final void postFadeAnimation() {
        mExpanded = !mExpanded;
        refreshLayout();
        mExpansionButton.setExpanded(mExpanded);
    }

    public final void setOnScrollChangeListener() {
        mScrollView.setOnScrollChangeListener((view, i, i2, i3, i4) -> updateScrollExpansionButton());
    }

}
