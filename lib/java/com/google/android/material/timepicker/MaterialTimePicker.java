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
import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.shape.MaterialShapeDrawable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashSet;

/** A {@link Dialog} with a clock display and a clock face to choose the time. */
public final class MaterialTimePicker extends DialogFragment {

  private static final int CLOCK_ICON = R.drawable.ic_clock_black_24dp;
  private static final int KEYBOARD_ICON = R.drawable.ic_keyboard_black_24dp;

  private TimePickerView timePickerView;
  private LinearLayout textInputView;

  @Nullable private TimePickerClockPresenter timePickerClockPresenter;
  @Nullable private TimePickerTextInputPresenter timePickerTextInputPresenter;
  @Nullable private TimePickerPresenter activePresenter;

  /** Values supported for the input type of the dialog. */
  @IntDef({INPUT_MODE_CLOCK, INPUT_MODE_KEYBOARD})
  @Retention(RetentionPolicy.SOURCE)
  @interface InputMode {}

  public static final int INPUT_MODE_CLOCK = 0;
  public static final int INPUT_MODE_KEYBOARD = 1;

  static final String TIME_MODEL_EXTRA = "TIME_PICKER_TIME_MODEL";
  static final String INPUT_MODE_EXTRA = "TIME_PICKER_INPUT_MODE";

  private MaterialButton modeButton;

  @InputMode private int inputMode = INPUT_MODE_CLOCK;

  /**
   * The callback interface used to indicate the user is done filling in
   * the time (e.g. they clicked on the 'OK' button).
   */
  public interface OnTimeSetListener {

    /** **
     * Called when the user is done setting a new time and the dialog has
     * closed.
     *
     * <p> use {@link #getHour()}, {@link #getMinute()} to get the selection.
     *
     * @param dialog the dialog associated with this listener
     */
    void onTimeSet(MaterialTimePicker dialog);
  }

  private TimeModel time = new TimeModel();

  @NonNull
  public static MaterialTimePicker newInstance() {
    return new MaterialTimePicker();
  }

  public void setHour(int hour) {
    time.setHourOfDay(hour);
  }

  public void setMinute(int minute) {
    time.setMinute(minute);
  }

  public int getMinute() {
    return time.minute;
  }

  public int getHour() {
    return time.hour % 24;
  }

  public void setTimeFormat(@TimeFormat int format) {
    time = new TimeModel(format);
  }

  public void setInputMode(@InputMode int inputMode) {
    this.inputMode = inputMode;
  }

  @InputMode
  public int getInputMode() {
    return inputMode;
  }
  
