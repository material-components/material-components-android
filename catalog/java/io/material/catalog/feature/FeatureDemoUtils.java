/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.feature;

import io.material.catalog.R;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/** Utils for feature demos. */
public abstract class FeatureDemoUtils {

  private static final int MAIN_ACTIVITY_FRAGMENT_CONTAINER_ID = R.id.container;

  public static void startFragment(FragmentActivity activity, Fragment fragment, String tag) {
    activity
        .getSupportFragmentManager()
        .beginTransaction()
        .setCustomAnimations(
            R.anim.abc_grow_fade_in_from_bottom,
            R.anim.abc_fade_out,
            R.anim.abc_fade_in,
            R.anim.abc_shrink_fade_out_from_bottom)
        .replace(MAIN_ACTIVITY_FRAGMENT_CONTAINER_ID, fragment, tag)
        .addToBackStack(null /* name */)
        .commit();
  }

  public static Fragment getCurrentFragment(FragmentActivity activity) {
    return activity
        .getSupportFragmentManager()
        .findFragmentById(MAIN_ACTIVITY_FRAGMENT_CONTAINER_ID);
  }
}
