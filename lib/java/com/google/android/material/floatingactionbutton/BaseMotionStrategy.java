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

package com.google.android.material.floatingactionbutton;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.content.Context;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Preconditions;
import android.view.View;
import com.google.android.material.animation.AnimatorSetCompat;
import com.google.android.material.animation.MotionSpec;
import java.util.ArrayList;
import java.util.List;

/** Common functionality for all classes implementing {@link MotionStrategy} */
abstract class BaseMotionStrategy implements MotionStrategy {

  private final Context context;
  @NonNull private final ExtendedFloatingActionButton fab;
  private final ArrayList<AnimatorListener> listeners = new ArrayList<>();
  private final AnimatorTracker tracker;

  @Nullable private MotionSpec defaultMotionSpec;
  @Nullable private MotionSpec motionSpec;

  BaseMotionStrategy(@NonNull ExtendedFloatingActionButton fab, AnimatorTracker tracker) {
    this.fab = fab;
    this.context = fab.getContext();
    this.tracker = tracker;
  }

  @Override
  public final void setMotionSpec(@Nullable MotionSpec motionSpec) {
    this.motionSpec = motionSpec;
  }

  @Override
  public final MotionSpec getCurrentMotionSpec() {
    if (motionSpec != null) {
      return motionSpec;
    }

    if (defaultMotionSpec == null) {
      defaultMotionSpec =
          MotionSpec.createFromResource(
              context,
              getDefaultMotionSpecResource());
    }

    return Preconditions.checkNotNull(defaultMotionSpec);
  }

  @Override
  public final void addAnimationListener(@NonNull AnimatorListener listener) {
    listeners.add(listener);
  }

  @Override
  public final void removeAnimationListener(@NonNull AnimatorListener listener) {
    listeners.remove(listener);
  }

  @NonNull
  @Override
  public final List<AnimatorListener> getListeners() {
    return listeners;
  }

  @Override
  @Nullable
  public MotionSpec getMotionSpec() {
    return motionSpec;
  }

  @Override
  @CallSuper
  public void onAnimationStart(Animator animator) {
    tracker.onNextAnimationStart(animator);
  }

  @Override
  @CallSuper
  public void onAnimationEnd() {
    tracker.clear();
  }

  @Override
  @CallSuper
  public void onAnimationCancel() {
    tracker.clear();
  }

  @Override
  public AnimatorSet createAnimator() {
    return createAnimator(getCurrentMotionSpec());
  }

  @NonNull
  AnimatorSet createAnimator(@NonNull MotionSpec spec) {
    List<Animator> animators = new ArrayList<>();

    if (spec.hasPropertyValues("opacity")) {
      animators.add(spec.getAnimator("opacity", fab, View.ALPHA));
    }

    if (spec.hasPropertyValues("scale")) {
      animators.add(spec.getAnimator("scale", fab, View.SCALE_Y));
      animators.add(spec.getAnimator("scale", fab, View.SCALE_X));
    }

    if (spec.hasPropertyValues("width")) {
      animators.add(spec.getAnimator("width", fab, ExtendedFloatingActionButton.WIDTH));
    }

    if (spec.hasPropertyValues("height")) {
      animators.add(spec.getAnimator("height", fab, ExtendedFloatingActionButton.HEIGHT));
    }

    AnimatorSet set = new AnimatorSet();
    AnimatorSetCompat.playTogether(set, animators);
    return set;
  }
}
