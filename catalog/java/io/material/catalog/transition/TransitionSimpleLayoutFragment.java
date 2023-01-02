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

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

/** A fragment that displays the provided layout. */
public class TransitionSimpleLayoutFragment extends Fragment {

  private static final String KEY_LAYOUT_RES_ID = "KEY_LAYOUT_RES_ID";
  private static final String KEY_TRANSITION_NAME = "KEY_TRANSITION_NAME";
  private static final String KEY_TRANSITION_NAME_VIEW_ID = "KEY_TRANSITION_NAME_VIEW_ID";

  private int layoutResId;
  @Nullable private String transitionName;
  private int transitionNameViewId;

  public static TransitionSimpleLayoutFragment newInstance(@LayoutRes int layoutResId) {
    return newInstance(layoutResId, null);
  }

  public static TransitionSimpleLayoutFragment newInstance(
      @LayoutRes int layoutResId, @Nullable String transitionName) {
    return newInstance(layoutResId, transitionName, View.NO_ID);
  }

  public static TransitionSimpleLayoutFragment newInstance(
      @LayoutRes int layoutResId,
      @Nullable String transitionName,
      @IdRes int transitionNameViewId) {
    TransitionSimpleLayoutFragment fragment = new TransitionSimpleLayoutFragment();
    Bundle args = new Bundle();
    args.putInt(KEY_LAYOUT_RES_ID, layoutResId);
    args.putString(KEY_TRANSITION_NAME, transitionName);
    args.putInt(KEY_TRANSITION_NAME_VIEW_ID, transitionNameViewId);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);

    Bundle args = getArguments();
    if (args != null) {
      layoutResId = args.getInt(KEY_LAYOUT_RES_ID);
      transitionName = args.getString(KEY_TRANSITION_NAME);
      transitionNameViewId = args.getInt(KEY_TRANSITION_NAME_VIEW_ID);
    }
  }

  @Override
  public View onCreateView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    return layoutInflater.inflate(layoutResId, viewGroup, false /* attachToRoot */);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
    if (transitionName != null) {
      View transitionNameView =
          transitionNameViewId == View.NO_ID ? view : view.findViewById(transitionNameViewId);
      ViewCompat.setTransitionName(transitionNameView, transitionName);
    }
  }
}
