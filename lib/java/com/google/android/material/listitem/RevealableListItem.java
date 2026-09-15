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

import android.view.Gravity;
import androidx.annotation.IntDef;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Interface for the part of a ListItem that is able to be revealed when swiped. */
public interface RevealableListItem {

  /***
   * The gravity of where the RevealableListItem in relation to its sibling
   * {@link SwipeableListItem}.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({Gravity.START, Gravity.END, Gravity.LEFT, Gravity.RIGHT})
  @Retention(RetentionPolicy.SOURCE)
  @interface RevealGravity {}

  /** Disable the primary action. */
  int PRIMARY_ACTION_SWIPE_DISABLED = 0;

  /**
   * When swiping with a sibling {@link SwipeableListItem}, allow swiping to intermediary states
   * before the primary action.
   */
  int PRIMARY_ACTION_SWIPE_INDIRECT = 1;

  /**
   * When swiping with a sibling {@link SwipeableListItem}, swipe directly to the primary action.
   */
  int PRIMARY_ACTION_SWIPE_DIRECT = 2;

  /**
   * Mode which defines the behavior when swiping to reveal the primary action of the
   * RevealableListItem.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({
      PRIMARY_ACTION_SWIPE_DISABLED,
      PRIMARY_ACTION_SWIPE_INDIRECT,
      PRIMARY_ACTION_SWIPE_DIRECT,
  })
  @Retention(RetentionPolicy.SOURCE)
  @interface PrimaryActionSwipeMode {}

  /**
   * Sets the revealed width of RevealableListItem, in pixels.
   */
  void setRevealedWidth(@Px int revealedWidth);

  /**
   * Returns the intrinsic width of the RevealableListItem. This may be 0 if an intrinsic width
   * has not yet been measured.
   */
  @Px int getIntrinsicWidth();

  /**
   * Returns the {@link PrimaryActionSwipeMode} for the RevealableListItem that defines the swipe
   * to primary action behavior when swiping with a sibling {@link SwipeableListItem}.
   */
  @PrimaryActionSwipeMode
  int getPrimaryActionSwipeMode();

  /**
   * Sets the {@link PrimaryActionSwipeMode} for the RevealableListItem that defines the swipe
   * to primary action behavior when swiping with a sibling {@link SwipeableListItem}.
   */
  void setPrimaryActionSwipeMode(@PrimaryActionSwipeMode int swipeToPrimaryActionMode);
}
