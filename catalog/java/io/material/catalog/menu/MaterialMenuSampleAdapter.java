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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

/** Sample adapter for menus. */
public class MaterialMenuSampleAdapter extends Adapter<MaterialMenuSampleAdapter.ViewHolder> {

  private final String[] dataset =
      new String[] {
        "Item 1", "Item 2", "Item 3",
      };

  /** Simple view holder. */
  public static class ViewHolder extends RecyclerView.ViewHolder {

    public Button root;

    public ViewHolder(Button root) {
      super(root);
      this.root = root;
    }
  }

  @Override
  public MaterialMenuSampleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    Button button =
        (Button)
            LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);

    ViewHolder viewHolder = new ViewHolder(button);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull MaterialMenuSampleAdapter.ViewHolder holder, int position) {
    holder.root.setText(dataset[position]);
  }

  @Override
  public int getItemCount() {
    return dataset.length;
  }
}
