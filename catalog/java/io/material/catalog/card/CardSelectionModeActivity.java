/*
 * Copyright 2019 The Android Open Source Project
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

package io.material.catalog.card;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import io.material.catalog.card.SelectableCardsAdapter.Item;
import io.material.catalog.feature.DemoActivity;
import java.util.ArrayList;
import java.util.List;

/** An activity that displays a {@link RecyclerView} with checkable cards. */
public class CardSelectionModeActivity extends DemoActivity implements ActionMode.Callback {

  private static final int ITEM_COUNT = 20;

  private ActionMode actionMode;
  private SelectableCardsAdapter adapter;
  private SelectionTracker<Long> selectionTracker;

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.cat_card_selection_activity, viewGroup, false);
    RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
    setUpRecyclerView(recyclerView);

    return view;
  }

  protected void setUpRecyclerView(RecyclerView recyclerView) {
    adapter = new SelectableCardsAdapter();
    adapter.setItems(generateItems());
    recyclerView.setAdapter(adapter);

    selectionTracker =
        new SelectionTracker.Builder<>(
                "card_selection",
                recyclerView,
                new SelectableCardsAdapter.KeyProvider(adapter),
                new SelectableCardsAdapter.DetailsLookup(recyclerView),
                StorageStrategy.createLongStorage())
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build();

    adapter.setSelectionTracker(selectionTracker);
    selectionTracker.addObserver(
        new SelectionTracker.SelectionObserver<Long>() {
          @Override
          public void onSelectionChanged() {
            if (selectionTracker.getSelection().size() > 0) {
              if (actionMode == null) {
                actionMode = startSupportActionMode(CardSelectionModeActivity.this);
              }
              actionMode.setTitle(String.valueOf(selectionTracker.getSelection().size()));
            } else if (actionMode != null) {
              actionMode.finish();
            }
          }
        });
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
  }

  private List<Item> generateItems() {
    String titlePrefix = getString(R.string.cat_card_selectable_item_title);
    List<Item> items = new ArrayList<>();
    for (int i = 0; i < ITEM_COUNT; i++) {
      items.add(
          new Item(titlePrefix + " " + (i + 1), getString(R.string.cat_card_selectable_content)));
    }

    return items;
  }

  @Override
  public int getDemoTitleResId() {
    return R.string.cat_card_selection_mode;
  }

  @Override
  public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
    return true;
  }

  @Override
  public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
    return false;
  }

  @Override
  public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
    return false;
  }

  @Override
  public void onDestroyActionMode(ActionMode actionMode) {
    selectionTracker.clearSelection();
    this.actionMode = null;
  }
}
