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
package com.google.android.material.datepicker;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;

/**
 * Used to limit the display range of the calendar and set an openAt month.
 *
 * <p>Implements {@link Parcelable} in order to maintain the {@code CalendarConstraints} across
 * device configuration changes. Parcelable breaks when passed between processes.
 */
public final class CalendarConstraints implements Parcelable {

  @NonNull private final Month start;
  @NonNull private final Month end;
  @NonNull private final DateValidator validator;

  @Nullable private Month openAt;
  private final int firstDayOfWeek;

  private final int yearSpan;
  private final int monthSpan;

  /**
   * Used to determine whether calendar days are enabled.
   *
   * <p>Extends {@link Parcelable} in order to maintain the {@code DateValidator} across device
   * configuration changes. Parcelable breaks when passed between processes.
   */
  public interface DateValidator extends Parcelable {

    /** Returns true if the provided {@code date} is enabled. */
    boolean isValid(long date);
  }

  private CalendarConstraints(
      @NonNull Month start,
      @NonNull Month end,
      @NonNull DateValidator validator,
      @Nullable Month openAt,
      int firstDayOfWeek) {
    Objects.requireNonNull(start, "start cannot be null");
    Objects.requireNonNull(end, "end cannot be null");
    Objects.requireNonNull(validator, "validator cannot be null");
    this.start = start;
    this.end = end;
    this.openAt = openAt;
    this.firstDayOfWeek = firstDayOfWeek;
    this.validator = validator;
    if (openAt != null && start.compareTo(openAt) > 0) {
      throw new IllegalArgumentException("start Month cannot be after current Month");
    }
    if (openAt != null && openAt.compareTo(end) > 0) {
      throw new IllegalArgumentException("current Month cannot be after end Month");
    }
    if (firstDayOfWeek < 0
        || firstDayOfWeek > UtcDates.getUtcCalendar().getMaximum(Calendar.DAY_OF_WEEK)) {
      throw new IllegalArgumentException("firstDayOfWeek is not valid");
    }
    monthSpan = start.monthsUntil(end) + 1;
    yearSpan = end.year - start.year + 1;
  }

  boolean isWithinBounds(long date) {
    return start.getDay(1) <= date && date <= end.getDay(end.daysInMonth);
  }

  /**
   * Returns the {@link DateValidator} that determines whether a date can be clicked and selected.
   */
  public DateValidator getDateValidator() {
    return validator;
  }

  /** Returns the earliest month allowed by this set of bounds. */
  @NonNull
  Month getStart() {
    return start;
  }

  /** Returns the latest month allowed by this set of bounds. */
  @NonNull
  Month getEnd() {
    return end;
  }

  /** Returns the openAt month within this set of bounds. */
  @Nullable
  Month getOpenAt() {
    return openAt;
  }

  /** Sets the openAt month. */
  void setOpenAt(@Nullable Month openAt) {
    this.openAt = openAt;
  }

  /** Returns the firstDayOfWeek. */
  int getFirstDayOfWeek() {
    return firstDayOfWeek;
  }

  /**
   * Returns the total number of {@link java.util.Calendar#MONTH} included in {@code start} to
   * {@code end}.
   */
  int getMonthSpan() {
    return monthSpan;
  }

  /**
   * Returns the total number of {@link java.util.Calendar#YEAR} included in {@code start} to {@code
   * end}.
   */
  int getYearSpan() {
    return yearSpan;
  }

  /** Returns the earliest time in milliseconds allowed by this set of bounds. */
  public long getStartMs() {
    return start.timeInMillis;
  }

  /** Returns the latest time in milliseconds allowed by this set of bounds. */
  public long getEndMs() {
    return end.timeInMillis;
  }

