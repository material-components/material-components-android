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

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.transition.TransitionManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.MaterialArcMotion;
import com.google.android.material.transition.MaterialContainerTransform;
import dagger.android.support.DaggerFragment;
import io.material.catalog.musicplayer.MusicData.Album;
import io.material.catalog.musicplayer.MusicData.Track;

/** A Fragment that displays an album's details. */
public class MusicPlayerAlbumDemoFragment extends DaggerFragment {

  public static final String TAG = "MusicPlayerAlbumDemoFragment";
  private static final String ALBUM_ID_KEY = "album_id_key";

  public static MusicPlayerAlbumDemoFragment newInstance(long albumId) {
    MusicPlayerAlbumDemoFragment fragment = new MusicPlayerAlbumDemoFragment();
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
    return layoutInflater.inflate(R.layout.cat_music_player_album_fragment, viewGroup, false);
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

    AppBarLayout appBarLayout = view.findViewById(R.id.app_bar_layout);
    FloatingActionButton fab = view.findViewById(R.id.fab);
    View musicPlayerContainer = view.findViewById(R.id.music_player_container);

    appBarLayout.addOnOffsetChangedListener(
        (appBarLayout1, verticalOffset) -> {
          float verticalOffsetPercentage =
              (float) Math.abs(verticalOffset) / (float) appBarLayout1.getTotalScrollRange();
          if (verticalOffsetPercentage > 0.2F && fab.isOrWillBeShown()) {
            fab.hide();
          } else if (verticalOffsetPercentage <= 0.2F
              && fab.isOrWillBeHidden()
              && musicPlayerContainer.getVisibility() != View.VISIBLE) {
            fab.show();
          }
        });

    // Set up music player transitions
    Context context = requireContext();
    MaterialContainerTransform musicPlayerEnterTransform =
        createMusicPlayerTransform(context, /* entering= */ true, fab, musicPlayerContainer);

    fab.setOnClickListener(
        v -> {
          TransitionManager.beginDelayedTransition(container, musicPlayerEnterTransform);
          fab.setVisibility(View.GONE);
          musicPlayerContainer.setVisibility(View.VISIBLE);
        });

    MaterialContainerTransform musicPlayerExitTransform =
        createMusicPlayerTransform(context, /* entering= */ false, musicPlayerContainer, fab);

    musicPlayerContainer.setOnClickListener(
        v -> {
          TransitionManager.beginDelayedTransition(container, musicPlayerExitTransform);
          musicPlayerContainer.setVisibility(View.GONE);
          fab.setVisibility(View.VISIBLE);
        });
  }

  protected void setUpAlbumViews(
      @NonNull ViewGroup container,
      @NonNull Toolbar toolbar,
      @NonNull ImageView albumImage,
      @NonNull TextView albumTitle,
      @NonNull TextView albumArtist,
      @NonNull RecyclerView songRecyclerView) {
    long albumId = getArguments().getLong(ALBUM_ID_KEY, 0L);
    Album album = MusicData.getAlbumById(albumId);

    // Set the transition name which matches the list/grid item to be transitioned from for
    // the shared element transition.
    ViewCompat.setTransitionName(container, album.title);

    // Set up toolbar
    ViewCompat.setElevation(toolbar, 0F);
    toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());

    // Set up album info area
    albumImage.setImageResource(album.cover);
    albumTitle.setText(album.title);
    albumArtist.setText(album.artist);

    // Set up track list
    songRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    TrackAdapter adapter = new TrackAdapter();
    songRecyclerView.setAdapter(adapter);
    adapter.submitList(album.tracks);
  }

  private static MaterialContainerTransform createMusicPlayerTransform(
      Context context, boolean entering, View startView, View endView) {
    MaterialContainerTransform musicPlayerTransform =
        new MaterialContainerTransform(context, entering);
    musicPlayerTransform.setPathMotion(new MaterialArcMotion());
    musicPlayerTransform.setScrimColor(Color.TRANSPARENT);
    musicPlayerTransform.setStartView(startView);
    musicPlayerTransform.setEndView(endView);
    musicPlayerTransform.addTarget(endView);
    return musicPlayerTransform;
  }

  /** An adapter to hold an albums list of tracks. */
  class TrackAdapter extends ListAdapter<Track, TrackAdapter.TrackViewHolder> {

    TrackAdapter() {
      super(Track.DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
      return new TrackViewHolder(
          LayoutInflater.from(viewGroup.getContext())
              .inflate(R.layout.cat_music_player_track_list_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder trackViewHolder, int i) {
      trackViewHolder.bind(getItem(i));
    }

    /** A ViewHolder for a single track item. */
    class TrackViewHolder extends RecyclerView.ViewHolder {

      private final ImageView playingIcon;
      private final TextView trackNumber;
      private final TextView trackTitle;
      private final TextView trackDuration;

      TrackViewHolder(View view) {
        super(view);
        playingIcon = view.findViewById(R.id.playing_icon);
        trackNumber = view.findViewById(R.id.track_number);
        trackTitle = view.findViewById(R.id.track_title);
        trackDuration = view.findViewById(R.id.track_duration);
      }

      public void bind(Track track) {
        playingIcon.setVisibility(track.playing ? View.VISIBLE : View.INVISIBLE);
        trackNumber.setText(String.valueOf(track.track));
        trackTitle.setText(track.title);
        trackDuration.setText(track.duration);
      }
    }
  }
}
