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

package io.material.catalog.tableofcontents;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import com.google.common.collect.ImmutableList;
import io.material.catalog.feature.FeatureDemo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/** Handles individual items in the catalog table of contents. */
final class TocAdapter extends Adapter<TocViewHolder> implements Filterable {

  private final FragmentActivity activity;
  private final List<FeatureDemo> featureDemos;
  private final ImmutableList<FeatureDemo> featureDemosAll;

  private final Filter featureDemoFilter =
      new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
          List<FeatureDemo> filteredList = new ArrayList<>();

          if (constraint.length() == 0) {
            filteredList.addAll(featureDemosAll);
          } else {
            for (FeatureDemo featureDemo : featureDemosAll) {
              if (activity
                  .getString(featureDemo.getTitleResId())
                  .toLowerCase(Locale.ROOT)
                  .contains(constraint.toString().toLowerCase(Locale.ROOT))) {
                filteredList.add(featureDemo);
              }
            }
          }
          FilterResults filterResults = new FilterResults();
          filterResults.values = filteredList;
          return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults filterResults) {
          featureDemos.clear();
          featureDemos.addAll((Collection<? extends FeatureDemo>) filterResults.values);
          notifyDataSetChanged();
        }
      };

  TocAdapter(FragmentActivity activity, List<FeatureDemo> featureDemos) {
    this.activity = activity;
    this.featureDemos = featureDemos;
    this.featureDemosAll = ImmutableList.copyOf(featureDemos);
  }

  @Override
  public TocViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
    return new TocViewHolder(activity, viewGroup);
  }

  @Override
  public void onBindViewHolder(TocViewHolder tocViewHolder, int i) {
    tocViewHolder.bind(activity, featureDemos.get(i));
  }

  @Override
  public int getItemCount() {
    return featureDemos.size();
  }

  @Override
  public Filter getFilter() {
    return featureDemoFilter;
  }
}
