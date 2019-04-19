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

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.android.material.picker.selector.GridSelector;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
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
  private Month month;
  private MonthAdapter monthAdapter;
  private GridSelector<S> gridSelector;

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
    Calendar calendar = Calendar.getInstance();
    month = Month.create(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    monthAdapter = new MonthAdapter(getContext(), month, gridSelector);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    final View root = layoutInflater.inflate(R.layout.mtrl_calendar, viewGroup, false);
    GridView daysHeader = root.findViewById(R.id.calendar_days_header);
    GridView daysGrid = root.findViewById(R.id.calendar_grid);

    daysHeader.setAdapter(new DaysOfWeekAdapter());
    daysHeader.setNumColumns(month.daysInWeek);
    daysGrid.setAdapter(monthAdapter);
    daysGrid.setNumColumns(month.daysInWeek);
    daysGrid.setOnItemClickListener(
        new OnItemClickListener() {

          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // The onItemClick interface forces use of a wildcard AdapterView, but
            // GridSelector#changeSelection needs a MonthAdapter.
            // The cast is verified by the instanceof on the Adapter backing the AdapterView.
            if (parent.getAdapter() instanceof MonthAdapter) {
              @SuppressWarnings("unchecked")
              AdapterView<MonthAdapter> calendarGrid =
                  (AdapterView<MonthAdapter>) parent;
              gridSelector.changeSelection(calendarGrid, view, position, id);
              gridSelector.drawSelection(calendarGrid);
              for (OnSelectionChangedListener<S> onSelectionChangedListener : 
                  onSelectionChangedListeners) {
                onSelectionChangedListener.onSelectionChanged(gridSelector.getSelection());
              }
            }
          }
        });
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
}
