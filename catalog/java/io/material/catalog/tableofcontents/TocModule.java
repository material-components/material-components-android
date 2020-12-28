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
import io.material.catalog.bottomsheet.BottomSheetFragment;
import io.material.catalog.button.ButtonsFragment;
import io.material.catalog.card.CardFragment;
import io.material.catalog.checkbox.CheckBoxFragment;
import io.material.catalog.chip.ChipFragment;
import io.material.catalog.datepicker.DatePickerDemoLandingFragment;
import io.material.catalog.dialog.DialogDemoLandingFragment;
import io.material.catalog.elevation.ElevationFragment;
import io.material.catalog.fab.FabFragment;
import io.material.catalog.font.FontFragment;
import io.material.catalog.imageview.ShapeableImageViewFragment;
import io.material.catalog.menu.MenuFragment;
import io.material.catalog.progressindicator.ProgressIndicatorFragment;
import io.material.catalog.radiobutton.RadioButtonFragment;
import io.material.catalog.shapetheming.ShapeThemingFragment;
import io.material.catalog.slider.SliderFragment;
import io.material.catalog.switchmaterial.SwitchFragment;
import io.material.catalog.tabs.TabsFragment;
import io.material.catalog.textfield.TextFieldFragment;
import io.material.catalog.themeswitcher.ThemeSwitcherDialogFragment;
import io.material.catalog.themeswitcher.ThemeSwitcherResourceProvider;
import io.material.catalog.timepicker.TimePickerDemoLandingFragment;
import io.material.catalog.topappbar.TopAppBarFragment;
import io.material.catalog.transition.TransitionFragment;

/** The Dagger module for {@link TocFragment} dependencies. */
@dagger.Module(
    includes = {
      BottomAppBarFragment.Module.class,
      ButtonsFragment.Module.class,
      BottomNavigationFragment.Module.class,
      BottomSheetFragment.Module.class,
      CardFragment.Module.class,
      CheckBoxFragment.Module.class,
      ChipFragment.Module.class,
      DatePickerDemoLandingFragment.Module.class,
      DialogDemoLandingFragment.Module.class,
      ElevationFragment.Module.class,
      FabFragment.Module.class,
      FontFragment.Module.class,
      MenuFragment.Module.class,
      ProgressIndicatorFragment.Module.class,
      RadioButtonFragment.Module.class,
      ShapeableImageViewFragment.Module.class,
      ShapeThemingFragment.Module.class,
      SliderFragment.Module.class,
      SwitchFragment.Module.class,
      TabsFragment.Module.class,
      TextFieldFragment.Module.class,
      TimePickerDemoLandingFragment.Module.class,
      TopAppBarFragment.Module.class,
      TransitionFragment.Module.class
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
