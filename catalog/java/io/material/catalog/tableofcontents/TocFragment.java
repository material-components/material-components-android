/*
 * Copyright 2017 The Android Open Source Project
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

package io.material.catalog.tableofcontents;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.OnQueryTextListener;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.math.MathUtils;
import androidx.core.view.ViewCompat;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.transition.MaterialSharedAxis;
import dagger.android.support.DaggerFragment;
import io.material.catalog.feature.FeatureDemo;
import io.material.catalog.feature.FeatureDemoUtils;
import io.material.catalog.preferences.CatalogPreferencesDialogFragment;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

/** Initial table of contents screen for the catalog app. */
public class TocFragment extends DaggerFragment {

  private static final int GRID_SPAN_COUNT_MIN = 1;
  private static final int GRID_SPAN_COUNT_MAX = 4;

  @Dimension(unit = Dimension.DP)
  private static final int CATALOG_NARROW_SCREEN_SIZE_CUTOFF = 350;

  @Inject Set<FeatureDemo> featureDemos;
  @Inject TocResourceProvider tocResourceProvider;

  private AppBarLayout appBarLayout;
  private View gridTopDivider;
  private ConstraintLayout headerContainer;
  private ImageButton preferencesButton;
  private SearchView searchView;
  private ImageButton searchButton;
  private TocAdapter tocAdapter;
  private Transition openSearchViewTransition;
  private Transition closeSearchViewTransition;

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);

    if (bundle == null) {
      startDefaultDemoLandingIfNeeded();
    }
  }

  @Nullable
  @Override
  @SuppressWarnings("MissingInflatedId")
  public View onCreateView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(R.layout.cat_toc_fragment, viewGroup, false /* attachToRoot */);

    Toolbar toolbar = view.findViewById(R.id.toolbar);
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(toolbar);
    activity.getSupportActionBar().setDisplayShowTitleEnabled(false);

    ViewGroup content = view.findViewById(R.id.content);
    View.inflate(getContext(), R.layout.cat_toc_header, content);

    appBarLayout = view.findViewById(R.id.cat_toc_app_bar_layout);
    gridTopDivider = view.findViewById(R.id.cat_toc_grid_top_divider);
    headerContainer = view.findViewById(R.id.cat_toc_header_container);
    RecyclerView recyclerView = view.findViewById(R.id.cat_toc_grid);
    preferencesButton = view.findViewById(R.id.cat_toc_preferences_button);
    searchView = view.findViewById(R.id.cat_toc_search_view);
    searchButton = view.findViewById(R.id.cat_toc_search_button);

    // Inflate logo into the header container.
    View.inflate(getContext(), tocResourceProvider.getLogoLayout(), headerContainer);

    ViewCompat.setOnApplyWindowInsetsListener(
        view,
        (v, insetsCompat) -> {
          appBarLayout
              .findViewById(R.id.cat_toc_collapsingtoolbarlayout)
              .setPadding(0, insetsCompat.getSystemWindowInsetTop(), 0, 0);
          return insetsCompat;
        });

    addGridTopDividerVisibilityListener();

    final int gridSpanCount = calculateGridSpanCount();

    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), gridSpanCount));
    recyclerView.addItemDecoration(
        new GridDividerDecoration(
            getResources().getDimensionPixelSize(R.dimen.cat_toc_grid_divider_size),
            ContextCompat.getColor(getContext(), R.color.cat_toc_grid_divider_color),
            gridSpanCount));

    List<FeatureDemo> featureList = new ArrayList<>(featureDemos);
    // Sort features alphabetically
    Collator collator = Collator.getInstance();
    Collections.sort(
        featureList,
        (feature1, feature2) ->
            collator.compare(
                getContext().getString(feature1.getTitleResId()),
                getContext().getString(feature2.getTitleResId())));

    tocAdapter = new TocAdapter(getActivity(), featureList);
    recyclerView.setAdapter(tocAdapter);

    adjustLogoConstraintsForNarrowScreenWidths();

    initPreferencesButton();
    initSearchButton();
    initSearchView();
    initSearchViewTransitions();

    return view;
  }

  private void adjustLogoConstraintsForNarrowScreenWidths() {
    // Adjust logo constraints so that the logo text does not overlap with the search image button.
    if (getScreenWidth() < CATALOG_NARROW_SCREEN_SIZE_CUTOFF) {
      ConstraintSet narrowHeaderConstraintSet = createNarrowHeaderConstraintSet(headerContainer);
      narrowHeaderConstraintSet.applyTo(headerContainer);
    }
  }

  @Dimension(unit = Dimension.DP)
  private int getScreenWidth() {
    return getResources().getConfiguration().screenWidthDp;
  }

  private ConstraintSet createNarrowHeaderConstraintSet(ConstraintLayout constraintLayout) {
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(constraintLayout);
    constraintSet.connect(
        R.id.header_logo, ConstraintSet.END, R.id.cat_toc_search_button, ConstraintSet.START);
    // Add a bit of space on the start side of the logo to compensate for the extra 12dp of space
    // the search icon button includes on each side because of its 48dp minimum touch target width.
    constraintSet.setMargin(
        R.id.header_logo,
        ConstraintSet.START,
        getResources().getDimensionPixelOffset(R.dimen.cat_toc_header_additional_start_margin));
    return constraintSet;
  }

  private void addGridTopDividerVisibilityListener() {
    appBarLayout.addOnOffsetChangedListener(
        (appBarLayout, verticalOffset) -> {
          if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
            // CTL is collapsed, hide top divider
            gridTopDivider.setVisibility(View.GONE);
          } else {
            // CTL is expanded or expanding, show top divider
            gridTopDivider.setVisibility(View.VISIBLE);
          }
        });
  }

  private int calculateGridSpanCount() {
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    int displayWidth = displayMetrics.widthPixels;
    int itemSize = getResources().getDimensionPixelSize(R.dimen.cat_toc_item_size);
    int gridSpanCount = displayWidth / itemSize;
    return MathUtils.clamp(gridSpanCount, GRID_SPAN_COUNT_MIN, GRID_SPAN_COUNT_MAX);
  }

  private void initPreferencesButton() {
    preferencesButton.setOnClickListener(
        v ->
            new CatalogPreferencesDialogFragment()
                .show(getParentFragmentManager(), "preferences-screen"));
  }

  private void initSearchButton() {
    searchButton.setOnClickListener(v -> openSearchView());
  }

  private void initSearchView() {
    searchView.setOnClickListener(v -> closeSearchView());

    searchView.setOnQueryTextListener(
        new OnQueryTextListener() {
          @Override
          public boolean onQueryTextSubmit(String query) {
            return false;
          }

          @Override
          public boolean onQueryTextChange(String newText) {
            tocAdapter.getFilter().filter(newText);
            return false;
          }
        });
  }

  private void initSearchViewTransitions() {
    openSearchViewTransition = createSearchViewTransition(true);
    closeSearchViewTransition = createSearchViewTransition(false);
  }

  private void openSearchView() {
    TransitionManager.beginDelayedTransition(headerContainer, openSearchViewTransition);

    headerContainer.setVisibility(View.GONE);
    searchView.setVisibility(View.VISIBLE);

    searchView.requestFocus();
  }

  private void closeSearchView() {
    TransitionManager.beginDelayedTransition(headerContainer, closeSearchViewTransition);

    headerContainer.setVisibility(View.VISIBLE);
    searchView.setVisibility(View.GONE);

    clearSearchView();
  }

  @NonNull
  private MaterialSharedAxis createSearchViewTransition(boolean entering) {
    MaterialSharedAxis sharedAxisTransition =
        new MaterialSharedAxis(MaterialSharedAxis.X, entering);

    sharedAxisTransition.addTarget(headerContainer);
    sharedAxisTransition.addTarget(searchView);
    return sharedAxisTransition;
  }

  @Override
  public void onPause() {
    super.onPause();
    clearSearchView();
  }

  private void clearSearchView() {
    if (searchView != null) {
      searchView.setQuery("", true);
    }
  }

  private void startDefaultDemoLandingIfNeeded() {
    String defaultDemoLanding = FeatureDemoUtils.getDefaultDemoLanding(requireContext());
    if (!defaultDemoLanding.isEmpty()) {
      for (FeatureDemo demo : featureDemos) {
        Fragment fragment = demo.createFragment();
        String key = fragment.getClass().getName();
        if (key.equals(defaultDemoLanding)) {
          Bundle args = fragment.getArguments() != null ? fragment.getArguments() : new Bundle();
          args.putBoolean(FeatureDemo.KEY_FAVORITE_LAUNCH, true);
          fragment.setArguments(args);
          FeatureDemoUtils.startFragment(getActivity(), fragment, "fragment_content");
          return;
        }
      }
    }
  }
}
