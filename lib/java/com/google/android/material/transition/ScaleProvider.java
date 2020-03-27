/*
 * Copyright 2020 The Android Open Source Project
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

package com.google.android.material.transition;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewGroup;

/** A class that configures and is able to provide an {@link Animator} that scales a view. */
@RequiresApi(VERSION_CODES.LOLLIPOP)
public class ScaleProvider implements VisibilityAnimatorProvider {

  private float outgoingStartScale = 1f;
  private float outgoingEndScale = 1.1f;
  private float incomingStartScale = 0.8f;
  private float incomingEndScale = 1f;

  private boolean entering;
  private boolean scaleOnDisappear = true;

  public ScaleProvider() {
    this(true);
  }

  public ScaleProvider(boolean entering) {
    this.entering = entering;
  }

  public boolean isEntering() {
    return entering;
  }

  public void setEntering(boolean entering) {
    this.entering = entering;
  }

  public boolean isScaleOnDisappear() {
    return scaleOnDisappear;
  }

  public void setScaleOnDisappear(boolean scaleOnDisappear) {
    this.scaleOnDisappear = scaleOnDisappear;
  }

  public float getOutgoingStartScale() {
    return outgoingStartScale;
  }

  public void setOutgoingStartScale(float outgoingStartScale) {
    this.outgoingStartScale = outgoingStartScale;
  }

  public float getOutgoingEndScale() {
    return outgoingEndScale;
  }

  public void setOutgoingEndScale(float outgoingEndScale) {
    this.outgoingEndScale = outgoingEndScale;
  }

  public float getIncomingStartScale() {
    return incomingStartScale;
  }

  public void setIncomingStartScale(float incomingStartScale) {
    this.incomingStartScale = incomingStartScale;
  }

  public float getIncomingEndScale() {
    return incomingEndScale;
  }

  public void setIncomingEndScale(float incomingEndScale) {
    this.incomingEndScale = incomingEndScale;
  }

  @Nullable
  @Override
  public Animator createAppear(
      @NonNull ViewGroup sceneRoot,
      @NonNull View view,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues) {
    if (entering) {
      return createScaleAnimator(view, incomingStartScale, incomingEndScale);
    } else {
      return createScaleAnimator(view, outgoingEndScale, outgoingStartScale);
    }
  }

  @Nullable
  @Override
  public Animator createDisappear(
      @NonNull ViewGroup sceneRoot,
      @NonNull View view,
      @Nullable TransitionValues startValues,
      @Nullable TransitionValues endValues) {
    if (!scaleOnDisappear) {
      return null;
    }

    if (entering) {
      return createScaleAnimator(view, outgoingStartScale, outgoingEndScale);
    } else {
      return createScaleAnimator(view, incomingEndScale, incomingStartScale);
    }
  }

  private static Animator createScaleAnimator(View view, float startScale, float endScale) {
    return ObjectAnimator.ofPropertyValuesHolder(
        view,
        PropertyValuesHolder.ofFloat(View.SCALE_X, startScale, endScale),
        PropertyValuesHolder.ofFloat(View.SCALE_Y, startScale, endScale));
  }
}
