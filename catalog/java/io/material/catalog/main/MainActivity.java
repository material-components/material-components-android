/*
 * Copyright 2017 The Android Open Source Project
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

package io.material.catalog.main;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.google.common.base.Optional;
import dagger.BindsOptionalOf;
import dagger.android.ContributesAndroidInjector;
import io.material.catalog.application.scope.ActivityScope;
import io.material.catalog.feature.FeatureDemoUtils;
import io.material.catalog.internal.InternalOptionsMenuPresenter;
import io.material.catalog.preferences.BaseCatalogActivity;
import io.material.catalog.preferences.ThemeOverlayUtils;
import io.material.catalog.tableofcontents.TocFragment;
import io.material.catalog.tableofcontents.TocModule;
import io.material.catalog.windowpreferences.WindowPreferencesManager;
import javax.inject.Inject;

/**
 * The main launcher activity for the Catalog, capable of displaying a number of different screens
 * via Fragments.
 */
public class MainActivity extends BaseCatalogActivity {

  @Inject Optional<InternalOptionsMenuPresenter> internalOptionsMenu;
  TocFragment tocFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    ThemeOverlayUtils.applyThemeOverlays(this);
    super.onCreate(savedInstanceState);
    WindowPreferencesManager windowPreferencesManager = new WindowPreferencesManager(this);
    windowPreferencesManager.applyEdgeToEdgePreference(getWindow());

    setContentView(R.layout.cat_main_activity);

    if (savedInstanceState == null) {
      tocFragment = new TocFragment();
      getSupportFragmentManager().beginTransaction().add(R.id.container, tocFragment).commit();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    if (internalOptionsMenu.isPresent()) {
      MenuInflater inflater = getMenuInflater();
      internalOptionsMenu.get().onCreateOptionsMenu(menu, inflater);
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Let the fragment handle options first
    Fragment currentFragment = FeatureDemoUtils.getCurrentFragment(this);
    if (currentFragment != null && currentFragment.onOptionsItemSelected(item)) {
      return true;
    }

    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }

    if (internalOptionsMenu.isPresent()) {
      internalOptionsMenu.get().onOptionsItemSelected(item);
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean isPreferencesEnabled() {
    return true;
  }

  /** The Dagger module for {@link MainActivity} dependencies */
  @dagger.Module
  public abstract static class Module {
    @ActivityScope
    @ContributesAndroidInjector(modules = {TocModule.class})
    abstract MainActivity contributeMainActivity();

    @BindsOptionalOf
    abstract InternalOptionsMenuPresenter optionalInternalOptionsMenu();
  }
}
