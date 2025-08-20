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

package io.material.catalog.listitem;

import io.material.catalog.R;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.listitem.ListItemViewHolder;
import io.material.catalog.feature.DemoFragment;
import java.util.ArrayList;
import java.util.List;

/** A fragment that displays the main List demos for the Catalog app. */
public class ListsMainDemoFragment extends DemoFragment {
  @NonNull
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    RecyclerView view =
        (RecyclerView) layoutInflater.inflate(R.layout.cat_lists_fragment, viewGroup, false);

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
  public static class ListsAdapter extends Adapter<CustomItemViewHolder> {

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
                  .inflate(R.layout.cat_list_item_viewholder, parent, /* attachToRoot= */ false);
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
    private final MaterialCardView cardView;


    public CustomItemViewHolder(@NonNull View itemView) {
      super(itemView);
      textView = itemView.findViewById(R.id.cat_list_item_text);
      cardView = itemView.findViewById(R.id.cat_list_item_card_view);
    }

    public void bind(@NonNull CustomListItemData data) {
      super.bind();
      textView.setText(data.text);
      cardView.setChecked(data.checked);
      cardView.setOnClickListener(
          v -> {
            Toast.makeText(v.getContext(), R.string.mtrl_list_item_clicked, Toast.LENGTH_SHORT)
                .show();
            cardView.toggle();
            data.checked = !data.checked;
          });
    }
  }
}
