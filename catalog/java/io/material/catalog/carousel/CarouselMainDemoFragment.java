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
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the main Carousel demo for the Catalog app. */
public class CarouselMainDemoFragment extends DemoFragment {

  @NonNull
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    return layoutInflater.inflate(R.layout.cat_carousel, viewGroup, false /* attachToRoot */);
  }

  @Override
  @SuppressWarnings("RestrictTo")
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);

    RecyclerView carouselRecyclerView =
        view.findViewById(R.id.carousel_recycler_view);
    CarouselLayoutManager multiBrowseCenteredCarouselLayoutManager = new CarouselLayoutManager();
    carouselRecyclerView.setLayoutManager(multiBrowseCenteredCarouselLayoutManager);
    carouselRecyclerView.setNestedScrollingEnabled(false);

    CarouselAdapter adapter =
        new CarouselAdapter(
            (item, position) -> carouselRecyclerView.scrollToPosition(position));
    carouselRecyclerView.setAdapter(adapter);

    adapter.submitList(CarouselData.createItems());
  }
}
