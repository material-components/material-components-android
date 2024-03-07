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

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.CarouselSnapHelper;
import com.google.android.material.carousel.FullScreenCarouselStrategy;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.slider.Slider.OnSliderTouchListener;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.windowpreferences.WindowPreferencesManager;

/** A fragment that displays the fullscreen variant of the Carousel. */
public class FullScreenStrategyDemoFragment extends DemoFragment {

  private MaterialDividerItemDecoration verticalDivider;
  private BottomSheetDialog bottomSheetDialog;

  @NonNull
  @Override
  @SuppressLint("SourceLockedOrientationActivity")
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    // We want to force portrait mode for the fullscreen vertical carousel
    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    return layoutInflater.inflate(
        R.layout.cat_carousel_full_screen_fragment, viewGroup, false /* attachToRoot */);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
  }

  @Override
  @SuppressWarnings("RestrictTo")
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);

    bottomSheetDialog = new BottomSheetDialog(view.getContext());
    bottomSheetDialog.setContentView(R.layout.cat_carousel_bottom_sheet_contents);
    // Opt in to perform swipe to dismiss animation when dismissing bottom sheet dialog.
    bottomSheetDialog.setDismissWithAnimation(true);

    new WindowPreferencesManager(requireContext())
        .applyEdgeToEdgePreference(bottomSheetDialog.getWindow());
    verticalDivider =
        new MaterialDividerItemDecoration(requireContext(), MaterialDividerItemDecoration.VERTICAL);

    Button showBottomSheetButton = view.findViewById(R.id.show_bottomsheet_button);
    showBottomSheetButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        bottomSheetDialog.show();
      }
    });

    MaterialSwitch debugSwitch = bottomSheetDialog.findViewById(R.id.debug_switch);
    MaterialSwitch drawDividers = bottomSheetDialog.findViewById(R.id.draw_dividers_switch);
    MaterialSwitch enableFlingSwitch = bottomSheetDialog.findViewById(R.id.enable_fling_switch);
    AutoCompleteTextView itemCountDropdown =
        bottomSheetDialog.findViewById(R.id.item_count_dropdown);
    Slider positionSlider = bottomSheetDialog.findViewById(R.id.position_slider);

    // A vertical fullscreen carousel
    RecyclerView fullscreenRecyclerView =
        view.findViewById(R.id.fullscreen_carousel_recycler_view);
    CarouselLayoutManager carouselLayoutManager =
        new CarouselLayoutManager(new FullScreenCarouselStrategy(), RecyclerView.VERTICAL);
    carouselLayoutManager.setDebuggingEnabled(
        fullscreenRecyclerView, debugSwitch.isChecked());
    fullscreenRecyclerView.setLayoutManager(carouselLayoutManager);
    fullscreenRecyclerView.setNestedScrollingEnabled(false);

    debugSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          carouselLayoutManager.setOrientation(CarouselLayoutManager.VERTICAL);
          fullscreenRecyclerView.setBackgroundResource(
              isChecked ? R.drawable.dashed_outline_rectangle : 0);
          carouselLayoutManager.setDebuggingEnabled(
              fullscreenRecyclerView, isChecked);
        });

    drawDividers.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked) {
            fullscreenRecyclerView.addItemDecoration(verticalDivider);
          } else {
            fullscreenRecyclerView.removeItemDecoration(verticalDivider);
          }
        });

    CarouselAdapter adapter =
        new CarouselAdapter(
            (item, position) -> fullscreenRecyclerView.scrollToPosition(position),
            R.layout.cat_carousel_item_vertical);
    fullscreenRecyclerView.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          private boolean dragged = false;

          @Override
          public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
              dragged = true;
            } else if (dragged && newState == RecyclerView.SCROLL_STATE_IDLE) {
              if (recyclerView.computeVerticalScrollRange() != 0) {
                positionSlider.setValue(
                    (adapter.getItemCount() - 1)
                            * recyclerView.computeVerticalScrollOffset()
                            / recyclerView.computeVerticalScrollRange()
                        + 1);
              }
              dragged = false;
            }
          }
        });

    SnapHelper flingDisabledSnapHelper = new CarouselSnapHelper();
    SnapHelper flingEnabledSnapHelper = new CarouselSnapHelper(false);

    flingDisabledSnapHelper.attachToRecyclerView(fullscreenRecyclerView);

    enableFlingSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked) {
            flingDisabledSnapHelper.attachToRecyclerView(null);
            flingEnabledSnapHelper.attachToRecyclerView(fullscreenRecyclerView);
          } else {
            flingEnabledSnapHelper.attachToRecyclerView(null);
            flingDisabledSnapHelper.attachToRecyclerView(fullscreenRecyclerView);
          }
        });

    itemCountDropdown.setOnItemClickListener(
        (parent, view1, position, id) ->
            adapter.submitList(
                CarouselData.createItems().subList(0, position),
                updateSliderRange(positionSlider, adapter)));

    positionSlider.addOnSliderTouchListener(
        new OnSliderTouchListener() {
          @Override
          public void onStartTrackingTouch(@NonNull Slider slider) {}

          @Override
          public void onStopTrackingTouch(@NonNull Slider slider) {
            fullscreenRecyclerView.smoothScrollToPosition(((int) slider.getValue()) - 1);
          }
        });

    fullscreenRecyclerView.setAdapter(adapter);
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
