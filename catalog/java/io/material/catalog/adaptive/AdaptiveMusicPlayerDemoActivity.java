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

package io.material.catalog.adaptive;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import io.material.catalog.musicplayer.MusicPlayerDemoActivity;

/** An Activity which hosts an Adaptive music app flow. */
public class AdaptiveMusicPlayerDemoActivity extends MusicPlayerDemoActivity {

  @Override
  @NonNull
  protected Fragment getLibraryDemoFragment() {
    return new AdaptiveMusicPlayerLibraryDemoFragment();
  }
}
