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

package io.material.catalog.application;

import android.app.Application;
import dagger.BindsInstance;
import dagger.android.AndroidInjectionModule;
import io.material.catalog.application.scope.ApplicationScope;
import io.material.catalog.main.MainActivity;
import io.material.catalog.musicplayer.MusicPlayerDemoModule;
import io.material.catalog.transition.TransitionDemoModule;
import javax.inject.Singleton;

/** The Application's root component. */
@Singleton
@ApplicationScope
@dagger.Component(
    modules = {
      AndroidInjectionModule.class,
      CatalogApplicationModule.class,
      MainActivity.Module.class,
      CatalogDemoModule.class,
      TransitionDemoModule.class,
      MusicPlayerDemoModule.class,
    })
public interface CatalogApplicationComponent {
  void inject(CatalogApplication app);

  /** The root component's builder. */
  @dagger.Component.Builder
  interface Builder {
    @BindsInstance
    CatalogApplicationComponent.Builder application(Application application);

    CatalogApplicationComponent build();
  }
}
