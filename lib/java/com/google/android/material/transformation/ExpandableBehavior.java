/*
 * Copyright 2017 The Android Open Source Project
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
package com.google.android.material.transformation;

import android.content.Context;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import androidx.annotation.CallSuper;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior;
import com.google.android.material.expandable.ExpandableWidget;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Base Behavior for views that can react to an {@link ExpandableWidget}'s {@link
 * ExpandableWidget#setExpanded(boolean)} state changes.
 *
 * @deprecated Use {@link com.google.android.material.transition.MaterialContainerTransform}
 *     instead.
 */
@Deprecated
public abstract class ExpandableBehavior extends Behavior<View> {

  /** Uninitialized expanded state. */
  private static final int STATE_UNINITIALIZED = 0;
  /** Expanded state. */
  private static final int STATE_EXPANDED = 1;
  /** Collapsed state. */
  private static final int STATE_COLLAPSED = 2;

  @IntDef({STATE_UNINITIALIZED, STATE_EXPANDED, STATE_COLLAPSED})
  @Retention(RetentionPolicy.SOURCE)
  private @interface State {}

  /**
   * The current expanded state of this behavior. This state follows the expanded state of the
   * {@link com.google.android.material.expandable.ExpandableWidget} dependency, and is updated in
   * {@link #onLayoutChild(CoordinatorLayout, View, int)} and {@link
   * #onDependentViewChanged(CoordinatorLayout, View, View)}.
   *
   * <p>This state may be {@link #STATE_UNINITIALIZED} before either of those callbacks have been
   * invoked.
   */
  @State private int currentState = STATE_UNINITIALIZED;

  public ExpandableBehavior() {}

  public ExpandableBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public abstract boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency);

  /**
   * Reacts to a change in expanded state. This callback is guaranteed to be called only once even
   * if {@link com.google.android.material.expandable.ExpandableWidget#setExpanded(boolean)} is
   * called multiple times with the same value. Upon configuration change, this callback is called
   * with {@code animated} set to false.
   *
   * @param dependency the {@link com.google.android.material.expandable.ExpandableWidget}
   *     dependency containing the new expanded state.
   * @param child the view that should react to the change in expanded state.
   * @param expanded the new expanded state.
   * @param animated true if {@link
   *     com.google.android.material.expandable.ExpandableWidget#setExpanded(boolean)} was called,
   *     false if restoring from a configuration change.
   * @return true if the Behavior changed the child view's size or position, false otherwise.
   */
  protected abstract boolean onExpandedStateChange(
      View dependency, View child, boolean expanded, boolean animated);

  @CallSuper
  @Override
  public boolean onLayoutChild(
      @NonNull CoordinatorLayout parent, @NonNull final View child, int layoutDirection) {
    if (!ViewCompat.isLaidOut(child)) {
      final ExpandableWidget dep = findExpandableWidget(parent, child);
      if (dep != null && didStateChange(dep.isExpanded())) {
        currentState = dep.isExpanded() ? STATE_EXPANDED : STATE_COLLAPSED;
        @State final int expectedState = currentState;
        child
            .getViewTreeObserver()
            .addOnPreDrawListener(
                new OnPreDrawListener() {
                  @Override
                  public boolean onPreDraw() {
                    child.getViewTreeObserver().removeOnPreDrawListener(this);
                    // Proceed only if the state did not change while we're waiting for pre-draw.
                    if (currentState == expectedState) {
                      onExpandedStateChange((View) dep, child, dep.isExpanded(), false);
                    }
                    return false;
                  }
                });
      }
    }

    return false;
  }

  @CallSuper
  @Override
  public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
    ExpandableWidget dep = (ExpandableWidget) dependency;
    boolean expanded = dep.isExpanded();
    if (didStateChange(expanded)) {
      currentState = dep.isExpanded() ? STATE_EXPANDED : STATE_COLLAPSED;
      return onExpandedStateChange((View) dep, child, dep.isExpanded(), true);
    }

    return false;
  }

  @Nullable
  protected ExpandableWidget findExpandableWidget(
      @NonNull CoordinatorLayout parent, @NonNull View child) {
    List<View> dependencies = parent.getDependencies(child);
    for (int i = 0, size = dependencies.size(); i < size; i++) {
      View dependency = dependencies.get(i);
      if (layoutDependsOn(parent, child, dependency)) {
        return (ExpandableWidget) dependency;
      }
    }
    return null;
  }

  private boolean didStateChange(boolean expanded) {
    if (expanded) {
      // Can expand from uninitialized or collapsed state.
      return currentState == STATE_UNINITIALIZED || currentState == STATE_COLLAPSED;
    } else {
      // Can only collapse from expanded state. Uninitialized is equivalent to collapsed state.
      return currentState == STATE_EXPANDED;
    }
  }

  /**
   * A utility function to get the {@link ExpandableBehavior} attached to the {@code view}.
   *
   * @param view The {@link View} that the {@link ExpandableBehavior} is attached to.
   * @param klass The expected {@link Class} of the attached {@link ExpandableBehavior}.
   * @return The {@link ExpandableBehavior} attached to the {@code view}.
   */
  @Nullable
  public static <T extends ExpandableBehavior> T from(@NonNull View view, @NonNull Class<T> klass) {
    ViewGroup.LayoutParams params = view.getLayoutParams();
    if (!(params instanceof CoordinatorLayout.LayoutParams)) {
      throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
    }
    CoordinatorLayout.Behavior<?> behavior =
        ((CoordinatorLayout.LayoutParams) params).getBehavior();
    if (!(behavior instanceof ExpandableBehavior)) {
      throw new IllegalArgumentException("The view is not associated with ExpandableBehavior");
    }
    return klass.cast(behavior);
  }
}
