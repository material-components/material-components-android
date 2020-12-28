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

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.transition.MaterialFadeThrough;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the Fade Through Transition demo for the Catalog app. */
public class TransitionFadeThroughDemoFragment extends DemoFragment {

  private static final SparseIntArray LAYOUT_RES_MAP = new SparseIntArray();

  static {
    LAYOUT_RES_MAP.append(R.id.action_albums, R.layout.cat_transition_fade_through_albums_fragment);
    LAYOUT_RES_MAP.append(R.id.action_photos, R.layout.cat_transition_fade_through_photos_fragment);
    LAYOUT_RES_MAP.append(R.id.action_search, R.layout.cat_transition_fade_through_search_fragment);
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    return layoutInflater.inflate(R.layout.cat_transition_fade_through_fragment, viewGroup, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {

    replaceFragment(R.id.action_albums);

    BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomnavigation);
    bottomNavigationView.setOnNavigationItemSelectedListener(
        item -> {
          replaceFragment(item.getItemId());
          return true;
        });
  }

  @LayoutRes
  private static int getLayoutForItemId(@IdRes int itemId) {
    return LAYOUT_RES_MAP.get(itemId);
  }

  private void replaceFragment(@IdRes int itemId) {
    Fragment fragment = TransitionSimpleLayoutFragment.newInstance(getLayoutForItemId(itemId));
    // Set the transition as the Fragment's enter transition. This will be used when the fragment
    // is added to the container and re-used when the fragment is removed from the container.
    fragment.setEnterTransition(createTransition());

    getChildFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commit();
  }

  private MaterialFadeThrough createTransition(){
    MaterialFadeThrough fadeThrough = new MaterialFadeThrough();

    // Add targets for this transition to explicitly run transitions only on these views. Without
    // targeting, a MaterialFadeThrough would be run for every view in the Fragment's layout.
    fadeThrough.addTarget(R.id.albums_fragment);
    fadeThrough.addTarget(R.id.photos_fragment);
    fadeThrough.addTarget(R.id.search_fragment);

    return fadeThrough;
  }
}
