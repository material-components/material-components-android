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

package io.material.catalog.slider;

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

/** A fragment that displays slider demos for the Catalog app. */
public class SliderFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_slider_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_slider_description;
  }

  @Override
  public Demo getMainDemo() {
    return new Demo() {
      @Override
      public Fragment createFragment() {
        return new SliderMainDemoFragment();
      }
    };
  }

  @Override
  public List<Demo> getAdditionalDemos() {
    List<Demo> additionalDemos = new ArrayList<>();
    additionalDemos.add(
        new Demo(R.string.cat_slider_demo_continuous_title) {
          @Override
          public Fragment createFragment() {
            return new SliderContinuousDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_slider_demo_discrete_title) {
          @Override
          public Fragment createFragment() {
            return new SliderDiscreteDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_slider_demo_centered_title) {
          @Override
          public Fragment createFragment() {
            return new SliderCenteredDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_slider_demo_scroll_container_title) {
          @Override
          public Fragment createFragment() {
            return new SliderScrollContainerDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_slider_demo_label_behavior_title) {
          @Override
          public Fragment createFragment() {
            return new SliderLabelBehaviorDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_slider_demo_corner_title) {
          @Override
          public Fragment createFragment() {
            return new SliderCornerDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_slider_demo_icon_title) {
          @Override
          public Fragment createFragment() {
            return new SliderTrackIconDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_slider_demo_vertical_title) {
          @Override
          public Fragment createFragment() {
            return new SliderVerticalDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_slider_demo_vertical_scroll_container_title) {
          @Override
          public Fragment createFragment() {
            return new SliderVerticalScrollContainerDemoFragment();
          }
        });
    return additionalDemos;
  }

  /** The Dagger module for {@link SliderFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {

    @FragmentScope
    @ContributesAndroidInjector
    abstract SliderFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(R.string.cat_slider_title, R.drawable.ic_sliders_24px) {
        @Override
        public Fragment createFragment() {
          return new SliderFragment();
        }
      };
    }
  }
}
