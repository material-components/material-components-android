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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Base Behavior for views that a {@link ExpandableWidget} can transform into. Reacts to {@link
 * ExpandableWidget#setExpanded(boolean)} state changes with an animation.
 */
public abstract class ExpandableTransformationBehavior extends Behavior<View> {

  /** Unknown expanded state. */
  private static final int UNKNOWN = 0;
  /** Expanded state. */
  private static final int EXPANDED = 1;
  /** Collapsed state. */
  private static final int COLLAPSED = 2;

  @IntDef({UNKNOWN, EXPANDED, COLLAPSED})
  @Retention(RetentionPolicy.SOURCE)
  private @interface State {}

  @State private int lastKnownState = UNKNOWN;
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
    if (!didChangeState(expanded)) {
      return false;
    }

    lastKnownState = expanded ? EXPANDED : COLLAPSED;

    if (currentAnimation != null) {
      currentAnimation.cancel();
    }

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
    if (!didChangeState(expanded)) {
      return;
    }

    lastKnownState = expanded ? EXPANDED : COLLAPSED;

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

  private boolean didChangeState(boolean expanded) {
    return (expanded && ((lastKnownState == UNKNOWN) || (lastKnownState == COLLAPSED)))
        || (!expanded && ((lastKnownState == UNKNOWN) || (lastKnownState == EXPANDED)));
  }
}
