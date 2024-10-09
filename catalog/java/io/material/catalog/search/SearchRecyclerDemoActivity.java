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
import android.os.Bundle;
import android.os.Handler;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import dagger.android.ContributesAndroidInjector;
import io.material.catalog.application.scope.ActivityScope;
import io.material.catalog.feature.DemoActivity;
import java.util.ArrayList;
import java.util.List;

/** An activity that displays an Open Search Bar {@link RecyclerView} demo for the Catalog app. */
public class SearchRecyclerDemoActivity extends DemoActivity {

  private static final int ITEM_COUNT = 30;

  private final OnBackPressedCallback contextualToolbarOnBackPressedCallback =
      new OnBackPressedCallback(/* enabled= */ false) {
        @Override
        public void handleOnBackPressed() {
          hideContextualToolbarAndClearSelection();
        }
      };

  private List<Item> items;
  private Adapter adapter;
  private RecyclerView recyclerView;
  private AppBarLayout appBarLayout;
  private SearchBar searchBar;
  private SearchView searchView;
  private LinearLayout suggestionContainer;
  private ViewGroup contextualToolbarContainer;
  private Toolbar contextualToolbar;
  private ProgressBar spinner;

  @Nullable
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.cat_search_recycler_fragment, viewGroup, false);

    recyclerView = view.findViewById(R.id.recycler_view);
    appBarLayout = view.findViewById(R.id.app_bar_layout);
    searchBar = view.findViewById(R.id.open_search_bar);
    searchView = view.findViewById(R.id.open_search_view);
    suggestionContainer = view.findViewById(R.id.open_search_view_suggestion_container);
    contextualToolbarContainer = view.findViewById(R.id.contextual_toolbar_container);
    contextualToolbar = view.findViewById(R.id.contextual_toolbar);
    spinner = view.findViewById(R.id.spinner);

    setUpRecyclerView();
    setUpContextualToolbar();
    SearchDemoUtils.setUpSearchBar(this, searchBar);
    SearchDemoUtils.setUpSearchView(this, searchBar, searchView);
    SearchDemoUtils.setUpSuggestions(suggestionContainer, searchBar, searchView);
    SearchDemoUtils.startOnLoadAnimation(searchBar, bundle);

    ViewCompat.setOnApplyWindowInsetsListener(
        contextualToolbarContainer,
        (insetsView, insets) -> {
          int systemInsetTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
          insetsView.setPadding(0, systemInsetTop, 0, 0);
          return insets;
        });

    getOnBackPressedDispatcher().addCallback(this, contextualToolbarOnBackPressedCallback);

    return view;
  }

  @Override
  protected boolean shouldShowDefaultDemoActionBar() {
    return false;
  }

  @SuppressLint("NotifyDataSetChanged")
  private void setUpRecyclerView() {
    items = generateItems();
    adapter = new Adapter();
    adapter.setOnItemSelectedStateChangedListener(
        item -> {
          long selectedItemCount = getSelectedItemCount();
          if (selectedItemCount > 0 && Adapter.selectionModeEnabled) {
            contextualToolbar.setTitle(String.valueOf(selectedItemCount));
            expandContextualToolbar();
          } else {
            Adapter.selectionModeEnabled = false;
            collapseContextualToolbar();
          }
        });

    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(adapter);

    // Simulate data loaded asynchronously.
    new Handler()
        .postDelayed(
            () -> {
              spinner.setVisibility(View.GONE);
              adapter.setItems(items);
              adapter.notifyDataSetChanged();
            },
            1500);
  }

  private void setUpContextualToolbar() {
    contextualToolbar.setNavigationOnClickListener(v -> hideContextualToolbarAndClearSelection());
    contextualToolbar.inflateMenu(R.menu.cat_searchbar_contextual_toolbar_menu);
    contextualToolbar.setOnMenuItemClickListener(
        menuItem -> {
          if (menuItem.getItemId() == R.id.action_select_all) {
            setItemsSelected(true);
            contextualToolbar.setTitle(String.valueOf(getSelectedItemCount()));
            return true;
          }
          return false;
        });
  }

  private void hideContextualToolbarAndClearSelection() {
    Adapter.selectionModeEnabled = false;
    if (collapseContextualToolbar()) {
      setItemsSelected(false);
    }
  }

  private void expandContextualToolbar() {
    contextualToolbarOnBackPressedCallback.setEnabled(true);
    searchBar.expand(contextualToolbarContainer, appBarLayout);
  }

  private boolean collapseContextualToolbar() {
    contextualToolbarOnBackPressedCallback.setEnabled(false);
    return searchBar.collapse(contextualToolbarContainer, appBarLayout);
  }

  private List<Item> generateItems() {
    String titlePrefix = getString(R.string.cat_searchbar_recycler_item_title_prefix);
    String[] fillerTexts =
        new String[] {
          getString(R.string.cat_searchbar_lorem_ipsum_1),
          getString(R.string.cat_searchbar_lorem_ipsum_2),
          getString(R.string.cat_searchbar_lorem_ipsum_3),
          getString(R.string.cat_searchbar_lorem_ipsum_4),
          getString(R.string.cat_searchbar_lorem_ipsum_5)
        };
    List<Item> items = new ArrayList<>();
    for (int i = 1; i <= ITEM_COUNT; i++) {
      items.add(new Item(titlePrefix + " " + i, fillerTexts[i % fillerTexts.length]));
    }
    return items;
  }

  private long getSelectedItemCount() {
    long count = 0;
    for (Item item : items) {
      if (item.selected) {
        count++;
      }
    }
    return count;
  }

  @SuppressLint("NotifyDataSetChanged")
  private void setItemsSelected(boolean selected) {
    for (Item item : items) {
      item.selected = selected;
    }
    adapter.notifyDataSetChanged();
  }

  private static class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    static boolean selectionModeEnabled;

    private List<Item> items;

    @Nullable private OnItemSelectedStateChangedListener onItemSelectedStateChangedListener;

    public Adapter() {
      this.items = new ArrayList<>();
    }

    public void setItems(List<Item> items) {
      this.items = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      View view = inflater.inflate(R.layout.cat_search_recycler_item, parent, false);
      return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
      ((ItemViewHolder) viewHolder).bind(items.get(position), onItemSelectedStateChangedListener);
    }

    @Override
    public int getItemCount() {
      return items.size();
    }

    void setOnItemSelectedStateChangedListener(
        @Nullable OnItemSelectedStateChangedListener onItemSelectedStateChangedListener) {
      this.onItemSelectedStateChangedListener = onItemSelectedStateChangedListener;
    }
  }

  private static class ItemViewHolder extends RecyclerView.ViewHolder {

    private final MaterialCardView materialCardView;
    private final TextView titleView;
    private final TextView subtitleView;

    ItemViewHolder(View itemView) {
      super(itemView);
      materialCardView = itemView.findViewById(R.id.cat_searchbar_recycler_card);
      titleView = itemView.findViewById(R.id.cat_searchbar_recycler_title);
      subtitleView = itemView.findViewById(R.id.cat_searchbar_recycler_subtitle);
    }

    private void bind(
        Item item,
        @Nullable OnItemSelectedStateChangedListener onItemSelectedStateChangedListener) {
      titleView.setText(item.title);
      subtitleView.setText(item.subtitle);
      bindSelectedState(item);
      itemView.setOnLongClickListener(
          v -> {
            if (Adapter.selectionModeEnabled) {
              return false;
            }

            Adapter.selectionModeEnabled = true;
            toggleItem(item, onItemSelectedStateChangedListener);
            return true;
          });
      itemView.setOnClickListener(
          v -> {
            if (Adapter.selectionModeEnabled) {
              toggleItem(item, onItemSelectedStateChangedListener);
            }
          });
    }

    private void toggleItem(
        Item item,
        @Nullable OnItemSelectedStateChangedListener onItemSelectedStateChangedListener) {
      item.selected = !item.selected;
      bindSelectedState(item);
      if (onItemSelectedStateChangedListener != null) {
        onItemSelectedStateChangedListener.onItemSelectedStateChanged(item);
      }
    }

    private void bindSelectedState(Item item) {
      materialCardView.setChecked(item.selected);
    }
  }

  private static class Item {
    private final String title;
    private final String subtitle;

    private boolean selected;

    private Item(String title, String subtitle) {
      this.title = title;
      this.subtitle = subtitle;
    }
  }

  private interface OnItemSelectedStateChangedListener {
    void onItemSelectedStateChanged(Item item);
  }

  /** The Dagger module for {@link SearchRecyclerDemoActivity} dependencies. */
  @dagger.Module
  public abstract static class Module {

    @ActivityScope
    @ContributesAndroidInjector
    abstract SearchRecyclerDemoActivity contributeInjector();
  }
}
