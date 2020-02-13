/*
 * Copyright 2019 The Android Open Source Project
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

import android.content.Context;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.transition.Visibility;
import com.google.android.material.animation.AnimationUtils;

/**
 * A {@link TransitionSet} that, by default, provides a fade in and scale out when appearing and a
 * fade out and scale out when disappearing.
 */
@RequiresApi(VERSION_CODES.LOLLIPOP)
public class MaterialFadeThrough extends MaterialTransitionSet<FadeThrough> {

  private static final float DEFAULT_START_SCALE = 0.92f;

  @NonNull
  public static MaterialFadeThrough create(@NonNull Context context) {
    MaterialFadeThrough materialFadeThrough = new MaterialFadeThrough();
    materialFadeThrough.initialize(context);
    return materialFadeThrough;
  }

  private MaterialFadeThrough() {
    setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
  }

  @NonNull
  @Override
  FadeThrough getDefaultPrimaryTransition() {
    return new FadeThrough();
  }

  @Nullable
  @Override
  Transition getDefaultSecondaryTransition() {
    Scale scale = new Scale();
    scale.setMode(Visibility.MODE_IN);
    scale.setIncomingStartScale(DEFAULT_START_SCALE);
    return scale;
  }
}
