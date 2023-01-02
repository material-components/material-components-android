/*
 * Copyright 2018 The Android Open Source Project
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

package io.material.catalog.chip;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import com.google.android.material.chip.Chip;
import io.material.catalog.feature.DemoFragment;
import java.util.HashSet;
import java.util.Set;

/** A fragment that displays a demo of chips in a RecyclerView for the Catalog app. */
public class ChipRecyclerviewDemoFragment extends DemoFragment {

  private RecyclerView recyclerView;
  private ChipAdapter adapter;
  private RecyclerView.LayoutManager layoutManager;

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getChipContent(), viewGroup, false /* attachToRoot
  */);

    recyclerView = view.findViewById(R.id.chip_recyclerview_parent);
    layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
    recyclerView.setLayoutManager(layoutManager);

    adapter = new ChipAdapter();
    recyclerView.setAdapter(adapter);

    return view;
  }

  /** A RecyclerView adapter that displays the checkable chips. */
  public static class ChipAdapter extends RecyclerView.Adapter<ChipAdapter.MyViewHolder> {
    private final Set<Integer> checkedChipId = new HashSet<>(getItemCount());

    /** Provide a reference to the views for each data item. */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
      public Chip chip;

      public MyViewHolder(View view, Set<Integer> checkedChipId) {
        super(view);
        chip = (Chip) view;
        chip.setOnClickListener(
            v -> {
              Integer chipId = (Integer) v.getTag();
              if (chip.isChecked()) {
                checkedChipId.add(chipId);
              } else {
                checkedChipId.remove(chipId);
              }
            });
      }
    }

    public ChipAdapter() {}

    @Override
    public ChipAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view =
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.cat_chip_group_item_filter, parent, false);
      MyViewHolder vh = new MyViewHolder(view, checkedChipId);
      return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
      holder.chip.setTag(position);
      holder.chip.setChecked(checkedChipId.contains(position));
      holder.chip.setText(
          holder.chip.getResources().getString(R.string.cat_chip_text) + " " + position);
    }

    @Override
    public int getItemCount() {
      return 30;
    }
  }

  @LayoutRes
  protected int getChipContent() {
    return R.layout.cat_chip_recyclerview_fragment;
  }
}
