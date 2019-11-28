package com.google.android.material.lists;

import java.util.Arrays;
import java.util.List;

/** Interface that defines interactions between {@link MaterialViewHolder}s and data */
public class MaterialListBinder<T, E extends MaterialViewHolder> {

  protected List<T> data;

  public MaterialListBinder() {
  }

  public MaterialListBinder(List<T> data) {
    this.data = data;
  }

  public MaterialListBinder(T[] data) {
    this.data = Arrays.asList(data);
  }

  public void onBind(E viewHolder, T data, int position) {}

  //TODO: Add other methods that allow items to be added, read, updated and deleted
}
