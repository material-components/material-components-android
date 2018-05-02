/*
 * Copyright 2017 The Android Open Source Project
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

package io.material.catalog.topappbar;

import io.material.catalog.R;

import android.support.v4.app.Fragment;
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

/** A landing fragment that links to Top App Bar demos for the Catalog app. */
public class TopAppBarFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_topappbar_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_topappbar_description;
  }

  @Override
  public Demo getMainDemo() {
    return new Demo() {
      @Override
      public Fragment createFragment() {
        return new TopAppBarMainDemoFragment();
      }
    };
  }

  @Override
  public List<Demo> getAdditionalDemos() {
    List<Demo> additionalDemos = new ArrayList<>();
    additionalDemos.add(
        new Demo(R.string.cat_topappbar_scrolling_title) {
          @Override
          public Fragment createFragment() {
            return new TopAppBarScrollingDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_topappbar_collapsing_title) {
          @Override
          public Fragment createFragment() {
            return new TopAppBarCollapsingDemoFragment();
          }
        });
    return additionalDemos;
  }

  /** The Dagger module for {@link TopAppBarFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {

    @FragmentScope
    @ContributesAndroidInjector
    abstract TopAppBarFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(R.string.cat_topappbar_title, R.drawable.ic_topappbar_24px) {
        @Override
        public Fragment createFragment() {
          return new TopAppBarFragment();
        }
      };
    }
  }
}
