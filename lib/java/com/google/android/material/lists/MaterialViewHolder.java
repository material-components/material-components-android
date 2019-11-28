package com.google.android.material.lists;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/** A standard view holder that all specific Material view holders extend */
public abstract class MaterialViewHolder extends RecyclerView.ViewHolder {

  public MaterialViewHolder(@NonNull View itemView) {
    super(itemView);
  }
}
