/*
 * Copyright 2022 The Android Open Source Project
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

package io.material.catalog.carousel;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.StartCarouselConfiguration;
import com.google.android.material.materialswitch.MaterialSwitch;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the multi-browse variants of the Carousel. */
public class MultiBrowseDemoFragment extends DemoFragment {

  @NonNull
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    return layoutInflater.inflate(
        R.layout.cat_carousel_multi_browse_fragment, viewGroup, false /* attachToRoot */);
  }

  @Override
  @SuppressWarnings("RestrictTo")
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);

    MaterialSwitch debugSwitch = view.findViewById(R.id.debug_switch);
    MaterialSwitch forceCompactSwitch = view.findViewById(R.id.force_compact_arrangement_switch);

    // A start-aligned multi-browse carousel
    RecyclerView multiBrowseStartRecyclerView =
        view.findViewById(R.id.multi_browse_start_carousel_recycler_view);
    CarouselLayoutManager multiBrowseStartCarouselLayoutManager = new CarouselLayoutManager();
    multiBrowseStartCarouselLayoutManager.setCarouselConfiguration(
        new StartCarouselConfiguration(multiBrowseStartCarouselLayoutManager));
    multiBrowseStartCarouselLayoutManager.setDrawDebugEnabled(
        multiBrowseStartRecyclerView, debugSwitch.isChecked());
    multiBrowseStartRecyclerView.setLayoutManager(multiBrowseStartCarouselLayoutManager);
    multiBrowseStartRecyclerView.setNestedScrollingEnabled(false);

    debugSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          multiBrowseStartRecyclerView.setBackgroundResource(
              isChecked ? R.drawable.dashed_outline_rectangle : 0);
          multiBrowseStartCarouselLayoutManager.setDrawDebugEnabled(
              multiBrowseStartRecyclerView, isChecked);
        });

    forceCompactSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) ->
            multiBrowseStartCarouselLayoutManager.setCarouselConfiguration(
                new StartCarouselConfiguration(multiBrowseStartCarouselLayoutManager, isChecked)));

    CarouselAdapter adapter =
        new CarouselAdapter(
            (item, position) -> multiBrowseStartRecyclerView.scrollToPosition(position));

    multiBrowseStartRecyclerView.setAdapter(adapter);
    adapter.submitList(CarouselData.createItems());
  }
}
