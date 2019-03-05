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
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.StyleRes;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * A {@link Dialog} with a header, {@link MaterialCalendarView}, and set of actions.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public abstract class MaterialPickerDialog<S> extends Dialog {

  private SimpleDateFormat simpleDateFormat =
      new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

  private TextView header;
  private S selection;
  private MaterialCalendarView<? extends S> materialCalendarView;

  @StyleRes
  protected static final int getThemeResource(
      Context context, int defaultThemeAttr, int themeResId) {
    if (themeResId != 0) {
      return themeResId;
    }
    TypedValue outValue = new TypedValue();
    context.getTheme().resolveAttribute(defaultThemeAttr, outValue, true);
    return outValue.resourceId;
  }

  public MaterialPickerDialog(Context context) {
    this(context, 0);
  }

  public MaterialPickerDialog(Context context, int themeResId) {
    super(context, themeResId);
  }

  /**
   * Returns a {@link S} instance representing the selection or null if the user has not confirmed a
   * selection.
   */
  @Nullable
  public final S getSelection() {
    return selection;
  }

  @Override
  protected final void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    materialCalendarView = getMaterialCalendarView();

    setContentView(R.layout.date_picker_dialog);
    header = findViewById(R.id.date_picker_header_title);
    FrameLayout calendarViewFrame = findViewById(R.id.date_picker_calendar_view_frame);
    calendarViewFrame.addView(materialCalendarView);
    materialCalendarView.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            updateHeader();
          }
        });

    Button confirmButton = findViewById(R.id.confirm_button);
    Button cancelButton = findViewById(R.id.cancel_button);

    confirmButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            selection = materialCalendarView.getSelection();
            dismiss();
          }
        });
    cancelButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            selection = null;
            cancel();
          }
        });
    updateHeader();
  }

  private void updateHeader() {
    header.setText(getHeaderText());
  }

  public final void setSimpleDateFormat(SimpleDateFormat simpleDateFormat) {
    this.simpleDateFormat = simpleDateFormat;
  }

  public final SimpleDateFormat getSimpleDateFormat() {
    return simpleDateFormat;
  }

  protected abstract String getHeaderText();

  protected abstract MaterialCalendarView<? extends S> getMaterialCalendarView();
}
