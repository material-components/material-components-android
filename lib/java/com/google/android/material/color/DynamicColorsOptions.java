/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.color;

import android.app.Activity;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import com.google.android.material.color.DynamicColors.OnAppliedCallback;
import com.google.android.material.color.DynamicColors.Precondition;

/** Wrapper class for specifying dynamic colors options when applying dynamic colors. */
public class DynamicColorsOptions {

  private static final Precondition ALWAYS_ALLOW =
      new Precondition() {
        @Override
        public boolean shouldApplyDynamicColors(@NonNull Activity activity, int theme) {
          return true;
        }
      };

  private static final OnAppliedCallback NO_OP_CALLBACK =
      new OnAppliedCallback() {
        @Override
        public void onApplied(@NonNull Activity activity) {}
      };

  @Nullable private final Application application;
  @Nullable private final Activity activity;
  @StyleRes private final int themeOverlay;
  @NonNull private final Precondition precondition;
  @NonNull private final OnAppliedCallback onAppliedCallback;

  private DynamicColorsOptions(Builder builder) {
    this.application = builder.application;
    this.activity = builder.activity;
    this.themeOverlay = builder.themeOverlay;
    this.precondition = builder.precondition;
    this.onAppliedCallback = builder.onAppliedCallback;
  }

  /** Returns the application where dynamic color is applied. */
  @Nullable
  public Application getApplication() {
    return application;
  }

  /** Returns the activity where dynamic color is applied. */
  @Nullable
  public Activity getActivity() {
    return activity;
  }

  /** Returns the resource ID of the theme overlay that provides dynamic color definition. */
  @StyleRes
  public int getThemeOverlay() {
    return themeOverlay;
  }

  /** Returns the precondition that decides if dynamic colors should be applied. */
  @NonNull
  public Precondition getPrecondition() {
    return precondition;
  }

  /** Returns the callback method after dynamic colors have been applied. */
  @NonNull
  public OnAppliedCallback getOnAppliedCallback() {
    return onAppliedCallback;
  }

  /**
   * Builder class for specifying options when applying dynamic colors. When building {@code
   * DynamicColorOptions}, either an {@link Application} or {@link Activity} is required. When
   * {@link Application} is specified, dynamic colors will be applied to all activities in the
   * application. For example:
   * </pre>
   * DynamicColorOptions dynamicColorOptions =
   *    new DynamicColorsOptions.Builder(application)
   *        .setThemeOverlay(themeOverlay)
   *        .setPrecondition(precondition)
   *        .setOnAppliedCallback(onAppliedCallback)
   *        .build()
   * </pre>
   */
  public static class Builder {

    @Nullable private final Application application;
    @Nullable private final Activity activity;
    @StyleRes private int themeOverlay;
    @NonNull private Precondition precondition = ALWAYS_ALLOW;
    @NonNull private OnAppliedCallback onAppliedCallback = NO_OP_CALLBACK;

    public Builder(@NonNull Application application) {
      this.application = application;
      this.activity = null;
    }

    public Builder(@NonNull Activity activity) {
      this.activity = activity;
      this.application = null;
    }

    /** Sets the resource ID of the theme overlay that provides dynamic color definition. */
    @NonNull
    public Builder setThemeOverlay(@StyleRes int themeOverlay) {
      this.themeOverlay = themeOverlay;
      return this;
    }

    /** Sets the precondition that decides if dynamic colors should be applied. */
    @NonNull
    public Builder setPrecondition(@NonNull Precondition precondition) {
      this.precondition = precondition;
      return this;
    }

    /** Sets the callback method for after the dynamic colors have been applied. */
    @NonNull
    public Builder setOnAppliedCallback(@NonNull OnAppliedCallback onAppliedCallback) {
      this.onAppliedCallback = onAppliedCallback;
      return this;
    }

    @NonNull
    public DynamicColorsOptions build() {
      return new DynamicColorsOptions(this);
    }
  }
}
