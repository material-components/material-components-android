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

package com.google.android.material.datepicker;

import com.google.android.material.test.R;

import static com.google.common.truth.Truth.assertThat;

import android.os.Parcel;
import com.google.android.material.datepicker.CalendarConstraints.DateValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Test for {@link DateValidatorPointForward} */
@RunWith(RobolectricTestRunner.class)
public class DateValidatorPointForwardTest {

  @Test
  public void testValidDate() {
    DateValidator dateValidatorPointForward = DateValidatorPointForward.from(5);
    assertThat(dateValidatorPointForward.isValid(6)).isTrue();
  }

  @Test
  public void testInvalidDate() {
    DateValidator dateValidatorPointForward = DateValidatorPointForward.from(5);
    assertThat(dateValidatorPointForward.isValid(4)).isFalse();
  }

  @Test
  public void testParcelable() {
    DateValidator original = DateValidatorPointForward.from(5);

    // Obtain a Parcel object and write the parcelable object to it:
    Parcel parcel = Parcel.obtain();
    original.writeToParcel(parcel, 0);

    // After you're done with writing, you need to reset the parcel for reading:
    parcel.setDataPosition(0);

    // Reconstruct object from parcel and asserts:
    DateValidator createdFromParcel = DateValidatorPointForward.CREATOR.createFromParcel(parcel);
    assertThat(original).isEqualTo(createdFromParcel);
  }
}
