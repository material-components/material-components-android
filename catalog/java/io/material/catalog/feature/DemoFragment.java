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

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.AndroidSupportInjection;
import dagger.android.support.HasSupportFragmentInjector;
import io.material.catalog.themeswitcher.ThemeSwitcherHelper;
import io.material.catalog.themeswitcher.ThemeSwitcherHelper.ThemeSwitcherFragment;
import javax.inject.Inject;

/** Base Fragment class that provides a demo screen structure for a single demo. */
public abstract class DemoFragment extends Fragment
    implements ThemeSwitcherFragment, HasSupportFragmentInjector {

  public static final String ARG_DEMO_TITLE = "demo_title";

  private Toolbar toolbar;
  private ViewGroup demoContainer;
  @Nullable private ThemeSwitcherHelper themeSwitcherHelper;

  @Inject DispatchingAndroidInjector<Fragment> childFragmentInjector;

  @Override
  public void onAttach(Context context) {
    safeInject();
    super.onAttach(context);

    themeSwitcherHelper = new ThemeSwitcherHelper(this);
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(R.layout.cat_demo_fragment, viewGroup, false /* attachToRoot */);

    toolbar = view.findViewById(R.id.toolbar);
    demoContainer = view.findViewById(R.id.cat_demo_fragment_container);

    initDemoActionBar();
    demoContainer.addView(onCreateDemoView(layoutInflater, viewGroup, bundle));

    return view;
  }

  @StringRes
  public int getDemoTitleResId() {
    return 0;
  }

  /**
   * Whether this fragment wants to use the default demo action bar, or if the fragment wants to use
   * its own Toolbar as the action bar.
   */
  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return true;
  }

  @Override
  public AndroidInjector<Fragment> supportFragmentInjector() {
    return childFragmentInjector;
  }

  private void safeInject() {
    try {
      AndroidSupportInjection.inject(this);
    } catch (Exception e) {
      // Ignore exception, not all DemoFragment subclasses need to inject
    }
  }

  private void initDemoActionBar() {
    if (shouldShowDefaultDemoActionBar()) {
      AppCompatActivity activity = (AppCompatActivity) getActivity();
      activity.setSupportActionBar(toolbar);
      setDemoActionBarTitle(activity.getSupportActionBar());
    } else {
      toolbar.setVisibility(View.GONE);
    }
  }

  private void setDemoActionBarTitle(ActionBar actionBar) {
    if (getDemoTitleResId() != 0) {
      actionBar.setTitle(getDemoTitleResId());
    } else {
      actionBar.setTitle(getDefaultDemoTitle());
    }
  }

  protected String getDefaultDemoTitle() {
    Bundle args = getArguments();
    if (args != null) {
      return args.getString(ARG_DEMO_TITLE, "");
    } else {
      return "";
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
    super.onCreateOptionsMenu(menu, menuInflater);
    themeSwitcherHelper.onCreateOptionsMenu(menu, menuInflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    return themeSwitcherHelper.onOptionsItemSelected(menuItem)
        || super.onOptionsItemSelected(menuItem);
  }

  public abstract View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle);
}
