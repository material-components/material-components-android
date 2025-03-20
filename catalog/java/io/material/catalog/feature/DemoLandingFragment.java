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
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import dagger.android.support.DaggerFragment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Base class that provides a landing screen structure for a single feature demo. */
public abstract class DemoLandingFragment extends DaggerFragment {

  private static final String FRAGMENT_DEMO_CONTENT = "fragment_demo_content";
  @ColorInt private int menuIconColorUnchecked;
  @ColorInt private int menuIconColorChecked;

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    setHasOptionsMenu(true);

    if (bundle == null && isFavoriteLaunch()) {
      startDefaultDemoIfNeeded();
    }
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
            .obtainStyledAttributes(
                new int[] {
                  com.google.android.material.R.attr.colorOnSurfaceVariant, androidx.appcompat.R.attr.colorPrimary
                });
    menuIconColorUnchecked = a.getColor(0, 0);
    menuIconColorChecked = a.getColor(1, 0);
    a.recycle();

    TextView descriptionTextView = view.findViewById(R.id.cat_demo_landing_description);
    ViewGroup mainDemoContainer = view.findViewById(R.id.cat_demo_landing_main_demo_container);

    // Links should be added whether or not the feature is restricted.
    addLinks(layoutInflater, view);

    // If this fragments demos is restricted, due to conditions set by the subclass, exit early
    // without showing any demos and just show the restricted message.
    if (isRestricted()) {
      ViewGroup additionalDemosSection =
          view.findViewById(R.id.cat_demo_landing_additional_demos_section);
      descriptionTextView.setText(getRestrictedMessageId());
      mainDemoContainer.setVisibility(View.GONE);
      additionalDemosSection.setVisibility(View.GONE);
      return view;
    }

    descriptionTextView.setText(getDescriptionResId());
    clearAndAddDemoViews(view, layoutInflater);

