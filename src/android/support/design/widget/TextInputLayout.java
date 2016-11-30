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
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.R;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableWrapper;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.Space;
import android.support.v7.widget.AppCompatDrawableManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
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
 * <p>Also supports showing an error via {@link #setErrorEnabled(boolean)} and
 * {@link #setError(CharSequence)}, and a character counter via
 * {@link #setCounterEnabled(boolean)}.</p>
 *
 * The {@link TextInputEditText} class is provided to be used as a child of this layout. Using
 * TextInputEditText allows TextInputLayout greater control over the visual aspects of any
 * text input. An example usage is as so:
 *
 * <pre>
 * &lt;android.support.design.widget.TextInputLayout
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;wrap_content&quot;&gt;
 *
 *     &lt;android.support.design.widget.TextInputEditText
 *             android:layout_width=&quot;match_parent&quot;
 *             android:layout_height=&quot;wrap_content&quot;
 *             android:hint=&quot;@string/form_username&quot;/&gt;
 *
 * &lt;/android.support.design.widget.TextInputLayout&gt;
 * </pre>
 */
public class TextInputLayout extends LinearLayout {

    private static final int ANIMATION_DURATION = 200;
    private static final int INVALID_MAX_LENGTH = -1;

    private static final String LOG_TAG = "TextInputLayout";

    private EditText mEditText;

    private boolean mHintEnabled;
    private CharSequence mHint;

    private Paint mTmpPaint;

    private LinearLayout mIndicatorArea;
    private int mIndicatorsAdded;

    private boolean mErrorEnabled;
    private TextView mErrorView;
    private int mErrorTextAppearance;
    private boolean mErrorShown;
    private CharSequence mError;

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

    private boolean mHasReconstructedEditTextBackground;

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
        mHintEnabled = a.getBoolean(R.styleable.TextInputLayout_hintEnabled, true);
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
     * Set the typeface to use for both the expanded and floating hint.
     *
     * @param typeface typeface to use, or {@code null} to use the default.
     */
    public void setTypeface(@Nullable Typeface typeface) {
        mCollapsingTextHelper.setTypefaces(typeface);
    }

    /**
     * Returns the typeface used for both the expanded and floating hint.
     */
    @NonNull
    public Typeface getTypeface() {
        // This could be either the collapsed or expanded
        return mCollapsingTextHelper.getCollapsedTypeface();
    }

    private void setEditText(EditText editText) {
        // If we already have an EditText, throw an exception
        if (mEditText != null) {
            throw new IllegalArgumentException("We already have an EditText, can only have one");
        }

        if (!(editText instanceof TextInputEditText)) {
            Log.i(LOG_TAG, "EditText added is not a TextInputEditText. Please switch to using that"
                    + " class instead.");
        }

        mEditText = editText;

        // Use the EditText's typeface, and it's text size for our expanded text
        mCollapsingTextHelper.setTypefaces(mEditText.getTypeface());
        mCollapsingTextHelper.setExpandedTextSize(mEditText.getTextSize());

        final int editTextGravity = mEditText.getGravity();
        mCollapsingTextHelper.setCollapsedTextGravity(
                Gravity.TOP | (editTextGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK));
        mCollapsingTextHelper.setExpandedTextGravity(editTextGravity);

        // Add a TextWatcher so that we know when the text input has changed
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateLabelState(true);
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

        // If we do not have a valid hint, try and retrieve it from the EditText, if enabled
        if (mHintEnabled && TextUtils.isEmpty(mHint)) {
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
        updateLabelState(false);
    }

    private LayoutParams updateEditTextMargin(ViewGroup.LayoutParams lp) {
        // Create/update the LayoutParams so that we can add enough top margin
        // to the EditText so make room for the label
        LayoutParams llp = lp instanceof LayoutParams ? (LayoutParams) lp : new LayoutParams(lp);

        if (mHintEnabled) {
            if (mTmpPaint == null) {
                mTmpPaint = new Paint();
            }
            mTmpPaint.setTypeface(mCollapsingTextHelper.getCollapsedTypeface());
            mTmpPaint.setTextSize(mCollapsingTextHelper.getCollapsedTextSize());
            llp.topMargin = (int) -mTmpPaint.ascent();
        } else {
            llp.topMargin = 0;
        }

        return llp;
    }

    private void updateLabelState(boolean animate) {
        final boolean hasText = mEditText != null && !TextUtils.isEmpty(mEditText.getText());
        final boolean isFocused = arrayContains(getDrawableState(), android.R.attr.state_focused);
        final boolean isErrorShowing = !TextUtils.isEmpty(getError());

        if (mDefaultTextColor != null) {
            mCollapsingTextHelper.setExpandedTextColor(mDefaultTextColor.getDefaultColor());
        }

        if (mCounterOverflowed && mCounterView != null) {
            mCollapsingTextHelper.setCollapsedTextColor(mCounterView.getCurrentTextColor());
        } else if (isFocused && mFocusedTextColor != null) {
            mCollapsingTextHelper.setCollapsedTextColor(mFocusedTextColor.getDefaultColor());
        } else if (mDefaultTextColor != null) {
            mCollapsingTextHelper.setCollapsedTextColor(mDefaultTextColor.getDefaultColor());
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
     * Set the hint to be displayed in the floating label, if enabled.
     *
     * @see #setHintEnabled(boolean)
     *
     * @attr ref android.support.design.R.styleable#TextInputLayout_android_hint
     */
    public void setHint(@Nullable CharSequence hint) {
        if (mHintEnabled) {
            setHintInternal(hint);
            sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
        }
    }

    private void setHintInternal(CharSequence hint) {
        mHint = hint;
        mCollapsingTextHelper.setText(hint);
    }

    /**
     * Returns the hint which is displayed in the floating label, if enabled.
     *
     * @return the hint, or null if there isn't one set, or the hint is not enabled.
     *
     * @attr ref android.support.design.R.styleable#TextInputLayout_android_hint
     */
    @Nullable
    public CharSequence getHint() {
        return mHintEnabled ? mHint : null;
    }

    /**
     * Sets whether the floating label functionality is enabled or not in this layout.
     *
     * <p>If enabled, any non-empty hint in the child EditText will be moved into the floating
     * hint, and its existing hint will be cleared. If disabled, then any non-empty floating hint
     * in this layout will be moved into the EditText, and this layout's hint will be cleared.</p>
     *
     * @see #setHint(CharSequence)
     * @see #isHintEnabled()
     *
     * @attr ref android.support.design.R.styleable#TextInputLayout_hintEnabled
     */
    public void setHintEnabled(boolean enabled) {
        if (enabled != mHintEnabled) {
            mHintEnabled = enabled;

            final CharSequence editTextHint = mEditText.getHint();
            if (!mHintEnabled) {
                if (!TextUtils.isEmpty(mHint) && TextUtils.isEmpty(editTextHint)) {
                    // If the hint is disabled, but we have a hint set, and the EditText doesn't,
                    // pass it through...
                    mEditText.setHint(mHint);
                }
                // Now clear out any set hint
                setHintInternal(null);
            } else {
                if (!TextUtils.isEmpty(editTextHint)) {
                    // If the hint is now enabled and the EditText has one set, we'll use it if
                    // we don't already have one, and clear the EditText's
                    if (TextUtils.isEmpty(mHint)) {
                        setHint(editTextHint);
                    }
                    mEditText.setHint(null);
                }
            }

            // Now update the EditText top margin
            if (mEditText != null) {
                final LayoutParams lp = updateEditTextMargin(mEditText.getLayoutParams());
                mEditText.setLayoutParams(lp);
            }
        }
    }

    /**
     * Returns whether the floating label functionality is enabled or not in this layout.
     *
     * @see #setHintEnabled(boolean)
     *
     * @attr ref android.support.design.R.styleable#TextInputLayout_hintEnabled
     */
    public boolean isHintEnabled() {
        return mHintEnabled;
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
            updateLabelState(false);

            // Text size might have changed so update the top margin
            LayoutParams lp = updateEditTextMargin(mEditText.getLayoutParams());
            mEditText.setLayoutParams(lp);
            mEditText.requestLayout();
        }
    }

    private void addIndicator(TextView indicator, int index) {
        if (mIndicatorArea == null) {
            mIndicatorArea = new LinearLayout(getContext());
            mIndicatorArea.setOrientation(LinearLayout.HORIZONTAL);
            addView(mIndicatorArea, LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            // Add a flexible spacer in the middle so that the left/right views stay pinned
            final Space spacer = new Space(getContext());
            final LinearLayout.LayoutParams spacerLp = new LinearLayout.LayoutParams(0, 0, 1f);
            mIndicatorArea.addView(spacer, spacerLp);

            if (mEditText != null) {
                adjustIndicatorPadding();
            }
        }
        mIndicatorArea.setVisibility(View.VISIBLE);
        mIndicatorArea.addView(indicator, index);
        mIndicatorsAdded++;
    }

    private void adjustIndicatorPadding() {
        // Add padding to the error and character counter so that they match the EditText
        ViewCompat.setPaddingRelative(mIndicatorArea, ViewCompat.getPaddingStart(mEditText),
                0, ViewCompat.getPaddingEnd(mEditText), mEditText.getPaddingBottom());
    }

    private void removeIndicator(TextView indicator) {
        if (mIndicatorArea != null) {
            mIndicatorArea.removeView(indicator);
            if (--mIndicatorsAdded == 0) {
                mIndicatorArea.setVisibility(View.GONE);
            }
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
                try {
                    mErrorView.setTextAppearance(getContext(), mErrorTextAppearance);
                } catch (Exception e) {
                    // Probably caused by our theme not extending from Theme.Design*. Instead
                    // we manually set something appropriate
                    mErrorView.setTextAppearance(getContext(),
                            android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Caption);
                    mErrorView.setTextColor(ContextCompat.getColor(
                            getContext(), R.color.design_textinput_error_color_light));
                }
                mErrorView.setVisibility(INVISIBLE);
                ViewCompat.setAccessibilityLiveRegion(mErrorView,
                        ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
                addIndicator(mErrorView, 0);
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
    public void setError(@Nullable final CharSequence error) {
        mError = error;

        if (!mErrorEnabled) {
            if (TextUtils.isEmpty(error)) {
                // If error isn't enabled, and the error is empty, just return
                return;
            }
            // Else, we'll assume that they want to enable the error functionality
            setErrorEnabled(true);
        }

        // Only animate if we've been laid out already and we have a different error
        final boolean animate = ViewCompat.isLaidOut(this)
                && !TextUtils.equals(mErrorView.getText(), error);
        mErrorShown = !TextUtils.isEmpty(error);

        // Cancel any on-going animation
        ViewCompat.animate(mErrorView).cancel();

        if (mErrorShown) {
            mErrorView.setText(error);
            mErrorView.setVisibility(VISIBLE);

            if (animate) {
                if (ViewCompat.getAlpha(mErrorView) == 1f) {
                    // If it's currently 100% show, we'll animate it from 0
                    ViewCompat.setAlpha(mErrorView, 0f);
                }
                ViewCompat.animate(mErrorView)
                        .alpha(1f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)
                        .setListener(new ViewPropertyAnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(View view) {
                                view.setVisibility(VISIBLE);
                            }
                        }).start();
            } else {
                // Set alpha to 1f, just in case
                ViewCompat.setAlpha(mErrorView, 1f);
            }
        } else {
            if (mErrorView.getVisibility() == VISIBLE) {
                if (animate) {
                    ViewCompat.animate(mErrorView)
                            .alpha(0f)
                            .setDuration(ANIMATION_DURATION)
                            .setInterpolator(AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR)
                            .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(View view) {
                                    mErrorView.setText(error);
                                    view.setVisibility(INVISIBLE);
                                }
                            }).start();
                } else {
                    mErrorView.setText(error);
                    mErrorView.setVisibility(INVISIBLE);
                }
            }
        }

        updateEditTextBackground();
        updateLabelState(true);
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
                try {
                    mCounterView.setTextAppearance(getContext(), mCounterTextAppearance);
                } catch (Exception e) {
                    // Probably caused by our theme not extending from Theme.Design*. Instead
                    // we manually set something appropriate
                    mCounterView.setTextAppearance(getContext(),
                            android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Caption);
                    mCounterView.setTextColor(ContextCompat.getColor(
                            getContext(), R.color.design_textinput_error_color_light));
                }
                addIndicator(mCounterView, -1);
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
     * Returns whether the character counter functionality is enabled or not in this layout.
     *
     * @attr ref android.support.design.R.styleable#TextInputLayout_counterEnabled
     *
     * @see #setCounterEnabled(boolean)
     */
    public boolean isCounterEnabled() {
        return mCounterEnabled;
    }

    /**
     * Sets the max length to display at the character counter.
     *
     * @param maxLength maxLength to display. Any value less than or equal to 0 will not be shown.
     *
     * @attr ref android.support.design.R.styleable#TextInputLayout_counterMaxLength
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
     *
     * @attr ref android.support.design.R.styleable#TextInputLayout_counterMaxLength
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
            updateLabelState(false);
            updateEditTextBackground();
        }
    }

    private void updateEditTextBackground() {
        ensureBackgroundDrawableStateWorkaround();

        Drawable editTextBackground = mEditText.getBackground();
        if (editTextBackground == null) {
            return;
        }

        if (android.support.v7.widget.DrawableUtils.canSafelyMutateDrawable(editTextBackground)) {
            editTextBackground = editTextBackground.mutate();
        }

        if (mErrorShown && mErrorView != null) {
            // Set a color filter of the error color
            editTextBackground.setColorFilter(
                    AppCompatDrawableManager.getPorterDuffColorFilter(
                            mErrorView.getCurrentTextColor(), PorterDuff.Mode.SRC_IN));
        } else if (mCounterOverflowed && mCounterView != null) {
            // Set a color filter of the counter color
            editTextBackground.setColorFilter(
                    AppCompatDrawableManager.getPorterDuffColorFilter(
                            mCounterView.getCurrentTextColor(), PorterDuff.Mode.SRC_IN));
        } else {
            // Else reset the color filter and refresh the drawable state so that the
            // normal tint is used
            clearColorFilter(editTextBackground);
            mEditText.refreshDrawableState();
        }
    }

    private static void clearColorFilter(@NonNull Drawable drawable) {
        drawable.clearColorFilter();

        if (Build.VERSION.SDK_INT == 21 || Build.VERSION.SDK_INT == 22) {
            // API 21 + 22 have an issue where clearing a color filter on a DrawableContainer
            // will not propagate to all of its children. To workaround this we unwrap the drawable
            // to find any DrawableContainers, and then unwrap those to clear the filter on its
            // children manually
            if (drawable instanceof InsetDrawable) {
                clearColorFilter(((InsetDrawable) drawable).getDrawable());
            } else if (drawable instanceof DrawableWrapper) {
                clearColorFilter(((DrawableWrapper) drawable).getWrappedDrawable());
            } else if (drawable instanceof DrawableContainer) {
                final DrawableContainer container = (DrawableContainer) drawable;
                final DrawableContainer.DrawableContainerState state =
                        (DrawableContainer.DrawableContainerState) container.getConstantState();
                if (state != null) {
                    for (int i = 0, count = state.getChildCount(); i < count; i++) {
                        clearColorFilter(state.getChild(i));
                    }
                }
            }
        }
    }

    private void ensureBackgroundDrawableStateWorkaround() {
        final int sdk = Build.VERSION.SDK_INT;
        if (sdk != 21 && sdk != 22) {
            // The workaround is only required on API 21-22
            return;
        }
        final Drawable bg = mEditText.getBackground();
        if (bg == null) {
            return;
        }

        if (!mHasReconstructedEditTextBackground) {
            // This is gross. There is an issue in the platform which affects container Drawables
            // where the first drawable retrieved from resources will propogate any changes
            // (like color filter) to all instances from the cache. We'll try to workaround it...

            final Drawable newBg = bg.getConstantState().newDrawable();

            if (bg instanceof DrawableContainer) {
                // If we have a Drawable container, we can try and set it's constant state via
                // reflection from the new Drawable
                mHasReconstructedEditTextBackground =
                        DrawableUtils.setContainerConstantState(
                                (DrawableContainer) bg, newBg.getConstantState());
            }

            if (!mHasReconstructedEditTextBackground) {
                // If we reach here then we just need to set a brand new instance of the Drawable
                // as the background. This has the unfortunate side-effect of wiping out any
                // user set padding, but I'd hope that use of custom padding on an EditText
                // is limited.
                mEditText.setBackgroundDrawable(newBg);
                mHasReconstructedEditTextBackground = true;
            }
        }
    }

    static class SavedState extends AbsSavedState {
        CharSequence error;

        SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            error = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);

        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            TextUtils.writeToParcel(error, dest, flags);
        }

        @Override
        public String toString() {
            return "TextInputLayout.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " error=" + error + "}";
        }

        public static final Creator<SavedState> CREATOR = ParcelableCompat.newCreator(
                new ParcelableCompatCreatorCallbacks<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                        return new SavedState(in, loader);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                });
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        if (mErrorShown) {
            ss.error = getError();
        }
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setError(ss.error);
        requestLayout();
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
        return mErrorEnabled ? mError : null;
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

        if (mHintEnabled) {
            mCollapsingTextHelper.draw(canvas);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mHintEnabled && mEditText != null) {
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
        updateLabelState(ViewCompat.isLaidOut(this));
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