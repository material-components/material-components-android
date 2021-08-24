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

package io.material.catalog.bottomnav;

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
import java.util.ArrayList;
import java.util.List;

/** A landing fragment that links to bottom nav demos for the Catalog app. */
public class BottomNavigationFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_bottom_nav_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_bottom_nav_description;
  }

  @Override
  public Demo getMainDemo() {
    return new Demo() {
      @Override
      public Fragment createFragment() {
        return new BottomNavigationMainDemoFragment();
      }
    };
  }

  @Override
  public List<Demo> getAdditionalDemos() {
    List<Demo> additionalDemos = new ArrayList<>();
    additionalDemos.add(
        new Demo(R.string.cat_bottom_nav_label_visibility_demo_title) {
          @Override
          public Fragment createFragment() {
            return new BottomNavigationLabelVisibilityDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_bottom_nav_animated_demo_title) {
          @Override
          public Fragment createFragment() {
            return new BottomNavigationAnimatedDemoFragment();
          }
        });
    return additionalDemos;
  }

  /** The Dagger module for {@link BottomNavigationFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {
    @FragmentScope
    @ContributesAndroidInjector
    abstract BottomNavigationFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(R.string.cat_bottom_nav_title, R.drawable.ic_bottomnavigation) {
        @Override
        public Fragment createFragment() {
          return new BottomNavigationFragment();
        }
      };
    }
  }
}
