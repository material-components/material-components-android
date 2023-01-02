/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.google.android.material.internal;

import com.google.android.material.test.R;

import android.os.Bundle;
import android.os.Parcel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** test for {@link ParcelableSparseArray} */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class ParcelableSparseArrayTest {

  private static final String TEST_KEY = "TEST_KEY";
  private static final char TEST_CHAR = 'C';

  @Test
  public void testArray() {
    ParcelableSparseArray array = new ParcelableSparseArray();
    Bundle bundle = new Bundle();
    bundle.putChar(TEST_KEY, TEST_CHAR);
    array.append(0, bundle);

    Parcel parcel = Parcel.obtain();
    array.writeToParcel(parcel, array.describeContents());
    parcel.setDataPosition(0);

    ParcelableSparseArray createdFromParcel =
        ParcelableSparseArray.CREATOR.createFromParcel(parcel);
    Bundle first = (Bundle) createdFromParcel.get(0);
    Assert.assertEquals(TEST_CHAR, first.getChar(TEST_KEY));
  }
}
