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
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.TooltipCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
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
import androidx.annotation.VisibleForTesting;
import androidx.core.graphics.Insets;
import androidx.core.util.Pair;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.dialog.InsetDialogOnTouchListener;
import com.google.android.material.internal.CheckableImageButton;
import com.google.android.material.internal.EdgeToEdgeUtils;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;

/**
 * A {@link Dialog} with a header, {@link MaterialCalendar}, and set of actions.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/DatePicker.md">component
 * developer guidance</a> and <a href="https://material.io/components/date-pickers/overview">design
 * guidelines</a>.
 */
public class MaterialDatePicker<S> extends DialogFragment {

  private static final String OVERRIDE_THEME_RES_ID = "OVERRIDE_THEME_RES_ID";
  private static final String DATE_SELECTOR_KEY = "DATE_SELECTOR_KEY";
  private static final String CALENDAR_CONSTRAINTS_KEY = "CALENDAR_CONSTRAINTS_KEY";
  private static final String DAY_VIEW_DECORATOR_KEY = "DAY_VIEW_DECORATOR_KEY";
  private static final String TITLE_TEXT_RES_ID_KEY = "TITLE_TEXT_RES_ID_KEY";
  private static final String TITLE_TEXT_KEY = "TITLE_TEXT_KEY";
  private static final String POSITIVE_BUTTON_TEXT_RES_ID_KEY = "POSITIVE_BUTTON_TEXT_RES_ID_KEY";
  private static final String POSITIVE_BUTTON_TEXT_KEY = "POSITIVE_BUTTON_TEXT_KEY";
  private static final String POSITIVE_BUTTON_CONTENT_DESCRIPTION_RES_ID_KEY =
      "POSITIVE_BUTTON_CONTENT_DESCRIPTION_RES_ID_KEY";
  private static final String POSITIVE_BUTTON_CONTENT_DESCRIPTION_KEY =
      "POSITIVE_BUTTON_CONTENT_DESCRIPTION_KEY";
  private static final String NEGATIVE_BUTTON_TEXT_RES_ID_KEY = "NEGATIVE_BUTTON_TEXT_RES_ID_KEY";
  private static final String NEGATIVE_BUTTON_TEXT_KEY = "NEGATIVE_BUTTON_TEXT_KEY";
  private static final String NEGATIVE_BUTTON_CONTENT_DESCRIPTION_RES_ID_KEY =
      "NEGATIVE_BUTTON_CONTENT_DESCRIPTION_RES_ID_KEY";
  private static final String NEGATIVE_BUTTON_CONTENT_DESCRIPTION_KEY =
      "NEGATIVE_BUTTON_CONTENT_DESCRIPTION_KEY";
  private static final String INPUT_MODE_KEY = "INPUT_MODE_KEY";
  private static final String CALENDAR_FRAGMENT_TAG = "CALENDAR_FRAGMENT_TAG";
  private static final String TEXT_INPUT_FRAGMENT_TAG = "TEXT_INPUT_FRAGMENT_TAG";

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
    return getDateSelector().getSelectionDisplayString(getContext());
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
  @Nullable private DayViewDecorator dayViewDecorator;
  private MaterialCalendar<S> calendar;
  @StringRes private int titleTextResId;
  private CharSequence titleText;
  private boolean fullscreen;
  @InputMode private int inputMode;
  @StringRes private int positiveButtonTextResId;
  private CharSequence positiveButtonText;
  @StringRes private int positiveButtonContentDescriptionResId;
  private CharSequence positiveButtonContentDescription;
  @StringRes private int negativeButtonTextResId;
  private CharSequence negativeButtonText;
  @StringRes private int negativeButtonContentDescriptionResId;
  private CharSequence negativeButtonContentDescription;
  private TextView headerTitleTextView;
  private TextView headerSelectionText;
  private CheckableImageButton headerToggleButton;
  @Nullable private MaterialShapeDrawable background;
  private Button confirmButton;

  private boolean edgeToEdgeEnabled;
  @Nullable private CharSequence fullTitleText;
  @Nullable private CharSequence singleLineTitleText;

