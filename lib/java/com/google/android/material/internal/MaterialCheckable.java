/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.widget.Checkable;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/**
 * An custom checkable interface extending {@link Checkable} to support check group logic.
 *
 * @see CheckableGroup
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public interface MaterialCheckable<T extends MaterialCheckable<T>> extends Checkable {
  @IdRes int getId();

  void setInternalOnCheckedChangeListener(@Nullable OnCheckedChangeListener<T> listener);

  /**
   * Interface definition for a callback to be invoked when a {@link MaterialCheckable} is checked
   * or unchecked.
   */
  interface OnCheckedChangeListener<C> {
    /**
     * Called when the checked state of a {@link MaterialCheckable} has changed.
     *
     * @param checkable The compound button view whose state has changed.
     * @param isChecked  The new checked state of buttonView.
     */
    void onCheckedChanged(C checkable, boolean isChecked);
  }
}
