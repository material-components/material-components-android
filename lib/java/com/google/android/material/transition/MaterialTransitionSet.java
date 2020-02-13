/*
 * Copyright 2019 The Android Open Source Project
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

package com.google.android.material.transition;

import static com.google.android.material.transition.TransitionUtils.maybeAddTransition;
import static com.google.android.material.transition.TransitionUtils.maybeRemoveTransition;

import android.content.Context;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.transition.Transition;
import android.transition.TransitionSet;

/**
 * A base {@link TransitionSet} that provides primary and secondary {@link Transition} references
 * that can be further configured.
 */
@RequiresApi(VERSION_CODES.KITKAT)
abstract class MaterialTransitionSet<T extends Transition> extends TransitionSet {

  @NonNull protected Context context;

  @NonNull private T primaryTransition;

  @Nullable private Transition secondaryTransition;

  @NonNull
  abstract T getDefaultPrimaryTransition();

  @Nullable
  abstract Transition getDefaultSecondaryTransition();

  protected void initialize(Context context) {
    this.context = context;
    primaryTransition = getDefaultPrimaryTransition();
    addTransition(primaryTransition);
    setSecondaryTransition(getDefaultSecondaryTransition());
  }

  @NonNull
  public T getPrimaryTransition() {
    return primaryTransition;
  }

  @Nullable
  public Transition getSecondaryTransition() {
    return secondaryTransition;
  }

  public void setSecondaryTransition(@Nullable Transition secondaryTransition) {
    maybeRemoveTransition(this, this.secondaryTransition);
    this.secondaryTransition = secondaryTransition;
    maybeAddTransition(this, this.secondaryTransition);
  }
}