  /**
   * Returns the openAt time in milliseconds within this set of bounds. Returns null if not
   * available.
   */
  @Nullable
  public Long getOpenAtMs() {
    return openAt == null ? null : openAt.timeInMillis;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CalendarConstraints)) {
      return false;
    }
    CalendarConstraints that = (CalendarConstraints) o;
    return start.equals(that.start)
        && end.equals(that.end)
        && ObjectsCompat.equals(openAt, that.openAt)
        && firstDayOfWeek == that.firstDayOfWeek
        && validator.equals(that.validator);
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {start, end, openAt, firstDayOfWeek, validator};
    return Arrays.hashCode(hashedFields);
  }

  /* Parcelable interface */

  /** {@link Parcelable.Creator} */
  public static final Parcelable.Creator<CalendarConstraints> CREATOR =
      new Parcelable.Creator<CalendarConstraints>() {
        @NonNull
        @Override
        public CalendarConstraints createFromParcel(@NonNull Parcel source) {
          Month start = source.readParcelable(Month.class.getClassLoader());
          Month end = source.readParcelable(Month.class.getClassLoader());
          Month openAt = source.readParcelable(Month.class.getClassLoader());
          DateValidator validator = source.readParcelable(DateValidator.class.getClassLoader());
          int firstDayOfWeek = source.readInt();
          return new CalendarConstraints(start, end, validator, openAt, firstDayOfWeek);
        }

        @NonNull
        @Override
        public CalendarConstraints[] newArray(int size) {
          return new CalendarConstraints[size];
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
    dest.writeParcelable(openAt, /* parcelableFlags= */ 0);
    dest.writeParcelable(validator, /* parcelableFlags = */ 0);
    dest.writeInt(firstDayOfWeek);
  }

  /**
   * Returns the given month if it's within the constraints or the closest bound if it's outside.
   */
  Month clamp(Month month) {
    if (month.compareTo(start) < 0) {
      return start;
    }

    if (month.compareTo(end) > 0) {
      return end;
    }

    return month;
  }

  /** Builder for {@link com.google.android.material.datepicker.CalendarConstraints}. */
  public static final class Builder {

    /**
     * Default UTC timeInMilliseconds for the first selectable month unless {@link Builder#setStart}
     * is called. Set to January, 1900.
     */
    static final long DEFAULT_START =
        UtcDates.canonicalYearMonthDay(Month.create(1900, Calendar.JANUARY).timeInMillis);
    /**
     * Default UTC timeInMilliseconds for the last selectable month unless {@link Builder#setEnd} is
     * called. Set to December, 2100.
     */
    static final long DEFAULT_END =
        UtcDates.canonicalYearMonthDay(Month.create(2100, Calendar.DECEMBER).timeInMillis);

    private static final String DEEP_COPY_VALIDATOR_KEY = "DEEP_COPY_VALIDATOR_KEY";

    private long start = DEFAULT_START;
    private long end = DEFAULT_END;
    private Long openAt;
    private int firstDayOfWeek;
    private DateValidator validator = DateValidatorPointForward.from(Long.MIN_VALUE);

    public Builder() {}

    Builder(@NonNull CalendarConstraints clone) {
      start = clone.start.timeInMillis;
      end = clone.end.timeInMillis;
      openAt = clone.openAt.timeInMillis;
      firstDayOfWeek = clone.firstDayOfWeek;
      validator = clone.validator;
    }

    /**
     * A UTC timeInMilliseconds contained within the earliest month the calendar will page to.
     * Defaults January, 1900.
     *
     * <p>If you have access to java.time in Java 8, you can obtain a long using {@code
     * java.time.ZonedDateTime}.
     *
     * <pre>{@code
     * LocalDateTime local = LocalDateTime.of(year, month, 1, 0, 0);
     * local.atZone(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).toInstant().toEpochMilli();
     * }</pre>
     *
     * <p>If you don't have access to java.time in Java 8, you can obtain this value using a {@code
     * java.util.Calendar} instance from the UTC timezone.
     *
     * <pre>{@code
     * Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
     * c.set(year, month, 1);
     * c.getTimeInMillis();
     * }</pre>
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setStart(long month) {
      start = month;
      return this;
    }

    /**
     * A UTC timeInMilliseconds contained within the latest month the calendar will page to.
     * Defaults December, 2100.
     *
     * <p>If you have access to java.time in Java 8, you can obtain a long using {@code
     * java.time.ZonedDateTime}.
     *
     * <pre>{@code
     * LocalDateTime local = LocalDateTime.of(year, month, 1, 0, 0);
     * local.atZone(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).toInstant().toEpochMilli();
     * }</pre>
     *
     * <p>If you don't have access to java.time in Java 8, you can obtain this value using a {@code
     * java.util.Calendar} instance from the UTC timezone.
     *
     * <pre>{@code
     * Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
     * c.set(year, month, 1);
     * c.getTimeInMillis();
     * }</pre>
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setEnd(long month) {
      end = month;
      return this;
    }

    /**
     * A UTC timeInMilliseconds contained within the month the calendar should openAt. Defaults to
     * the month containing today if within bounds; otherwise, defaults to the starting month.
     *
     * <p>If you have access to java.time in Java 8, you can obtain a long using {@code
     * java.time.ZonedDateTime}.
     *
     * <pre>{@code
     * LocalDateTime local = LocalDateTime.of(year, month, 1, 0, 0);
     * local.atZone(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).toInstant().toEpochMilli();
     * }</pre>
     *
     * <p>If you don't have access to java.time in Java 8, you can obtain this value using a {@code
     * java.util.Calendar} instance from the UTC timezone.
     *
     * <pre>{@code
     * Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
     * c.set(year, month, 1);
     * c.getTimeInMillis();
     * }</pre>
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setOpenAt(long month) {
      openAt = month;
      return this;
    }

    /**
     * Sets what the first day of the week is; e.g., <code>Calendar.SUNDAY</code> in the U.S.,
     * <code>Calendar.MONDAY</code> in France.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setFirstDayOfWeek(int firstDayOfWeek) {
      this.firstDayOfWeek = firstDayOfWeek;
      return this;
    }

    /**
     * Limits valid dates to those for which {@link DateValidator#isValid(long)} is true. Defaults
     * to all dates as valid.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setValidator(@NonNull DateValidator validator) {
      Objects.requireNonNull(validator, "validator cannot be null");
      this.validator = validator;
      return this;
    }

    /** Builds the {@link CalendarConstraints} object using the set parameters or defaults. */
    @NonNull
    public CalendarConstraints build() {
      Bundle deepCopyBundle = new Bundle();
      deepCopyBundle.putParcelable(DEEP_COPY_VALIDATOR_KEY, validator);
      return new CalendarConstraints(
          Month.create(start),
          Month.create(end),
          (DateValidator) deepCopyBundle.getParcelable(DEEP_COPY_VALIDATOR_KEY),
          openAt == null ? null : Month.create(openAt),
          firstDayOfWeek);
    }
  }
}