  private final LinkedHashSet<OnTimeSetListener>
      onPositiveButtonClickListeners = new LinkedHashSet<>();
  private final LinkedHashSet<View.OnClickListener> onNegativeButtonClickListeners =
      new LinkedHashSet<>();
  private final LinkedHashSet<DialogInterface.OnCancelListener> onCancelListeners =
      new LinkedHashSet<>();
  private final LinkedHashSet<DialogInterface.OnDismissListener> onDismissListeners =
      new LinkedHashSet<>();

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
            0,
            R.style.Widget_MaterialComponents_TimePicker);

    background.initializeElevationOverlay(context);
    background.setFillColor(ColorStateList.valueOf(surfaceColor));
    Window window = dialog.getWindow();
    window.setBackgroundDrawable(background);
    window.requestFeature(Window.FEATURE_NO_TITLE);
    // On some Android APIs the dialog won't wrap content by default. Explicitly update here.
    window.setLayout(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);

    return dialog;
  }

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    restoreState(bundle);
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle bundle) {
    super.onSaveInstanceState(bundle);
    bundle.putParcelable(TIME_MODEL_EXTRA, time);
    bundle.putInt(INPUT_MODE_EXTRA, inputMode);
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
    textInputView = root.findViewById(R.id.material_textinput_timepicker);
    modeButton = root.findViewById(R.id.material_timepicker_mode_button);
    updateInputMode(modeButton);
    MaterialButton okButton = root.findViewById(R.id.material_timepicker_ok_button);
    okButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
         for (OnTimeSetListener listener :
            onPositiveButtonClickListeners) {
          if (listener != null) {
            listener.onTimeSet(MaterialTimePicker.this);
          }
        }
        dismiss();
      }
    });

    MaterialButton cancelButton = root.findViewById(R.id.material_timepicker_cancel_button);
    cancelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        for (View.OnClickListener listener : onNegativeButtonClickListeners) {
          if (listener != null) {
            listener.onClick(v);
          }
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
    for (DialogInterface.OnCancelListener listener : onCancelListeners) {
      if (listener != null) {
        listener.onCancel(dialogInterface);
      }
    }
    super.onCancel(dialogInterface);
  }

  @Override
  public final void onDismiss(@NonNull DialogInterface dialogInterface) {
    for (DialogInterface.OnDismissListener listener : onDismissListeners) {
      if (listener != null) {
        listener.onDismiss(dialogInterface);
      }
    }
    ViewGroup viewGroup = ((ViewGroup) getView());
    if (viewGroup != null) {
      viewGroup.removeAllViews();
    }
    super.onDismiss(dialogInterface);
  }

  private void updateInputMode(MaterialButton modeButton) {
    modeButton.setChecked(false);
    if (activePresenter != null) {
      activePresenter.hide();
    }

    activePresenter = initializeOrRetrieveActivePresenterForMode(inputMode);
    activePresenter.show();
    activePresenter.invalidate();
    modeButton.setIconResource(iconForMode(inputMode));
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
      timePickerTextInputPresenter = new TimePickerTextInputPresenter(textInputView, time);
    }

    return timePickerTextInputPresenter;
  }

  @DrawableRes
  private static int iconForMode(@InputMode int mode) {
    switch (mode) {
      case INPUT_MODE_KEYBOARD:
        return CLOCK_ICON;
      case INPUT_MODE_CLOCK:
        return KEYBOARD_ICON;
      default:
        throw new IllegalArgumentException("no icon for mode: " + mode);
    }
  }
  
  /**
   * @deprecated use {@link  MaterialTimePicker#addOnPositiveButtonClickListener}
   */
  @Deprecated
  public void setListener(@Nullable OnTimeSetListener listener) {
    addOnPositiveButtonClickListener(listener);
  }

  @Nullable
  TimePickerClockPresenter getTimePickerClockPresenter() {
    return timePickerClockPresenter;
  }
  
  /** The supplied listener is called when the user confirms a valid selection. */
  public boolean addOnPositiveButtonClickListener(
      OnTimeSetListener onPositiveButtonClickListener) {
    return onPositiveButtonClickListeners.add(onPositiveButtonClickListener);
  }

  /**
   * Removes a listener previously added via {@link
   * MaterialTimePicker#addOnPositiveButtonClickListener}.
   */
  public boolean removeOnPositiveButtonClickListener(
      OnTimeSetListener onPositiveButtonClickListener) {
    return onPositiveButtonClickListeners.remove(onPositiveButtonClickListener);
  }

  /**
   * Removes all listeners added via {@link MaterialTimePicker#addOnPositiveButtonClickListener}.
   */
  public void clearOnPositiveButtonClickListeners() {
    onPositiveButtonClickListeners.clear();
  }

  /** The supplied listener is called when the user clicks the cancel button. */
  public boolean addOnNegativeButtonClickListener(
      View.OnClickListener onNegativeButtonClickListener) {
    return onNegativeButtonClickListeners.add(onNegativeButtonClickListener);
  }

  /**
   * Removes a listener previously added via {@link
   * MaterialTimePicker#addOnNegativeButtonClickListener}.
   */
  public boolean removeOnNegativeButtonClickListener(
      View.OnClickListener onNegativeButtonClickListener) {
    return onNegativeButtonClickListeners.remove(onNegativeButtonClickListener);
  }

  /**
   * Removes all listeners added via {@link MaterialTimePicker#addOnNegativeButtonClickListener}.
   */
  public void clearOnNegativeButtonClickListeners() {
    onNegativeButtonClickListeners.clear();
  }

  /**
   * The supplied listener is called when the user cancels the picker via back button or a touch
   * outside the view. It is not called when the user clicks the cancel button. To add a listener
   * for use when the user clicks the cancel button, use {@link
   * MaterialTimePicker#addOnNegativeButtonClickListener}.
   */
  public boolean addOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
    return onCancelListeners.add(onCancelListener);
  }

  /** Removes a listener previously added via {@link MaterialTimePicker#addOnCancelListener}. */
  public boolean removeOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
    return onCancelListeners.remove(onCancelListener);
  }

  /** Removes all listeners added via {@link MaterialTimePicker#addOnCancelListener}. */
  public void clearOnCancelListeners() {
    onCancelListeners.clear();
  }

  /**
   * The supplied listener is called whenever the DialogFragment is dismissed, no matter how it is
   * dismissed.
   */
  public boolean addOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
    return onDismissListeners.add(onDismissListener);
  }

  /** Removes a listener previously added via {@link MaterialTimePicker#addOnDismissListener}. */
  public boolean removeOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
    return onDismissListeners.remove(onDismissListener);
  }

  /** Removes all listeners added via {@link MaterialTimePicker#addOnDismissListener}. */
  public void clearOnDismissListeners() {
    onDismissListeners.clear();
  }
}
