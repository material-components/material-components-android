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

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.listitem.ListItemCardView;
import com.google.android.material.listitem.ListItemViewHolder;
import io.material.catalog.feature.DemoFragment;
import java.util.ArrayList;
import java.util.List;

/** A fragment that displays a multi-section List demo for the Catalog app. */
public class MultiSectionListDemoFragment extends DemoFragment {

  @NonNull
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    RecyclerView view =
        (RecyclerView) layoutInflater.inflate(R.layout.cat_lists_bright_background_fragment, viewGroup, false);
    view.setLayoutManager(new LinearLayoutManager(getContext()));
    List<CustomListItemData> data = new ArrayList<>();
    int itemCount = 0;
    data.add(
        new CustomListItemData(
            String.format(view.getContext().getString(R.string.cat_list_item_text), 0), 0, 1));
    itemCount += 1;

    data.add(new CustomListItemData("Subheader 1"));

    for (int i = 0; i < 3; i++) {
      data.add(
          new CustomListItemData(
              String.format(
                  view.getContext().getString(R.string.cat_list_item_text), itemCount + i),
              i,
              3));
    }

    data.add(new CustomListItemData("Subheader 2"));

    for (int i = 0; i < 5; i++) {
      data.add(
          new CustomListItemData(
              String.format(
                  view.getContext().getString(R.string.cat_list_item_text), itemCount + i),
              i,
              5));
    }

    view.setAdapter(new ListsAdapter(data));
    view.addItemDecoration(new MarginItemDecoration(getContext()));

    return view;
  }

  /** An Adapter that shows custom list items */
  public static class ListsAdapter extends Adapter<ViewHolder> {

    private static final int VIEW_TYPE_SUBHEADING= 1;
    private static final int VIEW_TYPE_LIST_ITEM = 2;
    private final List<CustomListItemData> items;

    public ListsAdapter(@NonNull List<CustomListItemData> items) {
      this.items = items;
    }

    @Nullable
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      switch (viewType) {
        case VIEW_TYPE_LIST_ITEM:
          ViewGroup item =
              (ViewGroup)
                  LayoutInflater.from(parent.getContext())
                      .inflate(
                          R.layout.cat_list_multisection_viewholder,
                          parent,
                          /* attachToRoot= */ false);
          return new CustomItemViewHolder(item);
        case VIEW_TYPE_SUBHEADING:
          TextView subheader =
              (TextView)
                  LayoutInflater.from(parent.getContext())
                      .inflate(R.layout.cat_list_item_subheader, parent, /* attachToRoot= */ false);
          return new SubheaderViewHolder(subheader);
        default: // fall out
      }
      return null;
    }

    @Override
    public int getItemViewType(int position) {
      CustomListItemData data = getItemAt(position);
      if (data.subheading != null) {
        return VIEW_TYPE_SUBHEADING;
      }
      return VIEW_TYPE_LIST_ITEM;
    }

    @Override
    public void onBindViewHolder(
        @NonNull ViewHolder viewHolder, int position) {
      CustomListItemData data = getItemAt(position);
      if (getItemViewType(position) == VIEW_TYPE_SUBHEADING) {
        ((SubheaderViewHolder) viewHolder).bind(data);
      } else if (getItemViewType(position) == VIEW_TYPE_LIST_ITEM) {
        ((CustomItemViewHolder) viewHolder).bind(data);
      }
    }

    @Override
    public int getItemCount() {
      return items.size();
    }

    @NonNull
    public CustomListItemData getItemAt(int i) {
      return items.get(i);
    }
  }

  static class MarginItemDecoration extends ItemDecoration {
    private final int itemMargin;

    public MarginItemDecoration(Context context) {
      itemMargin = context.getResources().getDimensionPixelSize(R.dimen.cat_list_item_margin);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect,
        @NonNull View view,
        @NonNull RecyclerView parent,
        @NonNull RecyclerView.State state) {
      int position = parent.getChildAdapterPosition(view);
      if (position != state.getItemCount() - 1) {
        outRect.bottom = itemMargin;
      }
    }
  }

  /** A ViewHolder that shows custom list items */
  public static class CustomItemViewHolder extends ListItemViewHolder {

    private final TextView textView;
    private final ListItemCardView cardView;

    public CustomItemViewHolder(@NonNull View itemView) {
      super(itemView);
      textView = itemView.findViewById(R.id.cat_list_item_text);
      cardView = itemView.findViewById(R.id.cat_list_item_card_view);
    }

    public void bind(@NonNull CustomListItemData data) {
      super.bind(data.indexInSection, data.sectionCount);
      textView.setText(data.text);
      cardView.setOnClickListener(
          v -> Toast.makeText(v.getContext(), R.string.mtrl_list_item_clicked, Toast.LENGTH_SHORT)
              .show());
    }
  }

  /** A ViewHolder that shows a subheader list item */
  public static class SubheaderViewHolder extends ViewHolder {

    private final TextView text;

    public SubheaderViewHolder(@NonNull View itemView) {
      super(itemView);
      text = itemView.findViewById(R.id.cat_list_subheader_text);
    }

    public void bind(@NonNull CustomListItemData data) {
      text.setText(data.subheading);
    }
  }
}
