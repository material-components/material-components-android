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

import static android.animation.LayoutTransition.APPEARING;
import static android.animation.LayoutTransition.CHANGE_APPEARING;
import static android.animation.LayoutTransition.CHANGE_DISAPPEARING;
import static android.animation.LayoutTransition.CHANGING;
import static android.animation.LayoutTransition.DISAPPEARING;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.core.text.TextUtilsCompat;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */
public class SeslChipGroup extends ChipGroup {
    public static int sChipInitialWidth;
    public int mChipMaxWidth;
    public boolean mDynamicChipTextTruncation;
    public int mEmptyContainerHeight;
    public final LayoutTransition mLayoutTransition;
    public int mRowCount;

    public final void postRemoveView() {
    }

    public SeslChipGroup(Context context) {
        this(context, null);
    }

    public SeslChipGroup(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.chipGroupStyle);
    }

    public SeslChipGroup(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        mDynamicChipTextTruncation = true;
        mLayoutTransition = new LayoutTransition();
        mEmptyContainerHeight = 0;
        sChipInitialWidth = (int) getResources().getDimension(R.dimen.chip_start_width);
        setLayoutDirection(TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()));
        setChipLayoutTransition();
        enableSeslLayoutTransitions(true);
    }

    public void enableSeslLayoutTransitions(boolean enable) {
        if (!enable) {
            setLayoutTransition(null);
        } else {
            setLayoutTransition(mLayoutTransition);
        }
    }

    @Override
    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        super.addView(view, i, layoutParams);
        addRemoveAnim();
        if (view instanceof Chip) {
            postAddView((Chip) view);
        }
    }

    @Override
    public void removeView(View view) {
        setStaticHeight();
        super.removeView(view);
        addRemoveAnim();
        postRemoveView();
    }

    @Override
    public void removeViewInLayout(View view) {
        setStaticHeight();
        super.removeViewInLayout(view);
        addRemoveAnim();
        postRemoveView();
    }

    @Override
    public void removeViewsInLayout(int start, int count) {
        setStaticHeight();
        super.removeViewsInLayout(start, count);
        addRemoveAnim();
        postRemoveView();
    }

    @Override
    public void removeViewAt(int index) {
        setStaticHeight();
        super.removeViewAt(index);
        addRemoveAnim();
        postRemoveView();
    }

    @Override
    public void removeViews(int start, int count) {
        setStaticHeight();
        super.removeViews(start, count);
        addRemoveAnim();
        postRemoveView();
    }

    @Override
    public void removeAllViews() {
        setStaticHeight();
        super.removeAllViews();
        addRemoveAnim();
        postRemoveView();
    }

    @Override
    public void removeAllViewsInLayout() {
        setStaticHeight();
        super.removeAllViewsInLayout();
        addRemoveAnim();
        postRemoveView();
    }

    @Override
    public int getRowCount() {
        return mRowCount;
    }

    public void setMaxChipWidth(int maxWidth) {
        mChipMaxWidth = maxWidth - (getPaddingStart() + getPaddingEnd());
    }

    public int getTotalWidth() {
        int paddingStart = getPaddingStart() + getPaddingEnd();
        int childCount = getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                paddingStart += getChildAt(i).getWidth();
            }
            return childCount > 1 ? paddingStart + (getChipSpacingHorizontal() * (childCount - 2)) : paddingStart;
        }
        return paddingStart;
    }

    public int getInternalHeight(float f) {
        int childCount = getChildCount();
        if (childCount == 0) {
            return 0;
        }
        int paddingStart = getPaddingStart();
        int paddingEnd = getPaddingEnd();
        int chipSpacingHorizontal = getChipSpacingHorizontal();
        int width = paddingStart + paddingEnd + getChildAt(0).getWidth() + chipSpacingHorizontal;
        int i = 1;
        for (int i2 = 1; i2 < childCount; i2++) {
            int intrinsicWidth = ((Chip) getChildAt(i2)).getChipDrawable().getIntrinsicWidth();
            if (width + intrinsicWidth < f) {
                width += intrinsicWidth + chipSpacingHorizontal;
            } else {
                i++;
                width = intrinsicWidth + chipSpacingHorizontal + paddingStart + paddingEnd;
            }
        }
        int chipSpacingVertical = getChipSpacingVertical();
        return (((i * (getChildAt(0).getHeight() + chipSpacingVertical)) + getPaddingBottom()) + getPaddingTop()) - chipSpacingVertical;
    }

    public final void postAddView(Chip chip) {
        if (mDynamicChipTextTruncation) {
            int i = mChipMaxWidth;
            if (i > 0) {
                chip.setMaxWidth(i);
            }
            chip.setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    public final void setStaticHeight() {
        mEmptyContainerHeight = getHeight();
    }

    public final void addRemoveAnim() {
        int height = getHeight();
        int internalHeight = getInternalHeight(getWidth());
        if (height != internalHeight && shouldAnimateHeight()) {
            startSeslLayoutHeightAnim(height, internalHeight);
            return;
        }
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = WRAP_CONTENT;
        mEmptyContainerHeight = 0;
        setLayoutParams(layoutParams);
    }

    public final boolean shouldAnimateHeight() {
        return !isSingleLine() || (isSingleLine() && getChildCount() == 0);
    }

    public final void startSeslLayoutHeightAnim(final int start, int end) {
        final int distance = end - start;
        if (Math.abs(distance) < getContext().getResources().getDimension(R.dimen.chip_height)) {
            return;
        }
        ValueAnimator heightAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        heightAnimator.setDuration(getContext().getResources().getInteger(R.integer.sesl_chip_default_anim_duration));
        heightAnimator.setInterpolator(AnimationUtils.loadInterpolator(getContext(), androidx.appcompat.R.interpolator.sesl_chip_default_interpolator));
        heightAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                ViewGroup.LayoutParams lp = getLayoutParams();
                lp.height = WRAP_CONTENT;
                mEmptyContainerHeight = 0;
                setLayoutParams(lp);
                SeslChipGroup seslChipGroup = SeslChipGroup.this;
                seslChipGroup.mEmptyContainerHeight = 0;
                seslChipGroup.setLayoutParams(lp);
            }
        });

        heightAnimator.addUpdateListener(valueAnimator -> {
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            int floatValue = start + ((int) (distance * (Float) valueAnimator.getAnimatedValue()));
            layoutParams.height = floatValue;
            mEmptyContainerHeight = floatValue;
            setLayoutParams(layoutParams);

        });
        heightAnimator.start();
    }


    public final void setChipLayoutTransition() {
        mLayoutTransition.enableTransitionType(APPEARING);
        mLayoutTransition.enableTransitionType(DISAPPEARING);
        mLayoutTransition.enableTransitionType(CHANGING);
        mLayoutTransition.enableTransitionType(CHANGE_APPEARING);
        mLayoutTransition.enableTransitionType(CHANGE_DISAPPEARING);
        mLayoutTransition.setStartDelay(APPEARING, 0L);
        mLayoutTransition.setStartDelay(DISAPPEARING, 0L);
        mLayoutTransition.setStartDelay(CHANGING, 0L);
        mLayoutTransition.setStartDelay(CHANGE_APPEARING, 0L);
        mLayoutTransition.setStartDelay(CHANGE_DISAPPEARING, 0L);

        int duration = getContext().getResources().getInteger(R.integer.sesl_chip_default_anim_duration);
        SeslValueAnimator showAnimator = SeslValueAnimator.ofFloat(0.0f, 1.0f);
        showAnimator.setDuration(duration);
        showAnimator.setStartDelay(0L);
        showAnimator.addUpdateListener(SeslChipGroup::animateChipAppear);
        showAnimator.addListener(getAddRemAnimListener());
        mLayoutTransition.setAnimator(APPEARING, showAnimator);

        SeslValueAnimator hideAnimator = SeslValueAnimator.ofFloat(1.0f, 0.0f);
        hideAnimator.setDuration(duration);
        hideAnimator.addUpdateListener(SeslChipGroup::animateChipDisappear);
        hideAnimator.addListener(getAddRemAnimListener());
        mLayoutTransition.setAnimator(DISAPPEARING, hideAnimator);

        Interpolator interpolator = AnimationUtils.loadInterpolator(getContext(), androidx.appcompat.R.interpolator.sesl_chip_default_interpolator);
        mLayoutTransition.setInterpolator(DISAPPEARING, interpolator);
        mLayoutTransition.setInterpolator(APPEARING, interpolator);
        mLayoutTransition.setInterpolator(CHANGING, interpolator);
        mLayoutTransition.setInterpolator(CHANGE_APPEARING, interpolator);
        mLayoutTransition.setInterpolator(CHANGE_DISAPPEARING, interpolator);
        mLayoutTransition.addTransitionListener(getChipTransitionListener());
    }

    public final LayoutTransition.TransitionListener getChipTransitionListener() {
        return new LayoutTransition.TransitionListener() {
            @Override
            public void endTransition(LayoutTransition layoutTransition,
                                      ViewGroup viewGroup, View view, int i) {}

            @Override
            public void startTransition(LayoutTransition layoutTransition,
                                        ViewGroup viewGroup, View view, int i) {}
        };
    }

    public final AnimatorListenerAdapter getAddRemAnimListener() {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                View targetView = ((SeslValueAnimator) animator).getTargetView();
                if (targetView == null) {
                    return;
                }
                targetView.setAlpha(1.0f);
            }
        };
    }

    public static void animateChipDisappear(ValueAnimator valueAnimator) {
        View targetView = ((SeslValueAnimator) valueAnimator).getTargetView();
        if (targetView == null) {
            return;
        }
        targetView.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    public static void animateChipAppear(ValueAnimator valueAnimator) {
        View targetView = ((SeslValueAnimator) valueAnimator).getTargetView();
        if (targetView == null) {
            return;
        }
        targetView.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    @Override
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (getChildCount() <= 0) {
            setMeasuredDimension(getWidth(), mEmptyContainerHeight);
        }
    }

    @Override
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7 = 0;
        if (getChildCount() == 0) {
            mRowCount = 0;
            return;
        }
        int i8 = 1;
        mRowCount = 1;
        boolean z2 = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
        int paddingRight = z2 ? getPaddingRight() : getPaddingLeft();
        int paddingLeft = z2 ? getPaddingLeft() : getPaddingRight();
        int paddingTop = getPaddingTop();
        int lineSpacing = getLineSpacing();
        int itemSpacing = getItemSpacing();
        int i9 = i3 - i;
        int i10 = i9 - paddingLeft;
        if (!z2) {
            i9 = i10;
        }
        int i11 = 0;
        int i12 = paddingRight;
        int i13 = paddingTop;
        while (i11 < getChildCount()) {
            View childAt = getChildAt(i11);
            if (childAt.getVisibility() == View.GONE) {
                childAt.setTag(R.id.row_index_key, -1);
            } else {
                ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
                if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
                    i6 = MarginLayoutParamsCompat.getMarginStart(marginLayoutParams);
                    i5 = MarginLayoutParamsCompat.getMarginEnd(marginLayoutParams);
                } else {
                    i5 = i7;
                    i6 = i5;
                }
                int measuredWidth = i12 + i6 + childAt.getMeasuredWidth();
                if (!isSingleLine() && measuredWidth > i10) {
                    i13 = paddingTop + lineSpacing;
                    mRowCount += i8;
                    i12 = paddingRight;
                }
                childAt.setTag(R.id.row_index_key, mRowCount - i8);
                int i14 = i12 + i6;
                int measuredWidth2 = childAt.getMeasuredWidth() + i14;
                int measuredHeight = i13 + childAt.getMeasuredHeight();
                if (z2) {
                    childAt.layout(i9 - measuredWidth2, i13, (i9 - i12) - i6, measuredHeight);
                } else {
                    childAt.layout(i14, i13, measuredWidth2, measuredHeight);
                }
                i12 += i6 + i5 + childAt.getMeasuredWidth() + itemSpacing;
                paddingTop = measuredHeight;
            }
            i11++;
            i8 = 1;
            i7 = 0;
        }
    }


    public static class SeslValueAnimator extends ValueAnimator {
        public ArrayList<Animator.AnimatorListener> mSeslListeners;
        public ArrayList<ValueAnimator.AnimatorUpdateListener> mSeslUpdateListeners;
        public WeakReference<View> mTargetView;
        public float[] mValues;

        public static SeslValueAnimator ofFloat(float... fArr) {
            SeslValueAnimator seslValueAnimator = new SeslValueAnimator();
            seslValueAnimator.setFloatValues(fArr);
            seslValueAnimator.mValues = fArr;
            seslValueAnimator.mSeslUpdateListeners = new ArrayList<>();
            seslValueAnimator.mSeslListeners = new ArrayList<>();
            return seslValueAnimator;
        }

        @Override
        public void setTarget(Object obj) {
            mTargetView = new WeakReference<>((View) obj);
            super.setTarget(obj);
        }

        public View getTargetView() {
            return (View) mTargetView.get();
        }

        @Override
        public void addListener(Animator.AnimatorListener animatorListener) {
            super.addListener(animatorListener);
            mSeslListeners.add(animatorListener);
        }

        @Override
        public void addUpdateListener(ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
            super.addUpdateListener(animatorUpdateListener);
            mSeslUpdateListeners.add(animatorUpdateListener);
        }

        @NonNull
        @Override
        public SeslValueAnimator clone() {
            SeslValueAnimator ofFloat = ofFloat(mValues);
            ArrayList<AnimatorUpdateListener> arrayList = mSeslUpdateListeners;
            if (arrayList != null) {
                for (AnimatorUpdateListener animatorUpdateListener : arrayList) {
                    ofFloat.addUpdateListener(animatorUpdateListener);
                }
            }
            ArrayList<AnimatorListener> arrayList2 = mSeslListeners;
            if (arrayList2 != null) {
                for (AnimatorListener animatorListener : arrayList2) {
                    ofFloat.addListener(animatorListener);
                }
            }
            return ofFloat;
        }
    }
}
