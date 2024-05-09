/*
 * Copyright 2019 The Android Open Source Project
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

package io.material.catalog.elevation;

import io.material.catalog.R;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import com.google.android.material.shape.MaterialShapeDrawable;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the Elevation animation demo for the Catalog app. */
public class ElevationAnimationDemoFragment extends DemoFragment {

  private static final long ANIMATION_DURATION_MILLIS = 2000;
  private static final long ANIMATION_START_DELAY_MILLIS = 1000;

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_elevation_animation_fragment, viewGroup, false /* attachToRoot */);

    TextView translationZLabelView = view.findViewById(R.id.translation_z_label);

    float maxTranslationZ = getResources().getDimension(R.dimen.cat_elevation_max_translation_z);
    int maxTranslationZDp = (int) (maxTranslationZ / getResources().getDisplayMetrics().density);
    translationZLabelView.setText(
        getString(R.string.cat_elevation_animation_label, maxTranslationZDp));

    MaterialShapeDrawable materialShapeDrawable =
        MaterialShapeDrawable.createWithElevationOverlay(view.getContext());
    view.setBackground(materialShapeDrawable);
    setTranslationZ(view, materialShapeDrawable, maxTranslationZ);
    startTranslationZAnimation(view, materialShapeDrawable, maxTranslationZ);

    return view;
  }

  private void startTranslationZAnimation(
      View view, MaterialShapeDrawable materialShapeDrawable, float maxTranslationZ) {
    ValueAnimator animator = ValueAnimator.ofFloat(0, maxTranslationZ);
    animator.setDuration(ANIMATION_DURATION_MILLIS);
    animator.setStartDelay(ANIMATION_START_DELAY_MILLIS);
    animator.setRepeatMode(ValueAnimator.RESTART);
    animator.setRepeatCount(ValueAnimator.INFINITE);
    animator.addUpdateListener(
        animation ->
            setTranslationZ(view, materialShapeDrawable, (float) animation.getAnimatedValue()));
    animator.start();
  }

  private void setTranslationZ(
      View view, MaterialShapeDrawable materialShapeDrawable, float translationZ) {
    materialShapeDrawable.setTranslationZ(translationZ);
    ViewCompat.setTranslationZ(view, translationZ);
  }
}
