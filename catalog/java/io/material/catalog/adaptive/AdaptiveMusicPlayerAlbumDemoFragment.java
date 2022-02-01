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

import io.material.catalog.R;

import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.material.catalog.musicplayer.MusicPlayerAlbumDemoFragment;

/** A Fragment that displays an album's details. */
public class AdaptiveMusicPlayerAlbumDemoFragment extends MusicPlayerAlbumDemoFragment {

  public static final String TAG = "AdaptiveMusicPlayerAlbumDemoFragment";
  private static final String ALBUM_ID_KEY = "album_id_key";

  @NonNull
  public static AdaptiveMusicPlayerAlbumDemoFragment newInstance(long albumId) {
    AdaptiveMusicPlayerAlbumDemoFragment fragment = new AdaptiveMusicPlayerAlbumDemoFragment();
    Bundle bundle = new Bundle();
    bundle.putLong(ALBUM_ID_KEY, albumId);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    return layoutInflater.inflate(
        R.layout.cat_adaptive_music_player_album_fragment, viewGroup, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    ViewGroup container = view.findViewById(R.id.container);
    Toolbar toolbar = view.findViewById(R.id.toolbar);
    ImageView albumImage = view.findViewById(R.id.album_image);
    TextView albumTitle = view.findViewById(R.id.album_title);
    TextView albumArtist = view.findViewById(R.id.album_artist);
    RecyclerView songRecyclerView = view.findViewById(R.id.song_recycler_view);

    setUpAlbumViews(container, toolbar, albumImage, albumTitle, albumArtist, songRecyclerView);
  }
}
