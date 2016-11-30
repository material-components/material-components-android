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
import android.os.Handler;
import android.os.Message;
import android.support.design.R;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
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
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Layout which wraps an {@link android.widget.EditText} to show a floating label when
 * the hint is hidden due to the user inputting text.
 */
public class TextInputLayout extends LinearLayout {

    private static final long ANIMATION_DURATION = 200;
    private static final int MSG_UPDATE_LABEL = 0;

    private EditText mEditText;
    private CharSequence mHint;

    private boolean mErrorEnabled;
    private TextView mErrorView;
    private int mErrorTextAppearance;

    private ColorStateList mLabelTextColor;

    private final CollapsingTextHelper mCollapsingTextHelper;
    private final Handler mHandler;

    public TextInputLayout(Context context) {
        this(context, null);
    }

    public TextInputLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);
        setWillNotDraw(false);

        mCollapsingTextHelper = new CollapsingTextHelper(this);
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_LABEL:
                        updateLabelVisibility(true);
                        return true;
                }
                return false;
            }
        });

        mCollapsingTextHelper.setTextSizeInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
        mCollapsingTextHelper.setPositionInterpolator(new AccelerateInterpolator());
        mCollapsingTextHelper.setCollapsedTextVerticalGravity(Gravity.TOP);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.TextInputLayout, defStyleAttr, R.style.Widget_Design_TextInputLayout);
        mHint = a.getText(R.styleable.TextInputLayout_android_hint);

        final int hintAppearance = a.getResourceId(
                R.styleable.TextInputLayout_hintTextAppearance, -1);
        if (hintAppearance != -1) {
            mCollapsingTextHelper.setCollapsedTextAppearance(hintAppearance);
        }

        mErrorTextAppearance = a.getResourceId(R.styleable.TextInputLayout_errorTextAppearance, 0);
        final boolean errorEnabled = a.getBoolean(R.styleable.TextInputLayout_errorEnabled, false);

        // We create a ColorStateList using the specified text color, combining it with our
        // theme's textColorHint
        mLabelTextColor = createLabelTextColorStateList(
                mCollapsingTextHelper.getCollapsedTextColor());

        mCollapsingTextHelper.setCollapsedTextColor(mLabelTextColor.getDefaultColor());
        mCollapsingTextHelper.setExpandedTextColor(mLabelTextColor.getDefaultColor());

        a.recycle();

        if (errorEnabled) {
            setErrorEnabled(true);
        }

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
            params = setEditText((EditText) child, params);
            super.addView(child, 0, params);
        } else {
            // Carry on adding the View...
            super.addView(child, index, params);
        }
    }

    private LayoutParams setEditText(EditText editText, ViewGroup.LayoutParams lp) {
        // If we already have an EditText, throw an exception
        if (mEditText != null) {
            throw new IllegalArgumentException("We already have an EditText, can only have one");
        }
        mEditText = editText;

        // Use the EditText's text size for our expanded text
        mCollapsingTextHelper.setExpandedTextSize(mEditText.getTextSize());

        // Add a TextWatcher so that we know when the text input has changed
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mHandler.sendEmptyMessage(MSG_UPDATE_LABEL);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        // Add focus listener to the EditText so that we can notify the label that it is activated.
        // Allows the use of a ColorStateList for the text color on the label
        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                mHandler.sendEmptyMessage(MSG_UPDATE_LABEL);
            }
        });

        // If we do not have a valid hint, try and retrieve it from the EditText
        if (TextUtils.isEmpty(mHint)) {
            setHint(mEditText.getHint());
            // Clear the EditText's hint as we will display it ourselves
            mEditText.setHint(null);
        }

        if (mErrorView != null) {
            // Add some start/end padding to the error so that it matches the EditText
            ViewCompat.setPaddingRelative(mErrorView, ViewCompat.getPaddingStart(mEditText),
                    0, ViewCompat.getPaddingEnd(mEditText), mEditText.getPaddingBottom());
        }

        // Update the label visibility with no animation
        updateLabelVisibility(false);

        // Create a new FrameLayout.LayoutParams so that we can add enough top margin
        // to the EditText so make room for the label
        LayoutParams newLp = new LayoutParams(lp);
        Paint paint = new Paint();
        paint.setTextSize(mCollapsingTextHelper.getExpandedTextSize());
        newLp.topMargin = (int) -paint.ascent();

        return newLp;
    }

    private void updateLabelVisibility(boolean animate) {
        boolean hasText = !TextUtils.isEmpty(mEditText.getText());
        boolean isFocused = mEditText.isFocused();

        mCollapsingTextHelper.setCollapsedTextColor(mLabelTextColor.getColorForState(
                isFocused ? FOCUSED_STATE_SET : EMPTY_STATE_SET,
                mLabelTextColor.getDefaultColor()));

        if (hasText || isFocused) {
            // We should be showing the label so do so if it isn't already
            collapseHint(animate);
        } else {
            // We should not be showing the label so hide it
            expandHint(animate);
        }
    }

    /**
     * @return the {@link android.widget.EditText} text input
     */
    public EditText getEditText() {
        return mEditText;
    }

    /**
     * Set the hint to be displayed in the floating label
     */
    public void setHint(CharSequence hint) {
        mHint = hint;
        mCollapsingTextHelper.setText(hint);

        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }

    /**
     * Whether the error functionality is enabled or not in this layout. Enabling this
     * functionality before setting an error message via {@link #setError(CharSequence)}, will mean
     * that this layout will not change size when an error is displayed.
     *
     * @attr R.attr.errorEnabled
     */
    public void setErrorEnabled(boolean enabled) {
        if (mErrorEnabled != enabled) {
            if (enabled) {
                mErrorView = new TextView(getContext());
                mErrorView.setTextAppearance(getContext(), mErrorTextAppearance);
                mErrorView.setVisibility(INVISIBLE);
                addView(mErrorView);

                if (mEditText != null) {
                    // Add some start/end padding to the error so that it matches the EditText
                    ViewCompat.setPaddingRelative(mErrorView, ViewCompat.getPaddingStart(mEditText),
                            0, ViewCompat.getPaddingEnd(mEditText), mEditText.getPaddingBottom());
                }
            } else {
                removeView(mErrorView);
                mErrorView = null;
            }
            mErrorEnabled = enabled;
        }
    }

    /**
     * Sets an error message that will be displayed below our {@link EditText}. If the
     * {@code error} is {@code null}, the error message will be cleared.
     * <p>
     * If the error functionality has not been enabled via {@link #setErrorEnabled(boolean)}, then
     * it will be automatically enabled if {@code error} is not empty.
     *
     * @param error Error message to display, or null to clear
     */
    public void setError(CharSequence error) {
        if (!mErrorEnabled) {
            if (TextUtils.isEmpty(error)) {
                // If error isn't enabled, and the error is empty, just return
                return;
            }
            // Else, we'll assume that they want to enable the error functionality
            setErrorEnabled(true);
        }

        if (!TextUtils.isEmpty(error)) {
            mErrorView.setText(error);
            mErrorView.setVisibility(VISIBLE);
            ViewCompat.setAlpha(mErrorView, 0f);
            ViewCompat.animate(mErrorView)
                    .alpha(1f)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setListener(null)
                    .start();
        } else {
            if (mErrorView.getVisibility() == VISIBLE) {
                ViewCompat.animate(mErrorView)
                        .alpha(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                        .setListener(new ViewPropertyAnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(View view) {
                                mErrorView.setText(null);
                                mErrorView.setVisibility(INVISIBLE);
                            }
                        }).start();
            }
        }

        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        mCollapsingTextHelper.draw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mCollapsingTextHelper.onLayout(changed, left, top, right, bottom);

        if (mEditText != null) {
            final int l = mEditText.getLeft() + mEditText.getPaddingLeft();
            final int r = mEditText.getRight() - mEditText.getPaddingRight();

            mCollapsingTextHelper.setExpandedBounds(l,
                    mEditText.getTop() + mEditText.getPaddingTop(),
                    r, mEditText.getBottom() - mEditText.getPaddingBottom());

            // Set the collapsed bounds to be the the full height (minus padding) to match the
            // EditText's editable area
            mCollapsingTextHelper.setCollapsedBounds(l, getPaddingTop(),
                    r, bottom - top - getPaddingBottom());
        }
    }

    private void collapseHint(boolean animate) {
        if (animate) {
            animateToExpansionFraction(1f);
        } else {
            mCollapsingTextHelper.setExpansionFraction(1f);
        }
    }

    private void expandHint(boolean animate) {
        if (animate) {
            animateToExpansionFraction(0f);
        } else {
            mCollapsingTextHelper.setExpansionFraction(0f);
        }
    }

    private void animateToExpansionFraction(final float target) {
        final float current = mCollapsingTextHelper.getExpansionFraction();

        Animation anim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                mCollapsingTextHelper.setExpansionFraction(
                        AnimationUtils.lerp(current, target, interpolatedTime));
            }
        };
        anim.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
        anim.setDuration(ANIMATION_DURATION);
        startAnimation(anim);
    }

    private ColorStateList createLabelTextColorStateList(int color) {
        final int[][] states = new int[2][];
        final int[] colors = new int[2];
        int i = 0;

        // Focused
        states[i] = FOCUSED_STATE_SET;
        colors[i] = color;
        i++;

        states[i] = EMPTY_STATE_SET;
        colors[i] = getThemeAttrColor(android.R.attr.textColorHint);
        i++;

        return new ColorStateList(states, colors);
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
}