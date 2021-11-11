/*
 * Copyright 2021 The Android Open Source Project
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

package io.material.catalog.musicplayer;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import io.material.catalog.application.scope.ActivityScope;
import io.material.catalog.application.scope.FragmentScope;

/** A module for Music Player demo dependencies. */
@Module
public abstract class MusicPlayerDemoModule {

  @ActivityScope
  @ContributesAndroidInjector
  abstract MusicPlayerDemoActivity contributeMusicPlayerDemoActivity();

  @FragmentScope
  @ContributesAndroidInjector
  abstract MusicPlayerAlbumDemoFragment contributeMusicPlayerAlbumDemoFragment();
}
