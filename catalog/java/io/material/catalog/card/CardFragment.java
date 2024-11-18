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

package io.material.catalog.card;

import io.material.catalog.R;

import android.content.Intent;
import androidx.fragment.app.Fragment;
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

/** A landing fragment that links to card demos for the Catalog app. */
public class CardFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_card_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_card_description;
  }

  @Override
  public Demo getMainDemo() {
    return new Demo() {
      @Override
      public Fragment createFragment() {
        return new CardMainDemoFragment();
      }
    };
  }

  @Override
  public List<Demo> getAdditionalDemos() {
    return Arrays.asList(
        new Demo(R.string.cat_card_selection_mode) {
          @Override
          public Intent createActivityIntent() {
            return new Intent(getContext(), CardSelectionModeActivity.class);
          }
        },
        new Demo(R.string.cat_card_states) {
          @Override
          public Fragment createFragment() {
            return new CardStatesFragment();
          }
        },
        new Demo(R.string.cat_card_rich_media_demo) {
          @Override
          public Fragment createFragment() {
            return new CardRichMediaDemoFragment();
          }
        },
        new Demo(R.string.cat_card_list) {
          @Override
          public Fragment createFragment() {
            return new CardListDemoFragment();
          }
        },
        new Demo(R.string.cat_card_swipe_dismiss) {
          @Override
          public Fragment createFragment() {
            return new CardSwipeDismissFragment();
          }
        });
  }

  /** The Dagger module for {@link CardFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {

    @FragmentScope
    @ContributesAndroidInjector
    abstract CardFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(R.string.cat_card_title, R.drawable.ic_card) {
        @Override
        public Fragment createFragment() {
          return new CardFragment();
        }
      };
    }
  }
}
