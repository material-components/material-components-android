/*
 * Copyright 2019 The Android Open Source Project
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

package io.material.catalog.transition;

import io.material.catalog.R;

import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.fragment.app.Fragment;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.IntoSet;
import io.material.catalog.application.scope.ActivityScope;
import io.material.catalog.application.scope.FragmentScope;
import io.material.catalog.feature.Demo;
import io.material.catalog.feature.DemoLandingFragment;
import io.material.catalog.feature.FeatureDemo;
import io.material.catalog.transition.hero.TransitionMusicDemoActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A landing fragment that links to Transition demos for the Catalog app. */
public class TransitionFragment extends DemoLandingFragment {

  @Override
  public int getTitleResId() {
    return R.string.cat_transition_title;
  }

  @Override
  public int getDescriptionResId() {
    return R.string.cat_transition_description;
  }

  @Override
  public Demo getMainDemo() {
    return new Demo() {
      @Override
      public Intent createActivityIntent() {
        return new Intent(getContext(), TransitionMusicDemoActivity.class);
      }
    };
  }

  @Override
  public List<Demo> getAdditionalDemos() {
    List<Demo> demos = new ArrayList<>();
    boolean shouldAddPlatformDemos = VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
    if (shouldAddPlatformDemos) {
      demos.add(
          new Demo(R.string.cat_transition_container_transform_activity_title) {
            @Override
            public Intent createActivityIntent() {
              return new Intent(getContext(), TransitionContainerTransformStartDemoActivity.class);
            }
          });
    }
    demos.addAll(
        Arrays.asList(
            new Demo(R.string.cat_transition_container_transform_fragment_title) {
              @Override
              public Fragment createFragment() {
                return new TransitionContainerTransformDemoFragment();
              }
            },
            new Demo(R.string.cat_transition_container_transform_view_title) {
              @Override
              public Fragment createFragment() {
                return new TransitionContainerTransformViewDemoFragment();
              }
            }));

    if (shouldAddPlatformDemos) {
      demos.add(
          new Demo(R.string.cat_transition_shared_axis_activity_title) {
            @Override
            public Intent createActivityIntent() {
              return new Intent(getContext(), TransitionSharedAxisStartDemoActivity.class);
            }
          }
      );
    }
    demos.addAll(
        Arrays.asList(
            new Demo(R.string.cat_transition_shared_axis_fragment_title) {
              @Override
              public Fragment createFragment() {
                return new TransitionSharedAxisDemoFragment();
              }
            },
            new Demo(R.string.cat_transition_shared_axis_view_title) {
              @Override
              public Fragment createFragment() {
                return new TransitionSharedAxisViewDemoFragment();
              }
            },
            new Demo(R.string.cat_transition_fade_through_title) {
              @Override
              public Fragment createFragment() {
                return new TransitionFadeThroughDemoFragment();
              }
            },
            new Demo(R.string.cat_transition_fade_title) {
              @Override
              public Fragment createFragment() {
                return new TransitionFadeDemoFragment();
              }
            }));
    return demos;
  }

  /** The Dagger module for {@link TransitionFragment} dependencies. */
  @dagger.Module
  public abstract static class Module {

    @FragmentScope
    @ContributesAndroidInjector
    abstract TransitionFragment contributeInjector();

    @IntoSet
    @Provides
    @ActivityScope
    static FeatureDemo provideFeatureDemo() {
      return new FeatureDemo(R.string.cat_transition_title, R.drawable.ic_transition) {
        @Override
        public Fragment createFragment() {
          return new TransitionFragment();
        }
      };
    }
  }
}
