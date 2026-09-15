/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.timepicker;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.timepicker.TimeFormat.CLOCK_24H;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.TooltipCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.timepicker.TimePickerView.OnDoubleTapListener;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A {@link Dialog} with a clock display and a clock face to choose the time.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/TimePicker.md">component
 * developer guidance</a> and <a href="https://material.io/components/time-pickers/overview">design
 * guidelines</a>.
 */
public final class MaterialTimePicker extends DialogFragment implements OnDoubleTapListener {

  private final Set<OnClickListener> positiveButtonListeners = new LinkedHashSet<>();
  private final Set<OnClickListener> negativeButtonListeners = new LinkedHashSet<>();
  private final Set<OnCancelListener> cancelListeners = new LinkedHashSet<>();
  private final Set<OnDismissListener> dismissListeners = new LinkedHashSet<>();

  private TimePickerView timePickerView;
  private ViewStub textInputStub;

  @Nullable private TimePickerClockPresenter timePickerClockPresenter;
  @Nullable private TimePickerTextInputPresenter timePickerTextInputPresenter;
  @Nullable private TimePickerPresenter activePresenter;

  @DrawableRes private int keyboardIcon;
  @DrawableRes private int clockIcon;

  @StringRes private int titleResId = 0;
  private CharSequence titleText;
  @StringRes private int positiveButtonTextResId = 0;
  private CharSequence positiveButtonText;
  @StringRes private int negativeButtonTextResId = 0;
  private CharSequence negativeButtonText;

  /** Values supported for the input type of the dialog. */
  @IntDef({INPUT_MODE_CLOCK, INPUT_MODE_KEYBOARD})
  @Retention(RetentionPolicy.SOURCE)
  @interface InputMode {}

  public static final int INPUT_MODE_CLOCK = 0;
  public static final int INPUT_MODE_KEYBOARD = 1;

  static final String TIME_MODEL_EXTRA = "TIME_PICKER_TIME_MODEL";
  static final String INPUT_MODE_EXTRA = "TIME_PICKER_INPUT_MODE";
  static final String TITLE_RES_EXTRA = "TIME_PICKER_TITLE_RES";
  static final String TITLE_TEXT_EXTRA = "TIME_PICKER_TITLE_TEXT";
  static final String POSITIVE_BUTTON_TEXT_RES_EXTRA = "TIME_PICKER_POSITIVE_BUTTON_TEXT_RES";
  static final String POSITIVE_BUTTON_TEXT_EXTRA = "TIME_PICKER_POSITIVE_BUTTON_TEXT";
  static final String NEGATIVE_BUTTON_TEXT_RES_EXTRA = "TIME_PICKER_NEGATIVE_BUTTON_TEXT_RES";
  static final String NEGATIVE_BUTTON_TEXT_EXTRA = "TIME_PICKER_NEGATIVE_BUTTON_TEXT";
  static final String OVERRIDE_THEME_RES_ID = "TIME_PICKER_OVERRIDE_THEME_RES_ID";

  private MaterialButton modeButton;
  private Button okButton;
  private Button cancelButton;

  @InputMode private int inputMode = INPUT_MODE_CLOCK;

  private TimeModel time;

  private int overrideThemeResId = 0;

  @NonNull
  private static MaterialTimePicker newInstance(@NonNull Builder options) {
    MaterialTimePicker fragment = new MaterialTimePicker();
    Bundle args = new Bundle();
    args.putParcelable(TIME_MODEL_EXTRA, options.time);
    if (options.inputMode != null) {
      args.putInt(INPUT_MODE_EXTRA, options.inputMode);
    }
    args.putInt(TITLE_RES_EXTRA, options.titleTextResId);
    if (options.titleText != null) {
      args.putCharSequence(TITLE_TEXT_EXTRA, options.titleText);
    }
    args.putInt(POSITIVE_BUTTON_TEXT_RES_EXTRA, options.positiveButtonTextResId);
    if (options.positiveButtonText != null) {
      args.putCharSequence(POSITIVE_BUTTON_TEXT_EXTRA, options.positiveButtonText);
    }
    args.putInt(NEGATIVE_BUTTON_TEXT_RES_EXTRA, options.negativeButtonTextResId);
    if (options.negativeButtonText != null) {
      args.putCharSequence(NEGATIVE_BUTTON_TEXT_EXTRA, options.negativeButtonText);
    }
    args.putInt(OVERRIDE_THEME_RES_ID, options.overrideThemeResId);

    fragment.setArguments(args);
    return fragment;
  }

