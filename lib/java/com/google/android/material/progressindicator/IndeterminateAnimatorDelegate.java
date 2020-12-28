/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.material.progressindicator;

import android.animation.Animator;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback;

/** A delegate abstract class for animating properties used in drawing the graphics. */
abstract class IndeterminateAnimatorDelegate<T extends Animator> {

  // The drawable associated with this delegate.
  protected IndeterminateDrawable drawable;
  // A float array of numbers in [0, 1] representing the positions of ends of each segment on the
  // track.
  protected final float[] segmentPositions;
  // An integer array of displayed colors used for each indicator segment defined by every two
  // segment positions.
  protected final int[] segmentColors;

  /**
   * This constructor should be overridden with other necessary actions, e.g. instantiating the
   * animator.
   */
  protected IndeterminateAnimatorDelegate(int segmentCount) {
    this.segmentPositions = new float[segmentCount * 2];
    this.segmentColors = new int[segmentCount];
  }

  /** Registers the drawable associated to this delegate. */
  protected void registerDrawable(@NonNull IndeterminateDrawable drawable) {
    this.drawable = drawable;
  }

  /** Starts the animator. */
  abstract void startAnimator();

  /** Cancels the animator immediately. */
  abstract void cancelAnimatorImmediately();

  /** Requests to cancel the main animator after the current cycle finishes. */
  abstract void requestCancelAnimatorAfterCurrentCycle();

  /**
   * Resets all properties controlled by the animator for a fresh start. This should be called after
   * the drawable is hidden or before the drawable becomes visible.
   */
  abstract void resetPropertiesForNewStart();

  /**
   * Resets all properties controlled by the animator for the next animation cycle. This should be
   * called between the animation cycles.
   */
  abstract void resetPropertiesForNextCycle();

  /**
   * Invalidates the spec values used by the animator delegate. When the spec values are changed in
   * indicator class, values assigned to animators or segments don't get updated until they are
   * explicitly reset. Call this to apply the changes immediately.
   */
  public abstract void invalidateSpecValues();

  /**
   * Registers an {@link AnimationCallback} to the animator for the process what needs to be done
   * after the current animation cycle.
   *
   * @param callback Callback to execute the process at the end of current animation cycle. Note:
   *     only {@link AnimationCallback#onAnimationEnd(Drawable)} should be overridden. Overriding
   *     other events may cause undesired result.
   */
  public abstract void registerAnimatorsCompleteCallback(@NonNull AnimationCallback callback);

  /**
   * Unregisters the {@link AnimationCallback} for the process to be done after the current
   * animation cycle.
   */
  public abstract void unregisterAnimatorsCompleteCallback();
}
