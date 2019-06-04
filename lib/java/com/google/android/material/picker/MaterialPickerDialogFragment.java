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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.internal.CheckableImageButton;
import com.google.android.material.resources.MaterialAttributes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A {@link Dialog} with a header, {@link MaterialCalendar}, and set of actions.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public abstract class MaterialPickerDialogFragment<S> extends DialogFragment {

  /**
   * The earliest selectable {@link Month} if {@link CalendarBounds} are not specified: January
   * 1900.
   */
  public static final Month DEFAULT_START = Month.create(1900, Calendar.JANUARY);
  /**
   * The earliest selectable {@link Month} if {@link CalendarBounds} are not specified: December
   * 2100.
   */
  public static final Month DEFAULT_END = Month.create(2100, Calendar.DECEMBER);

  /**
   * The default {@link CalendarBounds}: starting at {@code DEFAULT_START}, ending at {@code
   * DEFAULT_END}, and opening on {@link Month#today()}
   */
  public static final CalendarBounds DEFAULT_BOUNDS =
      CalendarBounds.create(DEFAULT_START, DEFAULT_END);

  private static final String THEME_RES_ID_KEY = "THEME_RES_ID";
  private static final String GRID_SELECTOR_KEY = "GRID_SELECTOR_KEY";
  private static final String CALENDAR_BOUNDS_KEY = "CALENDAR_BOUNDS_KEY";
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
   *
   * @param selection The current user selection
   */
  protected abstract String getHeaderText(@Nullable S selection);

  /** Returns an {@link @AttrRes} to apply as a theme overlay to the DialogFragment */
  protected abstract int getDefaultThemeAttr();

  /**
   * Creates the {@link GridSelector} used for the {@link MaterialCalendar} in this {@link
   * DialogFragment}.
   */
  protected abstract GridSelector<S> createGridSelector();

  private SimpleDateFormat userDefinedSimpleDateFormat;

  @AttrRes private int themeResId;
  private GridSelector<S> gridSelector;
  private PickerFragment<S> pickerFragment;
  private CalendarBounds calendarBounds;
  @StringRes private int titleTextResId;

  private TextView header;
  private CheckableImageButton headerToggleButton;

  /**
   * Adds the super class required arguments to the Bundle.
   *
   * <p>Call this method in subclasses before the initial call to {@link
   * DialogFragment#setArguments(Bundle)}
   *
   * @param args The Bundle from the subclassing DialogFragment
   * @param themeResId 0 or a {@link StyleRes} representing a ThemeOverlay
   */
  protected static void addArgsToBundle(
      Bundle args,
      int themeResId,
      CalendarBounds calendarBounds,
      @StringRes int overlineTextResId) {
    args.putInt(THEME_RES_ID_KEY, themeResId);
    args.putParcelable(CALENDAR_BOUNDS_KEY, calendarBounds);
    args.putInt(TITLE_TEXT_RES_ID_KEY, overlineTextResId);
  }

  @StyleRes
  private static int getThemeResource(Context context, int defaultThemeAttr, int themeResId) {
    if (themeResId != 0) {
      return themeResId;
    }
    return MaterialAttributes.resolveOrThrow(
        context, defaultThemeAttr, MaterialPickerDialogFragment.class.getCanonicalName());
  }

  @Override
  public final void onSaveInstanceState(Bundle bundle) {
    super.onSaveInstanceState(bundle);
    bundle.putInt(THEME_RES_ID_KEY, themeResId);
    bundle.putParcelable(GRID_SELECTOR_KEY, gridSelector);
    bundle.putParcelable(CALENDAR_BOUNDS_KEY, calendarBounds);
    bundle.putInt(TITLE_TEXT_RES_ID_KEY, titleTextResId);
  }

  @Override
  public final void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    Bundle activeBundle = bundle == null ? getArguments() : bundle;
    themeResId =
        getThemeResource(
            getContext(), getDefaultThemeAttr(), activeBundle.getInt(THEME_RES_ID_KEY));
    gridSelector = activeBundle.getParcelable(GRID_SELECTOR_KEY);
    calendarBounds = activeBundle.getParcelable(CALENDAR_BOUNDS_KEY);
    titleTextResId = activeBundle.getInt(TITLE_TEXT_RES_ID_KEY);

    if (gridSelector == null) {
      gridSelector = createGridSelector();
    }
  }

  @Override
  public final Dialog onCreateDialog(@Nullable Bundle bundle) {
    return new Dialog(requireContext(), themeResId);
  }

  @NonNull
  @Override
  public final View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View root = layoutInflater.inflate(R.layout.mtrl_picker_dialog, viewGroup);
    Context context = root.getContext();
    header = root.findViewById(R.id.mtrl_picker_header_text);
    headerToggleButton = root.findViewById(R.id.mtrl_picker_header_toggle);
    ((TextView) root.findViewById(R.id.mtrl_picker_title_text)).setText(titleTextResId);

    initHeaderToggle(context);

    MaterialButton confirmButton = root.findViewById(R.id.confirm_button);
    confirmButton.setTag(CONFIRM_BUTTON_TAG);
    confirmButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            dismiss();
          }
        });

    MaterialButton cancelButton = root.findViewById(R.id.cancel_button);
    cancelButton.setTag(CANCEL_BUTTON_TAG);
    cancelButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            dismiss();
          }
        });
    return root;
  }

  @Override
  public void onStart() {
    super.onStart();
    startPickerFragment();
  }

  @Override
  public void onStop() {
    pickerFragment.getGridSelector().clearOnSelectionChangedListeners();
    super.onStop();
  }

  @Override
  public final void onDismiss(@NonNull DialogInterface dialogInterface) {
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

  /**
   * Sets a user-defined date formatter.
   *
   * <p>Useful when the default localized date format is inadequate
   */
  public final void setSimpleDateFormat(@Nullable SimpleDateFormat simpleDateFormat) {
    userDefinedSimpleDateFormat = simpleDateFormat;
  }

  /** Returns the user-defined date formatter. */
  @Nullable
  public final SimpleDateFormat getSimpleDateFormat() {
    return userDefinedSimpleDateFormat;
  }

  private void updateHeader(S selection) {
    header.setText(getHeaderText(selection));
  }

  private void startPickerFragment() {
    pickerFragment =
        headerToggleButton.isChecked()
            ? MaterialTextInputPicker.newInstance(gridSelector, calendarBounds)
            : MaterialCalendar.newInstance(gridSelector, themeResId, calendarBounds);
    updateHeader(gridSelector.getSelection());

    FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.mtrl_calendar_frame, pickerFragment);
    fragmentTransaction.commitNow();

    pickerFragment
        .getGridSelector()
        .addOnSelectionChangedListener(
            new OnSelectionChangedListener<S>() {
              @Override
              public void onSelectionChanged(S selection) {
                updateHeader(selection);
              }
            });
  }

  private void initHeaderToggle(Context context) {
    headerToggleButton.setTag(TOGGLE_BUTTON_TAG);
    headerToggleButton.setImageDrawable(createHeaderToggleDrawable(context));
    headerToggleButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            headerToggleButton.toggle();
            startPickerFragment();
          }
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
}
