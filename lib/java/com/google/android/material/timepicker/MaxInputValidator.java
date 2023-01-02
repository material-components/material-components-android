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

import android.text.InputFilter;
import android.text.Spanned;

/** A {@link InputFilter} that prevents a value bigger that {@code max} from being entered */
class MaxInputValidator implements InputFilter {
  private int max;

  public MaxInputValidator(int max) {
    this.max = max;
  }

  public void setMax(int max) {
    this.max = max;
  }

  public int getMax() {
    return max;
  }

  @Override
  public CharSequence filter(
      CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
    try {
      StringBuilder builder = new StringBuilder(dest);
      builder.replace(dstart, dend, source.subSequence(start, end).toString());
      String newVal = builder.toString();
      int input = Integer.parseInt(newVal);
      if (input <= max) {
        return null;
      }
    } catch (NumberFormatException ok) {
      // Just ignored if we couldn't parse the number
    }
    return "";
  }
}