  /** Returns the minute in the range [0, 59]. */
  @IntRange(from = 0, to = 59)
  public int getMinute() {
    return time.minute;
  }

  /** Sets the minute in the range [0, 59]. */
  public void setMinute(@IntRange(from = 0, to = 59) int minute) {
    time.setMinute(minute);
    if (activePresenter != null) {
      activePresenter.invalidate();
    }
  }

  /** Returns the hour of day in the range [0, 23]. */
  @IntRange(from = 0, to = 23)
  public int getHour() {
    return time.hour % 24;
  }

  /** Sets the hour of day in the range [0, 23]. */
  public void setHour(@IntRange(from = 0, to = 23) int hour) {
    time.setHourOfDay(hour);
    if (activePresenter != null) {
      activePresenter.invalidate();
    }
  }

  @InputMode
  public int getInputMode() {
    return inputMode;
  }

  @NonNull
  @Override
  public final Dialog onCreateDialog(@Nullable Bundle bundle) {
    Dialog dialog = new Dialog(requireContext(), getThemeResId());
    Context context = dialog.getContext();

    MaterialShapeDrawable background =
        new MaterialShapeDrawable(
            context,
            null,
            R.attr.materialTimePickerStyle,
            R.style.Widget_MaterialComponents_TimePicker);

    TypedArray a =
        context.obtainStyledAttributes(
            null,
            R.styleable.MaterialTimePicker,
            R.attr.materialTimePickerStyle,
            R.style.Widget_MaterialComponents_TimePicker);

    clockIcon = a.getResourceId(R.styleable.MaterialTimePicker_clockIcon, 0);
    keyboardIcon = a.getResourceId(R.styleable.MaterialTimePicker_keyboardIcon, 0);
    int backgroundColor = a.getColor(R.styleable.MaterialTimePicker_backgroundTint, 0);

    a.recycle();

    background.initializeElevationOverlay(context);
    background.setFillColor(ColorStateList.valueOf(backgroundColor));
    Window window = dialog.getWindow();
    window.setBackgroundDrawable(background);
    window.requestFeature(Window.FEATURE_NO_TITLE);
    // On some Android APIs the dialog won't wrap content by default. Explicitly update here.
    window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    // This has to be done after requestFeature() is called on API <= 23.
    background.setElevation(window.getDecorView().getElevation());

    return dialog;
  }

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    restoreState(bundle == null ? getArguments() : bundle);
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle bundle) {
    super.onSaveInstanceState(bundle);
    bundle.putParcelable(TIME_MODEL_EXTRA, time);
    bundle.putInt(INPUT_MODE_EXTRA, inputMode);
    bundle.putInt(TITLE_RES_EXTRA, titleResId);
    bundle.putCharSequence(TITLE_TEXT_EXTRA, titleText);
    bundle.putInt(POSITIVE_BUTTON_TEXT_RES_EXTRA, positiveButtonTextResId);
    bundle.putCharSequence(POSITIVE_BUTTON_TEXT_EXTRA, positiveButtonText);
    bundle.putInt(NEGATIVE_BUTTON_TEXT_RES_EXTRA, negativeButtonTextResId);
    bundle.putCharSequence(NEGATIVE_BUTTON_TEXT_EXTRA, negativeButtonText);
    bundle.putInt(OVERRIDE_THEME_RES_ID, overrideThemeResId);
  }

  private void restoreState(@Nullable Bundle bundle) {
    if (bundle == null) {
      return;
    }

    time = bundle.getParcelable(TIME_MODEL_EXTRA);
    if (time == null) {
      time = new TimeModel();
    }

    boolean forceKeyboardInputMode =
        getResources().getBoolean(R.bool.timepicker_force_input_mode_keyboard);
    int defaultInputMode =
        forceKeyboardInputMode || time.format == CLOCK_24H ? INPUT_MODE_KEYBOARD : INPUT_MODE_CLOCK;
    inputMode = bundle.getInt(INPUT_MODE_EXTRA, defaultInputMode);
    titleResId = bundle.getInt(TITLE_RES_EXTRA, 0);
    titleText = bundle.getCharSequence(TITLE_TEXT_EXTRA);
    positiveButtonTextResId = bundle.getInt(POSITIVE_BUTTON_TEXT_RES_EXTRA, 0);
    positiveButtonText = bundle.getCharSequence(POSITIVE_BUTTON_TEXT_EXTRA);
    negativeButtonTextResId = bundle.getInt(NEGATIVE_BUTTON_TEXT_RES_EXTRA, 0);
    negativeButtonText = bundle.getCharSequence(NEGATIVE_BUTTON_TEXT_EXTRA);
    overrideThemeResId = bundle.getInt(OVERRIDE_THEME_RES_ID, 0);
  }

  @NonNull
  @Override
  public final View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    ViewGroup root =
        (ViewGroup) layoutInflater.inflate(R.layout.material_timepicker_dialog, viewGroup);
    timePickerView = root.findViewById(R.id.material_timepicker_view);
    timePickerView.setOnDoubleTapListener(this);
    textInputStub = root.findViewById(R.id.material_textinput_timepicker);
    modeButton = root.findViewById(R.id.material_timepicker_mode_button);
    okButton = root.findViewById(R.id.material_timepicker_ok_button);
    cancelButton = root.findViewById(R.id.material_timepicker_cancel_button);
    TextView headerTitle = root.findViewById(R.id.header_title);

    if (titleResId != 0) {
      headerTitle.setText(titleResId);
    } else if (!TextUtils.isEmpty(titleText)) {
      headerTitle.setText(titleText);
    }

    updateInputMode(modeButton);
    okButton.setOnClickListener(
        v -> {
          if (activePresenter instanceof TimePickerTextInputPresenter) {
            TimePickerTextInputPresenter presenter = (TimePickerTextInputPresenter) activePresenter;
            if (presenter.hasError()) {
              presenter.vibrateAndMaybeBeep(root);
              presenter.accessibilityFocusOnError();
              return;
            }
          }
          for (OnClickListener listener : positiveButtonListeners) {
            listener.onClick(v);
          }
          dismiss();
        });
    if (positiveButtonTextResId != 0) {
      okButton.setText(positiveButtonTextResId);
    } else if (!TextUtils.isEmpty(positiveButtonText)) {
      okButton.setText(positiveButtonText);
    }

    cancelButton.setOnClickListener(
        v -> {
          for (OnClickListener listener : negativeButtonListeners) {
            listener.onClick(v);
          }
          dismiss();
        });
    if (negativeButtonTextResId != 0) {
      cancelButton.setText(negativeButtonTextResId);
    } else if (!TextUtils.isEmpty(negativeButtonText)) {
      cancelButton.setText(negativeButtonText);
    }

    updateCancelButtonVisibility();

    modeButton.setOnClickListener(
        v -> {
          inputMode = (inputMode == INPUT_MODE_CLOCK) ? INPUT_MODE_KEYBOARD : INPUT_MODE_CLOCK;
          updateInputMode(modeButton);
        });

    return root;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    // TODO(b/246354286): Investigate issue with keyboard not showing on Android 12+
    if (activePresenter instanceof TimePickerTextInputPresenter) {
      view.postDelayed(
          () -> {
            if (activePresenter instanceof TimePickerTextInputPresenter) {
              ((TimePickerTextInputPresenter) activePresenter).resetChecked();
            }
          },
          100);
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    activePresenter = null;
    timePickerClockPresenter = null;
    timePickerTextInputPresenter = null;
    if (timePickerView != null) {
      timePickerView.setOnDoubleTapListener(null);
      timePickerView = null;
    }
  }

  @Override
  public final void onCancel(@NonNull DialogInterface dialogInterface) {
    for (OnCancelListener listener : cancelListeners) {
      listener.onCancel(dialogInterface);
    }
    super.onCancel(dialogInterface);
  }

  @Override
  public final void onDismiss(@NonNull DialogInterface dialogInterface) {
    for (OnDismissListener listener : dismissListeners) {
      listener.onDismiss(dialogInterface);
    }

    super.onDismiss(dialogInterface);
  }

  @Override
  public void setCancelable(boolean cancelable) {
    super.setCancelable(cancelable);
    updateCancelButtonVisibility();
  }

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @Override
  public void onDoubleTap() {
    inputMode = INPUT_MODE_KEYBOARD;
    updateInputMode(modeButton);
    timePickerTextInputPresenter.resetChecked();
  }

  private void updateInputMode(MaterialButton modeButton) {
    if (modeButton == null || timePickerView == null || textInputStub == null) {
      return;
    }

    if (activePresenter != null) {
      activePresenter.hide();
    }

    activePresenter =
        initializeOrRetrieveActivePresenterForMode(inputMode, timePickerView, textInputStub);
    activePresenter.show();
    activePresenter.invalidate();
    ModeButtonData modeButtonData = getModeButtonData(inputMode);
    modeButton.setIconResource(modeButtonData.iconResId);
    modeButton.setContentDescription(
        getResources().getString(modeButtonData.contentDescriptionResId));
    TooltipCompat.setTooltipText(
        modeButton, getResources().getString(modeButtonData.tooltipTextResId));
    modeButton.sendAccessibilityEvent(
        AccessibilityEventCompat.CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION);
  }

  private void updateCancelButtonVisibility() {
    if (cancelButton != null) {
      cancelButton.setVisibility(isCancelable() ? View.VISIBLE : View.GONE);
    }
  }

  private TimePickerPresenter initializeOrRetrieveActivePresenterForMode(
      int mode, @NonNull TimePickerView timePickerView, @NonNull ViewStub textInputStub) {
    if (mode == INPUT_MODE_CLOCK) {
      timePickerClockPresenter =
          timePickerClockPresenter == null
              ? new TimePickerClockPresenter(timePickerView, time)
              : timePickerClockPresenter;

      return timePickerClockPresenter;
    }

    if (timePickerTextInputPresenter == null) {
      LinearLayout textInputView = (LinearLayout) textInputStub.inflate();
      timePickerTextInputPresenter = new TimePickerTextInputPresenter(textInputView, time);
    }

    timePickerTextInputPresenter.clearError();
    timePickerTextInputPresenter.clearCheck();

    return timePickerTextInputPresenter;
  }

  private ModeButtonData getModeButtonData(@InputMode int mode) {
    switch (mode) {
      case INPUT_MODE_KEYBOARD:
        return new ModeButtonData(
            clockIcon,
            R.string.material_timepicker_clock_mode_description,
            R.string.material_timepicker_clock_mode_tooltip);
      case INPUT_MODE_CLOCK:
        return new ModeButtonData(
            keyboardIcon,
            R.string.material_timepicker_text_input_mode_description,
            R.string.material_timepicker_text_input_mode_tooltip);
      default:
        throw new IllegalArgumentException("no button data for mode: " + mode);
    }
  }

  @Nullable
  TimePickerClockPresenter getTimePickerClockPresenter() {
    return timePickerClockPresenter;
  }

  @VisibleForTesting
  void setActivePresenter(@Nullable TimePickerPresenter presenter) {
    activePresenter = presenter;
  }

  /** The supplied listener is called when the user confirms a valid selection. */
  public boolean addOnPositiveButtonClickListener(@NonNull OnClickListener listener) {
    return positiveButtonListeners.add(listener);
  }

  /**
   * Removes a listener previously added via {@link
   * MaterialTimePicker#addOnPositiveButtonClickListener(OnClickListener)}.
   */
  public boolean removeOnPositiveButtonClickListener(@NonNull OnClickListener listener) {
    return positiveButtonListeners.remove(listener);
  }

  /**
   * Removes all listeners added via {@link
   * MaterialTimePicker#addOnPositiveButtonClickListener(OnClickListener)}.
   */
  public void clearOnPositiveButtonClickListeners() {
    positiveButtonListeners.clear();
  }

  /** The supplied listener is called when the user clicks the cancel button. */
  public boolean addOnNegativeButtonClickListener(@NonNull OnClickListener listener) {
    return negativeButtonListeners.add(listener);
  }

  /**
   * Removes a listener previously added via {@link
   * MaterialTimePicker#addOnNegativeButtonClickListener(OnClickListener)}.
   */
  public boolean removeOnNegativeButtonClickListener(@NonNull OnClickListener listener) {
    return negativeButtonListeners.remove(listener);
  }

  /**
   * Removes all listeners added via {@link
   * MaterialTimePicker#addOnNegativeButtonClickListener(OnClickListener)}.
   */
  public void clearOnNegativeButtonClickListeners() {
    negativeButtonListeners.clear();
  }

  /**
   * The supplied listener is called when the user cancels the picker via back button or a touch
   * outside the view.
   *
   * <p>It is not called when the user clicks the cancel button. To add a listener for use when the
   * user clicks the cancel button, use {@link
   * MaterialTimePicker#addOnNegativeButtonClickListener(OnClickListener)}.
   */
  public boolean addOnCancelListener(@NonNull OnCancelListener listener) {
    return cancelListeners.add(listener);
  }

  /**
   * Removes a listener previously added via {@link
   * MaterialTimePicker#addOnCancelListener(OnCancelListener)}.
   */
  public boolean removeOnCancelListener(@NonNull OnCancelListener listener) {
    return cancelListeners.remove(listener);
  }

  /**
   * Removes all listeners added via {@link
   * MaterialTimePicker#addOnCancelListener(OnCancelListener)}.
   */
  public void clearOnCancelListeners() {
    cancelListeners.clear();
  }

  /**
   * The supplied listener is called whenever the DialogFragment is dismissed, no matter how it is
   * dismissed.
   */
  public boolean addOnDismissListener(@NonNull OnDismissListener listener) {
    return dismissListeners.add(listener);
  }

  /**
   * Removes a listener previously added via {@link
   * MaterialTimePicker#addOnDismissListener(OnDismissListener)}.
   */
  public boolean removeOnDismissListener(@NonNull OnDismissListener listener) {
    return dismissListeners.remove(listener);
  }

  /**
   * Removes all listeners added via {@link
   * MaterialTimePicker#addOnDismissListener(OnDismissListener)}.
   */
  public void clearOnDismissListeners() {
    dismissListeners.clear();
  }

  private int getThemeResId() {
    if (overrideThemeResId != 0) {
      return overrideThemeResId;
    }
    TypedValue value = MaterialAttributes.resolve(requireContext(), R.attr.materialTimePickerTheme);
    return value == null ? 0 : value.data;
  }

  /** Used to create {@link MaterialTimePicker} instances. */
  public static final class Builder {

    private TimeModel time = new TimeModel();

    @Nullable private Integer inputMode;
    @StringRes private int titleTextResId = 0;
    private CharSequence titleText;
    @StringRes private int positiveButtonTextResId = 0;
    private CharSequence positiveButtonText;
    @StringRes private int negativeButtonTextResId = 0;
    private CharSequence negativeButtonText;
    private int overrideThemeResId = 0;

    /** Sets the input mode with which to start the time picker. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setInputMode(@InputMode int inputMode) {
      this.inputMode = inputMode;
      return this;
    }

    /**
     * Sets the hour with which to start the time picker.
     *
     * @param hour The hour value is independent of the time format ({@link #setTimeFormat(int)}),
     *     and should always be a number in the [0, 23] range.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setHour(@IntRange(from = 0, to = 23) int hour) {
      time.setHourOfDay(hour);
      return this;
    }

    /** Sets the minute with which to start the time picker. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setMinute(@IntRange(from = 0, to = 59) int minute) {
      time.setMinute(minute);
      return this;
    }

    /**
     * Sets the time format for the time picker.
     *
     * @param format Either {@code CLOCK_12H} 12 hour format with an AM/PM toggle or {@code
     *     CLOCK_24} 24 hour format without toggle.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setTimeFormat(@TimeFormat int format) {
      int hour = time.hour;
      int minute = time.minute;
      time = new TimeModel(format);
      time.setMinute(minute);
      time.setHourOfDay(hour);
      return this;
    }

    /** Sets the text used to guide the user at the top of the picker. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setTitleText(@StringRes int titleTextResId) {
      this.titleTextResId = titleTextResId;
      return this;
    }

    /** Sets the text used to guide the user at the top of the picker. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setTitleText(@Nullable CharSequence charSequence) {
      this.titleText = charSequence;
      return this;
    }

    /** Sets the text used in the positive action button. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setPositiveButtonText(@StringRes int positiveButtonTextResId) {
      this.positiveButtonTextResId = positiveButtonTextResId;
      return this;
    }

    /** Sets the text used in the positive action button. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setPositiveButtonText(@Nullable CharSequence positiveButtonText) {
      this.positiveButtonText = positiveButtonText;
      return this;
    }

    /** Sets the text used in the negative action button. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setNegativeButtonText(@StringRes int negativeButtonTextResId) {
      this.negativeButtonTextResId = negativeButtonTextResId;
      return this;
    }

    /** Sets the text used in the negative action button. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setNegativeButtonText(@Nullable CharSequence negativeButtonText) {
      this.negativeButtonText = negativeButtonText;
      return this;
    }

    /** Sets the theme for the time picker. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setTheme(@StyleRes int themeResId) {
      this.overrideThemeResId = themeResId;
      return this;
    }

    /** Creates a {@link MaterialTimePicker} with the provided options. */
    @NonNull
    public MaterialTimePicker build() {
      return MaterialTimePicker.newInstance(this);
    }
  }

  private static final class ModeButtonData {
    @DrawableRes final int iconResId;
    @StringRes final int contentDescriptionResId;
    @StringRes final int tooltipTextResId;

    ModeButtonData(
        @DrawableRes int iconResId,
        @StringRes int contentDescriptionResId,
        @StringRes int tooltipTextResId) {
      this.iconResId = iconResId;
      this.contentDescriptionResId = contentDescriptionResId;
      this.tooltipTextResId = tooltipTextResId;
    }
  }
}
