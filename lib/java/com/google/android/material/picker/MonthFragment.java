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

/**
 * A {@link Fragment} representing the days of a single {@link Month} highlighted by a {@link
 * GridSelector}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class MonthFragment extends Fragment {

  private static final String MONTH_KEY = "MONTH_KEY";
  private static final String GRID_SELECTOR_KEY = "GRID_SELECTOR_KEY";

  private Month month;
  private MonthAdapter monthAdapter;
  private GridSelector<?> gridSelector;
  private OnFragmentClickedListener onFragmentClickedListener;

  public void setOnFragmentClickedListener(OnFragmentClickedListener onFragmentClickedListener) {
    this.onFragmentClickedListener = onFragmentClickedListener;
  }

  /**
   * Constructs a new {@link MonthFragment}.
   *
   * @param month The {@link Month} this {@link MonthFragment} displays data for
   * @param gridSelector The {@link GridSelector} used to highlight and mark the {@link GridView}
   */
  public static MonthFragment newInstance(Month month, GridSelector<?> gridSelector) {
    MonthFragment monthFragment = new MonthFragment();
    Bundle arguments = new Bundle();
    arguments.putParcelable(MONTH_KEY, month);
    arguments.putParcelable(GRID_SELECTOR_KEY, gridSelector);
    monthFragment.setArguments(arguments);
    return monthFragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    month = getArguments().getParcelable(MONTH_KEY);
    gridSelector = getArguments().getParcelable(GRID_SELECTOR_KEY);
    monthAdapter = new MonthAdapter(getContext(), month, gridSelector);
  }

  @Override
  public GridView onCreateView(
      LayoutInflater layoutInflater, ViewGroup root, Bundle savedInstanceState) {
    View view = layoutInflater.inflate(R.layout.mtrl_month_grid, root, false);
    GridView gridview = view.findViewById(R.id.month_grid);
    gridview.setNumColumns(month.daysInWeek);
    gridview.setAdapter(monthAdapter);
    gridview.setOnItemClickListener(createOnItemClickListener());
    return gridview;
  }

  void onPagerSelectionChanged() {
    monthAdapter.notifyDataSetChanged();
  }

  private OnItemClickListener createOnItemClickListener() {
    return new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        gridSelector.changeSelection(monthAdapter, view, position, id);
        if (onFragmentClickedListener != null) {
          onFragmentClickedListener.onFragmentClicked();
        }
      }
    };
  }
}
