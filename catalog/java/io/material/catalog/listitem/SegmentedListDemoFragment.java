/*
 * Copyright 2025 The Android Open Source Project
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

package io.material.catalog.listitem;

import io.material.catalog.R;

import static android.widget.Adapter.NO_SELECTION;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.listitem.ListItemCardView;
import com.google.android.material.listitem.ListItemViewHolder;
import java.util.ArrayList;
import java.util.List;

/** A fragment that displays a segmented List demos for the Catalog app. */
public class SegmentedListDemoFragment extends ListsMainDemoFragment {

  private static final String KEY_LIST_DATA = "key_list_data";
  private static final String KEY_SELECTED_POSITION = "key_selected_position";

  private ArrayList<CustomListItemData> listData;
  private ListsAdapter adapter;

  @NonNull
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    RecyclerView view =
        (RecyclerView)
            layoutInflater.inflate(R.layout.cat_lists_bright_background_fragment, viewGroup, false);

    view.setLayoutManager(new LinearLayoutManager(getContext()));
    if (bundle != null) {
      listData = bundle.getParcelableArrayList(KEY_LIST_DATA);
    } else {
      listData = new ArrayList<>();
      for (int i = 0; i < 20; i++) {
        listData.add(
            new CustomListItemData(
                String.format(view.getContext().getString(R.string.cat_list_item_text), i + 1),
                i,
                20));
      }
    }

    adapter = new ListsAdapter(listData);
    if (bundle != null) {
      adapter.setSelectedPosition(bundle.getInt(KEY_SELECTED_POSITION, NO_SELECTION));
    }
    view.setAdapter(adapter);
    view.addItemDecoration(new MarginItemDecoration(getContext()));

    return view;
  }

  /** An Adapter that shows custom list items */
  public class ListsAdapter extends Adapter<CustomItemViewHolder> {
    private int selectedPosition = NO_SELECTION;
    private final List<CustomListItemData> items;

    public ListsAdapter(@NonNull List<CustomListItemData> items) {
      this.items = items;
    }

    @NonNull
    @Override
    public CustomItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
      ViewGroup item =
          (ViewGroup)
              LayoutInflater.from(parent.getContext())
                  .inflate(
                      R.layout.cat_list_item_segmented_viewholder,
                      parent,
                      /* attachToRoot= */ false);
      return new CustomItemViewHolder(item);
    }
    @Override
    public void onBindViewHolder(
        @NonNull CustomItemViewHolder viewHolder, int position) {
      CustomListItemData data = getItemAt(position);
      viewHolder.bind(data);
    }
    @Override
    public int getItemCount() {
      return items.size();
    }
    @NonNull
    public CustomListItemData getItemAt(int i) {
      return items.get(i);
    }
    /**
     * Set exclusive selected position.
     */
    public void setSelectedPosition(int selectedPosition) {
      this.selectedPosition = selectedPosition;
    }
    /**
     * Return exclusive selected position.
     */
    public int getSelectedPosition() {
      return selectedPosition;
    }
  }
  /** A ViewHolder that shows custom list items */
  public class CustomItemViewHolder extends ListItemViewHolder {
    private final TextView textView;
    private final ListItemCardView cardView;

    public CustomItemViewHolder(@NonNull View itemView) {
      super(itemView);
      textView = itemView.findViewById(R.id.cat_list_item_text);
      cardView = itemView.findViewById(R.id.cat_list_item_card_view);
    }
    public void bind(@NonNull CustomListItemData data) {
      super.bind();
      textView.setText(data.text);
      cardView.setChecked(data.indexInSection == adapter.getSelectedPosition());
      cardView.setOnClickListener(
          v -> {
            int previouslySelectedPosition = adapter.getSelectedPosition();
            adapter.setSelectedPosition(data.indexInSection);
            Toast.makeText(v.getContext(), R.string.mtrl_list_item_clicked, Toast.LENGTH_SHORT)
                .show();
            adapter.notifyItemChanged(data.indexInSection);
            if (previouslySelectedPosition != NO_SELECTION) {
              adapter.notifyItemChanged(previouslySelectedPosition);
            }
          });
    }
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelableArrayList(KEY_LIST_DATA, listData);
    outState.putInt(KEY_SELECTED_POSITION, adapter.getSelectedPosition());
  }
}