  @NonNull
  static <S> MaterialDatePicker<S> newInstance(@NonNull Builder<S> options) {
    MaterialDatePicker<S> materialDatePickerDialogFragment = new MaterialDatePicker<>();
    Bundle args = new Bundle();
    args.putInt(OVERRIDE_THEME_RES_ID, options.overrideThemeResId);
    args.putParcelable(DATE_SELECTOR_KEY, options.dateSelector);
    args.putParcelable(CALENDAR_CONSTRAINTS_KEY, options.calendarConstraints);
    args.putParcelable(DAY_VIEW_DECORATOR_KEY, options.dayViewDecorator);
    args.putInt(TITLE_TEXT_RES_ID_KEY, options.titleTextResId);
    args.putCharSequence(TITLE_TEXT_KEY, options.titleText);
    args.putInt(INPUT_MODE_KEY, options.inputMode);
    args.putInt(POSITIVE_BUTTON_TEXT_RES_ID_KEY, options.positiveButtonTextResId);
    args.putCharSequence(POSITIVE_BUTTON_TEXT_KEY, options.positiveButtonText);
    args.putInt(
        POSITIVE_BUTTON_CONTENT_DESCRIPTION_RES_ID_KEY,
        options.positiveButtonContentDescriptionResId);
    args.putCharSequence(
        POSITIVE_BUTTON_CONTENT_DESCRIPTION_KEY, options.positiveButtonContentDescription);
    args.putInt(NEGATIVE_BUTTON_TEXT_RES_ID_KEY, options.negativeButtonTextResId);
    args.putCharSequence(NEGATIVE_BUTTON_TEXT_KEY, options.negativeButtonText);
    args.putInt(
        NEGATIVE_BUTTON_CONTENT_DESCRIPTION_RES_ID_KEY,
        options.negativeButtonContentDescriptionResId);
    args.putCharSequence(
        NEGATIVE_BUTTON_CONTENT_DESCRIPTION_KEY, options.negativeButtonContentDescription);
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
    Month currentMonth = calendar == null ? null : calendar.getCurrentMonth();
    if (currentMonth != null) {
      constraintsBuilder.setOpenAt(currentMonth.timeInMillis);
    }
    bundle.putParcelable(CALENDAR_CONSTRAINTS_KEY, constraintsBuilder.build());
    bundle.putParcelable(DAY_VIEW_DECORATOR_KEY, dayViewDecorator);
    bundle.putInt(TITLE_TEXT_RES_ID_KEY, titleTextResId);
    bundle.putCharSequence(TITLE_TEXT_KEY, titleText);
    bundle.putInt(INPUT_MODE_KEY, inputMode);
    bundle.putInt(POSITIVE_BUTTON_TEXT_RES_ID_KEY, positiveButtonTextResId);
    bundle.putCharSequence(POSITIVE_BUTTON_TEXT_KEY, positiveButtonText);
    bundle.putInt(
        POSITIVE_BUTTON_CONTENT_DESCRIPTION_RES_ID_KEY, positiveButtonContentDescriptionResId);
    bundle.putCharSequence(
        POSITIVE_BUTTON_CONTENT_DESCRIPTION_KEY, positiveButtonContentDescription);
    bundle.putInt(NEGATIVE_BUTTON_TEXT_RES_ID_KEY, negativeButtonTextResId);
    bundle.putCharSequence(NEGATIVE_BUTTON_TEXT_KEY, negativeButtonText);
    bundle.putInt(
        NEGATIVE_BUTTON_CONTENT_DESCRIPTION_RES_ID_KEY, negativeButtonContentDescriptionResId);
    bundle.putCharSequence(
        NEGATIVE_BUTTON_CONTENT_DESCRIPTION_KEY, negativeButtonContentDescription);
  }

