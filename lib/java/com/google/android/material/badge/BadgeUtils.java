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

import com.google.android.material.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import com.google.android.material.internal.ParcelableSparseArray;
import com.google.android.material.internal.ToolbarUtils;

/**
 * Utility class for {@link BadgeDrawable}.
 *
 * <p>Warning: This class is experimental and the APIs are subject to change.
 */
@ExperimentalBadgeUtils
public class BadgeUtils {

  private static final String LOG_TAG = "BadgeUtils";

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

  public static void attachBadgeDrawable(
      @NonNull BadgeDrawable badgeDrawable, @NonNull View anchor) {
    attachBadgeDrawable(badgeDrawable, anchor, /* customBadgeParent */ null);
  }

  /**
   * Attaches a BadgeDrawable to its associated anchor and update the BadgeDrawable's coordinates
   * based on the anchor. The BadgeDrawable will be added as a view overlay as default. If it has a
   * FrameLayout custom parent that is an ancestor of the anchor, then the BadgeDrawable will be set
   * as the foreground of that.
   */
  public static void attachBadgeDrawable(
      @NonNull BadgeDrawable badgeDrawable,
      @NonNull View anchor,
      @Nullable FrameLayout customBadgeParent) {
    setBadgeDrawableBounds(badgeDrawable, anchor, customBadgeParent);

    if (badgeDrawable.getCustomBadgeParent() != null) {
      badgeDrawable.getCustomBadgeParent().setForeground(badgeDrawable);
    } else {
      anchor.getOverlay().add(badgeDrawable);
    }
  }

  /**
   * A convenience method to attach a BadgeDrawable to the specified menu item on a toolbar, update
   * the BadgeDrawable's coordinates based on its anchor and adjust the BadgeDrawable's offset so it
   * is not clipped off by the toolbar.
   *
   * <p>Menu item views are reused by the menu, so any structural changes to the menu may require
   * detaching the BadgeDrawable and re-attaching it to the correct item.
   */
  public static void attachBadgeDrawable(
      @NonNull BadgeDrawable badgeDrawable, @NonNull Toolbar toolbar, @IdRes int menuItemId) {
    attachBadgeDrawable(badgeDrawable, toolbar, menuItemId, null /*customBadgeParent */);
  }

  /**
   * Attaches a BadgeDrawable to its associated action menu item on a toolbar, update the
   * BadgeDrawable's coordinates based on this anchor and adjust the BadgeDrawable's offset so it is
   * not clipped off by the toolbar. The BadgeDrawable will be added as a view overlay as default.
   * If it has a FrameLayout custom parent that is an ancestor of the anchor, then the BadgeDrawable
   * will be set as the foreground of that.
   *
   * <p>Menu item views are reused by the menu, so any structural changes to the menu may require
   * detaching the BadgeDrawable and re-attaching it to the correct item.
   */
  public static void attachBadgeDrawable(
      @NonNull final BadgeDrawable badgeDrawable,
      @NonNull final Toolbar toolbar,
      @IdRes final int menuItemId,
      @Nullable final FrameLayout customBadgeParent) {

    toolbar.post(
        new Runnable() {
          @Override
          public void run() {
            ActionMenuItemView menuItemView =
                ToolbarUtils.getActionMenuItemView(toolbar, menuItemId);
            if (menuItemView != null) {
              setToolbarOffset(badgeDrawable, toolbar.getResources());
              BadgeUtils.attachBadgeDrawable(badgeDrawable, menuItemView, customBadgeParent);
              attachBadgeContentDescription(badgeDrawable, menuItemView);
            }
          }
        });
  }

