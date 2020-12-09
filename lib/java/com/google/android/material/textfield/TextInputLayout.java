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

package com.google.android.material.textfield;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.textfield.IndicatorViewController.COUNTER_INDEX;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.widget.TextViewCompat;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatDrawableManager;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.TintTypedArray;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStructure;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import androidx.core.text.BidiFormatter;
import androidx.customview.view.AbsSavedState;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.CheckableImageButton;
import com.google.android.material.internal.CollapsingTextHelper;
import com.google.android.material.internal.DescendantOffsetUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * Layout which wraps a {@link TextInputEditText}, {@link android.widget.EditText}, or descendant to
 * show a floating label when the hint is hidden while the user inputs text.
 *
 * <p>Also supports:
 *
 * <ul>
 *   <li>Showing an error via {@link #setErrorEnabled(boolean)} and {@link #setError(CharSequence)},
 *       along with showing an error icon via {@link #setErrorIconDrawable}
 *   <li>Showing helper text via {@link #setHelperTextEnabled(boolean)} and {@link
 *       #setHelperText(CharSequence)}
 *   <li>Showing placeholder text via {@link #setPlaceholderText(CharSequence)}
 *   <li>Showing prefix text via {@link #setPrefixText(CharSequence)}
 *   <li>Showing suffix text via {@link #setSuffixText(CharSequence)}
 *   <li>Showing a character counter via {@link #setCounterEnabled(boolean)} and {@link
 *       #setCounterMaxLength(int)}
 *   <li>Password visibility toggling via {@link #setEndIconMode(int)} API and related attribute. If
 *       set, a button is displayed to toggle between the password being displayed as plain-text or
 *       disguised, when your EditText is set to display a password.
 *   <li>Clearing text functionality via {@link #setEndIconMode(int)} API and related attribute. If
 *       set, a button is displayed when text is present and clicking it clears the EditText field.
 *   <li>Showing a custom icon specified via {@link #setEndIconMode(int)} API and related attribute.
 *       You should specify a drawable and content description for the icon. Optionally, you can
 *       also specify an {@link android.view.View.OnClickListener}, an {@link
 *       OnEditTextAttachedListener} and an {@link OnEndIconChangedListener}.
 *       <p><strong>Note:</strong> When using an end icon, the 'end' compound drawable of the
 *       EditText will be overridden while the end icon view is visible. To ensure that any existing
 *       drawables are restored correctly, you should set those compound drawables relatively
 *       (start/end), as opposed to absolutely (left/right).
 *   <li>Showing a start icon via {@link #setStartIconDrawable(Drawable)} API and related attribute.
 *       You should specify a content description for the icon. Optionally, you can also specify an
 *       {@link android.view.View.OnClickListener} for it.
 *       <p><strong>Note:</strong> Use the {@link #setStartIconDrawable(Drawable)} API in place of
 *       setting a start/left compound drawable on the EditText. When using a start icon, the
 *       'start/left' compound drawable of the EditText will be overridden.
 *   <li>Showing a button that when clicked displays a dropdown menu. The selected option is
 *       displayed above the dropdown. You need to use an {@link AutoCompleteTextView} instead of a
 *       {@link TextInputEditText} as the input text child, and a
 *       Widget.MaterialComponents.TextInputLayout.(...).ExposedDropdownMenu style.
 *       <p>To disable user input you should set
 *       <pre>android:editable=&quot;false&quot;</pre>
 *       on the {@link AutoCompleteTextView}.
 * </ul>
 *
 * <p>The {@link TextInputEditText} class is provided to be used as the input text child of this
 * layout. Using TextInputEditText instead of an EditText provides accessibility support for the
 * text field and allows TextInputLayout greater control over the visual aspects of the text field.
 * An example usage is as so:
 *
 * <pre>
 * &lt;com.google.android.material.textfield.TextInputLayout
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;wrap_content&quot;
 *         android:hint=&quot;@string/form_username&quot;&gt;
 *
 *     &lt;com.google.android.material.textfield.TextInputEditText
 *             android:layout_width=&quot;match_parent&quot;
 *             android:layout_height=&quot;wrap_content&quot;/&gt;
 *
 * &lt;/com.google.android.material.textfield.TextInputLayout&gt;
 * </pre>
 *
 * The hint should be set on the TextInputLayout, rather than the EditText. If a hint is specified
 * on the child EditText in XML, the TextInputLayout might still work correctly; TextInputLayout
 * will use the EditText's hint as its floating label. However, future calls to modify the hint will
 * not update TextInputLayout's hint. To avoid unintended behavior, call {@link
 * TextInputLayout#setHint(CharSequence)} and {@link TextInputLayout#getHint()} on TextInputLayout,
 * instead of on EditText.
 *
 * <p>If the {@link EditText} child is not a {@link TextInputEditText}, make sure to set the {@link
 * EditText}'s {@code android:background} to {@code null} when using an outlined or filled text
 * field. This allows {@link TextInputLayout} to set the {@link EditText}'s background to an
 * outlined or filled box, respectively.
 *
 * <p><strong>Note:</strong> The actual view hierarchy present under TextInputLayout is
 * <strong>NOT</strong> guaranteed to match the view hierarchy as written in XML. As a result, calls
 * to getParent() on children of the TextInputLayout -- such as a TextInputEditText -- may not
 * return the TextInputLayout itself, but rather an intermediate View. If you need to access a View
 * directly, set an {@code android:id} and use {@link View#findViewById(int)}.
 */
public class TextInputLayout extends LinearLayout {

  private static final int DEF_STYLE_RES = R.style.Widget_Design_TextInputLayout;

  /** Duration for the label's scale up and down animations. */
  private static final int LABEL_SCALE_ANIMATION_DURATION = 167;

  private static final int INVALID_MAX_LENGTH = -1;

  private static final String LOG_TAG = "TextInputLayout";

  @NonNull private final FrameLayout inputFrame;
  @NonNull private final LinearLayout startLayout;
  @NonNull private final LinearLayout endLayout;
  @NonNull private final FrameLayout endIconFrame;
  EditText editText;
  private CharSequence originalHint;

  private final IndicatorViewController indicatorViewController = new IndicatorViewController(this);

  boolean counterEnabled;
  private int counterMaxLength;
  private boolean counterOverflowed;
  @Nullable private TextView counterView;
  private int counterOverflowTextAppearance;
  private int counterTextAppearance;

  private CharSequence placeholderText;
  private boolean placeholderEnabled;
  private TextView placeholderTextView;
  @Nullable private ColorStateList placeholderTextColor;
  private int placeholderTextAppearance;

  @Nullable private ColorStateList counterTextColor;
  @Nullable private ColorStateList counterOverflowTextColor;

  @Nullable private CharSequence prefixText;
  @NonNull private final TextView prefixTextView;
  @Nullable private CharSequence suffixText;
  @NonNull private final TextView suffixTextView;

  private boolean hintEnabled;
  private CharSequence hint;

  /**
   * {@code true} when providing a hint on behalf of a child {@link EditText}. If the child is an
   * instance of {@link TextInputEditText}, this value defines the behavior of its {@link
   * TextInputEditText#getHint()} method.
   */
  private boolean isProvidingHint;

  @Nullable private MaterialShapeDrawable boxBackground;
  @Nullable private MaterialShapeDrawable boxUnderline;
  @NonNull private ShapeAppearanceModel shapeAppearanceModel;

  private final int boxLabelCutoutPaddingPx;
  @BoxBackgroundMode private int boxBackgroundMode;
  private int boxCollapsedPaddingTopPx;
  private int boxStrokeWidthPx;
  private int boxStrokeWidthDefaultPx;
  private int boxStrokeWidthFocusedPx;
  @ColorInt private int boxStrokeColor;
  @ColorInt private int boxBackgroundColor;

  /**
   * Values for box background mode. There is either a filled background, an outline background, or
   * no background.
   */
  @IntDef({BOX_BACKGROUND_NONE, BOX_BACKGROUND_FILLED, BOX_BACKGROUND_OUTLINE})
  @Retention(RetentionPolicy.SOURCE)
  public @interface BoxBackgroundMode {}

  public static final int BOX_BACKGROUND_NONE = 0;
  public static final int BOX_BACKGROUND_FILLED = 1;
  public static final int BOX_BACKGROUND_OUTLINE = 2;

  private final Rect tmpRect = new Rect();
  private final Rect tmpBoundsRect = new Rect();
  private final RectF tmpRectF = new RectF();
  private Typeface typeface;

  @NonNull private final CheckableImageButton startIconView;
  private ColorStateList startIconTintList;
  private boolean hasStartIconTintList;
  private PorterDuff.Mode startIconTintMode;
  private boolean hasStartIconTintMode;
  @Nullable private Drawable startDummyDrawable;
  private int startDummyDrawableWidth;
  private OnLongClickListener startIconOnLongClickListener;

  /**
   * Values for the end icon mode.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({
    END_ICON_CUSTOM,
    END_ICON_NONE,
    END_ICON_PASSWORD_TOGGLE,
    END_ICON_CLEAR_TEXT,
    END_ICON_DROPDOWN_MENU
  })
  @Retention(RetentionPolicy.SOURCE)
  public @interface EndIconMode {}

  /**
   * The TextInputLayout will show a custom icon specified by the user.
   *
   * @see #setEndIconMode(int)
   * @see #getEndIconMode()
   * @see #setEndIconDrawable(Drawable)
   * @see #setEndIconContentDescription(CharSequence)
   * @see #setEndIconOnClickListener(OnClickListener) (optionally)
   * @see #addOnEditTextAttachedListener(OnEditTextAttachedListener) (optionally)
   * @see #addOnEndIconChangedListener(OnEndIconChangedListener) (optionally)
   */
  public static final int END_ICON_CUSTOM = -1;

  /**
   * Default for the TextInputLayout. It will not display an end icon.
   *
   * @see #setEndIconMode(int)
   * @see #getEndIconMode()
   */
  public static final int END_ICON_NONE = 0;

  /**
   * The TextInputLayout will show a password toggle button if its EditText displays a password.
   * When this end icon is clicked, the password is shown as plain-text if it was disguised, or
   * vice-versa.
   *
   * @see #setEndIconMode(int)
   * @see #getEndIconMode()
   */
  public static final int END_ICON_PASSWORD_TOGGLE = 1;

  /**
   * The TextInputLayout will show a clear text button while there is input in the EditText.
   * Clicking it will clear out the text and hide the icon.
   *
   * @see #setEndIconMode(int)
   * @see #getEndIconMode()
   */
  public static final int END_ICON_CLEAR_TEXT = 2;

  /**
   * The TextInputLayout will show a dropdown button if the EditText is an {@link
   * AutoCompleteTextView} and a {@code
   * Widget.MaterialComponents.TextInputLayout.(...).ExposedDropdownMenu} style is being used.
   *
   * <p>Clicking the button will display a popup with a list of options. The current selected option
   * is displayed on the EditText.
   */
  public static final int END_ICON_DROPDOWN_MENU = 3;

  /**
   * Callback interface invoked when the view's {@link EditText} is attached, or from {@link
   * #addOnEditTextAttachedListener(OnEditTextAttachedListener)} if the edit text is already
   * present.
   *
   * @see #addOnEditTextAttachedListener(OnEditTextAttachedListener)
   */
  public interface OnEditTextAttachedListener {

    /**
     * Called when the {@link EditText} is attached, or from {@link
     * #addOnEditTextAttachedListener(OnEditTextAttachedListener)} if the edit text is already
     * present.
     *
     * @param textInputLayout the {@link TextInputLayout}
     */
    void onEditTextAttached(@NonNull TextInputLayout textInputLayout);
  }

  /**
   * Callback interface invoked when the view's end icon changes.
   *
   * @see #setEndIconMode(int)
   */
  public interface OnEndIconChangedListener {

    /**
     * Called when the end icon changes.
     *
     * @param textInputLayout the {@link TextInputLayout}
     * @param previousIcon the end icon mode the view previously had set
     */
    void onEndIconChanged(@NonNull TextInputLayout textInputLayout, @EndIconMode int previousIcon);
  }

  private final LinkedHashSet<OnEditTextAttachedListener> editTextAttachedListeners =
      new LinkedHashSet<>();

  @EndIconMode private int endIconMode = END_ICON_NONE;
  private final SparseArray<EndIconDelegate> endIconDelegates = new SparseArray<>();
  @NonNull private final CheckableImageButton endIconView;
  private final LinkedHashSet<OnEndIconChangedListener> endIconChangedListeners =
      new LinkedHashSet<>();
  private ColorStateList endIconTintList;
  private boolean hasEndIconTintList;
  private PorterDuff.Mode endIconTintMode;
  private boolean hasEndIconTintMode;
  @Nullable private Drawable endDummyDrawable;
  private int endDummyDrawableWidth;
  private Drawable originalEditTextEndDrawable;
  private OnLongClickListener endIconOnLongClickListener;
  private OnLongClickListener errorIconOnLongClickListener;
  @NonNull private final CheckableImageButton errorIconView;
  private ColorStateList errorIconTintList;

  private ColorStateList defaultHintTextColor;
  private ColorStateList focusedTextColor;

  @ColorInt private int defaultStrokeColor;
  @ColorInt private int hoveredStrokeColor;
  @ColorInt private int focusedStrokeColor;
  private ColorStateList strokeErrorColor;

  @ColorInt private int defaultFilledBackgroundColor;
  @ColorInt private int disabledFilledBackgroundColor;
  @ColorInt private int focusedFilledBackgroundColor;
  @ColorInt private int hoveredFilledBackgroundColor;

  @ColorInt private int disabledColor;

  // Only used for testing
  private boolean hintExpanded;

  final CollapsingTextHelper collapsingTextHelper = new CollapsingTextHelper(this);

  private boolean expandedHintEnabled;
  private boolean hintAnimationEnabled;
  private ValueAnimator animator;

  private boolean inDrawableStateChanged;

  private boolean restoringSavedState;

  public TextInputLayout(@NonNull Context context) {
    this(context, null);
  }

  public TextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.textInputStyle);
  }

  public TextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    setOrientation(VERTICAL);
    setWillNotDraw(false);
    setAddStatesFromChildren(true);

    inputFrame = new FrameLayout(context);
    inputFrame.setAddStatesFromChildren(true);
    addView(inputFrame);
    startLayout = new LinearLayout(context);
    startLayout.setOrientation(HORIZONTAL);
    startLayout.setLayoutParams(
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.START | Gravity.LEFT));
    inputFrame.addView(startLayout);
    endLayout = new LinearLayout(context);
    endLayout.setOrientation(HORIZONTAL);
    endLayout.setLayoutParams(
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.END | Gravity.RIGHT));
    inputFrame.addView(endLayout);
    endIconFrame = new FrameLayout(context);
    endIconFrame.setLayoutParams(
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

    collapsingTextHelper.setTextSizeInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    collapsingTextHelper.setPositionInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    collapsingTextHelper.setCollapsedTextGravity(Gravity.TOP | GravityCompat.START);

    final TintTypedArray a =
        ThemeEnforcement.obtainTintedStyledAttributes(
            context,
            attrs,
            R.styleable.TextInputLayout,
            defStyleAttr,
            DEF_STYLE_RES,
            R.styleable.TextInputLayout_counterTextAppearance,
            R.styleable.TextInputLayout_counterOverflowTextAppearance,
            R.styleable.TextInputLayout_errorTextAppearance,
            R.styleable.TextInputLayout_helperTextTextAppearance,
            R.styleable.TextInputLayout_hintTextAppearance);

    hintEnabled = a.getBoolean(R.styleable.TextInputLayout_hintEnabled, true);
    setHint(a.getText(R.styleable.TextInputLayout_android_hint));
    hintAnimationEnabled = a.getBoolean(R.styleable.TextInputLayout_hintAnimationEnabled, true);
    expandedHintEnabled = a.getBoolean(R.styleable.TextInputLayout_expandedHintEnabled, true);

    shapeAppearanceModel =
        ShapeAppearanceModel.builder(context, attrs, defStyleAttr, DEF_STYLE_RES).build();

    boxLabelCutoutPaddingPx =
        context
            .getResources()
            .getDimensionPixelOffset(R.dimen.mtrl_textinput_box_label_cutout_padding);
    boxCollapsedPaddingTopPx =
        a.getDimensionPixelOffset(R.styleable.TextInputLayout_boxCollapsedPaddingTop, 0);

    boxStrokeWidthDefaultPx =
        a.getDimensionPixelSize(
            R.styleable.TextInputLayout_boxStrokeWidth,
            context
                .getResources()
                .getDimensionPixelSize(R.dimen.mtrl_textinput_box_stroke_width_default));
    boxStrokeWidthFocusedPx =
        a.getDimensionPixelSize(
            R.styleable.TextInputLayout_boxStrokeWidthFocused,
            context
                .getResources()
                .getDimensionPixelSize(R.dimen.mtrl_textinput_box_stroke_width_focused));
    boxStrokeWidthPx = boxStrokeWidthDefaultPx;

    float boxCornerRadiusTopStart =
        a.getDimension(R.styleable.TextInputLayout_boxCornerRadiusTopStart, -1f);
    float boxCornerRadiusTopEnd =
        a.getDimension(R.styleable.TextInputLayout_boxCornerRadiusTopEnd, -1f);
    float boxCornerRadiusBottomEnd =
        a.getDimension(R.styleable.TextInputLayout_boxCornerRadiusBottomEnd, -1f);
    float boxCornerRadiusBottomStart =
        a.getDimension(R.styleable.TextInputLayout_boxCornerRadiusBottomStart, -1f);
    ShapeAppearanceModel.Builder shapeBuilder = shapeAppearanceModel.toBuilder();
    if (boxCornerRadiusTopStart >= 0) {
      shapeBuilder.setTopLeftCornerSize(boxCornerRadiusTopStart);
    }
    if (boxCornerRadiusTopEnd >= 0) {
      shapeBuilder.setTopRightCornerSize(boxCornerRadiusTopEnd);
    }
    if (boxCornerRadiusBottomEnd >= 0) {
      shapeBuilder.setBottomRightCornerSize(boxCornerRadiusBottomEnd);
    }
    if (boxCornerRadiusBottomStart >= 0) {
      shapeBuilder.setBottomLeftCornerSize(boxCornerRadiusBottomStart);
    }
    shapeAppearanceModel = shapeBuilder.build();

    ColorStateList filledBackgroundColorStateList =
        MaterialResources.getColorStateList(
            context, a, R.styleable.TextInputLayout_boxBackgroundColor);
    if (filledBackgroundColorStateList != null) {
      defaultFilledBackgroundColor = filledBackgroundColorStateList.getDefaultColor();
      boxBackgroundColor = defaultFilledBackgroundColor;
      if (filledBackgroundColorStateList.isStateful()) {
        disabledFilledBackgroundColor =
            filledBackgroundColorStateList.getColorForState(
                new int[] {-android.R.attr.state_enabled}, -1);
        focusedFilledBackgroundColor =
            filledBackgroundColorStateList.getColorForState(
                new int[] {android.R.attr.state_focused, android.R.attr.state_enabled}, -1);
        hoveredFilledBackgroundColor =
            filledBackgroundColorStateList.getColorForState(
                new int[] {android.R.attr.state_hovered, android.R.attr.state_enabled}, -1);
      } else {
        focusedFilledBackgroundColor = defaultFilledBackgroundColor;
        ColorStateList mtrlFilledBackgroundColorStateList =
            AppCompatResources.getColorStateList(context, R.color.mtrl_filled_background_color);
        disabledFilledBackgroundColor =
            mtrlFilledBackgroundColorStateList.getColorForState(
                new int[] {-android.R.attr.state_enabled}, -1);
        hoveredFilledBackgroundColor =
            mtrlFilledBackgroundColorStateList.getColorForState(
                new int[] {android.R.attr.state_hovered}, -1);
      }
    } else {
      boxBackgroundColor = Color.TRANSPARENT;
      defaultFilledBackgroundColor = Color.TRANSPARENT;
      disabledFilledBackgroundColor = Color.TRANSPARENT;
      focusedFilledBackgroundColor = Color.TRANSPARENT;
      hoveredFilledBackgroundColor = Color.TRANSPARENT;
    }

    if (a.hasValue(R.styleable.TextInputLayout_android_textColorHint)) {
      defaultHintTextColor =
          focusedTextColor = a.getColorStateList(R.styleable.TextInputLayout_android_textColorHint);
    }

    ColorStateList boxStrokeColorStateList =
        MaterialResources.getColorStateList(context, a, R.styleable.TextInputLayout_boxStrokeColor);
    // Default values for stroke colors if boxStrokeColorStateList is not stateful
    focusedStrokeColor = a.getColor(R.styleable.TextInputLayout_boxStrokeColor, Color.TRANSPARENT);
    defaultStrokeColor =
        ContextCompat.getColor(context, R.color.mtrl_textinput_default_box_stroke_color);
    disabledColor = ContextCompat.getColor(context, R.color.mtrl_textinput_disabled_color);
    hoveredStrokeColor =
        ContextCompat.getColor(context, R.color.mtrl_textinput_hovered_box_stroke_color);
    // Values from boxStrokeColorStateList
    if (boxStrokeColorStateList != null) {
      setBoxStrokeColorStateList(boxStrokeColorStateList);
    }
    if (a.hasValue(R.styleable.TextInputLayout_boxStrokeErrorColor)) {
      setBoxStrokeErrorColor(
          MaterialResources.getColorStateList(
              context, a, R.styleable.TextInputLayout_boxStrokeErrorColor));
    }

    final int hintAppearance = a.getResourceId(R.styleable.TextInputLayout_hintTextAppearance, -1);
    if (hintAppearance != -1) {
      setHintTextAppearance(a.getResourceId(R.styleable.TextInputLayout_hintTextAppearance, 0));
    }

    final int errorTextAppearance =
        a.getResourceId(R.styleable.TextInputLayout_errorTextAppearance, 0);
    final CharSequence errorContentDescription =
        a.getText(R.styleable.TextInputLayout_errorContentDescription);
    final boolean errorEnabled = a.getBoolean(R.styleable.TextInputLayout_errorEnabled, false);
    // Initialize error icon view.
    errorIconView =
        (CheckableImageButton)
            LayoutInflater.from(getContext())
                .inflate(R.layout.design_text_input_end_icon, endLayout, false);
    errorIconView.setId(R.id.text_input_error_icon);
    errorIconView.setVisibility(GONE);
    if (MaterialResources.isFontScaleAtLeast1_3(context)) {
      ViewGroup.MarginLayoutParams lp =
          (ViewGroup.MarginLayoutParams) errorIconView.getLayoutParams();
      MarginLayoutParamsCompat.setMarginStart(lp, 0);
    }
    if (a.hasValue(R.styleable.TextInputLayout_errorIconDrawable)) {
      setErrorIconDrawable(a.getDrawable(R.styleable.TextInputLayout_errorIconDrawable));
    }
    if (a.hasValue(R.styleable.TextInputLayout_errorIconTint)) {
      setErrorIconTintList(
          MaterialResources.getColorStateList(
              context, a, R.styleable.TextInputLayout_errorIconTint));
    }
    if (a.hasValue(R.styleable.TextInputLayout_errorIconTintMode)) {
      setErrorIconTintMode(
          ViewUtils.parseTintMode(
              a.getInt(R.styleable.TextInputLayout_errorIconTintMode, -1), null));
    }
    errorIconView.setContentDescription(
        getResources().getText(R.string.error_icon_content_description));
    ViewCompat.setImportantForAccessibility(
        errorIconView, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
    errorIconView.setClickable(false);
    errorIconView.setPressable(false);
    errorIconView.setFocusable(false);

    final int helperTextTextAppearance =
        a.getResourceId(R.styleable.TextInputLayout_helperTextTextAppearance, 0);
    final boolean helperTextEnabled =
        a.getBoolean(R.styleable.TextInputLayout_helperTextEnabled, false);
    final CharSequence helperText = a.getText(R.styleable.TextInputLayout_helperText);

    final int placeholderTextAppearance =
        a.getResourceId(R.styleable.TextInputLayout_placeholderTextAppearance, 0);
    final CharSequence placeholderText = a.getText(R.styleable.TextInputLayout_placeholderText);

    final int prefixTextAppearance =
        a.getResourceId(R.styleable.TextInputLayout_prefixTextAppearance, 0);
    final CharSequence prefixText = a.getText(R.styleable.TextInputLayout_prefixText);

    final int suffixTextAppearance =
        a.getResourceId(R.styleable.TextInputLayout_suffixTextAppearance, 0);
    final CharSequence suffixText = a.getText(R.styleable.TextInputLayout_suffixText);

    final boolean counterEnabled = a.getBoolean(R.styleable.TextInputLayout_counterEnabled, false);
    setCounterMaxLength(a.getInt(R.styleable.TextInputLayout_counterMaxLength, INVALID_MAX_LENGTH));
    counterTextAppearance = a.getResourceId(R.styleable.TextInputLayout_counterTextAppearance, 0);
    counterOverflowTextAppearance =
        a.getResourceId(R.styleable.TextInputLayout_counterOverflowTextAppearance, 0);

    // Initialize start icon view.
    startIconView =
        (CheckableImageButton)
            LayoutInflater.from(getContext())
                .inflate(R.layout.design_text_input_start_icon, startLayout, false);
    startIconView.setVisibility(GONE);
    if (MaterialResources.isFontScaleAtLeast1_3(context)) {
      ViewGroup.MarginLayoutParams lp =
          (ViewGroup.MarginLayoutParams) startIconView.getLayoutParams();
      MarginLayoutParamsCompat.setMarginEnd(lp, 0);
    }
    setStartIconOnClickListener(null);
    setStartIconOnLongClickListener(null);
    // Set up start icon if any.
    if (a.hasValue(R.styleable.TextInputLayout_startIconDrawable)) {
      setStartIconDrawable(a.getDrawable(R.styleable.TextInputLayout_startIconDrawable));
      if (a.hasValue(R.styleable.TextInputLayout_startIconContentDescription)) {
        setStartIconContentDescription(
            a.getText(R.styleable.TextInputLayout_startIconContentDescription));
      }
      setStartIconCheckable(a.getBoolean(R.styleable.TextInputLayout_startIconCheckable, true));
    }
    // Default tint for a start icon or value specified by user.
    if (a.hasValue(R.styleable.TextInputLayout_startIconTint)) {
      setStartIconTintList(
          MaterialResources.getColorStateList(
              context, a, R.styleable.TextInputLayout_startIconTint));
    }
    // Default tint mode for a start icon or value specified by user.
    if (a.hasValue(R.styleable.TextInputLayout_startIconTintMode)) {
      setStartIconTintMode(
          ViewUtils.parseTintMode(
              a.getInt(R.styleable.TextInputLayout_startIconTintMode, -1), null));
    }

    setBoxBackgroundMode(
        a.getInt(R.styleable.TextInputLayout_boxBackgroundMode, BOX_BACKGROUND_NONE));

    // Initialize end icon view.
    endIconView =
        (CheckableImageButton)
            LayoutInflater.from(getContext())
                .inflate(R.layout.design_text_input_end_icon, endIconFrame, false);
    endIconFrame.addView(endIconView);
    endIconView.setVisibility(GONE);
    if (MaterialResources.isFontScaleAtLeast1_3(context)) {
      ViewGroup.MarginLayoutParams lp =
          (ViewGroup.MarginLayoutParams) endIconView.getLayoutParams();
      MarginLayoutParamsCompat.setMarginStart(lp, 0);
    }
    endIconDelegates.append(END_ICON_CUSTOM, new CustomEndIconDelegate(this));
    endIconDelegates.append(END_ICON_NONE, new NoEndIconDelegate(this));
    endIconDelegates.append(END_ICON_PASSWORD_TOGGLE, new PasswordToggleEndIconDelegate(this));
    endIconDelegates.append(END_ICON_CLEAR_TEXT, new ClearTextEndIconDelegate(this));
    endIconDelegates.append(END_ICON_DROPDOWN_MENU, new DropdownMenuEndIconDelegate(this));
    // Set up the end icon if any.
    if (a.hasValue(R.styleable.TextInputLayout_endIconMode)) {
      // Specific defaults depending on which end icon mode is set
      setEndIconMode(a.getInt(R.styleable.TextInputLayout_endIconMode, END_ICON_NONE));
      // Overwrite default values if user specified any different ones
      if (a.hasValue(R.styleable.TextInputLayout_endIconDrawable)) {
        setEndIconDrawable(a.getDrawable(R.styleable.TextInputLayout_endIconDrawable));
      }
      if (a.hasValue(R.styleable.TextInputLayout_endIconContentDescription)) {
        setEndIconContentDescription(
            a.getText(R.styleable.TextInputLayout_endIconContentDescription));
      }
      setEndIconCheckable(a.getBoolean(R.styleable.TextInputLayout_endIconCheckable, true));
    } else if (a.hasValue(R.styleable.TextInputLayout_passwordToggleEnabled)) {
      // Support for deprecated attributes related to the password toggle end icon
      boolean passwordToggleEnabled =
          a.getBoolean(R.styleable.TextInputLayout_passwordToggleEnabled, false);
      setEndIconMode(passwordToggleEnabled ? END_ICON_PASSWORD_TOGGLE : END_ICON_NONE);
      setEndIconDrawable(a.getDrawable(R.styleable.TextInputLayout_passwordToggleDrawable));
      setEndIconContentDescription(
          a.getText(R.styleable.TextInputLayout_passwordToggleContentDescription));
      if (a.hasValue(R.styleable.TextInputLayout_passwordToggleTint)) {
        setEndIconTintList(
            MaterialResources.getColorStateList(
                context, a, R.styleable.TextInputLayout_passwordToggleTint));
      }
      if (a.hasValue(R.styleable.TextInputLayout_passwordToggleTintMode)) {
        setEndIconTintMode(
            ViewUtils.parseTintMode(
                a.getInt(R.styleable.TextInputLayout_passwordToggleTintMode, -1), null));
      }
    }

    if (!a.hasValue(R.styleable.TextInputLayout_passwordToggleEnabled)) {
      // Default tint for any end icon or value specified by user
      if (a.hasValue(R.styleable.TextInputLayout_endIconTint)) {
        setEndIconTintList(
            MaterialResources.getColorStateList(
                context, a, R.styleable.TextInputLayout_endIconTint));
      }
      // Default tint mode for any end icon or value specified by user
      if (a.hasValue(R.styleable.TextInputLayout_endIconTintMode)) {
        setEndIconTintMode(
            ViewUtils.parseTintMode(
                a.getInt(R.styleable.TextInputLayout_endIconTintMode, -1), null));
      }
    }

    // Set up prefix view.
    prefixTextView = new AppCompatTextView(context);
    prefixTextView.setId(R.id.textinput_prefix_text);
    prefixTextView.setLayoutParams(
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    ViewCompat.setAccessibilityLiveRegion(
        prefixTextView, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);

    startLayout.addView(startIconView);
    startLayout.addView(prefixTextView);

    // Set up suffix view.
    suffixTextView = new AppCompatTextView(context);
    suffixTextView.setId(R.id.textinput_suffix_text);
    suffixTextView.setLayoutParams(
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM));
    ViewCompat.setAccessibilityLiveRegion(
        suffixTextView, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);

    endLayout.addView(suffixTextView);
    endLayout.addView(errorIconView);
    endLayout.addView(endIconFrame);

    setHelperTextEnabled(helperTextEnabled);
    setHelperText(helperText);
    setHelperTextTextAppearance(helperTextTextAppearance);
    setErrorEnabled(errorEnabled);
    setErrorTextAppearance(errorTextAppearance);
    setErrorContentDescription(errorContentDescription);
    setCounterTextAppearance(counterTextAppearance);
    setCounterOverflowTextAppearance(counterOverflowTextAppearance);
    setPlaceholderText(placeholderText);
    setPlaceholderTextAppearance(placeholderTextAppearance);
    setPrefixText(prefixText);
    setPrefixTextAppearance(prefixTextAppearance);
    setSuffixText(suffixText);
    setSuffixTextAppearance(suffixTextAppearance);

    if (a.hasValue(R.styleable.TextInputLayout_errorTextColor)) {
      setErrorTextColor(a.getColorStateList(R.styleable.TextInputLayout_errorTextColor));
    }
    if (a.hasValue(R.styleable.TextInputLayout_helperTextTextColor)) {
      setHelperTextColor(a.getColorStateList(R.styleable.TextInputLayout_helperTextTextColor));
    }
    if (a.hasValue(R.styleable.TextInputLayout_hintTextColor)) {
      setHintTextColor(a.getColorStateList(R.styleable.TextInputLayout_hintTextColor));
    }
    if (a.hasValue(R.styleable.TextInputLayout_counterTextColor)) {
      setCounterTextColor(a.getColorStateList(R.styleable.TextInputLayout_counterTextColor));
    }
    if (a.hasValue(R.styleable.TextInputLayout_counterOverflowTextColor)) {
      setCounterOverflowTextColor(
          a.getColorStateList(R.styleable.TextInputLayout_counterOverflowTextColor));
    }
    if (a.hasValue(R.styleable.TextInputLayout_placeholderTextColor)) {
      setPlaceholderTextColor(
          a.getColorStateList(R.styleable.TextInputLayout_placeholderTextColor));
    }
    if (a.hasValue(R.styleable.TextInputLayout_prefixTextColor)) {
      setPrefixTextColor(a.getColorStateList(R.styleable.TextInputLayout_prefixTextColor));
    }
    if (a.hasValue(R.styleable.TextInputLayout_suffixTextColor)) {
      setSuffixTextColor(a.getColorStateList(R.styleable.TextInputLayout_suffixTextColor));
    }
    setCounterEnabled(counterEnabled);

    setEnabled(a.getBoolean(R.styleable.TextInputLayout_android_enabled, true));

    a.recycle();

    // For accessibility, consider TextInputLayout itself to be a simple container for an EditText,
    // and do not expose it to accessibility services.
    ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);

    // For autofill to work as intended, TextInputLayout needs to pass the hint text to the nested
    // EditText so marking it as IMPORTANT_FOR_AUTOFILL_YES.
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      ViewCompat.setImportantForAutofill(this, View.IMPORTANT_FOR_AUTOFILL_YES);
    }
  }

  @Override
  public void addView(
      @NonNull View child, int index, @NonNull final ViewGroup.LayoutParams params) {
    if (child instanceof EditText) {
      // Make sure that the EditText is vertically at the bottom, so that it sits on the
      // EditText's underline
      FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(params);
      flp.gravity = Gravity.CENTER_VERTICAL | (flp.gravity & ~Gravity.VERTICAL_GRAVITY_MASK);
      inputFrame.addView(child, flp);

      // Now use the EditText's LayoutParams as our own and update them to make enough space
      // for the label
      inputFrame.setLayoutParams(params);
      updateInputLayoutMargins();

      setEditText((EditText) child);
    } else {
      // Carry on adding the View...
      super.addView(child, index, params);
    }
  }

  @NonNull
  MaterialShapeDrawable getBoxBackground() {
    if (boxBackgroundMode == BOX_BACKGROUND_FILLED || boxBackgroundMode == BOX_BACKGROUND_OUTLINE) {
      return boxBackground;
    }
    throw new IllegalStateException();
  }

  /**
   * Set the box background mode (filled, outline, or none).
   *
   * <p>May be one of {@link #BOX_BACKGROUND_NONE}, {@link #BOX_BACKGROUND_FILLED}, or {@link
   * #BOX_BACKGROUND_OUTLINE}.
   *
   * <p>Note: This method defines TextInputLayout's internal behavior (for example, it allows the
   * hint to be displayed inline with the stroke in a cutout), but doesn't set all attributes that
   * are set in the styles provided for the box background modes. To achieve the look of an outlined
   * or filled text field, supplement this method with other methods that modify the box, such as
   * {@link #setBoxStrokeColor(int)} and {@link #setBoxBackgroundColor(int)}.
   *
   * @param boxBackgroundMode box's background mode
   * @throws IllegalArgumentException if boxBackgroundMode is not a @BoxBackgroundMode constant
   */
  public void setBoxBackgroundMode(@BoxBackgroundMode int boxBackgroundMode) {
    if (boxBackgroundMode == this.boxBackgroundMode) {
      return;
    }
    this.boxBackgroundMode = boxBackgroundMode;
    if (editText != null) {
      onApplyBoxBackgroundMode();
    }
  }

  /**
   * Get the box background mode (filled, outline, or none).
   *
   * <p>May be one of {@link #BOX_BACKGROUND_NONE}, {@link #BOX_BACKGROUND_FILLED}, or {@link
   * #BOX_BACKGROUND_OUTLINE}.
   */
  @BoxBackgroundMode
  public int getBoxBackgroundMode() {
    return boxBackgroundMode;
  }

  private void onApplyBoxBackgroundMode() {
    assignBoxBackgroundByMode();
    setEditTextBoxBackground();
    updateTextInputBoxState();
    updateBoxCollapsedPaddingTop();
    adjustFilledEditTextPaddingForLargeFont();
    if (boxBackgroundMode != BOX_BACKGROUND_NONE) {
      updateInputLayoutMargins();
    }
  }

  private void assignBoxBackgroundByMode() {
    switch (boxBackgroundMode) {
      case BOX_BACKGROUND_FILLED:
        boxBackground = new MaterialShapeDrawable(shapeAppearanceModel);
        boxUnderline = new MaterialShapeDrawable();
        break;
      case BOX_BACKGROUND_OUTLINE:
        if (hintEnabled && !(boxBackground instanceof CutoutDrawable)) {
          boxBackground = new CutoutDrawable(shapeAppearanceModel);
        } else {
          boxBackground = new MaterialShapeDrawable(shapeAppearanceModel);
        }
        boxUnderline = null;
        break;
      case BOX_BACKGROUND_NONE:
        boxBackground = null;
        boxUnderline = null;
        break;
      default:
        throw new IllegalArgumentException(
            boxBackgroundMode + " is illegal; only @BoxBackgroundMode constants are supported.");
    }
  }

  private void setEditTextBoxBackground() {
    // Set the EditText background to boxBackground if we should use that as the box background.
    if (shouldUseEditTextBackgroundForBoxBackground()) {
      ViewCompat.setBackground(editText, boxBackground);
    }
  }

  private boolean shouldUseEditTextBackgroundForBoxBackground() {
    // When the text field's EditText's background is null, use the EditText's background for the
    // box background.
    return editText != null
        && boxBackground != null
        && editText.getBackground() == null
        && boxBackgroundMode != BOX_BACKGROUND_NONE;
  }

  private void updateBoxCollapsedPaddingTop() {
    if (boxBackgroundMode == BOX_BACKGROUND_FILLED) {
      if (MaterialResources.isFontScaleAtLeast2_0(getContext())) {
        boxCollapsedPaddingTopPx =
            getResources()
                .getDimensionPixelSize(R.dimen.material_font_2_0_box_collapsed_padding_top);
      } else if (MaterialResources.isFontScaleAtLeast1_3(getContext())) {
        boxCollapsedPaddingTopPx =
            getResources()
                .getDimensionPixelSize(R.dimen.material_font_1_3_box_collapsed_padding_top);
      }
    }
  }

  private void adjustFilledEditTextPaddingForLargeFont() {
    if (editText == null || boxBackgroundMode != BOX_BACKGROUND_FILLED) {
      return;
    }
    // Both dense and default styles end up with the same vertical padding.
    if (MaterialResources.isFontScaleAtLeast2_0(getContext())) {
      ViewCompat.setPaddingRelative(
          editText,
          ViewCompat.getPaddingStart(editText),
          getResources()
              .getDimensionPixelSize(R.dimen.material_filled_edittext_font_2_0_padding_top),
          ViewCompat.getPaddingEnd(editText),
          getResources()
              .getDimensionPixelSize(
                  R.dimen.material_filled_edittext_font_2_0_padding_bottom));
    } else if (MaterialResources.isFontScaleAtLeast1_3(getContext())) {
      ViewCompat.setPaddingRelative(
          editText,
          ViewCompat.getPaddingStart(editText),
          getResources()
              .getDimensionPixelSize(R.dimen.material_filled_edittext_font_1_3_padding_top),
          ViewCompat.getPaddingEnd(editText),
          getResources()
              .getDimensionPixelSize(R.dimen.material_filled_edittext_font_1_3_padding_bottom));
    }
  }

  /**
   * Set the resource dimension to use for the box's stroke when in outline box mode, or for the
   * underline stroke in filled mode.
   *
   * @param boxStrokeWidthResId the resource dimension to use for the box's stroke width
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_boxStrokeWidth
   * @see #setBoxStrokeWidth(int)
   * @see #getBoxStrokeWidth()
   */
  public void setBoxStrokeWidthResource(@DimenRes int boxStrokeWidthResId) {
    setBoxStrokeWidth(getResources().getDimensionPixelSize(boxStrokeWidthResId));
  }

  /**
   * Set the value to use for the box's stroke when in outline box mode, or for the underline stroke
   * in filled mode.
   *
   * @param boxStrokeWidth the value to use for the box's stroke
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_boxStrokeWidth
   * @see #getBoxStrokeWidth()
   */
  public void setBoxStrokeWidth(int boxStrokeWidth) {
    boxStrokeWidthDefaultPx = boxStrokeWidth;
    updateTextInputBoxState();
  }

  /**
   * Returns the box's stroke width.
   *
   * @return the value used for the box's stroke width
   * @see #setBoxStrokeWidth(int)
   */
  public int getBoxStrokeWidth() {
    return boxStrokeWidthDefaultPx;
  }

  /**
   * Set the resource dimension to use for the focused box's stroke when in outline box mode, or for
   * the focused underline stroke in filled mode.
   *
   * @param boxStrokeWidthFocusedResId the resource dimension to use for the box's stroke width when
   *     focused
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_boxStrokeWidthFocused
   * @see #setBoxStrokeWidthFocused(int)
   * @see #getBoxStrokeWidthFocused()
   */
  public void setBoxStrokeWidthFocusedResource(@DimenRes int boxStrokeWidthFocusedResId) {
    setBoxStrokeWidthFocused(getResources().getDimensionPixelSize(boxStrokeWidthFocusedResId));
  }

  /**
   * Set the value to use for the focused box's stroke when in outline box mode, or for the focused
   * underline stroke in filled mode.
   *
   * @param boxStrokeWidthFocused the value to use for the box's stroke when focused
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_boxStrokeWidthFocused
   * @see #getBoxStrokeWidthFocused()
   */
  public void setBoxStrokeWidthFocused(int boxStrokeWidthFocused) {
    boxStrokeWidthFocusedPx = boxStrokeWidthFocused;
    updateTextInputBoxState();
  }

  /**
   * Returns the box's stroke focused width.
   *
   * @return the value used for the box's stroke width when focused
   * @see #setBoxStrokeWidthFocused(int)
   */
  public int getBoxStrokeWidthFocused() {
    return boxStrokeWidthFocusedPx;
  }

  /**
   * Set the outline box's stroke focused color.
   *
   * <p>Calling this method when not in outline box mode will do nothing.
   *
   * @param boxStrokeColor the color to use for the box's stroke when focused
   * @see #getBoxStrokeColor()
   */
  public void setBoxStrokeColor(@ColorInt int boxStrokeColor) {
    if (focusedStrokeColor != boxStrokeColor) {
      focusedStrokeColor = boxStrokeColor;
      updateTextInputBoxState();
    }
  }

  /**
   * Returns the box's stroke focused color.
   *
   * @return the color used for the box's stroke when focused
   * @see #setBoxStrokeColor(int)
   */
  public int getBoxStrokeColor() {
    return focusedStrokeColor;
  }

  /**
   * Set the box's stroke color state list.
   *
   * @param boxStrokeColorStateList the color state list to use for the box's stroke
   */
  public void setBoxStrokeColorStateList(@NonNull ColorStateList boxStrokeColorStateList) {
    if (boxStrokeColorStateList.isStateful()) {
      defaultStrokeColor = boxStrokeColorStateList.getDefaultColor();
      disabledColor =
          boxStrokeColorStateList.getColorForState(new int[] {-android.R.attr.state_enabled}, -1);
      hoveredStrokeColor =
          boxStrokeColorStateList.getColorForState(
              new int[] {android.R.attr.state_hovered, android.R.attr.state_enabled}, -1);
      focusedStrokeColor =
          boxStrokeColorStateList.getColorForState(
              new int[] {android.R.attr.state_focused, android.R.attr.state_enabled}, -1);
    } else if (focusedStrokeColor != boxStrokeColorStateList.getDefaultColor()) {
      // If attribute boxStrokeColor is not a color state list but only a single value, its value
      // will be applied to the box's focus state.
      focusedStrokeColor = boxStrokeColorStateList.getDefaultColor();
    }
    updateTextInputBoxState();
  }

  /**
   * Set the outline box's stroke color when an error is being displayed.
   *
   * <p>Calling this method when not in outline box mode will do nothing.
   *
   * @param strokeErrorColor the error color to use for the box's stroke
   * @see #getBoxStrokeErrorColor()
   */
  public void setBoxStrokeErrorColor(@Nullable ColorStateList strokeErrorColor) {
    if (this.strokeErrorColor != strokeErrorColor) {
      this.strokeErrorColor = strokeErrorColor;
      updateTextInputBoxState();
    }
  }

  /**
   * Returns the box's stroke color when an error is being displayed.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_boxStrokeErrorColor
   * @see #setBoxStrokeErrorColor(ColorStateList)
   */
  @Nullable
  public ColorStateList getBoxStrokeErrorColor() {
    return strokeErrorColor;
  }

  /**
   * Set the resource used for the filled box's background color.
   *
   * <p>Note: The background color is only supported for filled boxes. When used with box variants
   * other than {@link BoxBackgroundMode#BOX_BACKGROUND_FILLED}, the box background color may not
   * work as intended.
   *
   * @param boxBackgroundColorId the resource to use for the box's background color
   */
  public void setBoxBackgroundColorResource(@ColorRes int boxBackgroundColorId) {
    setBoxBackgroundColor(ContextCompat.getColor(getContext(), boxBackgroundColorId));
  }

  /**
   * Sets the filled box's default background color. Calling this method will make the background
   * color not be stateful, if it was before.
   *
   * <p>Note: The background color is only supported for filled boxes. When used with box variants
   * other than {@link BoxBackgroundMode#BOX_BACKGROUND_FILLED}, the box background color may not
   * work as intended.
   *
   * @param boxBackgroundColor the color to use for the filled box's background
   * @see #getBoxBackgroundColor()
   */
  public void setBoxBackgroundColor(@ColorInt int boxBackgroundColor) {
    if (this.boxBackgroundColor != boxBackgroundColor) {
      this.boxBackgroundColor = boxBackgroundColor;
      defaultFilledBackgroundColor = boxBackgroundColor;
      focusedFilledBackgroundColor = boxBackgroundColor;
      hoveredFilledBackgroundColor = boxBackgroundColor;
      applyBoxAttributes();
    }
  }

  /**
   * Sets the box's background color state list.
   *
   * <p>Note: The background color is only supported for filled boxes. When used with box variants
   * other than {@link BoxBackgroundMode#BOX_BACKGROUND_FILLED}, the box background color may not
   * work as intended.
   *
   * @param boxBackgroundColorStateList the color state list to use for the box's background color
   */
  public void setBoxBackgroundColorStateList(@NonNull ColorStateList boxBackgroundColorStateList) {
    defaultFilledBackgroundColor = boxBackgroundColorStateList.getDefaultColor();
    boxBackgroundColor = defaultFilledBackgroundColor;
    disabledFilledBackgroundColor =
        boxBackgroundColorStateList.getColorForState(new int[] {-android.R.attr.state_enabled}, -1);
    focusedFilledBackgroundColor =
        boxBackgroundColorStateList.getColorForState(
            new int[] {android.R.attr.state_focused, android.R.attr.state_enabled}, -1);
    hoveredFilledBackgroundColor =
        boxBackgroundColorStateList.getColorForState(
            new int[] {android.R.attr.state_hovered, android.R.attr.state_enabled}, -1);
    applyBoxAttributes();
  }

  /**
   * Returns the filled box's default background color.
   *
   * @return the color used for the filled box's background
   * @see #setBoxBackgroundColor(int)
   */
  public int getBoxBackgroundColor() {
    return boxBackgroundColor;
  }

  /**
   * Set the resources used for the box's corner radii.
   *
   * @param boxCornerRadiusTopStartId the resource to use for the box's top start corner radius
   * @param boxCornerRadiusTopEndId the resource to use for the box's top end corner radius
   * @param boxCornerRadiusBottomEndId the resource to use for the box's bottom end corner radius
   * @param boxCornerRadiusBottomStartId the resource to use for the box's bottom start corner
   *     radius
   */
  public void setBoxCornerRadiiResources(
      @DimenRes int boxCornerRadiusTopStartId,
      @DimenRes int boxCornerRadiusTopEndId,
      @DimenRes int boxCornerRadiusBottomEndId,
      @DimenRes int boxCornerRadiusBottomStartId) {
    setBoxCornerRadii(
        getContext().getResources().getDimension(boxCornerRadiusTopStartId),
        getContext().getResources().getDimension(boxCornerRadiusTopEndId),
        getContext().getResources().getDimension(boxCornerRadiusBottomStartId),
        getContext().getResources().getDimension(boxCornerRadiusBottomEndId));
  }

  /**
   * Set the box's corner radii.
   *
   * @param boxCornerRadiusTopStart the value to use for the box's top start corner radius
   * @param boxCornerRadiusTopEnd the value to use for the box's top end corner radius
   * @param boxCornerRadiusBottomEnd the value to use for the box's bottom end corner radius
   * @param boxCornerRadiusBottomStart the value to use for the box's bottom start corner radius
   * @see #getBoxCornerRadiusTopStart()
   * @see #getBoxCornerRadiusTopEnd()
   * @see #getBoxCornerRadiusBottomEnd()
   * @see #getBoxCornerRadiusBottomStart()
   */
  public void setBoxCornerRadii(
      float boxCornerRadiusTopStart,
      float boxCornerRadiusTopEnd,
      float boxCornerRadiusBottomStart,
      float boxCornerRadiusBottomEnd) {
    if (boxBackground == null
        || boxBackground.getTopLeftCornerResolvedSize() != boxCornerRadiusTopStart
        || boxBackground.getTopRightCornerResolvedSize() != boxCornerRadiusTopEnd
        || boxBackground.getBottomRightCornerResolvedSize() != boxCornerRadiusBottomEnd
        || boxBackground.getBottomLeftCornerResolvedSize() != boxCornerRadiusBottomStart) {
      shapeAppearanceModel =
          shapeAppearanceModel.toBuilder()
              .setTopLeftCornerSize(boxCornerRadiusTopStart)
              .setTopRightCornerSize(boxCornerRadiusTopEnd)
              .setBottomRightCornerSize(boxCornerRadiusBottomEnd)
              .setBottomLeftCornerSize(boxCornerRadiusBottomStart)
              .build();
      applyBoxAttributes();
    }
  }

  /**
   * Returns the box's top start corner radius.
   *
   * @return the value used for the box's top start corner radius
   * @see #setBoxCornerRadii(float, float, float, float)
   */
  public float getBoxCornerRadiusTopStart() {
    return boxBackground.getTopLeftCornerResolvedSize();
  }

  /**
   * Returns the box's top end corner radius.
   *
   * @return the value used for the box's top end corner radius
   * @see #setBoxCornerRadii(float, float, float, float)
   */
  public float getBoxCornerRadiusTopEnd() {
    return boxBackground.getTopRightCornerResolvedSize();
  }

  /**
   * Returns the box's bottom end corner radius.
   *
   * @return the value used for the box's bottom end corner radius
   * @see #setBoxCornerRadii(float, float, float, float)
   */
  public float getBoxCornerRadiusBottomEnd() {
    return boxBackground.getBottomLeftCornerResolvedSize();
  }

  /**
   * Returns the box's bottom start corner radius.
   *
   * @return the value used for the box's bottom start corner radius
   * @see #setBoxCornerRadii(float, float, float, float)
   */
  public float getBoxCornerRadiusBottomStart() {
    return boxBackground.getBottomRightCornerResolvedSize();
  }

  /**
   * Set the typeface to use for the hint and any label views (such as counter and error views).
   *
   * @param typeface typeface to use, or {@code null} to use the default.
   */
  @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
  public void setTypeface(@Nullable Typeface typeface) {
    if (typeface != this.typeface) {
      this.typeface = typeface;

      collapsingTextHelper.setTypefaces(typeface);
      indicatorViewController.setTypefaces(typeface);

      if (counterView != null) {
        counterView.setTypeface(typeface);
      }
    }
  }

  /**
   * Returns the typeface used for the hint and any label views (such as counter and error views).
   */
  @Nullable
  public Typeface getTypeface() {
    return typeface;
  }

  @Override
  @TargetApi(VERSION_CODES.O)
  public void dispatchProvideAutofillStructure(@NonNull ViewStructure structure, int flags) {
    if (editText == null) {
      super.dispatchProvideAutofillStructure(structure, flags);
      return;
    }

    if (originalHint != null) {
      // Temporarily sets child's hint to its original value so it is properly set in the
      // child's ViewStructure.
      boolean wasProvidingHint = isProvidingHint;
      // Ensures a child TextInputEditText does not retrieve its hint from this TextInputLayout.
      isProvidingHint = false;
      final CharSequence hint = editText.getHint();
      editText.setHint(originalHint);
      try {
        super.dispatchProvideAutofillStructure(structure, flags);
      } finally {
        editText.setHint(hint);
        isProvidingHint = wasProvidingHint;
      }
    } else {
      // Pass the hint set on the outer TextInputLayout to the nested edit text to allow autofill
      // services to work as intended.
      structure.setAutofillId(getAutofillId());
      onProvideAutofillStructure(structure, flags);
      onProvideAutofillVirtualStructure(structure, flags);

      structure.setChildCount(inputFrame.getChildCount());
      for (int i = 0; i < inputFrame.getChildCount(); i++) {
        View child = inputFrame.getChildAt(i);
        ViewStructure childStructure = structure.newChild(i);
        child.dispatchProvideAutofillStructure(childStructure, flags);
        if (child == editText) {
          childStructure.setHint(getHint());
        }
      }
    }
  }

  private void setEditText(EditText editText) {
    // If we already have an EditText, throw an exception
    if (this.editText != null) {
      throw new IllegalArgumentException("We already have an EditText, can only have one");
    }

    if (endIconMode != END_ICON_DROPDOWN_MENU && !(editText instanceof TextInputEditText)) {
      Log.i(
          LOG_TAG,
          "EditText added is not a TextInputEditText. Please switch to using that"
              + " class instead.");
    }

    this.editText = editText;
    onApplyBoxBackgroundMode();
    setTextInputAccessibilityDelegate(new AccessibilityDelegate(this));

    // Use the EditText's typeface, and its text size for our expanded text.
    collapsingTextHelper.setTypefaces(this.editText.getTypeface());
    collapsingTextHelper.setExpandedTextSize(this.editText.getTextSize());

    final int editTextGravity = this.editText.getGravity();
    collapsingTextHelper.setCollapsedTextGravity(
        Gravity.TOP | (editTextGravity & ~Gravity.VERTICAL_GRAVITY_MASK));
    collapsingTextHelper.setExpandedTextGravity(editTextGravity);

    // Add a TextWatcher so that we know when the text input has changed.
    this.editText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void afterTextChanged(@NonNull Editable s) {
            updateLabelState(!restoringSavedState);
            if (counterEnabled) {
              updateCounter(s.length());
            }
            if (placeholderEnabled) {
              updatePlaceholderText(s.length());
            }
          }

          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

    // Use the EditText's hint colors if we don't have one set
    if (defaultHintTextColor == null) {
      defaultHintTextColor = this.editText.getHintTextColors();
    }

    // If we do not have a valid hint, try and retrieve it from the EditText, if enabled
    if (hintEnabled) {
      if (TextUtils.isEmpty(hint)) {
        // Save the hint so it can be restored on dispatchProvideAutofillStructure();
        originalHint = this.editText.getHint();
        setHint(originalHint);
        // Clear the EditText's hint as we will display it ourselves
        this.editText.setHint(null);
      }
      this.isProvidingHint = true;
    }

    if (counterView != null) {
      updateCounter(this.editText.getText().length());
    }
    updateEditTextBackground();

    indicatorViewController.adjustIndicatorPadding();

    startLayout.bringToFront();
    endLayout.bringToFront();
    endIconFrame.bringToFront();
    errorIconView.bringToFront();
    dispatchOnEditTextAttached();
    updatePrefixTextViewPadding();
    updateSuffixTextViewPadding();

    // Only call setEnabled on the edit text if the layout is disabled, to prevent reenabling an
    // already disabled edit text.
    if (!isEnabled()) {
      editText.setEnabled(false);
    }

    // Update the label visibility with no animation, but force a state change
    updateLabelState(false, true);
  }

  private void updateInputLayoutMargins() {
    // Create/update the LayoutParams so that we can add enough top margin
    // to the EditText to make room for the label.
    if (boxBackgroundMode != BOX_BACKGROUND_FILLED) {
      final LayoutParams lp = (LayoutParams) inputFrame.getLayoutParams();
      final int newTopMargin = calculateLabelMarginTop();

      if (newTopMargin != lp.topMargin) {
        lp.topMargin = newTopMargin;
        inputFrame.requestLayout();
      }
    }
  }

  @Override
  public int getBaseline() {
    if (editText != null) {
      return editText.getBaseline() + getPaddingTop() + calculateLabelMarginTop();
    } else {
      return super.getBaseline();
    }
  }

  void updateLabelState(boolean animate) {
    updateLabelState(animate, false);
  }

  private void updateLabelState(boolean animate, boolean force) {
    final boolean isEnabled = isEnabled();
    final boolean hasText = editText != null && !TextUtils.isEmpty(editText.getText());
    final boolean hasFocus = editText != null && editText.hasFocus();
    final boolean errorShouldBeShown = indicatorViewController.errorShouldBeShown();

    // Set the expanded and collapsed labels to the default text color.
    if (defaultHintTextColor != null) {
      collapsingTextHelper.setCollapsedTextColor(defaultHintTextColor);
      collapsingTextHelper.setExpandedTextColor(defaultHintTextColor);
    }

    // Set the collapsed and expanded label text colors based on the current state.
    if (!isEnabled) {
      int disabledHintColor =
          defaultHintTextColor != null
              ? defaultHintTextColor.getColorForState(
                  new int[] {-android.R.attr.state_enabled}, disabledColor)
              : disabledColor;
      collapsingTextHelper.setCollapsedTextColor(ColorStateList.valueOf(disabledHintColor));
      collapsingTextHelper.setExpandedTextColor(ColorStateList.valueOf(disabledHintColor));
    } else if (errorShouldBeShown) {
      collapsingTextHelper.setCollapsedTextColor(indicatorViewController.getErrorViewTextColors());
    } else if (counterOverflowed && counterView != null) {
      collapsingTextHelper.setCollapsedTextColor(counterView.getTextColors());
    } else if (hasFocus && focusedTextColor != null) {
      collapsingTextHelper.setCollapsedTextColor(focusedTextColor);
    } // If none of these states apply, leave the expanded and collapsed colors as they are.

    if (hasText || !expandedHintEnabled || (isEnabled() && hasFocus)) {
      // We should be showing the label so do so if it isn't already
      if (force || hintExpanded) {
        collapseHint(animate);
      }
    } else {
      // We should not be showing the label so hide it
      if (force || !hintExpanded) {
        expandHint(animate);
      }
    }
  }

  /** Returns the {@link android.widget.EditText} used for text input. */
  @Nullable
  public EditText getEditText() {
    return editText;
  }

  /**
   * Set the hint to be displayed in the floating label, if enabled.
   *
   * @see #setHintEnabled(boolean)
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_android_hint
   */
  public void setHint(@Nullable CharSequence hint) {
    if (hintEnabled) {
      setHintInternal(hint);
      sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
    }
  }

  /**
   * Set the hint to be displayed in the floating label, if enabled, using the given resource id.
   *
   * @see #setHintEnabled(boolean)
   * @see #setHint(CharSequence)
   * @param textHintId The resource id of the text to display in the floating label
   */
  public void setHint(@StringRes int textHintId) {
    setHint(textHintId != 0 ? getResources().getText(textHintId) : null);
  }

  private void setHintInternal(CharSequence hint) {
    if (!TextUtils.equals(hint, this.hint)) {
      this.hint = hint;
      collapsingTextHelper.setText(hint);
      // Reset the cutout to make room for a larger hint.
      if (!hintExpanded) {
        openCutout();
      }
    }
  }

  /**
   * Returns the hint which is displayed in the floating label, if enabled.
   *
   * @return the hint, or null if there isn't one set, or the hint is not enabled.
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_android_hint
   */
  @Nullable
  public CharSequence getHint() {
    return hintEnabled ? hint : null;
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
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_hintEnabled
   */
  public void setHintEnabled(boolean enabled) {
    if (enabled != hintEnabled) {
      hintEnabled = enabled;
      if (!hintEnabled) {
        // Ensures a child TextInputEditText provides its internal hint, not this TextInputLayout's.
        isProvidingHint = false;
        if (!TextUtils.isEmpty(hint) && TextUtils.isEmpty(editText.getHint())) {
          // If the child EditText has no hint, but this layout does, restore it on the child.
          editText.setHint(hint);
        }
        // Now clear out any set hint
        setHintInternal(null);
      } else {
        final CharSequence editTextHint = editText.getHint();
        if (!TextUtils.isEmpty(editTextHint)) {
          // If the hint is now enabled and the EditText has one set, we'll use it if
          // we don't already have one, and clear the EditText's
          if (TextUtils.isEmpty(hint)) {
            setHint(editTextHint);
          }
          editText.setHint(null);
        }
        isProvidingHint = true;
      }

      // Now update the EditText top margin
      if (editText != null) {
        updateInputLayoutMargins();
      }
    }
  }

  /**
   * Returns whether the floating label functionality is enabled or not in this layout.
   *
   * @see #setHintEnabled(boolean)
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_hintEnabled
   */
  public boolean isHintEnabled() {
    return hintEnabled;
  }

  /**
   * Returns whether or not this layout is actively managing a child {@link EditText}'s hint. If the
   * child is an instance of {@link TextInputEditText}, this value defines the behavior of {@link
   * TextInputEditText#getHint()}.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public boolean isProvidingHint() {
    return isProvidingHint;
  }

  /**
   * Sets the collapsed hint text color, size, style from the specified TextAppearance resource.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_hintTextAppearance
   */
  public void setHintTextAppearance(@StyleRes int resId) {
    collapsingTextHelper.setCollapsedTextAppearance(resId);
    focusedTextColor = collapsingTextHelper.getCollapsedTextColor();

    if (editText != null) {
      updateLabelState(false);
      // Text size might have changed so update the top margin
      updateInputLayoutMargins();
    }
  }
  /**
   * Sets the collapsed hint text color from the specified ColorStateList resource.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_hintTextColor
   */
  public void setHintTextColor(@Nullable ColorStateList hintTextColor) {
    if (focusedTextColor != hintTextColor) {
      if (defaultHintTextColor == null) {
        collapsingTextHelper.setCollapsedTextColor(hintTextColor);
      }

      focusedTextColor = hintTextColor;

      if (editText != null) {
        updateLabelState(false);
      }
    }
  }

  /**
   * Gets the collapsed hint text color.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_hintTextColor
   */
  @Nullable
  public ColorStateList getHintTextColor() {
    return focusedTextColor;
  }

  /** Sets the text color used by the hint in both the collapsed and expanded states. */
  public void setDefaultHintTextColor(@Nullable ColorStateList textColor) {
    defaultHintTextColor = textColor;
    focusedTextColor = textColor;

    if (editText != null) {
      updateLabelState(false);
    }
  }

  /**
   * Returns the text color used by the hint in both the collapsed and expanded states, or null if
   * no color has been set.
   */
  @Nullable
  public ColorStateList getDefaultHintTextColor() {
    return defaultHintTextColor;
  }

  /**
   * Whether the error functionality is enabled or not in this layout. Enabling this functionality
   * before setting an error message via {@link #setError(CharSequence)}, will mean that this layout
   * will not change size when an error is displayed.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_errorEnabled
   */
  public void setErrorEnabled(boolean enabled) {
    indicatorViewController.setErrorEnabled(enabled);
  }

  /**
   * Sets the text color and size for the error message from the specified TextAppearance resource.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_errorTextAppearance
   */
  public void setErrorTextAppearance(@StyleRes int errorTextAppearance) {
    indicatorViewController.setErrorTextAppearance(errorTextAppearance);
  }

  /** Sets the text color used by the error message in all states. */
  public void setErrorTextColor(@Nullable ColorStateList errorTextColor) {
    indicatorViewController.setErrorViewTextColor(errorTextColor);
  }

  /** Returns the text color used by the error message in current state. */
  @ColorInt
  public int getErrorCurrentTextColors() {
    return indicatorViewController.getErrorViewCurrentTextColor();
  }

  /**
   * Sets the text color and size for the helper text from the specified TextAppearance resource.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_helperTextTextAppearance
   */
  public void setHelperTextTextAppearance(@StyleRes int helperTextTextAppearance) {
    indicatorViewController.setHelperTextAppearance(helperTextTextAppearance);
  }

  /** Sets the text color used by the helper text in all states. */
  public void setHelperTextColor(@Nullable ColorStateList helperTextColor) {
    indicatorViewController.setHelperTextViewTextColor(helperTextColor);
  }

  /**
   * Returns whether the error functionality is enabled or not in this layout.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_errorEnabled
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
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_helperTextEnabled
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
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_helperTextEnabled
   * @see #setHelperTextEnabled(boolean)
   */
  public boolean isHelperTextEnabled() {
    return indicatorViewController.isHelperTextEnabled();
  }

  /** Returns the text color used by the helper text in the current states. */
  @ColorInt
  public int getHelperTextCurrentTextColor() {
    return indicatorViewController.getHelperTextViewCurrentTextColor();
  }

  /**
   * Sets a content description for the error message.
   *
   * <p>A content description should be set when the error message contains special characters that
   * screen readers or other accessibility systems are not able to read, so that they announce the
   * content description instead.
   *
   * @param errorContentDecription Content description to set, or null to clear it
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_errorContentDescription
   */
  public void setErrorContentDescription(@Nullable final CharSequence errorContentDecription) {
    indicatorViewController.setErrorContentDescription(errorContentDecription);
  }

  /**
   * Returns the content description of the error message, or null if not set.
   *
   * @see #setErrorContentDescription(CharSequence)
   */
  @Nullable
  public CharSequence getErrorContentDescription() {
    return indicatorViewController.getErrorContentDescription();
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
   * Set the drawable to use for the error icon.
   *
   * @param resId resource id of the drawable to set, or 0 to clear the icon
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_errorIconDrawable
   */
  public void setErrorIconDrawable(@DrawableRes int resId) {
    setErrorIconDrawable(resId != 0 ? AppCompatResources.getDrawable(getContext(), resId) : null);
    refreshErrorIconDrawableState();
  }

  /**
   * Set the drawable to use for the error icon.
   *
   * @param errorIconDrawable Drawable to set, may be null to clear the icon
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_errorIconDrawable
   */
  public void setErrorIconDrawable(@Nullable Drawable errorIconDrawable) {
    errorIconView.setImageDrawable(errorIconDrawable);
    setErrorIconVisible(errorIconDrawable != null && indicatorViewController.isErrorEnabled());
  }

  /**
   * Returns the drawable currently used for the error icon.
   *
   * @see #setErrorIconDrawable(Drawable)
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_errorIconDrawable
   */
  @Nullable
  public Drawable getErrorIconDrawable() {
    return errorIconView.getDrawable();
  }

  /**
   * Applies a tint to the error icon drawable.
   *
   * @param errorIconTintList the tint to apply, may be null to clear tint
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_errorIconTint
   */
  public void setErrorIconTintList(@Nullable ColorStateList errorIconTintList) {
    this.errorIconTintList = errorIconTintList;
    Drawable icon = errorIconView.getDrawable();
    if (icon != null) {
      icon = DrawableCompat.wrap(icon).mutate();
      DrawableCompat.setTintList(icon, errorIconTintList);
    }

    if (errorIconView.getDrawable() != icon) {
      errorIconView.setImageDrawable(icon);
    }
  }

  /**
   * Specifies the blending mode used to apply tint to the end icon drawable. The default mode is
   * {@link PorterDuff.Mode#SRC_IN}.
   *
   * @param errorIconTintMode the blending mode used to apply the tint, may be null to clear tint
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_errorIconTintMode
   */
  public void setErrorIconTintMode(@Nullable PorterDuff.Mode errorIconTintMode) {
    Drawable icon = errorIconView.getDrawable();
    if (icon != null) {
      icon = DrawableCompat.wrap(icon).mutate();
      DrawableCompat.setTintMode(icon, errorIconTintMode);
    }

    if (errorIconView.getDrawable() != icon) {
      errorIconView.setImageDrawable(icon);
    }
  }

  /**
   * Whether the character counter functionality is enabled or not in this layout.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_counterEnabled
   */
  public void setCounterEnabled(boolean enabled) {
    if (counterEnabled != enabled) {
      if (enabled) {
        counterView = new AppCompatTextView(getContext());
        counterView.setId(R.id.textinput_counter);
        if (typeface != null) {
          counterView.setTypeface(typeface);
        }
        counterView.setMaxLines(1);
        indicatorViewController.addIndicator(counterView, COUNTER_INDEX);
        MarginLayoutParamsCompat.setMarginStart(
            (MarginLayoutParams) counterView.getLayoutParams(),
            getResources().getDimensionPixelOffset(R.dimen.mtrl_textinput_counter_margin_start));
        updateCounterTextAppearanceAndColor();
        updateCounter();
      } else {
        indicatorViewController.removeIndicator(counterView, COUNTER_INDEX);
        counterView = null;
      }
      counterEnabled = enabled;
    }
  }

  /**
   * Sets the text color and size for the character counter using the specified TextAppearance
   * resource.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_counterTextAppearance
   * @see #setCounterTextColor(ColorStateList)
   */
  public void setCounterTextAppearance(int counterTextAppearance) {
    if (this.counterTextAppearance != counterTextAppearance) {
      this.counterTextAppearance = counterTextAppearance;
      updateCounterTextAppearanceAndColor();
    }
  }

  /**
   * Sets the text color for the character counter using a ColorStateList.
   *
   * <p>This text color takes precedence over a text color set in counterTextAppearance.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_counterTextColor
   * @param counterTextColor text color used for the character counter
   */
  public void setCounterTextColor(@Nullable ColorStateList counterTextColor) {
    if (this.counterTextColor != counterTextColor) {
      this.counterTextColor = counterTextColor;
      updateCounterTextAppearanceAndColor();
    }
  }

  /**
   * Returns the text color used for the character counter, or null if one has not been set.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_counterOverflowTextColor
   * @see #setCounterTextAppearance(int)
   * @return the text color used for the character counter
   */
  @Nullable
  public ColorStateList getCounterTextColor() {
    return counterTextColor;
  }

  /**
   * Sets the text color and size for the overflowed character counter using the specified
   * TextAppearance resource.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_counterOverflowTextAppearance
   * @see #setCounterOverflowTextColor(ColorStateList)
   */
  public void setCounterOverflowTextAppearance(int counterOverflowTextAppearance) {
    if (this.counterOverflowTextAppearance != counterOverflowTextAppearance) {
      this.counterOverflowTextAppearance = counterOverflowTextAppearance;
      updateCounterTextAppearanceAndColor();
    }
  }

  /**
   * Sets the text color for the overflowed character counter using a ColorStateList.
   *
   * <p>This text color takes precedence over a text color set in counterOverflowTextAppearance.
   *
   * @see #setCounterOverflowTextAppearance(int)
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_counterOverflowTextColor
   * @param counterOverflowTextColor the text color used for the overflowed character counter
   */
  public void setCounterOverflowTextColor(@Nullable ColorStateList counterOverflowTextColor) {
    if (this.counterOverflowTextColor != counterOverflowTextColor) {
      this.counterOverflowTextColor = counterOverflowTextColor;
      updateCounterTextAppearanceAndColor();
    }
  }

  /**
   * Returns the text color used for the overflowed character counter, or null if one has not been
   * set.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_counterOverflowTextColor
   * @see #setCounterOverflowTextAppearance(int)
   * @return the text color used for the overflowed character counter
   */
  @Nullable
  public ColorStateList getCounterOverflowTextColor() {
    return counterTextColor;
  }

  /**
   * Returns whether the character counter functionality is enabled or not in this layout.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_counterEnabled
   * @see #setCounterEnabled(boolean)
   */
  public boolean isCounterEnabled() {
    return counterEnabled;
  }

  /**
   * Sets the max length to display at the character counter.
   *
   * @param maxLength maxLength to display. Any value less than or equal to 0 will not be shown.
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_counterMaxLength
   */
  public void setCounterMaxLength(int maxLength) {
    if (counterMaxLength != maxLength) {
      if (maxLength > 0) {
        counterMaxLength = maxLength;
      } else {
        counterMaxLength = INVALID_MAX_LENGTH;
      }
      if (counterEnabled) {
        updateCounter();
      }
    }
  }

  private void updateCounter() {
    if (counterView != null) {
      updateCounter(editText == null ? 0 : editText.getText().length());
    }
  }

  void updateCounter(int length) {
    boolean wasCounterOverflowed = counterOverflowed;
    if (counterMaxLength == INVALID_MAX_LENGTH) {
      counterView.setText(String.valueOf(length));
      counterView.setContentDescription(null);
      counterOverflowed = false;
    } else {
      counterOverflowed = length > counterMaxLength;
      updateCounterContentDescription(
          getContext(), counterView, length, counterMaxLength, counterOverflowed);

      if (wasCounterOverflowed != counterOverflowed) {
        updateCounterTextAppearanceAndColor();
      }
      BidiFormatter bidiFormatter = BidiFormatter.getInstance();
      counterView.setText(
          bidiFormatter.unicodeWrap(
              getContext()
                  .getString(R.string.character_counter_pattern, length, counterMaxLength)));
    }
    if (editText != null && wasCounterOverflowed != counterOverflowed) {
      updateLabelState(false);
      updateTextInputBoxState();
      updateEditTextBackground();
    }
  }

  private static void updateCounterContentDescription(
      @NonNull Context context,
      @NonNull TextView counterView,
      int length,
      int counterMaxLength,
      boolean counterOverflowed) {
    counterView.setContentDescription(
        context.getString(
            counterOverflowed
                ? R.string.character_counter_overflowed_content_description
                : R.string.character_counter_content_description,
            length,
            counterMaxLength));
  }

  /**
   * Sets placeholder text that will be displayed in the input area when the hint is collapsed
   * before text is entered. If the {@code placeholder} is {@code null}, any previous placeholder
   * text will be hidden and no placeholder text will be shown.
   *
   * @param placeholderText Placeholder text to display
   * @see #getPlaceholderText()
   */
  public void setPlaceholderText(@Nullable final CharSequence placeholderText) {
    // If placeholder text is null, disable placeholder if it's enabled.
    if (placeholderEnabled && TextUtils.isEmpty(placeholderText)) {
      setPlaceholderTextEnabled(false);
    } else {
      if (!placeholderEnabled) {
        // Enable the placeholder if it isn't already.
        setPlaceholderTextEnabled(true);
      }
      this.placeholderText = placeholderText;
    }
    updatePlaceholderText();
  }

  /**
   * Returns the placeholder text that was set to be displayed with {@link
   * #setPlaceholderText(CharSequence)}, or <code>null</code> if there is no placeholder text.
   *
   * @see #setPlaceholderText(CharSequence)
   */
  @Nullable
  public CharSequence getPlaceholderText() {
    return placeholderEnabled ? placeholderText : null;
  }

  private void setPlaceholderTextEnabled(boolean placeholderEnabled) {
    // If the enabled state is the same as before, do nothing.
    if (this.placeholderEnabled == placeholderEnabled) {
      return;
    }

    // Otherwise, adjust enabled state.
    if (placeholderEnabled) {
      placeholderTextView = new AppCompatTextView(getContext());
      placeholderTextView.setId(R.id.textinput_placeholder);

      ViewCompat.setAccessibilityLiveRegion(
          placeholderTextView, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);

      setPlaceholderTextAppearance(placeholderTextAppearance);
      setPlaceholderTextColor(placeholderTextColor);
      addPlaceholderTextView();
    } else {
      removePlaceholderTextView();
      placeholderTextView = null;
    }
    this.placeholderEnabled = placeholderEnabled;
  }

  private void updatePlaceholderText() {
    updatePlaceholderText(editText == null ? 0 : editText.getText().length());
  }

  private void updatePlaceholderText(int inputTextLength) {
    if (inputTextLength == 0 && !hintExpanded) {
      showPlaceholderText();
    } else {
      hidePlaceholderText();
    }
  }

  private void showPlaceholderText() {
    if (placeholderTextView != null && placeholderEnabled) {
      placeholderTextView.setText(placeholderText);
      placeholderTextView.setVisibility(VISIBLE);
      placeholderTextView.bringToFront();
    }
  }

  private void hidePlaceholderText() {
    if (placeholderTextView != null && placeholderEnabled) {
      placeholderTextView.setText(null);
      placeholderTextView.setVisibility(INVISIBLE);
    }
  }

  private void addPlaceholderTextView() {
    if (placeholderTextView != null) {
      inputFrame.addView(placeholderTextView);
      placeholderTextView.setVisibility(VISIBLE);
    }
  }

  private void removePlaceholderTextView() {
    if (placeholderTextView != null) {
      placeholderTextView.setVisibility(GONE);
    }
  }

  /**
   * Sets the text color used by the placeholder text in all states.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_placeholderTextColor
   */
  public void setPlaceholderTextColor(@Nullable ColorStateList placeholderTextColor) {
    if (this.placeholderTextColor != placeholderTextColor) {
      this.placeholderTextColor = placeholderTextColor;
      if (placeholderTextView != null && placeholderTextColor != null) {
        placeholderTextView.setTextColor(placeholderTextColor);
      }
    }
  }

  /**
   * Returns the ColorStateList used for the placeholder text.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_placeholderTextColor
   */
  @Nullable
  public ColorStateList getPlaceholderTextColor() {
    return placeholderTextColor;
  }

  /**
   * Sets the text color and size for the placeholder text from the specified TextAppearance
   * resource.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_placeholderTextAppearance
   */
  public void setPlaceholderTextAppearance(@StyleRes int placeholderTextAppearance) {
    this.placeholderTextAppearance = placeholderTextAppearance;
    if (placeholderTextView != null) {
      TextViewCompat.setTextAppearance(placeholderTextView, placeholderTextAppearance);
    }
  }

  /**
   * Returns the TextAppearance resource used for the placeholder text color.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_placeholderTextAppearance
   */
  @StyleRes
  public int getPlaceholderTextAppearance() {
    return placeholderTextAppearance;
  }

  /**
   * Sets prefix text that will be displayed in the input area when the hint is collapsed before
   * text is entered. If the {@code prefix} is {@code null}, any previous prefix text will be hidden
   * and no prefix text will be shown.
   *
   * @param prefixText Prefix text to display
   * @see #getPrefixText()
   */
  public void setPrefixText(@Nullable final CharSequence prefixText) {
    this.prefixText = TextUtils.isEmpty(prefixText) ? null : prefixText;
    prefixTextView.setText(prefixText);
    updatePrefixTextVisibility();
  }

  /**
   * Returns the prefix text that was set to be displayed with {@link #setPrefixText(CharSequence)},
   * or <code>null</code> if there is no prefix text.
   *
   * @see #setPrefixText(CharSequence)
   */
  @Nullable
  public CharSequence getPrefixText() {
    return prefixText;
  }

  /**
   * Returns the prefix text view.
   *
   * <p>Note: In order for the prefix to work correctly, text should always be set only via {@link
   * #setPrefixText(CharSequence)}, instead of on the {@link TextView} directly.
   *
   * @see #setPrefixText(CharSequence)
   */
  @NonNull
  public TextView getPrefixTextView() {
    return prefixTextView;
  }

  private void updatePrefixTextVisibility() {
    prefixTextView.setVisibility((prefixText != null && !isHintExpanded()) ? VISIBLE : GONE);
    updateDummyDrawables();
  }

  /**
   * Sets the text color used by the prefix text in all states.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_prefixTextColor
   */
  public void setPrefixTextColor(@NonNull ColorStateList prefixTextColor) {
    prefixTextView.setTextColor(prefixTextColor);
  }

  /**
   * Returns the ColorStateList used for the prefix text.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_prefixTextColor
   */
  @Nullable
  public ColorStateList getPrefixTextColor() {
    return prefixTextView.getTextColors();
  }

  /**
   * Sets the text color and size for the prefix text from the specified TextAppearance resource.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_prefixTextAppearance
   */
  public void setPrefixTextAppearance(@StyleRes int prefixTextAppearance) {
    TextViewCompat.setTextAppearance(prefixTextView, prefixTextAppearance);
  }

  private void updatePrefixTextViewPadding() {
    if (editText == null) {
      return;
    }
    int startPadding = isStartIconVisible() ? 0 : ViewCompat.getPaddingStart(editText);
    ViewCompat.setPaddingRelative(
        prefixTextView,
        startPadding,
        editText.getCompoundPaddingTop(),
        getContext()
            .getResources()
            .getDimensionPixelSize(R.dimen.material_input_text_to_prefix_suffix_padding),
        editText.getCompoundPaddingBottom());
  }

  /**
   * Sets suffix text that will be displayed in the input area when the hint is collapsed before
   * text is entered. If the {@code suffix} is {@code null}, any previous suffix text will be hidden
   * and no suffix text will be shown.
   *
   * @param suffixText Suffix text to display
   * @see #getSuffixText()
   */
  public void setSuffixText(@Nullable final CharSequence suffixText) {
    this.suffixText = TextUtils.isEmpty(suffixText) ? null : suffixText;
    suffixTextView.setText(suffixText);
    updateSuffixTextVisibility();
  }

  /**
   * Returns the suffix text that was set to be displayed with {@link #setSuffixText(CharSequence)},
   * or <code>null</code> if there is no suffix text.
   *
   * @see #setSuffixText(CharSequence)
   */
  @Nullable
  public CharSequence getSuffixText() {
    return suffixText;
  }

  /**
   * Returns the suffix text view.
   *
   * <p>Note: In order for the suffix to work correctly, text should always be set only via {@link
   * #setSuffixText(CharSequence)}, instead of on the {@link TextView} directly.
   *
   * @see #setSuffixText(CharSequence)
   */
  @NonNull
  public TextView getSuffixTextView() {
    return suffixTextView;
  }

  private void updateSuffixTextVisibility() {
    int oldSuffixVisibility = suffixTextView.getVisibility();
    boolean visible = suffixText != null && !isHintExpanded();
    suffixTextView.setVisibility(visible ? VISIBLE : GONE);
    if (oldSuffixVisibility != suffixTextView.getVisibility()) {
      getEndIconDelegate().onSuffixVisibilityChanged(visible);
    }
    updateDummyDrawables();
  }

  /**
   * Sets the text color used by the suffix text in all states.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_suffixTextColor
   */
  public void setSuffixTextColor(@NonNull ColorStateList suffixTextColor) {
    suffixTextView.setTextColor(suffixTextColor);
  }

  /**
   * Returns the ColorStateList used for the suffix text.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_suffixTextColor
   */
  @Nullable
  public ColorStateList getSuffixTextColor() {
    return suffixTextView.getTextColors();
  }

  /**
   * Sets the text color and size for the suffix text from the specified TextAppearance resource.
   *
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_suffixTextAppearance
   */
  public void setSuffixTextAppearance(@StyleRes int suffixTextAppearance) {
    TextViewCompat.setTextAppearance(suffixTextView, suffixTextAppearance);
  }

  private void updateSuffixTextViewPadding() {
    if (editText == null) {
      return;
    }
    int endPadding =
        (isEndIconVisible() || isErrorIconVisible()) ? 0 : ViewCompat.getPaddingEnd(editText);
    ViewCompat.setPaddingRelative(
        suffixTextView,
        getContext()
            .getResources()
            .getDimensionPixelSize(R.dimen.material_input_text_to_prefix_suffix_padding),
        editText.getPaddingTop(),
        endPadding,
        editText.getPaddingBottom());
  }

  @Override
  public void setEnabled(boolean enabled) {
    // Since we're set to addStatesFromChildren, we need to make sure that we set all
    // children to enabled/disabled otherwise any enabled children will wipe out our disabled
    // drawable state
    recursiveSetEnabled(this, enabled);
    super.setEnabled(enabled);
  }

  private static void recursiveSetEnabled(@NonNull final ViewGroup vg, final boolean enabled) {
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
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_counterMaxLength
   */
  public int getCounterMaxLength() {
    return counterMaxLength;
  }

  /**
   * Returns the {@code contentDescription} for accessibility purposes of the counter view, or
   * {@code null} if the counter is not enabled, not overflowed, or has no description.
   */
  @Nullable
  CharSequence getCounterOverflowDescription() {
    if (counterEnabled && counterOverflowed && (counterView != null)) {
      return counterView.getContentDescription();
    }
    return null;
  }

  private void updateCounterTextAppearanceAndColor() {
    if (counterView != null) {
      setTextAppearanceCompatWithErrorFallback(
          counterView, counterOverflowed ? counterOverflowTextAppearance : counterTextAppearance);
      if (!counterOverflowed && counterTextColor != null) {
        counterView.setTextColor(counterTextColor);
      }
      if (counterOverflowed && counterOverflowTextColor != null) {
        counterView.setTextColor(counterOverflowTextColor);
      }
    }
  }

  void setTextAppearanceCompatWithErrorFallback(
      @NonNull TextView textView, @StyleRes int textAppearance) {
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
      textView.setTextColor(ContextCompat.getColor(getContext(), R.color.design_error));
    }
  }

  private int calculateLabelMarginTop() {
    if (!hintEnabled) {
      return 0;
    }

    switch (boxBackgroundMode) {
      case BOX_BACKGROUND_OUTLINE:
        return (int) (collapsingTextHelper.getCollapsedTextHeight() / 2);
      case BOX_BACKGROUND_FILLED:
      case BOX_BACKGROUND_NONE:
        return (int) collapsingTextHelper.getCollapsedTextHeight();
      default:
        return 0;
    }
  }

  @NonNull
  private Rect calculateCollapsedTextBounds(@NonNull Rect rect) {
    if (editText == null) {
      throw new IllegalStateException();
    }
    Rect bounds = tmpBoundsRect;
    boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;

    bounds.bottom = rect.bottom;
    switch (boxBackgroundMode) {
      case BOX_BACKGROUND_OUTLINE:
        bounds.left = rect.left + editText.getPaddingLeft();
        bounds.top = rect.top - calculateLabelMarginTop();
        bounds.right = rect.right - editText.getPaddingRight();
        return bounds;
      case BOX_BACKGROUND_FILLED:
        bounds.left = getLabelLeftBoundAlightWithPrefix(rect.left, isRtl);
        bounds.top = rect.top + boxCollapsedPaddingTopPx;
        bounds.right = getLabelRightBoundAlignedWithSuffix(rect.right, isRtl);
        return bounds;
      case BOX_BACKGROUND_NONE:
      default:
        bounds.left = getLabelLeftBoundAlightWithPrefix(rect.left, isRtl);
        bounds.top = getPaddingTop();
        bounds.right = getLabelRightBoundAlignedWithSuffix(rect.right, isRtl);
        return bounds;
    }
  }

  private int getLabelLeftBoundAlightWithPrefix(int rectLeft, boolean isRtl) {
    int left = rectLeft + editText.getCompoundPaddingLeft();
    if (prefixText != null && !isRtl) {
      // Label should be vertically aligned with prefix
      left = left - prefixTextView.getMeasuredWidth() + prefixTextView.getPaddingLeft();
    }
    return left;
  }

  private int getLabelRightBoundAlignedWithSuffix(int rectRight, boolean isRtl) {
    int right = rectRight - editText.getCompoundPaddingRight();
    if (prefixText != null && isRtl) {
      // Label should be vertically aligned with prefix if in RTL
      right += prefixTextView.getMeasuredWidth() - prefixTextView.getPaddingRight();
    }
    return right;
  }

  @NonNull
  private Rect calculateExpandedTextBounds(@NonNull Rect rect) {
    if (editText == null) {
      throw new IllegalStateException();
    }

    Rect bounds = tmpBoundsRect;

    float labelHeight = collapsingTextHelper.getExpandedTextHeight();

    bounds.left = rect.left + editText.getCompoundPaddingLeft();
    bounds.top = calculateExpandedLabelTop(rect, labelHeight);
    bounds.right = rect.right - editText.getCompoundPaddingRight();
    bounds.bottom = calculateExpandedLabelBottom(rect, bounds, labelHeight);

    return bounds;
  }

  private int calculateExpandedLabelTop(@NonNull Rect rect, float labelHeight) {
    if (isSingleLineFilledTextField()) {
      return (int) (rect.centerY() - labelHeight / 2);
    }
    return rect.top + editText.getCompoundPaddingTop();
  }

  private int calculateExpandedLabelBottom(
      @NonNull Rect rect, @NonNull Rect bounds, float labelHeight) {
    if (isSingleLineFilledTextField()) {
      // Add the label's height to the top of the bounds rather than calculating from the vertical
      // center for both the top and bottom of the label. This prevents a potential fractional loss
      // of label height caused by the float to int conversion.
      return (int) (bounds.top + labelHeight);
    }
    return rect.bottom - editText.getCompoundPaddingBottom();
  }

  private boolean isSingleLineFilledTextField() {
    return boxBackgroundMode == BOX_BACKGROUND_FILLED
        && (VERSION.SDK_INT < 16 || editText.getMinLines() <= 1);
  }

  /*
   * Calculates the box background color that should be set.
   *
   * The filled text field has a surface layer with value {@code ?attr/colorSurface} underneath its
   * background that is taken into account when calculating the background color.
   */
  private int calculateBoxBackgroundColor() {
    int backgroundColor = boxBackgroundColor;
    if (boxBackgroundMode == BOX_BACKGROUND_FILLED) {
      int surfaceLayerColor = MaterialColors.getColor(this, R.attr.colorSurface, Color.TRANSPARENT);
      backgroundColor = MaterialColors.layer(surfaceLayerColor, boxBackgroundColor);
    }
    return backgroundColor;
  }

  private void applyBoxAttributes() {
    if (boxBackground == null) {
      return;
    }

    boxBackground.setShapeAppearanceModel(shapeAppearanceModel);

    if (canDrawOutlineStroke()) {
      boxBackground.setStroke(boxStrokeWidthPx, boxStrokeColor);
    }

    boxBackgroundColor = calculateBoxBackgroundColor();
    boxBackground.setFillColor(ColorStateList.valueOf(boxBackgroundColor));
    if (endIconMode == END_ICON_DROPDOWN_MENU) {
      // Makes sure the exposed dropdown menu gets updated properly.
      editText.getBackground().invalidateSelf();
    }
    applyBoxUnderlineAttributes();
    invalidate();
  }

  private void applyBoxUnderlineAttributes() {
    // Exit if the underline is not being drawn by TextInputLayout.
    if (boxUnderline == null) {
      return;
    }

    if (canDrawStroke()) {
      boxUnderline.setFillColor(ColorStateList.valueOf(boxStrokeColor));
    }
    invalidate();
  }

  private boolean canDrawOutlineStroke() {
    return boxBackgroundMode == BOX_BACKGROUND_OUTLINE && canDrawStroke();
  }

  private boolean canDrawStroke() {
    return boxStrokeWidthPx > -1 && boxStrokeColor != Color.TRANSPARENT;
  }

  void updateEditTextBackground() {
    // Only update the color filter for the legacy text field, since we can directly change the
    // Paint colors of the MaterialShapeDrawable box background without having to use color filters.
    if (editText == null || boxBackgroundMode != BOX_BACKGROUND_NONE) {
      return;
    }

    Drawable editTextBackground = editText.getBackground();
    if (editTextBackground == null) {
      return;
    }

    if (androidx.appcompat.widget.DrawableUtils.canSafelyMutateDrawable(editTextBackground)) {
      editTextBackground = editTextBackground.mutate();
    }

    if (indicatorViewController.errorShouldBeShown()) {
      // Set a color filter for the error color
      editTextBackground.setColorFilter(
          AppCompatDrawableManager.getPorterDuffColorFilter(
              indicatorViewController.getErrorViewCurrentTextColor(), PorterDuff.Mode.SRC_IN));
    } else if (counterOverflowed && counterView != null) {
      // Set a color filter of the counter color
      editTextBackground.setColorFilter(
          AppCompatDrawableManager.getPorterDuffColorFilter(
              counterView.getCurrentTextColor(), PorterDuff.Mode.SRC_IN));
    } else {
      // Else reset the color filter and refresh the drawable state so that the
      // normal tint is used
      DrawableCompat.clearColorFilter(editTextBackground);
      editText.refreshDrawableState();
    }
  }

  static class SavedState extends AbsSavedState {
    @Nullable CharSequence error;
    boolean isEndIconChecked;
    @Nullable CharSequence hintText;
    @Nullable CharSequence helperText;
    @Nullable CharSequence placeholderText;

    SavedState(Parcelable superState) {
      super(superState);
    }

    SavedState(@NonNull Parcel source, ClassLoader loader) {
      super(source, loader);
      error = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
      isEndIconChecked = (source.readInt() == 1);
      hintText =  TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
      helperText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
      placeholderText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      TextUtils.writeToParcel(error, dest, flags);
      dest.writeInt(isEndIconChecked ? 1 : 0);
      TextUtils.writeToParcel(hintText, dest, flags);
      TextUtils.writeToParcel(helperText, dest, flags);
      TextUtils.writeToParcel(placeholderText, dest, flags);
    }

    @NonNull
    @Override
    public String toString() {
      return "TextInputLayout.SavedState{"
          + Integer.toHexString(System.identityHashCode(this))
          + " error="
          + error
          + " hint="
          + hintText
          + " helperText="
          + helperText
          + " placeholderText="
          + placeholderText
          + "}";
    }

    public static final Creator<SavedState> CREATOR =
        new ClassLoaderCreator<SavedState>() {
          @NonNull
          @Override
          public SavedState createFromParcel(@NonNull Parcel in, ClassLoader loader) {
            return new SavedState(in, loader);
          }

          @Nullable
          @Override
          public SavedState createFromParcel(@NonNull Parcel in) {
            return new SavedState(in, null);
          }

          @NonNull
          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }

  @Nullable
  @Override
  public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState ss = new SavedState(superState);
    if (indicatorViewController.errorShouldBeShown()) {
      ss.error = getError();
    }
    ss.isEndIconChecked = hasEndIcon() && endIconView.isChecked();
    ss.hintText = getHint();
    ss.helperText = getHelperText();
    ss.placeholderText = getPlaceholderText();
    return ss;
  }

  @Override
  protected void onRestoreInstanceState(@Nullable Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }
    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(ss.getSuperState());
    setError(ss.error);
    if (ss.isEndIconChecked) {
      // Make sure the end icon is not clicked before the application is visible.
      endIconView.post(
          new Runnable() {
            @Override
            public void run() {
              endIconView.performClick();
              // Skip animation
              endIconView.jumpDrawablesToCurrentState();
            }
          });
    }
    setHint(ss.hintText);
    setHelperText(ss.helperText);
    setPlaceholderText(ss.placeholderText);
    requestLayout();
  }

  @Override
  protected void dispatchRestoreInstanceState(@NonNull SparseArray<Parcelable> container) {
    restoringSavedState = true;
    super.dispatchRestoreInstanceState(container);
    restoringSavedState = false;
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
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_hintAnimationEnabled
   */
  public boolean isHintAnimationEnabled() {
    return hintAnimationEnabled;
  }

  /**
   * Set whether any hint state changes, due to being focused or non-empty text, are animated.
   *
   * @see #isHintAnimationEnabled()
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_hintAnimationEnabled
   */
  public void setHintAnimationEnabled(boolean enabled) {
    hintAnimationEnabled = enabled;
  }

  /**
   * Returns whether the hint expands to occupy the input area when the text field is unpopulated
   * and not focused.
   *
   * @see #setExpandedHintEnabled(boolean)
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_hintExpadedEnabled
   */
  public boolean isExpandedHintEnabled() {
    return expandedHintEnabled;
  }

  /**
   * Sets whether the hint should expand to occupy the input area when the text field is
   * unpopulated and not focused.
   *
   * @see #isExpandedHintEnabled()
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_hintExpadedEnabled
   */
  public void setExpandedHintEnabled(boolean enabled) {
    if (expandedHintEnabled != enabled) {
      expandedHintEnabled = enabled;
      updateLabelState(false);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    boolean updatedHeight = updateEditTextHeightBasedOnIcon();
    boolean updatedIcon = updateDummyDrawables();
    if (updatedHeight || updatedIcon) {
      editText.post(
          new Runnable() {
            @Override
            public void run() {
              editText.requestLayout();
            }
          });
    }
    updatePlaceholderMeasurementsBasedOnEditText();
    updatePrefixTextViewPadding();
    updateSuffixTextViewPadding();
  }

  private boolean updateEditTextHeightBasedOnIcon() {
    if (editText == null) {
      return false;
    }

    // We need to make sure that the EditText's height is at least the same as the end or start
    // icon's height (whichever is bigger). This ensures focus works properly, and there is no
    // visual jump if the icon is enabled/disabled.
    int maxIconHeight = Math.max(endLayout.getMeasuredHeight(), startLayout.getMeasuredHeight());
    if (editText.getMeasuredHeight() < maxIconHeight) {
      editText.setMinimumHeight(maxIconHeight);
      return true;
    }

    return false;
  }

  private void updatePlaceholderMeasurementsBasedOnEditText() {
    if (placeholderTextView != null && editText != null) {
      // Use the EditText's positioning for the placeholder.
      final int editTextGravity = this.editText.getGravity();
      placeholderTextView.setGravity(editTextGravity);

      placeholderTextView.setPadding(
          editText.getCompoundPaddingLeft(),
          editText.getCompoundPaddingTop(),
          editText.getCompoundPaddingRight(),
          editText.getCompoundPaddingBottom());
    }
  }

  /**
   * Sets the start icon.
   *
   * <p>If you use an icon you should also set a description for its action using {@link
   * #setStartIconContentDescription(CharSequence)}. This is used for accessibility.
   *
   * @param resId resource id of the drawable to set, or 0 to clear and remove the icon
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_startIconDrawable
   */
  public void setStartIconDrawable(@DrawableRes int resId) {
    setStartIconDrawable(resId != 0 ? AppCompatResources.getDrawable(getContext(), resId) : null);
  }

  /**
   * Sets the start icon.
   *
   * <p>If you use an icon you should also set a description for its action using {@link
   * #setStartIconContentDescription(CharSequence)}. This is used for accessibility.
   *
   * @param startIconDrawable Drawable to set, may be null to clear and remove the icon
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_startIconDrawable
   */
  public void setStartIconDrawable(@Nullable Drawable startIconDrawable) {
    startIconView.setImageDrawable(startIconDrawable);
    if (startIconDrawable != null) {
      setStartIconVisible(true);
      refreshStartIconDrawableState();
    } else {
      setStartIconVisible(false);
      setStartIconOnClickListener(null);
      setStartIconOnLongClickListener(null);
      setStartIconContentDescription(null);
    }
  }

  /**
   * Returns the start icon.
   *
   * @see #setStartIconDrawable(Drawable)
   * @return the drawable used for the start icon
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_startIconDrawable
   */
  @Nullable
  public Drawable getStartIconDrawable() {
    return startIconView.getDrawable();
  }

  /**
   * Sets the start icon's functionality that is performed when the start icon is clicked. The icon
   * will not be clickable if its click and long click listeners are null.
   *
   * @param startIconOnClickListener the {@link android.view.View.OnClickListener} the start icon
   *     view will have, or null to clear it.
   */
  public void setStartIconOnClickListener(@Nullable OnClickListener startIconOnClickListener) {
    setIconOnClickListener(startIconView, startIconOnClickListener, startIconOnLongClickListener);
  }

  /**
   * Sets the start icon's functionality that is performed when the start icon is long clicked. The
   * icon will not be clickable if its click and long click listeners are null.
   *
   * @param startIconOnLongClickListener the {@link android.view.View.OnLongClickListener} the start
   *     icon view will have, or null to clear it.
   */
  public void setStartIconOnLongClickListener(
      @Nullable OnLongClickListener startIconOnLongClickListener) {
    this.startIconOnLongClickListener = startIconOnLongClickListener;
    setIconOnLongClickListener(startIconView, startIconOnLongClickListener);
  }

  /**
   * Sets the start icon to be VISIBLE or GONE.
   *
   * @param visible whether the icon should be set to visible
   */
  public void setStartIconVisible(boolean visible) {
    if (isStartIconVisible() != visible) {
      startIconView.setVisibility(visible ? View.VISIBLE : View.GONE);
      updatePrefixTextViewPadding();
      updateDummyDrawables();
    }
  }

  /**
   * Returns whether the current start icon is visible.
   *
   * @see #setStartIconVisible(boolean)
   */
  public boolean isStartIconVisible() {
    return startIconView.getVisibility() == View.VISIBLE;
  }

  /**
   * This method should be called from within your icon's click listener if your icon's tint list
   * has a color for a state that depends on a click (such as checked state).
   */
  public void refreshStartIconDrawableState() {
    refreshIconDrawableState(startIconView, startIconTintList);
  }

  /**
   * Sets the current start icon to be checkable or not.
   *
   * <p>If the icon works just as a button and the fact that it's checked or not doesn't affect its
   * behavior, such as the clear text end icon, calling this method is encouraged so that screen
   * readers will not announce the icon's checked state.
   *
   * @param startIconCheckable whether the icon should be checkable
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_startIconCheckable
   */
  public void setStartIconCheckable(boolean startIconCheckable) {
    startIconView.setCheckable(startIconCheckable);
  }

  /**
   * Returns whether the start icon is checkable.
   *
   * @see #setStartIconCheckable(boolean)
   */
  public boolean isStartIconCheckable() {
    return startIconView.isCheckable();
  }

  /**
   * Set a content description for the start icon.
   *
   * <p>The content description will be read via screen readers or other accessibility systems to
   * explain the purpose or action of the icon.
   *
   * @param resId Resource ID of a content description string to set, or 0 to clear the description
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_startIconContentDescription
   */
  public void setStartIconContentDescription(@StringRes int resId) {
    setStartIconContentDescription(resId != 0 ? getResources().getText(resId) : null);
  }

  /**
   * Set a content description for the start icon.
   *
   * <p>The content description will be read via screen readers or other accessibility systems to
   * explain the purpose or action of the icon.
   *
   * @param startIconContentDescription Content description to set, or null to clear the content
   *     description
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_startIconContentDescription
   */
  public void setStartIconContentDescription(@Nullable CharSequence startIconContentDescription) {
    if (getStartIconContentDescription() != startIconContentDescription) {
      startIconView.setContentDescription(startIconContentDescription);
    }
  }

  /**
   * Returns the currently configured content description for the start icon.
   *
   * <p>This will be used to describe the navigation action to users through mechanisms such as
   * screen readers.
   */
  @Nullable
  public CharSequence getStartIconContentDescription() {
    return startIconView.getContentDescription();
  }

  /**
   * Applies a tint to the start icon drawable. Does not modify the current tint mode, which is
   * {@link PorterDuff.Mode#SRC_IN} by default.
   *
   * <p>Subsequent calls to {@link #setStartIconDrawable(Drawable)} will automatically mutate the
   * drawable and apply the specified tint and tint mode using {@link
   * DrawableCompat#setTintList(Drawable, ColorStateList)}.
   *
   * @param startIconTintList the tint to apply, may be null to clear tint
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_startIconTint
   */
  public void setStartIconTintList(@Nullable ColorStateList startIconTintList) {
    if (this.startIconTintList != startIconTintList) {
      this.startIconTintList = startIconTintList;
      hasStartIconTintList = true;
      applyStartIconTint();
    }
  }

  /**
   * Specifies the blending mode used to apply the tint specified by {@link
   * #setEndIconTintList(ColorStateList)} to the start icon drawable. The default mode is {@link
   * PorterDuff.Mode#SRC_IN}.
   *
   * @param startIconTintMode the blending mode used to apply the tint, may be null to clear tint
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_startIconTintMode
   */
  public void setStartIconTintMode(@Nullable PorterDuff.Mode startIconTintMode) {
    if (this.startIconTintMode != startIconTintMode) {
      this.startIconTintMode = startIconTintMode;
      hasStartIconTintMode = true;
      applyStartIconTint();
    }
  }

  /**
   * Set up the end icon mode. When set, a button is placed at the end of the EditText which enables
   * the user to perform the specific icon's functionality.
   *
   * @param endIconMode the end icon mode to be set: {@link #END_ICON_PASSWORD_TOGGLE}, {@link
   *     #END_ICON_CLEAR_TEXT}, or {@link #END_ICON_CUSTOM}; or {@link #END_ICON_NONE} to clear the
   *     current icon if any
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_endIconMode
   */
  public void setEndIconMode(@EndIconMode int endIconMode) {
    int previousEndIconMode = this.endIconMode;
    this.endIconMode = endIconMode;
    dispatchOnEndIconChanged(previousEndIconMode);
    setEndIconVisible(endIconMode != END_ICON_NONE);
    if (getEndIconDelegate().isBoxBackgroundModeSupported(boxBackgroundMode)) {
      getEndIconDelegate().initialize();
    } else {
      throw new IllegalStateException(
          "The current box background mode "
              + boxBackgroundMode
              + " is not supported by the end icon mode "
              + endIconMode);
    }
    applyEndIconTint();
  }

  /**
   * Returns the current end icon mode.
   *
   * @return the end icon mode enum
   * @see #setEndIconMode(int)
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_endIconMode
   */
  @EndIconMode
  public int getEndIconMode() {
    return endIconMode;
  }

  /**
   * Sets the end icon's functionality that is performed when the icon is clicked. The icon will not
   * be clickable if its click and long click listeners are null.
   *
   * @param endIconOnClickListener the {@link android.view.View.OnClickListener} the end icon view
   *     will have
   */
  public void setEndIconOnClickListener(@Nullable OnClickListener endIconOnClickListener) {
    setIconOnClickListener(endIconView, endIconOnClickListener, endIconOnLongClickListener);
  }

  /**
   * Sets the error icon's functionality that is performed when the icon is clicked. The icon will
   * not be clickable if its click and long click listeners are null.
   *
   * @param errorIconOnClickListener the {@link android.view.View.OnClickListener} the error icon
   *     view will have
   */
  public void setErrorIconOnClickListener(@Nullable OnClickListener errorIconOnClickListener) {
    setIconOnClickListener(errorIconView, errorIconOnClickListener, errorIconOnLongClickListener);
  }

  /**
   * Sets the end icon's functionality that is performed when the end icon is long clicked. The icon
   * will not be clickable if its click and long click listeners are null.
   *
   * @param endIconOnLongClickListener the {@link android.view.View.OnLongClickListener} the end
   *     icon view will have, or null to clear it.
   */
  public void setEndIconOnLongClickListener(
      @Nullable OnLongClickListener endIconOnLongClickListener) {
    this.endIconOnLongClickListener = endIconOnLongClickListener;
    setIconOnLongClickListener(endIconView, endIconOnLongClickListener);
  }

  /**
   * Sets the error icon's functionality that is performed when the end icon is long clicked. The
   * icon will not be clickable if its click and long click listeners are null.
   *
   * @param errorIconOnLongClickListener the {@link android.view.View.OnLongClickListener} the error
   *     icon view will have, or null to clear it.
   */
  public void setErrorIconOnLongClickListener(
      @Nullable OnLongClickListener errorIconOnLongClickListener) {
    this.errorIconOnLongClickListener = errorIconOnLongClickListener;
    setIconOnLongClickListener(errorIconView, errorIconOnLongClickListener);
  }

  /**
   * This method should be called from within your icon's click listener if your icon's tint list
   * has a color for a state that depends on a click (such as checked state).
   */
  public void refreshErrorIconDrawableState() {
    refreshIconDrawableState(errorIconView, errorIconTintList);
  }

  /**
   * Sets the current end icon to be VISIBLE or GONE.
   *
   * @param visible whether the icon should be set to visible
   */
  public void setEndIconVisible(boolean visible) {
    if (isEndIconVisible() != visible) {
      endIconView.setVisibility(visible ? View.VISIBLE : View.GONE);
      updateSuffixTextViewPadding();
      updateDummyDrawables();
    }
  }

  /**
   * Returns whether the current end icon is visible.
   *
   * @see #setEndIconVisible(boolean)
   */
  public boolean isEndIconVisible() {
    return endIconFrame.getVisibility() == VISIBLE && endIconView.getVisibility() == VISIBLE;
  }

  /**
   * Sets the current end icon's state to be activated or not.
   *
   * @param endIconActivated whether the icon should be activated
   */
  public void setEndIconActivated(boolean endIconActivated) {
    endIconView.setActivated(endIconActivated);
  }

  /**
   * This method should be called from within your icon's click listener if your icon's tint list
   * has a color for a state that depends on a click (such as checked state).
   */
  public void refreshEndIconDrawableState() {
    refreshIconDrawableState(endIconView, endIconTintList);
  }

  /**
   * Sets the current end icon to be checkable or not.
   *
   * <p>If the icon works just as a button and the fact that it's checked or not doesn't affect its
   * behavior, such as the clear text end icon, calling this method is encouraged so that screen
   * readers will not announce the icon's checked state.
   *
   * @param endIconCheckable whether the icon should be checkable
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_endIconCheckable
   */
  public void setEndIconCheckable(boolean endIconCheckable) {
    endIconView.setCheckable(endIconCheckable);
  }

  /**
   * Returns whether the end icon is checkable.
   *
   * @see #setEndIconCheckable(boolean)
   */
  public boolean isEndIconCheckable() {
    return endIconView.isCheckable();
  }

  /**
   * Set the icon to use for the end icon.
   *
   * <p>If you use an icon you should also set a description for its action using {@link
   * #setEndIconContentDescription(CharSequence)}. This is used for accessibility.
   *
   * @param resId resource id of the drawable to set, or 0 to clear the icon
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_endIconDrawable
   */
  public void setEndIconDrawable(@DrawableRes int resId) {
    setEndIconDrawable(resId != 0 ? AppCompatResources.getDrawable(getContext(), resId) : null);
  }

  /**
   * Set the icon to use for the end icon.
   *
   * <p>If you use an icon you should also set a description for its action using {@link
   * #setEndIconContentDescription(CharSequence)}. This is used for accessibility.
   *
   * @param endIconDrawable Drawable to set, may be null to clear the icon
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_endIconDrawable
   */
  public void setEndIconDrawable(@Nullable Drawable endIconDrawable) {
    endIconView.setImageDrawable(endIconDrawable);
    refreshEndIconDrawableState();
  }

  /**
   * Returns the drawable currently used for the end icon.
   *
   * @see #setEndIconDrawable(Drawable)
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_endIconDrawable
   */
  @Nullable
  public Drawable getEndIconDrawable() {
    return endIconView.getDrawable();
  }

  /**
   * Set a content description for the end icon.
   *
   * <p>The content description will be read via screen readers or other accessibility systems to
   * explain the action of the icon.
   *
   * @param resId Resource ID of a content description string to set, or 0 to clear the description
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_endIconContentDescription
   */
  public void setEndIconContentDescription(@StringRes int resId) {
    setEndIconContentDescription(resId != 0 ? getResources().getText(resId) : null);
  }

  /**
   * Set a content description for the end icon.
   *
   * <p>The content description will be read via screen readers or other accessibility systems to
   * explain the action of the icon.
   *
   * @param endIconContentDescription Content description to set, or null to clear the content
   *     description
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_endIconContentDescription
   */
  public void setEndIconContentDescription(@Nullable CharSequence endIconContentDescription) {
    if (getEndIconContentDescription() != endIconContentDescription) {
      endIconView.setContentDescription(endIconContentDescription);
    }
  }

  /**
   * Returns the currently configured content description for the end icon.
   *
   * <p>This will be used to describe the navigation action to users through mechanisms such as
   * screen readers.
   */
  @Nullable
  public CharSequence getEndIconContentDescription() {
    return endIconView.getContentDescription();
  }

  /**
   * Applies a tint to the end icon drawable. Does not modify the current tint mode, which is {@link
   * PorterDuff.Mode#SRC_IN} by default.
   *
   * <p>Subsequent calls to {@link #setEndIconDrawable(Drawable)} will automatically mutate the
   * drawable and apply the specified tint and tint mode using {@link
   * DrawableCompat#setTintList(Drawable, ColorStateList)}.
   *
   * @param endIconTintList the tint to apply, may be null to clear tint
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_endIconTint
   */
  public void setEndIconTintList(@Nullable ColorStateList endIconTintList) {
    if (this.endIconTintList != endIconTintList) {
      this.endIconTintList = endIconTintList;
      hasEndIconTintList = true;
      applyEndIconTint();
    }
  }

  /**
   * Specifies the blending mode used to apply the tint specified by {@link
   * #setEndIconTintList(ColorStateList)} to the end icon drawable. The default mode is {@link
   * PorterDuff.Mode#SRC_IN}.
   *
   * @param endIconTintMode the blending mode used to apply the tint, may be null to clear tint
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_endIconTintMode
   */
  public void setEndIconTintMode(@Nullable PorterDuff.Mode endIconTintMode) {
    if (this.endIconTintMode != endIconTintMode) {
      this.endIconTintMode = endIconTintMode;
      hasEndIconTintMode = true;
      applyEndIconTint();
    }
  }

  /**
   * Add a {@link TextInputLayout.OnEndIconChangedListener} that will be invoked when the end icon
   * gets changed.
   *
   * <p>Components that add a listener should take care to remove it when finished via {@link
   * #removeOnEndIconChangedListener(OnEndIconChangedListener)}.
   *
   * @param listener listener to add
   */
  public void addOnEndIconChangedListener(@NonNull OnEndIconChangedListener listener) {
    endIconChangedListeners.add(listener);
  }

  /**
   * Remove the given {@link TextInputLayout.OnEndIconChangedListener} that was previously added via
   * {@link #addOnEndIconChangedListener(OnEndIconChangedListener)}.
   *
   * @param listener listener to remove
   */
  public void removeOnEndIconChangedListener(@NonNull OnEndIconChangedListener listener) {
    endIconChangedListeners.remove(listener);
  }

  /** Remove all previously added {@link TextInputLayout.OnEndIconChangedListener}s. */
  public void clearOnEndIconChangedListeners() {
    endIconChangedListeners.clear();
  }

  /**
   * Add a {@link OnEditTextAttachedListener} that will be invoked when the edit text is attached,
   * or from this method if the EditText is already present.
   *
   * <p>Components that add a listener should take care to remove it when finished via {@link
   * #removeOnEditTextAttachedListener(OnEditTextAttachedListener)}.
   *
   * @param listener listener to add
   */
  public void addOnEditTextAttachedListener(@NonNull OnEditTextAttachedListener listener) {
    editTextAttachedListeners.add(listener);
    if (editText != null) {
      listener.onEditTextAttached(this);
    }
  }

  /**
   * Remove the given {@link OnEditTextAttachedListener} that was previously added via {@link
   * #addOnEditTextAttachedListener(OnEditTextAttachedListener)}.
   *
   * @param listener listener to remove
   */
  public void removeOnEditTextAttachedListener(@NonNull OnEditTextAttachedListener listener) {
    editTextAttachedListeners.remove(listener);
  }

  /** Remove all previously added {@link OnEditTextAttachedListener}s. */
  public void clearOnEditTextAttachedListeners() {
    editTextAttachedListeners.clear();
  }

  /**
   * Set the icon to use for the password visibility toggle button.
   *
   * <p>If you use an icon you should also set a description for its action using {@link
   * #setPasswordVisibilityToggleContentDescription(CharSequence)}. This is used for accessibility.
   *
   * @param resId resource id of the drawable to set, or 0 to clear the icon
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_passwordToggleDrawable
   * @deprecated Use {@link #setEndIconDrawable(int)} instead.
   */
  @Deprecated
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
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_passwordToggleDrawable
   * @deprecated Use {@link #setEndIconDrawable(Drawable)} instead.
   */
  @Deprecated
  public void setPasswordVisibilityToggleDrawable(@Nullable Drawable icon) {
    endIconView.setImageDrawable(icon);
  }

  /**
   * Set a content description for the navigation button if one is present.
   *
   * <p>The content description will be read via screen readers or other accessibility systems to
   * explain the action of the password visibility toggle.
   *
   * @param resId Resource ID of a content description string to set, or 0 to clear the description
   * @attr ref
   *     com.google.android.material.R.styleable#TextInputLayout_passwordToggleContentDescription
   * @deprecated Use {@link #setEndIconContentDescription(int)} instead.
   */
  @Deprecated
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
   * @attr ref
   *     com.google.android.material.R.styleable#TextInputLayout_passwordToggleContentDescription
   * @deprecated Use {@link #setEndIconContentDescription(CharSequence)} instead.
   */
  @Deprecated
  public void setPasswordVisibilityToggleContentDescription(@Nullable CharSequence description) {
    endIconView.setContentDescription(description);
  }

  /**
   * Returns the icon currently used for the password visibility toggle button.
   *
   * @see #setPasswordVisibilityToggleDrawable(Drawable)
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_passwordToggleDrawable
   * @deprecated Use {@link #getEndIconDrawable()} instead.
   */
  @Nullable
  @Deprecated
  public Drawable getPasswordVisibilityToggleDrawable() {
    return endIconView.getDrawable();
  }

  /**
   * Returns the currently configured content description for the password visibility toggle button.
   *
   * <p>This will be used to describe the navigation action to users through mechanisms such as
   * screen readers.
   *
   * @deprecated Use {@link #getEndIconContentDescription()} instead.
   */
  @Nullable
  @Deprecated
  public CharSequence getPasswordVisibilityToggleContentDescription() {
    return endIconView.getContentDescription();
  }

  /**
   * Returns whether the password visibility toggle functionality is currently enabled.
   *
   * @see #setPasswordVisibilityToggleEnabled(boolean)
   * @deprecated Use {@link #getEndIconMode()} instead.
   */
  @Deprecated
  public boolean isPasswordVisibilityToggleEnabled() {
    return endIconMode == END_ICON_PASSWORD_TOGGLE;
  }

  /**
   * Enables or disable the password visibility toggle functionality.
   *
   * <p>When enabled, a button is placed at the end of the EditText which enables the user to switch
   * between the field's input being visibly disguised or not.
   *
   * @param enabled true to enable the functionality
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_passwordToggleEnabled
   * @deprecated Use {@link #setEndIconMode(int)} instead.
   */
  @Deprecated
  public void setPasswordVisibilityToggleEnabled(final boolean enabled) {
    if (enabled && endIconMode != END_ICON_PASSWORD_TOGGLE) {
      // Set password toggle end icon if it's not already set
      setEndIconMode(END_ICON_PASSWORD_TOGGLE);
    } else if (!enabled) {
      // Set end icon to null
      setEndIconMode(END_ICON_NONE);
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
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_passwordToggleTint
   * @deprecated Use {@link #setEndIconTintList(ColorStateList)} instead.
   */
  @Deprecated
  public void setPasswordVisibilityToggleTintList(@Nullable ColorStateList tintList) {
    endIconTintList = tintList;
    hasEndIconTintList = true;
    applyEndIconTint();
  }

  /**
   * Specifies the blending mode used to apply the tint specified by {@link
   * #setPasswordVisibilityToggleTintList(ColorStateList)} to the password visibility toggle
   * drawable. The default mode is {@link PorterDuff.Mode#SRC_IN}.
   *
   * @param mode the blending mode used to apply the tint, may be null to clear tint
   * @attr ref com.google.android.material.R.styleable#TextInputLayout_passwordToggleTintMode
   * @deprecated Use {@link #setEndIconTintMode(PorterDuff.Mode)} instead.
   */
  @Deprecated
  public void setPasswordVisibilityToggleTintMode(@Nullable PorterDuff.Mode mode) {
    endIconTintMode = mode;
    hasEndIconTintMode = true;
    applyEndIconTint();
  }

  /**
   * Handles visibility for a password toggle icon when changing obfuscation in a password edit
   * text. Public so that clients can override this method for custom UI changes when toggling the
   * display of password text
   *
   * @param shouldSkipAnimations true if the password toggle indicator icon should not animate
   *     changes
   * @deprecated The password toggle will show as checked or unchecked depending on whether the
   *     {@link EditText}'s {@link android.text.method.TransformationMethod} is of type {@link
   *     android.text.method.PasswordTransformationMethod}
   */
  @Deprecated
  public void passwordVisibilityToggleRequested(boolean shouldSkipAnimations) {
    if (endIconMode == END_ICON_PASSWORD_TOGGLE) {
      endIconView.performClick();
      if (shouldSkipAnimations) {
        endIconView.jumpDrawablesToCurrentState();
      }
    }
  }

  /**
   * Sets an {@link TextInputLayout.AccessibilityDelegate} providing an accessibility implementation
   * for the {@link EditText} used by this layout.
   *
   * <p>Note: This method should be used in place of providing an {@link AccessibilityDelegate}
   * directly on the {@link EditText}.
   */
  public void setTextInputAccessibilityDelegate(
      @Nullable TextInputLayout.AccessibilityDelegate delegate) {
    if (editText != null) {
      ViewCompat.setAccessibilityDelegate(editText, delegate);
    }
  }

  @NonNull
  CheckableImageButton getEndIconView() {
    return endIconView;
  }

  private EndIconDelegate getEndIconDelegate() {
    EndIconDelegate endIconDelegate = endIconDelegates.get(endIconMode);
    return endIconDelegate != null ? endIconDelegate : endIconDelegates.get(END_ICON_NONE);
  }

  private void dispatchOnEditTextAttached() {
    for (OnEditTextAttachedListener listener : editTextAttachedListeners) {
      listener.onEditTextAttached(this);
    }
  }

  private void applyStartIconTint() {
    applyIconTint(
        startIconView,
        hasStartIconTintList,
        startIconTintList,
        hasStartIconTintMode,
        startIconTintMode);
  }

  private boolean hasEndIcon() {
    return endIconMode != END_ICON_NONE;
  }

  private void dispatchOnEndIconChanged(@EndIconMode int previousIcon) {
    for (OnEndIconChangedListener listener : endIconChangedListeners) {
      listener.onEndIconChanged(this, previousIcon);
    }
  }

  private void tintEndIconOnError(boolean tintEndIconOnError) {
    if (tintEndIconOnError && getEndIconDrawable() != null) {
      // Setting the tint here instead of calling setEndIconTintList() in order to preserve and
      // restore the icon's original tint.
      Drawable endIconDrawable = DrawableCompat.wrap(getEndIconDrawable()).mutate();
      DrawableCompat.setTint(
          endIconDrawable, indicatorViewController.getErrorViewCurrentTextColor());
      endIconView.setImageDrawable(endIconDrawable);
    } else {
      applyEndIconTint();
    }
  }

  private void applyEndIconTint() {
    applyIconTint(
        endIconView, hasEndIconTintList, endIconTintList, hasEndIconTintMode, endIconTintMode);
  }

  /*
   * We need to add a dummy drawable as the start and/or end compound drawables so that the text is
   * indented and doesn't display below the icon or suffix/prefix views.
   */
  private boolean updateDummyDrawables() {
    if (editText == null) {
      return false;
    }

    boolean updatedIcon = false;
    // Update start dummy drawable if needed.
    if (shouldUpdateStartDummyDrawable()) {
      int right = startLayout.getMeasuredWidth() - editText.getPaddingLeft();
      if (startDummyDrawable == null || startDummyDrawableWidth != right) {
        startDummyDrawable = new ColorDrawable();
        startDummyDrawableWidth = right;
        startDummyDrawable.setBounds(0, 0, startDummyDrawableWidth, 1);
      }
      final Drawable[] compounds = TextViewCompat.getCompoundDrawablesRelative(editText);
      if (compounds[0] != startDummyDrawable) {
        TextViewCompat.setCompoundDrawablesRelative(
            editText, startDummyDrawable, compounds[1], compounds[2], compounds[3]);
        updatedIcon = true;
      }
    } else if (startDummyDrawable != null) {
      // Remove the dummy start compound drawable if it exists and clear it.
      final Drawable[] compounds = TextViewCompat.getCompoundDrawablesRelative(editText);
      TextViewCompat.setCompoundDrawablesRelative(
          editText, null, compounds[1], compounds[2], compounds[3]);
      startDummyDrawable = null;
      updatedIcon = true;
    }

    // Update end dummy drawable if needed.
    if (shouldUpdateEndDummyDrawable()) {
      int right = suffixTextView.getMeasuredWidth() - editText.getPaddingRight();
      View iconView = getEndIconToUpdateDummyDrawable();
      if (iconView != null) {
        right =
            right
                + iconView.getMeasuredWidth()
                + MarginLayoutParamsCompat.getMarginStart(
                    ((MarginLayoutParams) iconView.getLayoutParams()));
      }
      final Drawable[] compounds = TextViewCompat.getCompoundDrawablesRelative(editText);
      if (endDummyDrawable != null && endDummyDrawableWidth != right) {
        // If endLayout only changed width, update dummy drawable here so that we don't override
        // the currently saved originalEditTextEndDrawable.
        endDummyDrawableWidth = right;
        endDummyDrawable.setBounds(0, 0, endDummyDrawableWidth, 1);
        TextViewCompat.setCompoundDrawablesRelative(
            editText, compounds[0], compounds[1], endDummyDrawable, compounds[3]);
        updatedIcon = true;
      } else {
        if (endDummyDrawable == null) {
          endDummyDrawable = new ColorDrawable();
          endDummyDrawableWidth = right;
          endDummyDrawable.setBounds(0, 0, endDummyDrawableWidth, 1);
        }
        // Store the user defined end compound drawable so that we can restore it later.
        if (compounds[2] != endDummyDrawable) {
          originalEditTextEndDrawable = compounds[2];
          TextViewCompat.setCompoundDrawablesRelative(
              editText, compounds[0], compounds[1], endDummyDrawable, compounds[3]);
          updatedIcon = true;
        }
      }
    } else if (endDummyDrawable != null) {
      // Remove the dummy end compound drawable if it exists and clear it.
      final Drawable[] compounds = TextViewCompat.getCompoundDrawablesRelative(editText);
      if (compounds[2] == endDummyDrawable) {
        TextViewCompat.setCompoundDrawablesRelative(
            editText, compounds[0], compounds[1], originalEditTextEndDrawable, compounds[3]);
        updatedIcon = true;
      }
      endDummyDrawable = null;
    }

    return updatedIcon;
  }

  private boolean shouldUpdateStartDummyDrawable() {
    return (getStartIconDrawable() != null || prefixText != null)
        && (startLayout.getMeasuredWidth() > 0);
  }

  private boolean shouldUpdateEndDummyDrawable() {
    return (errorIconView.getVisibility() == VISIBLE
            || (hasEndIcon() && isEndIconVisible())
            || suffixText != null)
        && (endLayout.getMeasuredWidth() > 0);
  }

  @Nullable
  private CheckableImageButton getEndIconToUpdateDummyDrawable() {
    if (errorIconView.getVisibility() == VISIBLE) {
      return errorIconView;
    } else if (hasEndIcon() && isEndIconVisible()) {
      return endIconView;
    } else {
      return null;
    }
  }

  private void applyIconTint(
      @NonNull CheckableImageButton iconView,
      boolean hasIconTintList,
      ColorStateList iconTintList,
      boolean hasIconTintMode,
      PorterDuff.Mode iconTintMode) {
    Drawable icon = iconView.getDrawable();
    if (icon != null && (hasIconTintList || hasIconTintMode)) {
      icon = DrawableCompat.wrap(icon).mutate();

      if (hasIconTintList) {
        DrawableCompat.setTintList(icon, iconTintList);
      }
      if (hasIconTintMode) {
        DrawableCompat.setTintMode(icon, iconTintMode);
      }
    }

    if (iconView.getDrawable() != icon) {
      iconView.setImageDrawable(icon);
    }
  }

  private static void setIconOnClickListener(
      @NonNull CheckableImageButton iconView,
      @Nullable OnClickListener onClickListener,
      @Nullable OnLongClickListener onLongClickListener) {
    iconView.setOnClickListener(onClickListener);
    setIconClickable(iconView, onLongClickListener);
  }

  private static void setIconOnLongClickListener(
      @NonNull CheckableImageButton iconView, @Nullable OnLongClickListener onLongClickListener) {
    iconView.setOnLongClickListener(onLongClickListener);
    setIconClickable(iconView, onLongClickListener);
  }

  private static void setIconClickable(
      @NonNull CheckableImageButton iconView, @Nullable OnLongClickListener onLongClickListener) {
    boolean iconClickable = ViewCompat.hasOnClickListeners(iconView);
    boolean iconLongClickable = onLongClickListener != null;
    boolean iconFocusable = iconClickable || iconLongClickable;
    iconView.setFocusable(iconFocusable);
    iconView.setClickable(iconClickable);
    iconView.setPressable(iconClickable);
    iconView.setLongClickable(iconLongClickable);
    ViewCompat.setImportantForAccessibility(
        iconView,
        iconFocusable
            ? ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES
            : ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    if (editText != null) {
      Rect rect = tmpRect;
      DescendantOffsetUtils.getDescendantRect(this, editText, rect);
      updateBoxUnderlineBounds(rect);

      if (hintEnabled) {
        collapsingTextHelper.setExpandedTextSize(editText.getTextSize());
        final int editTextGravity = editText.getGravity();
        collapsingTextHelper.setCollapsedTextGravity(
            Gravity.TOP | (editTextGravity & ~Gravity.VERTICAL_GRAVITY_MASK));
        collapsingTextHelper.setExpandedTextGravity(editTextGravity);
        collapsingTextHelper.setCollapsedBounds(calculateCollapsedTextBounds(rect));
        collapsingTextHelper.setExpandedBounds(calculateExpandedTextBounds(rect));
        collapsingTextHelper.recalculate();

        // If the label should be collapsed, set the cutout bounds on the CutoutDrawable to make
        // sure it draws with a cutout in draw().
        if (cutoutEnabled() && !hintExpanded) {
          openCutout();
        }
      }
    }
  }

  private void updateBoxUnderlineBounds(@NonNull Rect bounds) {
    if (boxUnderline != null) {
      int top = bounds.bottom - boxStrokeWidthFocusedPx;
      boxUnderline.setBounds(bounds.left, top, bounds.right, bounds.bottom);
    }
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    super.draw(canvas);
    drawHint(canvas);
    drawBoxUnderline(canvas);
  }

  private void drawHint(@NonNull Canvas canvas) {
    if (hintEnabled) {
      collapsingTextHelper.draw(canvas);
    }
  }

  private void drawBoxUnderline(Canvas canvas) {
    if (boxUnderline != null) {
      // Draw using the current boxStrokeWidth.
      Rect underlineBounds = boxUnderline.getBounds();
      underlineBounds.top = underlineBounds.bottom - boxStrokeWidthPx;
      boxUnderline.draw(canvas);
    }
  }

  private void collapseHint(boolean animate) {
    if (animator != null && animator.isRunning()) {
      animator.cancel();
    }
    if (animate && hintAnimationEnabled) {
      animateToExpansionFraction(1f);
    } else {
      collapsingTextHelper.setExpansionFraction(1f);
    }
    hintExpanded = false;
    if (cutoutEnabled()) {
      openCutout();
    }
    updatePlaceholderText();

    updatePrefixTextVisibility();
    updateSuffixTextVisibility();
  }

  private boolean cutoutEnabled() {
    return hintEnabled && !TextUtils.isEmpty(hint) && boxBackground instanceof CutoutDrawable;
  }

  private void openCutout() {
    if (!cutoutEnabled()) {
      return;
    }
    final RectF cutoutBounds = tmpRectF;
    collapsingTextHelper.getCollapsedTextActualBounds(
        cutoutBounds, editText.getWidth(), editText.getGravity());
    applyCutoutPadding(cutoutBounds);
    // Offset the cutout bounds by the TextInputLayout's left and top paddings to ensure that the
    // cutout is inset relative to the TextInputLayout's bounds.
    cutoutBounds.offset(-getPaddingLeft(), -getPaddingTop());
    ((CutoutDrawable) boxBackground).setCutout(cutoutBounds);
  }

  private void closeCutout() {
    if (cutoutEnabled()) {
      ((CutoutDrawable) boxBackground).removeCutout();
    }
  }

  private void applyCutoutPadding(@NonNull RectF cutoutBounds) {
    cutoutBounds.left -= boxLabelCutoutPaddingPx;
    cutoutBounds.top -= boxLabelCutoutPaddingPx;
    cutoutBounds.right += boxLabelCutoutPaddingPx;
    cutoutBounds.bottom += boxLabelCutoutPaddingPx;
  }

  @VisibleForTesting
  boolean cutoutIsOpen() {
    return cutoutEnabled() && ((CutoutDrawable) boxBackground).hasCutout();
  }

  @Override
  protected void drawableStateChanged() {
    if (inDrawableStateChanged) {
      // Some of the calls below will update the drawable state of child views. Since we're
      // using addStatesFromChildren we can get into infinite recursion, hence we'll just
      // exit in this instance
      return;
    }

    inDrawableStateChanged = true;

    super.drawableStateChanged();

    final int[] state = getDrawableState();
    boolean changed = false;

    if (collapsingTextHelper != null) {
      changed |= collapsingTextHelper.setState(state);
    }

    // Drawable state has changed so see if we need to update the label
    if (editText != null) {
      updateLabelState(ViewCompat.isLaidOut(this) && isEnabled());
    }
    updateEditTextBackground();
    updateTextInputBoxState();

    if (changed) {
      invalidate();
    }

    inDrawableStateChanged = false;
  }

  void updateTextInputBoxState() {
    if (boxBackground == null || boxBackgroundMode == BOX_BACKGROUND_NONE) {
      return;
    }

    final boolean hasFocus = isFocused() || (editText != null && editText.hasFocus());
    final boolean isHovered = isHovered() || (editText != null && editText.isHovered());

    // Update the text box's stroke color based on the current state.
    if (!isEnabled()) {
      boxStrokeColor = disabledColor;
    } else if (indicatorViewController.errorShouldBeShown()) {
      if (strokeErrorColor != null) {
        updateStrokeErrorColor(hasFocus, isHovered);
      } else {
        boxStrokeColor = indicatorViewController.getErrorViewCurrentTextColor();
      }
    } else if (counterOverflowed && counterView != null) {
      if (strokeErrorColor != null) {
        updateStrokeErrorColor(hasFocus, isHovered);
      } else {
        boxStrokeColor = counterView.getCurrentTextColor();
      }
    } else if (hasFocus) {
      boxStrokeColor = focusedStrokeColor;
    } else if (isHovered) {
      boxStrokeColor = hoveredStrokeColor;
    } else {
      boxStrokeColor = defaultStrokeColor;
    }

    setErrorIconVisible(
        getErrorIconDrawable() != null
            && indicatorViewController.isErrorEnabled()
            && indicatorViewController.errorShouldBeShown());

    // Update icons tints
    refreshErrorIconDrawableState();
    refreshStartIconDrawableState();
    refreshEndIconDrawableState();

    if (getEndIconDelegate().shouldTintIconOnError()) {
      tintEndIconOnError(indicatorViewController.errorShouldBeShown());
    }

    // Update the text box's stroke width based on the current state.
    if (hasFocus && isEnabled()) {
      boxStrokeWidthPx = boxStrokeWidthFocusedPx;
    } else {
      boxStrokeWidthPx = boxStrokeWidthDefaultPx;
    }

    // Update the text box's background color based on the current state.
    if (boxBackgroundMode == BOX_BACKGROUND_FILLED) {
      if (!isEnabled()) {
        boxBackgroundColor = disabledFilledBackgroundColor;
      } else if (isHovered && !hasFocus) {
        boxBackgroundColor = hoveredFilledBackgroundColor;
      } else if (hasFocus) {
        boxBackgroundColor = focusedFilledBackgroundColor;
      } else {
        boxBackgroundColor = defaultFilledBackgroundColor;
      }
    }

    applyBoxAttributes();
  }

  private void updateStrokeErrorColor(boolean hasFocus, boolean isHovered) {
    int defaultStrokeErrorColor = strokeErrorColor.getDefaultColor();
    int hoveredStrokeErrorColor =
        strokeErrorColor.getColorForState(
            new int[] {android.R.attr.state_hovered, android.R.attr.state_enabled},
            defaultStrokeErrorColor);
    int focusedStrokeErrorColor =
        strokeErrorColor.getColorForState(
            new int[] {android.R.attr.state_activated, android.R.attr.state_enabled},
            defaultStrokeErrorColor);
    if (hasFocus) {
      boxStrokeColor = focusedStrokeErrorColor;
    } else if (isHovered) {
      boxStrokeColor = hoveredStrokeErrorColor;
    } else {
      boxStrokeColor = defaultStrokeErrorColor;
    }
  }

  private void setErrorIconVisible(boolean errorIconVisible) {
    errorIconView.setVisibility(errorIconVisible ? VISIBLE : GONE);
    endIconFrame.setVisibility(errorIconVisible ? GONE : VISIBLE);
    updateSuffixTextViewPadding();
    if (!hasEndIcon()) {
      updateDummyDrawables();
    }
  }

  private boolean isErrorIconVisible() {
    return errorIconView.getVisibility() == VISIBLE;
  }

  private void refreshIconDrawableState(
      CheckableImageButton iconView, ColorStateList colorStateList) {
    Drawable icon = iconView.getDrawable();
    if (iconView.getDrawable() == null || colorStateList == null || !colorStateList.isStateful()) {
      return;
    }

    int color =
        colorStateList.getColorForState(mergeIconState(iconView), colorStateList.getDefaultColor());

    icon = DrawableCompat.wrap(icon).mutate();
    DrawableCompat.setTintList(icon, ColorStateList.valueOf(color));
    iconView.setImageDrawable(icon);
  }

  private int[] mergeIconState(CheckableImageButton iconView) {
    int[] textInputStates = this.getDrawableState();
    int[] iconStates = iconView.getDrawableState();

    int index = textInputStates.length;
    int[] states = Arrays.copyOf(textInputStates, textInputStates.length + iconStates.length);

    System.arraycopy(iconStates, 0, states, index, iconStates.length);

    return states;
  }

  private void expandHint(boolean animate) {
    if (animator != null && animator.isRunning()) {
      animator.cancel();
    }
    if (animate && hintAnimationEnabled) {
      animateToExpansionFraction(0f);
    } else {
      collapsingTextHelper.setExpansionFraction(0f);
    }
    if (cutoutEnabled() && ((CutoutDrawable) boxBackground).hasCutout()) {
      closeCutout();
    }
    hintExpanded = true;
    hidePlaceholderText();

    updatePrefixTextVisibility();
    updateSuffixTextVisibility();
  }

  @VisibleForTesting
  void animateToExpansionFraction(final float target) {
    if (collapsingTextHelper.getExpansionFraction() == target) {
      return;
    }
    if (this.animator == null) {
      this.animator = new ValueAnimator();
      this.animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
      this.animator.setDuration(LABEL_SCALE_ANIMATION_DURATION);
      this.animator.addUpdateListener(
          new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animator) {
              collapsingTextHelper.setExpansionFraction((float) animator.getAnimatedValue());
            }
          });
    }
    this.animator.setFloatValues(collapsingTextHelper.getExpansionFraction(), target);
    this.animator.start();
  }

  @VisibleForTesting
  final boolean isHintExpanded() {
    return hintExpanded;
  }

  @VisibleForTesting
  final boolean isHelperTextDisplayed() {
    return indicatorViewController.helperTextIsDisplayed();
  }

  @VisibleForTesting
  final int getHintCurrentCollapsedTextColor() {
    return collapsingTextHelper.getCurrentCollapsedTextColor();
  }

  @VisibleForTesting
  final float getHintCollapsedTextHeight() {
    return collapsingTextHelper.getCollapsedTextHeight();
  }

  @VisibleForTesting
  final int getErrorTextCurrentColor() {
    return indicatorViewController.getErrorViewCurrentTextColor();
  }

  /**
   * An {@link AccessibilityDelegate} intended to be set on an {@link EditText} or {@link
   * TextInputEditText} with {@link
   * TextInputLayout#setTextInputAccessibilityDelegate(TextInputLayout.AccessibilityDelegate)} to
   * provide attributes for accessibility that are managed by {@link TextInputLayout}.
   */
  public static class AccessibilityDelegate extends AccessibilityDelegateCompat {
    private final TextInputLayout layout;

    public AccessibilityDelegate(@NonNull TextInputLayout layout) {
      this.layout = layout;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(
        @NonNull View host, @NonNull AccessibilityNodeInfoCompat info) {
      super.onInitializeAccessibilityNodeInfo(host, info);
      EditText editText = layout.getEditText();
      CharSequence inputText = (editText != null) ? editText.getText() : null;
      CharSequence hintText = layout.getHint();
      CharSequence errorText = layout.getError();
      CharSequence placeholderText = layout.getPlaceholderText();
      int maxCharLimit = layout.getCounterMaxLength();
      CharSequence counterOverflowDesc = layout.getCounterOverflowDescription();
      boolean showingText = !TextUtils.isEmpty(inputText);
      boolean hasHint = !TextUtils.isEmpty(hintText);
      boolean isHintCollapsed = !layout.isHintExpanded();
      boolean showingError = !TextUtils.isEmpty(errorText);
      boolean contentInvalid = showingError || !TextUtils.isEmpty(counterOverflowDesc);

      String hint = hasHint ? hintText.toString() : "";

      if (showingText) {
        info.setText(inputText);
      } else if (!TextUtils.isEmpty(hint)) {
        info.setText(hint);
        if (isHintCollapsed && placeholderText != null) {
          info.setText(hint + ", " + placeholderText);
        }
      } else if (placeholderText != null) {
        info.setText(placeholderText);
      }

      if (!TextUtils.isEmpty(hint)) {
        if (VERSION.SDK_INT >= 26) {
          info.setHintText(hint);
        } else {
          // Due to a TalkBack bug, setHintText has no effect in APIs < 26 so we append the hint to
          // the text announcement. The resulting announcement is the same as in APIs >= 26.
          String text = showingText ? (inputText + ", " + hint) : hint;
          info.setText(text);
        }
        info.setShowingHintText(!showingText);
      }

      // Announce when the character limit is reached.
      info.setMaxTextLength(
          (inputText != null && inputText.length() == maxCharLimit) ? maxCharLimit : -1);

      if (contentInvalid) {
        info.setError(showingError ? errorText : counterOverflowDesc);
      }

      if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1 && editText != null) {
        editText.setLabelFor(R.id.textinput_helper_text);
      }
    }
  }
}
