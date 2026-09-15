/*
 * Copyright 2026 The Android Open Source Project
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
package com.google.android.material.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages multiple animations, including time-based animations, like {@link Animator} and {@link
 * AnimatorSet}, and physics-based animations, like {@link SpringAnimation}, allowing them to be
 * started together and providing callbacks for start and end events.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class AnimationCoordinator {

  /** Listener for animation coordinator events. */
  public interface Listener {
    /** Called before any animation starts. */
    void onAnimationsStart();

    /** Called after all animations have finished. */
    void onAnimationsEnd();
  }

  private final List<Animator> durationAnimations = new ArrayList<>();
  private final List<DynamicAnimation<?>> dynamicAnimations = new ArrayList<>();
  private final List<Listener> listeners = new ArrayList<>();

  private int animationsRunning = 0;
  private boolean started = false;

  public AnimationCoordinator() {}

  /** Adds an {@link Animator} or {@link AnimatorSet} to be managed by this coordinator. */
  public void addAnimator(@NonNull Animator animator) {
    durationAnimations.add(animator);
  }

  /** Adds a {@link DynamicAnimation} to be managed by this coordinator. */
  public void addDynamicAnimation(@NonNull DynamicAnimation<?> dynamicAnimation) {
    dynamicAnimations.add(dynamicAnimation);
  }

  /** Adds a listener to receive animation start and end events. */
  public void addListener(@NonNull Listener listener) {
    listeners.add(listener);
  }

  /** Removes a listener. */
  public void removeListener(@NonNull Listener listener) {
    listeners.remove(listener);
  }

  /** Clears all animations and listeners. */
  public void clear() {
    List<Animator> animatorsToEnd = new ArrayList<>(durationAnimations);
    durationAnimations.clear();
    for (Animator animator : animatorsToEnd) {
      animator.end();
    }

    List<DynamicAnimation<?>> dynamicAnimsToClear = new ArrayList<>(dynamicAnimations);
    dynamicAnimations.clear();
    for (DynamicAnimation<?> dynamicAnimation : dynamicAnimsToClear) {
      if (dynamicAnimation instanceof SpringAnimation) {
        SpringAnimation springAnimation = (SpringAnimation) dynamicAnimation;
        if (springAnimation.canSkipToEnd()) {
          springAnimation.skipToEnd();
        } else {
          springAnimation.cancel();
        }
      } else {
        dynamicAnimation.cancel();
      }
    }

    listeners.clear();
    animationsRunning = 0;
    started = false;
  }

  /**
   * Starts all managed animations simultaneously. If animations are already running, this method
   * does nothing.
   */
  public void start() {
    if (started) {
      return;
    }
    started = true;

    for (Listener listener : listeners) {
      listener.onAnimationsStart();
    }

    animationsRunning = dynamicAnimations.size();
    if (!durationAnimations.isEmpty()) {
      animationsRunning++;
    }

    if (animationsRunning == 0) {
      notifyAnimationsEnd();
      return;
    }

    DynamicAnimation.OnAnimationEndListener dynamicListener =
        new DynamicAnimation.OnAnimationEndListener() {
          @SuppressWarnings("rawtypes") // interface is defined using raw type
          @Override
          public void onAnimationEnd(
              DynamicAnimation animation, boolean canceled, float value, float velocity) {
            animation.removeEndListener(this);
            onAnimationFinished();
          }
        };
    for (DynamicAnimation<?> dynamicAnimation : dynamicAnimations) {
      dynamicAnimation.addEndListener(dynamicListener);
      dynamicAnimation.start();
    }

    if (!durationAnimations.isEmpty()) {
      AnimatorSet animatorSet = new AnimatorSet();
      AnimatorSetCompat.playTogether(animatorSet, new ArrayList<>(durationAnimations));
      animatorSet.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              onAnimationFinished();
            }
          });
      animatorSet.start();
    }
  }

  private void onAnimationFinished() {
    animationsRunning--;
    if (animationsRunning == 0) {
      notifyAnimationsEnd();
    }
  }

  private void notifyAnimationsEnd() {
    for (Listener listener : listeners) {
      listener.onAnimationsEnd();
    }
    started = false;
  }
}
