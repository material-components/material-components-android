/*
 * Copyright 2022 The Android Open Source Project
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

package io.material.catalog.search;

import io.material.catalog.R;

import android.content.Intent;
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

/** A landing fragment that links to Open Search Bar demos for the Catalog app. */
public class SearchFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_searchbar_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_searchbar_description;
  }

  @Override
  @NonNull
  public Demo getMainDemo() {
    return new Demo() {
      @Nullable
      @Override
      public Intent createActivityIntent() {
        return new Intent(getContext(), SearchMainDemoActivity.class);
      }
    };
  }

  @Override
  @NonNull
  public List<Demo> getAdditionalDemos() {
    List<Demo> additionalDemos = new ArrayList<>();
    additionalDemos.add(
        new Demo(R.string.cat_searchbar_recycler_title) {
          @Override
          public Intent createActivityIntent() {
            return new Intent(getContext(), SearchRecyclerDemoActivity.class);
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_searchbar_appbar_with_icons_title) {
          @Override
          public Intent createActivityIntent() {
            return new Intent(getContext(), SearchBarWithAppBarIconsDemoActivity.class);
          }
        });
    return additionalDemos;
  }

  /** The Dagger module for {@link SearchFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {

    @FragmentScope
    @ContributesAndroidInjector
    abstract SearchFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(R.string.cat_searchbar_title, R.drawable.ic_search_bar) {
        @Override
        public Fragment createFragment() {
          return new SearchFragment();
        }
      };
    }
  }
}
