/*
 * Copyright 2026 The Android Open Source Project
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

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.chip.Chip;

/** A {@link Chip} for displaying hour and minute values in {@link MaterialTimePicker}. */
class TimePickerChip extends Chip {

  public TimePickerChip(@NonNull Context context) {
    super(context);
  }

  public TimePickerChip(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public TimePickerChip(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public int getTextAlignment() {
    int textAlignment = super.getTextAlignment();
    // Apps without android:supportsRtl="true" resolve textAlignment to TEXT_ALIGNMENT_GRAVITY,
    // falling back to Chip's hardcoded Gravity.START. Force TEXT_ALIGNMENT_CENTER on API 34+
    // (matching values-v34/ zero chip paddings) while leaving older API levels unchanged.
    if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE
        && textAlignment == TEXT_ALIGNMENT_GRAVITY) {
      return TEXT_ALIGNMENT_CENTER;
    }
    return textAlignment;
  }
}
