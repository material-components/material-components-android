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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.text.TextUtils;
import android.util.Pair;
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
import androidx.annotation.StringRes;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.timepicker.TimePickerView.OnDoubleTapListener;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashSet;
import java.util.Set;

/** A {@link Dialog} with a clock display and a clock face to choose the time. */
public final class MaterialTimePicker extends DialogFragment {

  private final Set<OnClickListener> positiveButtonListeners = new LinkedHashSet<>();
  private final Set<OnClickListener> negativeButtonListeners = new LinkedHashSet<>();
  private final Set<OnCancelListener> cancelListeners = new LinkedHashSet<>();
  private final Set<OnDismissListener> dismissListeners = new LinkedHashSet<>();

  private TimePickerView timePickerView;
  private LinearLayout textInputView;
  private ViewStub textInputStub;

  @Nullable private TimePickerClockPresenter timePickerClockPresenter;
  @Nullable private TimePickerTextInputPresenter timePickerTextInputPresenter;
  @Nullable private TimePickerPresenter activePresenter;

  @DrawableRes private int keyboardIcon;
  @DrawableRes private int clockIcon;

  private int titleResId = 0;
  private String titleText;

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

  private MaterialButton modeButton;

  @InputMode private int inputMode = INPUT_MODE_CLOCK;

  private TimeModel time;

  @NonNull
  private static MaterialTimePicker newInstance(@NonNull Builder options) {
    MaterialTimePicker fragment = new MaterialTimePicker();
    Bundle args = new Bundle();
    args.putParcelable(TIME_MODEL_EXTRA, options.time);
    args.putInt(INPUT_MODE_EXTRA, options.inputMode);
    args.putInt(TITLE_RES_EXTRA, options.titleTextResId);
    if (options.titleText != null) {
      args.putString(TITLE_TEXT_EXTRA, options.titleText.toString());
    }

    fragment.setArguments(args);
    return fragment;
  }

  @IntRange(from = 0, to = 60)
  public int getMinute() {
    return time.minute;
  }

  /** Returns the hour of day in the range [0, 23]. */
  @IntRange(from = 0, to = 23)
  public int getHour() {
    return time.hour % 24;
  }

  @InputMode
  public int getInputMode() {
    return inputMode;
  }

  @NonNull
  @Override
  public final Dialog onCreateDialog(@Nullable Bundle bundle) {
    TypedValue value = MaterialAttributes.resolve(requireContext(), R.attr.materialTimePickerTheme);
    Dialog dialog = new Dialog(requireContext(), value == null ? 0 : value.data);
    Context context = dialog.getContext();
    int surfaceColor =
        MaterialAttributes.resolveOrThrow(
            context, R.attr.colorSurface, MaterialTimePicker.class.getCanonicalName());

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

    a.recycle();

    background.initializeElevationOverlay(context);
    background.setFillColor(ColorStateList.valueOf(surfaceColor));
    Window window = dialog.getWindow();
    window.setBackgroundDrawable(background);
    window.requestFeature(Window.FEATURE_NO_TITLE);
    // On some Android APIs the dialog won't wrap content by default. Explicitly update here.
    window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

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
    bundle.putString(TITLE_TEXT_EXTRA, titleText);
  }

