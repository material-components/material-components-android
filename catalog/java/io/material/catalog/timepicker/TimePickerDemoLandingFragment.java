/*
 * Copyright 2020 The Android Open Source Project
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
package io.material.catalog.timepicker;

import io.material.catalog.R;

import androidx.fragment.app.Fragment;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoSet;
import io.material.catalog.application.scope.ActivityScope;
import io.material.catalog.application.scope.FragmentScope;
import io.material.catalog.feature.Demo;
import io.material.catalog.feature.DemoLandingFragment;
import io.material.catalog.feature.FeatureDemo;

/** A landing fragment that links to Picker demos for the Catalog app. */
public class TimePickerDemoLandingFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_time_picker_demo_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_time_picker_description;
  }

  @Override
  public Demo getMainDemo() {
    return new Demo() {
      @Override
      public Fragment createFragment() {
        return new TimePickerMainDemoFragment();
      }
    };
  }

  /** The Dagger module for {@link TimePickerDemoLandingFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {

    @FragmentScope
    @ContributesAndroidInjector
    abstract TimePickerDemoLandingFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(R.string.cat_time_picker_demo_title, R.drawable.ic_placeholder) {
        @Override
        public Fragment createFragment() {
          return new TimePickerDemoLandingFragment();
        }
      };
    }
  }
}
