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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.appcompat.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import com.google.android.material.dialog.InsetDialogOnTouchListener;
import com.google.android.material.internal.CheckableImageButton;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.shape.MaterialShapeDrawable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashSet;

/** A {@link Dialog} with a header, {@link MaterialCalendar}, and set of actions. */
public final class MaterialDatePicker<S> extends DialogFragment {

  private static final String OVERRIDE_THEME_RES_ID = "OVERRIDE_THEME_RES_ID";
  private static final String DATE_SELECTOR_KEY = "DATE_SELECTOR_KEY";
  private static final String CALENDAR_CONSTRAINTS_KEY = "CALENDAR_CONSTRAINTS_KEY";
  private static final String TITLE_TEXT_RES_ID_KEY = "TITLE_TEXT_RES_ID_KEY";
  private static final String TITLE_TEXT_KEY = "TITLE_TEXT_KEY";
  private static final String INPUT_MODE_KEY = "INPUT_MODE_KEY";

  static final Object CONFIRM_BUTTON_TAG = "CONFIRM_BUTTON_TAG";
  static final Object CANCEL_BUTTON_TAG = "CANCEL_BUTTON_TAG";
  static final Object TOGGLE_BUTTON_TAG = "TOGGLE_BUTTON_TAG";

  /** Date picker will start with calendar view. */
  public static final int INPUT_MODE_CALENDAR = 0;

  /** Date picker will start with input text view. */
  public static final int INPUT_MODE_TEXT = 1;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(value = {INPUT_MODE_CALENDAR, INPUT_MODE_TEXT})
  @Retention(RetentionPolicy.SOURCE)
  public @interface InputMode {}

  /** Returns the UTC milliseconds representing the first moment of today in local timezone. */
  public static long todayInUtcMilliseconds() {
    return UtcDates.getTodayCalendar().getTimeInMillis();
  }

  /**
   * Returns the UTC milliseconds representing the first moment in current month in local timezone.
   */
  public static long thisMonthInUtcMilliseconds() {
    return Month.current().timeInMillis;
  }

  /**
   * Returns the text to display at the top of the {@link DialogFragment}
   *
   * <p>The text is updated when the Dialog launches and on user clicks.
   */
  public String getHeaderText() {
    return dateSelector.getSelectionDisplayString(getContext());
  }

  private final LinkedHashSet<MaterialPickerOnPositiveButtonClickListener<? super S>>
      onPositiveButtonClickListeners = new LinkedHashSet<>();
  private final LinkedHashSet<View.OnClickListener> onNegativeButtonClickListeners =
      new LinkedHashSet<>();
  private final LinkedHashSet<DialogInterface.OnCancelListener> onCancelListeners =
      new LinkedHashSet<>();
  private final LinkedHashSet<DialogInterface.OnDismissListener> onDismissListeners =
      new LinkedHashSet<>();

  @StyleRes private int overrideThemeResId;
  @Nullable private DateSelector<S> dateSelector;
  private PickerFragment<S> pickerFragment;
  @Nullable private CalendarConstraints calendarConstraints;
  private MaterialCalendar<S> calendar;
  @StringRes private int titleTextResId;
  private CharSequence titleText;
  private boolean fullscreen;
  @InputMode private int inputMode;

  private TextView headerSelectionText;
  private CheckableImageButton headerToggleButton;
  @Nullable private MaterialShapeDrawable background;
  private Button confirmButton;

  @NonNull
  static <S> MaterialDatePicker<S> newInstance(@NonNull Builder<S> options) {
    MaterialDatePicker<S> materialDatePickerDialogFragment = new MaterialDatePicker<>();
    Bundle args = new Bundle();
    args.putInt(OVERRIDE_THEME_RES_ID, options.overrideThemeResId);
    args.putParcelable(DATE_SELECTOR_KEY, options.dateSelector);
    args.putParcelable(CALENDAR_CONSTRAINTS_KEY, options.calendarConstraints);
    args.putInt(TITLE_TEXT_RES_ID_KEY, options.titleTextResId);
    args.putCharSequence(TITLE_TEXT_KEY, options.titleText);
    args.putInt(INPUT_MODE_KEY, options.inputMode);
    materialDatePickerDialogFragment.setArguments(args);
    return materialDatePickerDialogFragment;
  }

