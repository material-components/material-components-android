/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.lists.viewholders;

import com.google.android.material.R;
import com.google.android.material.lists.MaterialViewHolder;

import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/** A simple two line list item. */
public class TwoLineItemViewHolder extends MaterialViewHolder {

  public ImageView icon;
  public TextView primaryText;
  public TextView secondaryText;

  public TwoLineItemViewHolder(@NonNull View view) {
    super(view);
    this.icon = itemView.findViewById(R.id.mtrl_list_item_icon);
    this.primaryText = itemView.findViewById(R.id.mtrl_list_item_text);
    this.secondaryText = itemView.findViewById(R.id.mtrl_list_item_secondary_text);
  }
  public TwoLineItemViewHolder(@NonNull ViewGroup parent) {
    this(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.material_list_item_two_line, parent, false));
  }
}