  @Override
  public final void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    Bundle activeBundle = bundle == null ? getArguments() : bundle;
    overrideThemeResId = activeBundle.getInt(OVERRIDE_THEME_RES_ID);
    dateSelector = activeBundle.getParcelable(DATE_SELECTOR_KEY);
    calendarConstraints = activeBundle.getParcelable(CALENDAR_CONSTRAINTS_KEY);
    dayViewDecorator = activeBundle.getParcelable(DAY_VIEW_DECORATOR_KEY);
    titleTextResId = activeBundle.getInt(TITLE_TEXT_RES_ID_KEY);
    titleText = activeBundle.getCharSequence(TITLE_TEXT_KEY);
    inputMode = activeBundle.getInt(INPUT_MODE_KEY);
    positiveButtonTextResId = activeBundle.getInt(POSITIVE_BUTTON_TEXT_RES_ID_KEY);
    positiveButtonText = activeBundle.getCharSequence(POSITIVE_BUTTON_TEXT_KEY);
    positiveButtonContentDescriptionResId =
        activeBundle.getInt(POSITIVE_BUTTON_CONTENT_DESCRIPTION_RES_ID_KEY);
    positiveButtonContentDescription =
        activeBundle.getCharSequence(POSITIVE_BUTTON_CONTENT_DESCRIPTION_KEY);
    negativeButtonTextResId = activeBundle.getInt(NEGATIVE_BUTTON_TEXT_RES_ID_KEY);
    negativeButtonText = activeBundle.getCharSequence(NEGATIVE_BUTTON_TEXT_KEY);
    negativeButtonContentDescriptionResId =
        activeBundle.getInt(NEGATIVE_BUTTON_CONTENT_DESCRIPTION_RES_ID_KEY);
    negativeButtonContentDescription =
        activeBundle.getCharSequence(NEGATIVE_BUTTON_CONTENT_DESCRIPTION_KEY);

