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
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.transition.Visibility;
import com.google.android.material.animation.AnimationUtils;

/**
 * A {@link TransitionSet} that provides a fade with a scale of incoming content and a simple fade
 * of outgoing content.
 */
@RequiresApi(VERSION_CODES.LOLLIPOP)
public class MaterialFade extends MaterialTransitionSet<Fade> {

  private static final long DEFAULT_DURATION_ENTER = 150;
  private static final long DEFAULT_DURATION_ENTER_FADE = 45;
  private static final long DEFAULT_DURATION_RETURN = 75;
  private static final float DEFAULT_START_SCALE = 0.8f;

  @NonNull
  public static MaterialFade create(@NonNull Context context) {
    return create(context, true);
  }

  @NonNull
  public static MaterialFade create(@NonNull Context context, boolean entering) {
    MaterialFade materialFade = new MaterialFade(entering);
    materialFade.initialize(context);
    if (entering) {
      // Must be done after adding the transition to the set to avoid the duration overwrite.
      materialFade.getPrimaryTransition().setDuration(DEFAULT_DURATION_ENTER_FADE);
    }
    return materialFade;
  }

  private MaterialFade(boolean entering) {
    setDuration(entering ? DEFAULT_DURATION_ENTER : DEFAULT_DURATION_RETURN);
    setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
  }

  @NonNull
  @Override
  Fade getDefaultPrimaryTransition() {
    return new Fade();
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
