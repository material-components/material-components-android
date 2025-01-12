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

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import io.material.catalog.feature.DemoActivity;

/**
 * An activity that displays the Container Transform activity transition demo for the Catalog app.
 */
public class TransitionContainerTransformStartDemoActivity extends DemoActivity {

  static ContainerTransformConfigurationHelper configurationHelper;

  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

    // Set up shared element transition
    setExitSharedElementCallback(new MaterialContainerTransformSharedElementCallback());

    super.onCreate(bundle);

    configurationHelper = new ContainerTransformConfigurationHelper();

    addTransitionableTarget(R.id.start_fab);
    addTransitionableTarget(R.id.single_line_list_item);
    addTransitionableTarget(R.id.vertical_card_item);
    addTransitionableTarget(R.id.horizontal_card_item);
    addTransitionableTarget(R.id.grid_card_item);
    addTransitionableTarget(R.id.grid_tall_card_item);
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    return layoutInflater.inflate(
        R.layout.cat_transition_container_transform_start_fragment,
        viewGroup,
        /* attachToRoot= */ false);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    configurationHelper = null;
  }

  @Override
  public int getDemoTitleResId() {
    return R.string.cat_transition_container_transform_activity_title;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.configure_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    if (menuItem.getItemId() == R.id.configure) {
      configurationHelper.showConfigurationChooser(this, null);
      return true;
    }
    return super.onOptionsItemSelected(menuItem);
  }

  private void addTransitionableTarget(@IdRes int id) {
    View target = findViewById(id);
    if (target != null) {
      target.setOnClickListener(this::startEndActivity);
    }
  }

  private void startEndActivity(View sharedElement) {
    Intent intent = new Intent(this, TransitionContainerTransformEndDemoActivity.class);
    ActivityOptions options =
        ActivityOptions.makeSceneTransitionAnimation(
            this, sharedElement, "shared_element_end_root");
    startActivity(intent, options.toBundle());
  }
}