    DemoUtils.addBottomSpaceInsetsIfNeeded((ViewGroup) view, viewGroup);
    return view;
  }

  private void clearAndAddDemoViews(View view, LayoutInflater layoutInflater) {
    ViewGroup mainDemoContainer = view.findViewById(R.id.cat_demo_landing_main_demo_container);
    ViewGroup additionalDemosSection =
        view.findViewById(R.id.cat_demo_landing_additional_demos_section);
    ViewGroup additionalDemosContainer =
        view.findViewById(R.id.cat_demo_landing_additional_demos_container);

    mainDemoContainer.removeAllViews();
    additionalDemosContainer.removeAllViews();

    String defaultDemoClassName = FeatureDemoUtils.getDefaultDemo(requireContext());
    addDemoView(layoutInflater, mainDemoContainer, getMainDemo(), false, defaultDemoClassName);
    List<Demo> additionalDemos = getAdditionalDemos();
    for (Demo additionalDemo : additionalDemos) {
      addDemoView(
          layoutInflater, additionalDemosContainer, additionalDemo, true, defaultDemoClassName);
    }
    additionalDemosSection.setVisibility(additionalDemos.isEmpty() ? View.GONE : View.VISIBLE);
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
      LayoutInflater layoutInflater,
      ViewGroup demoContainer,
      Demo demo,
      boolean isAdditional,
      String defaultDemoClassName) {
    View demoView = layoutInflater.inflate(R.layout.cat_demo_landing_row, demoContainer, false);

    View rootView = demoView.findViewById(R.id.cat_demo_landing_row_root);
    View titlesView = demoView.findViewById(R.id.cat_demo_landing_row_titles);
    TextView titleTextView = demoView.findViewById(R.id.cat_demo_landing_row_title);
    TextView subtitleTextView = demoView.findViewById(R.id.cat_demo_landing_row_subtitle);
    MaterialButton favoriteButton = demoView.findViewById(R.id.cat_demo_landing_row_favorite);

    String transitionName = getString(demo.getTitleResId());
    ViewCompat.setTransitionName(rootView, transitionName);
    rootView.setOnClickListener(v -> startDemo(demo, v, transitionName));

    titleTextView.setText(demo.getTitleResId());
    String demoClassName = demo.getDemoClassName();
    subtitleTextView.setText(demoClassName);
    favoriteButton.setChecked(defaultDemoClassName.equals(demoClassName));
    favoriteButton.setOnClickListener(
        v -> {
          updateFavoriteDemoLandingPreference(favoriteButton.isChecked());
          updateFavoriteDemoPreference(demo, favoriteButton.isChecked());
          // Make sure the favorite icons in the demo rows and toolbar are in the correct state.
          clearAndAddDemoViews(requireView(), layoutInflater);
          requireActivity().invalidateOptionsMenu();
        });

    if (isAdditional) {
      setMarginStart(titlesView, R.dimen.cat_list_text_margin_from_icon_large);
    }

    demoContainer.addView(demoView);
  }

  private void startDemo(@NonNull Demo demo) {
    startDemo(demo, null, null);
  }

  private void startDemo(
      @NonNull Demo demo, @Nullable View sharedElement, @Nullable String transitionName) {
    Fragment fragment = demo.createFragment();
    if (fragment != null) {
      startDemoFragment(fragment, sharedElement, transitionName);
      return;
    }
    Intent activityIntent = demo.createActivityIntent();
    if (activityIntent != null) {
      startDemoActivity(activityIntent, sharedElement, transitionName);
      return;
    }
    throw new IllegalStateException("Demo must implement createFragment or createActivityIntent");
  }

  private void startDemoFragment(
      Fragment fragment, @Nullable View sharedElement, @Nullable String transitionName) {
    Bundle args = new Bundle();
    args.putString(DemoFragment.ARG_DEMO_TITLE, getString(getTitleResId()));
    args.putString(FeatureDemoUtils.ARG_TRANSITION_NAME, transitionName);
    fragment.setArguments(args);
    FeatureDemoUtils.startFragment(
        getActivity(), fragment, FRAGMENT_DEMO_CONTENT, sharedElement, transitionName);
  }

  private void startDemoActivity(
      Intent intent, @Nullable View sharedElement, @Nullable String transitionName) {
    intent.putExtra(DemoActivity.EXTRA_DEMO_TITLE, getString(getTitleResId()));

    if (sharedElement != null && transitionName != null) {
      intent.putExtra(DemoActivity.EXTRA_TRANSITION_NAME, transitionName);

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

  private void updateFavoriteDemoLandingPreference(boolean isChecked) {
    FeatureDemoUtils.saveDefaultDemoLanding(
        requireContext(), isChecked ? getClass().getName() : "");
  }

  private void updateFavoriteDemoPreference(Demo demo, boolean isChecked) {
    FeatureDemoUtils.saveDefaultDemo(requireContext(), isChecked ? demo.getDemoClassName() : "");
  }

  private void clearFavoriteDemoPreference() {
    FeatureDemoUtils.saveDefaultDemo(requireContext(), "");
  }

  private void startDefaultDemoIfNeeded() {
    String defaultDemo = FeatureDemoUtils.getDefaultDemo(requireContext());
    if (!defaultDemo.isEmpty()) {
      List<Demo> allDemos = new ArrayList<>();
      allDemos.add(getMainDemo());
      allDemos.addAll(getAdditionalDemos());
      for (Demo demo : allDemos) {
        if (demo.getDemoClassName().equals(defaultDemo)) {
          startDemo(demo);
          return;
        }
      }
    }
  }

  private boolean isFavoriteLaunch() {
    Bundle arguments = getArguments();
    if (arguments != null) {
      return arguments.getBoolean(FeatureDemo.KEY_FAVORITE_LAUNCH);
    }
    return false;
  }

  private void setMarginStart(View view, @DimenRes int marginResId) {
    int margin = getResources().getDimensionPixelOffset(marginResId);
    MarginLayoutParams layoutParams = (MarginLayoutParams) view.getLayoutParams();
    layoutParams.setMarginStart(margin);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.mtrl_favorite_menu, menu);
    super.onCreateOptionsMenu(menu, menuInflater);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    MenuItem item = menu.findItem(R.id.favorite_toggle);
    boolean isChecked =
        FeatureDemoUtils.getDefaultDemoLanding(requireContext()).equals(getClass().getName());
    setMenuItemChecked(item, isChecked);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    if (menuItem.getItemId() == R.id.favorite_toggle) {
      boolean isChecked = !menuItem.isChecked();
      updateFavoriteDemoLandingPreference(isChecked);
      clearFavoriteDemoPreference();
      setMenuItemChecked(menuItem, isChecked);
      clearAndAddDemoViews(requireView(), getLayoutInflater());
      return true;
    }

    return super.onOptionsItemSelected(menuItem);
  }

  private void setMenuItemChecked(MenuItem menuItem, boolean isChecked) {
    menuItem.setChecked(isChecked);
    MenuItemCompat.setIconTintList(
        menuItem,
        ColorStateList.valueOf(isChecked ? menuIconColorChecked : menuIconColorUnchecked));
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
