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

import static androidx.core.util.Preconditions.checkNotNull;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.android.material.datepicker.CalendarConstraints.DateValidator;
import java.util.List;

/** A {@link DateValidator} that accepts a list of Date Validators. */
public final class CompositeDateValidator implements DateValidator {

  @NonNull private final Operator operator;
  @NonNull private final List<DateValidator> validators;

  private static final int COMPARATOR_ANY_ID = 1;

  private static final int COMPARATOR_ALL_ID = 2;

  private interface Operator {
    boolean isValid(@NonNull List<DateValidator> validators, long date);

    int getId();
  }

  private static final Operator ANY_OPERATOR =
      new Operator() {

        @Override
        public boolean isValid(@NonNull List<DateValidator> validators, long date) {
          for (DateValidator validator : validators) {
            if (validator == null) {
              continue;
            }
            if (validator.isValid(date)) {
              return true;
            }
          }

          return false;
        }

        @Override
        public int getId() {
          return COMPARATOR_ANY_ID;
        }
      };

  private static final Operator ALL_OPERATOR =
      new Operator() {

        @Override
        public boolean isValid(@NonNull List<DateValidator> validators, long date) {
          for (DateValidator validator : validators) {
            if (validator == null) {
              continue;
            }
            if (!validator.isValid(date)) {
              return false;
            }
          }

          return true;
        }

        @Override
        public int getId() {
          return COMPARATOR_ALL_ID;
        }
      };

  private CompositeDateValidator(@NonNull List<DateValidator> validators, Operator operator) {
    this.validators = validators;
    this.operator = operator;
  }

  /**
   * Returns a {@link DateValidator} that can perform validation for every given validator.
   */
  @NonNull
  public static DateValidator allOf(@NonNull List<DateValidator> validators) {
    return new CompositeDateValidator(validators, ALL_OPERATOR);
  }

  /** Returns a {@link DateValidator} that can perform validation for any given validator. */
  @NonNull
  public static DateValidator anyOf(@NonNull List<DateValidator> validators) {
    return new CompositeDateValidator(validators, ANY_OPERATOR);
  }

  /** Part of {@link Parcelable} requirements. Do not use. */
  public static final Creator<CompositeDateValidator> CREATOR =
      new Creator<CompositeDateValidator>() {
        @NonNull
        @Override
        public CompositeDateValidator createFromParcel(@NonNull Parcel source) {
          @SuppressWarnings("unchecked")
          List<DateValidator> validators =
              source.readArrayList(DateValidator.class.getClassLoader());
          int id = source.readInt();
          Operator operator = null;
          if (id == COMPARATOR_ALL_ID) {
            operator = ALL_OPERATOR;
          } else if (id == COMPARATOR_ANY_ID) {
            operator = ANY_OPERATOR;
          } else {
            operator = ALL_OPERATOR;
          }

          return new CompositeDateValidator(checkNotNull(validators), operator);
        }

        @NonNull
        @Override
        public CompositeDateValidator[] newArray(int size) {
          return new CompositeDateValidator[size];
        }
      };

  /**
   * Performs the {@link DateValidator#isValid(long)} check as an AND of all validators.
   * e.g. If every validator in this class returns `true` for each {@link
   * DateValidator#isValid(long)}, this this method will return true.
   *
   * @param date milliseconds date to validate against.
   * @return True, if the given date is valid for every given validator in this class.
   */
  @Override
  public boolean isValid(long date) {
    return operator.isValid(validators, date);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {
    dest.writeList(validators);
    dest.writeInt(operator.getId());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof CompositeDateValidator)) {
      return false;
    }

    CompositeDateValidator that = (CompositeDateValidator) o;

    return validators.equals(that.validators) && operator.getId() == that.operator.getId();
  }

  @Override
  public int hashCode() {
    return validators.hashCode();
  }
}
