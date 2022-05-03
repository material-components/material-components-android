/*
 * Copyright 2019 The Android Open Source Project
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
package io.material.catalog.datepicker;

import android.os.Parcel;
import com.google.android.material.datepicker.CalendarConstraints.DateValidator;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

/** A {@link DateValidator} that enables all days of the week, except Saturday and Sunday. */
public class DateValidatorWeekdays implements DateValidator {

  private Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

  public static final Creator<DateValidatorWeekdays> CREATOR =
      new Creator<DateValidatorWeekdays>() {
        @Override
        public DateValidatorWeekdays createFromParcel(Parcel source) {
          return new DateValidatorWeekdays();
        }

        @Override
        public DateValidatorWeekdays[] newArray(int size) {
          return new DateValidatorWeekdays[size];
        }
      };

  @Override
  public boolean isValid(long date) {
    utc.setTimeInMillis(date);
    int dayOfWeek = utc.get(Calendar.DAY_OF_WEEK);
    return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DateValidatorWeekdays)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {};
    return Arrays.hashCode(hashedFields);
  }
}
