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

/*
 * NOTE: THIS CLASS IS AUTO-GENERATED FROM THE EQUIVALENT CLASS IN THE PARENT TRANSITION PACKAGE.
 * IT SHOULD NOT BE EDITED DIRECTLY.
 */
package com.google.android.material.transition.platform;

/**
 * A {@link android.transition.Visibility} transition that scales the size of a surface up or down
 * to emphasize elevation changes.
 *
 * <p>This can be useful as an exit transition and reenter transition in conjunction with the {@link
 * MaterialContainerTransform}.
 */
@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
public final class MaterialElevationScale extends MaterialVisibility<ScaleProvider> {

  private static final float DEFAULT_SCALE = 0.85f;

  private final boolean growing;

  public MaterialElevationScale(boolean growing) {
    super(createPrimaryAnimatorProvider(growing), createSecondaryAnimatorProvider());
    this.growing = growing;
  }

  public boolean isGrowing() {
    return growing;
  }

  private static ScaleProvider createPrimaryAnimatorProvider(boolean growing) {
    ScaleProvider scaleProvider = new ScaleProvider(growing);
    scaleProvider.setOutgoingEndScale(DEFAULT_SCALE);
    scaleProvider.setIncomingStartScale(DEFAULT_SCALE);
    return scaleProvider;
  }

  private static VisibilityAnimatorProvider createSecondaryAnimatorProvider() {
    return new FadeProvider();
  }
}
