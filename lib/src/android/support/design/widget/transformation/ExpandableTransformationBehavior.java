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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
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
 * Base Behavior for views that a {@link ExpandableWidget} can transform into. Reacts to {@link
 * ExpandableWidget#setExpanded(boolean)} state changes with an animation.
 */
public abstract class ExpandableTransformationBehavior extends Behavior<View> {

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

  @Nullable private Animator currentAnimation;

  public ExpandableTransformationBehavior() {}

  public ExpandableTransformationBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
    return dependency instanceof ExpandableWidget;
  }

  @Override
  public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
    if (!ViewCompat.isLaidOut(child)) {
      List<View> dependencies = parent.getDependencies(child);
      ExpandableWidget dependency = findExpandableWidget(dependencies);
      jumpToState(dependency, child);
    }

    return false;
  }

  @Override
  public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
    ExpandableWidget dep = (ExpandableWidget) dependency;
    boolean expanded = dep.isExpanded();
    if (!didStateChange(expanded)) {
      return false;
    }

    if (currentAnimation != null) {
      currentAnimation.cancel();
    }

    currentState = expanded ? STATE_EXPANDED : STATE_COLLAPSED;
    currentAnimation = createAnimation(dep, child);
    currentAnimation.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            if (expanded) {
              child.setVisibility(View.VISIBLE);
            }
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            if (!expanded) {
              child.setVisibility(View.INVISIBLE);
            }
            currentAnimation = null;
          }
        });
    currentAnimation.start();
    return true;
  }

  protected void jumpToState(ExpandableWidget dep, View child) {
    boolean expanded = dep.isExpanded();
    if (!didStateChange(expanded)) {
      return;
    }

    currentState = expanded ? STATE_EXPANDED : STATE_COLLAPSED;

    if (expanded) {
      child.setVisibility(View.VISIBLE);
    } else {
      child.setVisibility(View.INVISIBLE);
    }
  }

  protected abstract Animator createAnimation(ExpandableWidget dep, View child);

  private ExpandableWidget findExpandableWidget(List<View> dependencies) {
    for (int i = 0, size = dependencies.size(); i < size; i++) {
      View dependency = dependencies.get(i);
      if (dependency instanceof ExpandableWidget) {
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
   * A utility function to get the {@link ExpandableTransformationBehavior} attached to the {@code
   * view}.
   *
   * @param view The {@link View} that the {@link ExpandableTransformationBehavior} is attached to.
   * @param klass The expected {@link Class} of the attached {@link
   *     ExpandableTransformationBehavior}.
   * @return The {@link ExpandableTransformationBehavior} attached to the {@code view}.
   */
  public static <T extends ExpandableTransformationBehavior> T from(View view, Class<T> klass) {
    ViewGroup.LayoutParams params = view.getLayoutParams();
    if (!(params instanceof CoordinatorLayout.LayoutParams)) {
      throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
    }
    CoordinatorLayout.Behavior<?> behavior =
        ((CoordinatorLayout.LayoutParams) params).getBehavior();
    if (!(behavior instanceof ExpandableTransformationBehavior)) {
      throw new IllegalArgumentException(
          "The view is not associated with ExpandableTransformationBehavior");
    }
    return klass.cast(view);
  }
}
