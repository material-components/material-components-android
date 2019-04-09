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
import androidx.annotation.RestrictTo;
import android.util.SparseBooleanArray;

/**
 * Child of SparseBooleanArray that is parcelable.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ParcelableSparseBooleanArray extends SparseBooleanArray implements Parcelable {

  public ParcelableSparseBooleanArray() {
    super();
  }

  public ParcelableSparseBooleanArray(int initialCapacity) {
    super(initialCapacity);
  }

  public ParcelableSparseBooleanArray(SparseBooleanArray sparseBooleanArray) {
    super(sparseBooleanArray.size());
    for (int i = 0; i < sparseBooleanArray.size(); i++) {
      put(sparseBooleanArray.keyAt(i), sparseBooleanArray.valueAt(i));
    }
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    int[] keys = new int[size()];
    boolean[] values = new boolean[size()];

    for (int i = 0; i < size(); i++) {
      keys[i] = keyAt(i);
      values[i] = valueAt(i);
    }

    dest.writeInt(size());
    dest.writeIntArray(keys);
    dest.writeBooleanArray(values);
  }

  public static final Parcelable.Creator<ParcelableSparseBooleanArray> CREATOR =
      new Parcelable.Creator<ParcelableSparseBooleanArray>() {
        @Override
        public ParcelableSparseBooleanArray createFromParcel(Parcel source) {
          int size = source.readInt();
          ParcelableSparseBooleanArray read = new ParcelableSparseBooleanArray(size);

          int[] keys = new int[size];
          boolean[] values = new boolean[size];

          source.readIntArray(keys);
          source.readBooleanArray(values);

          for (int i = 0; i < size; i++) {
            read.put(keys[i], values[i]);
          }

          return read;
        }

        @Override
        public ParcelableSparseBooleanArray[] newArray(int size) {
          return new ParcelableSparseBooleanArray[size];
        }
      };
}
