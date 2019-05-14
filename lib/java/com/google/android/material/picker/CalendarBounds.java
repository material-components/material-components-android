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
package com.google.android.material.picker;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.util.Arrays;

/**
 * Used to limit the display range of {@link MaterialCalendar} and set a starting {@link Month}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public final class CalendarBounds implements Parcelable {

  private final Month start;
  private final Month end;
  private final Month current;

  private CalendarBounds(Month start, Month end, Month current) {
    this.start = start;
    this.end = end;
    this.current = current;
    if (start.compareTo(current) > 0) {
      throw new IllegalArgumentException("start Month cannot be after current Month");
    }
    if (current.compareTo(end) > 0) {
      throw new IllegalArgumentException("current Month cannot be after end Month");
    }
  }

  /**
   * Creates a CalendarBounds instance which opens onto {@code current} and is bounded between
   * {@code start} and {@code end}.
   */
  public static CalendarBounds create(Month start, Month end, Month current) {
    return new CalendarBounds(start, end, current);
  }

  /**
   * Creates a CalendarBounds instance that opens on today if it is within the bounds or {@code
   * start} if today is not within the bounds.
   */
  public static CalendarBounds create(Month start, Month end) {
    Month today = Month.today();
    if (end.compareTo(today) >= 0 && today.compareTo(start) >= 0) {
      return new CalendarBounds(start, end, Month.today());
    }
    return new CalendarBounds(start, end, start);
  }

  /** Returns the earliest {@link Month} allowed by this set of bounds. */
  public Month getStart() {
    return start;
  }

  /** Returns the latest {@link Month} allowed by this set of bounds. */
  public Month getEnd() {
    return end;
  }

  /** Returns the current {@link Month} within this set of bounds. */
  public Month getCurrent() {
    return current;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CalendarBounds)) {
      return false;
    }
    CalendarBounds that = (CalendarBounds) o;
    return start.equals(that.start) && end.equals(that.end) && current.equals(that.current);
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {start, end, current};
    return Arrays.hashCode(hashedFields);
  }

  /* Parcelable interface */

  /** {@link Parcelable.Creator} */
  public static final Parcelable.Creator<CalendarBounds> CREATOR =
      new Parcelable.Creator<CalendarBounds>() {
        @Override
        public CalendarBounds createFromParcel(Parcel source) {
          Month start = source.readParcelable(Month.class.getClassLoader());
          Month end = source.readParcelable(Month.class.getClassLoader());
          Month current = source.readParcelable(Month.class.getClassLoader());
          return CalendarBounds.create(start, end, current);
        }

        @Override
        public CalendarBounds[] newArray(int size) {
          return new CalendarBounds[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(start, /* parcelableFlags= */ 0);
    dest.writeParcelable(end, /* parcelableFlags= */ 0);
    dest.writeParcelable(current, /* parcelableFlags= */ 0);
  }
}