  private void restoreState(@Nullable Bundle bundle) {
    if (bundle == null) {
      return;
    }

    time = bundle.getParcelable(TIME_MODEL_EXTRA);
    if (time == null) {
      time = new TimeModel();
    }
    inputMode = bundle.getInt(INPUT_MODE_EXTRA, INPUT_MODE_CLOCK);
    titleResId = bundle.getInt(TITLE_RES_EXTRA, 0);
    titleText = bundle.getString(TITLE_TEXT_EXTRA);
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
    timePickerView.setOnDoubleTapListener(
        new OnDoubleTapListener() {
          @Override
          public void onDoubleTap() {
            inputMode = INPUT_MODE_KEYBOARD;
            updateInputMode(modeButton);
            timePickerTextInputPresenter.resetChecked();
          }
        });
    textInputStub = root.findViewById(R.id.material_textinput_timepicker);
    modeButton = root.findViewById(R.id.material_timepicker_mode_button);
    TextView headerTitle = root.findViewById(R.id.header_title);

    if (!TextUtils.isEmpty(titleText)) {
      headerTitle.setText(titleText);
    }

    if (titleResId != 0) {
      headerTitle.setText(titleResId);
    }

    updateInputMode(modeButton);
    Button okButton = root.findViewById(R.id.material_timepicker_ok_button);
    okButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            for (OnClickListener listener : positiveButtonListeners) {
              listener.onClick(v);
            }
            dismiss();
          }
        });

    Button cancelButton = root.findViewById(R.id.material_timepicker_cancel_button);
    cancelButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            for (OnClickListener listener : negativeButtonListeners) {
              listener.onClick(v);
            }
            dismiss();
          }
        });

    modeButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            inputMode = (inputMode == INPUT_MODE_CLOCK) ? INPUT_MODE_KEYBOARD : INPUT_MODE_CLOCK;
            updateInputMode(modeButton);
          }
        });

    return root;
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
    ViewGroup viewGroup = ((ViewGroup) getView());
    if (viewGroup != null) {
      viewGroup.removeAllViews();
    }
    super.onDismiss(dialogInterface);
  }

  private void updateInputMode(MaterialButton modeButton) {
    if (activePresenter != null) {
      activePresenter.hide();
    }

    activePresenter = initializeOrRetrieveActivePresenterForMode(inputMode);
    activePresenter.show();
    activePresenter.invalidate();
    Pair<Integer, Integer> buttonData = dataForMode(inputMode);
    modeButton.setIconResource(buttonData.first);
    modeButton.setContentDescription(getResources().getString(buttonData.second));
  }

  private TimePickerPresenter initializeOrRetrieveActivePresenterForMode(int mode) {

    if (mode == INPUT_MODE_CLOCK) {
      timePickerClockPresenter =
          timePickerClockPresenter == null
              ? new TimePickerClockPresenter(timePickerView, time)
              : timePickerClockPresenter;

      return timePickerClockPresenter;
    }

    if (timePickerTextInputPresenter == null) {
      textInputView = (LinearLayout) textInputStub.inflate();
      timePickerTextInputPresenter = new TimePickerTextInputPresenter(textInputView, time);
    }

    timePickerTextInputPresenter.clearCheck();

    return timePickerTextInputPresenter;
  }

  private Pair<Integer, Integer> dataForMode(@InputMode int mode) {
    switch (mode) {
      case INPUT_MODE_KEYBOARD:
        return new Pair<>(clockIcon, R.string.material_timepicker_clock_mode_description);
      case INPUT_MODE_CLOCK:
        return new Pair<>(keyboardIcon, R.string.material_timepicker_text_input_mode_description);
    }

    throw new IllegalArgumentException("no icon for mode: " + mode);
  }

  @Nullable
  TimePickerClockPresenter getTimePickerClockPresenter() {
    return timePickerClockPresenter;
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

  /** Used to create {@link MaterialTimePicker} instances. */
  public static final class Builder {

    private TimeModel time = new TimeModel();

    private int inputMode;
    private int titleTextResId = 0;
    private CharSequence titleText;

    /** Sets the input mode with which to start the time picker. */
    @NonNull
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
    public Builder setHour(@IntRange(from = 0, to = 23) int hour) {
      time.setHourOfDay(hour);
      return this;
    }

    /** Sets the minute with which to start the time picker. */
    @NonNull
    public Builder setMinute(@IntRange(from = 0, to = 60) int minute) {
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
    public Builder setTimeFormat(@TimeFormat int format) {
      int hour = time.hour;
      int minute = time.minute;
      time = new TimeModel(format);
      time.setMinute(minute);
      time.setHourOfDay(hour);
      return this;
    }

    /**
     * Sets the text used to guide the user at the top of the picker.
     */
    @NonNull
    public Builder setTitleText(@StringRes int titleTextResId) {
      this.titleTextResId = titleTextResId;
      return this;
    }

    /**
     * Sets the text used to guide the user at the top of the picker.
     */
    @NonNull
    public Builder setTitleText(@Nullable CharSequence charSequence) {
      this.titleText = charSequence;
      return this;
    }

    /** Creates a {@link MaterialTimePicker} with the provided options. */
    @NonNull
    public MaterialTimePicker build() {
      return MaterialTimePicker.newInstance(this);
    }
  }
}
