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

package com.google.android.material.badge;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;
import com.google.android.material.badge.BadgeDrawable.SavedState;
import com.google.android.material.internal.ParcelableSparseArray;

/**
 * Utility class for {@link BadgeDrawable}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
public class BadgeUtils {

  public static final boolean USE_COMPAT_PARENT = VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN_MR2;

  private BadgeUtils() {
    // Private constructor to prevent unwanted construction.
  }

  /**
   * Updates a badge's bounds using its center coordinate, {@code halfWidth} and {@code halfHeight}.
   *
   * @param rect Holds rectangular coordinates of the badge's bounds.
   * @param centerX A badge's center x coordinate.
   * @param centerY A badge's center y coordinate.
   * @param halfWidth Half of a badge's width.
   * @param halfHeight Half of a badge's height.
   */
  public static void updateBadgeBounds(
      @NonNull Rect rect, float centerX, float centerY, float halfWidth, float halfHeight) {
    rect.set(
        (int) (centerX - halfWidth),
        (int) (centerY - halfHeight),
        (int) (centerX + halfWidth),
        (int) (centerY + halfHeight));
  }

  /*
   * Attaches a BadgeDrawable to its associated anchor and update the BadgeDrawable's coordinates
   * based on the anchor.
   * For API 18+, the BadgeDrawable will be added as a view overlay.
   * For pre-API 18, the BadgeDrawable will be set as the foreground of a FrameLayout that is an
   * ancestor of the anchor.
   */
  public static void attachBadgeDrawable(
      @NonNull BadgeDrawable badgeDrawable,
      @NonNull View anchor,
      @NonNull FrameLayout compatBadgeParent) {
    setBadgeDrawableBounds(badgeDrawable, anchor, compatBadgeParent);
    if (USE_COMPAT_PARENT) {
      compatBadgeParent.setForeground(badgeDrawable);
    } else {
      anchor.getOverlay().add(badgeDrawable);
    }
  }

  /*
   * Detaches a BadgeDrawable to its associated anchor.
   * For API 18+, the BadgeDrawable will be removed from its anchor's ViewOverlay.
   * For pre-API 18, the BadgeDrawable will be removed from the foreground of a FrameLayout that is
   * an ancestor of the anchor.
   */
  public static void detachBadgeDrawable(
      @Nullable BadgeDrawable badgeDrawable,
      @NonNull View anchor,
      @NonNull FrameLayout compatBadgeParent) {
    if (badgeDrawable == null) {
      return;
    }
    if (USE_COMPAT_PARENT) {
      compatBadgeParent.setForeground(null);
    } else {
      anchor.getOverlay().remove(badgeDrawable);
    }
  }

  /**
   * Sets the bounds of a BadgeDrawable to match the bounds of its anchor (for API 18+) or its
   * anchor's FrameLayout ancestor (pre-API 18).
   */
  public static void setBadgeDrawableBounds(
      @NonNull BadgeDrawable badgeDrawable,
      @NonNull View anchor,
      @NonNull FrameLayout compatBadgeParent) {
    Rect badgeBounds = new Rect();
    View badgeParent = USE_COMPAT_PARENT ? compatBadgeParent : anchor;
    badgeParent.getDrawingRect(badgeBounds);
    badgeDrawable.setBounds(badgeBounds);
    badgeDrawable.updateBadgeCoordinates(anchor, compatBadgeParent);
  }

  /**
   * Given a map of int keys to {@code BadgeDrawable BadgeDrawables}, creates a parcelable map of
   * unique int keys to {@code BadgeDrawable.SavedState SavedStates}. Useful for state restoration.
   *
   * @param badgeDrawables A {@link SparseArray} that contains a map of int keys (e.g. menuItemId)
   *     to {@code BadgeDrawable BadgeDrawables}.
   * @return A parcelable {@link SparseArray} that contains a map of int keys (e.g. menuItemId) to
   *     {@code BadgeDrawable.SavedState SavedStates}.
   */
  @NonNull
  public static ParcelableSparseArray createParcelableBadgeStates(
      @NonNull SparseArray<BadgeDrawable> badgeDrawables) {
    ParcelableSparseArray badgeStates = new ParcelableSparseArray();
    for (int i = 0; i < badgeDrawables.size(); i++) {
      int key = badgeDrawables.keyAt(i);
      BadgeDrawable badgeDrawable = badgeDrawables.valueAt(i);
      if (badgeDrawable == null) {
        throw new IllegalArgumentException("badgeDrawable cannot be null");
      }
      badgeStates.put(key, badgeDrawable.getSavedState());
    }
    return badgeStates;
  }

  /**
   * Given a map of int keys to {@link BadgeDrawable.SavedState SavedStates}, creates a parcelable
   * map of int keys to {@link BadgeDrawable BadgeDrawbles}. Useful for state restoration.
   *
   * @param context Current context
   * @param badgeStates A parcelable {@link SparseArray} that contains a map of int keys (e.g.
   *     menuItemId) to {@link BadgeDrawable.SavedState states}.
   * @return A {@link SparseArray} that contains a map of int keys (e.g. menuItemId) to {@code
   *     BadgeDrawable BadgeDrawbles}.
   */
  @NonNull
  public static SparseArray<BadgeDrawable> createBadgeDrawablesFromSavedStates(
      Context context, @NonNull ParcelableSparseArray badgeStates) {
    SparseArray<BadgeDrawable> badgeDrawables = new SparseArray<>(badgeStates.size());
    for (int i = 0; i < badgeStates.size(); i++) {
      int key = badgeStates.keyAt(i);
      BadgeDrawable.SavedState savedState = (SavedState) badgeStates.valueAt(i);
      if (savedState == null) {
        throw new IllegalArgumentException("BadgeDrawable's savedState cannot be null");
      }
      BadgeDrawable badgeDrawable = BadgeDrawable.createFromSavedState(context, savedState);
      badgeDrawables.put(key, badgeDrawable);
    }
    return badgeDrawables;
  }
}
