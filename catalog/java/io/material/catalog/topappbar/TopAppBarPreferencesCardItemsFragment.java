/*
 * Copyright 2026 The Android Open Source Project
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

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;
import com.google.android.material.color.MaterialColors;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;

/** A fragment that displays a preferences Top App Bar demo for the Catalog app. */
public class TopAppBarPreferencesCardItemsFragment extends DemoFragment {

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    setHasOptionsMenu(true);
  }

  @Nullable
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(R.layout.cat_topappbar_preferences_fragment, viewGroup, false);

    Toolbar toolbar = view.findViewById(R.id.toolbar);
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(toolbar);

    view.findViewById(R.id.coordinator)
        .setBackgroundColor(MaterialColors.getColor(view, com.google.android.material.R.attr.colorSurfaceContainerHigh));

    getChildFragmentManager()
        .beginTransaction()
        .replace(R.id.cat_topappbar_preferences_container, new PreferencesFragment())
        .commit();

    return view;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.cat_topappbar_menu, menu);
    super.onCreateOptionsMenu(menu, menuInflater);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    return DemoUtils.showSnackbar(getActivity(), item) || super.onOptionsItemSelected(item);
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }

  /** Example preferences fragment. */
  public static class PreferencesFragment extends PreferenceFragmentCompat {

    @Nullable private Context themedContext = null;

    @Nullable
    @Override
    public Context getContext() {
      Context context = super.getContext();
      if (context == null) {
        return null;
      }
      if (themedContext == null) {
        themedContext =
            new ContextThemeWrapper(context, R.style.ThemeOverlay_Preferences_CardItems);
      }
      return themedContext;
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @NonNull String rootKey) {
      setPreferencesFromResource(R.xml.cat_topappbar_card_item_preferences, rootKey);
    }

    @NonNull
    @Override
    protected RecyclerView.Adapter<PreferenceViewHolder> onCreateAdapter(@NonNull PreferenceScreen preferenceScreen) {
      return new CardItemPreferenceGroupAdapter(preferenceScreen);
    }

    @SuppressWarnings("RestrictTo")
    private static class CardItemPreferenceGroupAdapter extends PreferenceGroupAdapter {

      private final int[] firstStateSet = {android.R.attr.state_first};
      private final int[] middleStateSet = {android.R.attr.state_middle};
      private final int[] lastStateSet = {android.R.attr.state_last};
      private final int[] singleStateSet = {android.R.attr.state_single};

      private final int verticalSpace;
      private final int listBottomMargin;

      public CardItemPreferenceGroupAdapter(@NonNull PreferenceGroup preferenceGroup) {
        super(preferenceGroup);

        Resources resources = preferenceGroup.getContext().getResources();
        verticalSpace =
            resources.getDimensionPixelSize(
                R.dimen.cat_topappbar_preference_card_item_vertical_space);
        listBottomMargin =
            resources.getDimensionPixelSize(
                R.dimen.cat_topappbar_preference_card_item_list_bottom_margin);
      }

      @Override
      public void onBindViewHolder(@NonNull PreferenceViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        Preference currentItem = getItem(position);

        if (currentItem instanceof PreferenceCategory) {
          return;
        }

        Preference previousItem = position == 0 ? null : getItem(position - 1);
        Preference nextItem = position == getItemCount() - 1 ? null : getItem(position + 1);
        boolean first = previousItem == null || previousItem instanceof PreferenceCategory;
        boolean last = nextItem == null || nextItem instanceof PreferenceCategory;

        DrawableStateLinearLayout linearLayout = (DrawableStateLinearLayout) holder.itemView;
        MarginLayoutParams layoutParams = (MarginLayoutParams) linearLayout.getLayoutParams();

        if (first && last) {
          layoutParams.topMargin = 0;
          layoutParams.bottomMargin = 0;
          linearLayout.setExtraDrawableState(singleStateSet);
        } else if (first) {
          layoutParams.topMargin = 0;
          layoutParams.bottomMargin = verticalSpace;
          linearLayout.setExtraDrawableState(firstStateSet);
        } else if (last) {
          layoutParams.topMargin = verticalSpace;
          layoutParams.bottomMargin = 0;
          linearLayout.setExtraDrawableState(lastStateSet);
        } else {
          layoutParams.topMargin = verticalSpace;
          layoutParams.bottomMargin = verticalSpace;
          linearLayout.setExtraDrawableState(middleStateSet);
        }

        if (nextItem == null) {
          layoutParams.bottomMargin = listBottomMargin;
        }
      }
    }
  }
}
