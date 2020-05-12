package com.google.android.material.lists.viewHolders;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DimenRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.R;

/** A standard view holder that all specific Material view holders extend */
public abstract class MaterialViewHolder extends RecyclerView.ViewHolder {

  protected MaterialViewHolder(@NonNull View itemView) {
      super(itemView);
  }

  protected MaterialViewHolder(@LayoutRes int layout, @NonNull ViewGroup parent, @DimenRes int insetDividerXRes) {

    this(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    initializeDivider(insetDividerXRes);
  }

  View divider;
  private void initializeDivider(@DimenRes int insetDividerXRes){

    insetDividerX = itemView.getContext().getResources().getDimension(insetDividerXRes);
    divider = itemView.findViewById(R.id.material_list_item_divider);

    ViewGroup.LayoutParams params = divider.getLayoutParams();
    params.width = fullBleedDividerWidth;
    divider.setLayoutParams(params);
    this.insetDividerX = insetDividerX * this.itemView.getContext().getResources().getDisplayMetrics().density;

  }

  public boolean isInteractive() {return itemView.isClickable();}
  public void setInteractive(boolean isInteractive){itemView.setClickable(isInteractive);}

  private int fullBleedDividerWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
  protected float insetDividerX = 0;
  public enum DividerType{NONE, INSET, FULL_BLEED}

  public void setDivider(final DividerType dividerType) {

    float newDividerX = 0;

    switch (dividerType) {
      case NONE:
        newDividerX = fullBleedDividerWidth;
        break;
      case INSET:
        newDividerX = insetDividerX;
        break;
    }

    divider.setX(newDividerX);
  }
}
