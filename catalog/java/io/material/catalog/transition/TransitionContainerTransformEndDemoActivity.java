/*
 * Copyright 2020 The Android Open Source Project
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

package io.material.catalog.transition;

import io.material.catalog.R;

import static io.material.catalog.transition.TransitionContainerTransformStartDemoActivity.configurationHelper;

import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import io.material.catalog.feature.DemoActivity;

/**
 * An activity that displays the Container Transform activity transition demo for the Catalog app.
 */
@RequiresApi(VERSION_CODES.LOLLIPOP)
public class TransitionContainerTransformEndDemoActivity extends DemoActivity {

  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

    // Set up shared element transition
    findViewById(android.R.id.content).setTransitionName("shared_element_end_root");
    setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
    getWindow().setSharedElementEnterTransition(buildContainerTransform(true));
    getWindow().setSharedElementReturnTransition(buildContainerTransform(false));

    super.onCreate(bundle);
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    return layoutInflater.inflate(
        R.layout.cat_transition_container_transform_end_activity,
        viewGroup,
        /* attachToRoot= */ false);
  }

  @Override
  public int getDemoTitleResId() {
    return R.string.cat_transition_container_transform_activity_title;
  }

  private MaterialContainerTransform buildContainerTransform(boolean entering) {
    MaterialContainerTransform transform = new MaterialContainerTransform(this, entering);
    // Use all 3 container layer colors since this transform can be configured using any fade mode
    // and some of the start views don't have a background and the end view doesn't have a
    // background.
    transform.setAllContainerColors(
        MaterialColors.getColor(findViewById(android.R.id.content), R.attr.colorSurface));
    transform.addTarget(android.R.id.content);
    configurationHelper.configure(transform, entering);
    return transform;
  }
}
