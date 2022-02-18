/*
 * Copyright 2021 The Android Open Source Project
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

package io.material.catalog.adaptive;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.ViewCompat;

/** A Fragment that hosts a hero demo. */
public class AdaptiveHeroDemoFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.cat_adaptive_hero_fragment, viewGroup, false);
    RecyclerView sideContentList = view.findViewById(R.id.hero_side_content);
    RecyclerView.LayoutManager layoutManager =
        new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
    sideContentList.setLayoutManager(layoutManager);
    HeroAdapter adapter = new HeroAdapter();
    sideContentList.setAdapter(adapter);
    ViewCompat.setNestedScrollingEnabled(sideContentList, /* enabled= */ false);

    // Set up constraint sets.
    ConstraintLayout constraintLayout = view.findViewById(R.id.hero_constraint_layout);
    ConstraintSet smallLayout = getSmallLayout(constraintLayout);
    ConstraintSet mediumLayout = getMediumLayout(smallLayout);
    ConstraintSet largeLayout = getLargeLayout(mediumLayout);

    int screenWidth = getResources().getConfiguration().screenWidthDp;
    if (screenWidth < AdaptiveUtils.MEDIUM_SCREEN_WIDTH_SIZE) {
      smallLayout.applyTo(constraintLayout);
    } else if (screenWidth < AdaptiveUtils.LARGE_SCREEN_WIDTH_SIZE) {
      mediumLayout.applyTo(constraintLayout);
    } else {
      largeLayout.applyTo(constraintLayout);
    }

    return view;
  }

  /* Returns the constraint set to be used for the small layout configuration. */
  private ConstraintSet getSmallLayout(@NonNull ConstraintLayout constraintLayout) {
    ConstraintSet constraintSet = new ConstraintSet();
    // Use the constraint set from the constraint layout.
    constraintSet.clone(constraintLayout);
    return constraintSet;
  }

  /* Returns the constraint set to be used for the medium layout configuration. */
  private ConstraintSet getMediumLayout(@NonNull ConstraintSet smallLayout) {
    int marginHorizontal = getResources().getDimensionPixelOffset(R.dimen.cat_adaptive_hero_margin);
    int noMargin = getResources().getDimensionPixelOffset(R.dimen.cat_adaptive_margin_none);
    int marginHorizontalAdditional =
        getResources()
            .getDimensionPixelOffset(R.dimen.cat_adaptive_hero_margin_horizontal_additional);

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(smallLayout);

    // Main content.
    constraintSet.connect(
        R.id.hero_main_content, ConstraintSet.TOP, R.id.hero_top_content, ConstraintSet.BOTTOM);
    constraintSet.connect(
        R.id.hero_main_content,
        ConstraintSet.END,
        R.id.hero_side_content_container,
        ConstraintSet.START);
    constraintSet.connect(
        R.id.hero_main_content,
        ConstraintSet.BOTTOM,
        ConstraintSet.PARENT_ID,
        ConstraintSet.BOTTOM);
    constraintSet.setMargin(R.id.hero_top_content, ConstraintSet.START, noMargin);
    constraintSet.setMargin(R.id.hero_top_content, ConstraintSet.LEFT, noMargin);
    constraintSet.setMargin(R.id.hero_top_content, ConstraintSet.END, marginHorizontalAdditional);
    constraintSet.setMargin(R.id.hero_top_content, ConstraintSet.RIGHT, marginHorizontalAdditional);

    // Side content.
    constraintSet.connect(
        R.id.hero_side_content_container,
        ConstraintSet.TOP,
        R.id.hero_top_content,
        ConstraintSet.BOTTOM);
    constraintSet.connect(
        R.id.hero_side_content_container,
        ConstraintSet.START,
        R.id.hero_main_content,
        ConstraintSet.END);
    constraintSet.constrainPercentWidth(R.id.hero_side_content_container, 0.4f);

    constraintSet.setMargin(
        R.id.hero_side_content_container, ConstraintSet.START, marginHorizontal);
    constraintSet.setMargin(R.id.hero_side_content_container, ConstraintSet.LEFT, marginHorizontal);

    // Add more margin to the right/end of the side content to make sure there is a 24dp margin on
    // the right/end of the side content.
    constraintSet.setMargin(
        R.id.hero_side_content_container, ConstraintSet.RIGHT, marginHorizontalAdditional);
    constraintSet.setMargin(
        R.id.hero_side_content_container, ConstraintSet.END, marginHorizontalAdditional);
    return constraintSet;
  }

  /* Returns the constraint set to be used for the large layout configuration. */
  private ConstraintSet getLargeLayout(@NonNull ConstraintSet mediumLayout) {
    int noMargin = getResources().getDimensionPixelOffset(R.dimen.cat_adaptive_margin_none);
    int marginHorizontal = getResources().getDimensionPixelOffset(R.dimen.cat_adaptive_hero_margin);

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(mediumLayout);
    // Hero container.
    constraintSet.connect(
        R.id.hero_top_content,
        ConstraintSet.END,
        R.id.hero_side_content_container,
        ConstraintSet.START);

    // Side content.
    constraintSet.connect(
        R.id.hero_side_content_container,
        ConstraintSet.TOP,
        ConstraintSet.PARENT_ID,
        ConstraintSet.TOP);

    // Remove the margin from the main content since it no longer is at the right/end side.
    constraintSet.setMargin(R.id.hero_main_content, ConstraintSet.RIGHT, noMargin);
    constraintSet.setMargin(R.id.hero_main_content, ConstraintSet.END, noMargin);

    constraintSet.setMargin(R.id.hero_top_content, ConstraintSet.RIGHT, marginHorizontal);
    constraintSet.setMargin(R.id.hero_top_content, ConstraintSet.END, marginHorizontal);
    return constraintSet;
  }

  /** A RecyclerView adapter for the side content list of the hero demo. */
  private static final class HeroAdapter extends RecyclerView.Adapter<HeroAdapter.HeroViewHolder> {

    HeroAdapter() {}

    /** Provides a reference to the views for each data item. */
    static class HeroViewHolder extends RecyclerView.ViewHolder {
      public HeroViewHolder(@NonNull View view) {
        super(view);
      }
    }

    @NonNull
    @Override
    public HeroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view =
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.cat_adaptive_hero_item, parent, false);
      return new HeroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HeroViewHolder holder, int position) {
      // Populate content. Empty for demo purposes.
    }

    @Override
    public int getItemCount() {
      return 10;
    }
  }
}
