/*
 * Copyright 2018 The Android Open Source Project
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

import io.material.catalog.R;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.annotation.NonNull;

/** Helper class for demos to support theme switcher functionality. */
public class CatalogPreferencesHelper {
  private final FragmentManager fragmentManager;
  private final boolean enabled;

  public <F extends Fragment & PreferencesFragment> CatalogPreferencesHelper(@NonNull F fragment) {
    fragmentManager = fragment.getParentFragmentManager();
    enabled =
        fragment.shouldShowDefaultDemoActionBar()
            && fragment.getActivity() instanceof BaseCatalogActivity
            && ((BaseCatalogActivity) fragment.getActivity()).isPreferencesEnabled();

    if (enabled) {
      fragment.setHasOptionsMenu(true);
    }
  }

  public CatalogPreferencesHelper(@NonNull FragmentManager fragmentManager) {
    this.fragmentManager = fragmentManager;
    this.enabled = true;
  }

  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
    if (enabled) {
      menuInflater.inflate(R.menu.mtrl_preferences_menu, menu);
    }
  }

  public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
    if (enabled) {
      if (menuItem.getItemId() == R.id.preferences) {
        showPreferences();
        return true;
      }
    }
    return false;
  }

  private void showPreferences() {
    new CatalogPreferencesDialogFragment().show(fragmentManager, "preferences");
  }

  /**
   * Implement this interface to allow a Fragment to be used with {@link CatalogPreferencesHelper}.
   */
  public interface PreferencesFragment {

    /**
     * If this is true, then the demo's default action bar comes with preferences screen support.
     */
    boolean shouldShowDefaultDemoActionBar();
  }
}
