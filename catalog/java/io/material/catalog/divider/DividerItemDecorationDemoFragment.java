/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.divider;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import io.material.catalog.feature.DemoFragment;

/** Demo of the MaterialDividerItemDecoration. */
public class DividerItemDecorationDemoFragment extends DemoFragment {

  @Nullable
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_divider_recyclerview_fragment, viewGroup, /* attachToRoot */ false);

    RecyclerView recyclerViewHorizontal = view.findViewById(R.id.divider_recyclerview_horizontal);
    RecyclerView recyclerViewVertical = view.findViewById(R.id.divider_recyclerview_vertical);

    setUpDividers(recyclerViewHorizontal, LinearLayoutManager.HORIZONTAL);
    setUpDividers(recyclerViewVertical, LinearLayoutManager.VERTICAL);

    return view;
  }

  private void setUpDividers(@NonNull RecyclerView recyclerView, int orientation) {
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(),
        orientation, false);
    recyclerView.setLayoutManager(layoutManager);

    MaterialDividerItemDecoration divider =
        new MaterialDividerItemDecoration(getContext(), orientation);
    recyclerView.addItemDecoration(divider);

    DividerAdapter adapter = new DividerAdapter();
    recyclerView.setAdapter(adapter);
  }

  /** A RecyclerView adapter. */
  private static final class DividerAdapter
      extends RecyclerView.Adapter<DividerAdapter.MyViewHolder> {

    private static final int ITEM_COUNT = 20;

    /** Provide a reference to the views for each data item. */
    private static class MyViewHolder extends RecyclerView.ViewHolder {

      TextView item;

      public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        item = (TextView) itemView;
      }
    }

    public DividerAdapter() {}

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view =
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.cat_divider_recyclerview_item, parent, false);
      return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
      holder.item.setText(
          holder.item.getResources().getString(R.string.cat_divider_item_text, position + 1));
    }

    @Override
    public int getItemCount() {
      return ITEM_COUNT;
    }
  }
}
