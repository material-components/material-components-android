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
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.resources.MaterialResources;
import androidx.fragment.app.Fragment;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
  @ColorInt private int colorControlNormal;
  @ColorInt private int colorAccent;

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    setHasOptionsMenu(true);
  }

  @SuppressWarnings("RestrictTo")
  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_demo_landing_fragment, viewGroup, false /* attachToRoot */);

    Toolbar toolbar = view.findViewById(R.id.toolbar);

    AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(toolbar);
    activity.getSupportActionBar().setTitle(getTitleResId());
    activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    Context toolbarContext = toolbar.getContext();
    TypedArray a =
        toolbarContext
            .getTheme()
            .obtainStyledAttributes(new int[] {R.attr.colorControlNormal, R.attr.colorAccent});
    colorControlNormal =
        MaterialResources.getColorStateList(toolbarContext, a, 0).getDefaultColor();
    colorAccent = a.getColor(1, 0);

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

    DemoUtils.addBottomSpaceInsetsIfNeeded((ViewGroup) view);
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

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.mtrl_favorite_menu, menu);
    super.onCreateOptionsMenu(menu, menuInflater);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    MenuItem item = menu.findItem(R.id.favorite_toggle);
    boolean isChecked = FeatureDemoUtils.getDefaultDemo(getContext()).equals(getClass().getName());
    item.setChecked(isChecked);
    MenuItemCompat.setIconTintList(
        item, ColorStateList.valueOf(isChecked ? colorAccent : colorControlNormal));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    if (menuItem.getItemId() == R.id.favorite_toggle) {
      boolean isChecked = !menuItem.isChecked();
      FeatureDemoUtils.saveDefaultDemo(getContext(), isChecked ? getClass().getName() : "");
      if (getActivity() != null) {
        getActivity().invalidateOptionsMenu();
      }
      return true;
    }

    return super.onOptionsItemSelected(menuItem);
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
