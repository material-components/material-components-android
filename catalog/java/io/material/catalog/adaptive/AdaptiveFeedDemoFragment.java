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
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.ReactiveGuide;
import androidx.core.view.ViewCompat;

/** A Fragment that hosts a feed demo. */
public class AdaptiveFeedDemoFragment extends Fragment {

  private ReactiveGuide fold;
  private ConstraintLayout constraintLayout;
  private ConstraintSet closedLayout;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.cat_adaptive_feed_fragment, viewGroup, false);
    fold = view.findViewById(R.id.fold);
    // Set up content lists.
    RecyclerView smallContentList = view.findViewById(R.id.small_content_list);
    setUpContentRecyclerView(smallContentList, /* isSmallContent= */ true, 15);
    RecyclerView largeContentList = view.findViewById(R.id.large_content_list);
    setUpContentRecyclerView(largeContentList, /* isSmallContent= */ false, 5);
    // Set up constraint sets.
    constraintLayout = view.findViewById(R.id.feed_constraint_layout);
    closedLayout = new ConstraintSet();
    closedLayout.clone(constraintLayout);
    return view;
  }

  /* Sets up a recycler view with either small or large items list. */
  private void setUpContentRecyclerView(
      @NonNull RecyclerView recyclerView, boolean isSmallContent, int itemCount) {
    RecyclerView.LayoutManager layoutManager =
        new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
    FeedAdapter feedAdapter =
        new FeedAdapter(
            isSmallContent
                ? R.layout.cat_adaptive_feed_small_item
                : R.layout.cat_adaptive_feed_large_item,
            itemCount);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(feedAdapter);
    ViewCompat.setNestedScrollingEnabled(recyclerView, /* enabled= */ false);
  }

  /* Returns the constraint set to be used for the open layout configuration. */
  private ConstraintSet getOpenLayout(@NonNull ConstraintSet closedLayout, int foldWidth) {
    int marginHorizontal =
        getResources().getDimensionPixelOffset(R.dimen.cat_adaptive_margin_horizontal);
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(closedLayout);
    // Change top button to be on the right of the fold.
    constraintSet.connect(
        R.id.top_button,
        ConstraintSet.START,
        R.id.fold,
        ConstraintSet.END,
        marginHorizontal + foldWidth);
    // Change small content list to be on the right of the fold and below top button.
    constraintSet.connect(
        R.id.small_content_list,
        ConstraintSet.START,
        R.id.fold,
        ConstraintSet.END,
        marginHorizontal + foldWidth);
    constraintSet.connect(
        R.id.small_content_list, ConstraintSet.TOP, R.id.top_button, ConstraintSet.BOTTOM);
    constraintSet.setVisibility(R.id.highlight_content_card, View.GONE);
    constraintSet.setVisibility(R.id.large_content_list, View.VISIBLE);

    return constraintSet;
  }

  /**
   * Applies the open layout configuration.
   *
   * @param foldPosition position of the fold
   * @param foldWidth width of the fold if it's a hinge
   */
  void setOpenLayout(int foldPosition, int foldWidth) {
    ConstraintSet openLayout = getOpenLayout(closedLayout, foldWidth);
    openLayout.applyTo(constraintLayout);
    fold.setGuidelineEnd(foldPosition);
  }

  /* Applies the closed layout configuration. */
  void setClosedLayout() {
    fold.setGuidelineEnd(0);
    closedLayout.applyTo(constraintLayout);
  }

  /** A RecyclerView adapter for the content lists of the feed. */
  private static final class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    @LayoutRes private final int itemLayout;
    private final int itemCount;

    FeedAdapter(@LayoutRes int itemLayout, int itemCount) {
      this.itemLayout = itemLayout;
      this.itemCount = itemCount;
    }

    /** Provides a reference to the views for each data item. */
    static class FeedViewHolder extends RecyclerView.ViewHolder {
      public FeedViewHolder(@NonNull View view) {
        super(view);
      }
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
      return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
      // Populate content. Empty for demo purposes.
    }

    @Override
    public int getItemCount() {
      return itemCount;
    }
  }
}
