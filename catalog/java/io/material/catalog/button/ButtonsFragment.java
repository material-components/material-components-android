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

package io.material.catalog.button;

import io.material.catalog.R;

import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoSet;
import io.material.catalog.application.scope.ActivityScope;
import io.material.catalog.application.scope.FragmentScope;
import io.material.catalog.feature.Demo;
import io.material.catalog.feature.DemoLandingFragment;
import io.material.catalog.feature.FeatureDemo;
import java.util.Arrays;
import java.util.List;

/** A landing fragment that links to button demos for the Catalog app. */
public class ButtonsFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_buttons_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_buttons_description;
  }

  @Override
  public Demo getMainDemo() {
    return new Demo() {
      @Override
      public Fragment createFragment() {
        return new ButtonsMainDemoFragment();
      }
    };
  }

  @Override
  public List<Demo> getAdditionalDemos() {
    return Arrays.asList(
        new Demo(R.string.cat_buttons_toggle_group) {
          @Nullable
          @Override
          public Fragment createFragment() {
            return new ButtonToggleGroupDemoFragment();
          }
        });
  }

  /** The Dagger module for {@link ButtonsFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {

    @FragmentScope
    @ContributesAndroidInjector
    abstract ButtonsFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(R.string.cat_buttons_title, R.drawable.ic_button) {
        @Override
        public Fragment createFragment() {
          return new ButtonsFragment();
        }
      };
    }
  }
}
