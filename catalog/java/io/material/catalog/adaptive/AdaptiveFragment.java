/*
 * Copyright 2021 The Android Open Source Project
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

package io.material.catalog.adaptive;

import io.material.catalog.R;

import android.content.Intent;
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

/** A landing fragment that links to Adaptive demos for the Catalog app. */
public class AdaptiveFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_adaptive_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_adaptive_description;
  }

  @Override
  @NonNull
  public Demo getMainDemo() {
    return new Demo(R.string.cat_list_view_title) {
      @Override
      public Intent createActivityIntent() {
        return new Intent(getContext(), AdaptiveListViewDemoActivity.class);
      }
    };
  }

  @Override
  @NonNull
  public List<Demo> getAdditionalDemos() {
    List<Demo> additionalDemos = new ArrayList<>();
    additionalDemos.add(
        new Demo(R.string.cat_feed_title) {
          @Override
          public Intent createActivityIntent() {
            return new Intent(getContext(), AdaptiveFeedDemoActivity.class);
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_hero_title) {
          @Override
          public Intent createActivityIntent() {
            return new Intent(getContext(), AdaptiveHeroDemoActivity.class);
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_supporting_panel_title) {
          @Override
          public Intent createActivityIntent() {
            return new Intent(getContext(), AdaptiveSupportingPanelDemoActivity.class);
          }
        });
    return additionalDemos;
  }

  /** The Dagger module for {@link AdaptiveFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {

    @FragmentScope
    @ContributesAndroidInjector
    abstract AdaptiveFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(R.string.cat_adaptive_title, R.drawable.ic_side_drawer) {
        @Override
        public Fragment createFragment() {
          return new AdaptiveFragment();
        }
      };
    }
  }
}