    fullTitleText =
        titleText != null ? titleText : requireContext().getResources().getText(titleTextResId);
    singleLineTitleText = getFirstLineBySeparator(fullTitleText);
  }

  private int getThemeResId(Context context) {
    if (overrideThemeResId != 0) {
      return overrideThemeResId;
    }
    return getDateSelector().getDefaultThemeResId(context);
  }

  @NonNull
  @Override
  public final Dialog onCreateDialog(@Nullable Bundle bundle) {
    Dialog dialog = new Dialog(requireContext(), getThemeResId(requireContext()));
    Context context = dialog.getContext();
    fullscreen = isFullscreen(context);
    background =
        new MaterialShapeDrawable(
            context,
            null,
            R.attr.materialCalendarStyle,
            R.style.Widget_MaterialComponents_MaterialCalendar);

    TypedArray a =
        context.obtainStyledAttributes(
            null,
            R.styleable.MaterialCalendar,
            R.attr.materialCalendarStyle,
            R.style.Widget_MaterialComponents_MaterialCalendar);

    int backgroundColor = a.getColor(R.styleable.MaterialCalendar_backgroundTint, 0);

    a.recycle();

    background.initializeElevationOverlay(context);
    background.setFillColor(ColorStateList.valueOf(backgroundColor));
    background.setElevation(dialog.getWindow().getDecorView().getElevation());
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

    if (dayViewDecorator != null) {
      dayViewDecorator.initialize(context);
    }

    if (fullscreen) {
      View frame = root.findViewById(R.id.mtrl_calendar_frame);
      frame.setLayoutParams(
          new LayoutParams(getPaddedPickerWidth(context), LayoutParams.WRAP_CONTENT));
    } else {
      View pane = root.findViewById(R.id.mtrl_calendar_main_pane);
      pane.setLayoutParams(
          new LayoutParams(getPaddedPickerWidth(context), LayoutParams.MATCH_PARENT));
    }

    headerSelectionText = root.findViewById(R.id.mtrl_picker_header_selection_text);
    headerSelectionText.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
    headerToggleButton = root.findViewById(R.id.mtrl_picker_header_toggle);
    headerTitleTextView = root.findViewById(R.id.mtrl_picker_title_text);
    initHeaderToggle(context);

    confirmButton = root.findViewById(R.id.confirm_button);
    if (getDateSelector().isSelectionComplete()) {
      confirmButton.setEnabled(true);
    } else {
      confirmButton.setEnabled(false);
    }
    confirmButton.setTag(CONFIRM_BUTTON_TAG);
    if (positiveButtonText != null) {
      confirmButton.setText(positiveButtonText);
    } else if (positiveButtonTextResId != 0) {
      confirmButton.setText(positiveButtonTextResId);
    }
    if (positiveButtonContentDescription != null) {
      confirmButton.setContentDescription(positiveButtonContentDescription);
    } else if (positiveButtonContentDescriptionResId != 0) {
      confirmButton.setContentDescription(
          getContext().getResources().getText(positiveButtonContentDescriptionResId));
    }
    confirmButton.setOnClickListener(this::onPositiveButtonClick);

    Button cancelButton = root.findViewById(R.id.cancel_button);
    cancelButton.setTag(CANCEL_BUTTON_TAG);
    if (negativeButtonText != null) {
      cancelButton.setText(negativeButtonText);
    } else if (negativeButtonTextResId != 0) {
      cancelButton.setText(negativeButtonTextResId);
    }
    if (negativeButtonContentDescription != null) {
      cancelButton.setContentDescription(negativeButtonContentDescription);
    } else if (negativeButtonContentDescriptionResId != 0) {
      cancelButton.setContentDescription(
          getContext().getResources().getText(negativeButtonContentDescriptionResId));
    }
    cancelButton.setOnClickListener(this::onNegativeButtonClick);
    return root;
  }

  /**
   * Called when the positive button on the picker has been clicked.
   *
   * @param view The view that was clicked.
   */
  public void onPositiveButtonClick(@NonNull View view) {
    for (MaterialPickerOnPositiveButtonClickListener<? super S> listener :
        onPositiveButtonClickListeners) {
      listener.onPositiveButtonClick(getSelection());
    }
    dismiss();
  }

  /**
   * Called when the negative button on the picker has been clicked.
   *
   * @param view The view that was clicked.
   */
  public void onNegativeButtonClick(@NonNull View view) {
    for (View.OnClickListener listener : onNegativeButtonClickListeners) {
      listener.onClick(view);
    }
    dismiss();
  }

  @Override
  public void onStart() {
    super.onStart();
    Window window = requireDialog().getWindow();
    // Dialogs use a background with an InsetDrawable by default, so we have to replace it.
    if (fullscreen) {
      window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
      window.setBackgroundDrawable(background);
      enableEdgeToEdgeIfNeeded(window);
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
    return getDateSelector().getSelection();
  }

  /** Returns the current {@link InputMode}. */
  @InputMode
  public int getInputMode() {
    return inputMode;
  }

  private void enableEdgeToEdgeIfNeeded(Window window) {
    if (edgeToEdgeEnabled) {
      // Avoid enabling edge-to-edge multiple times.
      return;
    }
    final View headerLayout = requireView().findViewById(R.id.fullscreen_header);
    EdgeToEdgeUtils.applyEdgeToEdge(window, true, ViewUtils.getBackgroundColor(headerLayout), null);
    final int originalPaddingTop = headerLayout.getPaddingTop();
    final int originalPaddingLeft = headerLayout.getPaddingLeft();
    final int originalPaddingRight = headerLayout.getPaddingRight();
    final int originalHeaderHeight = headerLayout.getLayoutParams().height;
    ViewCompat.setOnApplyWindowInsetsListener(
        headerLayout,
        new OnApplyWindowInsetsListener() {
          @Override
          public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
            Insets inset = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (originalHeaderHeight >= 0) {
              headerLayout.getLayoutParams().height = originalHeaderHeight + inset.top;
              headerLayout.setLayoutParams(headerLayout.getLayoutParams());
            }
            headerLayout.setPadding(
                originalPaddingLeft + inset.left,
                originalPaddingTop + inset.top,
                originalPaddingRight + inset.right,
                headerLayout.getPaddingBottom());
            return insets;
          }
        });
    edgeToEdgeEnabled = true;
  }

  private void updateTitle() {
    // Set up title text forcing single line for landscape text input mode due to space constraints.
    headerTitleTextView.setText(
        inputMode == INPUT_MODE_TEXT && isLandscape() ? singleLineTitleText : fullTitleText);
  }

  @VisibleForTesting
  void updateHeader(String headerText) {
    headerSelectionText.setContentDescription(getHeaderContentDescription());
    headerSelectionText.setText(headerText);
  }

  private String getHeaderContentDescription() {
    return getDateSelector().getSelectionContentDescription(requireContext());
  }

  private void startPickerFragment() {
    int themeResId = getThemeResId(requireContext());
    String currentTag =
        inputMode == INPUT_MODE_TEXT ? TEXT_INPUT_FRAGMENT_TAG : CALENDAR_FRAGMENT_TAG;

    Fragment foundFragment = getChildFragmentManager().findFragmentByTag(currentTag);
    @SuppressWarnings("unchecked")
    PickerFragment<S> fragment =
        foundFragment instanceof PickerFragment ? (PickerFragment<S>) foundFragment : null;

    if (fragment == null) {
      if (inputMode == INPUT_MODE_TEXT) {
        fragment =
            MaterialTextInputPicker.newInstance(getDateSelector(), themeResId, calendarConstraints);
      } else {
        calendar =
            MaterialCalendar.newInstance(
                getDateSelector(), themeResId, calendarConstraints, dayViewDecorator);
        fragment = calendar;
      }
    }

    pickerFragment = fragment;
    pickerFragment.addOnSelectionChangedListener(
        new OnSelectionChangedListener<S>() {
          @Override
          public void onSelectionChanged(S selection) {
            updateHeader(getHeaderText());
            confirmButton.setEnabled(getDateSelector().isSelectionComplete());
          }

          @Override
          public void onIncompleteSelectionChanged() {
            confirmButton.setEnabled(false);
          }
        });

    updateTitle();
    updateHeader(getHeaderText());

    getChildFragmentManager()
        .beginTransaction()
        .replace(R.id.mtrl_calendar_frame, pickerFragment, currentTag)
        .commitNow();
  }

  private void initHeaderToggle(Context context) {
    headerToggleButton.setTag(TOGGLE_BUTTON_TAG);
    headerToggleButton.setImageDrawable(createHeaderToggleDrawable(context));
    headerToggleButton.setChecked(inputMode != INPUT_MODE_CALENDAR);

    // By default, CheckableImageButton adds a delegate that reads checked state.
    // This information is not useful; we remove the delegate and use custom content descriptions.
    ViewCompat.setAccessibilityDelegate(headerToggleButton, null);
    updateToggleContentDescription(headerToggleButton);
    updateToggleTooltip(headerToggleButton);
    headerToggleButton.setOnClickListener(
        v -> {
          // Update confirm button in case in progress selection has been reset
          confirmButton.setEnabled(getDateSelector().isSelectionComplete());

          headerToggleButton.toggle();
          inputMode = (inputMode == INPUT_MODE_TEXT) ? INPUT_MODE_CALENDAR : INPUT_MODE_TEXT;
          updateToggleContentDescription(headerToggleButton);
          updateToggleTooltip(headerToggleButton);
          startPickerFragment();
        });
  }

  private void updateToggleContentDescription(@NonNull CheckableImageButton toggle) {
    String contentDescription =
        inputMode == INPUT_MODE_TEXT
            ? toggle.getContext().getString(R.string.mtrl_picker_toggle_to_calendar_input_mode)
            : toggle.getContext().getString(R.string.mtrl_picker_toggle_to_text_input_mode);
    headerToggleButton.setContentDescription(contentDescription);
  }

  private void updateToggleTooltip(@NonNull CheckableImageButton toggle) {
    String tooltipText =
        inputMode == INPUT_MODE_TEXT
            ? toggle
                .getContext()
                .getString(R.string.mtrl_picker_toggle_to_calendar_input_mode_tooltip)
            : toggle.getContext().getString(R.string.mtrl_picker_toggle_to_text_input_mode_tooltip);
    TooltipCompat.setTooltipText(headerToggleButton, tooltipText);
  }

  private DateSelector<S> getDateSelector() {
    if (dateSelector == null) {
      dateSelector = getArguments().getParcelable(DATE_SELECTOR_KEY);
    }
    return dateSelector;
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

  @Nullable
  private static CharSequence getFirstLineBySeparator(@Nullable CharSequence charSequence) {
    if (charSequence != null) {
      String[] lines = TextUtils.split(String.valueOf(charSequence), "\n");
      return lines.length > 1 ? lines[0] : charSequence;
    }
    return null;
  }

  private boolean isLandscape() {
    return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
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
    @Nullable DayViewDecorator dayViewDecorator;
    int titleTextResId = 0;
    CharSequence titleText = null;
    int positiveButtonTextResId = 0;
    CharSequence positiveButtonText = null;
    int positiveButtonContentDescriptionResId = 0;
    CharSequence positiveButtonContentDescription = null;
    int negativeButtonTextResId = 0;
    CharSequence negativeButtonText = null;
    int negativeButtonContentDescriptionResId = 0;
    CharSequence negativeButtonContentDescription = null;
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

    /**
     * Sets the formatter that will be used to input dates using a keyboard.
     *
     * <p>This affects the hint text and error suggestions of the date input field. Using this
     * setter requires caution to ensure dates are formatted properly in different languages and
     * locales.
     *
     * @param format a {@link SimpleDateFormat} used to format text input dates
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder<S> setTextInputFormat(@Nullable SimpleDateFormat format) {
      dateSelector.setTextInputFormat(format);
      return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public Builder<S> setSelection(S selection) {
      this.selection = selection;
      return this;
    }

    /** Sets the theme controlling fullscreen mode as well as other styles. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder<S> setTheme(@StyleRes int themeResId) {
      this.overrideThemeResId = themeResId;
      return this;
    }

    /** Sets the first, last, and starting month. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder<S> setCalendarConstraints(CalendarConstraints bounds) {
      this.calendarConstraints = bounds;
      return this;
    }

    /** Sets the {@link DayViewDecorator}. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder<S> setDayViewDecorator(@Nullable DayViewDecorator dayViewDecorator) {
      this.dayViewDecorator = dayViewDecorator;
      return this;
    }

    /**
     * Sets the text used to guide the user at the top of the picker. Defaults to a standard title
     * based upon the type of selection.
     */
    @NonNull
    @CanIgnoreReturnValue
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
    @CanIgnoreReturnValue
    public Builder<S> setTitleText(@Nullable CharSequence charSequence) {
      this.titleText = charSequence;
      this.titleTextResId = 0;
      return this;
    }

    /**
     * Sets the text used in the positive button
     *
     * @param textId resource id to be used as text in the positive button
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder<S> setPositiveButtonText(@StringRes int textId) {
      this.positiveButtonTextResId = textId;
      this.positiveButtonText = null;
      return this;
    }

    /**
     * Sets the text used in the positive button
     *
     * @param text text used in the positive button
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder<S> setPositiveButtonText(@Nullable CharSequence text) {
      this.positiveButtonText = text;
      this.positiveButtonTextResId = 0;
      return this;
    }

    /**
     * Sets the content description used in the positive button
     *
     * @param contentDescriptionId resource id to be used as content description in the positive
     *     button
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder<S> setPositiveButtonContentDescription(@StringRes int contentDescriptionId) {
      this.positiveButtonContentDescriptionResId = contentDescriptionId;
      this.positiveButtonContentDescription = null;
      return this;
    }

    /**
     * Sets the content description used in the positive button
     *
     * @param contentDescription content description used in the positive button
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder<S> setPositiveButtonContentDescription(
        @Nullable CharSequence contentDescription) {
      this.positiveButtonContentDescription = contentDescription;
      this.positiveButtonContentDescriptionResId = 0;
      return this;
    }

    /**
     * Sets the text used in the negative button
     *
     * @param textId resource id to be used as text in the negative button
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder<S> setNegativeButtonText(@StringRes int textId) {
      this.negativeButtonTextResId = textId;
      this.negativeButtonText = null;
      return this;
    }

    /**
     * Sets the text used in the negative button
     *
     * @param text text used in the negative button
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder<S> setNegativeButtonText(@Nullable CharSequence text) {
      this.negativeButtonText = text;
      this.negativeButtonTextResId = 0;
      return this;
    }

    /**
     * Sets the content description used in the negative button
     *
     * @param contentDescriptionId resource id to be used as content description in the negative
     *     button
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder<S> setNegativeButtonContentDescription(@StringRes int contentDescriptionId) {
      this.negativeButtonContentDescriptionResId = contentDescriptionId;
      this.negativeButtonContentDescription = null;
      return this;
    }

    /**
     * Sets the content description used in the negative button
     *
     * @param contentDescription content description used in the negative button
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder<S> setNegativeButtonContentDescription(
        @Nullable CharSequence contentDescription) {
      this.negativeButtonContentDescription = contentDescription;
      this.negativeButtonContentDescriptionResId = 0;
      return this;
    }

    /** Sets the input mode to start with. */
    @NonNull
    @CanIgnoreReturnValue
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
      if (!dateSelector.getSelectedDays().isEmpty()) {
        // Set the month to the first selected month in the selection
        Month firstSelectedMonth = Month.create(dateSelector.getSelectedDays().iterator().next());
        // Make sure the selection is in a valid month we can open to; otherwise use default openAt
        if (monthInValidRange(firstSelectedMonth, calendarConstraints)) {
          return firstSelectedMonth;
        }
      }

      Month thisMonth = Month.current();
      return monthInValidRange(thisMonth, calendarConstraints)
          ? thisMonth
          : calendarConstraints.getStart();
    }

    private static boolean monthInValidRange(Month month, CalendarConstraints constraints) {
      return month.compareTo(constraints.getStart()) >= 0
          && month.compareTo(constraints.getEnd()) <= 0;
    }
  }
}
