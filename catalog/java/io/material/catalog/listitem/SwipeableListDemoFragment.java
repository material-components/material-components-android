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

import static com.google.android.material.listitem.SwipeableListItem.STATE_CLOSED;
import static com.google.android.material.listitem.SwipeableListItem.STATE_DRAGGING;
import static com.google.android.material.listitem.SwipeableListItem.STATE_OPEN;
import static com.google.android.material.listitem.SwipeableListItem.STATE_SETTLING;
import static com.google.android.material.listitem.SwipeableListItem.STATE_SWIPE_PRIMARY_ACTION;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.listitem.ListItemCardView;
import com.google.android.material.listitem.ListItemCardView.SwipeCallback;
import com.google.android.material.listitem.ListItemLayout;
import com.google.android.material.listitem.ListItemViewHolder;
import com.google.android.material.listitem.RevealableListItem;
import java.util.ArrayList;
import java.util.List;

/** A fragment that displays a swipeable List demo for the Catalog app. */
public class SwipeableListDemoFragment extends ListsMainDemoFragment {

  private static final String KEY_LIST_DATA = "key_list_data";

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
    view.setAdapter(adapter);
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
                  .inflate(
                      R.layout.cat_list_item_swipeable_viewholder,
                      parent,
                      /* attachToRoot= */ false);
      return new CustomItemViewHolder(item, this);
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

    public void removeItemAt(int i) {
      items.remove(i);
      notifyItemRemoved(i);
    }
  }

  /** A ViewHolder that shows custom list items */
  public static class CustomItemViewHolder extends ListItemViewHolder {
    private CustomListItemData data;
    private final TextView textView;
    private final ListItemCardView cardView;
    private final ListItemLayout listItemLayout;
    private final MaterialButton startActionsButton;
    private final MaterialButton endActionsButton;
    private final MaterialButton addActionButton;
    private final MaterialButton starActionButton;
    private final MaterialButton deleteActionButton;
    private final Drawable backArrow;
    private final Drawable forwardArrow;

    public CustomItemViewHolder(@NonNull View itemView, @NonNull ListsAdapter adapter) {
      super(itemView);
      backArrow = itemView.getResources().getDrawable(R.drawable.ic_arrow_back_24px);
      forwardArrow = itemView.getResources().getDrawable(R.drawable.ic_arrow_forward_24px);
      listItemLayout = (ListItemLayout) itemView;
      textView = itemView.findViewById(R.id.cat_list_item_text);
      cardView = itemView.findViewById(R.id.cat_list_item_card_view);
      addActionButton = itemView.findViewById(R.id.cat_list_action_add_button);
      starActionButton = itemView.findViewById(R.id.cat_list_action_star_button);
      deleteActionButton = itemView.findViewById(R.id.cat_list_action_delete_button);
      startActionsButton = itemView.findViewById(R.id.cat_list_item_start_icon);
      endActionsButton = itemView.findViewById(R.id.cat_list_item_end_icon);

      addActionButton.setOnClickListener(
          v -> Toast.makeText(v.getContext(), R.string.cat_list_item_add_action_clicked, Toast.LENGTH_SHORT)
              .show());
      starActionButton.setOnClickListener(
          v -> Toast.makeText(v.getContext(), R.string.cat_list_item_star_action_clicked, Toast.LENGTH_SHORT)
              .show());
      deleteActionButton.setOnClickListener(
          v ->
              Toast.makeText(
                      v.getContext(),
                      R.string.cat_list_item_delete_action_clicked,
                      Toast.LENGTH_SHORT)
                  .show());
      startActionsButton.setOnClickListener(
          v -> {
            if (listItemLayout.getSwipeState() == STATE_CLOSED) {
              listItemLayout.setSwipeState(STATE_SWIPE_PRIMARY_ACTION, Gravity.START);
            } else {
              listItemLayout.setSwipeState(STATE_CLOSED, Gravity.START);
            }
          });
      endActionsButton.setOnClickListener(
          v -> {
            if (listItemLayout.getSwipeState() == STATE_CLOSED) {
              listItemLayout.setSwipeState(STATE_OPEN, Gravity.END);
            } else {
              listItemLayout.setSwipeState(STATE_CLOSED, Gravity.END);
            }
          });
      cardView.setOnClickListener(
          v -> {
            Toast.makeText(v.getContext(), R.string.mtrl_list_item_clicked, Toast.LENGTH_SHORT)
                .show();
          });

      cardView.addSwipeCallback(
          new SwipeCallback() {
            @Override
            public void onSwipe(int swipeOffset) {}

            @Override
            public <T extends View & RevealableListItem> void onSwipeStateChanged(
                int newState, T activeRevealableListItem, int gravity) {
              if (data == null) {
                return;
              }
              if (newState == STATE_SWIPE_PRIMARY_ACTION) {
                Toast.makeText(
                        cardView.getContext(),
                        R.string.cat_list_item_primary_action,
                        Toast.LENGTH_SHORT)
                    .show();
                if (gravity == Gravity.START) {
                  int position = getBindingAdapterPosition();
                  if (position != RecyclerView.NO_POSITION) {
                    adapter.removeItemAt(position);
                  }
                } else if (gravity == Gravity.END) {
                  itemView.postDelayed(
                      () -> listItemLayout.setSwipeState(STATE_CLOSED, gravity), 500);
                }
              }
              if (newState != STATE_DRAGGING && newState != STATE_SETTLING) {
                data.swipeState = newState;
                data.swipeGravity = gravity;
              }
              endActionsButton.setIcon(data.swipeState == STATE_CLOSED ? forwardArrow : backArrow);
              endActionsButton.setContentDescription(
                  data.swipeState == STATE_CLOSED
                      ? itemView
                          .getContext()
                          .getString(R.string.cat_list_item_show_end_actions_content_description)
                      : itemView
                          .getContext()
                          .getString(R.string.cat_list_item_hide_end_actions_content_description));
            }
          });
    }

    public void bind(@NonNull CustomListItemData data) {
      super.bind();
      this.data = data;
      textView.setText(data.text);
      endActionsButton.setIcon(data.swipeState == STATE_CLOSED ? forwardArrow : backArrow);
      listItemLayout.setSwipeState(data.swipeState, data.swipeGravity, /* animate= */ false);
    }
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelableArrayList(KEY_LIST_DATA, listData);
  }
}
