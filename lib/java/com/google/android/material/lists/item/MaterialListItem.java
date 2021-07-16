/*
 * Copyright 2020 The Android Open Source Project
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
package com.google.android.material.lists.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.R;

public class MaterialListItem extends RecyclerView.ViewHolder {

  private Visual visual;
  private TextCollection textCollection;
  private SecondaryContent secondaryContent;

  @NonNull
  public Visual getVisual() {
    return visual;
  }

  @NonNull
  public TextView getOverlineText() {
    return textCollection.getOverlineText();
  }

  @NonNull
  public TextView getPrimaryText() {
    return textCollection.getPrimaryText();
  }

  @NonNull
  public TextView getSecondaryText() {
    return textCollection.getSecondaryText();
  }

  @NonNull
  public TextView getMetadata() {
    return secondaryContent.getMetadata();
  }

  @NonNull
  public SecondaryContent getSecondaryContent() {
    return secondaryContent;
  }

  @NonNull
  public FrameLayout getAction() {
    return secondaryContent.getAction();
  }

  public MaterialListItem(@NonNull ViewGroup parent) {

    super(inflateListItemLayout(parent));

    visual = itemView.findViewById(R.id.material_list_item_visual);
    textCollection = itemView.findViewById(R.id.material_list_item_text_collection);
    secondaryContent = itemView.findViewById(R.id.material_list_item_secondary_content);

    textCollection.setTotalLinesListener(new TotalLinesListener() {
      @Override
      public void onTotalLinesChange(int totalLines) {
        updateTotalLines(totalLines);
      }
    });

    updateTotalLines(0);
  }

  private static View inflateListItemLayout(@NonNull ViewGroup parent) {
    Context context = parent.getContext();
    LayoutInflater layoutInflater = LayoutInflater.from(context);
    View itemView = layoutInflater.inflate(R.layout.material_list_item, parent, false);
    return itemView;
  }

  private void updateTotalLines(int totalLines) {
    visual.onTotalLinesChange(totalLines);
    textCollection.onTotalLinesChange(totalLines);
    secondaryContent.onTotalLinesChange(totalLines);
  }
}
