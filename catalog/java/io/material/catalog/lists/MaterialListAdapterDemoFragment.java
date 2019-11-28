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

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.lists.MaterialListAdapter;
import com.google.android.material.lists.MaterialListBinder;
import com.google.android.material.lists.viewholders.SingleLineItemViewHolder;
import com.google.android.material.lists.viewholders.ThreeLineItemViewHolder;
import com.google.android.material.lists.viewholders.TwoLineItemViewHolder;

import java.util.Arrays;
import java.util.List;

import io.material.catalog.R;
import io.material.catalog.feature.DemoFragment;

/** A fragment that uses MaterialListAdapters to display different lists of cards */
public class MaterialListAdapterDemoFragment extends DemoFragment {

  @Override
  @SuppressWarnings("RestrictTo")
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {

    View layout = layoutInflater.inflate(R.layout.cat_material_list_adapter_fragment, viewGroup, false);

    RecyclerView recyclerView = layout.findViewById(R.id.recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.setAdapter(materialListAdapters[0]);

    final List<String> options = Arrays.asList("Single Line Item", "Two Line Item", "Three Line Item");

    AutoCompleteTextView editTextFilledExposedDropdown =
        layout.findViewById(R.id.filled_exposed_dropdown);
    editTextFilledExposedDropdown.setText(options.get(0));
    editTextFilledExposedDropdown.setAdapter(new ArrayAdapter<>(
        getContext(),
        R.layout.cat_popup_item,
        options));
    editTextFilledExposedDropdown.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        editTextFilledExposedDropdown.setAdapter(new ArrayAdapter<>(
            getContext(),
            R.layout.cat_popup_item,
            options));
        recyclerView.setAdapter(materialListAdapters[options.indexOf(s.toString())]);
      }
    });

    return layout;
  }

  //Defining a dataset of twenty Strings
  String[] listData = new String[]{
      "First Item",
      "Second Item",
      "Third Item",
      "Fourth Item",
      "Fifth Item",
      "Sixth Item",
      "Seventh Item",
      "Eighth Item",
      "Ninth Item",
      "Tenth Item",
      "Eleventh Item",
      "Twelfth Item",
      "Thirteenth Item",
      "Fourteenth Item",
      "Fifteenth Item",
      "Sixteenth Item",
      "Seventeenth Item",
      "Eighteenth Item",
      "Nineteenth Item",
      "Twentieth Item"};

  //Creating adapters using the dataset, where the title of each card corresponds to a data value
  MaterialListAdapter[] materialListAdapters = {
      new MaterialListAdapter<>(new MaterialListBinder<String, SingleLineItemViewHolder>(listData) {
        @Override
        public void onBind(SingleLineItemViewHolder viewHolder, String data, int position) {
          viewHolder.text.setText(data);
          viewHolder.icon.setImageResource(R.drawable.logo_avatar_anonymous_40dp);
        }
      }, SingleLineItemViewHolder.class),

      new MaterialListAdapter<>(new MaterialListBinder<String, TwoLineItemViewHolder>(listData) {
        @Override
        public void onBind(TwoLineItemViewHolder viewHolder, String data, int position) {
          viewHolder.primaryText.setText(data);
          viewHolder.secondaryText.setText(R.string.mtrl_list_item_secondary_text);
          viewHolder.icon.setImageResource(R.drawable.logo_avatar_anonymous_40dp);
        }
      }, TwoLineItemViewHolder.class)

      ,
      new MaterialListAdapter<>(new MaterialListBinder<String, ThreeLineItemViewHolder>() {
        @Override
        public void onBind(ThreeLineItemViewHolder viewHolder, String data, int position) {
          viewHolder.primaryText.setText(data);
          viewHolder.secondaryText.setText(R.string.mtrl_list_item_secondary_text);
          viewHolder.tertiaryText.setText(R.string.mtrl_list_item_tertiary_text);
          viewHolder.icon.setImageResource(R.drawable.logo_avatar_anonymous_40dp);
        }
      }, ThreeLineItemViewHolder.class)};
}
