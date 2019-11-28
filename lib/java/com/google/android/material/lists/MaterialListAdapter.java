package com.google.android.material.lists;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.InvocationTargetException;

/**
 * An adapter that uses {@link MaterialViewHolder}s and binds data to them using {@link MaterialListBinder}
 */
public class MaterialListAdapter<T, E extends MaterialViewHolder> extends RecyclerView.Adapter<E> {

  private MaterialListBinder<T, E> materialListBinder;
  private Class<E> viewHolderClass;

  public MaterialListAdapter(MaterialListBinder<T, E> materialListBinder, Class<E> viewHolderClass) {
    this.materialListBinder = materialListBinder;
    this.viewHolderClass = viewHolderClass;
  }

  @NonNull
  @Override
  public E onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    try {
      return viewHolderClass.getConstructor(ViewGroup.class).newInstance(parent);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(viewHolderClass.getName() +
          " needs to make the constructor with only a ViewGroup parameter public", e);
    } catch (InstantiationException e) {
      throw new RuntimeException(viewHolderClass.getName() +
          " needs to be a standard class and not abstract", e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(viewHolderClass.getName() +
          " 's constructor with only a ViewGroup parameter threw an exception ", e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(viewHolderClass.getName() +
          " needs a public constructor with only a ViewGroup parameter", e);
    }

  }

  @Override
  public int getItemCount() {
    return materialListBinder.data.size();
  }

  public void onBindViewHolder(@NonNull E holder, int position) {
    materialListBinder.onBind(holder, materialListBinder.data.get(position), position);
  }
}

