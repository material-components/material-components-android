/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.divider;

import io.material.catalog.R;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoSet;
import io.material.catalog.application.scope.ActivityScope;
import io.material.catalog.application.scope.FragmentScope;
import io.material.catalog.feature.Demo;
import io.material.catalog.feature.DemoLandingFragment;
import io.material.catalog.feature.FeatureDemo;
import java.util.ArrayList;
import java.util.List;

/**
 * A landing fragment that links to demos of the divider.
 */
public class DividerFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_divider_demo_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_divider_description;
  }

  @NonNull
  @Override
  public Demo getMainDemo() {
    return new Demo() {
      @Override
      public Fragment createFragment() {
        return new DividerMainDemoFragment();
      }
    };
  }

  @Override
  @NonNull
  public List<Demo> getAdditionalDemos() {
    List<Demo> additionalDemos = new ArrayList<>();
    additionalDemos.add(
        new Demo(R.string.cat_divider_item_decoration_demo_title) {
          @Nullable
          @Override
          public Fragment createFragment() {
            return new DividerItemDecorationDemoFragment();
          }
        }
    );
    return additionalDemos;
  }

  /** The Dagger module for {@link DividerFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {

    @FragmentScope
    @ContributesAndroidInjector
    abstract DividerFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(R.string.cat_divider_demo_title, R.drawable.ic_placeholder) {
        @Override
        public Fragment createFragment() {
          return new DividerFragment();
        }
      };
    }
  }
}
