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

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.math.MathUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import dagger.android.support.DaggerFragment;
import io.material.catalog.feature.FeatureDemo;
import io.material.catalog.feature.FeatureDemoUtils;
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

  @Inject Set<FeatureDemo> featureDemos;
  @Inject TocResourceProvider tocResourceProvider;

  private DarkThemePreferencesRepository darkThemePreferencesRepository;
  private AppBarLayout appBarLayout;
  private View gridTopDivider;
  private RecyclerView recyclerView;
  private ImageButton darkThemeToggle;

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);

    darkThemePreferencesRepository = new DarkThemePreferencesRepository(getContext());

    String defaultDemo = FeatureDemoUtils.getDefaultDemo(getContext());
    if (!defaultDemo.isEmpty() && bundle == null) {
      for (FeatureDemo demo : featureDemos) {
        Fragment fragment = demo.createFragment();
        String key = fragment.getClass().getName();
        if (key.equals(defaultDemo)) {
          FeatureDemoUtils.startFragment(getActivity(), fragment, "fragment_content");
          return;
        }
      }
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(R.layout.cat_toc_fragment, viewGroup, false /* attachToRoot */);

    Toolbar toolbar = view.findViewById(R.id.toolbar);
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(toolbar);
    activity.getSupportActionBar().setDisplayShowTitleEnabled(false);

    ViewGroup content = view.findViewById(R.id.content);
    View.inflate(getContext(), tocResourceProvider.getHeaderContent(), content);

    appBarLayout = view.findViewById(R.id.cat_toc_app_bar_layout);
    gridTopDivider = view.findViewById(R.id.cat_toc_grid_top_divider);
    recyclerView = view.findViewById(R.id.cat_toc_grid);
    darkThemeToggle = view.findViewById(R.id.cat_toc_dark_theme_toggle);

    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      addGridTopDividerVisibilityListener();
    } else {
      gridTopDivider.setVisibility(View.VISIBLE);
    }

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

    TocAdapter tocAdapter = new TocAdapter(getActivity(), featureList);
    recyclerView.setAdapter(tocAdapter);

    initDarkThemeToggle();

    return view;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

    ((AppCompatActivity) getActivity()).setSupportActionBar(null);
  }

  private void addGridTopDividerVisibilityListener() {
    appBarLayout.addOnOffsetChangedListener(
        new OnOffsetChangedListener() {
          @Override
          public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
              // CTL is collapsed, hide top divider
              gridTopDivider.setVisibility(View.GONE);
            } else {
              // CTL is expanded or expanding, show top divider
              gridTopDivider.setVisibility(View.VISIBLE);
            }
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

  private void initDarkThemeToggle() {
    boolean darkThemeEnabled = darkThemePreferencesRepository.isDarkThemeEnabled();
    darkThemeToggle.setImageResource(
        darkThemeEnabled
            ? R.drawable.ic_night_on_vd_theme_24px
            : R.drawable.ic_night_off_vd_theme_24px);
    ensureDefaultNightMode(convertToNightMode(darkThemeEnabled));
    darkThemeToggle.setOnClickListener(v -> toggleNightMode());
  }

  private void ensureDefaultNightMode(int mode) {
    if (AppCompatDelegate.getDefaultNightMode() != mode) {
      AppCompatDelegate.setDefaultNightMode(mode);
    }
  }

  private void toggleNightMode() {
    boolean newDarkThemeEnabled = !darkThemePreferencesRepository.isDarkThemeEnabled();
    darkThemePreferencesRepository.saveDarkThemeEnabled(newDarkThemeEnabled);
    AppCompatDelegate.setDefaultNightMode(convertToNightMode(newDarkThemeEnabled));
  }

  private int convertToNightMode(boolean darkThemeEnabled) {
    return darkThemeEnabled
        ? AppCompatDelegate.MODE_NIGHT_YES
        : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
  }
}
