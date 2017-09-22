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

import static android.support.design.widget.IndicatorViewController.COUNTER_INDEX;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.annotation.VisibleForTesting;
import android.support.design.R;
import android.support.design.animation.AnimationUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.TintTypedArray;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Layout which wraps an {@link android.widget.EditText} (or descendant) to show a floating label
 * when the hint is hidden while the user inputs text.
 *
 * <p>Also supports:
 *
 * <ul>
 *   <li>Showing an error via {@link #setErrorEnabled(boolean)} and {@link #setError(CharSequence)}
 *   <li>Showing helper text via {@link #setHelperTextEnabled(boolean)} and {@link
 *       #setHelperText(CharSequence)}
 *   <li>Showing a character counter via {@link #setCounterEnabled(boolean)} and {@link
 *       #setCounterMaxLength(int)}
 *   <li>Password visibility toggling via the {@link #setPasswordVisibilityToggleEnabled(boolean)}
 *       API and related attribute. If enabled, a button is displayed to toggle between the password
 *       being displayed as plain-text or disguised, when your EditText is set to display a
 *       password.
 *       <p><strong>Note:</strong> When using the password toggle functionality, the 'end' compound
 *       drawable of the EditText will be overridden while the toggle is enabled. To ensure that any
 *       existing drawables are restored correctly, you should set those compound drawables
 *       relatively (start/end), opposed to absolutely (left/right).
 * </ul>
 *
 * <p>The {@link TextInputEditText} class is provided to be used as a child of this layout. Using
 * TextInputEditText allows TextInputLayout greater control over the visual aspects of any text
 * input. An example usage is as so:
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
 *
 * <p><strong>Note:</strong> The actual view hierarchy present under TextInputLayout is
 * <strong>NOT</strong> guaranteed to match the view hierarchy as written in XML. As a result, calls
 * to getParent() on children of the TextInputLayout -- such as an TextInputEditText -- may not
 * return the TextInputLayout itself, but rather an intermediate View. If you need to access a View
 * directly, set an {@code android:id} and use {@link View#findViewById(int)}.
 */
public class TextInputLayout extends LinearLayout {

  /** Duration for the label's scale up and down animations. */
  private static final int LABEL_SCALE_ANIMATION_DURATION = 167;

  private static final int INVALID_MAX_LENGTH = -1;

  private static final String LOG_TAG = "TextInputLayout";

  private final FrameLayout mInputFrame;
  EditText mEditText;

  private final IndicatorViewController indicatorViewController = new IndicatorViewController(this);

  boolean mCounterEnabled;
  private int mCounterMaxLength;
  private boolean mCounterOverflowed;
  private TextView mCounterView;
  private final int mCounterOverflowTextAppearance;
  private final int mCounterTextAppearance;

  private boolean mHintEnabled;
  private CharSequence mHint;

  private GradientDrawable mBoxBackground;
  private final int mBoxPaddingOffsetPx;
  @BoxBackgroundMode private int mBoxBackgroundMode;
  private float mBoxCornerRadius;
  private int mBoxStrokeWidth;
  private ColorStateList mBoxStrokeColor;
  private ColorStateList mBoxBackgroundColor;
  private Drawable mEditTextOriginalDrawable;

  /**
   * Values for box background mode. There is either a filled background, an outline background, or
   * no background.
   */
  @IntDef({BOX_BACKGROUND_NONE, BOX_BACKGROUND_FILLED, BOX_BACKGROUND_OUTLINE})
  public @interface BoxBackgroundMode {}

  public static final int BOX_BACKGROUND_NONE = 0;
  public static final int BOX_BACKGROUND_FILLED = 1;
  public static final int BOX_BACKGROUND_OUTLINE = 2;

  private Paint mTmpPaint;
  private final Rect mTmpRect = new Rect();
  private Typeface mTypeface;

  private boolean mPasswordToggleEnabled;
  private Drawable mPasswordToggleDrawable;
  private CharSequence mPasswordToggleContentDesc;
  private CheckableImageButton mPasswordToggleView;
  private boolean mPasswordToggledVisible;
  private Drawable mPasswordToggleDummyDrawable;
  private Drawable mOriginalEditTextEndDrawable;

  private ColorStateList mPasswordToggleTintList;
  private boolean mHasPasswordToggleTintList;
  private PorterDuff.Mode mPasswordToggleTintMode;
  private boolean mHasPasswordToggleTintMode;

  private ColorStateList mDefaultTextColor;
  private ColorStateList mFocusedTextColor;

  // Only used for testing
  private boolean mHintExpanded;

  final CollapsingTextHelper mCollapsingTextHelper = new CollapsingTextHelper(this);

  private boolean mHintAnimationEnabled;
  private ValueAnimator mAnimator;

  private boolean mHasReconstructedEditTextBackground;
  private boolean mInDrawableStateChanged;

  private boolean mRestoringSavedState;

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

    mInputFrame = new FrameLayout(context);
    mInputFrame.setAddStatesFromChildren(true);
    addView(mInputFrame);

    mCollapsingTextHelper.setTextSizeInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    mCollapsingTextHelper.setPositionInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    mCollapsingTextHelper.setCollapsedTextGravity(Gravity.TOP | GravityCompat.START);

    final TintTypedArray a =
        TintTypedArray.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.TextInputLayout,
            defStyleAttr,
            R.style.Widget_Design_TextInputLayout);

    @BoxBackgroundMode
    final int boxBackgroundMode =
        a.getInt(R.styleable.TextInputLayout_boxBackgroundMode, BOX_BACKGROUND_NONE);
    setBoxBackgroundMode(boxBackgroundMode);
    mBoxPaddingOffsetPx =
        context.getResources().getDimensionPixelOffset(R.dimen.design_textinput_box_offset);

    mHintEnabled = a.getBoolean(R.styleable.TextInputLayout_hintEnabled, true);
    setHint(a.getText(R.styleable.TextInputLayout_android_hint));
    mHintAnimationEnabled = a.getBoolean(R.styleable.TextInputLayout_hintAnimationEnabled, true);

    if (a.hasValue(R.styleable.TextInputLayout_android_textColorHint)) {
      mDefaultTextColor =
          mFocusedTextColor =
              a.getColorStateList(R.styleable.TextInputLayout_android_textColorHint);
    }

    final int hintAppearance = a.getResourceId(R.styleable.TextInputLayout_hintTextAppearance, -1);
    if (hintAppearance != -1) {
      setHintTextAppearance(a.getResourceId(R.styleable.TextInputLayout_hintTextAppearance, 0));
    }

    final int mErrorTextAppearance =
        a.getResourceId(R.styleable.TextInputLayout_errorTextAppearance, 0);
    final boolean errorEnabled = a.getBoolean(R.styleable.TextInputLayout_errorEnabled, false);

    final int mHelperTextTextAppearance =
        a.getResourceId(R.styleable.TextInputLayout_helperTextTextAppearance, 0);
    final boolean helperTextEnabled =
        a.getBoolean(R.styleable.TextInputLayout_helperTextEnabled, false);

    final boolean counterEnabled = a.getBoolean(R.styleable.TextInputLayout_counterEnabled, false);
    setCounterMaxLength(a.getInt(R.styleable.TextInputLayout_counterMaxLength, INVALID_MAX_LENGTH));
    mCounterTextAppearance = a.getResourceId(R.styleable.TextInputLayout_counterTextAppearance, 0);
    mCounterOverflowTextAppearance =
        a.getResourceId(R.styleable.TextInputLayout_counterOverflowTextAppearance, 0);

    mPasswordToggleEnabled = a.getBoolean(R.styleable.TextInputLayout_passwordToggleEnabled, false);
    mPasswordToggleDrawable = a.getDrawable(R.styleable.TextInputLayout_passwordToggleDrawable);
    mPasswordToggleContentDesc =
        a.getText(R.styleable.TextInputLayout_passwordToggleContentDescription);
    if (a.hasValue(R.styleable.TextInputLayout_passwordToggleTint)) {
      mHasPasswordToggleTintList = true;
      mPasswordToggleTintList = a.getColorStateList(R.styleable.TextInputLayout_passwordToggleTint);
    }
    if (a.hasValue(R.styleable.TextInputLayout_passwordToggleTintMode)) {
      mHasPasswordToggleTintMode = true;
      mPasswordToggleTintMode =
          ViewUtils.parseTintMode(
              a.getInt(R.styleable.TextInputLayout_passwordToggleTintMode, -1), null);
    }

    a.recycle();

    setHelperTextEnabled(helperTextEnabled);
    setHelperTextTextAppearance(mHelperTextTextAppearance);
    setErrorEnabled(errorEnabled);
    setErrorTextAppearance(mErrorTextAppearance);
    setCounterEnabled(counterEnabled);

    applyPasswordToggleTint();

    if (ViewCompat.getImportantForAccessibility(this)
        == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
      // Make sure we're important for accessibility if we haven't been explicitly not
      ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    ViewCompat.setAccessibilityDelegate(this, new TextInputAccessibilityDelegate());
  }

  @Override
  public void addView(View child, int index, final ViewGroup.LayoutParams params) {
    if (child instanceof EditText) {
      // Make sure that the EditText is vertically at the bottom, so that it sits on the
      // EditText's underline
      FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(params);
      flp.gravity = Gravity.CENTER_VERTICAL | (flp.gravity & ~Gravity.VERTICAL_GRAVITY_MASK);
      mInputFrame.addView(child, flp);

      // Now use the EditText's LayoutParams as our own and update them to make enough space
      // for the label
      mInputFrame.setLayoutParams(params);
      updateInputLayoutMargins();

      setEditText((EditText) child);
    } else {
      // Carry on adding the View...
      super.addView(child, index, params);
    }
  }

  /**
   * Set the mode for the box's background (filled, outline, or none).
   *
   * @param boxBackgroundMode the box's background mode.
   */
  public void setBoxBackgroundMode(@BoxBackgroundMode int boxBackgroundMode) {
    if (boxBackgroundMode == mBoxBackgroundMode) {
      return;
    }
    mBoxBackgroundMode = boxBackgroundMode;
    mBoxBackground = mBoxBackgroundMode != BOX_BACKGROUND_NONE ? new GradientDrawable() : null;
  }

  /**
   * Set the typeface to use for the hint and any label views (such as counter and error views).
   *
   * @param typeface typeface to use, or {@code null} to use the default.
   */
  @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
  public void setTypeface(@Nullable Typeface typeface) {
    if (typeface != mTypeface) {
      mTypeface = typeface;

      mCollapsingTextHelper.setTypefaces(typeface);
      indicatorViewController.setTypefaces(typeface);

      if (mCounterView != null) {
        mCounterView.setTypeface(typeface);
      }
    }
  }

  /**
   * Returns the typeface used for the hint and any label views (such as counter and error views).
   */
  @Nullable
  public Typeface getTypeface() {
    return mTypeface;
  }

  private void setEditText(EditText editText) {
    // If we already have an EditText, throw an exception
    if (mEditText != null) {
      throw new IllegalArgumentException("We already have an EditText, can only have one");
    }

    if (!(editText instanceof TextInputEditText)) {
      Log.i(
          LOG_TAG,
          "EditText added is not a TextInputEditText. Please switch to using that"
              + " class instead.");
    }

    mEditText = editText;

    final boolean hasPasswordTransformation = hasPasswordTransformation();

    // Use the EditText's typeface, and its text size for our expanded text.
    if (!hasPasswordTransformation) {
      // We don't want a monospace font just because we have a password field
      mCollapsingTextHelper.setTypefaces(mEditText.getTypeface());
    }
    mCollapsingTextHelper.setExpandedTextSize(mEditText.getTextSize());

    final int editTextGravity = mEditText.getGravity();
    mCollapsingTextHelper.setCollapsedTextGravity(
        Gravity.TOP | (editTextGravity & ~Gravity.VERTICAL_GRAVITY_MASK));
    mCollapsingTextHelper.setExpandedTextGravity(editTextGravity);

    // Add a TextWatcher so that we know when the text input has changed
    mEditText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void afterTextChanged(Editable s) {
            updateLabelState(!mRestoringSavedState);
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

    indicatorViewController.adjustIndicatorPadding();

    updatePasswordToggleView();

    // Update the label visibility with no animation, but force a state change
    updateLabelState(false, true);
  }

  private void updateInputLayoutMargins() {
    // Create/update the LayoutParams so that we can add enough top margin
    // to the EditText to make room for the label.
    final LayoutParams lp = (LayoutParams) mInputFrame.getLayoutParams();
    final int newTopMargin;

    if (mHintEnabled) {
      if (mTmpPaint == null) {
        mTmpPaint = new Paint();
      }
      mTmpPaint.setTypeface(mCollapsingTextHelper.getCollapsedTypeface());
      mTmpPaint.setTextSize(mCollapsingTextHelper.getCollapsedTextSize());
      newTopMargin = (int) -mTmpPaint.ascent();
    } else {
      newTopMargin = 0;
    }

    if (newTopMargin != lp.topMargin) {
      lp.topMargin = newTopMargin;
      mInputFrame.requestLayout();
    }
  }

  void updateLabelState(boolean animate) {
    updateLabelState(animate, false);
  }

  private void updateLabelState(boolean animate, boolean force) {
    final boolean isEnabled = isEnabled();
    final boolean hasText = mEditText != null && !TextUtils.isEmpty(mEditText.getText());
    final boolean isFocused = arrayContains(getDrawableState(), android.R.attr.state_focused);
    final boolean errorShouldBeShown = indicatorViewController.errorShouldBeShown();

    if (mDefaultTextColor != null) {
      mCollapsingTextHelper.setExpandedTextColor(mDefaultTextColor);
    }

    if (isEnabled && errorShouldBeShown) {
      mCollapsingTextHelper.setCollapsedTextColor(indicatorViewController.getErrorViewTextColors());
    } else if (isEnabled && mCounterOverflowed && mCounterView != null) {
      mCollapsingTextHelper.setCollapsedTextColor(mCounterView.getTextColors());
    } else if (isEnabled && isFocused && mFocusedTextColor != null) {
      mCollapsingTextHelper.setCollapsedTextColor(mFocusedTextColor);
    } else if (mDefaultTextColor != null) {
      mCollapsingTextHelper.setCollapsedTextColor(mDefaultTextColor);
    }

    if (hasText || (isEnabled() && (isFocused || errorShouldBeShown))) {
      // We should be showing the label so do so if it isn't already
      if (force || mHintExpanded) {
        collapseHint(animate);
      }
    } else {
      // We should not be showing the label so hide it
      if (force || !mHintExpanded) {
        expandHint(animate);
      }
    }
  }

  /** Returns the {@link android.widget.EditText} used for text input. */
  @Nullable
  public EditText getEditText() {
    return mEditText;
  }

  /**
   * Set the hint to be displayed in the floating label, if enabled.
   *
   * @see #setHintEnabled(boolean)
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
   * @attr ref android.support.design.R.styleable#TextInputLayout_android_hint
   */
  @Nullable
  public CharSequence getHint() {
    return mHintEnabled ? mHint : null;
  }

  /**
   * Sets whether the floating label functionality is enabled or not in this layout.
   *
   * <p>If enabled, any non-empty hint in the child EditText will be moved into the floating hint,
   * and its existing hint will be cleared. If disabled, then any non-empty floating hint in this
   * layout will be moved into the EditText, and this layout's hint will be cleared.
   *
   * @see #setHint(CharSequence)
   * @see #isHintEnabled()
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
        updateInputLayoutMargins();
      }
    }
  }

  /**
   * Returns whether the floating label functionality is enabled or not in this layout.
   *
   * @see #setHintEnabled(boolean)
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
    mFocusedTextColor = mCollapsingTextHelper.getCollapsedTextColor();

    if (mEditText != null) {
      updateLabelState(false);
      // Text size might have changed so update the top margin
      updateInputLayoutMargins();
    }
  }

  /**
   * Whether the error functionality is enabled or not in this layout. Enabling this functionality
   * before setting an error message via {@link #setError(CharSequence)}, will mean that this layout
   * will not change size when an error is displayed.
   *
   * @attr ref android.support.design.R.styleable#TextInputLayout_errorEnabled
   */
  public void setErrorEnabled(boolean enabled) {
    indicatorViewController.setErrorEnabled(enabled);
  }

  /**
   * Sets the text color and size for the error message from the specified TextAppearance resource.
   *
   * @attr ref android.support.design.R.styleable#TextInputLayout_errorTextAppearance
   */
  public void setErrorTextAppearance(@StyleRes int resId) {
    indicatorViewController.setErrorTextAppearance(resId);
  }

  /**
   * Sets the text color and size for the helper text from the specified TextAppearance resource.
   *
   * @attr ref android.support.design.R.styleable#TextInputLayout_helperTextTextAppearance
   */
  public void setHelperTextTextAppearance(@StyleRes int resId) {
    indicatorViewController.setHelperTextAppearance(resId);
  }

  /**
   * Returns whether the error functionality is enabled or not in this layout.
   *
   * @attr ref android.support.design.R.styleable#TextInputLayout_errorEnabled
   * @see #setErrorEnabled(boolean)
   */
  public boolean isErrorEnabled() {
    return indicatorViewController.isErrorEnabled();
  }

  /**
   * Whether the helper text functionality is enabled or not in this layout. Enabling this
   * functionality before setting a helper message via {@link #setHelperText(CharSequence)} will
   * mean that this layout will not change size when a helper message is displayed.
   *
   * @attr ref android.support.design.R.styleable#TextInputLayout_helperTextEnabled
   */
  public void setHelperTextEnabled(boolean enabled) {
    indicatorViewController.setHelperTextEnabled(enabled);
  }

  /**
   * Sets a helper message that will be displayed below the {@link EditText}. If the {@code helper}
   * is {@code null}, the helper text functionality will be disabled and the helper message will be
   * hidden.
   *
   * <p>If the helper text functionality has not been enabled via {@link
   * #setHelperTextEnabled(boolean)}, then it will be automatically enabled if {@code helper} is not
   * empty.
   *
   * @param helperText Helper text to display
   * @see #getHelperText()
   */
  public void setHelperText(@Nullable final CharSequence helperText) {
    // If helper text is null, disable helper if it's enabled.
    if (TextUtils.isEmpty(helperText)) {
      if (isHelperTextEnabled()) {
        setHelperTextEnabled(false);
      }
    } else {
      if (!isHelperTextEnabled()) {
        setHelperTextEnabled(true);
      }
      indicatorViewController.showHelper(helperText);
    }
  }

  /**
   * Returns whether the helper text functionality is enabled or not in this layout.
   *
   * @attr ref android.support.design.R.styleable#TextInputLayout_helperTextEnabled
   * @see #setHelperTextEnabled(boolean)
   */
  public boolean isHelperTextEnabled() {
    return indicatorViewController.isHelperTextEnabled();
  }

  /**
   * Sets an error message that will be displayed below our {@link EditText}. If the {@code error}
   * is {@code null}, the error message will be cleared.
   *
   * <p>If the error functionality has not been enabled via {@link #setErrorEnabled(boolean)}, then
   * it will be automatically enabled if {@code error} is not empty.
   *
   * @param errorText Error message to display, or null to clear
   * @see #getError()
   */
  public void setError(@Nullable final CharSequence errorText) {
    if (!indicatorViewController.isErrorEnabled()) {
      if (TextUtils.isEmpty(errorText)) {
        // If error isn't enabled, and the error is empty, just return
        return;
      }
      // Else, we'll assume that they want to enable the error functionality
      setErrorEnabled(true);
    }

    if (!TextUtils.isEmpty(errorText)) {
      indicatorViewController.showError(errorText);
    } else {
      indicatorViewController.hideError();
    }
  }

  /**
   * Whether the character counter functionality is enabled or not in this layout.
   *
   * @attr ref android.support.design.R.styleable#TextInputLayout_counterEnabled
   */
  public void setCounterEnabled(boolean enabled) {
    if (mCounterEnabled != enabled) {
      if (enabled) {
        mCounterView = new AppCompatTextView(getContext());
        mCounterView.setId(R.id.textinput_counter);
        if (mTypeface != null) {
          mCounterView.setTypeface(mTypeface);
        }
        mCounterView.setMaxLines(1);
        setTextAppearanceCompatWithErrorFallback(mCounterView, mCounterTextAppearance);
        indicatorViewController.addIndicator(mCounterView, COUNTER_INDEX);
        if (mEditText == null) {
          updateCounter(0);
        } else {
          updateCounter(mEditText.getText().length());
        }
      } else {
        indicatorViewController.removeIndicator(mCounterView, COUNTER_INDEX);
        mCounterView = null;
      }
      mCounterEnabled = enabled;
    }
  }

  /**
   * Returns whether the character counter functionality is enabled or not in this layout.
   *
   * @attr ref android.support.design.R.styleable#TextInputLayout_counterEnabled
   * @see #setCounterEnabled(boolean)
   */
  public boolean isCounterEnabled() {
    return mCounterEnabled;
  }

  /**
   * Sets the max length to display at the character counter.
   *
   * @param maxLength maxLength to display. Any value less than or equal to 0 will not be shown.
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

  @Override
  public void setEnabled(boolean enabled) {
    // Since we're set to addStatesFromChildren, we need to make sure that we set all
    // children to enabled/disabled otherwise any enabled children will wipe out our disabled
    // drawable state
    recursiveSetEnabled(this, enabled);
    super.setEnabled(enabled);
  }

  private static void recursiveSetEnabled(final ViewGroup vg, final boolean enabled) {
    for (int i = 0, count = vg.getChildCount(); i < count; i++) {
      final View child = vg.getChildAt(i);
      child.setEnabled(enabled);
      if (child instanceof ViewGroup) {
        recursiveSetEnabled((ViewGroup) child, enabled);
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

  void updateCounter(int length) {
    boolean wasCounterOverflowed = mCounterOverflowed;
    if (mCounterMaxLength == INVALID_MAX_LENGTH) {
      mCounterView.setText(String.valueOf(length));
      mCounterOverflowed = false;
    } else {
      mCounterOverflowed = length > mCounterMaxLength;
      if (wasCounterOverflowed != mCounterOverflowed) {
        setTextAppearanceCompatWithErrorFallback(
            mCounterView,
            mCounterOverflowed ? mCounterOverflowTextAppearance : mCounterTextAppearance);
      }
      mCounterView.setText(
          getContext().getString(R.string.character_counter_pattern, length, mCounterMaxLength));
    }
    if (mEditText != null && wasCounterOverflowed != mCounterOverflowed) {
      updateLabelState(false);
      updateEditTextBackground();
    }
  }

  void setTextAppearanceCompatWithErrorFallback(TextView textView, @StyleRes int textAppearance) {
    boolean useDefaultColor = false;
    try {
      TextViewCompat.setTextAppearance(textView, textAppearance);

      if (VERSION.SDK_INT >= VERSION_CODES.M
          && textView.getTextColors().getDefaultColor() == Color.MAGENTA) {
        // Caused by our theme not extending from Theme.Design*. On API 23 and
        // above, unresolved theme attrs result in MAGENTA rather than an exception.
        // Flag so that we use a decent default
        useDefaultColor = true;
      }
    } catch (Exception e) {
      // Caused by our theme not extending from Theme.Design*. Flag so that we use
      // a decent default
      useDefaultColor = true;
    }
    if (useDefaultColor) {
      // Probably caused by our theme not extending from Theme.Design*. Instead
      // we manually set something appropriate
      TextViewCompat.setTextAppearance(textView, R.style.TextAppearance_AppCompat_Caption);
      textView.setTextColor(ContextCompat.getColor(getContext(), R.color.error_color_material));
    }
  }

  private void updateTextInputBoxBounds() {
    if (mBoxBackground == null || mEditText == null || getRight() == 0) {
      return;
    }

    mBoxBackground.setBounds(
        getLeft(), getPaddingTop(), getRight(), mEditText.getBottom() + mBoxPaddingOffsetPx);
    applyBoxAttributes();
    updateEditTextBackgroundBounds();
  }

  private void updateEditTextBackgroundBounds() {
    Drawable editTextBackground = mEditText.getBackground();
    if (editTextBackground == null) {
      return;
    }

    if (android.support.v7.widget.DrawableUtils.canSafelyMutateDrawable(editTextBackground)) {
      editTextBackground = editTextBackground.mutate();
    }

    final Rect editTextBounds = new Rect();
    ViewGroupUtils.getDescendantRect(this, mEditText, editTextBounds);

    Rect editTextBackgroundBounds = editTextBackground.getBounds();
    final int left = editTextBackgroundBounds.left - mEditText.getPaddingLeft();
    final int right = editTextBackgroundBounds.right + mEditText.getPaddingRight();
    editTextBackground.setBounds(left, editTextBackgroundBounds.top, right, mEditText.getBottom());
  }

  private void setBoxAttributes() {
    switch (mBoxBackgroundMode) {
      case BOX_BACKGROUND_FILLED:
        mBoxCornerRadius = 0f;
        mBoxBackgroundColor = mDefaultTextColor;
        mBoxStrokeWidth = 0;
        break;

      case BOX_BACKGROUND_OUTLINE:
        mBoxCornerRadius = 16f;
        mBoxStrokeColor = mFocusedTextColor;
        mBoxBackgroundColor = null;
        mBoxStrokeWidth = 7;
        break;

      default:
        break;
    }
  }

  private void applyBoxAttributes() {
    if (mBoxBackground == null) {
      return;
    }

    setBoxAttributes();

    if (mEditText != null && mBoxBackgroundMode == BOX_BACKGROUND_OUTLINE) {
      // Store the EditText's background drawable, in case it needs to be restored later.
      if (mEditText.getBackground() != null) {
        mEditTextOriginalDrawable = mEditText.getBackground();
      }
      ViewCompat.setBackground(mEditText, null);
    }

    if (mEditText != null
        && mBoxBackgroundMode == BOX_BACKGROUND_FILLED
        && mEditTextOriginalDrawable != null) {
      // Restore the EditText drawable.
      ViewCompat.setBackground(mEditText, mEditTextOriginalDrawable);
    }

    if (mBoxStrokeWidth > -1 && mBoxStrokeColor != null) {
      setBoxBackgroundStroke(mBoxStrokeWidth, mBoxStrokeColor);
    }

    if (mBoxCornerRadius > -1) {
      mBoxBackground.setCornerRadius(mBoxCornerRadius);
    }

    setBoxBackgroundColor(mBoxBackgroundColor);
  }

  private void setBoxBackgroundStroke(int boxStrokeWidth, ColorStateList boxStrokeColor) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      mBoxBackground.setStroke(boxStrokeWidth, boxStrokeColor);
    } else {
      // Drop to compat, so we have to use a color int instead of a ColorStateList. The drawable's
      // stroke won't change colors based on the view's state changes.
      mBoxBackground.setStroke(
          boxStrokeWidth, boxStrokeColor.getColorForState(getDrawableState(), Color.TRANSPARENT));
    }
  }

  private void setBoxBackgroundColor(@Nullable ColorStateList boxBackgroundColor) {
    if (boxBackgroundColor != null) {
      if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
        mBoxBackground.setColor(boxBackgroundColor);
      } else {
        // Drop to compat, so we have to use a color int instead of a ColorStateList. The drawable's
        // background won't change colors based on the view's state changes.
        mBoxBackground.setColor(
            boxBackgroundColor.getColorForState(getDrawableState(), Color.TRANSPARENT));
      }
    } else {
      // Setting a color for transparent backgrounds to avoid a default black background in pre-17
      // versions.
      mBoxBackground.setColor(Color.TRANSPARENT);
    }
  }

  void updateEditTextBackground() {
    if (mEditText == null) {
      return;
    }

    Drawable editTextBackground = mEditText.getBackground();
    if (editTextBackground == null) {
      return;
    }

    ensureBackgroundDrawableStateWorkaround();

    if (android.support.v7.widget.DrawableUtils.canSafelyMutateDrawable(editTextBackground)) {
      editTextBackground = editTextBackground.mutate();
    }

    if (indicatorViewController.errorShouldBeShown()) {
      // Set a color filter for the error color
      editTextBackground.setColorFilter(
          AppCompatDrawableManager.getPorterDuffColorFilter(
              indicatorViewController.getErrorViewCurrentTextColor(), PorterDuff.Mode.SRC_IN));
    } else if (mCounterOverflowed && mCounterView != null) {
      // Set a color filter of the counter color
      editTextBackground.setColorFilter(
          AppCompatDrawableManager.getPorterDuffColorFilter(
              mCounterView.getCurrentTextColor(), PorterDuff.Mode.SRC_IN));
    } else {
      // Else reset the color filter and refresh the drawable state so that the
      // normal tint is used
      DrawableCompat.clearColorFilter(editTextBackground);
      mEditText.refreshDrawableState();
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
      // where the first drawable retrieved from resources will propagate any changes
      // (like color filter) to all instances from the cache. We'll try to work around it...

      final Drawable newBg = bg.getConstantState().newDrawable();

      if (bg instanceof DrawableContainer) {
        // If we have a Drawable container, we can try and set its constant state via
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
        ViewCompat.setBackground(mEditText, newBg);
        mHasReconstructedEditTextBackground = true;
      }
    }
  }

  static class SavedState extends AbsSavedState {
    CharSequence error;

    SavedState(Parcelable superState) {
      super(superState);
    }

    SavedState(Parcel source, ClassLoader loader) {
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
          + " error="
          + error
          + "}";
    }

    public static final Creator<SavedState> CREATOR =
        new ClassLoaderCreator<SavedState>() {
          @Override
          public SavedState createFromParcel(Parcel in, ClassLoader loader) {
            return new SavedState(in, loader);
          }

          @Override
          public SavedState createFromParcel(Parcel in) {
            return new SavedState(in, null);
          }

          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }

  @Override
  public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState ss = new SavedState(superState);
    if (indicatorViewController.errorShouldBeShown()) {
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

  @Override
  protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
    mRestoringSavedState = true;
    super.dispatchRestoreInstanceState(container);
    mRestoringSavedState = false;
  }

  /**
   * Returns the error message that was set to be displayed with {@link #setError(CharSequence)}, or
   * <code>null</code> if no error was set or if error displaying is not enabled.
   *
   * @see #setError(CharSequence)
   */
  @Nullable
  public CharSequence getError() {
    return indicatorViewController.isErrorEnabled() ? indicatorViewController.getErrorText() : null;
  }

  /**
   * Returns the helper message that was set to be displayed with {@link
   * #setHelperText(CharSequence)}, or <code>null</code> if no helper text was set or if helper text
   * functionality is not enabled.
   *
   * @see #setHelperText(CharSequence)
   */
  @Nullable
  public CharSequence getHelperText() {
    return indicatorViewController.isHelperTextEnabled()
        ? indicatorViewController.getHelperText()
        : null;
  }

  /**
   * Returns whether any hint state changes, due to being focused or non-empty text, are animated.
   *
   * @see #setHintAnimationEnabled(boolean)
   * @attr ref android.support.design.R.styleable#TextInputLayout_hintAnimationEnabled
   */
  public boolean isHintAnimationEnabled() {
    return mHintAnimationEnabled;
  }

  /**
   * Set whether any hint state changes, due to being focused or non-empty text, are animated.
   *
   * @see #isHintAnimationEnabled()
   * @attr ref android.support.design.R.styleable#TextInputLayout_hintAnimationEnabled
   */
  public void setHintAnimationEnabled(boolean enabled) {
    mHintAnimationEnabled = enabled;
  }

  @Override
  public void draw(Canvas canvas) {
    updateTextInputBoxBounds();
    super.draw(canvas);

    if (mHintEnabled) {
      mCollapsingTextHelper.draw(canvas);
    }
    if (mBoxBackground != null) {
      mBoxBackground.draw(canvas);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    updatePasswordToggleView();
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  private void updatePasswordToggleView() {
    if (mEditText == null) {
      // If there is no EditText, there is nothing to update
      return;
    }

    if (shouldShowPasswordIcon()) {
      if (mPasswordToggleView == null) {
        mPasswordToggleView =
            (CheckableImageButton)
                LayoutInflater.from(getContext())
                    .inflate(R.layout.design_text_input_password_icon, mInputFrame, false);
        mPasswordToggleView.setImageDrawable(mPasswordToggleDrawable);
        mPasswordToggleView.setContentDescription(mPasswordToggleContentDesc);
        mInputFrame.addView(mPasswordToggleView);

        mPasswordToggleView.setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                passwordVisibilityToggleRequested();
              }
            });
      }

      if (mEditText != null && ViewCompat.getMinimumHeight(mEditText) <= 0) {
        // We should make sure that the EditText has the same min-height as the password toggle
        // view. This ensures focus works properly, and there is no visual jump if the password
        // toggle is enabled/disabled.
        mEditText.setMinimumHeight(ViewCompat.getMinimumHeight(mPasswordToggleView));
      }

      mPasswordToggleView.setVisibility(VISIBLE);
      mPasswordToggleView.setChecked(mPasswordToggledVisible);

      // We need to add a dummy drawable as the end compound drawable so that the text is
      // indented and doesn't display below the toggle view
      if (mPasswordToggleDummyDrawable == null) {
        mPasswordToggleDummyDrawable = new ColorDrawable();
      }
      mPasswordToggleDummyDrawable.setBounds(0, 0, mPasswordToggleView.getMeasuredWidth(), 1);

      final Drawable[] compounds = TextViewCompat.getCompoundDrawablesRelative(mEditText);
      // Store the user defined end compound drawable so that we can restore it later
      if (compounds[2] != mPasswordToggleDummyDrawable) {
        mOriginalEditTextEndDrawable = compounds[2];
      }
      TextViewCompat.setCompoundDrawablesRelative(
          mEditText, compounds[0], compounds[1], mPasswordToggleDummyDrawable, compounds[3]);

      // Copy over the EditText's padding so that we match
      mPasswordToggleView.setPadding(
          mEditText.getPaddingLeft(),
          mEditText.getPaddingTop(),
          mEditText.getPaddingRight(),
          mEditText.getPaddingBottom());
    } else {
      if (mPasswordToggleView != null && mPasswordToggleView.getVisibility() == VISIBLE) {
        mPasswordToggleView.setVisibility(View.GONE);
      }

      if (mPasswordToggleDummyDrawable != null) {
        // Make sure that we remove the dummy end compound drawable if it exists, and then
        // clear it
        final Drawable[] compounds = TextViewCompat.getCompoundDrawablesRelative(mEditText);
        if (compounds[2] == mPasswordToggleDummyDrawable) {
          TextViewCompat.setCompoundDrawablesRelative(
              mEditText, compounds[0], compounds[1], mOriginalEditTextEndDrawable, compounds[3]);
          mPasswordToggleDummyDrawable = null;
        }
      }
    }
  }

  /**
   * Set the icon to use for the password visibility toggle button.
   *
   * <p>If you use an icon you should also set a description for its action using {@link
   * #setPasswordVisibilityToggleContentDescription(CharSequence)}. This is used for accessibility.
   *
   * @param resId resource id of the drawable to set, or 0 to clear the icon
   * @attr ref android.support.design.R.styleable#TextInputLayout_passwordToggleDrawable
   */
  public void setPasswordVisibilityToggleDrawable(@DrawableRes int resId) {
    setPasswordVisibilityToggleDrawable(
        resId != 0 ? AppCompatResources.getDrawable(getContext(), resId) : null);
  }

  /**
   * Set the icon to use for the password visibility toggle button.
   *
   * <p>If you use an icon you should also set a description for its action using {@link
   * #setPasswordVisibilityToggleContentDescription(CharSequence)}. This is used for accessibility.
   *
   * @param icon Drawable to set, may be null to clear the icon
   * @attr ref android.support.design.R.styleable#TextInputLayout_passwordToggleDrawable
   */
  public void setPasswordVisibilityToggleDrawable(@Nullable Drawable icon) {
    mPasswordToggleDrawable = icon;
    if (mPasswordToggleView != null) {
      mPasswordToggleView.setImageDrawable(icon);
    }
  }

  /**
   * Set a content description for the navigation button if one is present.
   *
   * <p>The content description will be read via screen readers or other accessibility systems to
   * explain the action of the password visibility toggle.
   *
   * @param resId Resource ID of a content description string to set, or 0 to clear the description
   * @attr ref android.support.design.R.styleable#TextInputLayout_passwordToggleContentDescription
   */
  public void setPasswordVisibilityToggleContentDescription(@StringRes int resId) {
    setPasswordVisibilityToggleContentDescription(
        resId != 0 ? getResources().getText(resId) : null);
  }

  /**
   * Set a content description for the navigation button if one is present.
   *
   * <p>The content description will be read via screen readers or other accessibility systems to
   * explain the action of the password visibility toggle.
   *
   * @param description Content description to set, or null to clear the content description
   * @attr ref android.support.design.R.styleable#TextInputLayout_passwordToggleContentDescription
   */
  public void setPasswordVisibilityToggleContentDescription(@Nullable CharSequence description) {
    mPasswordToggleContentDesc = description;
    if (mPasswordToggleView != null) {
      mPasswordToggleView.setContentDescription(description);
    }
  }

  /**
   * Returns the icon currently used for the password visibility toggle button.
   *
   * @see #setPasswordVisibilityToggleDrawable(Drawable)
   * @attr ref android.support.design.R.styleable#TextInputLayout_passwordToggleDrawable
   */
  @Nullable
  public Drawable getPasswordVisibilityToggleDrawable() {
    return mPasswordToggleDrawable;
  }

  /**
   * Returns the currently configured content description for the password visibility toggle button.
   *
   * <p>This will be used to describe the navigation action to users through mechanisms such as
   * screen readers.
   */
  @Nullable
  public CharSequence getPasswordVisibilityToggleContentDescription() {
    return mPasswordToggleContentDesc;
  }

  /**
   * Returns whether the password visibility toggle functionality is currently enabled.
   *
   * @see #setPasswordVisibilityToggleEnabled(boolean)
   */
  public boolean isPasswordVisibilityToggleEnabled() {
    return mPasswordToggleEnabled;
  }

  /**
   * Returns whether the password visibility toggle functionality is enabled or not.
   *
   * <p>When enabled, a button is placed at the end of the EditText which enables the user to switch
   * between the field's input being visibly disguised or not.
   *
   * @param enabled true to enable the functionality
   * @attr ref android.support.design.R.styleable#TextInputLayout_passwordToggleEnabled
   */
  public void setPasswordVisibilityToggleEnabled(final boolean enabled) {
    if (mPasswordToggleEnabled != enabled) {
      mPasswordToggleEnabled = enabled;

      if (!enabled && mPasswordToggledVisible && mEditText != null) {
        // If the toggle is no longer enabled, but we remove the PasswordTransformation
        // to make the password visible, add it back
        mEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
      }

      // Reset the visibility tracking flag
      mPasswordToggledVisible = false;

      updatePasswordToggleView();
    }
  }

  /**
   * Applies a tint to the password visibility toggle drawable. Does not modify the current tint
   * mode, which is {@link PorterDuff.Mode#SRC_IN} by default.
   *
   * <p>Subsequent calls to {@link #setPasswordVisibilityToggleDrawable(Drawable)} will
   * automatically mutate the drawable and apply the specified tint and tint mode using {@link
   * DrawableCompat#setTintList(Drawable, ColorStateList)}.
   *
   * @param tintList the tint to apply, may be null to clear tint
   * @attr ref android.support.design.R.styleable#TextInputLayout_passwordToggleTint
   */
  public void setPasswordVisibilityToggleTintList(@Nullable ColorStateList tintList) {
    mPasswordToggleTintList = tintList;
    mHasPasswordToggleTintList = true;
    applyPasswordToggleTint();
  }

  /**
   * Specifies the blending mode used to apply the tint specified by {@link
   * #setPasswordVisibilityToggleTintList(ColorStateList)} to the password visibility toggle
   * drawable. The default mode is {@link PorterDuff.Mode#SRC_IN}.
   *
   * @param mode the blending mode used to apply the tint, may be null to clear tint
   * @attr ref android.support.design.R.styleable#TextInputLayout_passwordToggleTintMode
   */
  public void setPasswordVisibilityToggleTintMode(@Nullable PorterDuff.Mode mode) {
    mPasswordToggleTintMode = mode;
    mHasPasswordToggleTintMode = true;
    applyPasswordToggleTint();
  }

  void passwordVisibilityToggleRequested() {
    if (mPasswordToggleEnabled) {
      // Store the current cursor position
      final int selection = mEditText.getSelectionEnd();

      if (hasPasswordTransformation()) {
        mEditText.setTransformationMethod(null);
        mPasswordToggledVisible = true;
      } else {
        mEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        mPasswordToggledVisible = false;
      }

      mPasswordToggleView.setChecked(mPasswordToggledVisible);

      // And restore the cursor position
      mEditText.setSelection(selection);
    }
  }

  private boolean hasPasswordTransformation() {
    return mEditText != null
        && mEditText.getTransformationMethod() instanceof PasswordTransformationMethod;
  }

  private boolean shouldShowPasswordIcon() {
    return mPasswordToggleEnabled && (hasPasswordTransformation() || mPasswordToggledVisible);
  }

  private void applyPasswordToggleTint() {
    if (mPasswordToggleDrawable != null
        && (mHasPasswordToggleTintList || mHasPasswordToggleTintMode)) {
      mPasswordToggleDrawable = DrawableCompat.wrap(mPasswordToggleDrawable).mutate();

      if (mHasPasswordToggleTintList) {
        DrawableCompat.setTintList(mPasswordToggleDrawable, mPasswordToggleTintList);
      }
      if (mHasPasswordToggleTintMode) {
        DrawableCompat.setTintMode(mPasswordToggleDrawable, mPasswordToggleTintMode);
      }

      if (mPasswordToggleView != null
          && mPasswordToggleView.getDrawable() != mPasswordToggleDrawable) {
        mPasswordToggleView.setImageDrawable(mPasswordToggleDrawable);
      }
    }
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    if (mBoxBackground != null) {
      updateTextInputBoxBounds();
    }

    if (mHintEnabled && mEditText != null) {
      final Rect rect = mTmpRect;
      ViewGroupUtils.getDescendantRect(this, mEditText, rect);

      final int l = rect.left + mEditText.getCompoundPaddingLeft();
      final int r = rect.right - mEditText.getCompoundPaddingRight();

      mCollapsingTextHelper.setExpandedBounds(
          l,
          rect.top + mEditText.getCompoundPaddingTop(),
          r,
          rect.bottom - mEditText.getCompoundPaddingBottom());

      // Set the collapsed bounds to be the full height (minus padding) to match the
      // EditText's editable area
      mCollapsingTextHelper.setCollapsedBounds(
          l, getPaddingTop(), r, bottom - top - getPaddingBottom());

      mCollapsingTextHelper.recalculate();
    }
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
    mHintExpanded = false;
  }

  @Override
  protected void drawableStateChanged() {
    if (mInDrawableStateChanged) {
      // Some of the calls below will update the drawable state of child views. Since we're
      // using addStatesFromChildren we can get into infinite recursion, hence we'll just
      // exit in this instance
      return;
    }

    mInDrawableStateChanged = true;

    super.drawableStateChanged();

    final int[] state = getDrawableState();
    boolean changed = false;

    // Drawable state has changed so see if we need to update the label
    updateLabelState(ViewCompat.isLaidOut(this) && isEnabled());

    updateEditTextBackground();
    updateTextInputBoxBounds();

    if (mCollapsingTextHelper != null) {
      changed |= mCollapsingTextHelper.setState(state);
    }

    if (changed) {
      invalidate();
    }

    mInDrawableStateChanged = false;
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
    mHintExpanded = true;
  }

  @VisibleForTesting
  void animateToExpansionFraction(final float target) {
    if (mCollapsingTextHelper.getExpansionFraction() == target) {
      return;
    }
    if (mAnimator == null) {
      mAnimator = new ValueAnimator();
      mAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
      mAnimator.setDuration(LABEL_SCALE_ANIMATION_DURATION);
      mAnimator.addUpdateListener(
          new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
              mCollapsingTextHelper.setExpansionFraction((float) animator.getAnimatedValue());
            }
          });
    }
    mAnimator.setFloatValues(mCollapsingTextHelper.getExpansionFraction(), target);
    mAnimator.start();
  }

  @VisibleForTesting
  final boolean isHintExpanded() {
    return mHintExpanded;
  }

  @VisibleForTesting
  final int getHintCurrentCollapsedTextColor() {
    return mCollapsingTextHelper.getCurrentCollapsedTextColor();
  }

  @VisibleForTesting
  final int getErrorTextCurrentColor() {
    return indicatorViewController.getErrorViewCurrentTextColor();
  }

  private class TextInputAccessibilityDelegate extends AccessibilityDelegateCompat {
    TextInputAccessibilityDelegate() {}

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

      if (indicatorViewController.errorIsDisplayed()) {
        info.setContentInvalid(true);
        info.setError(indicatorViewController.getErrorText());
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
