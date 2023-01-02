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

/*
 * NOTE: THIS CLASS IS AUTO-GENERATED FROM THE EQUIVALENT CLASS IN THE PARENT TRANSITION PACKAGE.
 * IT SHOULD NOT BE EDITED DIRECTLY.
 */
package com.google.android.material.transition.platform;

import com.google.android.material.R;

import android.animation.TimeInterpolator;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import com.google.android.material.animation.AnimationUtils;

/**
 * A {@link android.transition.Visibility} transition that is composed of a fade and scale of
 * incoming content and a simple fade of outgoing content.
 *
 * <p>MaterialFade supports theme-based easing and duration. The transition will load theme values
 * from the {@code SceneRoot}'s context before it runs, and only use them if the corresponding
 * properties weren't already set on the transition instance.
 */
@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
public final class MaterialFade extends MaterialVisibility<FadeProvider> {

  private static final float DEFAULT_START_SCALE = 0.8f;
  private static final float DEFAULT_FADE_END_THRESHOLD_ENTER = 0.3f;

  @AttrRes
  private static final int DEFAULT_THEMED_INCOMING_DURATION_ATTR = R.attr.motionDurationMedium4;

  @AttrRes
  private static final int DEFAULT_THEMED_OUTGOING_DURATION_ATTR = R.attr.motionDurationShort3;

  @AttrRes
  private static final int DEFAULT_THEMED_INCOMING_EASING_ATTR =
      R.attr.motionEasingEmphasizedDecelerateInterpolator;

  @AttrRes
  private static final int DEFAULT_THEMED_OUTGOING_EASING_ATTR =
      R.attr.motionEasingEmphasizedAccelerateInterpolator;

  public MaterialFade() {
    super(createPrimaryAnimatorProvider(), createSecondaryAnimatorProvider());
  }

  private static FadeProvider createPrimaryAnimatorProvider() {
    FadeProvider fadeProvider = new FadeProvider();
    fadeProvider.setIncomingEndThreshold(DEFAULT_FADE_END_THRESHOLD_ENTER);
    return fadeProvider;
  }

  private static VisibilityAnimatorProvider createSecondaryAnimatorProvider() {
    ScaleProvider scaleProvider = new ScaleProvider();
    scaleProvider.setScaleOnDisappear(false);
    scaleProvider.setIncomingStartScale(DEFAULT_START_SCALE);
    return scaleProvider;
  }

  @AttrRes
  @Override
  int getDurationThemeAttrResId(boolean appearing) {
    return appearing
        ? DEFAULT_THEMED_INCOMING_DURATION_ATTR
        : DEFAULT_THEMED_OUTGOING_DURATION_ATTR;
  }

  @AttrRes
  @Override
  int getEasingThemeAttrResId(boolean appearing) {
    return appearing
        ? DEFAULT_THEMED_INCOMING_EASING_ATTR
        : DEFAULT_THEMED_OUTGOING_EASING_ATTR;
  }

  @NonNull
  @Override
  TimeInterpolator getDefaultEasingInterpolator(boolean appearing) {
    return AnimationUtils.LINEAR_INTERPOLATOR;
  }
}
