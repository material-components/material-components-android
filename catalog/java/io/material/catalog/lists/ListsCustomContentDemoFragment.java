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

package io.material.catalog.lists;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.listitem.ListItemViewHolder;
import io.material.catalog.feature.DemoFragment;
import java.util.ArrayList;
import java.util.List;

/** A fragment that displays a List demos with custom content for the Catalog app. */
public class ListsCustomContentDemoFragment extends DemoFragment {

  @NonNull
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    RecyclerView view =
        (RecyclerView) layoutInflater.inflate(R.layout.cat_lists_fragment, viewGroup, false);

    view.setLayoutManager(new LinearLayoutManager(getContext()));
    List<CustomCardData> data = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      data.add(new CustomCardData(i+1));
    }

    view.setAdapter(new ListsAdapter(data));
    view.addItemDecoration(new MarginItemDecoration(getContext()));

    return view;
  }

  /** An Adapter that shows custom list items */
  public static class ListsAdapter extends Adapter<CustomItemViewHolder> {

    private final List<CustomCardData> items;

    public ListsAdapter(@NonNull List<CustomCardData> items) {
      this.items = items;
    }

    @NonNull
    @Override
    public CustomItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
      ViewGroup item =
          (ViewGroup)
              LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_list_item_viewholder, parent, /* attachToRoot= */ false);
      return new CustomItemViewHolder(item);
    }

    @Override
    public void onBindViewHolder(
        @NonNull CustomItemViewHolder viewHolder, int position) {
      CustomCardData data = getItemAt(position);
      viewHolder.bind(data);
    }

    @Override
    public int getItemCount() {
      return items.size();
    }

    @NonNull
    public CustomCardData getItemAt(int i) {
      return items.get(i);
    }
  }

  static class CustomCardData {
    int cardNumber;
    boolean checked;
    public CustomCardData(int i) {
      cardNumber = i;
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

  /** A ViewHolder that shows a custom list item */
  public static class CustomItemViewHolder extends ListItemViewHolder {

    private final ImageView startButton;
    private final ImageView endButton;
    private final TextView text;
    private final MaterialCardView cardView;


    public CustomItemViewHolder(@NonNull View itemView) {
      super(itemView);
      startButton = itemView.findViewById(R.id.cat_list_item_start_icon);
      endButton = itemView.findViewById(R.id.cat_list_item_end_icon);
      text = itemView.findViewById(R.id.cat_list_item_text);
      cardView = itemView.findViewById(R.id.cat_list_item_card_view);
    }

    public void bind(@NonNull CustomCardData data) {
      super.bind();
      text.setText(String.valueOf(data.cardNumber));
      startButton.setImageResource(R.drawable.logo_avatar_anonymous_40dp);
      endButton.setImageResource(R.drawable.ic_drag_handle_vd_theme_24px);

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
