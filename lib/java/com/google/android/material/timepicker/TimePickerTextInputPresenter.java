/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.material.timepicker;

import com.google.android.material.R;

import static android.view.View.GONE;
import static androidx.core.content.ContextCompat.getSystemService;
import static com.google.android.material.timepicker.TimeFormat.CLOCK_12H;
import static java.util.Calendar.AM;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.PM;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.appcompat.content.res.AppCompatResources;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.button.MaterialButtonToggleGroup.OnButtonCheckedListener;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.TextWatcherAdapter;
import com.google.android.material.timepicker.TimePickerView.OnSelectionChange;
import java.lang.reflect.Field;
import java.util.Locale;

class TimePickerTextInputPresenter implements OnSelectionChange, TimePickerPresenter {

  private final LinearLayout timePickerView;
  private final TimeModel time;
  private final TextWatcher minuteTextWatcher =
      new TextWatcherAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
          try {
            if (TextUtils.isEmpty(s)) {
              time.setMinute(0);
              return;
            }
            int minute = Integer.parseInt(s.toString());
            time.setMinute(minute);
          } catch (NumberFormatException ok) {
            // ignore invalid input
          }
        }
      };

  private final TextWatcher hourTextWatcher =
      new TextWatcherAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
          try {
            if (TextUtils.isEmpty(s)) {
              time.setHour(0);
              return;
            }
            int hour = Integer.parseInt(s.toString());
            time.setHour(hour);
          } catch (NumberFormatException ok) {
            // ignore invalid input
          }
        }
      };
  private final ChipTextInputComboView minuteTextInput;
  private final ChipTextInputComboView hourTextInput;
  private final TimePickerTextInputKeyController controller;
  private final EditText hourEditText;
  private final EditText minuteEditText;
  private MaterialButtonToggleGroup toggle;

  public TimePickerTextInputPresenter(LinearLayout timePickerView, TimeModel time) {
    this.timePickerView = timePickerView;
    this.time = time;
    Resources res = timePickerView.getResources();
    minuteTextInput = timePickerView.findViewById(R.id.material_minute_text_input);
    hourTextInput = timePickerView.findViewById(R.id.material_hour_text_input);
    TextView minuteLabel = minuteTextInput.findViewById(R.id.material_label);
    TextView hourLabel = hourTextInput.findViewById(R.id.material_label);

    minuteLabel.setText(res.getString(R.string.material_timepicker_minute));
    hourLabel.setText(res.getString(R.string.material_timepicker_hour));
    minuteTextInput.setTag(R.id.selection_type, MINUTE);
    hourTextInput.setTag(R.id.selection_type, HOUR);

    if (time.format == CLOCK_12H) {
      setupPeriodToggle();
    }

    OnClickListener onClickListener =
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            onSelectionChanged((int) v.getTag(R.id.selection_type));
          }
        };

    hourTextInput.setOnClickListener(onClickListener);
    minuteTextInput.setOnClickListener(onClickListener);
    hourTextInput.addInputFilter(time.getHourInputValidator());
    minuteTextInput.addInputFilter(time.getMinuteInputValidator());

    hourEditText = hourTextInput.getTextInput().getEditText();
    minuteEditText = minuteTextInput.getTextInput().getEditText();
    if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
      // Our XML drawable is not colored for pre-lollipop, set color programmatically.
      int primaryColor = MaterialColors.getColor(timePickerView, R.attr.colorPrimary);
      setCursorDrawableColor(hourEditText, primaryColor);
      setCursorDrawableColor(minuteEditText, primaryColor);
    }

    controller = new TimePickerTextInputKeyController(hourTextInput, minuteTextInput, time);
    hourTextInput.setChipDelegate(
        new ClickActionDelegate(timePickerView.getContext(), R.string.material_hour_selection));
    minuteTextInput.setChipDelegate(
        new ClickActionDelegate(timePickerView.getContext(), R.string.material_minute_selection));

    initialize();
  }

  @Override
  public void initialize() {
    addTextWatchers();
    setTime(time);
    controller.bind();
  }

  private void addTextWatchers() {
    hourEditText.addTextChangedListener(hourTextWatcher);
    minuteEditText.addTextChangedListener(minuteTextWatcher);
  }

  private void removeTextWatchers() {
    hourEditText.removeTextChangedListener(hourTextWatcher);
    minuteEditText.removeTextChangedListener(minuteTextWatcher);
  }

  private void setTime(TimeModel time) {
    removeTextWatchers();
    Locale current = timePickerView.getResources().getConfiguration().locale;
    String minuteFormatted = String.format(current, "%02d", time.minute);
    String hourFormatted = String.format(current, "%02d", time.getHourForDisplay());
    minuteTextInput.setText(minuteFormatted);
    hourTextInput.setText(hourFormatted);
    addTextWatchers();
    updateSelection();
  }

  private void setupPeriodToggle() {
    toggle = timePickerView.findViewById(R.id.material_clock_period_toggle);

    toggle.addOnButtonCheckedListener(
        new OnButtonCheckedListener() {
          @Override
          public void onButtonChecked(
              MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
            int period = checkedId == R.id.material_clock_period_pm_button ? PM : AM;
            time.setPeriod(period);
          }
        });
    toggle.setVisibility(View.VISIBLE);
    updateSelection();
  }

  private void updateSelection() {
    if (toggle == null) {
      return;
    }

    toggle.check(
        time.period == AM
            ? R.id.material_clock_period_am_button
            : R.id.material_clock_period_pm_button);
  }

  @Override
  public void onSelectionChanged(int selection) {
    time.selection = selection;
    minuteTextInput.setChecked(selection == MINUTE);
    hourTextInput.setChecked(selection == HOUR);
    updateSelection();
  }

  @Override
  public void show() {
    timePickerView.setVisibility(View.VISIBLE);
  }

  @Override
  public void hide() {
    View currentFocus = timePickerView.getFocusedChild();
    // Hide keyboard in case it was showing.
    if (currentFocus == null) {
      timePickerView.setVisibility(GONE);
      return;
    }
    Context context = timePickerView.getContext();
    InputMethodManager imm = getSystemService(context, InputMethodManager.class);
    if (imm != null) {
      imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
    }
    timePickerView.setVisibility(GONE);
  }

  @Override
  public void invalidate() {
    setTime(time);
  }

  /*
   * android:textColorDrawable doesn't have an app compat version to be able to use theme attributes
   * for colors. We have to apply a color filter manually. This method is only meant to be used
   * before API 21.
   */
  private static void setCursorDrawableColor(EditText view, @ColorInt int color) {
    try {
      Context context = view.getContext();
      Field cursorDrawableResField = TextView.class.getDeclaredField("mCursorDrawableRes");
      cursorDrawableResField.setAccessible(true);
      int cursorDrawableResId = cursorDrawableResField.getInt(view);
      Field editorField = TextView.class.getDeclaredField("mEditor");
      editorField.setAccessible(true);
      Object editor = editorField.get(view);
      Class<?> clazz = editor.getClass();
      Field cursorDrawableField = clazz.getDeclaredField("mCursorDrawable");
      cursorDrawableField.setAccessible(true);
      Drawable drawable = AppCompatResources.getDrawable(context, cursorDrawableResId);
      drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
      Drawable[] drawables = {drawable, drawable};
      cursorDrawableField.set(editor, drawables);
    } catch (Throwable ignored) {
      // ignore use the drawable default color (black).
    }
  }

  public void resetChecked() {
    minuteTextInput.setChecked(time.selection == MINUTE);
    hourTextInput.setChecked(time.selection == HOUR);
  }

  public void clearCheck() {
    minuteTextInput.setChecked(false);
    hourTextInput.setChecked(false);
  }
}
