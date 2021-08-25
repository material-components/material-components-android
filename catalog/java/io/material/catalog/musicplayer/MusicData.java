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

import androidx.recyclerview.widget.DiffUtil.ItemCallback;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** A static store of data to be used in the main music player flow example. */
public class MusicData {

  private MusicData() {}

  private static final List<Track> TRACKS =
      Arrays.asList(
          new Track(1, "First", "3:25", true),
          new Track(2, "Second", "4:51", false),
          new Track(3, "Third", "4:12", false),
          new Track(4, "Fourth", "2:34", false),
          new Track(5, "Fifth", "439", false),
          new Track(6, "Sixth", "2:31", false),
          new Track(7, "Seventh", "5:25", false),
          new Track(8, "Eighth", "3:46", false),
          new Track(9, "Ninth", "4:28", false),
          new Track(10, "Tenth", "4:47", false),
          new Track(11, "Eleventh", "5:14", false),
          new Track(12, "Twelfth", "4:46", false),
          new Track(13, "Thirteenth", "7:13", false),
          new Track(14, "Fourteenth", "2:43", false));

  static final List<Album> ALBUMS =
      Arrays.asList(
          new Album(
              0L,
              "Metamorphosis",
              "Sandra Adams",
              R.drawable.album_efe_kurnaz_unsplash,
              TRACKS,
              "52 mins"),
          new Album(
              1L,
              "Continuity",
              "Ali Connors",
              R.drawable.album_pawel_czerwinski_unsplash,
              TRACKS,
              "92 mins"),
          new Album(
              2L,
              "Break Point",
              "David Park",
              R.drawable.album_jean_philippe_delberghe_unsplash,
              TRACKS,
              "45 mins"),
          new Album(
              3L,
              "Headspace",
              "Charlie z.",
              R.drawable.album_karina_vorozheeva_unsplash,
              TRACKS,
              "65 mins"),
          new Album(
              4L,
              "New Neighbors",
              "Trevor Hansen",
              R.drawable.album_amy_shamblen_unsplash,
              TRACKS,
              "73 mins"),
          new Album(
              5L,
              "Spaced Out",
              "Jonas Eckhart",
              R.drawable.album_pawel_czerwinski_unsplash_2,
              TRACKS,
              "4 mins"),
          new Album(
              6L,
              "Holding on",
              "Elizabeth Park",
              R.drawable.album_kristopher_roller_unsplash,
              TRACKS,
              "40 mins"),
          new Album(
              7L,
              "Persistence",
              "Britta Holt",
              R.drawable.album_emile_seguin_unsplash,
              TRACKS,
              "39 mins"),
          new Album(
              8L,
              "At the Top",
              "Annie Chiu",
              R.drawable.album_ellen_qin_unsplash,
              TRACKS,
              "46 mins"),
          new Album(
              9L,
              "On Dry Land",
              "Alfonso Gonzalez",
              R.drawable.album_david_clode_unsplash,
              TRACKS,
              "55 mins"));

  static Album getAlbumById(long albumId) {
    for (Album album : MusicData.ALBUMS) {
      if (album.id == albumId) {
        return album;
      }
    }
    throw new IllegalArgumentException(
        String.format(Locale.US, "Album %d does not exist.", albumId));
  }

  /** A data class to hold information about a music album. */
  public static class Album {

    final long id;
    final String title;
    final String artist;
    @DrawableRes final int cover;
    final List<Track> tracks;
    final String duration;

    static final ItemCallback<Album> DIFF_CALLBACK =
        new ItemCallback<Album>() {
          @Override
          public boolean areItemsTheSame(@NonNull Album newItem, @NonNull Album oldItem) {
            return newItem.title.equals(oldItem.title);
          }

          @Override
          public boolean areContentsTheSame(@NonNull Album newItem, @NonNull Album oldItem) {
            return false;
          }
        };

    Album(
        long id,
        String title,
        String artist,
        @DrawableRes int cover,
        List<Track> tracks,
        String duration) {
      this.id = id;
      this.title = title;
      this.artist = artist;
      this.cover = cover;
      this.tracks = tracks;
      this.duration = duration;
    }

    public long getAlbumId() {
      return id;
    }
  }

  /** A data class to hold information about a track on an album. */
  static class Track {

    final int track;
    final String title;
    final String duration;

    boolean playing;

    static final ItemCallback<Track> DIFF_CALLBACK =
        new ItemCallback<Track>() {
          @Override
          public boolean areItemsTheSame(@NonNull Track newItem, @NonNull Track oldItem) {
            return newItem.title.equals(oldItem.title);
          }

          @Override
          public boolean areContentsTheSame(@NonNull Track newItem, @NonNull Track oldItem) {
            return false;
          }
        };

    Track(int track, String title, String duration, boolean isPlaying) {
      this.track = track;
      this.title = title;
      this.duration = duration;
      this.playing = isPlaying;
    }
  }
}
