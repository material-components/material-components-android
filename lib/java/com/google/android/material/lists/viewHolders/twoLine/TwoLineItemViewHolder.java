package com.google.android.material.lists.viewHolders.twoLine;
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
 * A two-lined ViewHolder with title text, one line of subtext and small meta text
 */
public class TwoLineItemViewHolder extends MaterialViewHolder {

    public TextView primaryText;
    public TextView secondaryText;
    public TextView meta;

    public TwoLineItemViewHolder(@NonNull ViewGroup parent) {

        super(R.layout.two_line_item, parent);
        this.primaryText = itemView.findViewById(R.id.material_list_item_primary_text);
        this.secondaryText = itemView.findViewById(R.id.material_list_item_secondary_text);
        this.meta = itemView.findViewById(R.id.material_list_item_meta);

    }

}
