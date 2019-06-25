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

package io.material.catalog.elevation;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import com.google.android.material.internal.ViewUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.material.card.MaterialCardView;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A fragment that displays the main Elevation demo for the Catalog app. */
public class ElevationMainDemoFragment extends DemoFragment {
  protected int currentElevation;
  protected int elevationDP;
  protected int[] elevationValues;

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getDemoContent(), viewGroup, false /* attachToRoot */);
    setUpElevationValues();
    setUp(view);
    return view;
  }

  @LayoutRes
  protected int getDemoContent() {
    return R.layout.cat_elevation_shadows_fragment;
  }

  @SuppressWarnings("RestrictTo") // It's safe to use restricted MDC code in MDC Catalog.
  protected void updateElevationLevel(View view, int newLevel) {
    List<MaterialCardView> elevationCards =
        DemoUtils.findViewsWithType(view, MaterialCardView.class);

    TextView levelText = view.findViewById(R.id.current_elevation_level);

    if (newLevel >= 0 && newLevel <= 7) {
      currentElevation = newLevel;
      elevationDP = elevationValues[currentElevation];
      for (MaterialCardView elevationCard : elevationCards) {
        elevationCard.setCardElevation(ViewUtils.dpToPx(view.getContext(), elevationDP));
      }
      levelText.setText(
          getResources().getString(R.string.cat_elevation_fragment_level, elevationDP));
    }
  }

  protected void setUp(View view) {
    currentElevation = 0;

    Button increaseButton = view.findViewById(R.id.increase_elevation);
    Button decreaseButton = view.findViewById(R.id.decrease_elevation);

    elevationDP = elevationValues[currentElevation];

    increaseButton.setOnClickListener(button -> updateElevationLevel(view, currentElevation + 1));
    decreaseButton.setOnClickListener(button -> updateElevationLevel(view, currentElevation - 1));
    updateElevationLevel(view, 0);
  }

  protected void setUpElevationValues() {
    elevationValues = getResources().getIntArray(R.array.cat_elevation_values);
  }
}
