/*
 * Copyright (C) 2019 The Android Open Source Project
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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseIntArray;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * Child of SparseIntArray that is parcelable.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ParcelableSparseIntArray extends SparseIntArray implements Parcelable {

  public ParcelableSparseIntArray() {
    super();
  }

  public ParcelableSparseIntArray(int initialCapacity) {
    super(initialCapacity);
  }

  public ParcelableSparseIntArray(@NonNull SparseIntArray sparseIntArray) {
    super();
    for (int i = 0; i < sparseIntArray.size(); i++) {
      put(sparseIntArray.keyAt(i), sparseIntArray.valueAt(i));
    }
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {
    int[] keys = new int[size()];
    int[] values = new int[size()];

    for (int i = 0; i < size(); i++) {
      keys[i] = keyAt(i);
      values[i] = valueAt(i);
    }

    dest.writeInt(size());
    dest.writeIntArray(keys);
    dest.writeIntArray(values);
  }

  public static final Creator<ParcelableSparseIntArray> CREATOR =
      new Creator<ParcelableSparseIntArray>() {
        @NonNull
        @Override
        public ParcelableSparseIntArray createFromParcel(@NonNull Parcel source) {
          int size = source.readInt();
          ParcelableSparseIntArray read = new ParcelableSparseIntArray(size);

          int[] keys = new int[size];
          int[] values = new int[size];

          source.readIntArray(keys);
          source.readIntArray(values);

          for (int i = 0; i < size; i++) {
            read.put(keys[i], values[i]);
          }

          return read;
        }

        @NonNull
        @Override
        public ParcelableSparseIntArray[] newArray(int size) {
          return new ParcelableSparseIntArray[size];
        }
      };
}
