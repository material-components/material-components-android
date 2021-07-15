package com.google.android.material.lists.viewHolders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/** A standard view holder that all specific Material view holders extend */
public abstract class MaterialViewHolder extends RecyclerView.ViewHolder {

    protected MaterialViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    protected MaterialViewHolder(int layout, @NonNull ViewGroup parent) {
        this(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    }
}
