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
import androidx.annotation.VisibleForTesting;
import com.google.android.material.button.MaterialButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;
import java.util.Calendar;

/**
 * Fragment for a days of week {@link Calendar} represented as a header row of days labels and
 * {@link GridView} of days backed by {@link MonthAdapter}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public final class MaterialCalendar<S> extends PickerFragment<S> {

  /** The views supported by {@link MaterialCalendar}. */
  enum CalendarSelector {
    DAY,
    YEAR
  };

  private static final String THEME_RES_ID_KEY = "THEME_RES_ID_KEY";
  private static final String GRID_SELECTOR_KEY = "GRID_SELECTOR_KEY";
  private static final String CALENDAR_BOUNDS_KEY = "CALENDAR_BOUNDS_KEY";

  @VisibleForTesting
  @RestrictTo(Scope.LIBRARY_GROUP)
  public static final Object VIEW_PAGER_TAG = "VIEW_PAGER_TAG";

  private int themeResId;
  private GridSelector<S> gridSelector;
  private CalendarBounds calendarBounds;
  private CalendarSelector calendarSelector;
  private RecyclerView yearSelector;
  private ViewPager2 monthPager;
  private View yearFrame;
  private View dayFrame;

  /**
   * Creates a {@link MaterialCalendar} with {@link GridSelector#drawItem(TextView, Calendar)}
   * applied to each cell.
   *
   * @param gridSelector Controls the highlight state of the {@link MaterialCalendar}
   * @param <T> Type of {@link GridSelector} returned from selections in this {@link
   *     MaterialCalendar} by {@link MaterialCalendar#getGridSelector()}
   */
  public static <T> MaterialCalendar<T> newInstance(
      GridSelector<T> gridSelector, int themeResId, CalendarBounds calendarBounds) {
    MaterialCalendar<T> materialCalendar = new MaterialCalendar<>();
    Bundle args = new Bundle();
    args.putInt(THEME_RES_ID_KEY, themeResId);
    args.putParcelable(GRID_SELECTOR_KEY, gridSelector);
    args.putParcelable(CALENDAR_BOUNDS_KEY, calendarBounds);
    materialCalendar.setArguments(args);
    return materialCalendar;
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle bundle) {
    super.onSaveInstanceState(bundle);
    bundle.putInt(THEME_RES_ID_KEY, themeResId);
    bundle.putParcelable(GRID_SELECTOR_KEY, gridSelector);
    bundle.putParcelable(CALENDAR_BOUNDS_KEY, calendarBounds);
  }

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    Bundle activeBundle = bundle == null ? getArguments() : bundle;
    themeResId = activeBundle.getInt(THEME_RES_ID_KEY);
    gridSelector = activeBundle.getParcelable(GRID_SELECTOR_KEY);
    calendarBounds = activeBundle.getParcelable(CALENDAR_BOUNDS_KEY);
  }

  @NonNull
  @Override
  public View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    ContextThemeWrapper themedContext = new ContextThemeWrapper(getContext(), themeResId);
    LayoutInflater themedInflater = layoutInflater.cloneInContext(themedContext);

    Month earliestMonth = calendarBounds.getStart();

    int layout;
    int orientation;
    if (MaterialPickerDialogFragment.isFullscreen(themedContext)) {
      layout = R.layout.mtrl_calendar_vertical;
      orientation = ViewPager2.ORIENTATION_VERTICAL;
    } else {
      layout = R.layout.mtrl_calendar_horizontal;
      orientation = ViewPager2.ORIENTATION_HORIZONTAL;
    }

    View root = themedInflater.inflate(layout, viewGroup, false);
    GridView daysHeader = root.findViewById(R.id.mtrl_calendar_days_of_week);
    daysHeader.setAdapter(new DaysOfWeekAdapter());
    daysHeader.setNumColumns(earliestMonth.daysInWeek);

    ViewPager2 monthsPager = root.findViewById(R.id.mtrl_calendar_viewpager);
    monthsPager.setOrientation(orientation);
    monthsPager.setTag(VIEW_PAGER_TAG);

    final MonthsPagerAdapter monthsPagerAdapter =
        new MonthsPagerAdapter(
            themedContext,
            getChildFragmentManager(),
            getLifecycle(),
            gridSelector,
            calendarBounds,
            day -> {
              gridSelector.select(day);
              monthsPager.getAdapter().notifyDataSetChanged();
              if (yearSelector != null) {
                yearSelector.getAdapter().notifyDataSetChanged();
              }
            });
    monthsPager.setAdapter(monthsPagerAdapter);
    monthsPager.setCurrentItem(monthsPagerAdapter.getStartPosition(), false);

    int columns =
        themedContext.getResources().getInteger(R.integer.mtrl_calendar_year_selector_span);
    yearSelector = root.findViewById(R.id.mtrl_calendar_year_selector_frame);
    if (yearSelector != null) {
      yearSelector.setHasFixedSize(true);
      yearSelector.setLayoutManager(
          new GridLayoutManager(themedContext, columns, RecyclerView.VERTICAL, false));
      yearSelector.setAdapter(new YearGridAdapter(this));
      yearSelector.addItemDecoration(gridSelector.createYearDecorator());
    }

    if (root.findViewById(R.id.month_navigation_fragment_toggle) != null) {
      addActionsToMonthNavigation(root, monthsPagerAdapter);
    }

    return root;
  }

  /** Returns the {@link CalendarBounds} in use by this {@link MaterialCalendar}. */
  CalendarBounds getCalendarBounds() {
    return calendarBounds;
  }

  /**
   * Changes the currently displayed {@link Month} to {@code moveTo}.
   *
   * @throws IllegalArgumentException If {@code moveTo} is not within the allowed {@link
   *     CalendarBounds}.
   */
  void setCurrentMonth(Month moveTo) {
    calendarBounds =
        CalendarBounds.create(calendarBounds.getStart(), calendarBounds.getEnd(), moveTo);
    int moveToPosition =
        ((MonthsPagerAdapter) monthPager.getAdapter()).getPosition(calendarBounds.getCurrent());
    monthPager.setCurrentItem(moveToPosition);
  }

  @Override
  public GridSelector<S> getGridSelector() {
    return gridSelector;
  }

  interface OnDayClickListener {

    void onDayClick(Calendar day);
  }

  /** Returns the pixel height of each {@link android.view.View} representing a day. */
  @Px
  static int getDayHeight(Context context) {
    return context.getResources().getDimensionPixelSize(R.dimen.mtrl_calendar_day_height);
  }

  void setSelector(CalendarSelector selector) {
    this.calendarSelector = selector;
    if (selector == CalendarSelector.YEAR) {
      yearSelector
          .getLayoutManager()
          .scrollToPosition(
              ((YearGridAdapter) yearSelector.getAdapter())
                  .getPositionForYear(calendarBounds.getCurrent().year));
      yearFrame.setVisibility(View.VISIBLE);
      dayFrame.setVisibility(View.GONE);
    } else if (selector == CalendarSelector.DAY) {
      yearFrame.setVisibility(View.GONE);
      dayFrame.setVisibility(View.VISIBLE);
    }
  }

  void toggleVisibleSelector() {
    if (calendarSelector == CalendarSelector.YEAR) {
      setSelector(CalendarSelector.DAY);
    } else if (calendarSelector == CalendarSelector.DAY) {
      setSelector(CalendarSelector.YEAR);
    }
  }

  private void addActionsToMonthNavigation(
      final View root, final MonthsPagerAdapter monthsPagerAdapter) {
    monthPager = root.findViewById(R.id.mtrl_calendar_viewpager);
    final MaterialButton monthDropSelect = root.findViewById(R.id.month_navigation_fragment_toggle);
    monthDropSelect.setText(monthsPagerAdapter.getPageTitle(monthPager.getCurrentItem()));
    final MaterialButton monthPrev = root.findViewById(R.id.month_navigation_previous);
    final MaterialButton monthNext = root.findViewById(R.id.month_navigation_next);

    yearFrame = root.findViewById(R.id.mtrl_calendar_year_selector_frame);
    dayFrame = root.findViewById(R.id.mtrl_calendar_day_selector_frame);
    setSelector(CalendarSelector.DAY);

    monthPager.registerOnPageChangeCallback(
        new OnPageChangeCallback() {
          @Override
          public void onPageSelected(int position) {
            calendarBounds =
                CalendarBounds.create(
                    calendarBounds.getStart(),
                    calendarBounds.getEnd(),
                    monthsPagerAdapter.getPageMonth(position));
            monthDropSelect.setText(monthsPagerAdapter.getPageTitle(position));
          }
        });

    monthDropSelect.setOnClickListener(
        view -> {
          toggleVisibleSelector();
        });

    monthNext.setOnClickListener(
        view -> {
          if (monthPager.getCurrentItem() + 1 < monthPager.getAdapter().getItemCount()) {
            setCurrentMonth(monthsPagerAdapter.getPageMonth(monthPager.getCurrentItem() + 1));
          }
        });
    monthPrev.setOnClickListener(
        view -> {
          if (monthPager.getCurrentItem() - 1 >= 0) {
            setCurrentMonth(monthsPagerAdapter.getPageMonth(monthPager.getCurrentItem() - 1));
          }
        });
  }
}
