/*
 * Copyright 2022 The Android Open Source Project
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

package io.material.catalog.carousel;

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
import java.util.Arrays;
import java.util.List;

/** A landing fragment that links to Carousel demos for the Catalog app. */
public class CarouselFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_carousel_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_carousel_description;
  }

  @NonNull
  @Override
  public Demo getMainDemo() {
    return new Demo() {
      @Nullable
      @Override
      public Fragment createFragment() {
        return new CarouselMainDemoFragment();
      }
    };
  }

  @NonNull
  @Override
  public List<Demo> getAdditionalDemos() {
    return Arrays.asList(
        new Demo(R.string.cat_carousel_multi_browse_demo_title) {
          @Override
          public Fragment createFragment() {
            return new MultiBrowseCarouselDemoFragment();
          }
        },
        new Demo(R.string.cat_carousel_hero_demo_title) {
          @Override
          public Fragment createFragment() {
            return new HeroCarouselDemoFragment();
          }
        },
        new Demo(R.string.cat_carousel_fullscreen_demo_title) {
          @Override
          public Fragment createFragment() {
            return new FullScreenStrategyDemoFragment();
          }
        },
        new Demo(R.string.cat_carousel_uncontained_demo_title) {
          @Override
          public Fragment createFragment() {
            return new UncontainedCarouselDemoFragment();
          }
        });
  }

  /** The Dagger module for {@link CarouselFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {

    @FragmentScope
    @ContributesAndroidInjector
    abstract CarouselFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(R.string.cat_carousel_title, R.drawable.ic_lists) {
        @Override
        public Fragment createFragment() {
          return new CarouselFragment();
        }
      };
    }
  }
}
