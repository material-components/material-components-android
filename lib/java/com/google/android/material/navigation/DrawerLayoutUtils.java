/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.google.android.material.navigation;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Color;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.graphics.ColorUtils;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.animation.AnimationUtils;

/**
 * Class for utilities related to {@link DrawerLayout}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class DrawerLayoutUtils {

  private static final int DEFAULT_SCRIM_COLOR = 0x99000000;
  private static final int DEFAULT_SCRIM_ALPHA = Color.alpha(DEFAULT_SCRIM_COLOR);

  private DrawerLayoutUtils() {}

  /**
   * Returns an {@link AnimatorUpdateListener} that fades out the {@link DrawerLayout} scrim color.
   */
  @NonNull
  public static AnimatorUpdateListener getScrimCloseAnimatorUpdateListener(
      @NonNull DrawerLayout drawerLayout) {
    return animation -> {
      int newScrimAlpha =
          AnimationUtils.lerp(DEFAULT_SCRIM_ALPHA, 0, animation.getAnimatedFraction());
      drawerLayout.setScrimColor(ColorUtils.setAlphaComponent(DEFAULT_SCRIM_COLOR, newScrimAlpha));
    };
  }

  /**
   * Returns an {@link AnimatorListener} that resets the {@link DrawerLayout} scrim color and closes
   * the drawer immediately, on animation end.
   */
  @NonNull
  public static AnimatorListener getScrimCloseAnimatorListener(
      @NonNull DrawerLayout drawerLayout, @NonNull View drawerView) {
    return new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        drawerLayout.closeDrawer(drawerView, false);
        drawerLayout.setScrimColor(DEFAULT_SCRIM_COLOR);
      }
    };
  }
}
