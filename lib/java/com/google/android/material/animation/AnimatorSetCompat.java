/*
 * Copyright 2017 The Android Open Source Project
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
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.util.List;

/**
 * Compatibility utils for {@link android.animation.AnimatorSet}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class AnimatorSetCompat {

  /** Sets up this AnimatorSet to play all of the supplied animations at the same time. */
  public static void playTogether(@NonNull AnimatorSet animatorSet, @NonNull List<Animator> items) {
    // Fix for pre-M bug where animators with start delay are not played correctly in an
    // AnimatorSet.
    long totalDuration = 0;
    for (int i = 0, count = items.size(); i < count; i++) {
      Animator animator = items.get(i);
      totalDuration = Math.max(totalDuration, animator.getStartDelay() + animator.getDuration());
    }
    Animator fix = ValueAnimator.ofInt(0, 0);
    fix.setDuration(totalDuration);
    items.add(0, fix);

    animatorSet.playTogether(items);
  }
}
