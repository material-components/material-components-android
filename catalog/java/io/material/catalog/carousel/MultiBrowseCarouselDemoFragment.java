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
import android.widget.AutoCompleteTextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.CarouselSnapHelper;
import com.google.android.material.carousel.MultiBrowseCarouselStrategy;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.slider.Slider.OnSliderTouchListener;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the multi-browse variants of the Carousel. */
public class MultiBrowseCarouselDemoFragment extends DemoFragment {

  private MaterialDividerItemDecoration horizontalDivider;

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

    horizontalDivider =
        new MaterialDividerItemDecoration(
            requireContext(), MaterialDividerItemDecoration.HORIZONTAL);

    MaterialSwitch debugSwitch = view.findViewById(R.id.debug_switch);
    MaterialSwitch forceCompactSwitch = view.findViewById(R.id.force_compact_arrangement_switch);
    MaterialSwitch drawDividers = view.findViewById(R.id.draw_dividers_switch);
    MaterialSwitch snapSwitch = view.findViewById(R.id.snap_switch);
    AutoCompleteTextView itemCountDropdown = view.findViewById(R.id.item_count_dropdown);
    Slider positionSlider = view.findViewById(R.id.position_slider);

    // A start-aligned multi-browse carousel
    RecyclerView multiBrowseStartRecyclerView =
        view.findViewById(R.id.multi_browse_start_carousel_recycler_view);
    CarouselLayoutManager multiBrowseStartCarouselLayoutManager = new CarouselLayoutManager();
    multiBrowseStartCarouselLayoutManager.setDebuggingEnabled(
        multiBrowseStartRecyclerView, debugSwitch.isChecked());
    multiBrowseStartRecyclerView.setLayoutManager(multiBrowseStartCarouselLayoutManager);
    multiBrowseStartRecyclerView.setNestedScrollingEnabled(false);

    debugSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          multiBrowseStartRecyclerView.setBackgroundResource(
              isChecked ? R.drawable.dashed_outline_rectangle : 0);
          multiBrowseStartCarouselLayoutManager.setDebuggingEnabled(
              multiBrowseStartRecyclerView, isChecked);
        });

    forceCompactSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) ->
            multiBrowseStartCarouselLayoutManager.setCarouselStrategy(
                new MultiBrowseCarouselStrategy(isChecked)));

    drawDividers.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked) {
            multiBrowseStartRecyclerView.addItemDecoration(horizontalDivider);
          } else {
            multiBrowseStartRecyclerView.removeItemDecoration(horizontalDivider);
          }
        });

    CarouselSnapHelper snapHelper = new CarouselSnapHelper();
    snapSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked) {
            snapHelper.attachToRecyclerView(multiBrowseStartRecyclerView);
          } else {
            snapHelper.attachToRecyclerView(null);
          }
        });

    CarouselAdapter adapter =
        new CarouselAdapter(
            (item, position) -> {
              multiBrowseStartRecyclerView.scrollToPosition(position);
              positionSlider.setValue(position + 1);
            },
            R.layout.cat_carousel_item_narrow);
    multiBrowseStartRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      private boolean dragged = false;

      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
          dragged = true;
        } else if (dragged && newState == RecyclerView.SCROLL_STATE_IDLE) {
          if (recyclerView.computeHorizontalScrollRange() != 0) {
            positionSlider.setValue((adapter.getItemCount() - 1) * recyclerView.computeHorizontalScrollOffset() / recyclerView.computeHorizontalScrollRange() + 1);
          }
          dragged = false;
        }
      }
    });

    itemCountDropdown.setOnItemClickListener(
        (parent, view1, position, id) -> {
          adapter.submitList(
              CarouselData.createItems().subList(0, position),
              updateSliderRange(positionSlider, adapter));
        });

    positionSlider.addOnSliderTouchListener(
        new OnSliderTouchListener() {
          @Override
          public void onStartTrackingTouch(@NonNull Slider slider) {}

          @Override
          public void onStopTrackingTouch(@NonNull Slider slider) {
            multiBrowseStartRecyclerView.smoothScrollToPosition((int) slider.getValue() - 1);
          }
        });

    multiBrowseStartRecyclerView.setAdapter(adapter);
    adapter.submitList(CarouselData.createItems(), updateSliderRange(positionSlider, adapter));
  }

  private static Runnable updateSliderRange(Slider slider, CarouselAdapter adapter) {
    return () -> {
      if (adapter.getItemCount() <= 1) {
        slider.setEnabled(false);
        return;
      }

      slider.setValueFrom(1);
      slider.setValue(1);
      slider.setValueTo(adapter.getItemCount());
      slider.setEnabled(true);
    };
  }
}
