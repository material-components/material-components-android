/*
 * Copyright 2019 The Android Open Source Project
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

package io.material.catalog.listitem;

import io.material.catalog.R;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
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

/** A landing fragment that links to List demos for the Catalog app. */
public class ListsFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_lists_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_lists_description;
  }

  @Override
  @NonNull
  public Demo getMainDemo() {
    return new Demo() {
      @Override
      public Fragment createFragment() {
        return new ListsMainDemoFragment();
      }
    };
  }

  @Override
  @NonNull
  public List<Demo> getAdditionalDemos() {
    List<Demo> additionalDemos = new ArrayList<>();
    additionalDemos.add(
        new Demo(R.string.cat_lists_segmented_demo_title) {
          @Override
          public Fragment createFragment() {
            return new SegmentedListDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_lists_multi_section_demo_title) {
          @Override
          public Fragment createFragment() {
            return new MultiSectionListDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_lists_expandable_demo_title) {
          @Override
          public Fragment createFragment() {
            return new ExpandableListDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_lists_swipe_demo_title) {
          @Override
          public Fragment createFragment() {
            return new SwipeableListDemoFragment();
          }
        });
    return additionalDemos;
  }

  /** The Dagger module for {@link ListsFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {

    @FragmentScope
    @ContributesAndroidInjector
    abstract ListsFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(R.string.cat_lists_title, R.drawable.ic_lists) {
        @Override
        public Fragment createFragment() {
          return new ListsFragment();
        }
      };
    }
  }
}
