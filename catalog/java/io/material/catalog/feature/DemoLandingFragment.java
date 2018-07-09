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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.annotation.DimenRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.TextView;
import dagger.android.support.DaggerFragment;
import java.util.Collections;
import java.util.List;

/** Base class that provides a landing screen structure for a single feature demo. */
public abstract class DemoLandingFragment extends DaggerFragment {

  private static final String FRAGMENT_DEMO_CONTENT = "fragment_demo_content";

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_demo_landing_fragment, viewGroup, false /* attachToRoot */);

    AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(view.findViewById(R.id.toolbar));
    activity.getSupportActionBar().setTitle(getTitleResId());
    activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    TextView descriptionTextView = view.findViewById(R.id.cat_demo_landing_description);
    ViewGroup mainDemoContainer = view.findViewById(R.id.cat_demo_landing_main_demo_container);
    ViewGroup additionalDemosSection =
        view.findViewById(R.id.cat_demo_landing_additional_demos_section);
    ViewGroup additionalDemosContainer =
        view.findViewById(R.id.cat_demo_landing_additional_demos_container);

    descriptionTextView.setText(getDescriptionResId());
    addLinks(layoutInflater, view);
    addDemoView(layoutInflater, mainDemoContainer, getMainDemo(), false);
    List<Demo> additionalDemos = getAdditionalDemos();
    for (Demo additionalDemo : additionalDemos) {
      addDemoView(layoutInflater, additionalDemosContainer, additionalDemo, true);
    }
    additionalDemosSection.setVisibility(additionalDemos.isEmpty() ? View.GONE : View.VISIBLE);

    return view;
  }

  private void addLinks(LayoutInflater layoutInflater, View view) {
    ViewGroup linksSection = view.findViewById(R.id.cat_demo_landing_links_section);
    int linksArrayResId = getLinksArrayResId();
    if (linksArrayResId != -1) {
      String[] linksStringArray = getResources().getStringArray(linksArrayResId);
      for (String linkString : linksStringArray) {
        addLinkView(layoutInflater, linksSection, linkString);
      }
      linksSection.setVisibility(View.VISIBLE);
    } else {
      linksSection.setVisibility(View.GONE);
    }
  }

  private void addLinkView(LayoutInflater layoutInflater, ViewGroup viewGroup, String linkString) {
    TextView linkView =
        (TextView) layoutInflater.inflate(R.layout.cat_demo_landing_link_entry, viewGroup, false);

    linkView.setText(linkString);
    viewGroup.addView(linkView);
  }

  private void addDemoView(
      LayoutInflater layoutInflater, ViewGroup demoContainer, Demo demo, boolean isAdditional) {
    View demoView = layoutInflater.inflate(R.layout.cat_demo_landing_row, demoContainer, false);

    View rootView = demoView.findViewById(R.id.cat_demo_landing_row_root);
    TextView titleTextView = demoView.findViewById(R.id.cat_demo_landing_row_title);
    TextView subtitleTextView = demoView.findViewById(R.id.cat_demo_landing_row_subtitle);

    rootView.setOnClickListener(v -> startDemo(demo));

    titleTextView.setText(demo.getTitleResId());
    subtitleTextView.setText(getDemoClassName(demo));

    if (isAdditional) {
      setMarginStart(titleTextView, R.dimen.cat_list_text_margin_from_icon_large);
      setMarginStart(subtitleTextView, R.dimen.cat_list_text_margin_from_icon_large);
    }

    demoContainer.addView(demoView);
  }

  private String getDemoClassName(Demo demo) {
    if (demo.createFragment() != null) {
      return demo.createFragment().getClass().getSimpleName();
    } else if (demo.createActivityIntent() != null) {
      String className = demo.createActivityIntent().getComponent().getClassName();
      return className.substring(className.lastIndexOf('.') + 1);
    } else {
      throw new IllegalStateException("Demo must implement createFragment or createActivityIntent");
    }
  }

  private void startDemo(Demo demo) {
    if (demo.createFragment() != null) {
      startDemoFragment(demo.createFragment());
    } else if (demo.createActivityIntent() != null) {
      startDemoActivity(demo.createActivityIntent());
    } else {
      throw new IllegalStateException("Demo must implement createFragment or createActivityIntent");
    }
  }

  private void startDemoFragment(Fragment fragment) {
    Bundle args = new Bundle();
    args.putString(DemoFragment.ARG_DEMO_TITLE, getString(getTitleResId()));
    fragment.setArguments(args);
    FeatureDemoUtils.startFragment(getActivity(), fragment, FRAGMENT_DEMO_CONTENT);
  }

  private void startDemoActivity(Intent intent) {
    intent.putExtra(DemoActivity.EXTRA_DEMO_TITLE, getString(getTitleResId()));
    startActivity(intent);
  }

  private void setMarginStart(View view, @DimenRes int marginResId) {
    int margin = getResources().getDimensionPixelOffset(marginResId);
    MarginLayoutParams layoutParams = (MarginLayoutParams) view.getLayoutParams();
    MarginLayoutParamsCompat.setMarginStart(layoutParams, margin);
  }

  @StringRes
  public abstract int getTitleResId();

  @StringRes
  public abstract int getDescriptionResId();

  public abstract Demo getMainDemo();

  @ArrayRes
  public int getLinksArrayResId() {
    return -1;
  }

  public List<Demo> getAdditionalDemos() {
    return Collections.emptyList();
  }
}
