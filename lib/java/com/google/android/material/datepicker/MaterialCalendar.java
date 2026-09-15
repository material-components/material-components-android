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
package com.google.android.material.datepicker;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import androidx.recyclerview.widget.RecyclerView.State;
import androidx.appcompat.widget.TooltipCompat;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.GridView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.core.util.Pair;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import com.google.android.material.button.MaterialButton;
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
  private static final String DAY_VIEW_DECORATOR_KEY = "DAY_VIEW_DECORATOR_KEY";
  private static final String CURRENT_MONTH_KEY = "CURRENT_MONTH_KEY";
  private static final int SMOOTH_SCROLL_MAX = 3;

  @VisibleForTesting static final Object MONTHS_VIEW_GROUP_TAG = "MONTHS_VIEW_GROUP_TAG";

  @VisibleForTesting static final Object NAVIGATION_PREV_TAG = "NAVIGATION_PREV_TAG";

  @VisibleForTesting static final Object NAVIGATION_NEXT_TAG = "NAVIGATION_NEXT_TAG";

  @VisibleForTesting static final Object SELECTOR_TOGGLE_TAG = "SELECTOR_TOGGLE_TAG";

  @StyleRes private int themeResId;
  @Nullable private DateSelector<S> dateSelector;
  @Nullable private CalendarConstraints calendarConstraints;
  @Nullable private DayViewDecorator dayViewDecorator;
  @Nullable private Month current;
  private CalendarSelector calendarSelector;
  private CalendarStyle calendarStyle;
  private RecyclerView yearSelector;
  private RecyclerView recyclerView;
  private View monthPrev;
  private View monthNext;
  private View yearFrame;
  private View dayFrame;
  private MaterialButton monthDropSelect;
  private AccessibilityManager accessibilityManager;
  @Nullable private PagerSnapHelper pagerSnapHelper;
  private boolean isFullscreen;

  @NonNull
  public static <T> MaterialCalendar<T> newInstance(
      @NonNull DateSelector<T> dateSelector,
      @StyleRes int themeResId,
      @NonNull CalendarConstraints calendarConstraints) {
    return newInstance(dateSelector, themeResId, calendarConstraints, null);
  }

  @NonNull
  public static <T> MaterialCalendar<T> newInstance(
      @NonNull DateSelector<T> dateSelector,
      @StyleRes int themeResId,
      @NonNull CalendarConstraints calendarConstraints,
      @Nullable DayViewDecorator dayViewDecorator) {
    MaterialCalendar<T> materialCalendar = new MaterialCalendar<>();
    Bundle args = new Bundle();
    args.putInt(THEME_RES_ID_KEY, themeResId);
    args.putParcelable(GRID_SELECTOR_KEY, dateSelector);
    args.putParcelable(CALENDAR_CONSTRAINTS_KEY, calendarConstraints);
    args.putParcelable(DAY_VIEW_DECORATOR_KEY, dayViewDecorator);
    args.putParcelable(CURRENT_MONTH_KEY, calendarConstraints.getOpenAt());
    materialCalendar.setArguments(args);
    return materialCalendar;
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle bundle) {
    super.onSaveInstanceState(bundle);
    bundle.putInt(THEME_RES_ID_KEY, themeResId);
    bundle.putParcelable(GRID_SELECTOR_KEY, dateSelector);
    bundle.putParcelable(CALENDAR_CONSTRAINTS_KEY, calendarConstraints);
    bundle.putParcelable(DAY_VIEW_DECORATOR_KEY, dayViewDecorator);
    bundle.putParcelable(CURRENT_MONTH_KEY, current);
  }

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    Bundle activeBundle = bundle == null ? getArguments() : bundle;
    themeResId = activeBundle.getInt(THEME_RES_ID_KEY);
    dateSelector = activeBundle.getParcelable(GRID_SELECTOR_KEY);
    calendarConstraints = activeBundle.getParcelable(CALENDAR_CONSTRAINTS_KEY);
    dayViewDecorator = activeBundle.getParcelable(DAY_VIEW_DECORATOR_KEY);
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

    accessibilityManager =
        (AccessibilityManager) requireContext().getSystemService(Context.ACCESSIBILITY_SERVICE);

    Month earliestMonth = calendarConstraints.getStart();

    int layout;
    final int orientation;
    isFullscreen = MaterialDatePicker.isFullscreen(themedContext);
    if (isFullscreen) {
      layout = R.layout.mtrl_calendar_vertical;
      orientation = LinearLayoutManager.VERTICAL;
    } else {
      layout = R.layout.mtrl_calendar_horizontal;
      orientation = LinearLayoutManager.HORIZONTAL;
    }

    View root = themedInflater.inflate(layout, viewGroup, false);
    root.setMinimumHeight(getDialogPickerHeight(requireContext()));
    GridView daysHeader = root.findViewById(R.id.mtrl_calendar_days_of_week);
    ViewCompat.setAccessibilityDelegate(
        daysHeader,
        new AccessibilityDelegateCompat() {
          @Override
          public void onInitializeAccessibilityNodeInfo(
              View view, @NonNull AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfoCompat);
            // Remove announcing row/col info.
            accessibilityNodeInfoCompat.setCollectionInfo(null);
          }
        });
    int firstDayOfWeek = calendarConstraints.getFirstDayOfWeek();
    daysHeader.setAdapter(
        firstDayOfWeek > 0 ? new DaysOfWeekAdapter(firstDayOfWeek) : new DaysOfWeekAdapter());
    daysHeader.setNumColumns(earliestMonth.daysInWeek);
    daysHeader.setEnabled(false);

    recyclerView = root.findViewById(R.id.mtrl_calendar_months);

    SmoothCalendarLayoutManager layoutManager =
        new SmoothCalendarLayoutManager(getContext(), orientation, false) {
          @Override
          protected void calculateExtraLayoutSpace(@NonNull State state, @NonNull int[] ints) {
            if (orientation == LinearLayoutManager.HORIZONTAL) {
              ints[0] = recyclerView.getWidth();
              ints[1] = recyclerView.getWidth();
            } else {
              ints[0] = recyclerView.getHeight();
              ints[1] = recyclerView.getHeight();
            }
          }
        };
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setTag(MONTHS_VIEW_GROUP_TAG);

    final MonthsPagerAdapter monthsPagerAdapter =
        new MonthsPagerAdapter(
            themedContext,
            dateSelector,
            calendarConstraints,
            dayViewDecorator,
            new OnDayClickListener() {

              @Override
              public void onDayClick(long day) {
                if (calendarConstraints.getDateValidator().isValid(day)) {
                  dateSelector.select(day);
                  for (OnSelectionChangedListener<S> listener : onSelectionChangedListeners) {
                    listener.onSelectionChanged(dateSelector.getSelection());
                  }
                  // TODO(b/134663744): Look into monthsPager.getAdapter().notifyItemRangeChanged();
                  recyclerView.getAdapter().notifyDataSetChanged();
                  if (yearSelector != null) {
                    yearSelector.getAdapter().notifyDataSetChanged();
                  }
                }
              }
            },
            new OnMonthNavigationListener() {
              @Override
              public boolean onMonthNavigationPrevious() {
                return handleNavigateToMonthForKeyboard(/* forward= */ false);
              }

              @Override
              public boolean onMonthNavigationNext() {
                return handleNavigateToMonthForKeyboard(/* forward= */ true);
              }
            });
    recyclerView.setAdapter(monthsPagerAdapter);

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

    if (!isFullscreen) {
      pagerSnapHelper = new PagerSnapHelper();
      pagerSnapHelper.attachToRecyclerView(recyclerView);
    }
    if (root.findViewById(R.id.month_navigation_fragment_toggle) != null) {
      addActionsToMonthNavigation(root, monthsPagerAdapter);
    }
    recyclerView.scrollToPosition(monthsPagerAdapter.getPosition(current));
    setUpForAccessibility();
    updateAccessibilityPaneTitle(root);
    return root;
  }

  private void setUpForAccessibility() {
    ViewCompat.setAccessibilityDelegate(
        recyclerView,
        new AccessibilityDelegateCompat() {
          @Override
          public void onInitializeAccessibilityNodeInfo(
              View view, @NonNull AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfoCompat);
            accessibilityNodeInfoCompat.setScrollable(false);
          }
        });
  }

  @NonNull
  private ItemDecoration createItemDecoration() {
    return new ItemDecoration() {

      private final Calendar startItem = UtcDates.getUtcCalendar();
      private final Calendar endItem = UtcDates.getUtcCalendar();

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
            int left =
                row == firstRow && firstView != null
                    ? firstView.getLeft() + firstView.getWidth() / 2
                    : 0;
            int right =
                row == lastRow && lastView != null
                    ? lastView.getLeft() + lastView.getWidth() / 2
                    : recyclerView.getWidth();
            canvas.drawRect(left, top, right, bottom, calendarStyle.rangeFill);
          }
        }
      }
    };
  }

  @Nullable
  Month getCurrentMonth() {
    return current;
  }

  /** Returns the {@link CalendarConstraints} in use by this {@link MaterialCalendar}. */
  @Nullable
  CalendarConstraints getCalendarConstraints() {
    return calendarConstraints;
  }

  /**
   * Handles navigating to the adjacent month in response to keyboard navigation.
   *
   * <p>This method pages horizontally to switch months in non-fullscreen mode. In fullscreen mode,
   * this method returns {@code false} because months are scrolled vertically.
   *
   * @param forward {@code true} to navigate to the next month, {@code false} to navigate to the
   *     previous month.
   * @return {@code true} if the event was handled.
   */
  private boolean handleNavigateToMonthForKeyboard(boolean forward) {
    if (isFullscreen) {
      return false;
    }

    // Do not navigate if scroll is in progress. Return true to indicate the event was handled,
    // but in practice navigation is ignored during scroll.
    if (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
      return true;
    }

    MonthsPagerAdapter adapter = (MonthsPagerAdapter) recyclerView.getAdapter();
    if (adapter == null || current == null) {
      return false;
    }

    int currentItem = adapter.getPosition(current);
    int newItem = currentItem + (forward ? 1 : -1);

    if (newItem >= 0 && newItem < adapter.getItemCount()) {
      adapter.setKeyboardFocusDirection(forward ? View.FOCUS_FORWARD : View.FOCUS_BACKWARD);
      setCurrentMonth(adapter.getPageMonth(newItem));
      return true;
    }
    return false;
  }

  /**
   * Changes the currently displayed month to {@code moveTo}.
   *
   * @throws IllegalArgumentException If {@code moveTo} is not within the allowed {@link
   *     CalendarConstraints}.
   */
  void setCurrentMonth(Month moveTo) {
    MonthsPagerAdapter adapter = (MonthsPagerAdapter) recyclerView.getAdapter();
    int moveToPosition = adapter.getPosition(moveTo);
    if (accessibilityManager != null && accessibilityManager.isEnabled()) {
      current = moveTo;
      recyclerView.scrollToPosition(moveToPosition);
    } else {
      int distance = moveToPosition - adapter.getPosition(current);
      boolean jump = Math.abs(distance) > SMOOTH_SCROLL_MAX;
      boolean isForward = distance > 0;
      current = moveTo;
      if (jump && isForward) {
        recyclerView.scrollToPosition(moveToPosition - SMOOTH_SCROLL_MAX);
        postSmoothRecyclerViewScroll(moveToPosition);
      } else if (jump) {
        recyclerView.scrollToPosition(moveToPosition + SMOOTH_SCROLL_MAX);
        postSmoothRecyclerViewScroll(moveToPosition);
      } else {
        postSmoothRecyclerViewScroll(moveToPosition);
      }
    }
    updateCurrentVisibleMonth();
    updateNavigationButtonsEnabled(moveToPosition);
  }

  private void updateCurrentVisibleMonth() {
    MonthsPagerAdapter adapter = (MonthsPagerAdapter) recyclerView.getAdapter();
    if (adapter != null && !isFullscreen) {
      adapter.setVisibleMonth(current);
    }
  }

  @Nullable
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

  /**
   * Listener for month navigation events.
   *
   * <p>This listener is used by {@link MaterialCalendarGridView} to signal when keyboard navigation
   * reaches the start or end of a month, allowing the calendar to scroll to the previous or next
   * month.
   */
  interface OnMonthNavigationListener {

    boolean onMonthNavigationPrevious();

    boolean onMonthNavigationNext();
  }

  /** Returns the pixel height of each {@link android.view.View} representing a day. */
  @Px
  static int getDayHeight(@NonNull Context context) {
    return context.getResources().getDimensionPixelSize(R.dimen.mtrl_calendar_day_height);
  }

  void setSelector(CalendarSelector selector) {
    this.calendarSelector = selector;
    if (selector == CalendarSelector.YEAR) {
      yearSelector
          .getLayoutManager()
          .scrollToPosition(
              ((YearGridAdapter) yearSelector.getAdapter()).getPositionForYear(current.year));
      yearFrame.setVisibility(View.VISIBLE);
      dayFrame.setVisibility(View.GONE);
      monthPrev.setVisibility(View.GONE);
      monthNext.setVisibility(View.GONE);
    } else if (selector == CalendarSelector.DAY) {
      yearFrame.setVisibility(View.GONE);
      dayFrame.setVisibility(View.VISIBLE);
      monthPrev.setVisibility(View.VISIBLE);
      monthNext.setVisibility(View.VISIBLE);
      // When visibility is toggled, the RecyclerView default opens to its lowest available id.
      // This id is always one month earlier than current, so we force it to current.
      setCurrentMonth(current);
    }
  }

  void sendAccessibilityFocusEventToMonthDropdown() {
    if (monthDropSelect != null) {
      monthDropSelect.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
    }
  }

  void toggleVisibleSelector() {
    if (calendarSelector == CalendarSelector.YEAR) {
      setSelector(CalendarSelector.DAY);
    } else if (calendarSelector == CalendarSelector.DAY) {
      setSelector(CalendarSelector.YEAR);
    }
    updateAccessibilityPaneTitle(getView());
  }

  private void updateAccessibilityPaneTitle(@Nullable View view) {
    if (view == null) {
      return;
    }

    if (calendarSelector == CalendarSelector.YEAR) {
      ViewCompat.setAccessibilityPaneTitle(
          view, getString(R.string.mtrl_picker_pane_title_year_view));
    } else if (calendarSelector == CalendarSelector.DAY) {
      ViewCompat.setAccessibilityPaneTitle(
          view, getString(R.string.mtrl_picker_pane_title_calendar_view));
    }
  }

  private void addActionsToMonthNavigation(
      @NonNull final View root, @NonNull final MonthsPagerAdapter monthsPagerAdapter) {
    monthDropSelect = root.findViewById(R.id.month_navigation_fragment_toggle);
    monthDropSelect.setTag(SELECTOR_TOGGLE_TAG);
    ViewCompat.setAccessibilityDelegate(
        monthDropSelect,
        new AccessibilityDelegateCompat() {

          @Override
          public void onInitializeAccessibilityNodeInfo(
              View view, @NonNull AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfoCompat);
            CharSequence description =
                dayFrame.getVisibility() == View.VISIBLE
                    ? getString(R.string.mtrl_picker_toggle_to_year_selection)
                    : getString(R.string.mtrl_picker_toggle_to_day_selection);
            AccessibilityActionCompat customClickDescription =
                new AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_CLICK, description);
            accessibilityNodeInfoCompat.addAction(customClickDescription);
          }
        });

    monthPrev = root.findViewById(R.id.month_navigation_previous);
    monthPrev.setTag(NAVIGATION_PREV_TAG);
    TooltipCompat.setTooltipText(monthPrev, getString(R.string.mtrl_picker_prev_month_tooltip));
    monthNext = root.findViewById(R.id.month_navigation_next);
    monthNext.setTag(NAVIGATION_NEXT_TAG);
    TooltipCompat.setTooltipText(monthNext, getString(R.string.mtrl_picker_next_month_tooltip));

    yearFrame = root.findViewById(R.id.mtrl_calendar_year_selector_frame);
    dayFrame = root.findViewById(R.id.mtrl_calendar_day_selector_frame);
    setSelector(CalendarSelector.DAY);
    monthDropSelect.setText(current.getLongName());
    recyclerView.addOnScrollListener(
        new OnScrollListener() {
          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            int position =
                dx < 0
                    ? getLayoutManager().findFirstVisibleItemPosition()
                    : getLayoutManager().findLastVisibleItemPosition();
            if (pagerSnapHelper == null) {
              current = monthsPagerAdapter.getPageMonth(position);
            }
            monthDropSelect.setText(monthsPagerAdapter.getPageTitle(position));
            updateNavigationButtonsEnabled(position);
          }

          @Override
          public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (newState != RecyclerView.SCROLL_STATE_IDLE || pagerSnapHelper == null) {
              return;
            }

            // If horizontal mode, find the snapped view and set it as the current month.
            View snapView = pagerSnapHelper.findSnapView(getLayoutManager());
            if (snapView != null) {
              int snapPosition = recyclerView.getChildAdapterPosition(snapView);
              if (snapPosition != RecyclerView.NO_POSITION) {
                current = monthsPagerAdapter.getPageMonth(snapPosition);
                monthDropSelect.setText(monthsPagerAdapter.getPageTitle(snapPosition));
                updateNavigationButtonsEnabled(snapPosition);
              }
            }
            updateCurrentVisibleMonth();
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
            int currentItem = getLayoutManager().findFirstVisibleItemPosition();
            monthsPagerAdapter.setKeyboardFocusDirection(View.FOCUS_FORWARD);
            setCurrentMonth(monthsPagerAdapter.getPageMonth(currentItem + 1));
          }
        });
    monthPrev.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View view) {
            int currentItem = getLayoutManager().findLastVisibleItemPosition();
            monthsPagerAdapter.setKeyboardFocusDirection(View.FOCUS_BACKWARD);
            setCurrentMonth(monthsPagerAdapter.getPageMonth(currentItem - 1));
          }
        });

    int currentMonthPosition = monthsPagerAdapter.getPosition(current);
    updateNavigationButtonsEnabled(currentMonthPosition);
  }

  private void updateNavigationButtonsEnabled(int currentMonthPosition) {
    if (monthNext != null) {
      monthNext.setEnabled(currentMonthPosition + 1 < recyclerView.getAdapter().getItemCount());
    }
    if (monthPrev != null) {
      monthPrev.setEnabled(currentMonthPosition - 1 >= 0);
    }
  }

  private void postSmoothRecyclerViewScroll(final int position) {
    recyclerView.post(
        new Runnable() {
          @Override
          public void run() {
            recyclerView.smoothScrollToPosition(position);
          }
        });
  }

  private static int getDialogPickerHeight(@NonNull Context context) {
    Resources resources = context.getResources();
    int navigationHeight =
        resources.getDimensionPixelSize(R.dimen.mtrl_calendar_navigation_height)
            + resources.getDimensionPixelOffset(R.dimen.mtrl_calendar_navigation_top_padding)
            + resources.getDimensionPixelOffset(R.dimen.mtrl_calendar_navigation_bottom_padding);
    int daysOfWeekHeight =
        resources.getDimensionPixelSize(R.dimen.mtrl_calendar_days_of_week_height);
    int calendarHeight =
        MonthAdapter.MAXIMUM_WEEKS
            * resources.getDimensionPixelSize(R.dimen.mtrl_calendar_day_height)
            + (MonthAdapter.MAXIMUM_WEEKS - 1)
            * resources.getDimensionPixelOffset(R.dimen.mtrl_calendar_month_vertical_padding);
    int calendarPadding = resources.getDimensionPixelOffset(R.dimen.mtrl_calendar_bottom_padding);
    return navigationHeight + daysOfWeekHeight + calendarHeight + calendarPadding;
  }

  @NonNull
  LinearLayoutManager getLayoutManager() {
    return (LinearLayoutManager) recyclerView.getLayoutManager();
  }

  @Override
  public boolean addOnSelectionChangedListener(@NonNull OnSelectionChangedListener<S> listener) {
    return super.addOnSelectionChangedListener(listener);
  }
}
