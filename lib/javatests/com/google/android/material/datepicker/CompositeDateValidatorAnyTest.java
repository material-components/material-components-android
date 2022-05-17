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

import com.google.android.material.R;

import static com.google.common.truth.Truth.assertThat;

import android.os.Parcel;
import com.google.android.material.datepicker.CalendarConstraints.DateValidator;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Test for {@link CompositeDateValidator#anyOf(List)} */
@RunWith(RobolectricTestRunner.class)
public class CompositeDateValidatorAnyTest {

  private DateValidator subject;

  @Before
  public void createSubject() {
    DateValidator validator1 = DateValidatorPointBackward.before(5);
    DateValidator validator2 = DateValidatorPointForward.from(10);

    ArrayList<DateValidator> validators = new ArrayList<>(2);
    validators.add(validator1);
    validators.add(validator2);

    subject = CompositeDateValidator.anyOf(validators);
  }

  @Test
  public void testValidDate() {
    assertThat(subject.isValid(2)).isTrue();
    assertThat(subject.isValid(5)).isTrue();
    assertThat(subject.isValid(11)).isTrue();
  }

  @Test
  public void testInvalidDate() {
    assertThat(subject.isValid(6)).isFalse();
    assertThat(subject.isValid(9)).isFalse();
  }

  @Test
  public void testParcelable() {
    DateValidator original = subject;

    Parcel parcel = Parcel.obtain();
    original.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);

    DateValidator createdFromParcel = CompositeDateValidator.CREATOR.createFromParcel(parcel);
    assertThat(original).isEqualTo(createdFromParcel);
  }
}
