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

import io.material.catalog.R;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.material.catalog.feature.DemoActivity;

/** An Activity which hosts a music app flow. */
public class MusicPlayerDemoActivity extends DemoActivity {

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    return layoutInflater.inflate(R.layout.cat_music_player_activity, viewGroup, false);
  }

  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, getLibraryDemoFragment())
        .commit();
  }

  @NonNull
  protected Fragment getLibraryDemoFragment() {
    return new MusicPlayerLibraryDemoFragment();
  }

  @Override
  protected boolean shouldShowDefaultDemoActionBar() {
    return false;
  }
}