  private static void attachBadgeContentDescription(
      @NonNull final BadgeDrawable badgeDrawable, @NonNull View view) {
    if (VERSION.SDK_INT >= VERSION_CODES.Q && ViewCompat.hasAccessibilityDelegate(view)) {
      ViewCompat.setAccessibilityDelegate(
          view,
          new AccessibilityDelegateCompat(view.getAccessibilityDelegate()) {
            @Override
            public void onInitializeAccessibilityNodeInfo(
                View host, AccessibilityNodeInfoCompat info) {
              super.onInitializeAccessibilityNodeInfo(host, info);
              info.setContentDescription(getBadgeAnchorContentDescription(view, badgeDrawable));
            }
          });
    } else {
      ViewCompat.setAccessibilityDelegate(
          view,
          new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(
                View host, AccessibilityNodeInfoCompat info) {
              super.onInitializeAccessibilityNodeInfo(host, info);
              info.setContentDescription(getBadgeAnchorContentDescription(view, badgeDrawable));
            }
          });
    }
  }

  private static CharSequence getBadgeAnchorContentDescription(
      View anchor, BadgeDrawable badgeDrawable) {
    CharSequence badgeContentDescription = badgeDrawable.getContentDescription();
    return badgeContentDescription != null
        ? badgeContentDescription
        : anchor.getContentDescription();
  }

  /**
   * Detaches a BadgeDrawable from its associated anchor. The BadgeDrawable will be removed from its
   * anchor's ViewOverlay. If it has a FrameLayout custom parent that is an ancestor of the anchor,
   * then the BadgeDrawable will be removed from the parent's foreground instead.
   */
  public static void detachBadgeDrawable(
      @Nullable BadgeDrawable badgeDrawable, @NonNull View anchor) {
    if (badgeDrawable == null) {
      return;
    }
    if (badgeDrawable.getCustomBadgeParent() != null) {
      badgeDrawable.getCustomBadgeParent().setForeground(null);
    } else {
      anchor.getOverlay().remove(badgeDrawable);
    }
  }

  /**
   * Detaches a BadgeDrawable from its associated action menu item on a toolbar, The BadgeDrawable
   * will be removed from its anchor's ViewOverlay. If it has a FrameLayout custom parent that is an
   * ancestor of the anchor, then the BadgeDrawable will be removed from the parent's foreground
   * instead.
   */
  public static void detachBadgeDrawable(
      @Nullable BadgeDrawable badgeDrawable, @NonNull Toolbar toolbar, @IdRes int menuItemId) {
    if (badgeDrawable == null) {
      return;
    }
    ActionMenuItemView menuItemView = ToolbarUtils.getActionMenuItemView(toolbar, menuItemId);
    if (menuItemView != null) {
      removeToolbarOffset(badgeDrawable);
      detachBadgeDrawable(badgeDrawable, menuItemView);
      detachBadgeContentDescription(menuItemView);
    } else {
      Log.w(LOG_TAG, "Trying to remove badge from a null menuItemView: " + menuItemId);
    }
  }

  private static void detachBadgeContentDescription(@NonNull View view) {
    if (VERSION.SDK_INT >= VERSION_CODES.Q && ViewCompat.hasAccessibilityDelegate(view)) {
      ViewCompat.setAccessibilityDelegate(
          view,
          new AccessibilityDelegateCompat(view.getAccessibilityDelegate()) {
            @Override
            public void onInitializeAccessibilityNodeInfo(
                View host, AccessibilityNodeInfoCompat info) {
              super.onInitializeAccessibilityNodeInfo(host, info);
              info.setContentDescription(view.getContentDescription());
            }
          });
    } else {
      ViewCompat.setAccessibilityDelegate(view, null);
    }
  }

  @VisibleForTesting
  static void setToolbarOffset(BadgeDrawable badgeDrawable, Resources resources) {
    badgeDrawable.setAdditionalHorizontalOffset(
        resources.getDimensionPixelOffset(
            R.dimen.mtrl_badge_toolbar_action_menu_item_horizontal_offset));
    badgeDrawable.setAdditionalVerticalOffset(
        resources.getDimensionPixelOffset(
            R.dimen.mtrl_badge_toolbar_action_menu_item_vertical_offset));
  }

  @VisibleForTesting
  static void removeToolbarOffset(BadgeDrawable badgeDrawable) {
    badgeDrawable.setAdditionalHorizontalOffset(0);
    badgeDrawable.setAdditionalVerticalOffset(0);
  }

  /**
   * Sets the bounds of a BadgeDrawable to match the bounds of its anchor or its anchor's
   * FrameLayout ancestor if it has a custom parent set.
   */
  public static void setBadgeDrawableBounds(
      @NonNull BadgeDrawable badgeDrawable,
      @NonNull View anchor,
      @Nullable FrameLayout compatBadgeParent) {
    Rect badgeBounds = new Rect();
    anchor.getDrawingRect(badgeBounds);
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
      badgeStates.put(key, badgeDrawable != null ? badgeDrawable.getSavedState() : null);
    }
    return badgeStates;
  }

  /**
   * Given a map of int keys to {@link BadgeState.State SavedStates}, creates a parcelable
   * map of int keys to {@link BadgeDrawable BadgeDrawbles}. Useful for state restoration.
   *
   * @param context Current context
   * @param badgeStates A parcelable {@link SparseArray} that contains a map of int keys (e.g.
   *     menuItemId) to {@link BadgeState.State states}.
   * @return A {@link SparseArray} that contains a map of int keys (e.g. menuItemId)
   * to {@link BadgeDrawable BadgeDrawables}.
   */
  @NonNull
  public static SparseArray<BadgeDrawable> createBadgeDrawablesFromSavedStates(
      Context context, @NonNull ParcelableSparseArray badgeStates) {
    SparseArray<BadgeDrawable> badgeDrawables = new SparseArray<>(badgeStates.size());
    for (int i = 0; i < badgeStates.size(); i++) {
      int key = badgeStates.keyAt(i);
      BadgeState.State savedState = (BadgeState.State) badgeStates.valueAt(i);
      BadgeDrawable badgeDrawable = null;
      if (savedState != null) {
        badgeDrawable = BadgeDrawable.createFromSavedState(context, savedState);
      }
      badgeDrawables.put(key, badgeDrawable);
    }
    return badgeDrawables;
  }
}
