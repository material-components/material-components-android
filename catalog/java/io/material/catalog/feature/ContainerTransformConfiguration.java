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

package io.material.catalog.feature;

import android.view.animation.Interpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

/** Holds the default configurations for the container transform in demos and catalog navigation. */
public class ContainerTransformConfiguration {

  public boolean isArcMotionEnabled() {
    return false;
  }

  public long getEnterDuration() {
    return 300;
  }

  public long getReturnDuration() {
    return 275;
  }

  public Interpolator getInterpolator() {
    return new FastOutSlowInInterpolator();
  }
}
