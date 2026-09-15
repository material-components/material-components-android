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

import android.view.View;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.google.android.material.listitem.RevealableListItem.RevealGravity;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Interface for the part of a ListItem that is able to be swiped.
 */
public interface SwipeableListItem {
  /** The state at which the {@link SwipeableListItem} is being dragged. */
  int STATE_DRAGGING = 1;

  /** The state at which the {@link SwipeableListItem} is settling to the nearest settling point. */
  int STATE_SETTLING = 2;

  /**
   * The state at which the associated {@link RevealableListItem} is closed and nothing is revealed.
   */
  int STATE_CLOSED = 3;

  /**
   * The state at which the associated {@link RevealableListItem} is revealed to its intrinsic
   * width.
   */
  int STATE_OPEN = 4;

  /**
   * The state at which the {@link SwipeableListItem} is fully swiped, meaning the primary action
   * has been committed.
   */
  int STATE_SWIPE_PRIMARY_ACTION = 5;

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
    STATE_SWIPE_PRIMARY_ACTION,
  })
  @Retention(RetentionPolicy.SOURCE)
  @interface SwipeState {}

  /**
   * Stable states that the {@link SwipeableListItem} can be in. These are states that the {@link
   * SwipeableListItem} can settle to.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({STATE_CLOSED, STATE_OPEN, STATE_SWIPE_PRIMARY_ACTION})
  @Retention(RetentionPolicy.SOURCE)
  @interface StableSwipeState {}

  /** Called when the position of the SwipeableListItem changes. */
  void onSwipe(int swipeOffset);

  /**
   * Callback for when the swipe state of the SwipeableListItem changes.
   *
   * @param swipeState The new {@link SwipeState} of the SwipeableListItem.
   * @param revealableListItem The associated {@link RevealableListItem} that is being revealed when
   *     swiped. If the new state is {@link #STATE_CLOSED}, this will be the last active {@link
   *     RevealableListItem}.
   * @param revealGravity The {@link RevealGravity} of the revealableListItem.
   */
  <T extends View & RevealableListItem> void onSwipeStateChanged(
      @SwipeState int swipeState, @NonNull T revealableListItem, @RevealGravity int revealGravity);

  /**
   * Returns the overshoot, in pixels, that the SwipeableListItem is able to be swiped past the
   * {@link #STATE_OPEN} or {@link #STATE_SWIPE_PRIMARY_ACTION} states by, before settling.
   */
  int getSwipeMaxOvershoot();

  /**
   * Whether or not to allow the SwipeableListItem to be swiped.
   */
  boolean isSwipeEnabled();

  /** Sets whether or not to allow the SwipeableListItem to be swiped. */
  void setSwipeEnabled(boolean swipeEnabled);
}
