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
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.R;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.internal.widget.TintManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Layout which wraps an {@link android.widget.EditText} (or descendant) to show a floating label
 * when the hint is hidden due to the user inputting text.
 *
 * Also supports showing an error via {@link #setErrorEnabled(boolean)} and
 * {@link #setError(CharSequence)}.
 */
public class TextInputLayout extends LinearLayout {

    private static final int ANIMATION_DURATION = 200;
    private static final int INVALID_MAX_LENGTH = -1;

    private EditText mEditText;
    private CharSequence mHint;

    private Paint mTmpPaint;

    private LinearLayout mIndicatorArea;

    private boolean mErrorEnabled;
    private TextView mErrorView;
    private int mErrorTextAppearance;
    private boolean mErrorShown;

    private boolean mCounterEnabled;
    private TextView mCounterView;
    private int mCounterMaxLength;
    private int mCounterTextAppearance;
    private int mCounterOverflowTextAppearance;
    private boolean mCounterOverflowed;

    private ColorStateList mDefaultTextColor;
    private ColorStateList mFocusedTextColor;

    private final CollapsingTextHelper mCollapsingTextHelper = new CollapsingTextHelper(this);

    private boolean mHintAnimationEnabled;
    private ValueAnimatorCompat mAnimator;

    public TextInputLayout(Context context) {
        this(context, null);
    }

    public TextInputLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        // Can't call through to super(Context, AttributeSet, int) since it doesn't exist on API 10
        super(context, attrs);

        ThemeUtils.checkAppCompatTheme(context);

        setOrientation(VERTICAL);
        setWillNotDraw(false);
        setAddStatesFromChildren(true);

        mCollapsingTextHelper.setTextSizeInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
        mCollapsingTextHelper.setPositionInterpolator(new AccelerateInterpolator());
        mCollapsingTextHelper.setCollapsedTextGravity(Gravity.TOP | GravityCompat.START);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.TextInputLayout, defStyleAttr, R.style.Widget_Design_TextInputLayout);
        setHint(a.getText(R.styleable.TextInputLayout_android_hint));
        mHintAnimationEnabled = a.getBoolean(
                R.styleable.TextInputLayout_hintAnimationEnabled, true);

        if (a.hasValue(R.styleable.TextInputLayout_android_textColorHint)) {
            mDefaultTextColor = mFocusedTextColor =
                    a.getColorStateList(R.styleable.TextInputLayout_android_textColorHint);
        }

        final int hintAppearance = a.getResourceId(
                R.styleable.TextInputLayout_hintTextAppearance, -1);
        if (hintAppearance != -1) {
            setHintTextAppearance(
                    a.getResourceId(R.styleable.TextInputLayout_hintTextAppearance, 0));
        }

        mErrorTextAppearance = a.getResourceId(R.styleable.TextInputLayout_errorTextAppearance, 0);
        final boolean errorEnabled = a.getBoolean(R.styleable.TextInputLayout_errorEnabled, false);

        final boolean counterEnabled = a.getBoolean(
                R.styleable.TextInputLayout_counterEnabled, false);
        setCounterMaxLength(
                a.getInt(R.styleable.TextInputLayout_counterMaxLength, INVALID_MAX_LENGTH));
        mCounterTextAppearance = a.getResourceId(
                R.styleable.TextInputLayout_counterTextAppearance, 0);
        mCounterOverflowTextAppearance = a.getResourceId(
                R.styleable.TextInputLayout_counterOverflowTextAppearance, 0);
        a.recycle();

        setErrorEnabled(errorEnabled);
        setCounterEnabled(counterEnabled);

        if (ViewCompat.getImportantForAccessibility(this)
                == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            // Make sure we're important for accessibility if we haven't been explicitly not
            ViewCompat.setImportantForAccessibility(this,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }

        ViewCompat.setAccessibilityDelegate(this, new TextInputAccessibilityDelegate());
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            setEditText((EditText) child);
            super.addView(child, 0, updateEditTextMargin(params));
        } else {
            // Carry on adding the View...
            super.addView(child, index, params);
        }
    }

    /**
     * Set the typeface to use for the both the expanded and floating hint.
     *
     * @param typeface typeface to use, or {@code null} to use the default.
     */
    public void setTypeface(@Nullable Typeface typeface) {
        mCollapsingTextHelper.setTypeface(typeface);
    }

    private void setEditText(EditText editText) {
        // If we already have an EditText, throw an exception
        if (mEditText != null) {
            throw new IllegalArgumentException("We already have an EditText, can only have one");
        }
        mEditText = editText;

        // Use the EditText's typeface, and it's text size for our expanded text
        mCollapsingTextHelper.setTypeface(mEditText.getTypeface());
        mCollapsingTextHelper.setExpandedTextSize(mEditText.getTextSize());
        mCollapsingTextHelper.setExpandedTextGravity(mEditText.getGravity());

        // Add a TextWatcher so that we know when the text input has changed
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateLabelVisibility(true);
                if (mCounterEnabled) {
                    updateCounter(s.length());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Use the EditText's hint colors if we don't have one set
        if (mDefaultTextColor == null) {
            mDefaultTextColor = mEditText.getHintTextColors();
        }

        // If we do not have a valid hint, try and retrieve it from the EditText
        if (TextUtils.isEmpty(mHint)) {
            setHint(mEditText.getHint());
            // Clear the EditText's hint as we will display it ourselves
            mEditText.setHint(null);
        }

        if (mCounterView != null) {
            updateCounter(mEditText.getText().length());
        }

        if (mIndicatorArea != null) {
            adjustIndicatorPadding();
        }

        // Update the label visibility with no animation
        updateLabelVisibility(false);
    }

    private LayoutParams updateEditTextMargin(ViewGroup.LayoutParams lp) {
        // Create/update the LayoutParams so that we can add enough top margin
        // to the EditText so make room for the label
        LayoutParams llp = lp instanceof LayoutParams ? (LayoutParams) lp : new LayoutParams(lp);

        if (mTmpPaint == null) {
            mTmpPaint = new Paint();
        }
        mTmpPaint.setTypeface(mCollapsingTextHelper.getTypeface());
        mTmpPaint.setTextSize(mCollapsingTextHelper.getCollapsedTextSize());
        llp.topMargin = (int) -mTmpPaint.ascent();

        return llp;
    }

    private void updateLabelVisibility(boolean animate) {
        boolean hasText = mEditText != null && !TextUtils.isEmpty(mEditText.getText());
        boolean isFocused = arrayContains(getDrawableState(), android.R.attr.state_focused);
        boolean isErrorShowing = !TextUtils.isEmpty(getError());


        if (mDefaultTextColor != null && mFocusedTextColor != null) {
            mCollapsingTextHelper.setExpandedTextColor(mDefaultTextColor.getDefaultColor());
            mCollapsingTextHelper.setCollapsedTextColor(isFocused
                    ? mFocusedTextColor.getDefaultColor()
                    : mDefaultTextColor.getDefaultColor());
        }

        if (hasText || isFocused || isErrorShowing) {
            // We should be showing the label so do so if it isn't already
            collapseHint(animate);
        } else {
            // We should not be showing the label so hide it
            expandHint(animate);
        }
    }

    /**
     * Returns the {@link android.widget.EditText} used for text input.
     */
    @Nullable
    public EditText getEditText() {
        return mEditText;
    }

    /**
     * Set the hint to be displayed in the floating label
     *
     * @attr ref android.support.design.R.styleable#TextInputLayout_android_hint
     */
    public void setHint(@Nullable CharSequence hint) {
        mHint = hint;
        mCollapsingTextHelper.setText(hint);

        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }

    /**
     * Returns the hint which is displayed in the floating label.
     *
     * @attr ref android.support.design.R.styleable#TextInputLayout_android_hint
     */
    @Nullable
    public CharSequence getHint() {
        return mHint;
    }

    /**
     * Sets the hint text color, size, style from the specified TextAppearance resource.
     *
     * @attr ref android.support.design.R.styleable#TextInputLayout_hintTextAppearance
     */
    public void setHintTextAppearance(@StyleRes int resId) {
        mCollapsingTextHelper.setCollapsedTextAppearance(resId);
        mFocusedTextColor = ColorStateList.valueOf(mCollapsingTextHelper.getCollapsedTextColor());

        if (mEditText != null) {
            updateLabelVisibility(false);

            // Text size might have changed so update the top margin
            LayoutParams lp = updateEditTextMargin(mEditText.getLayoutParams());
            mEditText.setLayoutParams(lp);
            mEditText.requestLayout();
        }
    }

    private void addIndicator(TextView indicator, int index, LinearLayout.LayoutParams params) {
        if (mIndicatorArea == null) {
            mIndicatorArea = new LinearLayout(getContext());
            mIndicatorArea.setOrientation(LinearLayout.HORIZONTAL);
            addView(mIndicatorArea);
            if (mEditText != null) {
                adjustIndicatorPadding();
            }
        }
        mIndicatorArea.addView(indicator, index, params);
    }

    private void adjustIndicatorPadding() {
        // Add padding to the error and character counter so that they match the EditText
        ViewCompat.setPaddingRelative(mIndicatorArea, ViewCompat.getPaddingStart(mEditText),
                0, ViewCompat.getPaddingEnd(mEditText), mEditText.getPaddingBottom());
    }

    private void removeIndicator(TextView indicator) {
        mIndicatorArea.removeView(indicator);
        if (mIndicatorArea.getChildCount() == 0) {
            removeView(mIndicatorArea);
        }
    }

    /**
     * Whether the error functionality is enabled or not in this layout. Enabling this
     * functionality before setting an error message via {@link #setError(CharSequence)}, will mean
     * that this layout will not change size when an error is displayed.
     *
     * @attr ref android.support.design.R.styleable#TextInputLayout_errorEnabled
     */
    public void setErrorEnabled(boolean enabled) {
        if (mErrorEnabled != enabled) {
            if (mErrorView != null) {
                ViewCompat.animate(mErrorView).cancel();
            }

            if (enabled) {
                mErrorView = new TextView(getContext());
                mErrorView.setTextAppearance(getContext(), mErrorTextAppearance);
                mErrorView.setVisibility(INVISIBLE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.weight = 1.f;
                addIndicator(mErrorView, 0, params);
            } else {
                mErrorShown = false;
                updateEditTextBackground();
                removeIndicator(mErrorView);
                mErrorView = null;
            }
            mErrorEnabled = enabled;
        }
    }

    /**
     * Returns whether the error functionality is enabled or not in this layout.
     *
     * @attr ref android.support.design.R.styleable#TextInputLayout_errorEnabled
     *
     * @see #setErrorEnabled(boolean)
     */
    public boolean isErrorEnabled() {
        return mErrorEnabled;
    }

    /**
     * Sets an error message that will be displayed below our {@link EditText}. If the
     * {@code error} is {@code null}, the error message will be cleared.
     * <p>
     * If the error functionality has not been enabled via {@link #setErrorEnabled(boolean)}, then
     * it will be automatically enabled if {@code error} is not empty.
     *
     * @param error Error message to display, or null to clear
     *
     * @see #getError()
     */
    public void setError(@Nullable CharSequence error) {
        if (!mErrorEnabled) {
            if (TextUtils.isEmpty(error)) {
                // If error isn't enabled, and the error is empty, just return
                return;
            }
            // Else, we'll assume that they want to enable the error functionality
            setErrorEnabled(true);
        }

        if (!TextUtils.isEmpty(error)) {
            ViewCompat.setAlpha(mErrorView, 0f);
            mErrorView.setText(error);
            ViewCompat.animate(mErrorView)
                    .alpha(1f)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(View view) {
                            view.setVisibility(VISIBLE);
                        }
                    })
                    .start();

            // Set the EditText's background tint to the error color
            mErrorShown = true;
            updateEditTextBackground();
            updateLabelVisibility(true);
        } else {
            if (mErrorView.getVisibility() == VISIBLE) {
                ViewCompat.animate(mErrorView)
                        .alpha(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                        .setListener(new ViewPropertyAnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(View view) {
                                view.setVisibility(INVISIBLE);

                                updateLabelVisibility(true);
                            }
                        }).start();

                // Restore the 'original' tint, using colorControlNormal and colorControlActivated
                mErrorShown = false;
                updateEditTextBackground();
            }
        }

        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }

    /**
     * Whether the character counter functionality is enabled or not in this layout.
     *
     * @attr ref android.support.design.R.styleable#TextInputLayout_counterEnabled
     */
    public void setCounterEnabled(boolean enabled) {
        if (mCounterEnabled != enabled) {
            if (enabled) {
                mCounterView = new TextView(getContext());
                mCounterView.setMaxLines(1);
                mCounterView.setTextAppearance(getContext(), mCounterTextAppearance);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = (params.gravity & Gravity.VERTICAL_GRAVITY_MASK) |
                        GravityCompat.END;
                addIndicator(mCounterView, -1, params);
                if (mEditText == null) {
                    updateCounter(0);
                } else {
                    updateCounter(mEditText.getText().length());
                }
            } else {
                removeIndicator(mCounterView);
                mCounterView = null;
            }
            mCounterEnabled = enabled;
        }
    }

    /**
     * Sets the max length to display at the character counter.
     *
     * @param maxLength maxLength to display. Any value less than or equal to 0 will not be shown.
     */
    public void setCounterMaxLength(int maxLength) {
        if (mCounterMaxLength != maxLength) {
            if (maxLength > 0) {
                mCounterMaxLength = maxLength;
            } else {
                mCounterMaxLength = INVALID_MAX_LENGTH;
            }
            if (mCounterEnabled) {
                updateCounter(mEditText == null ? 0 : mEditText.getText().length());
            }
        }
    }

    /**
     * Returns the max length shown at the character counter.
     */
    public int getCounterMaxLength() {
        return mCounterMaxLength;
    }

    private void updateCounter(int length) {
        boolean wasCounterOverflowed = mCounterOverflowed;
        if (mCounterMaxLength == INVALID_MAX_LENGTH) {
            mCounterView.setText(String.valueOf(length));
            mCounterOverflowed = false;
        } else {
            mCounterOverflowed = length > mCounterMaxLength;
            if (wasCounterOverflowed != mCounterOverflowed) {
                mCounterView.setTextAppearance(getContext(), mCounterOverflowed ?
                        mCounterOverflowTextAppearance : mCounterTextAppearance);
            }
            mCounterView.setText(getContext().getString(R.string.character_counter_pattern,
                    length, mCounterMaxLength));
        }
        if (mEditText != null && wasCounterOverflowed != mCounterOverflowed) {
            updateEditTextBackground();
        }
    }

    private void updateEditTextBackground() {
        if (mErrorShown && mErrorView != null) {
            // Set the EditText's background tint to the error color
            ViewCompat.setBackgroundTintList(mEditText,
                    ColorStateList.valueOf(mErrorView.getCurrentTextColor()));
        } else if (mCounterOverflowed && mCounterView != null) {
            ViewCompat.setBackgroundTintList(mEditText,
                    ColorStateList.valueOf(mCounterView.getCurrentTextColor()));
        } else {
            final TintManager tintManager = TintManager.get(getContext());
            ViewCompat.setBackgroundTintList(mEditText,
                    tintManager.getTintList(R.drawable.abc_edit_text_material));
        }
    }

    /**
     * Returns the error message that was set to be displayed with
     * {@link #setError(CharSequence)}, or <code>null</code> if no error was set
     * or if error displaying is not enabled.
     *
     * @see #setError(CharSequence)
     */
    @Nullable
    public CharSequence getError() {
        if (mErrorEnabled && mErrorView != null && mErrorView.getVisibility() == VISIBLE) {
            return mErrorView.getText();
        }
        return null;
    }

    /**
     * Returns whether any hint state changes, due to being focused or non-empty text, are
     * animated.
     *
     * @see #setHintAnimationEnabled(boolean)
     *
     * @attr ref android.support.design.R.styleable#TextInputLayout_hintAnimationEnabled
     */
    public boolean isHintAnimationEnabled() {
        return mHintAnimationEnabled;
    }

    /**
     * Set whether any hint state changes, due to being focused or non-empty text, are
     * animated.
     *
     * @see #isHintAnimationEnabled()
     *
     * @attr ref android.support.design.R.styleable#TextInputLayout_hintAnimationEnabled
     */
    public void setHintAnimationEnabled(boolean enabled) {
        mHintAnimationEnabled = enabled;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        mCollapsingTextHelper.draw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mEditText != null) {
            final int l = mEditText.getLeft() + mEditText.getCompoundPaddingLeft();
            final int r = mEditText.getRight() - mEditText.getCompoundPaddingRight();

            mCollapsingTextHelper.setExpandedBounds(l,
                    mEditText.getTop() + mEditText.getCompoundPaddingTop(),
                    r, mEditText.getBottom() - mEditText.getCompoundPaddingBottom());

            // Set the collapsed bounds to be the the full height (minus padding) to match the
            // EditText's editable area
            mCollapsingTextHelper.setCollapsedBounds(l, getPaddingTop(),
                    r, bottom - top - getPaddingBottom());

            mCollapsingTextHelper.recalculate();
        }
    }

    @Override
    public void refreshDrawableState() {
        super.refreshDrawableState();
        // Drawable state has changed so see if we need to update the label
        updateLabelVisibility(ViewCompat.isLaidOut(this));
    }

    private void collapseHint(boolean animate) {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        if (animate && mHintAnimationEnabled) {
            animateToExpansionFraction(1f);
        } else {
            mCollapsingTextHelper.setExpansionFraction(1f);
        }
    }

    private void expandHint(boolean animate) {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        if (animate && mHintAnimationEnabled) {
            animateToExpansionFraction(0f);
        } else {
            mCollapsingTextHelper.setExpansionFraction(0f);
        }
    }

    private void animateToExpansionFraction(final float target) {
        if (mCollapsingTextHelper.getExpansionFraction() == target) {
            return;
        }
        if (mAnimator == null) {
            mAnimator = ViewUtils.createAnimator();
            mAnimator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
            mAnimator.setDuration(ANIMATION_DURATION);
            mAnimator.setUpdateListener(new ValueAnimatorCompat.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimatorCompat animator) {
                    mCollapsingTextHelper.setExpansionFraction(animator.getAnimatedFloatValue());
                }
            });
        }
        mAnimator.setFloatValues(mCollapsingTextHelper.getExpansionFraction(), target);
        mAnimator.start();
    }

    private int getThemeAttrColor(int attr) {
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(attr, tv, true)) {
            return tv.data;
        } else {
            return Color.MAGENTA;
        }
    }

    private class TextInputAccessibilityDelegate extends AccessibilityDelegateCompat {
        @Override
        public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(host, event);
            event.setClassName(TextInputLayout.class.getSimpleName());
        }

        @Override
        public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onPopulateAccessibilityEvent(host, event);

            final CharSequence text = mCollapsingTextHelper.getText();
            if (!TextUtils.isEmpty(text)) {
                event.getText().add(text);
            }
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.setClassName(TextInputLayout.class.getSimpleName());

            final CharSequence text = mCollapsingTextHelper.getText();
            if (!TextUtils.isEmpty(text)) {
                info.setText(text);
            }
            if (mEditText != null) {
                info.setLabelFor(mEditText);
            }
            final CharSequence error = mErrorView != null ? mErrorView.getText() : null;
            if (!TextUtils.isEmpty(error)) {
                info.setContentInvalid(true);
                info.setError(error);
            }
        }
    }

    private static boolean arrayContains(int[] array, int value) {
        for (int v : array) {
            if (v == value) {
                return true;
            }
        }
        return false;
    }
}