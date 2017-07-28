/*
 * Copyright (C) 2017 The Android Open Source Project
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

package android.support.design.stateful;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.SimpleArrayMap;
import android.support.v4.view.AbsSavedState;

/**
 * SavedState for widgets that want to save and restore their own state in {@link
 * android.view.View#onSaveInstanceState()}. Supports widgets whose state is composed or delegated
 * out to multiple components.
 *
 * <p>Widgets with only composed or delegated state can directly instantiate this class and write to
 * {@link #extendableStates}. Widgets with additional state should subclass ExtendableSavedState
 * rather than trying to force the additional state into {@link #extendableStates}.
 */
public class ExtendableSavedState extends AbsSavedState {

  public final SimpleArrayMap<String, Bundle> extendableStates;

  public ExtendableSavedState(Parcelable superState) {
    super(superState);
    extendableStates = new SimpleArrayMap<>();
  }

  private ExtendableSavedState(Parcel in, ClassLoader loader) {
    super(in, loader);

    int size = in.readInt();

    String[] keys = new String[size];
    in.readStringArray(keys);

    Bundle[] states = new Bundle[size];
    in.readTypedArray(states, Bundle.CREATOR);

    extendableStates = new SimpleArrayMap<>(size);
    for (int i = 0; i < size; i++) {
      extendableStates.put(keys[i], states[i]);
    }
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    super.writeToParcel(out, flags);

    int size = extendableStates.size();
    out.writeInt(size);

    String[] keys = new String[size];
    Bundle[] states = new Bundle[size];

    for (int i = 0; i < size; i++) {
      keys[i] = extendableStates.keyAt(i);
      states[i] = extendableStates.valueAt(i);
    }

    out.writeStringArray(keys);
    out.writeTypedArray(states, 0);
  }

  @Override
  public String toString() {
    return "ExtendableSavedState{"
        + Integer.toHexString(System.identityHashCode(this))
        + " states="
        + extendableStates
        + "}";
  }

  public static final Parcelable.Creator<ExtendableSavedState> CREATOR =
      new Parcelable.ClassLoaderCreator<ExtendableSavedState>() {

        @Override
        public ExtendableSavedState createFromParcel(Parcel in, ClassLoader loader) {
          return new ExtendableSavedState(in, loader);
        }

        @Override
        public ExtendableSavedState createFromParcel(Parcel in) {
          return new ExtendableSavedState(in, null);
        }

        @Override
        public ExtendableSavedState[] newArray(int size) {
          return new ExtendableSavedState[size];
        }
      };
}
