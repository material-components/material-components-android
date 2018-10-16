/*
 * Copyright 2018 The Android Open Source Project
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

package io.material.catalog.topappbar;

import io.material.catalog.R;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import io.material.catalog.feature.DemoActivity;

/** A base activity for the Top App Bar Action Bar demos for the Catalog app. */
public abstract class BaseTopAppBarActionBarDemoActivity extends DemoActivity {

  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setSubtitle(R.string.cat_topappbar_action_bar_subtitle);
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(R.layout.cat_topappbar_action_bar_activity, viewGroup, false);
    TextView demoDescriptionTextView = view.findViewById(R.id.action_bar_demo_description);
    demoDescriptionTextView.setText(getActionBarDemoDescription());
    return view;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.cat_topappbar_menu, menu);
    return true;
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }

  @StringRes
  public int getActionBarDemoDescription() {
    return R.string.cat_topappbar_action_bar_description;
  }
}
