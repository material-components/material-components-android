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

package io.material.catalog.chip;

import io.material.catalog.R;

import androidx.fragment.app.Fragment;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
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

/** A landing fragment that links to Chip demos for the Catalog app. */
public class ChipFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_chip_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_chip_description;
  }

  @Override
  public Demo getMainDemo() {
    return new Demo() {
      @Override
      public Fragment createFragment() {
        return new ChipMainDemoFragment();
      }
    };
  }

  @Override
  public List<Demo> getAdditionalDemos() {
    List<Demo> additionalDemos = new ArrayList<>();
    additionalDemos.add(
        new Demo(R.string.cat_chip_group_demo_title) {
          @Override
          public Fragment createFragment() {
            return new ChipGroupDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_chip_recyclerview_demo_title) {
          @Override
          public Fragment createFragment() {
            return new ChipRecyclerviewDemoFragment();
          }
        });
    return additionalDemos;
  }

  @StringRes
  protected static int getDemoTitleResId() {
    return R.string.cat_chip_title;
  }

  @DrawableRes
  protected static int getDemoDrawableResId() {
    return R.drawable.ic_chips;
  }

  @StringRes
  protected static int getChipGroupTitleRes() {
    return R.string.cat_chip_group_demo_title;
  }

  /** The Dagger module for {@link ChipFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {

    @FragmentScope
    @ContributesAndroidInjector
    abstract ChipFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(getDemoTitleResId(), getDemoDrawableResId()) {
        @Override
        public Fragment createFragment() {
          return new ChipFragment();
        }
      };
    }
  }
}
