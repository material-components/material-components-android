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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.shape.MaterialShapeDrawable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.util.Pair;
import androidx.appcompat.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.google.android.material.dialog.InsetDialogOnTouchListener;
import com.google.android.material.internal.CheckableImageButton;
import java.util.Calendar;
import java.util.LinkedHashSet;

/**
 * A {@link Dialog} with a header, {@link MaterialCalendar}, and set of actions.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class MaterialDatePicker<S> extends DialogFragment {

  /**
   * The earliest selectable {@link Month} if {@link CalendarConstraints} are not specified: January
   * 1900.
   */
  public static final Month DEFAULT_START = Month.create(1900, Calendar.JANUARY);
  /**
   * The earliest selectable {@link Month} if {@link CalendarConstraints} are not specified:
   * December 2100.
   */
  public static final Month DEFAULT_END = Month.create(2100, Calendar.DECEMBER);

  /**
   * The default {@link CalendarConstraints}: starting at {@code DEFAULT_START}, ending at {@code
   * DEFAULT_END}, and opening on {@link Month#today()}
   */
  public static final CalendarConstraints DEFAULT_BOUNDS =
      CalendarConstraints.create(DEFAULT_START, DEFAULT_END);

  private static final String OVERRIDE_THEME_RES_ID = "OVERRIDE_THEME_RES_ID";
  private static final String GRID_SELECTOR_KEY = "GRID_SELECTOR_KEY";
  private static final String CALENDAR_CONSTRAINTS_KEY = "CALENDAR_CONSTRAINTS_KEY";
  private static final String TITLE_TEXT_RES_ID_KEY = "TITLE_TEXT_RES_ID_KEY";

  @VisibleForTesting
  @RestrictTo(Scope.LIBRARY_GROUP)
  public static final Object CONFIRM_BUTTON_TAG = "CONFIRM_BUTTON_TAG";

  @VisibleForTesting
  @RestrictTo(Scope.LIBRARY_GROUP)
  public static final Object CANCEL_BUTTON_TAG = "CANCEL_BUTTON_TAG";

  @VisibleForTesting
  @RestrictTo(Scope.LIBRARY_GROUP)
  public static final Object TOGGLE_BUTTON_TAG = "TOGGLE_BUTTON_TAG";

  /**
   * Returns the text to display at the top of the {@link DialogFragment}
   *
   * <p>The text is updated when the Dialog launches and on user clicks.
   */
  public String getHeaderText() {
    return gridSelector.getSelectionDisplayString(getContext());
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
  private GridSelector<S> gridSelector;
  private PickerFragment<S> pickerFragment;
  private CalendarConstraints calendarConstraints;
  @StringRes private int titleTextResId;
  private boolean fullscreen;

  private TextView headerSelectionText;
  private CheckableImageButton headerToggleButton;
  private MaterialShapeDrawable background;

  static <S> MaterialDatePicker<S> newInstance(Builder<S> options) {
    MaterialDatePicker<S> materialDatePickerDialogFragment = new MaterialDatePicker<>();
    Bundle args = new Bundle();
    args.putInt(OVERRIDE_THEME_RES_ID, options.overrideThemeResId);
    args.putParcelable(GRID_SELECTOR_KEY, options.gridSelector);
    args.putParcelable(CALENDAR_CONSTRAINTS_KEY, options.calendarConstraints);
    args.putInt(TITLE_TEXT_RES_ID_KEY, options.titleTextResId);
    materialDatePickerDialogFragment.setArguments(args);
    return materialDatePickerDialogFragment;
  }

  @Override
  public final void onSaveInstanceState(Bundle bundle) {
    super.onSaveInstanceState(bundle);
    bundle.putInt(OVERRIDE_THEME_RES_ID, overrideThemeResId);
    bundle.putParcelable(GRID_SELECTOR_KEY, gridSelector);
    bundle.putParcelable(CALENDAR_CONSTRAINTS_KEY, calendarConstraints);
    bundle.putInt(TITLE_TEXT_RES_ID_KEY, titleTextResId);
  }

  @Override
  public final void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    Bundle activeBundle = bundle == null ? getArguments() : bundle;
    overrideThemeResId = activeBundle.getInt(OVERRIDE_THEME_RES_ID);
    gridSelector = activeBundle.getParcelable(GRID_SELECTOR_KEY);
    calendarConstraints = activeBundle.getParcelable(CALENDAR_CONSTRAINTS_KEY);
    titleTextResId = activeBundle.getInt(TITLE_TEXT_RES_ID_KEY);
  }

  private int getThemeResId(Context context) {
    if (overrideThemeResId != 0) {
      return overrideThemeResId;
    }
    return gridSelector.getDefaultThemeResId(context);
  }

  @Override
  public final Dialog onCreateDialog(@Nullable Bundle bundle) {
    Dialog dialog = new Dialog(requireContext(), getThemeResId(requireContext()));
    Context context = dialog.getContext();
    fullscreen = isFullscreen(context);
    int surfaceColor =
        MaterialAttributes.resolveOrThrow(
            getContext(), R.attr.colorSurface, MaterialDatePicker.class.getCanonicalName());
    background =
        new MaterialShapeDrawable(
            context,
            null,
            R.attr.materialCalendarStyle,
            R.style.Widget_MaterialComponents_MaterialCalendar);
    background.initializeElevationOverlay(context);
    background.setFillColor(ColorStateList.valueOf(surfaceColor));
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

    View frame = root.findViewById(R.id.mtrl_calendar_frame);
    if (fullscreen) {
      frame.setLayoutParams(
          new LayoutParams(getPaddedPickerWidth(context), LayoutParams.WRAP_CONTENT));
    } else {
      frame.setLayoutParams(
          new LayoutParams(getPaddedPickerWidth(context), getDialogPickerHeight(context)));
    }
    headerSelectionText = root.findViewById(R.id.mtrl_picker_header_selection_text);
    headerToggleButton = root.findViewById(R.id.mtrl_picker_header_toggle);
    ((TextView) root.findViewById(R.id.mtrl_picker_title_text)).setText(titleTextResId);
    initHeaderToggle(context);

    MaterialButton confirmButton = root.findViewById(R.id.confirm_button);
    confirmButton.setTag(CONFIRM_BUTTON_TAG);
    confirmButton.setOnClickListener(
        v -> {
          for (MaterialPickerOnPositiveButtonClickListener<? super S> listener :
              onPositiveButtonClickListeners) {
            listener.onPositiveButtonClick(getSelection());
          }
          dismiss();
        });

    MaterialButton cancelButton = root.findViewById(R.id.cancel_button);
    cancelButton.setTag(CANCEL_BUTTON_TAG);
    cancelButton.setOnClickListener(
        v -> {
          for (View.OnClickListener listener : onNegativeButtonClickListeners) {
            listener.onClick(v);
          }
          dismiss();
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
      window.setBackgroundDrawable(new InsetDrawable(background, inset));
      Rect insets = new Rect(inset, inset, inset, inset);
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
   * Returns a {@link S} instance representing the selection or null if the user has not confirmed a
   * selection.
   */
  @Nullable
  public final S getSelection() {
    return gridSelector.getSelection();
  }

  private void updateHeader(S selection) {
    headerSelectionText.setText(getHeaderText());
  }

  private void startPickerFragment() {
    pickerFragment =
        headerToggleButton.isChecked()
            ? MaterialTextInputPicker.newInstance(gridSelector, calendarConstraints)
            : MaterialCalendar.newInstance(
                gridSelector, getThemeResId(requireContext()), calendarConstraints);
    updateHeader(gridSelector.getSelection());

    FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.mtrl_calendar_frame, pickerFragment);
    fragmentTransaction.commitNow();

    pickerFragment.addOnSelectionChangedListener(this::updateHeader);
  }

  private void initHeaderToggle(Context context) {
    headerToggleButton.setTag(TOGGLE_BUTTON_TAG);
    headerToggleButton.setImageDrawable(createHeaderToggleDrawable(context));
    headerToggleButton.setOnClickListener(
        v -> {
          headerToggleButton.toggle();
          startPickerFragment();
        });
  }

  // Create StateListDrawable programmatically for pre-lollipop support
  private static Drawable createHeaderToggleDrawable(Context context) {
    StateListDrawable toggleDrawable = new StateListDrawable();
    toggleDrawable.addState(
        new int[] {android.R.attr.state_checked},
        AppCompatResources.getDrawable(context, R.drawable.ic_calendar_black_24dp));
    toggleDrawable.addState(
        new int[] {}, AppCompatResources.getDrawable(context, R.drawable.ic_edit_black_24dp));
    return toggleDrawable;
  }

  static boolean isFullscreen(Context context) {
    int calendarStyle =
        MaterialAttributes.resolveOrThrow(
            context, R.attr.materialCalendarStyle, MaterialCalendar.class.getCanonicalName());
    int[] attrs = {android.R.attr.windowFullscreen};
    TypedArray a = context.obtainStyledAttributes(calendarStyle, attrs);
    boolean fullscreen = a.getBoolean(0, false);
    a.recycle();
    return fullscreen;
  }

  private static int getDialogPickerHeight(Context context) {
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

  private static int getPaddedPickerWidth(Context context) {
    Resources resources = context.getResources();
    int padding = resources.getDimensionPixelOffset(R.dimen.mtrl_calendar_content_padding);
    int daysInWeek = Month.today().daysInWeek;
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
  public static class Builder<S> {

    final GridSelector<S> gridSelector;
    int overrideThemeResId = 0;

    CalendarConstraints calendarConstraints;
    int titleTextResId = 0;

    private Builder(GridSelector<S> gridSelector) {
      this.gridSelector = gridSelector;
    }

    /**
     * Sets the Builder's selection manager to the provided {@link
     * com.google.android.material.picker.GridSelector}.
     */
    public static <S> Builder<S> customDatePicker(GridSelector<S> gridSelector) {
      return new Builder<>(gridSelector);
    }

    /** Used to create a Builder using a {@link DateGridSelector}. */
    public static Builder<Long> datePicker() {
      return new Builder<>(new DateGridSelector());
    }

    /** Used to create a Builder using {@link DateRangeGridSelector}. */
    public static Builder<Pair<Long, Long>> dateRangePicker() {
      return new Builder<>(new DateRangeGridSelector());
    }

    /** Sets the theme controlling fullscreen mode as well as other styles. */
    public Builder<S> setTheme(@StyleRes int themeResId) {
      this.overrideThemeResId = themeResId;
      return this;
    }

    /** Sets the first, last, and starting {@link Month}. */
    public Builder<S> setCalendarConstraints(CalendarConstraints bounds) {
      this.calendarConstraints = bounds;
      return this;
    }

    /** Sets the text used to guide the user at the top of the picker. */
    public Builder<S> setTitleTextResId(@StringRes int titleTextResId) {
      this.titleTextResId = titleTextResId;
      return this;
    }

    /** Creates a {@link MaterialDatePicker} with the provided options. */
    public MaterialDatePicker<S> build() {
      if (calendarConstraints == null) {
        calendarConstraints = MaterialDatePicker.DEFAULT_BOUNDS;
      }
      if (titleTextResId == 0) {
        titleTextResId = gridSelector.getDefaultTitleResId();
      }
      return MaterialDatePicker.newInstance(this);
    }
  }
}
