/*
 * Copyright 2022 The Android Open Source Project
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

package io.material.catalog.search;

import io.material.catalog.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.search.SearchView.TransitionState;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

/** Provides utility methods for the search demo. */
public final class SearchDemoUtils {

  private SearchDemoUtils() {}

  public static void setUpSearchBar(@NonNull Activity activity, @NonNull SearchBar searchBar) {
    searchBar.inflateMenu(R.menu.cat_searchbar_menu);
    searchBar.setOnMenuItemClickListener(
        menuItem -> {
          showSnackbar(activity, menuItem);
          return true;
        });
  }

  @SuppressLint("NewApi")
  public static void setUpSearchView(
      @NonNull AppCompatActivity activity,
      @NonNull SearchBar searchBar,
      @NonNull SearchView searchView) {
    searchView.inflateMenu(R.menu.cat_searchview_menu);
    searchView.setOnMenuItemClickListener(
        menuItem -> {
          showSnackbar(activity, menuItem);
          return true;
        });
    searchView
        .getEditText()
        .setOnEditorActionListener(
            (v, actionId, event) -> {
              submitSearchQuery(searchBar, searchView, searchView.getText().toString());
              return false;
            });
    OnBackPressedCallback onBackPressedCallback =
        new OnBackPressedCallback(/* enabled= */ false) {
          @Override
          public void handleOnBackPressed() {
            searchView.hide();
          }
        };
    activity.getOnBackPressedDispatcher().addCallback(activity, onBackPressedCallback);
    searchView.addTransitionListener(
        (searchView1, previousState, newState) ->
            onBackPressedCallback.setEnabled(newState == TransitionState.SHOWN));
  }

  public static void showSnackbar(@NonNull Activity activity, @NonNull MenuItem menuItem) {
    Snackbar.make(
            activity.findViewById(android.R.id.content), menuItem.getTitle(), Snackbar.LENGTH_SHORT)
        .show();
  }

  public static void startOnLoadAnimation(@NonNull SearchBar searchBar, @Nullable Bundle bundle) {
    // Don't start animation on rotation. Only needed in demo because minIntervalSeconds is 0.
    if (bundle == null) {
      searchBar.startOnLoadAnimation();
    }
  }

  public static void setUpSuggestions(
      @NonNull ViewGroup suggestionContainer,
      @NonNull SearchBar searchBar,
      @NonNull SearchView searchView) {
    addSuggestionTitleView(
        suggestionContainer, R.string.cat_searchview_suggestion_section_title_yesterday);
    addSuggestionItemViews(suggestionContainer, getYesterdaySuggestions(), searchBar, searchView);

    addSuggestionTitleView(
        suggestionContainer, R.string.cat_searchview_suggestion_section_title_this_week);
    addSuggestionItemViews(suggestionContainer, getThisWeekSuggestions(), searchBar, searchView);
  }

  private static void addSuggestionTitleView(ViewGroup parent, @StringRes int titleResId) {
    TextView titleView =
        (TextView)
            LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cat_search_suggestion_title, parent, false);

    titleView.setText(titleResId);

    parent.addView(titleView);
  }

  private static void addSuggestionItemViews(
      ViewGroup parent,
      List<SuggestionItem> suggestionItems,
      SearchBar searchBar,
      SearchView searchView) {
    for (SuggestionItem suggestionItem : suggestionItems) {
      addSuggestionItemView(parent, suggestionItem, searchBar, searchView);
    }
  }

  private static void addSuggestionItemView(
      ViewGroup parent, SuggestionItem suggestionItem, SearchBar searchBar, SearchView searchView) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.cat_search_suggestion_item, parent, false);

    ImageView iconView = view.findViewById(R.id.cat_searchbar_suggestion_icon);
    TextView titleView = view.findViewById(R.id.cat_searchbar_suggestion_title);
    TextView subtitleView = view.findViewById(R.id.cat_searchbar_suggestion_subtitle);

    iconView.setImageResource(suggestionItem.iconResId);
    titleView.setText(suggestionItem.title);
    subtitleView.setText(suggestionItem.subtitle);

    view.setOnClickListener(v -> submitSearchQuery(searchBar, searchView, suggestionItem.title));

    parent.addView(view);
  }

  private static List<SuggestionItem> getYesterdaySuggestions() {
    List<SuggestionItem> suggestionItems = new ArrayList<>();
    suggestionItems.add(
        new SuggestionItem(
            R.drawable.ic_schedule_vd_theme_24, "481 Van Brunt Street", "Brooklyn, NY"));
    suggestionItems.add(
        new SuggestionItem(
            R.drawable.ic_home_vd_theme_24, "Home", "199 Pacific Street, Brooklyn, NY"));
    return suggestionItems;
  }

  private static List<SuggestionItem> getThisWeekSuggestions() {
    List<SuggestionItem> suggestionItems = new ArrayList<>();
    suggestionItems.add(
        new SuggestionItem(
            R.drawable.ic_schedule_vd_theme_24,
            "BEP GA",
            "Forsyth Street, New York, NY"));
    suggestionItems.add(
        new SuggestionItem(
            R.drawable.ic_schedule_vd_theme_24,
            "Sushi Nakazawa",
            "Commerce Street, New York, NY"));
    suggestionItems.add(
        new SuggestionItem(
            R.drawable.ic_schedule_vd_theme_24,
            "IFC Center",
            "6th Avenue, New York, NY"));
    return suggestionItems;
  }

  private static void submitSearchQuery(SearchBar searchBar, SearchView searchView, String query) {
    searchBar.setText(query);
    searchView.hide();
  }

  private static class SuggestionItem {
    @DrawableRes private final int iconResId;
    private final String title;
    private final String subtitle;

    private SuggestionItem(int iconResId, String title, String subtitle) {
      this.iconResId = iconResId;
      this.title = title;
      this.subtitle = subtitle;
    }
  }
}
