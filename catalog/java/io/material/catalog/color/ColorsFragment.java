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

package io.material.catalog.color;

import io.material.catalog.R;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.HarmonizedColors;
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

/** A landing fragment that links to color demos for the Catalog app. */
public class ColorsFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_colors_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_colors_description;
  }

  @NonNull
  @Override
  public Demo getMainDemo() {
    return new Demo() {
      @Override
      public Fragment createFragment() {
        return new ColorMainDemoFragment();
      }
    };
  }

  @Nullable
  public Demo getColorHarmonizationDemo() {
    return new Demo(R.string.cat_color_harmonization) {
      @Override
      public Intent createActivityIntent() {
        return new Intent(getContext(), ColorHarmonizationDemoActivity.class);
      }
    };
  }

  @NonNull
  @Override
  public List<Demo> getAdditionalDemos() {
    List<Demo> additionalDemos = new ArrayList<>();
    if (DynamicColors.isDynamicColorAvailable()) {
      additionalDemos.add(
          new Demo(R.string.cat_color_dynamic_palette) {
            @Override
            public Fragment createFragment() {
              return new ColorDynamicDemoFragment();
            }
          });
    }
    if (HarmonizedColors.isHarmonizedColorAvailable() && getColorHarmonizationDemo() != null) {
      additionalDemos.add(getColorHarmonizationDemo());
    }
    return additionalDemos;
  }

  /** The Dagger module for {@link ColorsFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {

    @FragmentScope
    @ContributesAndroidInjector
    abstract ColorsFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(R.string.cat_colors_title, R.drawable.ic_placeholder) {
        @Override
        public Fragment createFragment() {
          return new ColorsFragment();
        }
      };
    }
  }
}
