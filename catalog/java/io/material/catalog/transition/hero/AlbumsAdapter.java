/*
 * Copyright 2020 The Android Open Source Project
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

package io.material.catalog.transition.hero;

import io.material.catalog.R;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.material.catalog.transition.hero.AlbumsAdapter.AlbumViewHolder;
import io.material.catalog.transition.hero.MusicData.Album;

/** An adapter which displays a list of albums in either a list or grid layout. */
class AlbumsAdapter extends ListAdapter<Album, AlbumViewHolder> {

  public interface AlbumAdapterListener {
    void onAlbumClicked(View view, Album album);
  }

  private final AlbumAdapterListener listener;
  private final int itemLayout;

  AlbumsAdapter(AlbumAdapterListener listener, boolean asGrid) {
    super(Album.DIFF_CALLBACK);
    this.listener = listener;
    this.itemLayout =
        asGrid ? R.layout.cat_transition_album_grid_item : R.layout.cat_transition_album_list_item;
  }

  @NonNull
  @Override
  public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    return new AlbumViewHolder(
        LayoutInflater.from(viewGroup.getContext()).inflate(itemLayout, viewGroup, false),
        listener);
  }

  @Override
  public void onBindViewHolder(@NonNull AlbumViewHolder albumViewHolder, int i) {
    albumViewHolder.bind(getItem(i));
  }

  static class AlbumViewHolder extends RecyclerView.ViewHolder {

    private final AlbumAdapterListener listener;

    private final View container;
    private final ImageView albumImage;
    private final TextView albumTitle;
    private final TextView albumArtist;
    // Optional field available only in the list item layout.
    @Nullable private final TextView albumDuration;

    AlbumViewHolder(View view, AlbumAdapterListener listener) {
      super(view);
      this.listener = listener;

      container = view.findViewById(R.id.album_item_container);
      albumImage = view.findViewById(R.id.album_image);
      albumTitle = view.findViewById(R.id.album_title);
      albumArtist = view.findViewById(R.id.album_artist);
      albumDuration = view.findViewById(R.id.album_duration);
    }

    public void bind(Album album) {
      // Use a unique transition name so this item can be used as a shared element when
      // transitioning to the album details screen.
      ViewCompat.setTransitionName(container, album.title);
      container.setOnClickListener(v -> listener.onAlbumClicked(container, album));
      albumImage.setImageResource(album.cover);
      albumTitle.setText(album.title);
      albumArtist.setText(album.artist);
      if (albumDuration != null) {
        albumDuration.setText(album.duration);
      }
    }
  }
}
