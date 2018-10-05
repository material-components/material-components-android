/*
 * Copyright 2018 The Android Open Source Project
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

package io.material.catalog.menu;

import io.material.catalog.R;

import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

/** List adapter for menus catalog demo. */
public class MenuListAdapter extends Adapter<MenuListAdapter.ViewHolder> {

  private final String[] dataset =
      new String[] {
        "Menu type 1", "Menu type 2", "Menu type 3",
      };

  public MenuListAdapter() {}

  /** View holder to add menus menus. */
  public static class ViewHolder extends RecyclerView.ViewHolder {

    private final View root;

    public ViewHolder(View root) {
      super(root);
      this.root = root;
    }
  }

  @Override
  public MenuListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

    ViewHolder vh = new ViewHolder(v);
    return vh;
  }

  @Override
  public void onBindViewHolder(@NonNull MenuListAdapter.ViewHolder holder, int position) {
    ListItem listItem = new ListItem(holder.root);
    listItem.textView.setText(dataset[position]);
    listItem.textView.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            PopupMenu popup = new PopupMenu(holder.root.getContext(), v);
            // Inflating the Popup using xml file
            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
            popup.show();
          }
        });
  }

  @Override
  public int getItemCount() {
    return dataset.length;
  }

  static class ListItem {

    final TextView textView;

    ListItem(View root) {
      textView = root.findViewById(R.id.text_view);
    }
  }
}
