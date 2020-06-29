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
import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
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

/** A {@link Dialog} with a clock display and a clock face to choose the time. */
public final class TimePickerDialog extends DialogFragment {

  private static final int CLOCK_ICON = R.drawable.ic_clock_black_24dp;
  private static final int KEYBOARD_ICON = R.drawable.ic_keyboard_black_24dp;

  private TimePickerView timePickerView;
  private LinearLayout textInputView;

  @Nullable private TimePickerClockPresenter timePickerClockPresenter;
  @Nullable private TimePickerTextInputPresenter timePickerTextInputPresenter;
  @Nullable private TimePickerPresenter activePresenter;

  /** Values supported for the input type of the dialog. */
  @IntDef({KEYBOARD, CLOCK})
  @Retention(RetentionPolicy.SOURCE)
  public @interface InputMode {}

  public static final int KEYBOARD = 0;
  public static final int CLOCK = 1;

  static final String TIME_MODEL_EXTRA = "TIME_PICKER_TIME_MODEL";
  static final String MODE_EXTRA = "TIME_PICKER_INPUT_MODE";

  private MaterialButton modeButton;

  @InputMode private int mode = CLOCK;

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
    void onTimeSet(TimePickerDialog dialog);
  }

  private TimeModel time = new TimeModel();

  private OnTimeSetListener listener;

  @NonNull
  public static TimePickerDialog newInstance() {
    return new TimePickerDialog();
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

  public void setMode(@InputMode int mode) {
    this.mode = mode;
  }

  @NonNull
  @Override
  public final Dialog onCreateDialog(@Nullable Bundle bundle) {
    Dialog dialog = super.onCreateDialog(bundle);
    Context context = dialog.getContext();
    int surfaceColor =
        MaterialAttributes.resolveOrThrow(
            context, R.attr.colorSurface, TimePickerDialog.class.getCanonicalName());

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
    bundle.putInt(MODE_EXTRA, mode);
  }

  private void restoreState(@Nullable Bundle bundle) {
    if (bundle == null) {
      return;
    }

    time = bundle.getParcelable(TIME_MODEL_EXTRA);
    if (time == null) {
      time = new TimeModel();
    }
    mode = bundle.getInt(MODE_EXTRA, CLOCK);
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
        if (listener != null) {
          listener.onTimeSet(TimePickerDialog.this);
        }
        dismiss();
      }
    });

    MaterialButton cancelButton = root.findViewById(R.id.material_timepicker_cancel_button);
    cancelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });

    modeButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            mode = (mode == CLOCK) ? KEYBOARD : CLOCK;
            updateInputMode(modeButton);
          }
        });

    return root;
  }

  private void updateInputMode(MaterialButton modeButton) {
    modeButton.setChecked(false);
    if (activePresenter != null) {
      activePresenter.hide();
    }

    activePresenter = initializeOrRetrieveActivePresenterForMode(mode);
    activePresenter.show();
    activePresenter.invalidate();
    modeButton.setIconResource(iconForMode(mode));
  }

  private TimePickerPresenter initializeOrRetrieveActivePresenterForMode(int mode) {
    if (mode == CLOCK) {
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
      case KEYBOARD:
        return CLOCK_ICON;
      case CLOCK:
        return KEYBOARD_ICON;
      default:
        throw new IllegalArgumentException("no icon for mode: " + mode);
    }
  }

  public void setListener(@Nullable OnTimeSetListener listener) {
    this.listener = listener;
  }

  @Nullable
  TimePickerClockPresenter getTimePickerClockPresenter() {
    return timePickerClockPresenter;
  }
}
