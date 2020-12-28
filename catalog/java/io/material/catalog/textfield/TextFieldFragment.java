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

package io.material.catalog.textfield;

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

/** A landing fragment that links to text field demos for the Catalog app. */
public class TextFieldFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_textfield_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_textfield_description;
  }

  @Override
  public Demo getMainDemo() {
    return new Demo() {
      @Override
      public Fragment createFragment() {
        return new TextFieldMainDemoFragment();
      }
    };
  }

  @Override
  public List<Demo> getAdditionalDemos() {
    List<Demo> additionalDemos = new ArrayList<>();
    additionalDemos.add(
        new Demo(R.string.cat_textfield_filled_demo_title) {
          @Override
          public Fragment createFragment() {
            return new TextFieldFilledDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_textfield_outlined_demo_title) {
          @Override
          public Fragment createFragment() {
            return new TextFieldOutlinedDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_textfield_exposed_dropdown_menu_demo_title) {
          @Override
          public Fragment createFragment() {
            return new TextFieldExposedDropdownMenuDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_textfield_filled_icons_demo_title) {
          @Override
          public Fragment createFragment() {
            return new TextFieldFilledIconsDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_textfield_outlined_icons_demo_title) {
          @Override
          public Fragment createFragment() {
            return new TextFieldOutlinedIconsDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_textfield_prefix_suffix_demo_title) {
          @Override
          public Fragment createFragment() {
            return new TextFieldPrefixSuffixDemoFragment();
          }
        });
    additionalDemos.add(
        new Demo(R.string.cat_textfield_legacy_demo_title) {
          @Override
          public Fragment createFragment() {
            return new TextFieldLegacyDemoFragment();
          }
        });
    return additionalDemos;
  }

  /** The Dagger module for {@link TextFieldFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {
    @FragmentScope
    @ContributesAndroidInjector
    abstract TextFieldFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(R.string.cat_textfield_title, R.drawable.ic_textfield) {
        @Override
        public Fragment createFragment() {
          return new TextFieldFragment();
        }
      };
    }
  }
}
