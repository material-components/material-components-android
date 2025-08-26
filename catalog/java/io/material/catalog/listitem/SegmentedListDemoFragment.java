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

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** A fragment that displays a segmented List demos for the Catalog app. */
public class SegmentedListDemoFragment extends ListsMainDemoFragment {

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
    List<CustomListItemData> data = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      data.add(
          new CustomListItemData(
              String.format(view.getContext().getString(R.string.cat_list_item_text), i + 1),
              i,
              20));
    }

    view.setAdapter(new ListsAdapter(data));
    view.addItemDecoration(new MarginItemDecoration(getContext()));

    return view;
  }

  /** An Adapter that shows custom list items */
  public static class ListsAdapter extends ListsMainDemoFragment.ListsAdapter {

    public ListsAdapter(@NonNull List<CustomListItemData> items) {
      super(items);
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
  }
}
