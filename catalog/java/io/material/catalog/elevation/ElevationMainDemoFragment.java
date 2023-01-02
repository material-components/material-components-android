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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.ArrayRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.internal.ViewUtils;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A fragment that displays the main Elevation demo for the Catalog app. */
public class ElevationMainDemoFragment extends DemoFragment {
  protected int currentElevationLevel = 0;
  protected int elevationInDp;
  private int[] elevationLevelValues;

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getDemoContent(), viewGroup, false /* attachToRoot */);

    elevationLevelValues = getResources().getIntArray(getElevationLevelValues());

    Button increaseButton = view.findViewById(R.id.increase_elevation);
    Button decreaseButton = view.findViewById(R.id.decrease_elevation);

    increaseButton.setOnClickListener(
        button -> updateCardsElevationLevel(view, currentElevationLevel + 1));
    decreaseButton.setOnClickListener(
        button -> updateCardsElevationLevel(view, currentElevationLevel - 1));

    updateCardsElevationLevel(view, currentElevationLevel);
    return view;
  }

  @LayoutRes
  private int getDemoContent() {
    return R.layout.cat_elevation_fragment;
  }

  @SuppressWarnings("RestrictTo") // It's safe to use restricted MDC code in MDC Catalog.
  private void updateCardsElevationLevel(View view, int newElevationLevel) {
    List<MaterialCardView> elevationCards =
        DemoUtils.findViewsWithType(view, MaterialCardView.class);

    if (newElevationLevel >= 0 && newElevationLevel <= getMaxElevationValue()) {
      setElevationLevel(newElevationLevel);
      for (MaterialCardView elevationCard : elevationCards) {
        elevationCard.setCardElevation(ViewUtils.dpToPx(view.getContext(), elevationInDp));
      }
      setElevationLevelTextView(view, getElevationLevelText());
    }
  }

  private void setElevationLevel(int newElevationLevel) {
    currentElevationLevel = newElevationLevel;
    elevationInDp = elevationLevelValues[currentElevationLevel];
  }

  private static void setElevationLevelTextView(View view, @NonNull String levelText) {
    TextView levelTextView = view.findViewById(R.id.current_elevation_level_label);
    levelTextView.setText(levelText);
  }

  protected String getElevationLevelText() {
    return getResources().getString(R.string.cat_elevation_fragment_level, elevationInDp);
  }

  @ArrayRes
  protected int getElevationLevelValues() {
    return R.array.cat_elevation_level_values;
  }

  private int getMaxElevationValue() {
    return elevationLevelValues.length - 1;
  }
}
