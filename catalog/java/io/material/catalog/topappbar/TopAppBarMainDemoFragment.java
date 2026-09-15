/*
 * Copyright 2017 The Android Open Source Project
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;

/** A fragment that displays the main Top App Bar demo for the Catalog app. */
public class TopAppBarMainDemoFragment extends DemoFragment {

  private AppBarLayout appBarLayout;
  private Toolbar toolbar;
  private BadgeDrawable badgeDrawable;
  private MaterialSwitch editMenuToggle;
  private MaterialSwitch liftOnScrollToggle;
  private MaterialSwitch liftToggle;
  private MaterialButton incrementBadgeNumber;

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.cat_topappbar_fragment, viewGroup, false);

    appBarLayout = view.findViewById(R.id.appbarlayout);
    toolbar = view.findViewById(R.id.toolbar);
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(toolbar);

    editMenuToggle = view.findViewById(R.id.cat_topappbar_switch_edit_menu);
    editMenuToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          BadgeUtils.detachBadgeDrawable(badgeDrawable, toolbar, R.id.cat_topappbar_item_favorite);
          toolbar.getMenu().findItem(R.id.cat_topappbar_item_edit).setVisible(isChecked);
          BadgeUtils.attachBadgeDrawable(badgeDrawable, toolbar, R.id.cat_topappbar_item_favorite);
        });

    liftOnScrollToggle = view.findViewById(R.id.cat_topappbar_switch_lift_on_scroll);
    liftOnScrollToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          appBarLayout.setLiftOnScroll(isChecked);
          liftToggle.setEnabled(!isChecked);
          if (!isChecked) {
            appBarLayout.setLifted(liftToggle.isChecked());
          }
        });

    liftToggle = view.findViewById(R.id.cat_topappbar_switch_lifted);
    liftToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> appBarLayout.setLifted(isChecked));

    incrementBadgeNumber = view.findViewById(R.id.cat_topappbar_button_increment_badge);
    incrementBadgeNumber.setOnClickListener(
        v -> {
          badgeDrawable.setNumber(badgeDrawable.getNumber() + 1);
          badgeDrawable.setVisible(true);
        });

    DemoUtils.setupClickableContentText(view);

    return view;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.cat_topappbar_menu, menu);
    super.onCreateOptionsMenu(menu, menuInflater);
    badgeDrawable = BadgeDrawable.create(requireContext());
    badgeDrawable.setNumber(1);
    BadgeUtils.attachBadgeDrawable(badgeDrawable, toolbar, R.id.cat_topappbar_item_favorite);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.cat_topappbar_item_favorite) {
      badgeDrawable.clearNumber();
      badgeDrawable.setVisible(false);
    }
    return DemoUtils.showSnackbar(getActivity(), item) || super.onOptionsItemSelected(item);
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }
}
