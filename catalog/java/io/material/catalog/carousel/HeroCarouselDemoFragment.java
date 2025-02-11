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

import static com.google.android.material.carousel.CarouselLayoutManager.ALIGNMENT_CENTER;
import static com.google.android.material.carousel.CarouselLayoutManager.ALIGNMENT_START;

import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.CarouselSnapHelper;
import com.google.android.material.carousel.HeroCarouselStrategy;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the hero variant of the Carousel. */
public class HeroCarouselDemoFragment extends DemoFragment {

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
        R.layout.cat_carousel_hero_fragment, viewGroup, false /* attachToRoot */);
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
    MaterialSwitch enableFlingSwitch = view.findViewById(R.id.enable_fling_switch);
    AutoCompleteTextView itemCountDropdown = view.findViewById(R.id.item_count_dropdown);
    positionSlider = view.findViewById(R.id.position_slider);
    RadioButton startAlignButton = view.findViewById(R.id.start_align);
    RadioButton centerAlignButton = view.findViewById(R.id.center_align);

    // A hero carousel
    RecyclerView heroStartRecyclerView = view.findViewById(R.id.hero_start_carousel_recycler_view);
    CarouselLayoutManager heroStartCarouselLayoutManager =
        new CarouselLayoutManager(new HeroCarouselStrategy());
    heroStartCarouselLayoutManager.setDebuggingEnabled(
        heroStartRecyclerView, debugSwitch.isChecked());
    heroStartRecyclerView.setLayoutManager(heroStartCarouselLayoutManager);
    heroStartRecyclerView.setNestedScrollingEnabled(false);

    debugSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          heroStartRecyclerView.setBackgroundResource(
              isChecked ? R.drawable.dashed_outline_rectangle : 0);
          heroStartCarouselLayoutManager.setDebuggingEnabled(heroStartRecyclerView, isChecked);
        });

    drawDividers.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked) {
            heroStartRecyclerView.addItemDecoration(horizontalDivider);
          } else {
            heroStartRecyclerView.removeItemDecoration(horizontalDivider);
          }
        });

    adapter =
        new CarouselAdapter(
            (item, position) -> {
              heroStartRecyclerView.scrollToPosition(position);
              positionSlider.setValue(position + 1);
            },
            R.layout.cat_carousel_item);
    heroStartRecyclerView.addOnScrollListener(
        CarouselDemoUtils.createUpdateSliderOnScrollListener(positionSlider, adapter));

    SnapHelper disableFlingSnapHelper = new CarouselSnapHelper();
    SnapHelper enableFlingSnapHelper = new CarouselSnapHelper(false);

    if (enableFlingSwitch.isChecked()) {
      enableFlingSnapHelper.attachToRecyclerView(heroStartRecyclerView);
    } else {
      disableFlingSnapHelper.attachToRecyclerView(heroStartRecyclerView);
    }

    enableFlingSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked) {
            disableFlingSnapHelper.attachToRecyclerView(null);
            enableFlingSnapHelper.attachToRecyclerView(heroStartRecyclerView);
          } else {
            enableFlingSnapHelper.attachToRecyclerView(null);
            disableFlingSnapHelper.attachToRecyclerView(heroStartRecyclerView);
          }
        });

    itemCountDropdown.setOnItemClickListener(
        (parent, view1, position, id) ->
            adapter.submitList(
                CarouselData.createItems().subList(0, position),
                updateSliderRange(positionSlider, adapter)));

    positionSlider.addOnSliderTouchListener(
        CarouselDemoUtils.createScrollToPositionSliderTouchListener(heroStartRecyclerView));

    startAlignButton.setOnClickListener(
        v -> heroStartCarouselLayoutManager.setCarouselAlignment(ALIGNMENT_START));
    centerAlignButton.setOnClickListener(
        v -> heroStartCarouselLayoutManager.setCarouselAlignment(ALIGNMENT_CENTER));

    heroStartRecyclerView.setAdapter(adapter);
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
