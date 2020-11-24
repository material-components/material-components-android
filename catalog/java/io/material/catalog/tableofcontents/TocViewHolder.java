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

import io.material.catalog.R;

import androidx.fragment.app.FragmentActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import io.material.catalog.feature.FeatureDemo;
import io.material.catalog.feature.FeatureDemoUtils;

/** Creates the UI for a single item in the catalog table of contents. */
class TocViewHolder extends ViewHolder {

  private static final String FRAGMENT_CONTENT = "fragment_content";

  private final TextView titleView;
  private final ImageView imageView;
  private final TextView statusWipLabelView;

  TocViewHolder(FragmentActivity activity, ViewGroup viewGroup) {
    super(
        LayoutInflater.from(activity)
            .inflate(R.layout.cat_toc_item, viewGroup, false /* attachToRoot */));

    titleView = itemView.findViewById(R.id.cat_toc_title);
    imageView = itemView.findViewById(R.id.cat_toc_image);
    statusWipLabelView = itemView.findViewById(R.id.cat_toc_status_wip_label);
  }

  void bind(FragmentActivity activity, FeatureDemo featureDemo) {
    String transitionName = activity.getString(featureDemo.getTitleResId());
    ViewCompat.setTransitionName(itemView, transitionName);
    titleView.setText(featureDemo.getTitleResId());
    imageView.setImageResource(featureDemo.getDrawableResId());
    itemView.setOnClickListener(
        v ->
            FeatureDemoUtils.startFragment(
                activity, featureDemo.createFragment(), FRAGMENT_CONTENT, v, transitionName));
    statusWipLabelView.setVisibility(
        featureDemo.getStatus() == FeatureDemo.STATUS_WIP ? View.VISIBLE : View.GONE);
  }
}
