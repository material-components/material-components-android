package com.google.android.material.lists.viewHolders.singleLine;
/*
 * Copyright (C) 2020 The Android Open Source Project
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

import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.R;
import com.google.android.material.lists.viewHolders.MaterialViewHolder;

/**
 * A single-lined ViewHolder with space for only one line of text
 */
public class SingleLineLongerTextItemViewHolder extends MaterialViewHolder {

  public TextView primaryText;

  public SingleLineLongerTextItemViewHolder(@NonNull ViewGroup parent) {

    super(R.layout.single_line_longer_text_item, parent, R.dimen.material_list_item_inset_position_small);
    this.primaryText = itemView.findViewById(R.id.material_list_item_primary_text);
  }

}
