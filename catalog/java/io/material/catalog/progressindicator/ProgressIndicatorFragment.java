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
package io.material.catalog.progressindicator;

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

/** A fragment that displays progress indicator demos for the Catalog app. */
public class ProgressIndicatorFragment extends DemoLandingFragment {
  @Override
  public int getTitleResId() {
    return R.string.cat_progress_indicator_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_progress_indicator_description;
  }

  @Override
  @NonNull
  public Demo getMainDemo() {
    return new Demo() {
      @Override
      public Fragment createFragment() {
        return new ProgressIndicatorMainDemoFragment();
      }
    };
  }

  @NonNull
  public List<Demo> getSharedAdditionalDemos() {
    List<Demo> additionalDemos = new ArrayList<>();
    additionalDemos.add(
        new Demo(R.string.cat_progress_indicator_visibility_demo_title) {
          @Nullable
          @Override
          public Fragment createFragment() {
            return new ProgressIndicatorVisibilityDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_progress_indicator_demo_standalone_title) {
          @Nullable
          @Override
          public Fragment createFragment() {
            return new ProgressIndicatorStandaloneDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_progress_indicator_wave_demo_title) {
          @Nullable
          @Override
          public Fragment createFragment() {
            return new ProgressIndicatorWaveDemoFragment();
          }
        });
    return additionalDemos;
  }

  @Override
  @NonNull
  public List<Demo> getAdditionalDemos() {
    List<Demo> additionalDemos = getSharedAdditionalDemos();
    additionalDemos.add(
        new Demo(R.string.cat_progress_indicator_multi_color_demo_title) {
          @Nullable
          @Override
          public Fragment createFragment() {
            return new ProgressIndicatorMultiColorDemoFragment();
          }
        });
    return additionalDemos;
  }

  /** The Dagger module for {@link ProgressIndicatorFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {
    @FragmentScope
    @ContributesAndroidInjector
    abstract ProgressIndicatorFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(
          R.string.cat_progress_indicator_title, R.drawable.ic_progress_activity_24px) {
        @Override
        public Fragment createFragment() {
          return new ProgressIndicatorFragment();
        }
      };
    }
  }
}
