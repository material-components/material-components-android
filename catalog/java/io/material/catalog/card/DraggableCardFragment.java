/*
 * Copyright 2018 The Android Open Source Project
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

package io.material.catalog.card;

import io.material.catalog.R;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.card.MaterialCardView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import io.material.catalog.draggable.DraggableCoordinatorLayout;
import io.material.catalog.feature.DemoFragment;

/** A fragment with a draggable MaterialCardView */
public class DraggableCardFragment extends DemoFragment {

  private static final int RECOMMENDED_DRAG_ELEVATION = 8;

  @Override
  public int getDemoTitleResId() {
    return R.string.cat_card_draggable_card;
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_card_draggable_demo, viewGroup, false /* attachToRoot */);
    DraggableCoordinatorLayout container = (DraggableCoordinatorLayout) view;
    final MaterialCardView card = view.findViewById(R.id.draggable_card);
    container.addDraggableChild(card);

    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
      return view;
    }

    // Add animations
    final ObjectAnimator dragAnimation =
        ObjectAnimator.ofFloat(
            card,
            View.TRANSLATION_Z,
            dpToPx(RECOMMENDED_DRAG_ELEVATION, getContext()) - card.getCardElevation());

    container.setViewDragListener(
        new DraggableCoordinatorLayout.ViewDragListener() {
          @Override
          public void onViewCaptured(@NonNull View view, int i) {
            dragAnimation.start();
          }

          @Override
          public void onViewReleased(@NonNull View view, float v, float v1) {
            dragAnimation.reverse();
          }
        });

    return view;
  }

  /** Returns the value given in dp converted to pixels. */
  private static int dpToPx(float dp, Context context) {
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
  }
}
