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
import android.os.Bundle;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.StyleRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * A {@link Dialog} with a header, {@link MaterialCalendar}, and set of actions.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public abstract class MaterialPickerDialogFragment<S> extends DialogFragment {

  private static final String THEME_RESOURCE_ID_KEY = "themeResId";

  /**
   * Returns the text to display at the top of the {@link DialogFragment}
   *
   * <p>The text is updated when the Dialog launches and on user clicks.
   */
  protected abstract String getHeaderText();

  /**
   * Creates a new instance of {@link MaterialCalendar}
   *
   * <p>The {@link MaterialCalendar} is rebuilt in every call to onCreate.
   */
  protected abstract MaterialCalendar<? extends S> createMaterialCalendar();

  /** Returns an {@link @AttrRes} to apply as a theme overlay to the DialogFragment */
  protected abstract int getDefaultThemeAttr();

  private MaterialCalendar<? extends S> materialCalendar;
  private SimpleDateFormat simpleDateFormat;

  @AttrRes private int themeResId;
  private TextView header;
  private S selection;

  /**
   * Adds the super class required arguments to the Bundle.
   *
   * <p>Call this method in subclasses before the initial call to {@link
   * DialogFragment#setArguments(Bundle)}
   *
   * @param args The Bundle from the subclassing DialogFragment
   * @param themeResId 0 or a {@link StyleRes} representing a ThemeOverlay
   */
  protected static void addThemeToBundle(Bundle args, int themeResId) {
    args.putInt(THEME_RESOURCE_ID_KEY, themeResId);
  }

  @StyleRes
  private static int getThemeResource(Context context, int defaultThemeAttr, int themeResId) {
    if (themeResId != 0) {
      return themeResId;
    }
    TypedValue outValue = new TypedValue();
    context.getTheme().resolveAttribute(defaultThemeAttr, outValue, true);
    return outValue.resourceId;
  }

  @Override
  public final void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    simpleDateFormat =
        new SimpleDateFormat(
            getResources().getString(R.string.mtrl_picker_date_format), Locale.getDefault());
    themeResId =
        getThemeResource(
            getContext(), getDefaultThemeAttr(), getArguments().getInt(THEME_RESOURCE_ID_KEY));
    materialCalendar = createMaterialCalendar();
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
    header = root.findViewById(R.id.date_picker_header_title);

    root.findViewById(R.id.confirm_button)
        .setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                selection = materialCalendar.getSelection();
                dismiss();
              }
            });
    root.findViewById(R.id.cancel_button)
        .setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                selection = null;
                dismiss();
              }
            });
    return root;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    super.onViewCreated(view, bundle);
    FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.calendar_frame, materialCalendar);
    fragmentTransaction.commit();
  }

  @Override
  public void onStart() {
    super.onStart();
    materialCalendar
        .getView()
        .setOnClickListener(
            new OnClickListener() {
              @Override
              public void onClick(View view) {
                updateHeader();
              }
            });
    updateHeader();
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
    return selection;
  }

  /**
   * Sets a date formatter.
   *
   * <p>Useful when the default localized date format is inadequate
   */
  public final void setSimpleDateFormat(SimpleDateFormat simpleDateFormat) {
    this.simpleDateFormat = simpleDateFormat;
  }

  /** Returns a localized date formatter */
  public final SimpleDateFormat getSimpleDateFormat() {
    return simpleDateFormat;
  }

  /**
   * Returns the {@link MaterialCalendar} from a previous call to {@link
   * MaterialPickerDialogFragment#createMaterialCalendar}
   *
   * <p>Returns null until after {@link DialogFragment#onCreate}
   */
  @Nullable
  public final MaterialCalendar<? extends S> getMaterialCalendar() {
    return materialCalendar;
  }

  private void updateHeader() {
    header.setText(getHeaderText());
  }
}
