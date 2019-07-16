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

package io.material.catalog.radiobutton;

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

/** A fragment that displays radio button demos for the Catalog app. */
public class RadioButtonFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_radiobutton_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_radiobutton_description;
  }

  @Override
  public Demo getMainDemo() {
    return new Demo() {
      @Override
      public Fragment createFragment() {
        return new RadioButtonMainDemoFragment();
      }
    };
  }

  /** The Dagger module for {@link RadioButtonFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {

    @FragmentScope
    @ContributesAndroidInjector
    abstract RadioButtonFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(R.string.cat_radiobutton_title, R.drawable.ic_radiobutton) {
        @Override
        public Fragment createFragment() {
          return new RadioButtonFragment();
        }
      };
    }
  }
}
