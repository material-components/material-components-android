/*
 * Copyright 2018 The Android Open Source Project
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

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewParent;

/**
 * Utility methods for dealing with touch targets of a view.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class TouchTargetUtils {

  private static final Rect HIT_RECT = new Rect();

  /**
   * Extend the tap target of the given view using a {@link TouchDelegate}.
   *
   * <p>This also adds an OnAttachStateChangeListener to the view to remove the TouchDelegate from
   * its ancestor when it is detached from its parent.
   *
   * <p>What this means for views which are part of a reusable layout is that you should call this
   * method to extend its tap target every time it is attached to a new ancestor.
   *
   * @param view the View whose tap target to extend
   * @param ancestorId the layout id of an ancestor of the given view. This ancestor must have
   *     bounds which include the extended tap target
   * @param left the left extension
   * @param top the top extension
   * @param right the right extension
   * @param bottom the bottom extension
   */
  public static void extendViewTouchTarget(
      final View view,
      final int ancestorId,
      final int left,
      final int top,
      final int right,
      final int bottom) {
    // Post to view's message queue to allow the view to be added to its
    // parent.
    view.post(
        new Runnable() {
          @Override
          public void run() {
            View ancestor = findViewAncestor(view, ancestorId);
            extendViewTouchTarget(view, ancestor, left, top, right, bottom);
          }
        });
  }

  /**
   * Extend the tap target of the given view using a {@link TouchDelegate}.
   *
   * <p>This also adds an OnAttachStateChangeListener to the view to remove the TouchDelegate from
   * its ancestor when it is detached from its parent.
   *
   * <p>What this means for views which are part of a reusable layout is that you should call this
   * method to extend its tap target every time it is attached to a new ancestor.
   *
   * @param view the View whose tap target to extend
   * @param nullableAncestor an ancestor of the given view. This ancestor must have bounds which
   *     include the extended tap target
   * @param left the left extension
   * @param top the top extension
   * @param right the right extension
   * @param bottom the bottom extension
   */
  public static void extendViewTouchTarget(
      final View view,
      @Nullable View nullableAncestor,
      final int left,
      final int top,
      final int right,
      final int bottom) {
    if (nullableAncestor == null) {
      return;
    }

    @NonNull final View ancestor = nullableAncestor;

    // Post to ancestor's message queue to allow the ancestor to layout its
    // children.
    ancestor.post(
        new Runnable() {
          @Override
          public void run() {
            // Find the hit rect of the given view, relative to its parent.
            Rect viewHitRect = new Rect();
            view.getHitRect(HIT_RECT);
            viewHitRect.set(HIT_RECT);

            // Keep offsetting viewHitRect until it is relative to the ancestor.
            ViewParent parent = view.getParent();
            while (parent != ancestor) {
              if (parent instanceof View) {
                View parentView = (View) parent;
                parentView.getHitRect(HIT_RECT);
                viewHitRect.offset(HIT_RECT.left, HIT_RECT.top);
                parent = parentView.getParent();
              } else {
                return;
              }
            }

            // viewHitRect is now relative to the ancestor.
            viewHitRect.left -= left;
            viewHitRect.top -= top;
            viewHitRect.right += right;
            viewHitRect.bottom += bottom;
            final TouchDelegate touchDelegate = new TouchDelegate(viewHitRect, view);

            // Add a touch delegate to the ancestor.
            final TouchDelegateGroup touchDelegateGroup = getOrCreateTouchDelegateGroup(ancestor);
            touchDelegateGroup.addTouchDelegate(touchDelegate);
            ancestor.setTouchDelegate(touchDelegateGroup);

            // Every time a view is recycled, it is removed from its parent.
            // Make sure to remove the touch delegate from the ancestor.
            view.addOnAttachStateChangeListener(
                new View.OnAttachStateChangeListener() {
                  @Override
                  public void onViewAttachedToWindow(View view) {}

                  @Override
                  public void onViewDetachedFromWindow(View view) {
                    touchDelegateGroup.removeTouchDelegate(touchDelegate);
                    view.removeOnAttachStateChangeListener(this);
                  }
                });
          }
        });
  }

  /**
   * Ensure that the given view has a TouchDelegateGroup. A new TouchDelegateGroup is created if one
   * does not exist for the view already. An existing TouchDelegate is retained and inserted into
   * the new group.
   */
  public static TouchDelegateGroup getOrCreateTouchDelegateGroup(View ancestor) {
    TouchDelegateGroup touchDelegateGroup;
    TouchDelegate existingTouchDelegate = ancestor.getTouchDelegate();
    if (existingTouchDelegate != null) {
      if (existingTouchDelegate instanceof TouchDelegateGroup) {
        touchDelegateGroup = (TouchDelegateGroup) existingTouchDelegate;
      } else {
        touchDelegateGroup = new TouchDelegateGroup(ancestor);
        touchDelegateGroup.addTouchDelegate(existingTouchDelegate);
      }
    } else {
      touchDelegateGroup = new TouchDelegateGroup(ancestor);
    }
    return touchDelegateGroup;
  }

  /** Return the first ancestor of the given View matching the ancestorId. */
  @Nullable
  public static View findViewAncestor(View view, int ancestorId) {
    View parent = view;
    while (parent != null && parent.getId() != ancestorId) {
      if (!(parent.getParent() instanceof View)) {
        return null;
      }
      parent = (View) parent.getParent();
    }
    return parent;
  }
}
