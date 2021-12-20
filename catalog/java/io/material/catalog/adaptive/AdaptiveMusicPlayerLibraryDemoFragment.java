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
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.core.view.ViewCompat;
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter;
import androidx.window.layout.DisplayFeature;
import androidx.window.layout.FoldingFeature;
import androidx.window.layout.WindowInfoTracker;
import androidx.window.layout.WindowLayoutInfo;
import com.google.android.material.transition.Hold;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.android.material.transition.MaterialFadeThrough;
import io.material.catalog.musicplayer.MusicData.Album;
import io.material.catalog.musicplayer.MusicPlayerLibraryDemoFragment;
import java.util.List;
import java.util.concurrent.Executor;

/** An adaptive Fragment that hosts a toolbar and a child fragment with a list of music data. */
public class AdaptiveMusicPlayerLibraryDemoFragment extends MusicPlayerLibraryDemoFragment {

  @Nullable private WindowInfoTrackerCallbackAdapter windowInfoTracker;
  private final Consumer<WindowLayoutInfo> stateContainer = new StateContainer();
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final Executor executor = command -> handler.post(() -> handler.post(command));

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getDemoLayoutResId(), viewGroup, false);
    windowInfoTracker =
        new WindowInfoTrackerCallbackAdapter(
            WindowInfoTracker.getOrCreate(requireActivity()));
    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    if (windowInfoTracker != null) {
      windowInfoTracker.addWindowLayoutInfoListener(this.getActivity(), executor, stateContainer);
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (windowInfoTracker != null) {
      windowInfoTracker.removeWindowLayoutInfoListener(stateContainer);
    }
  }

  @Override
  public void onAlbumClicked(@NonNull View view, @NonNull Album album) {
    AdaptiveMusicPlayerAlbumDemoFragment fragment =
        AdaptiveMusicPlayerAlbumDemoFragment.newInstance(album.getAlbumId());

    MaterialContainerTransform transform =
        new MaterialContainerTransform(requireContext(), /* entering= */ true);
    fragment.setSharedElementEnterTransition(transform);

    // Use a Hold transition to keep this fragment visible beneath the MaterialContainerTransform
    // that transitions to the album details screen. Without a Hold, this fragment would disappear
    // as soon its container is replaced with a new Fragment.
    Hold hold = new Hold();
    // Add root view as target for the Hold so that the entire view hierarchy is held in place as
    // one instead of each child view individually. Helps keep shadows during the transition.
    hold.addTarget(R.id.sliding_pane_layout);
    hold.setDuration(transform.getDuration());
    setExitTransition(hold);

    getParentFragmentManager()
        .beginTransaction()
        .addSharedElement(view, ViewCompat.getTransitionName(view))
        .replace(R.id.fragment_container, fragment, AdaptiveMusicPlayerAlbumDemoFragment.TAG)
        .addToBackStack(AdaptiveMusicPlayerAlbumDemoFragment.TAG)
        .commit();
  }

  private class StateContainer implements Consumer<WindowLayoutInfo> {

    public StateContainer() {}

    @Override
    public void accept(WindowLayoutInfo windowLayoutInfo) {
      List<DisplayFeature> displayFeatures = windowLayoutInfo.getDisplayFeatures();
      MaterialFadeThrough fadeThrough = new MaterialFadeThrough();
      boolean listTypeGrid = false;

      for (DisplayFeature displayFeature : displayFeatures) {
        FoldingFeature foldingFeature = (FoldingFeature) displayFeature;
        if (foldingFeature.getState() == FoldingFeature.State.HALF_OPENED
            || foldingFeature.getState() == FoldingFeature.State.FLAT) {
          // Device is foldable and open.
          listTypeGrid = true;
        }
      }

      setListType(listTypeGrid, fadeThrough);
    }
  }
}
