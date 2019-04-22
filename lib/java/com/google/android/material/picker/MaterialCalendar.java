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

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.android.material.picker.selector.GridSelector;
import com.google.android.material.resources.MaterialAttributes;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import java.util.Calendar;
import java.util.LinkedHashSet;

/**
 * Fragment for a days of week {@link Calendar} represented as a header row of days labels and
 * {@link GridView} of days backed by {@link MonthAdapter}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public final class MaterialCalendar<S> extends Fragment {

  private static final String GRID_SELECTOR_KEY = "GRID_SELECTOR_KEY";

  private final LinkedHashSet<OnSelectionChangedListener<S>> onSelectionChangedListeners =
      new LinkedHashSet<>();
  private GridSelector<S> gridSelector;
  private MonthsPagerAdapter monthsPagerAdapter;

  /**
   * Creates a {@link MaterialCalendar} with {@link GridSelector#drawCell(View, Calendar)} applied
   * to each cell.
   *
   * @param gridSelector Controls the highlight state of the {@link MaterialCalendar}
   * @param <T> Type returned from selections in this {@link MaterialCalendar} by {@link
   *     MaterialCalendar#getSelection()}
   */
  public static <T> MaterialCalendar<T> newInstance(GridSelector<T> gridSelector) {
    MaterialCalendar<T> materialCalendar = new MaterialCalendar<>();
    Bundle args = new Bundle();
    args.putParcelable(GRID_SELECTOR_KEY, gridSelector);
    materialCalendar.setArguments(args);
    return materialCalendar;
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle bundle) {
    super.onSaveInstanceState(bundle);
    bundle.putParcelable(GRID_SELECTOR_KEY, gridSelector);
  }

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    if (bundle == null) {
      bundle = getArguments();
    }
    gridSelector = bundle.getParcelable(GRID_SELECTOR_KEY);
  }

  @NonNull
  @Override
  public View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {

    Month earliestMonth = Month.create(1900, Calendar.JANUARY);
    Month latestMonth = Month.create(2100, Calendar.DECEMBER);
    Calendar today = Calendar.getInstance();
    Month startMonth = Month.create(today.get(Calendar.YEAR), today.get(Calendar.MONTH));

    final View root = layoutInflater.inflate(R.layout.mtrl_calendar, viewGroup, false);
    GridView daysHeader = root.findViewById(R.id.calendar_days_header);
    daysHeader.setAdapter(new DaysOfWeekAdapter());
    daysHeader.setNumColumns(startMonth.daysInWeek);

    ViewPager monthsPager = root.findViewById(R.id.month_pager);
    monthsPager.setLayoutParams(
        new LinearLayout.LayoutParams(
            /* width= */ LinearLayout.LayoutParams.MATCH_PARENT,
            /* height= */ MonthAdapter.MAXIMUM_WEEKS * getDayHeight(getContext())));
    monthsPagerAdapter =
        new MonthsPagerAdapter(
            getChildFragmentManager(),
            gridSelector,
            earliestMonth,
            latestMonth,
            startMonth,
            new OnDayClickListener() {

              @Override
              public void onDayClick(Calendar day) {
                gridSelector.select(day);
                monthsPagerAdapter.notifyDataSetChanged();
                for (OnSelectionChangedListener<S> listener : onSelectionChangedListeners) {
                  listener.onSelectionChanged(gridSelector.getSelection());
                }
              }
            });
    monthsPager.setAdapter(monthsPagerAdapter);
    monthsPager.setCurrentItem(monthsPagerAdapter.getStartPosition());

    return root;
  }

  public final S getSelection() {
    return gridSelector.getSelection();
  }

  boolean addOnSelectionChangedListener(OnSelectionChangedListener<S> listener) {
    return onSelectionChangedListeners.add(listener);
  }

  boolean removeOnSelectionChangedListener(OnSelectionChangedListener<S> listener) {
    return onSelectionChangedListeners.remove(listener);
  }

  void clearOnSelectionChangedListeners() {
    onSelectionChangedListeners.clear();
  }

  interface OnSelectionChangedListener<S> {

    void onSelectionChanged(S selection);
  }

  interface OnDayClickListener {

    void onDayClick(Calendar day);
  }

  /** Returns the pixel height of each {@link android.view.View} representing a day. */
  @Px
  static int getDayHeight(Context context) {
    return MaterialAttributes.resolveMinimumAccessibleTouchTarget(context);
  }
}
