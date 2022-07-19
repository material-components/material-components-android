/*
 * Copyright 2021 The Android Open Source Project
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

package io.material.catalog.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.google.android.material.internal.ContextUtils;
import com.google.common.collect.ImmutableList;

/**
 * An interface to implement a customizable preference.
 */
public abstract class CatalogPreference {
  private static final String SHARED_PREFERENCES_NAME = "catalog.preferences";
  private static final int INVALID_OPTION_ID = 0;

  /**
   * The string resources ID of a human readable description of the preference when showing in the
   * preference settings screen.
   */
  @StringRes public final int description;

  private final String id = getClass().getSimpleName();

  protected CatalogPreference(int description) {
    this.description = description;
  }

  /**
   * Sets the selected option of the preference. The selected option ID will be saved to
   * {@link SharedPreferences} and the selected option will be applied.
   */
  public final void setSelectedOption(Context context, int optionId) {
    if (optionId == getSelectedOptionId(context)) {
      return;
    }
    for (Option option : getOptions()) {
      if (option.id == optionId) {
        getSharedPreferences(context).edit().putInt(id, optionId).apply();
        apply(context, option);
        if (shouldRecreateActivityOnOptionChanged()) {
          recreateActivityIfPossible(context);
        }
        return;
      }
    }
  }

  /**
   * Returns the currently selected option.
   */
  public final Option getSelectedOption(Context context) {
    int selectedOptionId = getSelectedOptionId(context);
    if (selectedOptionId != INVALID_OPTION_ID) {
      for (Option option : getOptions()) {
        if (option.id == selectedOptionId) {
          return option;
        }
      }
    }
    Option defaultOption = getDefaultOption();
    setSelectedOption(context, defaultOption.id);
    return defaultOption;
  }

  /**
   * Applies the currently selected option.
   */
  public final void apply(@NonNull Context context) {
    apply(context, getSelectedOption(context));
  }

  /**
   * Applies the selected option to take effect on the app.
   */
  protected abstract void apply(@NonNull Context context, @NonNull Option selectedOption);

  private int getSelectedOptionId(Context context) {
    return getSharedPreferences(context).getInt(id, INVALID_OPTION_ID);
  }

  private SharedPreferences getSharedPreferences(Context context) {
    return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
  }

  @SuppressWarnings("RestrictTo") // To use ContextUtils
  private void recreateActivityIfPossible(Context context) {
    Activity activity = ContextUtils.getActivity(context);
    if (activity != null) {
      activity.recreate();
    }
  }

  /**
   * Override this method and return {@code false} when the preferences settings is not changeable.
   */
  protected boolean isEnabled() {
    return true;
  }

  /**
   * Override this method and return {@code true} if the current activity should be restarted after
   * the selected option is changed.
   */
  protected boolean shouldRecreateActivityOnOptionChanged() {
    return false;
  }

  /**
   * Returns all available options of the preference.
   */
  @NonNull
  protected abstract ImmutableList<Option> getOptions();

  /**
   * Returns the default option.
   */
  @NonNull
  protected abstract Option getDefaultOption();

  /**
   * A preference option that can be selected.
   */
  protected static class Option {

    /**
     * ID of the option. Will be used as the saved value in {@link SharedPreferences}.
     */
    public final int id;

    /**
     * The drawable resource ID of the icon of the option to show in the preferences screen.
     */
    @DrawableRes
    public final int icon;

    /**
     * The string resource ID of the human readable description of the option to show in the
     * preferences screen.
     */
    @StringRes
    public final int description;

    public Option(int id, @DrawableRes int icon, @StringRes int description) {
      this.id = id;
      this.icon = icon;
      this.description = description;
    }
  }
}
