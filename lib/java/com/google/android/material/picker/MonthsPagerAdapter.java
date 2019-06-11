/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.picker;

import com.google.android.material.R;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.material.picker.MaterialCalendar.OnDayClickListener;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import android.util.SparseArray;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;
import java.util.List;

/**
 * Manages the instances of {@link MonthFragment}, capping memory usage.
 *
 * @hide
 */
class MonthsPagerAdapter extends FragmentStateAdapter {

  private final Month firstPage;
  private final Month lastPage;
  private final int startIndex;
  private final GridSelector<?> gridSelector;
  private final SparseArray<AdapterDataObserver> observingFragments = new SparseArray<>();
  private final OnDayClickListener onDayClickListener;
  private final int itemHeight;

  /**
   * Creates a new {@link FragmentStateAdapter} that manages instances of {@link MonthFragment}.
   *
   * @param context The {@link Context} with the calendar theme and dimensions.
   * @param fragmentManager A {@link FragmentManager} for the {@link MonthFragment} objects. {@see
   *     Fragment#getChildFragmentManager()} and {@see
   *     FragmentActivity#getSupportFragmentManager()}.
   * @param lifecycle The {@link Lifecycle} to manage each {@link MonthFragment}. {@see
   *     Fragment#getLifecycle()} and {@see FragmentActivity#getLifecycle()}.
   * @param gridSelector The {@link GridSelector} that controls selection and highlights for all
   *     {@link MonthFragment} objects.
   * @param firstPage The earliest accessible {@link Month}. This {@link Month} will be at position
   *     0. {@link MonthsPagerAdapter#createFragment(int)}.
   * @param lastPage The latest accessible {@link Month}. Must be chronologically after or the same
   *     as {@code firstPage}.
   * @param startPage The starting {@link Month} displayed. Must be chronologically between {@code
   *     firstPage} and {@code lastPage} inclusive.
   */
  MonthsPagerAdapter(
      Context context,
      FragmentManager fragmentManager,
      Lifecycle lifecycle,
      GridSelector<?> gridSelector,
      Month firstPage,
      Month lastPage,
      Month startPage,
      OnDayClickListener onDayClickListener) {
    super(fragmentManager, lifecycle);
    if (firstPage.compareTo(startPage) > 0) {
      throw new IllegalArgumentException("firstPage cannot be after startPage");
    }
    if (startPage.compareTo(lastPage) > 0) {
      throw new IllegalArgumentException("startPage cannot be after lastPage");
    }

    int daysHeight = MonthAdapter.MAXIMUM_WEEKS * MaterialCalendar.getDayHeight(context);
    int daySpacingHeight =
        (MonthAdapter.MAXIMUM_WEEKS - 1)
            * context
                .getResources()
                .getDimensionPixelSize(R.dimen.mtrl_calendar_day_spacing_vertical);
    int labelHeight =
        MaterialPickerDialogFragment.isFullScreen(context)
            ? MaterialCalendar.getDayHeight(context)
            : 0;

    this.itemHeight = daysHeight + daySpacingHeight + labelHeight;
    this.firstPage = firstPage;
    this.lastPage = lastPage;
    startIndex = firstPage.monthsUntil(startPage);
    this.gridSelector = gridSelector;
    this.onDayClickListener = onDayClickListener;
  }

  @Override
  public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
    // TODO: Remove ViewPager2 workaround
    recyclerView.clearOnChildAttachStateChangeListeners();
  }

  @Override
  public void onBindViewHolder(
      @NonNull FragmentViewHolder holder, int position, @NonNull List<Object> payloads) {
    super.onBindViewHolder(holder, position, payloads);
    holder.itemView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, itemHeight));
  }

  @Override
  public int getItemCount() {
    return firstPage.monthsUntil(lastPage) + 1;
  }

  @Override
  public MonthFragment createFragment(final int position) {
    final MonthFragment monthFragment =
        MonthFragment.newInstance(firstPage.monthsLater(position), gridSelector);

    monthFragment
        .getLifecycle()
        .addObserver(
            new LifecycleObserver() {

              @OnLifecycleEvent(Event.ON_CREATE)
              void onCreated() {
                monthFragment.setOnDayClickListener(onDayClickListener);
                AdapterDataObserver dataSetObserver =
                    new AdapterDataObserver() {
                      @Override
                      public void onChanged() {
                        monthFragment.notifyDataSetChanged();
                      }
                    };
                registerAdapterDataObserver(dataSetObserver);
                observingFragments.put(position, dataSetObserver);
              }

              @OnLifecycleEvent(Event.ON_DESTROY)
              void onDestroyed() {
                AdapterDataObserver dataSetObserver = observingFragments.get(position);
                if (dataSetObserver != null) {
                  observingFragments.remove(position);
                  unregisterAdapterDataObserver(dataSetObserver);
                }
              }
            });

    return monthFragment;
  }

  /** Returns the position index of the {@link Month} startPage provided on construction. */
  int getStartPosition() {
    return startIndex;
  }

  @NonNull
  CharSequence getPageTitle(int position) {
    return getPageMonth(position).getLongName();
  }

  Month getPageMonth(int position) {
    return firstPage.monthsLater(position);
  }
}
