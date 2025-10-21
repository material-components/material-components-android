/*
 * Copyright 2025 The Android Open Source Project
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
package com.google.android.material.listitem;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.IntDef;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Interface for the part of a ListItem that is able to be swiped.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public interface SwipeableListItem {
  /** The state at which the {@link SwipeableListItem} is being dragged. */
  public static final int STATE_DRAGGING = 1;

  /** The state at which the {@link SwipeableListItem} is settling to the nearest settling point. */
  public static final int STATE_SETTLING = 2;

  /**
   * The state at which the associated {@link RevealableListItem} is closed and nothing is revealed.
   */
  public static final int STATE_CLOSED = 3;

  /**
   * The state at which the associated {@link RevealableListItem} is revealed to its intrinsic
   * width.
   */
  public static final int STATE_OPEN = 4;

  /**
   * States that the {@link SwipeableListItem} can be in.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({
    STATE_DRAGGING,
    STATE_SETTLING,
    STATE_CLOSED,
    STATE_OPEN,
  })
  @Retention(RetentionPolicy.SOURCE)
  @interface SwipeState {}

  /**
   * Stable states that the {@link SwipeableListItem} can be in. These are states that the
   * {@link SwipeableListItem} can settle to.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({STATE_CLOSED, STATE_OPEN})
  @Retention(RetentionPolicy.SOURCE)
  @interface StableSwipeState {}
}
