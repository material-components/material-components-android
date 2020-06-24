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

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
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
import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
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

    Bundle arguments = getArguments();
    if (arguments != null) {
      String transitionName = arguments.getString(FeatureDemoUtils.ARG_TRANSITION_NAME);
      ViewCompat.setTransitionName(view, transitionName);
    }

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

    // Links should be added whether or not the feature is restricted.
    addLinks(layoutInflater, view);

    // If this fragments demos is restricted, due to conditions set by the subclass, exit early
    // without showing any demos and just show the restricted message.
    if (isRestricted()) {
      descriptionTextView.setText(getRestrictedMessageId());
      mainDemoContainer.setVisibility(View.GONE);
      additionalDemosSection.setVisibility(View.GONE);
      return view;
    }

    descriptionTextView.setText(getDescriptionResId());
    addDemoView(layoutInflater, mainDemoContainer, getMainDemo(), false);
    List<Demo> additionalDemos = getAdditionalDemos();
    for (Demo additionalDemo : additionalDemos) {
      addDemoView(layoutInflater, additionalDemosContainer, additionalDemo, true);
    }
    additionalDemosSection.setVisibility(additionalDemos.isEmpty() ? View.GONE : View.VISIBLE);

    DemoUtils.addBottomSpaceInsetsIfNeeded((ViewGroup) view, viewGroup);
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

    String transitionName = getString(demo.getTitleResId());
    ViewCompat.setTransitionName(rootView, transitionName);
    rootView.setOnClickListener(v -> startDemo(v, demo, transitionName));

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

  private void startDemo(View sharedElement, Demo demo, String transitionName) {
    if (demo.createFragment() != null) {
      startDemoFragment(sharedElement, demo.createFragment(), transitionName);
    } else if (demo.createActivityIntent() != null) {
      startDemoActivity(sharedElement, demo.createActivityIntent(), transitionName);
    } else {
      throw new IllegalStateException("Demo must implement createFragment or createActivityIntent");
    }
  }

  private void startDemoFragment(View sharedElement, Fragment fragment, String transitionName) {
    Bundle args = new Bundle();
    args.putString(DemoFragment.ARG_DEMO_TITLE, getString(getTitleResId()));
    args.putString(FeatureDemoUtils.ARG_TRANSITION_NAME, transitionName);
    fragment.setArguments(args);
    FeatureDemoUtils.startFragment(
        getActivity(), fragment, FRAGMENT_DEMO_CONTENT, sharedElement, transitionName);
  }

  private void startDemoActivity(View sharedElement, Intent intent, String transitionName) {
    intent.putExtra(DemoActivity.EXTRA_DEMO_TITLE, getString(getTitleResId()));
    intent.putExtra(DemoActivity.EXTRA_TRANSITION_NAME, transitionName);

    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      // Set up shared element transition and disable overlay so views don't show above system bars
      FragmentActivity activity = getActivity();
      activity.setExitSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
      activity.getWindow().setSharedElementsUseOverlay(false);

      ActivityOptions options =
          ActivityOptions.makeSceneTransitionAnimation(activity, sharedElement, transitionName);
      startActivity(intent, options.toBundle());
    } else {
      startActivity(intent);
    }
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
    setMenuItemChecked(item, isChecked);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    if (menuItem.getItemId() == R.id.favorite_toggle) {
      boolean isChecked = !menuItem.isChecked();
      FeatureDemoUtils.saveDefaultDemo(getContext(), isChecked ? getClass().getName() : "");
      setMenuItemChecked(menuItem, isChecked);
      return true;
    }

    return super.onOptionsItemSelected(menuItem);
  }

  private void setMenuItemChecked(MenuItem menuItem, boolean isChecked) {
    menuItem.setChecked(isChecked);
    MenuItemCompat.setIconTintList(
        menuItem, ColorStateList.valueOf(isChecked ? colorAccent : colorControlNormal));
  }

  /**
   * Whether or not the feature shown by this fragment should be flagged as restricted.
   *
   * <p>Examples of restricted feature could be features which depends on an API level that is
   * greater than MDCs min sdk version. If overriding this method, you should also override {@link
   * #getRestrictedMessageId()} and provide information about why the feature is restricted.
   */
  public boolean isRestricted() {
    return false;
  }

  /**
   * The message to display if a feature {@link #isRestricted()}.
   *
   * <p>This message should provide insight into why the feature is restricted for the device it is
   * running on. This message will be displayed in the description area of the demo fragment instead
   * of the the provided {@link #getDescriptionResId()}. Additionally, all demos, both the main demo
   * and any additional demos will not be shown.
   */
  @StringRes
  public int getRestrictedMessageId() {
    return 0;
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
