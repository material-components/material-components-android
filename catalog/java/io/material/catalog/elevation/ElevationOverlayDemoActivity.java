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

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import com.google.android.material.elevation.ElevationOverlayProvider;
import com.google.android.material.internal.ViewUtils;
import io.material.catalog.feature.DemoActivity;
import java.util.Locale;

/** A fragment that displays the Elevation Overlay demo for the Catalog app. */
public class ElevationOverlayDemoActivity extends DemoActivity {

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_elevation_overlay_activity, viewGroup, /* attachToRoot= */ false);

    RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
    recyclerView.setAdapter(new Adapter(getElevationDpValues()));
    recyclerView.setLayoutManager(
        new LinearLayoutManager(this, RecyclerView.VERTICAL, /* reverseLayout= */ false));

    return view;
  }

  @Override
  public int getDemoTitleResId() {
    return R.string.cat_elevation_overlay_title;
  }

  protected int[] getElevationDpValues() {
    return new int[] {1, 2, 3, 4, 6, 8, 12, 16, 24};
  }

  private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int[] elevationDpValues;

    public Adapter(int[] elevationDpValues) {
      this.elevationDpValues = elevationDpValues;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      View view =
          inflater.inflate(R.layout.cat_elevation_overlay_item, parent, /* attachToRoot= */ false);
      return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
      ((ItemViewHolder) viewHolder).bind(elevationDpValues[position]);
    }

    @Override
    public int getItemCount() {
      return elevationDpValues.length;
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

      private final ElevationOverlayProvider overlayProvider;

      private final TextView dpLabelView;
      private final TextView alphaLabelView;

      private ItemViewHolder(View itemView) {
        super(itemView);
        overlayProvider = new ElevationOverlayProvider(itemView.getContext());
        dpLabelView = itemView.findViewById(R.id.elevation_overlay_dp_label);
        alphaLabelView = itemView.findViewById(R.id.elevation_overlay_alpha_label);
      }

      @SuppressWarnings("RestrictTo")
      private void bind(int elevationDp) {
        float elevation = ViewUtils.dpToPx(ElevationOverlayDemoActivity.this, elevationDp);
        int color = overlayProvider.compositeOverlayWithThemeSurfaceColorIfNeeded(elevation);
        int alphaPercent =
            Math.round(overlayProvider.calculateOverlayAlphaFraction(elevation) * 100);

        ViewCompat.setElevation(itemView, elevation);
        itemView.setBackgroundColor(color);
        dpLabelView.setText(String.format(Locale.getDefault(), "%02d dp", elevationDp));
        alphaLabelView.setText(String.format(Locale.getDefault(), "%d%% On Surface", alphaPercent));
      }
    }
  }
}
