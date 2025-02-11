/*
 * Copyright 2023 The Android Open Source Project
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
import com.google.android.material.carousel.UncontainedCarouselStrategy;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the uncontained variant of the Carousel. */
public class UncontainedCarouselDemoFragment extends DemoFragment {

  private MaterialDividerItemDecoration horizontalDivider;
  private CarouselAdapter adapter;
  private Slider positionSlider;

  @NonNull
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    return layoutInflater.inflate(
        R.layout.cat_carousel_uncontained_fragment, viewGroup, false /* attachToRoot */);
  }

  @Override
  @SuppressWarnings("RestrictTo")
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);

    horizontalDivider =
        new MaterialDividerItemDecoration(
            requireContext(), MaterialDividerItemDecoration.HORIZONTAL);

    MaterialSwitch debugSwitch = view.findViewById(R.id.debug_switch);
    MaterialSwitch drawDividers = view.findViewById(R.id.draw_dividers_switch);
    MaterialSwitch snapSwitch = view.findViewById(R.id.snap_switch);
    AutoCompleteTextView itemCountDropdown = view.findViewById(R.id.item_count_dropdown);
    positionSlider = view.findViewById(R.id.position_slider);

    RecyclerView uncontainedRecyclerView =
        view.findViewById(R.id.uncontained_carousel_recycler_view);
    CarouselLayoutManager uncontainedCarouselLayoutManager =
        new CarouselLayoutManager(new UncontainedCarouselStrategy());
    uncontainedCarouselLayoutManager.setDebuggingEnabled(
        uncontainedRecyclerView, debugSwitch.isChecked());
    uncontainedRecyclerView.setLayoutManager(uncontainedCarouselLayoutManager);
    uncontainedRecyclerView.setNestedScrollingEnabled(false);

    debugSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          uncontainedRecyclerView.setBackgroundResource(
              isChecked ? R.drawable.dashed_outline_rectangle : 0);
          uncontainedCarouselLayoutManager.setDebuggingEnabled(uncontainedRecyclerView, isChecked);
        });

    drawDividers.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked) {
            uncontainedRecyclerView.addItemDecoration(horizontalDivider);
          } else {
            uncontainedRecyclerView.removeItemDecoration(horizontalDivider);
          }
        });

    CarouselSnapHelper snapHelper = new CarouselSnapHelper();
    snapSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked) {
            snapHelper.attachToRecyclerView(uncontainedRecyclerView);
          } else {
            snapHelper.attachToRecyclerView(null);
          }
        });

    adapter =
        new CarouselAdapter(
            (item, position) -> {
              uncontainedRecyclerView.scrollToPosition(position);
              positionSlider.setValue(position + 1);
            },
            R.layout.cat_carousel_item_narrow);
    uncontainedRecyclerView.addOnScrollListener(
        CarouselDemoUtils.createUpdateSliderOnScrollListener(positionSlider, adapter));

    itemCountDropdown.setOnItemClickListener(
        (parent, view1, position, id) ->
            adapter.submitList(
                CarouselData.createItems().subList(0, position),
                updateSliderRange(positionSlider, adapter)));

    positionSlider.addOnSliderTouchListener(
        CarouselDemoUtils.createScrollToPositionSliderTouchListener(uncontainedRecyclerView));

    uncontainedRecyclerView.setAdapter(adapter);
  }

  @Override
  public void onStart() {
    super.onStart();
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
