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

package io.material.catalog.tabs;

import io.material.catalog.R;

import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.core.view.ViewCompat;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import androidx.annotation.ArrayRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.LabelVisibility;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** The main fragment that displays tabs demos for the Catalog app. */
public class TabsControllableDemoFragment extends DemoFragment {

  private static final int TAB_COUNT = 3;
  @DrawableRes private static final int ICON_DRAWABLE_RES = R.drawable.ic_tabs_24px;
  @StringRes private static final int LABEL_STRING_RES = R.string.cat_tab_item_label;

  private boolean showIcons = true;
  private boolean showLabels = true;
  private List<TabLayout> tabLayouts;
  private ViewPager pager;

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_tabs_controllable_fragment, viewGroup, false /* attachToRoot */);

    ViewGroup content = view.findViewById(R.id.content);
    View tabsContent = layoutInflater.inflate(getTabsContent(), content, false /* attachToRoot */);
    content.addView(tabsContent, 0);

    tabLayouts = DemoUtils.findViewsWithType(view, TabLayout.class);
    pager = view.findViewById(R.id.viewpager);

    CoordinatorLayout coordinatorLayout = view.findViewById(R.id.coordinator_layout);
    ViewCompat.setOnApplyWindowInsetsListener(
        view,
        (v, insetsCompat) -> {
          View scrollable = coordinatorLayout.findViewById(R.id.cat_tabs_controllable_scrollview);
          scrollable.setPadding(
              scrollable.getPaddingLeft(),
              0,
              scrollable.getPaddingRight(),
              scrollable.getPaddingBottom());
          return insetsCompat;
        });

    setupViewPager();
    setAllTabLayoutIcons(ICON_DRAWABLE_RES);
    setAllTabLayoutText(LABEL_STRING_RES);
    setAllTabLayoutBadges();

    SwitchCompat iconsToggle = view.findViewById(R.id.toggle_icons_switch);
    iconsToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          showIcons = isChecked;
          setAllTabLayoutIcons(ICON_DRAWABLE_RES);
        });

    SwitchCompat labelsToggle = view.findViewById(R.id.toggle_labels_switch);
    labelsToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          showLabels = isChecked;
          if (isChecked) {
            for (TabLayout tabLayout : tabLayouts) {
              setLabelVisibility(tabLayout, TabLayout.TAB_LABEL_VISIBILITY_LABELED);
            }
          } else {
            for (TabLayout tabLayout : tabLayouts) {
              setLabelVisibility(tabLayout, TabLayout.TAB_LABEL_VISIBILITY_UNLABELED);
            }
          }
        });

    RadioButton tabGravityFillButton = view.findViewById(R.id.tabs_gravity_fill_button);
    tabGravityFillButton.setOnClickListener(v -> setAllTabLayoutGravity(TabLayout.GRAVITY_FILL));

    RadioButton tabGravityCenterButton = view.findViewById(R.id.tabs_gravity_center_button);
    tabGravityCenterButton.setOnClickListener(
        v -> setAllTabLayoutGravity(TabLayout.GRAVITY_CENTER));

    RadioButton tabAnimationModeLinearButton =
        view.findViewById(R.id.tabs_animation_mode_linear_button);
    tabAnimationModeLinearButton.setOnClickListener(
        v -> setAllTabAnimationModes(TabLayout.INDICATOR_ANIMATION_MODE_LINEAR));

    RadioButton tabsAnimationModeElasticButton =
        view.findViewById(R.id.tabs_animation_mode_elastic_button);
    tabsAnimationModeElasticButton.setOnClickListener(
        v -> setAllTabAnimationModes(TabLayout.INDICATOR_ANIMATION_MODE_ELASTIC));

    SwitchCompat inlineToggle = view.findViewById(R.id.toggle_inline_switch);
    inlineToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> setAllTabLayoutInline(isChecked));

    Spinner selectedIndicatorSpinner = (Spinner) view.findViewById(R.id.selector_spinner);
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(
            selectedIndicatorSpinner.getContext(),
            getSelectedIndicatorDrawableTitles(),
            android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    selectedIndicatorSpinner.setAdapter(adapter);

    selectedIndicatorSpinner.setOnItemSelectedListener(
        new OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            setAllTabLayoutSelectedIndicators(position);
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {
            setAllTabLayoutSelectedIndicators(0);
          }
        });

    return view;
  }

  @ArrayRes
  protected int getSelectedIndicatorDrawableTitles() {
    return R.array.cat_tabs_selected_indicator_drawable_titles;
  }

  @ArrayRes
  protected int getSelectedIndicatorDrawables() {
    return R.array.cat_tabs_selected_indicator_drawables;
  }

  @ArrayRes
  protected int getSelectedIndicatorDrawableGravities() {
    return R.array.cat_tabs_selected_indicator_drawable_gravities;
  }

  @LayoutRes
  protected int getTabsContent() {
    return R.layout.cat_tabs_controllable_content;
  }

  private void setupViewPager() {
    pager.setAdapter(new TabsPagerAdapter(getChildFragmentManager(), getContext(), TAB_COUNT));
    for (TabLayout tabLayout : tabLayouts) {
      tabLayout.setupWithViewPager(pager);
    }
  }

  private void setAllTabLayoutIcons(@DrawableRes int iconResId) {
    for (TabLayout tabLayout : tabLayouts) {
      setTabLayoutIcons(tabLayout, iconResId);
    }
  }

  private void setTabLayoutIcons(TabLayout tabLayout, @DrawableRes int iconResId) {
    for (int i = 0; i < tabLayout.getTabCount(); i++) {
      if (showIcons) {
        tabLayout.getTabAt(i).setIcon(iconResId);
      } else {
        tabLayout.getTabAt(i).setIcon(null);
      }
    }
  }

  private void setAllTabLayoutText(@StringRes int stringResId) {
    for (TabLayout tabLayout : tabLayouts) {
      setTabLayoutText(tabLayout, stringResId);
    }
  }

  private void setTabLayoutText(TabLayout tabLayout, @StringRes int stringResId) {
    for (int i = 0; i < tabLayout.getTabCount(); i++) {
      // Convert tab index (zero-based) to readable tab label starting at 1.
      tabLayout.getTabAt(i).setText(getResources().getString(stringResId, i + 1));
    }
  }

  private void setAllTabLayoutBadges() {
    for (TabLayout tabLayout : tabLayouts) {
      setupBadging(tabLayout);
      tabLayout.addOnTabSelectedListener(
          new OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
              tab.removeBadge();
            }

            @Override
            public void onTabUnselected(Tab tab) {}

            @Override
            public void onTabReselected(Tab tab) {
              tab.removeBadge();
            }
          });
    }
  }

  private void setupBadging(TabLayout tabLayout) {
    BadgeDrawable badgeDrawable = tabLayout.getTabAt(0).getOrCreateBadge();
    badgeDrawable.setVisible(true);
    badgeDrawable.setNumber(1);

    badgeDrawable = tabLayout.getTabAt(1).getOrCreateBadge();
    badgeDrawable.setVisible(true);
    badgeDrawable.setNumber(88);

    badgeDrawable = tabLayout.getTabAt(2).getOrCreateBadge();
    badgeDrawable.setVisible(true);
    badgeDrawable.setNumber(999);
  }

  private void setLabelVisibility(TabLayout tabLayout, @LabelVisibility int mode) {
     for (int i = 0; i < tabLayout.getTabCount(); i++) {
      tabLayout.getTabAt(i).setTabLabelVisibility(mode);
    }
  }

  private void setAllTabLayoutGravity(int gravity) {
    for (TabLayout tabLayout : tabLayouts) {
      tabLayout.setTabGravity(gravity);
    }
  }

  private void setAllTabAnimationModes(int mode) {
    for (TabLayout tabLayout : tabLayouts) {
      tabLayout.setTabIndicatorAnimationMode(mode);
    }
  }

  private void setAllTabLayoutInline(boolean inline) {
    for (TabLayout tabLayout : tabLayouts) {
      tabLayout.setInlineLabel(inline);
    }
  }

  private void setAllTabLayoutSelectedIndicators(int position) {
    TypedArray drawables = getResources().obtainTypedArray(getSelectedIndicatorDrawables());
    @DrawableRes int drawableResId = drawables.getResourceId(position, 0);
    drawables.recycle();

    TypedArray drawableGravities =
        getResources().obtainTypedArray(getSelectedIndicatorDrawableGravities());
    int drawableGravity = drawableGravities.getInt(position, 0);
    drawableGravities.recycle();

    for (TabLayout tabLayout : tabLayouts) {
      tabLayout.setSelectedTabIndicator(drawableResId);
      tabLayout.setSelectedTabIndicatorGravity(drawableGravity);
    }
  }
}
