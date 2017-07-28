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
package android.support.design.widget.transformation;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.CoordinatorLayout.Behavior;
import android.support.design.widget.expandable.ExpandableWidget;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Base Behavior for views that can react to an {@link ExpandableWidget}'s {@link
 * ExpandableWidget#setExpanded(boolean)} state changes.
 */
public abstract class ExpandableBehavior extends Behavior<View> {

  /** Unknown expanded state. */
  private static final int STATE_UNKNOWN = 0;
  /** Expanded state. */
  private static final int STATE_EXPANDED = 1;
  /** Collapsed state. */
  private static final int STATE_COLLAPSED = 2;

  @IntDef({STATE_UNKNOWN, STATE_EXPANDED, STATE_COLLAPSED})
  @Retention(RetentionPolicy.SOURCE)
  private @interface State {}

  /**
   * The current expanded state of this behavior. This state follows the expanded state of the
   * {@link ExpandableWidget} dependency, and is updated in {@link #onLayoutChild(CoordinatorLayout,
   * View, int)} and {@link #onDependentViewChanged(CoordinatorLayout, View, View)}.
   *
   * <p>This state may be {@link #STATE_UNKNOWN} before either of those callbacks have been invoked.
   */
  @State private int currentState = STATE_UNKNOWN;

  public ExpandableBehavior() {}

  public ExpandableBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public abstract boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency);

  /**
   * Reacts to a change in expanded state. This callback is guaranteed to be called only once even
   * if {@link ExpandableWidget#setExpanded(boolean)} is called multiple times with the same value.
   * Upon configuration change, this callback is called with {@code animated} set to false.
   *
   * @param dependency the {@link ExpandableWidget} dependency containing the new expanded state.
   * @param child the view that should react to the change in expanded state.
   * @param expanded the new expanded state.
   * @param animated true if {@link ExpandableWidget#setExpanded(boolean)} was called, false if
   *     restoring from a configuration change.
   * @return true if the Behavior changed the child view's size or position, false otherwise.
   */
  protected abstract boolean onExpandedStateChange(
      ExpandableWidget dependency, View child, boolean expanded, boolean animated);

  @CallSuper
  @Override
  public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
    if (!ViewCompat.isLaidOut(child)) {
      ExpandableWidget dep = findExpandableWidget(parent, child);
      if (dep != null) {
        currentState = dep.isExpanded() ? STATE_EXPANDED : STATE_COLLAPSED;
        onExpandedStateChange(dep, child, dep.isExpanded(), false);
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
      return onExpandedStateChange(dep, child, dep.isExpanded(), true);
    }

    return false;
  }

  @Nullable
  protected ExpandableWidget findExpandableWidget(CoordinatorLayout parent, View child) {
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
    return (expanded && ((currentState == STATE_UNKNOWN) || (currentState == STATE_COLLAPSED)))
        || (!expanded && ((currentState == STATE_UNKNOWN) || (currentState == STATE_EXPANDED)));
  }

  /**
   * A utility function to get the {@link ExpandableBehavior} attached to the {@code view}.
   *
   * @param view The {@link View} that the {@link ExpandableBehavior} is attached to.
   * @param klass The expected {@link Class} of the attached {@link ExpandableBehavior}.
   * @return The {@link ExpandableBehavior} attached to the {@code view}.
   */
  public static <T extends ExpandableBehavior> T from(View view, Class<T> klass) {
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
