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

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** An Adapter that works with a collection of selectable card items */
class SelectableCardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private List<Item> items;

  private SelectionTracker<Long> selectionTracker;

  public SelectableCardsAdapter() {
    this.items = new ArrayList<>();
  }

  public void setItems(List<Item> items) {
    this.items = items;
  }

  @Override
  public int getItemViewType(int position) {
    return 0;
  }

  public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
    this.selectionTracker = selectionTracker;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    View view = inflater.inflate(R.layout.cat_card_item_view, parent, false);
    return new ItemViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
    Item item = items.get(position);
    ((ItemViewHolder) viewHolder).bind(item, position);
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  class ItemViewHolder extends RecyclerView.ViewHolder {

    private final Details details;
    private final MaterialCardView materialCardView;
    private final TextView titleView;
    private final TextView subtitleView;

    ItemViewHolder(View itemView) {
      super(itemView);
      materialCardView = itemView.findViewById(R.id.item_card);
      titleView = itemView.findViewById(R.id.cat_card_title);
      subtitleView = itemView.findViewById(R.id.cat_card_subtitle);
      details = new Details();
    }

    private void bind(Item item, int position) {
      details.position = position;
      titleView.setText(item.title);
      subtitleView.setText(item.subtitle);
      if (selectionTracker != null) {
        bindSelectedState();
      }
      // Set an OnLongClickListener for accessibility
      materialCardView.setOnLongClickListener(
          v -> {
            selectionTracker.setItemsSelected(
                Arrays.asList(details.getSelectionKey()), !materialCardView.isChecked());
            return true;
          });
    }

    private void bindSelectedState() {
      materialCardView.setChecked(selectionTracker.isSelected(details.getSelectionKey()));
    }

    ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
      return details;
    }
  }

  static class DetailsLookup extends ItemDetailsLookup<Long> {

    private final RecyclerView recyclerView;

    DetailsLookup(RecyclerView recyclerView) {
      this.recyclerView = recyclerView;
    }

    @Nullable
    @Override
    public ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
      View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
      if (view != null) {
        RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
        if (viewHolder instanceof ItemViewHolder) {
          return ((ItemViewHolder) viewHolder).getItemDetails();
        }
      }
      return null;
    }
  }

  static class KeyProvider extends ItemKeyProvider<Long> {

    KeyProvider(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
      super(ItemKeyProvider.SCOPE_MAPPED);
    }

    @Nullable
    @Override
    public Long getKey(int position) {
      return (long) position;
    }

    @Override
    public int getPosition(@NonNull Long key) {
      long value = key;
      return (int) value;
    }
  }

  static class Item {

    private final String title;
    private final String subtitle;

    Item(String title, String subtitle) {
      this.title = title;
      this.subtitle = subtitle;
    }
  }

  static class Details extends ItemDetailsLookup.ItemDetails<Long> {

    long position;

    Details() {
    }

    @Override
    public int getPosition() {
      return (int) position;
    }

    @Nullable
    @Override
    public Long getSelectionKey() {
      return position;
    }

    @Override
    public boolean inSelectionHotspot(@NonNull MotionEvent e) {
      return false;
    }

    @Override
    public boolean inDragRegion(@NonNull MotionEvent e) {
      return true;
    }
  }
}