  @Override
  public final void onSaveInstanceState(@NonNull Bundle bundle) {
    super.onSaveInstanceState(bundle);
    bundle.putInt(OVERRIDE_THEME_RES_ID, overrideThemeResId);
    bundle.putParcelable(DATE_SELECTOR_KEY, dateSelector);

    CalendarConstraints.Builder constraintsBuilder =
        new CalendarConstraints.Builder(calendarConstraints);
    if (calendar.getCurrentMonth() != null) {
      constraintsBuilder.setOpenAt(calendar.getCurrentMonth().timeInMillis);
    }
    bundle.putParcelable(CALENDAR_CONSTRAINTS_KEY, constraintsBuilder.build());
    bundle.putInt(TITLE_TEXT_RES_ID_KEY, titleTextResId);
    bundle.putCharSequence(TITLE_TEXT_KEY, titleText);
  }

  @Override
  public final void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    Bundle activeBundle = bundle == null ? getArguments() : bundle;
    overrideThemeResId = activeBundle.getInt(OVERRIDE_THEME_RES_ID);
    dateSelector = activeBundle.getParcelable(DATE_SELECTOR_KEY);
    calendarConstraints = activeBundle.getParcelable(CALENDAR_CONSTRAINTS_KEY);
    titleTextResId = activeBundle.getInt(TITLE_TEXT_RES_ID_KEY);
    titleText = activeBundle.getCharSequence(TITLE_TEXT_KEY);
    inputMode = activeBundle.getInt(INPUT_MODE_KEY);
  }

  private int getThemeResId(Context context) {
    if (overrideThemeResId != 0) {
      return overrideThemeResId;
    }
    return dateSelector.getDefaultThemeResId(context);
  }

  @NonNull
  @Override
  public final Dialog onCreateDialog(@Nullable Bundle bundle) {
    Dialog dialog = new Dialog(requireContext(), getThemeResId(requireContext()));
    Context context = dialog.getContext();
    fullscreen = isFullscreen(context);
    int surfaceColor =
        MaterialAttributes.resolveOrThrow(
            context, R.attr.colorSurface, MaterialDatePicker.class.getCanonicalName());
    background =
        new MaterialShapeDrawable(
            context,
            null,
            R.attr.materialCalendarStyle,
            R.style.Widget_MaterialComponents_MaterialCalendar);
    background.initializeElevationOverlay(context);
    background.setFillColor(ColorStateList.valueOf(surfaceColor));
    background.setElevation(ViewCompat.getElevation(dialog.getWindow().getDecorView()));
    return dialog;
  }

  @NonNull
  @Override
  public final View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    int layout = fullscreen ? R.layout.mtrl_picker_fullscreen : R.layout.mtrl_picker_dialog;
    View root = layoutInflater.inflate(layout, viewGroup);
    Context context = root.getContext();

    if (fullscreen) {
      View frame = root.findViewById(R.id.mtrl_calendar_frame);
      frame.setLayoutParams(
          new LayoutParams(getPaddedPickerWidth(context), LayoutParams.WRAP_CONTENT));
    } else {
      View pane = root.findViewById(R.id.mtrl_calendar_main_pane);
      View frame = root.findViewById(R.id.mtrl_calendar_frame);
      pane.setLayoutParams(
          new LayoutParams(getPaddedPickerWidth(context), LayoutParams.MATCH_PARENT));
      frame.setMinimumHeight(getDialogPickerHeight(requireContext()));
    }

    headerSelectionText = root.findViewById(R.id.mtrl_picker_header_selection_text);
    ViewCompat.setAccessibilityLiveRegion(
        headerSelectionText, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
    headerToggleButton = root.findViewById(R.id.mtrl_picker_header_toggle);
    TextView titleTextView = root.findViewById(R.id.mtrl_picker_title_text);
    if (titleText != null) {
      titleTextView.setText(titleText);
    } else {
      titleTextView.setText(titleTextResId);
    }
    initHeaderToggle(context);

    confirmButton = root.findViewById(R.id.confirm_button);
    if (dateSelector.isSelectionComplete()) {
      confirmButton.setEnabled(true);
    } else {
      confirmButton.setEnabled(false);
    }
    confirmButton.setTag(CONFIRM_BUTTON_TAG);
    confirmButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            for (MaterialPickerOnPositiveButtonClickListener<? super S> listener :
                onPositiveButtonClickListeners) {
              listener.onPositiveButtonClick(getSelection());
            }
            dismiss();
          }
        });

    Button cancelButton = root.findViewById(R.id.cancel_button);
    cancelButton.setTag(CANCEL_BUTTON_TAG);
    cancelButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            for (View.OnClickListener listener : onNegativeButtonClickListeners) {
              listener.onClick(v);
            }
            dismiss();
          }
        });
    return root;
  }

  @Override
  public void onStart() {
    super.onStart();
    Window window = requireDialog().getWindow();
    // Dialogs use a background with an InsetDrawable by default, so we have to replace it.
    if (fullscreen) {
      window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
      window.setBackgroundDrawable(background);
    } else {
      window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      int inset =
          getResources().getDimensionPixelOffset(R.dimen.mtrl_calendar_dialog_background_inset);
      Rect insets = new Rect(inset, inset, inset, inset);
      window.setBackgroundDrawable(new InsetDrawable(background, inset, inset, inset, inset));
      window
          .getDecorView()
          .setOnTouchListener(new InsetDialogOnTouchListener(requireDialog(), insets));
    }
    startPickerFragment();
  }

  @Override
  public void onStop() {
    pickerFragment.clearOnSelectionChangedListeners();
    super.onStop();
  }

  @Override
  public final void onCancel(@NonNull DialogInterface dialogInterface) {
    for (DialogInterface.OnCancelListener listener : onCancelListeners) {
      listener.onCancel(dialogInterface);
    }
    super.onCancel(dialogInterface);
  }

  @Override
  public final void onDismiss(@NonNull DialogInterface dialogInterface) {
    for (DialogInterface.OnDismissListener listener : onDismissListeners) {
      listener.onDismiss(dialogInterface);
    }
    ViewGroup viewGroup = ((ViewGroup) getView());
    if (viewGroup != null) {
      viewGroup.removeAllViews();
    }
    super.onDismiss(dialogInterface);
  }

  /**
   * Returns an {@code S} instance representing the selection or null if the user has not confirmed
   * a selection.
   */
  @Nullable
  public final S getSelection() {
    return dateSelector.getSelection();
  }

  private void updateHeader() {
    String headerText = getHeaderText();
    headerSelectionText.setContentDescription(
        String.format(getString(R.string.mtrl_picker_announce_current_selection), headerText));
    headerSelectionText.setText(headerText);
  }

  private void startPickerFragment() {
    int themeResId = getThemeResId(requireContext());
    calendar = MaterialCalendar.newInstance(dateSelector, themeResId, calendarConstraints);
    pickerFragment =
        headerToggleButton.isChecked()
            ? MaterialTextInputPicker.newInstance(dateSelector, themeResId, calendarConstraints)
            : calendar;
    updateHeader();

    FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.mtrl_calendar_frame, pickerFragment);
    fragmentTransaction.commitNow();

    pickerFragment.addOnSelectionChangedListener(
        new OnSelectionChangedListener<S>() {
          @Override
          public void onSelectionChanged(S selection) {
            updateHeader();
            confirmButton.setEnabled(dateSelector.isSelectionComplete());
          }

          @Override
          public void onIncompleteSelectionChanged() {
            confirmButton.setEnabled(false);
          }
        });
  }

  private void initHeaderToggle(Context context) {
    headerToggleButton.setTag(TOGGLE_BUTTON_TAG);
    headerToggleButton.setImageDrawable(createHeaderToggleDrawable(context));
    headerToggleButton.setChecked(inputMode != INPUT_MODE_CALENDAR);

    // By default, CheckableImageButton adds a delegate that reads checked state.
    // This information is not useful; we remove the delegate and use custom content descriptions.
    ViewCompat.setAccessibilityDelegate(headerToggleButton, null);
    updateToggleContentDescription(headerToggleButton);
    headerToggleButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            // Update confirm button in case in progress selection has been reset
            confirmButton.setEnabled(dateSelector.isSelectionComplete());

            headerToggleButton.toggle();
            updateToggleContentDescription(headerToggleButton);
            startPickerFragment();
          }
        });
  }

  private void updateToggleContentDescription(@NonNull CheckableImageButton toggle) {
    String contentDescription =
        headerToggleButton.isChecked()
            ? toggle.getContext().getString(R.string.mtrl_picker_toggle_to_calendar_input_mode)
            : toggle.getContext().getString(R.string.mtrl_picker_toggle_to_text_input_mode);
    headerToggleButton.setContentDescription(contentDescription);
  }

  // Create StateListDrawable programmatically for pre-lollipop support
  @NonNull
  private static Drawable createHeaderToggleDrawable(Context context) {
    StateListDrawable toggleDrawable = new StateListDrawable();
    toggleDrawable.addState(
        new int[] {android.R.attr.state_checked},
        AppCompatResources.getDrawable(context, R.drawable.material_ic_calendar_black_24dp));
    toggleDrawable.addState(
        new int[] {},
        AppCompatResources.getDrawable(context, R.drawable.material_ic_edit_black_24dp));
    return toggleDrawable;
  }

  static boolean isFullscreen(@NonNull Context context) {
    return readMaterialCalendarStyleBoolean(context, android.R.attr.windowFullscreen);
  }

  static boolean isNestedScrollable(@NonNull Context context) {
    return readMaterialCalendarStyleBoolean(context, R.attr.nestedScrollable);
  }

  static boolean readMaterialCalendarStyleBoolean(@NonNull Context context, int attributeResId) {
    int calendarStyle =
        MaterialAttributes.resolveOrThrow(
            context, R.attr.materialCalendarStyle, MaterialCalendar.class.getCanonicalName());
    int[] attrs = {attributeResId};
    TypedArray a = context.obtainStyledAttributes(calendarStyle, attrs);
    boolean attributeValue = a.getBoolean(0, false);
    a.recycle();
    return attributeValue;
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

  private static int getPaddedPickerWidth(@NonNull Context context) {
    Resources resources = context.getResources();
    int padding = resources.getDimensionPixelOffset(R.dimen.mtrl_calendar_content_padding);
    int daysInWeek = Month.current().daysInWeek;
    int dayWidth = resources.getDimensionPixelSize(R.dimen.mtrl_calendar_day_width);
    int horizontalSpace =
        resources.getDimensionPixelOffset(R.dimen.mtrl_calendar_month_horizontal_padding);
    return 2 * padding + daysInWeek * dayWidth + (daysInWeek - 1) * horizontalSpace;
  }

  /** The supplied listener is called when the user confirms a valid selection. */
  public boolean addOnPositiveButtonClickListener(
      MaterialPickerOnPositiveButtonClickListener<? super S> onPositiveButtonClickListener) {
    return onPositiveButtonClickListeners.add(onPositiveButtonClickListener);
  }

  /**
   * Removes a listener previously added via {@link
   * MaterialDatePicker#addOnPositiveButtonClickListener}.
   */
  public boolean removeOnPositiveButtonClickListener(
      MaterialPickerOnPositiveButtonClickListener<? super S> onPositiveButtonClickListener) {
    return onPositiveButtonClickListeners.remove(onPositiveButtonClickListener);
  }

  /**
   * Removes all listeners added via {@link MaterialDatePicker#addOnPositiveButtonClickListener}.
   */
  public void clearOnPositiveButtonClickListeners() {
    onPositiveButtonClickListeners.clear();
  }

  /** The supplied listener is called when the user clicks the cancel button. */
  public boolean addOnNegativeButtonClickListener(
      View.OnClickListener onNegativeButtonClickListener) {
    return onNegativeButtonClickListeners.add(onNegativeButtonClickListener);
  }

  /**
   * Removes a listener previously added via {@link
   * MaterialDatePicker#addOnNegativeButtonClickListener}.
   */
  public boolean removeOnNegativeButtonClickListener(
      View.OnClickListener onNegativeButtonClickListener) {
    return onNegativeButtonClickListeners.remove(onNegativeButtonClickListener);
  }

  /**
   * Removes all listeners added via {@link MaterialDatePicker#addOnNegativeButtonClickListener}.
   */
  public void clearOnNegativeButtonClickListeners() {
    onNegativeButtonClickListeners.clear();
  }

  /**
   * The supplied listener is called when the user cancels the picker via back button or a touch
   * outside the view. It is not called when the user clicks the cancel button. To add a listener
   * for use when the user clicks the cancel button, use {@link
   * MaterialDatePicker#addOnNegativeButtonClickListener}.
   */
  public boolean addOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
    return onCancelListeners.add(onCancelListener);
  }

  /** Removes a listener previously added via {@link MaterialDatePicker#addOnCancelListener}. */
  public boolean removeOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
    return onCancelListeners.remove(onCancelListener);
  }

  /** Removes all listeners added via {@link MaterialDatePicker#addOnCancelListener}. */
  public void clearOnCancelListeners() {
    onCancelListeners.clear();
  }

  /**
   * The supplied listener is called whenever the DialogFragment is dismissed, no matter how it is
   * dismissed.
   */
  public boolean addOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
    return onDismissListeners.add(onDismissListener);
  }

  /** Removes a listener previously added via {@link MaterialDatePicker#addOnDismissListener}. */
  public boolean removeOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
    return onDismissListeners.remove(onDismissListener);
  }

  /** Removes all listeners added via {@link MaterialDatePicker#addOnDismissListener}. */
  public void clearOnDismissListeners() {
    onDismissListeners.clear();
  }

  /** Used to create MaterialDatePicker instances with default and overridden settings */
  public static final class Builder<S> {

    final DateSelector<S> dateSelector;
    int overrideThemeResId = 0;

    CalendarConstraints calendarConstraints;
    int titleTextResId = 0;
    CharSequence titleText = null;
    @Nullable S selection = null;
    @InputMode int inputMode = INPUT_MODE_CALENDAR;

    private Builder(DateSelector<S> dateSelector) {
      this.dateSelector = dateSelector;
    }

    /**
     * Sets the Builder's selection manager to the provided {@link DateSelector}.
     *
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @NonNull
    public static <S> Builder<S> customDatePicker(@NonNull DateSelector<S> dateSelector) {
      return new Builder<>(dateSelector);
    }

    /**
     * Used to create a Builder that allows for choosing a single date in the {@code
     * MaterialDatePicker}.
     */
    @NonNull
    public static Builder<Long> datePicker() {
      return new Builder<>(new SingleDateSelector());
    }

    /**
     * Used to create a Builder that allows for choosing a date range in the {@code
     * MaterialDatePicker}.
     */
    @NonNull
    public static Builder<Pair<Long, Long>> dateRangePicker() {
      return new Builder<>(new RangeDateSelector());
    }

    @NonNull
    public Builder<S> setSelection(S selection) {
      this.selection = selection;
      return this;
    }

    /** Sets the theme controlling fullscreen mode as well as other styles. */
    @NonNull
    public Builder<S> setTheme(@StyleRes int themeResId) {
      this.overrideThemeResId = themeResId;
      return this;
    }

    /** Sets the first, last, and starting month. */
    @NonNull
    public Builder<S> setCalendarConstraints(CalendarConstraints bounds) {
      this.calendarConstraints = bounds;
      return this;
    }

    /**
     * Sets the text used to guide the user at the top of the picker. Defaults to a standard title
     * based upon the type of selection.
     */
    @NonNull
    public Builder<S> setTitleText(@StringRes int titleTextResId) {
      this.titleTextResId = titleTextResId;
      this.titleText = null;
      return this;
    }

    /**
     * Sets the text used to guide the user at the top of the picker. Setting to null will use a
     * default title based upon the type of selection.
     */
    @NonNull
    public Builder<S> setTitleText(@Nullable CharSequence charSequence) {
      this.titleText = charSequence;
      this.titleTextResId = 0;
      return this;
    }

    /** Sets the input mode to start with. */
    @NonNull
    public Builder<S> setInputMode(@InputMode int inputMode) {
      this.inputMode = inputMode;
      return this;
    }

    /** Creates a {@link MaterialDatePicker} with the provided options. */
    @NonNull
    public MaterialDatePicker<S> build() {
      if (calendarConstraints == null) {
        calendarConstraints = new CalendarConstraints.Builder().build();
      }
      if (titleTextResId == 0) {
        titleTextResId = dateSelector.getDefaultTitleResId();
      }

      if (selection != null) {
        dateSelector.setSelection(selection);
      }

      if (calendarConstraints.getOpenAt() == null) {
        calendarConstraints.setOpenAt(createDefaultOpenAt());
      }

      return MaterialDatePicker.newInstance(this);
    }

    private Month createDefaultOpenAt() {
      long start = calendarConstraints.getStart().timeInMillis;
      long end = calendarConstraints.getEnd().timeInMillis;

      if (!dateSelector.getSelectedDays().isEmpty()) {
        // Set the month to the first selected month in the selection
        long firstSelectedDay = dateSelector.getSelectedDays().iterator().next();

        // Make sure the selection is in a valid month we can open to; otherwise use default openAt
        if (firstSelectedDay >= start && firstSelectedDay <= end) {
          return Month.create(firstSelectedDay);
        }
      }

      long today = MaterialDatePicker.thisMonthInUtcMilliseconds();
      long openAt = start <= today && today <= end ? today : start;
      return Month.create(openAt);
    }
  }
}
