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

package io.material.catalog.lists;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.lists.SingleLineItemViewHolder;
import com.google.android.material.lists.ThreeLineItemViewHolder;
import com.google.android.material.lists.TwoLineItemViewHolder;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the main List demos for the Catalog app. */
public class ListsMainDemoFragment extends DemoFragment {

  @Override
  @SuppressWarnings("RestrictTo")
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    RecyclerView view =
        (RecyclerView) layoutInflater.inflate(R.layout.cat_lists_fragment, viewGroup, false);

    view.setLayoutManager(new LinearLayoutManager(getContext()));
    view.setAdapter(new ListsMainDemoAdapter());

    return view;
  }

  /** An Adapter that shows Single, Two, and Three line list items */
  public static class ListsMainDemoAdapter extends Adapter<ViewHolder> {

    static final int ITEM_SINGLE_LINE = 0;
    static final int ITEM_TWO_LINE = 1;
    static final int ITEM_THREE_LINE = 2;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
      switch (getItemViewType(position)) {
        case ITEM_SINGLE_LINE:
          return SingleLineItemViewHolder.create(parent);
        case ITEM_TWO_LINE:
          return TwoLineItemViewHolder.create(parent);
        case ITEM_THREE_LINE:
          return ThreeLineItemViewHolder.create(parent);
        default: // fall out
      }
      throw new RuntimeException();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
      switch (getItemViewType(position)) {
        case ITEM_SINGLE_LINE:
          bind((SingleLineItemViewHolder) viewHolder);
          break;
        case ITEM_TWO_LINE:
          bind((TwoLineItemViewHolder) viewHolder);
          break;
        case ITEM_THREE_LINE:
          bind((ThreeLineItemViewHolder) viewHolder);
          break;
        default: // fall out
      }

      viewHolder.itemView.setOnClickListener(
          v ->
              Toast.makeText(v.getContext(), R.string.mtrl_list_item_clicked, Toast.LENGTH_SHORT)
                  .show());
    }

    private void bind(SingleLineItemViewHolder vh) {
      vh.text.setText(R.string.mtrl_list_item_one_line);
      vh.icon.setImageResource(R.drawable.logo_avatar_anonymous_40dp);
    }

    private void bind(TwoLineItemViewHolder vh) {
      vh.text.setText(R.string.mtrl_list_item_two_line);
      vh.secondary.setText(R.string.mtrl_list_item_secondary_text);
      vh.icon.setImageResource(R.drawable.logo_avatar_anonymous_40dp);
    }

    private void bind(ThreeLineItemViewHolder vh) {
      vh.text.setText(R.string.mtrl_list_item_three_line);
      vh.secondary.setText(R.string.mtrl_list_item_secondary_text);
      vh.tertiary.setText(R.string.mtrl_list_item_tertiary_text);
      vh.icon.setImageResource(R.drawable.logo_avatar_anonymous_40dp);
    }

    @Override
    public int getItemViewType(int position) {
      return position % 3;
    }

    @Override
    public int getItemCount() {
      return 1000;
    }
  }
}
