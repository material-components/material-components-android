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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasFragmentInjector;
import dagger.android.support.HasSupportFragmentInjector;
import javax.inject.Inject;

/** Base Activity class that provides a demo screen structure for a single demo. */
public abstract class DemoActivity extends AppCompatActivity
    implements HasFragmentInjector, HasSupportFragmentInjector {

  public static final String EXTRA_DEMO_TITLE = "demo_title";

  private Toolbar toolbar;
  private ViewGroup demoContainer;

  @Inject DispatchingAndroidInjector<Fragment> supportFragmentInjector;
  @Inject DispatchingAndroidInjector<android.app.Fragment> frameworkFragmentInjector;

  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    safeInject();
    super.onCreate(bundle);
    setContentView(R.layout.cat_demo_activity);

    toolbar = findViewById(R.id.toolbar);
    demoContainer = findViewById(R.id.cat_demo_activity_container);

    initDemoActionBar();
    demoContainer.addView(onCreateDemoView(LayoutInflater.from(this), demoContainer, bundle));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @StringRes
  public int getDemoTitleResId() {
    return 0;
  }

  protected boolean shouldShowDefaultDemoActionBar() {
    return true;
  }

  @Override
  public AndroidInjector<Fragment> supportFragmentInjector() {
    return supportFragmentInjector;
  }

  @Override
  public AndroidInjector<android.app.Fragment> fragmentInjector() {
    return frameworkFragmentInjector;
  }

  private void safeInject() {
    try {
      AndroidInjection.inject(this);
    } catch (Exception e) {
      // Ignore exception, not all DemoActivity subclasses need to inject
    }
  }

  private void initDemoActionBar() {
    if (shouldShowDefaultDemoActionBar()) {
      setSupportActionBar(toolbar);
      setDemoActionBarTitle(getSupportActionBar());
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

  private String getDefaultDemoTitle() {
    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      return extras.getString(EXTRA_DEMO_TITLE, "");
    } else {
      return "";
    }
  }

  public abstract View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle);
}
