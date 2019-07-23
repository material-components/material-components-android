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

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import android.util.SparseArray;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;
import com.google.android.material.picker.MaterialCalendar.OnDayClickListener;
import java.util.List;

/**
 * Manages the instances of {@link MonthFragment}, capping memory usage.
 *
 * @hide
 */
class MonthsPagerAdapter extends FragmentStateAdapter {

  private final CalendarConstraints calendarConstraints;
  private final DateSelector<?> dateSelector;
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
   * @param dateSelector The {@link DateSelector} that controls selection and highlights for all
   *     {@link MonthFragment} objects.
   * @param calendarConstraints The {@link CalendarConstraints} that specifies the valid range and
   *     starting point for selection.
   */
  MonthsPagerAdapter(
      Context context,
      FragmentManager fragmentManager,
      Lifecycle lifecycle,
      DateSelector<?> dateSelector,
      CalendarConstraints calendarConstraints,
      OnDayClickListener onDayClickListener) {
    super(fragmentManager, lifecycle);
    Month firstPage = calendarConstraints.getStart();
    Month lastPage = calendarConstraints.getEnd();
    Month currentPage = calendarConstraints.getOpening();

    if (firstPage.compareTo(currentPage) > 0) {
      throw new IllegalArgumentException("firstPage cannot be after currentPage");
    }
    if (currentPage.compareTo(lastPage) > 0) {
      throw new IllegalArgumentException("currentPage cannot be after lastPage");
    }

    int daysHeight = MonthAdapter.MAXIMUM_WEEKS * MaterialCalendar.getDayHeight(context);
    int labelHeight =
        MaterialDatePicker.isFullscreen(context) ? MaterialCalendar.getDayHeight(context) : 0;

    this.itemHeight = daysHeight + labelHeight;
    this.calendarConstraints = calendarConstraints;
    this.dateSelector = dateSelector;
    this.onDayClickListener = onDayClickListener;
  }

  @Override
  public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
    // TODO(b/134764679): Remove ViewPager2 workaround
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
    return calendarConstraints.getMonthSpan();
  }

  @Override
  public MonthFragment createFragment(final int position) {
    final MonthFragment monthFragment =
        MonthFragment.newInstance(
            calendarConstraints.getStart().monthsLater(position),
            dateSelector,
            calendarConstraints);

    monthFragment
        .getLifecycle()
        .addObserver(
            new LifecycleEventObserver() {

              @Override
              public void onStateChanged(
                  @NonNull LifecycleOwner lifecycleOwner, @NonNull Event event) {
                switch (event) {
                  case ON_CREATE:
                    onCreated();
                    break;
                  case ON_DESTROY:
                    onDestroyed();
                    break;
                  default:
                    // do nothing
                    break;
                }
              }

              private void onCreated() {
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

              private void onDestroyed() {
                AdapterDataObserver dataSetObserver = observingFragments.get(position);
                if (dataSetObserver != null) {
                  observingFragments.remove(position);
                  unregisterAdapterDataObserver(dataSetObserver);
                }
              }
            });
    return monthFragment;
  }

  @NonNull
  CharSequence getPageTitle(int position) {
    return getPageMonth(position).getLongName();
  }

  Month getPageMonth(int position) {
    return calendarConstraints.getStart().monthsLater(position);
  }

  int getPosition(Month month) {
    return calendarConstraints.getStart().monthsUntil(month);
  }
}
