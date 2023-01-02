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

package io.material.catalog.menu;

import io.material.catalog.R;

import androidx.fragment.app.Fragment;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoSet;
import io.material.catalog.application.scope.ActivityScope;
import io.material.catalog.application.scope.FragmentScope;
import io.material.catalog.feature.Demo;
import io.material.catalog.feature.DemoLandingFragment;
import io.material.catalog.feature.FeatureDemo;

/** A landing fragment that links to menus demos for the Catalog app. */
public class MenuFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_menus_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_menus_description;
  }


  @LayoutRes
  public int getAdapterItemLayout() {
    return R.layout.cat_popup_item;
  }

  @Override
  public Demo getMainDemo() {
    return new Demo() {
      @Nullable
      @Override
      public Fragment createFragment() {
        MenuMainDemoFragment menuMainDemoFragment = new MenuMainDemoFragment();
        menuMainDemoFragment.setPopupItemLayoutRes(getAdapterItemLayout());
        return menuMainDemoFragment;
      }
    };
  }

  /** The Dagger module for {@link MenuFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {

    @FragmentScope
    @ContributesAndroidInjector
    abstract MenuFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(R.string.cat_menus_title, R.drawable.ic_menu) {
        @Override
        public Fragment createFragment() {
          return new MenuFragment();
        }
      };
    }
  }
}
