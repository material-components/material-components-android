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
import android.graphics.Canvas;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.VisibleForTesting;
import com.google.android.material.button.MaterialButton;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import androidx.recyclerview.widget.RecyclerView.State;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
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
  }

  private static final String THEME_RES_ID_KEY = "THEME_RES_ID_KEY";
  private static final String GRID_SELECTOR_KEY = "GRID_SELECTOR_KEY";
  private static final String CALENDAR_CONSTRAINTS_KEY = "CALENDAR_CONSTRAINTS_KEY";
  private static final String CURRENT_MONTH_KEY = "CURRENT_MONTH_KEY";

  @VisibleForTesting
  @RestrictTo(Scope.LIBRARY_GROUP)
  public static final Object MONTHS_VIEW_GROUP_TAG = "MONTHS_VIEW_GROUP_TAG";

  private int themeResId;
  private DateSelector<S> dateSelector;
  private CalendarConstraints calendarConstraints;
  private Month current;
  private CalendarSelector calendarSelector;
  private CalendarStyle calendarStyle;
  private RecyclerView yearSelector;
  private RecyclerView recyclerView;
  private View yearFrame;
  private View dayFrame;

  static <T> MaterialCalendar<T> newInstance(
      DateSelector<T> dateSelector, int themeResId, CalendarConstraints calendarConstraints) {
    MaterialCalendar<T> materialCalendar = new MaterialCalendar<>();
    Bundle args = new Bundle();
    args.putInt(THEME_RES_ID_KEY, themeResId);
    args.putParcelable(GRID_SELECTOR_KEY, dateSelector);
    args.putParcelable(CALENDAR_CONSTRAINTS_KEY, calendarConstraints);
    args.putParcelable(CURRENT_MONTH_KEY, calendarConstraints.getOpening());
    materialCalendar.setArguments(args);
    return materialCalendar;
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle bundle) {
    super.onSaveInstanceState(bundle);
    bundle.putInt(THEME_RES_ID_KEY, themeResId);
    bundle.putParcelable(GRID_SELECTOR_KEY, dateSelector);
    bundle.putParcelable(CALENDAR_CONSTRAINTS_KEY, calendarConstraints);
    bundle.putParcelable(CURRENT_MONTH_KEY, current);
  }

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    Bundle activeBundle = bundle == null ? getArguments() : bundle;
    themeResId = activeBundle.getInt(THEME_RES_ID_KEY);
    dateSelector = activeBundle.getParcelable(GRID_SELECTOR_KEY);
    calendarConstraints = activeBundle.getParcelable(CALENDAR_CONSTRAINTS_KEY);
    current = activeBundle.getParcelable(CURRENT_MONTH_KEY);
  }

  @NonNull
  @Override
  public View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    ContextThemeWrapper themedContext = new ContextThemeWrapper(getContext(), themeResId);
    calendarStyle = new CalendarStyle(themedContext);
    LayoutInflater themedInflater = layoutInflater.cloneInContext(themedContext);

    Month earliestMonth = calendarConstraints.getStart();

    int layout;
    final int orientation;
    if (MaterialDatePicker.isFullscreen(themedContext)) {
      layout = R.layout.mtrl_calendar_vertical;
      orientation = LinearLayoutManager.VERTICAL;
    } else {
      layout = R.layout.mtrl_calendar_horizontal;
      orientation = LinearLayoutManager.HORIZONTAL;
    }

    View root = themedInflater.inflate(layout, viewGroup, false);
    GridView daysHeader = root.findViewById(R.id.mtrl_calendar_days_of_week);
    daysHeader.setAdapter(new DaysOfWeekAdapter());
    daysHeader.setNumColumns(earliestMonth.daysInWeek);
    daysHeader.setEnabled(false);

    final RecyclerView monthsPager = root.findViewById(R.id.mtrl_calendar_months);

    LinearLayoutManager layoutManager =
        new LinearLayoutManager(getContext(), orientation, false) {
          @Override
          protected void calculateExtraLayoutSpace(@NonNull State state, @NonNull int[] ints) {
            if (orientation == LinearLayoutManager.HORIZONTAL) {
              ints[0] = monthsPager.getWidth();
              ints[1] = monthsPager.getWidth();
            } else {
              ints[0] = monthsPager.getHeight();
              ints[1] = monthsPager.getHeight();
            }
          }
        };
    monthsPager.setLayoutManager(layoutManager);
    monthsPager.setTag(MONTHS_VIEW_GROUP_TAG);

    final MonthsPagerAdapter monthsPagerAdapter =
        new MonthsPagerAdapter(
            themedContext,
            dateSelector,
            calendarConstraints,
            new OnDayClickListener() {

              @Override
              public void onDayClick(long day) {
                if (calendarConstraints.getDateValidator().isValid(day)) {
                  dateSelector.select(day);
                  for (OnSelectionChangedListener<S> listener : onSelectionChangedListeners) {
                    listener.onSelectionChanged(dateSelector.getSelection());
                  }
                  // TODO(b/134663744): Look into monthsPager.getAdapter().notifyItemRangeChanged();
                  monthsPager.getAdapter().notifyDataSetChanged();
                  if (yearSelector != null) {
                    yearSelector.getAdapter().notifyDataSetChanged();
                  }
                }
              }
            });
    monthsPager.setAdapter(monthsPagerAdapter);

    int columns =
        themedContext.getResources().getInteger(R.integer.mtrl_calendar_year_selector_span);
    yearSelector = root.findViewById(R.id.mtrl_calendar_year_selector_frame);
    if (yearSelector != null) {
      yearSelector.setHasFixedSize(true);
      yearSelector.setLayoutManager(
          new GridLayoutManager(themedContext, columns, RecyclerView.VERTICAL, false));
      yearSelector.setAdapter(new YearGridAdapter(this));
      yearSelector.addItemDecoration(createItemDecoration());
    }

    if (root.findViewById(R.id.month_navigation_fragment_toggle) != null) {
      addActionsToMonthNavigation(root, monthsPagerAdapter);
    }

    if (!MaterialDatePicker.isFullscreen(themedContext)) {
      new LinearSnapHelper().attachToRecyclerView(monthsPager);
    }
    monthsPager.scrollToPosition(monthsPagerAdapter.getPosition(current));
    return root;
  }

  private ItemDecoration createItemDecoration() {
    return new ItemDecoration() {

      private final Calendar startItem = Calendar.getInstance();
      private final Calendar endItem = Calendar.getInstance();

      @Override
      public void onDraw(
          @NonNull Canvas canvas, @NonNull RecyclerView recyclerView, @NonNull State state) {
        if (!(recyclerView.getAdapter() instanceof YearGridAdapter)
            || !(recyclerView.getLayoutManager() instanceof GridLayoutManager)) {
          return;
        }
        YearGridAdapter adapter = (YearGridAdapter) recyclerView.getAdapter();
        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();

        for (Pair<Long, Long> range : dateSelector.getSelectedRanges()) {
          if (range.first == null || range.second == null) {
            continue;
          }
          startItem.setTimeInMillis(range.first);
          endItem.setTimeInMillis(range.second);

          int firstHighlightPosition = adapter.getPositionForYear(startItem.get(Calendar.YEAR));
          int lastHighlightPosition = adapter.getPositionForYear(endItem.get(Calendar.YEAR));
          View firstView = layoutManager.findViewByPosition(firstHighlightPosition);
          View lastView = layoutManager.findViewByPosition(lastHighlightPosition);

          int firstRow = firstHighlightPosition / layoutManager.getSpanCount();
          int lastRow = lastHighlightPosition / layoutManager.getSpanCount();

          for (int row = firstRow; row <= lastRow; row++) {
            int firstPositionInRow = row * layoutManager.getSpanCount();
            View viewInRow = layoutManager.findViewByPosition(firstPositionInRow);
            if (viewInRow == null) {
              continue;
            }
            int top = viewInRow.getTop() + calendarStyle.year.getTopInset();
            int bottom = viewInRow.getBottom() - calendarStyle.year.getBottomInset();
            int left = row == firstRow ? firstView.getLeft() + firstView.getWidth() / 2 : 0;
            int right =
                row == lastRow
                    ? lastView.getLeft() + lastView.getWidth() / 2
                    : recyclerView.getWidth();
            canvas.drawRect(left, top, right, bottom, calendarStyle.rangeFill);
          }
        }
      }
    };
  }

  Month getCurrentMonth() {
    return current;
  }

  /** Returns the {@link CalendarConstraints} in use by this {@link MaterialCalendar}. */
  CalendarConstraints getCalendarConstraints() {
    return calendarConstraints;
  }

  /**
   * Changes the currently displayed {@link Month} to {@code moveTo}.
   *
   * @throws IllegalArgumentException If {@code moveTo} is not within the allowed {@link
   *     CalendarConstraints}.
   */
  void setCurrentMonth(Month moveTo) {
    setCurrentMonth(moveTo, /* smooth= */ true);
  }

  void setCurrentMonth(Month moveTo, boolean smooth) {
    current = moveTo;
    int moveToPosition = ((MonthsPagerAdapter) recyclerView.getAdapter()).getPosition(current);
    if (smooth) {
      recyclerView.smoothScrollToPosition(moveToPosition);
    } else {
      recyclerView.scrollToPosition(moveToPosition);
    }
  }

  @Override
  public DateSelector<S> getDateSelector() {
    return dateSelector;
  }

  CalendarStyle getCalendarStyle() {
    return calendarStyle;
  }

  interface OnDayClickListener {

    void onDayClick(long day);
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
                  .getPositionForYear(calendarConstraints.getOpening().year));
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
    recyclerView = root.findViewById(R.id.mtrl_calendar_months);
    final MaterialButton monthDropSelect = root.findViewById(R.id.month_navigation_fragment_toggle);
    final MaterialButton monthPrev = root.findViewById(R.id.month_navigation_previous);
    final MaterialButton monthNext = root.findViewById(R.id.month_navigation_next);

    yearFrame = root.findViewById(R.id.mtrl_calendar_year_selector_frame);
    dayFrame = root.findViewById(R.id.mtrl_calendar_day_selector_frame);
    setSelector(CalendarSelector.DAY);

    monthDropSelect.setText(current.getLongName());
    recyclerView.addOnScrollListener(
        new OnScrollListener() {
          @Override
          public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
              int currentItem =
                  ((LinearLayoutManager) MaterialCalendar.this.recyclerView.getLayoutManager())
                      .findFirstVisibleItemPosition();
              monthDropSelect.setText(monthsPagerAdapter.getPageTitle(currentItem));
            }
          }
        });

    monthDropSelect.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View view) {
            toggleVisibleSelector();
          }
        });

    monthNext.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View view) {
            int currentItem =
                ((LinearLayoutManager) recyclerView.getLayoutManager())
                    .findFirstVisibleItemPosition();
            if (currentItem + 1 < recyclerView.getAdapter().getItemCount()) {
              setCurrentMonth(monthsPagerAdapter.getPageMonth(currentItem + 1));
            }
          }
        });
    monthPrev.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View view) {
            int currentItem =
                ((LinearLayoutManager) recyclerView.getLayoutManager())
                    .findLastVisibleItemPosition();
            if (currentItem - 1 >= 0) {
              setCurrentMonth(monthsPagerAdapter.getPageMonth(currentItem - 1));
            }
          }
        });
  }
}
