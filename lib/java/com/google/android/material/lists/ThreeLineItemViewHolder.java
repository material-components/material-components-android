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

package com.google.android.material.lists;

import com.google.android.material.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;

/** A simple three line list item. */
public class ThreeLineItemViewHolder extends TwoLineItemViewHolder {

  public final TextView tertiary;

  public ThreeLineItemViewHolder(@NonNull View view) {
    super(view);
    this.tertiary = itemView.findViewById(R.id.mtrl_list_item_tertiary_text);
  }

  @NonNull
  public static ThreeLineItemViewHolder create(@NonNull ViewGroup parent) {
    return new ThreeLineItemViewHolder(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.material_list_item_three_line, parent, false));
  }
}
