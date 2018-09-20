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

package io.material.catalog.tableofcontents;

import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import io.material.catalog.application.scope.FragmentScope;
import io.material.catalog.bottomappbar.BottomAppBarFragment;
import io.material.catalog.bottomnav.BottomNavigationFragment;
import io.material.catalog.button.ButtonsFragment;
import io.material.catalog.card.CardFragment;
import io.material.catalog.checkbox.CheckBoxFragment;
import io.material.catalog.chip.ChipFragment;
import io.material.catalog.dialog.DialogDemoLandingFragment;
import io.material.catalog.fab.FabFragment;
import io.material.catalog.font.FontFragment;
import io.material.catalog.radiobutton.RadioButtonFragment;
import io.material.catalog.switchmaterial.SwitchFragment;
import io.material.catalog.tabs.TabsFragment;
import io.material.catalog.textfield.TextFieldFragment;
import io.material.catalog.themeswitcher.ThemeSwitcherDialogFragment;
import io.material.catalog.themeswitcher.ThemeSwitcherResourceProvider;
import io.material.catalog.topappbar.TopAppBarFragment;
import io.material.catalog.transformation.TransformationFragment;

/**
 * The Dagger module for {@link TocFragment} dependencies.
 *
 */
@dagger.Module(
    includes = {
      BottomAppBarFragment.Module.class,
      ButtonsFragment.Module.class,
      BottomNavigationFragment.Module.class,
      CardFragment.Module.class,
      CheckBoxFragment.Module.class,
      ChipFragment.Module.class,
      DialogDemoLandingFragment.Module.class,
      FabFragment.Module.class,
      FontFragment.Module.class,
      RadioButtonFragment.Module.class,
      SwitchFragment.Module.class,
      TabsFragment.Module.class,
      TextFieldFragment.Module.class,
      TopAppBarFragment.Module.class,
      TransformationFragment.Module.class,
    })
public abstract class TocModule {
  @FragmentScope
  @ContributesAndroidInjector
  abstract TocFragment contributeTocFragment();

  @Provides
  static TocResourceProvider provideTocResourceProvider() {
    return new TocResourceProvider();
  }

  @FragmentScope
  @ContributesAndroidInjector
  abstract ThemeSwitcherDialogFragment contributeThemeSwitcherDialogFragment();

  @Provides
  static ThemeSwitcherResourceProvider provideThemeSwitcherResourceProvider() {
    return new ThemeSwitcherResourceProvider();
  }
}
