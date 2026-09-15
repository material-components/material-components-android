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
import static com.google.android.material.timepicker.TimeFormat.CLOCK_12H;
import static com.google.android.material.timepicker.TimeFormat.CLOCK_24H;
import static java.util.Calendar.AM;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.PM;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.view.HapticFeedbackConstantsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.internal.TextWatcherAdapter;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.timepicker.TimePickerView.OnSelectionChange;
import java.util.Locale;

class TimePickerTextInputPresenter implements OnSelectionChange, TimePickerPresenter {

  private static final int MINUTES_MAX_VALUE = 59;
  private static final int HOURS_MAX_VALUE_12H = 12;
  private static final int HOURS_MAX_VALUE_24H = 23;
  private static final int MINUTES_MAX_LENGTH = 2;
  private static final int HOURS_MAX_LENGTH = 2;

  private final LinearLayout timePickerView;
  private final TimeModel time;
  private final TextWatcher minuteTextWatcher =
      new TextWatcherAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
          try {
            if (TextUtils.isEmpty(s)) {
              time.setMinute(0);
              clearMinuteError();
              return;
            }
            if (s.length() > MINUTES_MAX_LENGTH) {
              s = s.delete(MINUTES_MAX_LENGTH, s.length()); // lock to 2 digits
              vibrateAndMaybeBeep(minuteEditText);
              return;
            }
            int minute = Integer.parseInt(s.toString());
            if (minute > MINUTES_MAX_VALUE) {
              setMinuteError();
            } else {
              clearMinuteError();
            }
            time.setMinute(minute);
          } catch (NumberFormatException ok) {
            setMinuteError();
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
              clearHourError();
              return;
            }
            if (s.length() > HOURS_MAX_LENGTH) {
              s = s.delete(HOURS_MAX_LENGTH, s.length()); // lock to 2 digits
              vibrateAndMaybeBeep(hourEditText);
              return;
            }
            int hour = Integer.parseInt(s.toString());
            if ((time.format == CLOCK_12H && hour > HOURS_MAX_VALUE_12H)
                || (time.format == CLOCK_24H && hour > HOURS_MAX_VALUE_24H)) {
              setHourError();
            } else {
              clearHourError();
            }
            time.setHour(hour);
          } catch (NumberFormatException ok) {
            setHourError();
          }
        }
      };
  private final ChipTextInputComboView minuteTextInput;
  private final ChipTextInputComboView hourTextInput;
  private final TimePickerTextInputKeyController controller;
  private final EditText minuteEditText;
  private final EditText hourEditText;
  private final TextView minuteLabel;
  private final TextView hourLabel;
  private final String minuteText;
  private final String hourText;
  private final String minuteErrorText;
  private final String hourErrorText;
  private final String hourError24hText;

  private MaterialButtonToggleGroup toggle;

  public TimePickerTextInputPresenter(final LinearLayout timePickerView, final TimeModel time) {
    this.timePickerView = timePickerView;
    this.time = time;
    Resources res = timePickerView.getResources();
    minuteTextInput = timePickerView.findViewById(R.id.material_minute_text_input);
    hourTextInput = timePickerView.findViewById(R.id.material_hour_text_input);
    minuteLabel = minuteTextInput.findViewById(R.id.material_label);
    hourLabel = hourTextInput.findViewById(R.id.material_label);

    minuteLabel.setText(res.getString(R.string.material_timepicker_minute));
    minuteLabel.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

    hourLabel.setText(res.getString(R.string.material_timepicker_hour));
    hourLabel.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

    minuteText = res.getString(R.string.material_timepicker_minute);
    hourText = res.getString(R.string.material_timepicker_hour);
    minuteErrorText = res.getString(R.string.material_timepicker_minute_error);
    hourErrorText = res.getString(R.string.material_timepicker_hour_error);
    hourError24hText = res.getString(R.string.material_timepicker_hour_error_24h);

    minuteTextInput.setTag(R.id.selection_type, MINUTE);
    hourTextInput.setTag(R.id.selection_type, HOUR);

    if (time.format == CLOCK_12H) {
      setupPeriodToggle();
    }

    OnClickListener onClickListener = v -> onSelectionChanged((int) v.getTag(R.id.selection_type));
    hourTextInput.setOnClickListener(onClickListener);
    minuteTextInput.setOnClickListener(onClickListener);

    hourEditText = hourTextInput.getTextInput().getEditText();
    hourEditText.setAccessibilityDelegate(
        setTimeUnitAccessibilityLabel(
            timePickerView.getResources(), R.string.material_timepicker_hour));
    minuteEditText = minuteTextInput.getTextInput().getEditText();
    minuteEditText.setAccessibilityDelegate(
        setTimeUnitAccessibilityLabel(
            timePickerView.getResources(), R.string.material_timepicker_minute));

    controller = new TimePickerTextInputKeyController(hourTextInput, minuteTextInput, time);
    hourTextInput.setChipDelegate(
        new ClickActionDelegate(timePickerView.getContext(), R.string.material_hour_selection) {
          @Override
          public void onInitializeAccessibilityNodeInfo(
              View host, AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.setContentDescription(
                res.getString(R.string.material_timepicker_hour)
                    + " " // Adds a pause between the hour label and the hour value.
                    + host.getResources()
                        .getString(
                            time.getHourContentDescriptionResId(),
                            String.valueOf(time.getHourForDisplay())));
          }
        });
    minuteTextInput.setChipDelegate(
        new ClickActionDelegate(timePickerView.getContext(), R.string.material_minute_selection) {
          @Override
          public void onInitializeAccessibilityNodeInfo(
              View host, AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.setContentDescription(
                res.getString(R.string.material_timepicker_minute)
                    + " " // Adds a pause between the minute label and the minute value.
                    + host.getResources()
                        .getString(R.string.material_minute_suffix, String.valueOf(time.minute)));
          }
        });

    initialize();
  }

  private View.AccessibilityDelegate setTimeUnitAccessibilityLabel(
      Resources res, int contentDescriptionResId) {
    return new View.AccessibilityDelegate() {
      @Override
      public void onInitializeAccessibilityNodeInfo(View v, AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(v, info);
        info.setText(res.getString(contentDescriptionResId));
      }
    };
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

  @SuppressLint("FlaggedApi")
  private void setMinuteError() {
    minuteTextInput.setError(true);
    minuteLabel.setText(minuteErrorText);
    minuteLabel.announceForAccessibility(minuteLabel.getText());
    vibrateAndMaybeBeep(minuteLabel);
  }

  @SuppressLint("FlaggedApi")
  private void setHourError() {
    hourTextInput.setError(true);
    hourLabel.setText(time.format == CLOCK_24H ? hourError24hText : hourErrorText);
    hourLabel.announceForAccessibility(hourLabel.getText());
    vibrateAndMaybeBeep(hourLabel);
  }

  private void clearMinuteError() {
    minuteTextInput.setError(false);
    minuteLabel.setText(minuteText);
  }

  private void clearHourError() {
    hourTextInput.setError(false);
    hourLabel.setText(hourText);
  }

  boolean hasError() {
    return minuteTextInput.hasError() || hourTextInput.hasError();
  }

  void clearError() {
    clearMinuteError();
    clearHourError();
  }

  void vibrateAndMaybeBeep(@NonNull View view) {
    vibrate(view);
    if (!isTouchExplorationEnabled(view.getContext())) {
      beep(view.getContext());
    }
  }

  void accessibilityFocusOnError() {
    if (hourTextInput.hasError()) {
      requestAccessibilityFocusAndAnnounce(hourTextInput, hourLabel);
    } else if (minuteTextInput.hasError()) {
      requestAccessibilityFocusAndAnnounce(minuteTextInput, minuteLabel);
    }
  }

  private void requestAccessibilityFocusAndAnnounce(
      @NonNull ChipTextInputComboView viewToFocus, @NonNull TextView labelToAnnounce) {
    viewToFocus.requestAccessibilityFocus();
    labelToAnnounce.announceForAccessibility(labelToAnnounce.getText());
  }

  private void vibrate(@NonNull View view) {
    ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.REJECT);
  }

  private void beep(@NonNull Context context) {
    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    if (audioManager != null) {
      audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_INVALID);
    }
  }

  private void setTime(TimeModel time) {
    removeTextWatchers();
    Locale current = timePickerView.getResources().getConfiguration().locale;
    String minuteFormatted = String.format(current, "%02d", time.minute);
    String hourFormatted = String.format(current, "%02d", time.getHourForDisplay());
    minuteTextInput.setText(minuteFormatted);
    hourTextInput.setText(hourFormatted);
    addTextWatchers();
    onSelectionChanged(time.selection);
  }

  private void setupPeriodToggle() {
    toggle = timePickerView.findViewById(R.id.material_clock_period_toggle);

    toggle.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (!isChecked) {
            return;
          }

          int period = checkedId == R.id.material_clock_period_pm_button ? PM : AM;
          time.setPeriod(period);
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
    onSelectionChanged(time.selection);
  }

  @Override
  public void hide() {
    View currentFocus = timePickerView.getFocusedChild();
    if (currentFocus != null) {
      ViewUtils.hideKeyboard(currentFocus, /* useWindowInsetsController= */ false);
    }

    timePickerView.setVisibility(GONE);
  }

  @Override
  public void invalidate() {
    setTime(time);
  }

  public void resetChecked() {
    minuteTextInput.setChecked(time.selection == MINUTE);
    hourTextInput.setChecked(time.selection == HOUR);
  }

  public void clearCheck() {
    minuteTextInput.setChecked(false);
    hourTextInput.setChecked(false);
  }

  private static boolean isTouchExplorationEnabled(@NonNull Context context) {
    AccessibilityManager accessibilityManager =
        (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
    return accessibilityManager != null && accessibilityManager.isTouchExplorationEnabled();
  }
}
